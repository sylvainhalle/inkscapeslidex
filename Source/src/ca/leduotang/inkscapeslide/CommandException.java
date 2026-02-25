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

public class CommandException extends Exception
{
	/**
	 * The line number in the SVG layer where the error occurred.
	 */
	protected final int m_line;
	
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	public CommandException(Throwable t, int line)
	{
		super(t);
		m_line = line;
	}
	
	/**
	 * Get the line number in the SVG layer where the error occurred.
	 * @return The line number in the SVG layer where the error occurred.
	 */
	public int getLine()
	{
		return m_line;
	}

}
