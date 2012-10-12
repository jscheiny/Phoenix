// Variable.java
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

public abstract class Variable {

	private String typeName;
	
	public boolean literal = true;

	public Variable() {
		this.typeName = null;
	}
	
	public Variable(String typeName) {
		this.typeName = typeName;
	}
	
	protected final void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public boolean isLiteral() {
		return literal;
	}

	public void setLiteral(boolean literal) {
		this.literal = literal;
	}

	public abstract String stringValue();
	
	public abstract Variable copy();
	public abstract Variable assign(Variable x);

	public abstract Variable add(Variable x);
	public abstract Variable subtract(Variable x);
	public abstract Variable multiply(Variable x);
	public abstract Variable divide(Variable x);
	public abstract Variable mod(Variable x);
	public abstract Variable exponentiate(Variable x);
	public abstract Variable round(Variable x);
	public abstract Variable negate();
	
	public abstract BooleanVariable equalTo(Variable x);
	public abstract BooleanVariable notEqualTo(Variable x);
	public abstract BooleanVariable lessThan(Variable x);
	public abstract BooleanVariable lessThanOrEqual(Variable x);
	public abstract BooleanVariable greaterThan(Variable x);
	public abstract BooleanVariable greaterThanOrEqual(Variable x);
	
	public abstract Variable logicalAnd(Variable x);
	public abstract Variable logicalOr(Variable x);	
	public abstract Variable logicalNot();

	public abstract Variable convertTo(TypeVariable type);
	
	public abstract Variable call(Variable left, Variable right);
	
	public final Variable assignAdd(Variable x) {
		return assign(add(x));
	}

	public final Variable assignSubtract(Variable x) {
		return assign(subtract(x));
	}
	
	public final Variable assignMultiply(Variable x) {
		return assign(multiply(x));
	}

	public final Variable assignDivide(Variable x) {
		return assign(divide(x));
	}
	
	public final Variable assignMod(Variable x) {
		return assign(mod(x));
	}
	
	public final Variable assignExponentiate(Variable x) {
		return assign(exponentiate(x));
	}

	public final Variable assignRound(Variable x) {
		return assign(round(x));
	}
}
