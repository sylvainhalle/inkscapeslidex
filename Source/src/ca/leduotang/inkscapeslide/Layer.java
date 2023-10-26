package ca.leduotang.inkscapeslide;

import org.w3c.dom.Node;

public class Layer
{
	protected final Node m_content;
	
	protected final int m_index;
	
	public Layer(Node content, int index)
	{
		super();
		m_content = content;
		m_index = index;
	}
	
	public int getIndex()
	{
		return m_index;
	}
	
	public Node getContent()
	{
		return m_content;
	}
}
