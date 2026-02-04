/*
    Create slideshows from Inkscape SVG files
    Copyright (C) 2023-2026 Sylvain Hallé

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

/**
 * A Runnable that runs an Inkscape process to convert an SVG file to
 * another format.
 * @author Sylvain Hallé
 */
public class InkscapeRunnable implements Runnable
{
	/**
	 * The contents of the SVG file to convert.
	 */
	protected final String m_fileContents;

	/**
	 * The callback to report status to.
	 */
	protected final StatusCallback m_callback;

	protected byte[] m_outputBytes = null;

	/**
	 * The path to the Inkscape executable.
	 */
	
	protected final String m_inkPath; 

	/**
	 * Whether to export only the first page.
	 */
	protected final boolean m_onlyOnePage;
	
	/**
	 * The output format.
	 */
	protected final String m_outFormat;

	/**
	 * Constructor.
	 * @param ink_path The path to the Inkscape executable
	 * @param file_contents The contents of the SVG file to convert
	 * @param callback The callback to report status to
	 * @param only_one_page Whether to export only the first page
	 * @param out_format The output format
	 */
	public InkscapeRunnable(String ink_path, String file_contents, StatusCallback callback, boolean only_one_page, String out_format)
	{
		super();
		m_fileContents = file_contents;
		m_callback = callback;
		m_inkPath = ink_path;
		m_onlyOnePage = only_one_page;
		m_outFormat = out_format.toLowerCase();
	}

	/**
	 * Gets the output bytes produced by Inkscape.
	 * @return The output bytes
	 */
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
		String out_format = m_outFormat;
		if (m_outFormat.compareToIgnoreCase("gif") == 0)
		{
			// Inkscape does not support GIF export; use PNG instead and convert later
			out_format = "png";
		}
		{
			runner = new CommandRunner(new String[] {m_inkPath, "--pipe", "--export-filename=-", "--export-type=" + out_format}, m_fileContents);
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
