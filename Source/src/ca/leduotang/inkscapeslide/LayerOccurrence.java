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

public class LayerOccurrence implements Comparable<LayerOccurrence>
{
	protected final String m_name;

	protected final float m_alpha;

	protected int m_order;

	public LayerOccurrence(String name, float alpha, int order)
	{
		super();
		m_name = name;
		m_alpha = alpha;
		m_order = order;
	}

	public String getName()
	{
		return m_name;
	}

	public float getAlpha()
	{
		return m_alpha;
	}

	@Override
	public String toString()
	{
		return m_name + (m_alpha < 1 ? "*" + m_alpha : "");
	}

	@Override
	public int compareTo(LayerOccurrence o)
	{
		if (m_order == o.m_order)
		{
			return 0;
		}
		if (m_order > o.m_order)
		{
			return 1;
		}
		return -1;
	}
}