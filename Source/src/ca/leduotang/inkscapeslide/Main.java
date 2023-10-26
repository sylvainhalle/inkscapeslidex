package ca.leduotang.inkscapeslide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import ca.leduotang.inkscapeslide.CliParser.Argument;
import ca.leduotang.inkscapeslide.CliParser.ArgumentMap;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//import com.itextpdf.text.pdf.PdfReader;

public class Main
{
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, FileSystemException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException
	{
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		AnsiPrinter stdout = new AnsiPrinter(System.out);
		
		int num_threads = 2;
		
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
			OutputStream os = fs.writeTo(out_filename);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(is);
			is.close();
			doc.getDocumentElement().normalize();
			Slideshow s = new Slideshow(doc);
			List<Slide> slides = s.getSlides();
			int total = slides.size();
			List<InkscapeRunnable> pdfs = new ArrayList<InkscapeRunnable>();
			StatusCallback status = new StatusCallback(stdout, total);
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads);
			for (int i = 0; i < slides.size(); i++)
			{
				Slide slide = slides.get(i);
				Document new_doc = s.getSvgSlide(slide);
				SvgPrinter printer = new SvgPrinter();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				printer.print(new_doc, baos);
				InkscapeRunnable inkr = new InkscapeRunnable(baos.toString(), status);
				pdfs.add(inkr);
				executor.execute(inkr);
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
			PDFMergerUtility PDFmerger = new PDFMergerUtility();
			PDFmerger.setDestinationStream(os);
			for (InkscapeRunnable inkr : pdfs)
			{
				PDFmerger.addSource(new ByteArrayInputStream(inkr.getPdfBytes()));
			}
			PDFmerger.mergeDocuments(null);
			stdout.print("\r\033[2K");
			stdout.println("Done");
		}
		
		// Close file system
		fs.close();
	}
	
	/*protected static void optimizePdf()
	{
		com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
		PdfReader pdfReader = null;
    FileOutputStream fileOutputStream = null;
	}*/
	
	protected static CliParser setupCli()
	{
		CliParser parser = new CliParser();
		parser.addArgument(new Argument().withLongName("threads").withArgument("n").withDescription("Use up to n threads"));
		parser.addArgument(new Argument().withLongName("help").withDescription("\tShow command usage"));
		return parser;
	}
	
	protected static String getGreeting()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println("InkscapeSlide X version 0.1");
		ps.println("(C) 2023 Sylvain Hallé, Université du Québec à Chicoutimi, Canada");
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
