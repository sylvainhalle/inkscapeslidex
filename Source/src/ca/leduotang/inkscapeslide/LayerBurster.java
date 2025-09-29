package ca.leduotang.inkscapeslide;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class LayerBurster extends SvgProcessor
{
	public LayerBurster(Document doc) throws CommandException
	{
		super(doc);
	}

	public Map<String,Document> getLayers() throws XPathExpressionException
	{
		Map<String,Document> layers = new HashMap<String,Document>();
		for (String name : m_layers.keySet())
		{
			Document d = getSvgSlide(name);
			layers.put(name, d);
		}
		return layers;
	}

	protected Document getSvgSlide(String layer_name) throws XPathExpressionException
	{
		Document new_doc = s_builder.newDocument();
		Node orig_root = (Node) m_xpath.compile("svg").evaluate(m_document, XPathConstants.NODE);
		Node svg_root = orig_root.cloneNode(false);
		new_doc.adoptNode(svg_root);
		new_doc.appendChild(svg_root);
		{
			Node orig = (Node) m_xpath.compile("svg/defs").evaluate(m_document, XPathConstants.NODE);
			Node new_n = orig.cloneNode(true);
			new_doc.adoptNode(new_n);
			svg_root.appendChild(new_n);
		}
		{
			Node orig = (Node) m_xpath.compile("svg/namedview").evaluate(m_document, XPathConstants.NODE);
			Node new_n = orig.cloneNode(true);
			new_doc.adoptNode(new_n);
			svg_root.appendChild(new_n);
		}
		{
			Node orig = (Node) m_xpath.compile("svg/metadata").evaluate(m_document, XPathConstants.NODE);
			if (orig != null)
			{
				Node new_n = orig.cloneNode(true);
				new_doc.adoptNode(new_n);
				svg_root.appendChild(new_n);
			}
		}
		Layer l = m_layers.get(layer_name);
		Element new_n = (Element) l.getContent().cloneNode(true);
		new_n.setAttribute("style", "");
		new_doc.adoptNode(new_n);
		svg_root.appendChild(new_n);
		return new_doc;
	}
}
