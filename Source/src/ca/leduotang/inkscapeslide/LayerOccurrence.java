package ca.leduotang.inkscapeslide;

public class LayerOccurrence implements Comparable<LayerOccurrence>
{
	protected final String m_name;

	protected final float m_alpha;

	protected int m_order;

	public LayerOccurrence(String name, float alpha, int order)
	{
		super();
		m_name = name;
		m_alpha = alpha;
		m_order = order;
	}

	public String getName()
	{
		return m_name;
	}

	public float getAlpha()
	{
		return m_alpha;
	}

	@Override
	public String toString()
	{
		return m_name + (m_alpha < 1 ? "*" + m_alpha : "");
	}

	@Override
	public int compareTo(LayerOccurrence o)
	{
		if (m_order == o.m_order)
		{
			return 0;
		}
		if (m_order > o.m_order)
		{
			return 1;
		}
		return -1;
	}
}