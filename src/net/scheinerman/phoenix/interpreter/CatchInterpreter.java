package net.scheinerman.phoenix.interpreter;

import net.scheinerman.phoenix.exceptions.*;

public class CatchInterpreter extends Interpreter {

//	private PhoenixRuntimeException thrown;
	
	public CatchInterpreter(Interpreter parent, SourceCode source, int start, int end) {
		super(parent, source, start, end);
	}

	public void setThrown(PhoenixRuntimeException thrown) {
//		this.thrown = thrown;
	}
	
}
