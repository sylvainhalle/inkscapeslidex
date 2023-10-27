package ca.leduotang.inkscapeslide;

public interface Command
{
	public void interpret(CommandInterpreter interpreter) throws CommandException;
}
