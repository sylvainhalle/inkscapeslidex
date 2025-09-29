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

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.w3c.dom.Document;

//import com.itextpdf.text.pdf.PdfReader;

public class Main
{
	public static void main(String[] args) throws TransformerFactoryConfigurationError, Exception
	{
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		AnsiPrinter stdout = new AnsiPrinter(System.out);

		int num_threads = 2;
		String burst = null;
		String ink_path = "inkscape";

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
			burst = map.getOptionValue("burst");
		}
		if (map.hasOption("inkpath"))
		{
			ink_path = map.getOptionValue("inkpath");
		}
		stdout.println(getGreeting());

		// Get list of files to process
		List<String> filenames = map.getOthers();
		if (filenames.isEmpty())
		{
			filenames.add("-"); // stdin
		}
		FileSystem fs = new HardDisk("/").open();

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
				out_filename = filename.replace("svgz", "pdf");
			}
			else
			{
				out_filename = filename.replace("svg", "pdf");
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads);
			OutputStream os = fs.writeTo(out_filename);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(is);
			is.close();
			doc.getDocumentElement().normalize();
			List<InkscapeRunnable> pdfs = new ArrayList<InkscapeRunnable>();
			if (burst != null)
			{
				LayerBurster s = new LayerBurster(doc);
				Map<String,Document> layers = s.getLayers();
				StatusCallback status = new StatusCallback(stdout, layers.size());
				for (Map.Entry<String,Document> e : layers.entrySet())
				{
					if (burst.compareTo("svg") == 0)
					{
						String layer_filename = e.getKey().concat(".svg");
						SvgPrinter printer = new SvgPrinter();
						FileOutputStream fos = new FileOutputStream(layer_filename);
						printer.print(e.getValue(), fos);
						fos.close();
					}
					else if (burst.compareTo("pdf") == 0 || burst.compareTo("png") == 0)
					{
						String layer_filename = e.getKey().concat(".").concat(burst);
						SvgPrinter printer = new SvgPrinter();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						printer.print(e.getValue(), baos);
						InkscapeRunnable inkr = new NamedInkscapeRunnable(ink_path, burst, layer_filename, baos.toString(), status);
						pdfs.add(inkr);
						executor.execute(inkr);
					}
				}
			}
			else
			{
				CommandInterpreter s = new CommandInterpreter(doc);
				s.interpret();
				List<Document> slides = s.getSlides();
				int total = slides.size();
				StatusCallback status = new StatusCallback(stdout, total);
				for (int i = 0; i < slides.size(); i++)
				{
					SvgPrinter printer = new SvgPrinter();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					printer.print(slides.get(i), baos);
					InkscapeRunnable inkr = new InkscapeRunnable(ink_path, baos.toString(), status);
					pdfs.add(inkr);
					executor.execute(inkr);
				}
			}
			executor.shutdown();
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
			if (burst == null)
			{
				PDFMergerUtility PDFmerger = new PDFMergerUtility();
				ByteArrayOutputStream pdf_baos = new ByteArrayOutputStream();
				PDFmerger.setDestinationStream(pdf_baos);
				for (InkscapeRunnable inkr : pdfs)
				{
					PDFmerger.addSource(new ByteArrayInputStream(inkr.getPdfBytes()));
				}
				PDFmerger.mergeDocuments(null);
				// Optimize PDF
				byte[] optimized = DuplicateFontsRemover.normalizeFile(new ByteArrayInputStream(pdf_baos.toByteArray()));
				os.write(optimized);
				os.close();
			}
			stdout.print("\r\033[2K");
			stdout.println("Done");
		}

		// Close file system
		fs.close();
	}

	protected static CliParser setupCli()
	{
		CliParser parser = new CliParser();
		parser.addArgument(new Argument().withLongName("burst").withArgument("format").withDescription("Burst layers into separate files of format"));
		parser.addArgument(new Argument().withLongName("threads").withArgument("n").withDescription("Use up to n threads"));
		parser.addArgument(new Argument().withLongName("inkpath").withArgument("path").withDescription("Set path to Inkscape"));
		parser.addArgument(new Argument().withLongName("help").withDescription("\tShow command usage"));
		return parser;
	}

	protected static String getGreeting()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println("InkscapeSlide X version 0.2");
		ps.println("(C) 2023-2025 Sylvain Hallé, Université du Québec à Chicoutimi, Canada");
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
