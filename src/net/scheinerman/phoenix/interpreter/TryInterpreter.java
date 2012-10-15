package net.scheinerman.phoenix.interpreter;

import net.scheinerman.phoenix.exceptions.*;

public class TryInterpreter extends Interpreter {

	private CatchInterpreter catchIntepreter;
	
	public TryInterpreter(Interpreter parent, SourceCode source, int start, int end,
			CatchInterpreter catchInterpreter) {
		super(parent, source, start, end);
		this.catchIntepreter = catchInterpreter;
	}

	@Override
	public EndCondition interpret() {
		try {
			return super.interpret();
		} catch(PhoenixRuntimeException exception) {
			catchIntepreter.setThrown(exception);

			EndCondition condition = catchIntepreter.interpret();
			endConditionLine = catchIntepreter.getEndConditionLine();
			returnVariable = catchIntepreter.getReturnVariable();
			return condition;
		}
	}
	
}
