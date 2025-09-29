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

public class InkscapeRunnable implements Runnable
{
	protected final String m_fileContents;
	
	protected final StatusCallback m_callback;
	
	protected byte[] m_pdfBytes = null;
	
	protected final String m_inkPath; 
	
	public InkscapeRunnable(String ink_path, String file_contents, StatusCallback callback)
	{
		super();
		m_fileContents = file_contents;
		m_callback = callback;
		m_inkPath = ink_path;
	}
	
	public byte[] getPdfBytes()
	{
		return m_pdfBytes;
	}
	
	@Override
	public void run()
	{
		CommandRunner runner = new CommandRunner(new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=pdf"}, m_fileContents);
		runner.run();
		m_pdfBytes = runner.getBytes();
		m_callback.done();
	}

}
