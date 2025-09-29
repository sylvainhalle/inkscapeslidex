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
