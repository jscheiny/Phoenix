// InvalidCallParametersException.java
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

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

/**
 * Represents an Phoenix error that occurs due to a function being called with invalid parameter
 * types.
 *
 * @author Jonah Scheinerman
 */
public class InvalidCallParametersException extends PhoenixRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new invalid call parameters error.
	 * @param callee the variable being called (usually a {@link FunctionVariable}).
	 * @param left the left parameters passed to the callee
	 * @param right the right parameters passed to the callee
	 * @param sourceLine the line on which the error occurred.
	 */
	public InvalidCallParametersException(Variable callee, Variable left, Variable right,
			SourceCode.Line sourceLine) {
		setSourceLine(sourceLine);
		setErrorType("Parameters error");
		
		String message;
		if(callee instanceof FunctionVariable) {
			message = "Function " + ((FunctionVariable)callee).getName();
		} else {
			message = "Variable of type " + callee.getTypeName();
		}
		message += " cannot be called with ";
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
		if(left == null && right == null) {
			message += "no parameters.";
		}
		setMessage(message);
	}
	
}
