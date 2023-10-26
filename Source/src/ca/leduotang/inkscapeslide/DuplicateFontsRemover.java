package ca.leduotang.inkscapeslide;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

/**
 * Found on: https://turreta.com/blog/2013/12/13/remove-duplicate-fonts-in-pdf-files/
 */
public class DuplicateFontsRemover
{
	public static byte[] normalizeFile(InputStream is) throws Exception {
    Document pdfDocument = new Document();
    PdfReader pdfReader = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try
    {
      PdfSmartCopy pdfSmartCopy = new PdfSmartCopy(pdfDocument, baos);
      pdfDocument.open();
      pdfReader = new PdfReader(is);

      // Where the magic happens
      for (int i = 1; i <= pdfReader.getNumberOfPages(); i++)
      {
        pdfSmartCopy.addPage(pdfSmartCopy.getImportedPage(pdfReader, i));
      }
      pdfDocument.close();
      return baos.toByteArray();
    } 
    finally
    {
      if (pdfReader != null)
      {
        pdfReader.close();
      }
      if (baos != null)
      {
        baos.close();
      }
      pdfReader = null;
      baos = null;
    }
  }
}
