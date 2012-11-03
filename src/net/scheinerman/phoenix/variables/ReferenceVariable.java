// Reference.java
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

public class ReferenceVariable extends Variable {

	private Variable value;
	
	public ReferenceVariable(Variable value) {
		super("@" + value.getTypeName());
		this.value = value;
		setLiteral(false);
	}
	
	public Variable getValue() {
		return value;
	}
	
	@Override
	public String stringValue() {
		return value.stringValue();
	}
	
	@Override
	public Variable passValue() {
		return value;
	}

	@Override
	public Variable assign(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable add(Variable x) {
		return value.add(x);
	}

	@Override
	public Variable subtract(Variable x) {
		return value.subtract(x);
	}

	@Override
	public Variable multiply(Variable x) {
		return value.multiply(x);
	}

	@Override
	public Variable divide(Variable x) {
		return value.divide(x);
	}

	@Override
	public Variable mod(Variable x) {
		return value.mod(x);
	}

	@Override
	public Variable exponentiate(Variable x) {
		return value.exponentiate(x);
	}

	@Override
	public Variable negate() {
		return value.negate();
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		return value.equalTo(x);
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		return value.notEqualTo(x);
	}

	@Override
	public BooleanVariable lessThan(Variable x) {
		return value.lessThan(x);
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		return value.lessThanOrEqual(x);
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		return value.greaterThan(x);
	}

	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		return value.greaterThanOrEqual(x);
	}

	@Override
	public Variable logicalAnd(Variable x) {
		return value.logicalAnd(x);
	}

	@Override
	public Variable logicalOr(Variable x) {
		return value.logicalOr(x);
	}

	@Override
	public Variable logicalNot() {
		return value.logicalNot();
	}

	@Override
	public Variable call(Variable left, Variable right) {
		return value.call(left, right);
	}

}
