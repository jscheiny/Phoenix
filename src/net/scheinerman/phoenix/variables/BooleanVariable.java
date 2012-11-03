// BooleanVariable.java
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

package net.scheinerman.phoenix.variables;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;

/**
 * A variable that contains a boolean value. The type name of this variable is 'bool'.
 *
 * @author Jonah Scheinerman
 */
public class BooleanVariable extends Variable {

	/** The variable type name, 'bool'. */
	private static final String TYPE_NAME = Interpreter.Strings.BOOLEAN;

	public static class Definition extends TypeDefinition<BooleanVariable> {

		public Definition() {
			super(TYPE_NAME);
		}

		@Override
		public BooleanVariable createDefaultVariable(Interpreter interpreter) {
			return new BooleanVariable();
		}

		@Override
		public BooleanVariable createFromLiteral(Interpreter interpreter, String literal,
				Line source) {
			if(literal.equals(Interpreter.Strings.TRUE)) {
				return new BooleanVariable(true);
			}
			if(literal.equals(Interpreter.Strings.FALSE)) {
				return new BooleanVariable(false);
			}
			return null;
		}

	}
	
	/** The boolean value of this variable. */
	private boolean value;
	
	/** Create a new boolean variable with value false. */
	public BooleanVariable() {
		this(false);
	}
	
	/**
	 * Create a new boolean variable with the given value.
	 * @param value the value of the variable
	 */
	public BooleanVariable(boolean value) {
		super(TYPE_NAME);
		this.value = value;
	}
	
	@Override
	public String stringValue() {
		return "" + value;
	}

	/**
	 * Returns the boolean value of this variable.
	 * @return the boolean value of this variable
	 */
	public boolean getValue() {
		return value;
	}
	
	@Override
	public Variable assign(Variable x) {
		if(x instanceof BooleanVariable) {
			value = ((BooleanVariable)x).getValue();
		}
		throw new UnsupportedOperatorException();
	}
	
	/** Unsupported operator. */
	@Override
	public Variable add(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable subtract(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable multiply(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable divide(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable mod(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable exponentiate(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable negate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public BooleanVariable equalTo(Variable x) {
		if(x instanceof BooleanVariable) {
			return new BooleanVariable(value == ((BooleanVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(x instanceof BooleanVariable) {
			return new BooleanVariable(value == ((BooleanVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public BooleanVariable lessThan(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public BooleanVariable greaterThan(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalAnd(Variable x) {
		if(x instanceof BooleanVariable) {
			return new BooleanVariable(value && ((BooleanVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalOr(Variable x) {
		if(x instanceof BooleanVariable) {
			return new BooleanVariable(value || ((BooleanVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalNot() {
		return new BooleanVariable(!value);
	}

	/** Unsupported operator. */
	@Override
	public Variable call(Variable left, Variable right) {
		throw new UnsupportedOperatorException();
	}

	/** Pass by value. */
	@Override
	public Variable passValue() {
		return new BooleanVariable(value);
	}
	
}
