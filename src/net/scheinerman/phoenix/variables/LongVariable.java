// LongVariable.java
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

public class LongVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.LONG;
	
	private long value;
	
	public LongVariable() {
		this(0);
	}
	
	public LongVariable(long value) {
		super(TYPE_NAME);
		this.value = value;
	}
	
	@Override
	public String stringValue() {
		return "" + value;
	}
	
	@Override
	public String toString() {
		return stringValue();
	}
	
	public long getValue() {
		return value;
	}

	@Override
	public Variable copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof IntegerVariable) {
			value = ((IntegerVariable)x).getValue();
			return this;
		} else if(x instanceof LongVariable) {
			value = ((LongVariable)x).getValue();
			return this;
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable add(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable subtract(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable multiply(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable divide(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable mod(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable exponentiate(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable negate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable lessThan(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable logicalAnd(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable logicalOr(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable logicalNot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable convertTo(TypeVariable type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable call(Variable left, Variable right) {
		// TODO Auto-generated method stub
		return null;
	}

}
