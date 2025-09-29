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

import java.util.ArrayList;
import java.util.Collections;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Slideshow 
{
	public static final String CONTENT = "content";

	protected static DocumentBuilder s_builder;

	protected final XPath m_xpath = XPathFactory.newInstance().newXPath();

	protected final Document m_document;

	protected final Map<String,Layer> m_layers;

	protected final List<Slide> m_slides;

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

	public Slideshow(Document doc) throws XPathExpressionException
	{
		super();
		m_document = doc;
		m_layers = new HashMap<String,Layer>();
		m_slides = new ArrayList<Slide>();
		NodeList list = (NodeList) m_xpath.compile("//g[@groupmode='layer']").evaluate(doc, XPathConstants.NODESET);
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
		Node n = (Node) m_xpath.compile("//g[@groupmode='layer' and @label='content']").evaluate(doc, XPathConstants.NODE);
		analyseContentLayer(n);
	}

	public void addLayer(String name, Node n, int index)
	{
		m_layers.put(name, new Layer(n, index));
	}

	public Layer getLayer(String name)
	{
		return m_layers.get(name);
	}

	public List<Slide> getSlides()
	{
		return m_slides;
	}

	public Document getSvgSlide(Slide slide) throws XPathExpressionException
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
			Node new_n = orig.cloneNode(true);
			new_doc.adoptNode(new_n);
			svg_root.appendChild(new_n);
		}
		Collections.sort(slide);
		for (LayerOccurrence lo : slide)
		{
			if (m_layers.containsKey(lo.getName()))
			{
				Layer l = m_layers.get(lo.getName());
				Element new_n = (Element) l.getContent().cloneNode(true);
				new_n.setAttribute("style", "");
				new_doc.adoptNode(new_n);
				svg_root.appendChild(new_n);
			}
		}
		return new_doc;
	}

	protected void analyseContentLayer(Node n) throws XPathExpressionException
	{
		NodeList lines = (NodeList) m_xpath.compile("text/tspan[text()]").evaluate(n, XPathConstants.NODESET);
		for (int i = 0; i < lines.getLength(); i++)
		{
			Node n_line = lines.item(i);
			String line = n_line.getTextContent();
			boolean additive = false;
			if (line.startsWith("//"))
			{
				continue;
			}
			if (line.startsWith("+"))
			{
				line = line.substring(1);
				additive = true;
			}
			String[] parts = line.split(",");
			Slide slide = new Slide();
			for (String part : parts)
			{
				String name = null;
				float alpha = 1;
				if (part.contains("@"))
				{
					String[] in_parts = part.split("@");
					name = in_parts[0].trim();
					alpha = Float.parseFloat(in_parts[1].trim());
				}
				else
				{
					name = part.trim();
				}
				Layer l = m_layers.get(name);
				if (l == null)
				{
					throw new XPathExpressionException("Layer not found " + name);
				}
				slide.add(new LayerOccurrence(name, alpha, l.getIndex()));
				if (additive && i > 0)
				{
					slide.addAll(m_slides.get(i - 1));
				}
			}
			m_slides.add(slide);
		}
	}
}
