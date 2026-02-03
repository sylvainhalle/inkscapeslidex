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

	protected byte[] m_outputBytes = null;

	protected final String m_inkPath; 

	protected final boolean m_onlyOnePage;
	
	protected final String m_outFormat;

	public InkscapeRunnable(String ink_path, String file_contents, StatusCallback callback, boolean only_one_page, String out_format)
	{
		super();
		m_fileContents = file_contents;
		m_callback = callback;
		m_inkPath = ink_path;
		m_onlyOnePage = only_one_page;
		m_outFormat = out_format.toLowerCase();
	}

	public byte[] getOutputBytes()
	{
		return m_outputBytes;
	}

	@Override
	public void run()
	{
		CommandRunner runner;
		/*if (m_onlyOnePage)
		{
			runner = new CommandRunner(new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=pdf", "--export-page=1"}, m_fileContents);
		}
		else*/
		{
			runner = new CommandRunner(new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=" + m_outFormat}, m_fileContents);
		}
		runner.run();
		m_outputBytes = runner.getBytes();
		if (m_outputBytes == null || m_outputBytes.length == 0)
		{
			m_callback.error("Inkscape process did not return any data");
			m_callback.done();
			return;
		}
		int code = runner.getErrorCode();
		if (code != 0)
		{
			m_callback.error("Inkscape process returned error code " + code);
		}
		m_callback.done();
	}
	
	

}
