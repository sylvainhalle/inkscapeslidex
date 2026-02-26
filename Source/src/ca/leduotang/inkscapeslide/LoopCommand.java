package ca.leduotang.inkscapeslide;

import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

public abstract class LoopCommand implements Command
{
	protected final LayerOccurrence m_layerPattern;
	
	public LoopCommand(LayerOccurrence lo)
	{
		super();
		m_layerPattern = lo;
	}
	
	public static class AdditiveLoop extends LoopCommand
	{
		public AdditiveLoop(LayerOccurrence lo)
		{
			super(lo);
		}
		
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			String pattern = m_layerPattern.m_name;
			List<String> all_layer_names = interpreter.getAllLayerNames();
			Collections.sort(all_layer_names);
			for (String lname : all_layer_names)
			{
				if (lname.matches(pattern))
				{
					interpreter.addCurrentLayers(Collections.singletonList(new LayerOccurrence(lname, m_layerPattern.m_alpha, 0)));
					try
					{
						interpreter.outputSlide();
					}
					catch (XPathExpressionException e)
					{
						throw new CommandException(e, -1);
					}
				}
			}		
		}
	}
	
	public static class NormalLoop extends LoopCommand
	{
		public NormalLoop(LayerOccurrence lo)
		{
			super(lo);
		}
		
		@Override
		public void interpret(CommandInterpreter interpreter) throws CommandException
		{
			String pattern = m_layerPattern.m_name;
			List<String> all_layer_names = interpreter.getAllLayerNames();
			Collections.sort(all_layer_names);
			for (String lname : all_layer_names)
			{
				if (lname.matches(pattern))
				{
					interpreter.setCurrentLayers(Collections.singletonList(new LayerOccurrence(lname, m_layerPattern.m_alpha, 0)));
				}
				try
				{
					interpreter.outputSlide();
				}
				catch (XPathExpressionException e)
				{
					throw new CommandException(e, -1);
				}
			}		
		}
	}
}
