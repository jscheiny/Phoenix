package net.scheinerman.phoenix.exceptions;

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

public class InvalidCallParametersException extends PhoenixRuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidCallParametersException(Variable callee, Variable left, Variable right,
			SourceCode.Line sourceLine) {
		setSourceLine(sourceLine);
		setErrorType("Parameters error");
		
		String message = "Variable of type " + callee.getTypeName() + " cannot be called with ";
		if(left != null) {
			if(left instanceof TupleVariable) {
				message += "left parameters of types " + ((TupleVariable)left).typeString();
			} else {
				message += "left parameters of types (" + left.getTypeName() + ")";
			}
			if(right != null) {
				message += " and ";
			}
		}
		if(right != null) {
			if(right instanceof TupleVariable) {
				message += "right parameters of types " + ((TupleVariable)right).typeString();
			} else {
				message += "right parameters of types (" + right.getTypeName() + ")";
			}
			message += ".";
		}
		setMessage(message);
	}
	
}
