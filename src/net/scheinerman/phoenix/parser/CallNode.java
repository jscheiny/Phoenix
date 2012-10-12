package net.scheinerman.phoenix.parser;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

public class CallNode extends ParseTreeNode {

	private ParseTreeNode callee;
	
	public CallNode(ParseTreeNode callee, Type type, SourceCode.Line source) {
		super(type, source);
		this.callee = callee;
	}
	
	@Override
	public int precedence() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public String toString() {
		return (left() != null ? left().toString() : "") + callee.toString() + (right() != null ? right().toString() : "");
	}

	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		Variable calleeValue = callee.operate().getValue();
		try {
			Variable leftValue = (left == null ? null : left.operate().getValue());
			Variable rightValue = (right == null ? null : right.operate().getValue());
			
			return new DataNode(calleeValue.call(leftValue, rightValue), getSourceLine());
			
		} catch(UnsupportedOperatorException e) {
			e.setSourceLine(getSourceLine());
			if(!(calleeValue instanceof FunctionVariable))
				e.setMessage(calleeValue.getTypeName() + " type variables are not callable.");
			throw e;
		} catch(PhoenixRuntimeException e) {
			e.setSourceLine(getSourceLine());
			if(calleeValue instanceof FunctionVariable) {
				e.addFunctionTrace(getSourceLine(),
					((FunctionVariable)calleeValue).getInterpreter());
			}
			throw e;
		}
	}

}
