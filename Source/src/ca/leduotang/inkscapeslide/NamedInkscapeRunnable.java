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
