// UnsupportedOperatorException.java
// Copyright (C) 2012 by Jonah Scheinerman
//
// This file is part of the Phoenix programming language.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package net.scheinerman.phoenix.exceptions;

import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.variables.*;

/**
 * Represents an error that occurs when an operator is called with invalid operands.
 * 
 * @author Jonah Scheinerman
 */
public class UnsupportedOperatorException extends PhoenixRuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new unsupported operator error with no message. This is typically used by the
	 * operator methods in {@link Variable} subclasses. This then gets caught by the parser which
	 * rethrows a new error using the
	 * {@link UnsupportedOperatorException#UnsupportedOperatorException(String, Variable, Variable, Line)} constructor to pass
	 * the correct values.
	 */
	public UnsupportedOperatorException() {
		setMessage(null);
	}
	
	/**
	 * Constructs a new unsupported operator error and generates an error message using the values
	 * passed.
	 * @param operator the symbol the operator
	 * @param leftOperand the left-hand operand (may be null if there is none)
	 * @param rightOperand the right-hand operand (may be null if there is none)
	 * @param sourceLine the line on which the error occurred
	 */
	public UnsupportedOperatorException(String operator, Variable leftOperand,
			Variable rightOperand, Line sourceLine) {
		setSourceLine(sourceLine);
		
		if(leftOperand == null && rightOperand == null) {
			setMessage(operator + " is not defined.");
		} else if(leftOperand != null && rightOperand != null) {
			setMessage(operator + " is not defined for operands of types " +
					   leftOperand.getTypeName() + " and " + rightOperand.getTypeName());
		} else {
			setMessage(operator + " is not defined for operand of type " +
					   (rightOperand != null ? rightOperand.getTypeName() :
						   					   leftOperand.getTypeName()));
		}
		
		setErrorType("Operator error");
	}
}
