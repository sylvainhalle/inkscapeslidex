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
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

public abstract class LayerCommand implements Command
{
	protected final List<LayerOccurrence> m_layers;
	
	public LayerCommand()
	{
		super();
		m_layers = new ArrayList<LayerOccurrence>();
	}
	
	public void addLayer(LayerOccurrence lo)
	{
		m_layers.add(lo);
	}
	
	public void addLayers(Collection<LayerOccurrence> col)
	{
		m_layers.addAll(col);
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < m_layers.size(); i++)
		{
			if (i > 0)
			{
				out.append(",");
			}
			out.append(m_layers.get(i).toString());
		}
		return out.toString();
	}
	
	public static class NormalSlide extends LayerCommand
	{
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			interpreter.setCurrentLayers(m_layers);
			try
			{
				interpreter.outputSlide();
			}
			catch (XPathExpressionException e)
			{
				throw new CommandException(e);
			}
		}
	}
	
	public static class AdditiveSlide extends LayerCommand
	{
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			interpreter.addCurrentLayers(m_layers);
			try
			{
				interpreter.outputSlide();
			}
			catch (XPathExpressionException e)
			{
				throw new CommandException(e);
			}
		}
		
		@Override
		public String toString()
		{
			return "+" + super.toString();
		}
	}
	
	public static class SubtractiveSlide extends LayerCommand
	{
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			interpreter.deleteCurrentLayers(m_layers);
			try
			{
				interpreter.outputSlide();
			}
			catch (XPathExpressionException e)
			{
				throw new CommandException(e);
			}
		}
		
		@Override
		public String toString()
		{
			return "-" + super.toString();
		}
	}
	
	public static class SetTemplate extends LayerCommand
	{
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			interpreter.setTemplateLayers(m_layers);
		}
		
		@Override
		public String toString()
		{
			return "@template: " + super.toString();
		}
	}
}
