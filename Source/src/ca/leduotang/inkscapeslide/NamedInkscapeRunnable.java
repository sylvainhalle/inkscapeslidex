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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NamedInkscapeRunnable extends InkscapeRunnable
{
	protected final String m_filename;
	
	protected final String m_exportType;
	
	public NamedInkscapeRunnable(String ink_path, String export_type, String filename, String file_contents, StatusCallback callback)
	{
		super(ink_path, file_contents, callback);
		m_filename = filename;
		m_exportType = export_type;
	}
	
	@Override
	public void run()
	{
		String[] arguments;
		if (m_exportType.compareToIgnoreCase("PDF") == 0)
		{
			arguments = new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=" + m_exportType};
		}
		else
		{
			// Assume PNG exported at 600 dpi
			arguments = new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=" + m_exportType, "--export-dpi=600"};
		}
		CommandRunner runner = new CommandRunner(arguments, m_fileContents);
		runner.run();
		m_pdfBytes = runner.getBytes();
		File output_file = new File(m_filename);
		try (FileOutputStream outputStream = new FileOutputStream(output_file)) 
		{
	    outputStream.write(m_pdfBytes);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_callback.done();
	}
}
