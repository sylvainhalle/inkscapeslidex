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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AnimatedBitmapExporter implements Exporter
{
	/**
	 * The ImageMagick command to use.
	 */
	public static final String MAGICK_COMMAND = "convert";
	
	/**
	 * The number of frames per second in the output animation.
	 */
	protected final int m_fps;
	
	/**
	 * The output stream where to write the animated PNG.
	 */
	protected final OutputStream m_os;
	
	/**
	 * The image format to use for the output animation.
	 */
	protected final Format m_format;
	
	/**
	 * The supported output formats.
	 */
	public static enum Format
	{
		APNG,
		GIF
	}

	public AnimatedBitmapExporter(Format f, int fps, OutputStream os)
	{
		super();
		m_fps = fps;
		m_os = os;
		m_format = f;
	}

	@Override
	public void export(List<byte[]> images) throws Exception
	{
		// Write each image to a temporary file
		List<File> files = new ArrayList<>();
		for (int slide_nb = 0; slide_nb < images.size(); slide_nb++)
		{
			byte[] png_bytes = images.get(slide_nb);
			File tmp_file = File.createTempFile("slide_" + slide_nb + "_", ".png");
			tmp_file.deleteOnExit();
			Files.write(tmp_file.toPath(), png_bytes);
			files.add(tmp_file);
		}
		// Call ImageMagick to create the animated PNG
		File output_file = File.createTempFile("animated_", m_format == Format.APNG ? ".png" : ".gif");
		output_file.deleteOnExit();
		List<String> command = new ArrayList<>();
		command.add(MAGICK_COMMAND);
		command.add("-dispose");
		command.add("Background");
		command.add("-background");
		command.add("none");
		command.add("-delay");
		command.add(100 / m_fps + "");
		for (File f : files)
		{
			command.add(f.getAbsolutePath());
		}
		command.add("-loop");
		command.add("0");
		command.add(output_file.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(command);
		Process process = pb.start();
		int exit_code = process.waitFor();
		if (exit_code != 0)
		{
			System.err.println(new String(process.getErrorStream().readAllBytes()));
			throw new IOException("ImageMagick process returned error code " + exit_code);
		}
		// Read the output animated PNG/GIF and write it to the output stream
		byte[] animated_bitmap_bytes = Files.readAllBytes(output_file.toPath());
		m_os.write(animated_bitmap_bytes);
		m_os.flush();
	}
}
