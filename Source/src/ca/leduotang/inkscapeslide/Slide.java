package ca.leduotang.inkscapeslide;

import java.util.ArrayList;

public class Slide extends ArrayList<LayerOccurrence>
{
	private static final long serialVersionUID = 1L;
	
	protected String m_name;
	
	protected boolean m_incrementing;

	public Slide(String name)
	{
		super();
		m_name = name;
		m_incrementing = true;
	}
	
	public Slide()
	{
		this(null);
	}
	
	public boolean isIncrementing()
	{
		return m_incrementing;
	}
	
	public void setIncrementing(boolean b)
	{
		m_incrementing = b;
	}
	
	@Override
	public String toString()
	{
		return (m_name != null ? m_name + ":" : "") + super.toString();
	}
	
}
