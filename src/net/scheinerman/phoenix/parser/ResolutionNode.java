package net.scheinerman.phoenix.parser;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;

public class ResolutionNode extends ParseTreeNode {

	private Interpreter interpreter;
	private String name;
	
	public ResolutionNode(Interpreter interpreter, String name, Line source) {
		super(Type.NONARY, source);
		this.interpreter = interpreter;
		this.name = name;
	}

	@Override
	public int precedence() {
		return 0;
	}

	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		if(interpreter.getVAT().hasVariable(name)) {
			return new DataNode(interpreter.getVAT().getVariable(name), getSourceLine());
		}
		throw new SyntaxException("Unexpected symbol '" + name + "'", getSourceLine());
	}

	@Override
	public String toString() {
		return "res[" + name + "]";
	} 
	
}
