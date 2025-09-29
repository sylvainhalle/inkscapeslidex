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
import java.util.List;

import ca.leduotang.inkscapeslide.LayerCommand.AdditiveSlide;
import ca.leduotang.inkscapeslide.LayerCommand.NormalSlide;
import ca.leduotang.inkscapeslide.LayerCommand.SetTemplate;
import ca.leduotang.inkscapeslide.LayerCommand.SubtractiveSlide;
import ca.uqac.lif.bullwinkle.Builds;
import ca.uqac.lif.bullwinkle.ParseTreeObjectBuilder;

public class CommandBuilder extends ParseTreeObjectBuilder<Command>
{
	@Builds(rule = "<comment>", pop = true)
	public Comment buildComment(Object ... parts)
	{
		return new Comment();
	}
	
	@Builds(rule = "<alpha>", pop = true)
	public float buildAlpha(Object ... parts)
	{
		return Float.parseFloat(parts[0].toString().trim());
	}
	
	@Builds(rule = "<layername>", pop = true)
	public String buildLayerName(Object ... parts)
	{
		return parts[0].toString();
	}
	
	@Builds(rule = "<slidename>", pop = true)
	public String buildSlideName(Object ... parts)
	{
		return parts[0].toString();
	}
	
	@Builds(rule = "<layer>", pop = true, clean = true)
	public LayerOccurrence buildLayerOccurrence(Object ... parts)
	{
		float alpha = 1;
		if (parts.length == 2)
		{
			alpha = (Float) parts[1];
		}
		String layername = (String) parts[0];
		return new LayerOccurrence(layername, alpha, 0);
	}
	
	@SuppressWarnings("unchecked")
	@Builds(rule = "<layers>", pop = true, clean = true)
	public List<LayerOccurrence> buildLayers(Object ... parts)
	{
		LayerOccurrence lo = (LayerOccurrence) parts[0];
		List<LayerOccurrence> s;
		if (parts.length == 2)
		{
			s = (List<LayerOccurrence>) parts[1];
		}
		else
		{
			s = new ArrayList<LayerOccurrence>();
		}
		s.add(lo);
		return s;
	}
	
	@SuppressWarnings("unchecked")
	@Builds(rule = "<normal>", pop = true, clean = true)
	public NormalSlide buildNormal(Object ... parts)
	{
		NormalSlide c = new NormalSlide();
		List<LayerOccurrence> s = (List<LayerOccurrence>) parts[0];
		c.addLayers(s);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Builds(rule = "<additive>", pop = true, clean = true)
	public AdditiveSlide buildAdditive(Object ... parts)
	{
		AdditiveSlide c = new AdditiveSlide();
		List<LayerOccurrence> s = (List<LayerOccurrence>) parts[0];
		c.addLayers(s);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Builds(rule = "<subtractive>", pop = true, clean = true)
	public SubtractiveSlide buildSubtractive(Object ... parts)
	{
		SubtractiveSlide c = new SubtractiveSlide();
		List<LayerOccurrence> s = (List<LayerOccurrence>) parts[0];
		c.addLayers(s);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Builds(rule = "<template>", pop = true, clean = true)
	public SetTemplate buildTemplate(Object ... parts)
	{
		List<LayerOccurrence> s = (List<LayerOccurrence>) parts[0];
		SetTemplate set = new SetTemplate();
		set.addLayers(s);
		return set;
	}
	
	
}
