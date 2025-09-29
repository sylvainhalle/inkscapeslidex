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

public class Slide extends ArrayList<LayerOccurrence>
{
	private static final long serialVersionUID = 1L;
	
	protected String m_name;
	
	protected boolean m_incrementing;

	public Slide(String name)
	{
		super();
		m_name = name;
		m_incrementing = true;
	}
	
	public Slide()
	{
		this(null);
	}
	
	public boolean isIncrementing()
	{
		return m_incrementing;
	}
	
	public void setIncrementing(boolean b)
	{
		m_incrementing = b;
	}
	
	@Override
	public String toString()
	{
		return (m_name != null ? m_name + ":" : "") + super.toString();
	}
	
}
