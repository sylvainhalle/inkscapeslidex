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

import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class SvgPrinter
{
	protected final Transformer m_transformer;
	
	public SvgPrinter() throws TransformerConfigurationException, TransformerFactoryConfigurationError
	{
		super();
		m_transformer = TransformerFactory.newInstance().newTransformer();
		m_transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		m_transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	}
	
	public void print(Document doc, OutputStream os) throws TransformerException
	{
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(os);
		m_transformer.transform(source, result);
	}
}
