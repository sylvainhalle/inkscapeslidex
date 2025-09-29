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
import java.util.List;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uqac.lif.bullwinkle.BnfParser;
import ca.uqac.lif.bullwinkle.BnfParser.InvalidGrammarException;
import ca.uqac.lif.bullwinkle.BnfParser.ParseException;
import ca.uqac.lif.bullwinkle.ParseNode;
import ca.uqac.lif.bullwinkle.ParseTreeObjectBuilder.BuildException;

public class CommandInterpreter extends SvgProcessor
{
	protected final List<LayerOccurrence> m_currentLayers;
	
	protected final List<LayerOccurrence> m_templateLayers;
	
	protected final List<Command> m_commands;
	
	public CommandInterpreter(Document doc) throws CommandException
	{
		super(doc);
		m_currentLayers = new ArrayList<LayerOccurrence>();
		m_templateLayers = new ArrayList<LayerOccurrence>();
		m_commands = new ArrayList<Command>();
		try
		{
			Node n = (Node) m_xpath.compile("//g[@groupmode='layer' and @label='content']").evaluate(doc, XPathConstants.NODE);
			parseInstructions(n);
		}
		catch (XPathExpressionException e)
		{
			throw new CommandException(e);
		}
	}
	
	protected void parseInstructions(Node n) throws CommandException
	{
		BnfParser parser;
		try
		{
			parser = new BnfParser(CommandInterpreter.class.getResourceAsStream("grammar.bnf"));
		}
		catch (InvalidGrammarException e)
		{
			throw new CommandException(e);
		}
		NodeList lines = null;
		try
		{
			lines = (NodeList) m_xpath.compile("text/tspan[text()]").evaluate(n, XPathConstants.NODESET);
		}
		catch (XPathExpressionException e)
		{
			throw new CommandException(e);
		}
		for (int i = 0; i < lines.getLength(); i++)
		{
			Node n_line = lines.item(i);
			String line = n_line.getTextContent().trim();
			ParseNode tree;
			if (line.isEmpty())
			{
				continue;
			}
			try
			{
				tree = parser.parse(line);
			}
			catch (ParseException e)
			{
				throw new CommandException(e);
			}
			CommandBuilder builder = new CommandBuilder();
			Command c;
			try
			{
				c = builder.build(tree);
				m_commands.add(c);
			}
			catch (BuildException e)
			{
				throw new CommandException(e);
			}
		}
	}
	
	public List<Document> getSlides()
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
		for (LayerOccurrence lo : slide)
		{
			lo.m_order = getIndex(lo.getName());
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
	
	protected int getIndex(String layer_name)
	{
		return m_layers.get(layer_name).getIndex();
	}
	
	public List<LayerOccurrence> getCurrentLayers()
	{
		return m_currentLayers;
	}
	
	public List<LayerOccurrence> getTemplateLayers()
	{
		return m_templateLayers;
	}
	
	public void setCurrentLayers(List<LayerOccurrence> layers)
	{
		m_currentLayers.clear();
		m_currentLayers.addAll(layers);
	}
	
	public void addCurrentLayers(List<LayerOccurrence> layers)
	{
		m_currentLayers.addAll(layers);
	}
	
	public void deleteCurrentLayers(List<LayerOccurrence> layers)
	{
		m_currentLayers.removeAll(layers);
	}
	
	public void setTemplateLayers(List<LayerOccurrence> layers)
	{
		m_templateLayers.clear();
		m_templateLayers.addAll(layers);
	}
	
	public void outputSlide() throws XPathExpressionException
	{
		Slide slide = new Slide();
		slide.addAll(m_templateLayers);
		slide.addAll(m_currentLayers);
		m_slides.add(getSvgSlide(slide));
	}
	
	public void interpret() throws CommandException
	{
		for (Command c : m_commands)
		{
			c.interpret(this);
		}
	}
}
