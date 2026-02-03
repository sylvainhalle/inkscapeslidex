package ca.leduotang.inkscapeslide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

public class MultipagePdfExporter implements Exporter
{
	protected final AnsiPrinter m_stdout;
	
	protected final AnsiPrinter m_stderr;
	
	protected final OutputStream m_os;
	
	public MultipagePdfExporter(AnsiPrinter stdout, AnsiPrinter stderr, OutputStream os)
	{
		super();
		m_stdout = stdout;
		m_stderr = stderr;
		m_os = os;
	}

	@Override
	public void export(List<byte[]> pdfs) throws Exception
	{
	// Merge all PDFs into a single PDF
		PDFMergerUtility PDFmerger = new PDFMergerUtility();
		ByteArrayOutputStream pdf_baos = new ByteArrayOutputStream();
		PDFmerger.setDestinationStream(pdf_baos);
		boolean error_occurred = false;
		for (int slide_nb = 0; slide_nb < pdfs.size(); slide_nb++)
		{
			PDFmerger.addSource(new ByteArrayInputStream(pdfs.get(slide_nb)));
		}
		if (error_occurred)
		{
			m_stderr.println("Aborting PDF creation");
			return;
		}
		PDFmerger.mergeDocuments(null);
		// Optimize PDF
		byte[] optimized = DuplicateFontsRemover.normalizeFile(new ByteArrayInputStream(pdf_baos.toByteArray()));
		m_os.write(optimized);
	}


}
