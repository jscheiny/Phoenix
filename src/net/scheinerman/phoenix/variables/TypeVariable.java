// TypeVariable.java
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

public class TypeVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.TYPE;

	private String value;
	
	public TypeVariable() {
		this("void");
	}

	public TypeVariable(Variable value) {
		this(value.getTypeName());
	}
	
	public TypeVariable(String value) {
		super(TYPE_NAME);
		this.value = value;
	}

	@Override
	public String stringValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}

	@Override
	public Variable assign(Variable x) {
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
	public Variable negate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Variable exponentiate(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		if(x instanceof TypeVariable) {
			return new BooleanVariable(value.equals(x.stringValue()));
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(x instanceof TypeVariable) {
			return new BooleanVariable(!value.equals(x.stringValue()));
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
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalOr(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalNot() {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable call(Variable left, Variable right) {
		if(left != null || right == null) {
			throw new InvalidCallParametersException(this, left, right, null);
		}
		return new TypeVariable(right.getTypeName());
	}

	@Override
	public Variable passValue() {
		return new TypeVariable(value);
	}

}
