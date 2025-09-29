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
