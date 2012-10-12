package net.scheinerman.phoenix.parser;

import net.scheinerman.phoenix.interpreter.SourceCode.Line;

public class SurroundNode extends ParseTreeNode {

	private ParseTreeNode internal;
	
	public SurroundNode(ParseTreeNode internal, Line source) {
		super(Type.NONARY, source);
		this.internal = internal;
	}

	@Override
	public String toString() {
		return "(" + internal.toString() + ")";
	}
	
	@Override
	public int precedence() {
		return -1;
	}

	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		return internal.operate();
	}

}
