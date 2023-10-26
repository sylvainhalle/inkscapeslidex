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
