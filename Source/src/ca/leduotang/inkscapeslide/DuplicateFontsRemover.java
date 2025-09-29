/*
    Create slideshows from Inkscape SVG files
    Copyright (C) 2023-2025 Sylvain Hallé

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
