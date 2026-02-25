/*
    Create slideshows from Inkscape SVG files
    Copyright (C) 2023-2026 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.leduotang.inkscapeslide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import ca.leduotang.inkscapeslide.CliParser.Argument;
import ca.leduotang.inkscapeslide.CliParser.ArgumentMap;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;

import org.w3c.dom.Document;

public class Main
{
	/**
	 * Return code for successful execution.
	 */
	protected static final int RET_OK = 0;
	
	/**
	 * Return code for execution with errors.
	 */
	protected static final int RET_ERROR = 1;
	
	/**
	 * Return code for execution with syntax errors in the SVG layer.
	 */
	protected static final int RET_SYNTAX = 2;
	
	/**
	 * Main method for the command line interface.
	 * @param args Command line arguments
	 * @throws TransformerFactoryConfigurationError
	 * @throws Exception
	 */
	public static void main(String[] args) throws TransformerFactoryConfigurationError, Exception
	{
		int code = doMain(args);
		System.exit(code);
	}
	
	/**
	 * Main method for the command line interface, with error code return.
	 * @param args
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws Exception
	 */
	private static int doMain(String[] args) throws TransformerFactoryConfigurationError, Exception
	{
		int ret_code = RET_OK;
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		AnsiPrinter stdout = new AnsiPrinter(System.out);

		int num_threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		String out_format = "pdf";
		String ink_path = "inkscape";
		boolean burst = false;
		boolean first_page_only = false;

		// Parse CLI arguments
		CliParser parser = setupCli();
		ArgumentMap map = parser.parse(args);
		if (map == null || map.hasOption("help"))
		{
			parser.printHelp(getUsageString(map), stdout);
			System.exit(0);
		}
		if (map.hasOption("threads"))
		{
			num_threads = Integer.parseInt(map.getOptionValue("threads").trim());
		}
		if (map.hasOption("burst"))
		{
			burst = true;
		}
		if (map.hasOption("format"))
		{
			out_format = map.getOptionValue("format");
		}
		if (map.hasOption("inkpath"))
		{
			ink_path = map.getOptionValue("inkpath");
		}
		if (map.hasOption("first-page"))
		{
			first_page_only = true;
		}
		stdout.println(getGreeting());

		// Get list of files to process
		List<String> filenames = map.getOthers();
		if (filenames.isEmpty())
		{
			filenames.add("-"); // stdin
		}
		FileSystem fs = new HardDisk().open();

		// Loop for each file
		for (String filename : filenames)
		{
			InputStream is;
			if (filename.compareTo("-") == 0)
			{
				is = System.in;
			}
			else
			{
				is = fs.readFrom(filename);
			}
			if (is == null)
			{
				stderr.println("File " + filename + " not found");
			}
			String out_filename;
			if (filename.endsWith("svgz"))
			{
				// Zipped svg, unzip first
				GZIPInputStream gzis = new GZIPInputStream(is);
				byte[] unzipped_bytes = FileUtils.toBytes(gzis);
				is.close();
				is = new ByteArrayInputStream(unzipped_bytes);
				out_filename = filename.replace("svgz", out_format);
			}
			else
			{
				out_filename = filename.replace("svg", out_format);
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads);
			OutputStream os = fs.writeTo(out_filename);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(is);
			is.close();
			if (doc == null)
			{
				stderr.println("Could not parse " + filename + " as an XML document");
				continue;
			}
			doc.getDocumentElement().normalize();
			List<InkscapeRunnable> pdfs = new ArrayList<InkscapeRunnable>();
			if (burst)
			{
				LayerBurster s = new LayerBurster(doc);
				Map<String,Document> layers = s.getLayers();
				StatusCallback status = new StatusCallback(stdout, layers.size());
				for (Map.Entry<String,Document> e : layers.entrySet())
				{
					if (out_format.compareTo("svg") == 0)
					{
						String layer_filename = e.getKey().concat(".svg");
						SvgPrinter printer = new SvgPrinter();
						FileOutputStream fos = new FileOutputStream(layer_filename);
						printer.print(e.getValue(), fos);
						fos.close();
					}
					else if (out_format.compareTo("pdf") == 0 || out_format.compareTo("png") == 0)
					{
						String layer_filename = e.getKey().concat(".").concat(out_format);
						SvgPrinter printer = new SvgPrinter();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						printer.print(e.getValue(), baos);
						InkscapeRunnable inkr = new NamedInkscapeRunnable(ink_path, out_format, layer_filename, baos.toString(), status, first_page_only);
						pdfs.add(inkr);
						executor.execute(inkr);
					}
				}
			}
			else
			{
				try
				{
					CommandInterpreter s = new CommandInterpreter(doc);
					s.interpret();
					List<Document> slides = s.getSlides();
					int total = slides.size();
					StatusCallback status = new StatusCallback(stdout, total);
					for (int i = 0; i < slides.size(); i++)
					{
						Document slide_doc = slides.get(i);
						if (first_page_only)
						{
							SvgProcessor.removeInkscapePages(slide_doc);
						}
						SvgPrinter printer = new SvgPrinter();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						printer.print(slide_doc, baos);
						InkscapeRunnable inkr = new InkscapeRunnable(ink_path, baos.toString(), status, first_page_only, out_format);
						pdfs.add(inkr);
						executor.execute(inkr);
					}
				}
				catch (CommandException e)
				{
					stderr.println("Invalid syntax on line " + e.getLine());
					ret_code = RET_SYNTAX;
				}
			}
			executor.shutdown();
			if (ret_code != RET_OK)
			{
				return ret_code;
			}
			try
			{
				if (!executor.awaitTermination(120, TimeUnit.SECONDS))
				{
					executor.shutdownNow();
					if (!executor.awaitTermination(120, TimeUnit.SECONDS))
					{
						stderr.println("Cannot terminate process");
					}
				}
			}
			catch (InterruptedException e)
			{
				// (Re-)Cancel if current thread also interrupted
				executor.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
			if (!burst)
			{
				List<byte[]> output_bytes = new ArrayList<byte[]>(pdfs.size());
				for (int slide_nb = 0; slide_nb < pdfs.size(); slide_nb++)
				{
					InkscapeRunnable inkr = pdfs.get(slide_nb);
					byte[] bytes = inkr.getOutputBytes();
					output_bytes.add(bytes);
				}
				Exporter exporter = null;
				if (out_format.compareTo("png") == 0)
				{
					exporter = new AnimatedBitmapExporter(AnimatedBitmapExporter.Format.APNG, 1, os);
				}
				else if (out_format.compareTo("gif") == 0)
				{
					exporter = new AnimatedBitmapExporter(AnimatedBitmapExporter.Format.GIF, 1, os);
				}
				else if (out_format.compareTo("pdf") == 0)
				{
					exporter = new MultipagePdfExporter(stdout, stderr, os);
				}
				else
				{
					stderr.println("Unsupported output format: " + out_format);
					continue;
				}
				exporter.export(output_bytes);
			}
			os.close();
			stdout.print("\r\033[2K");
			stdout.println("Done");
		}

		// Close everything
		fs.close();
		stderr.close();
		stdout.close();
		return ret_code;
	}

	protected static CliParser setupCli()
	{
		CliParser parser = new CliParser();
		parser.addArgument(new Argument().withLongName("burst").withDescription("Burst layers into separate files"));
		parser.addArgument(new Argument().withLongName("format").withArgument("fmt").withDescription("Set output format (PDF or PNG)"));
		parser.addArgument(new Argument().withLongName("threads").withArgument("n").withDescription("Use up to n threads"));
		parser.addArgument(new Argument().withLongName("inkpath").withArgument("path").withDescription("Set path to Inkscape"));
		parser.addArgument(new Argument().withLongName("help").withDescription("\tShow command usage"));
		parser.addArgument(new Argument().withLongName("first-page").withShortName("1").withDescription("Consider only the first page"));
		return parser;
	}

	protected static String getGreeting()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println("InkscapeSlide X version 0.3");
		ps.println("(C) 2023-2026 Sylvain Hallé, Université du Québec à Chicoutimi, Canada");
		return baos.toString();
	}

	protected static String getUsageString(ArgumentMap map)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.print(getGreeting());
		if (map == null)
		{
			ps.println();
			ps.println("ERROR: Invalid command line argument");
			ps.println();
		}
		ps.print("Usage: inkscapeslidex [options] [filename]");
		return baos.toString();
	}
}
