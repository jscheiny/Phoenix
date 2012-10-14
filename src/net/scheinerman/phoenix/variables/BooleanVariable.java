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

public class BooleanVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.BOOLEAN;

	private boolean value;
	
	public BooleanVariable() {
		this(false);
	}
	
	public BooleanVariable(boolean value) {
		super(TYPE_NAME);
		this.value = value;
	}
	
	@Override
	public String stringValue() {
		return "" + value;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
	
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
	
	@Override
	public Variable add(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable subtract(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable multiply(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable divide(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable mod(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable exponentiate(Variable x) {
		throw new UnsupportedOperatorException();
	}

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

	@Override
	public BooleanVariable lessThan(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		throw new UnsupportedOperatorException();
	}

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

	@Override
	public Variable convertTo(TypeVariable type) {
		String typeName = type.stringValue();
		if(typeName.equals(Interpreter.Strings.INTEGER)) {
			return new IntegerVariable(value ? 1 : 0);
		} else if(typeName.equals(Interpreter.Strings.DOUBLE)) {
			return new DoubleVariable(value ? 1.0 : 0.0);
		} else if(typeName.equals(Interpreter.Strings.STRING)){
			return new StringVariable("" + value);
		}
		throw new InvalidConversionException(this, typeName);
	}

	@Override
	public Variable call(Variable left, Variable right) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable copy() {
		return new BooleanVariable(value);
	}
	
}
