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

public class StatusCallback
{
	protected static final int s_barWidth = 32;
	
	protected int m_currentlyDone;
	
	protected final int m_total;
	
	protected final AnsiPrinter m_out;
	
	public StatusCallback(AnsiPrinter out, int total)
	{
		super();
		m_out = out;
		m_total = total;
		printBar();
	}
	
	public synchronized void done()
	{
		m_currentlyDone++;
		printBar();
	}
	
	protected synchronized void printBar()
	{
		m_out.print("\r\033[2K");
		m_out.print("[");
		int chars = (int) Math.ceil(((float) m_currentlyDone / (float) m_total) * s_barWidth);
		for (int i = 0; i < chars; i++)
		{
			m_out.print("#");
		}
		for (int i = chars; i < s_barWidth; i++)
		{
			m_out.print(" ");
		}
		m_out.print("] ");
		m_out.print(m_currentlyDone + " / " + m_total);
	}
}
