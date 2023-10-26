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
