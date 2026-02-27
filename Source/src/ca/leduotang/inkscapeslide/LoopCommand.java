package ca.leduotang.inkscapeslide;

import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

public abstract class LoopCommand implements Command
{
	protected final String m_layerPattern;
	
	public LoopCommand(String pattern)
	{
		super();
		m_layerPattern = pattern;
	}
	
	protected abstract void handleLayer(String layer_name, CommandInterpreter interpreter) throws XPathExpressionException;
	
	@Override
	public void interpret(CommandInterpreter interpreter) throws CommandException
	{
		List<String> all_layer_names = interpreter.getAllLayerNames();
		Collections.sort(all_layer_names);
		for (String lname : all_layer_names)
		{
			if (lname.matches(m_layerPattern))
			{
				try
				{
					handleLayer(lname, interpreter);
				}
				catch (XPathExpressionException e)
				{
					throw new CommandException(e, -1);
				}
			}
		}		
	}
	
	public static class AdditiveLoop extends LoopCommand
	{
		public AdditiveLoop(String pattern)
		{
			super(pattern);
		}

		@Override
		protected void handleLayer(String layer_name, CommandInterpreter interpreter) throws XPathExpressionException
		{
			interpreter.setCurrentLayers(Collections.singletonList(new LayerOccurrence(layer_name, 1, 0)));
			interpreter.outputSlide();
		}
	}
	
	public static class XorLoop extends LoopCommand
	{
		protected String m_last;
		
		public XorLoop(String pattern)
		{
			super(pattern);
			m_last = null;
		}
		
		@Override
		protected void handleLayer(String layer_name, CommandInterpreter interpreter) throws XPathExpressionException
		{
			if (m_last != null)
			{
				interpreter.deleteCurrentLayers(Collections.singletonList(new LayerOccurrence(m_last, 1, 0)));
			}
			interpreter.addCurrentLayers(Collections.singletonList(new LayerOccurrence(layer_name, 1, 0)));
			interpreter.outputSlide();
			m_last = layer_name;
		}		
	}
	
	public static class NormalLoop extends LoopCommand
	{
		public NormalLoop(String pattern)
		{
			super(pattern);
		}
		
		@Override
		protected void handleLayer(String layer_name, CommandInterpreter interpreter) throws XPathExpressionException
		{
			interpreter.addCurrentLayers(Collections.singletonList(new LayerOccurrence(layer_name, 1, 0)));
			interpreter.outputSlide();
		}		
	}
}
