package ca.leduotang.inkscapeslide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class SvgProcessor
{
	public static final String CONTENT = "content";

	protected static DocumentBuilder s_builder;

	protected final XPath m_xpath = XPathFactory.newInstance().newXPath();

	protected final Document m_document;

	protected final Map<String,Layer> m_layers;

	protected final List<Document> m_slides;
	
	public SvgProcessor(Document doc) throws CommandException
	{
		m_document = doc;
		m_layers = new HashMap<String,Layer>();
		m_slides = new ArrayList<Document>();
		NodeList list;
		try
		{
			list = (NodeList) m_xpath.compile("//g[@groupmode='layer']").evaluate(doc, XPathConstants.NODESET);
		}
		catch (XPathExpressionException e)
		{
			throw new CommandException(e);
		}
		for (int i = 0; i < list.getLength(); i++)
		{
			Node n = list.item(i);
			Node n_label = n.getAttributes().getNamedItem("inkscape:label");
			if (n_label != null)
			{
				String label = n_label.getNodeValue();
				if (CONTENT.compareTo(label) != 0)
				{
					m_layers.put(label, new Layer(n, i));
				}
			}
		}
	}
	
	static
	{
		try
		{
			s_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			// Do nothing for now
		}
	}
}
