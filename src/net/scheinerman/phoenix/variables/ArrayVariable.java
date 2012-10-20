// ArrayVariable.java
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

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;

public class ArrayVariable extends Variable {

	private ArrayList<Variable> elements;
	
	public ArrayVariable(String typeName) {
		setTypeName(typeName);
	}
	
	public ArrayVariable(ArrayList<Variable> variables) {
		elements = new ArrayList<Variable>(variables.size());
		boolean first = true;
		String referenceType = null;
		for(Variable var : variables) {
			if(first) {
				first = false;
				referenceType = var.getTypeName();
			} else {
				if(!var.getTypeName().equals(referenceType)) {
					throw new SyntaxException("Multiple variable types within array.", null);
				}
			}
			elements.add(var);
		}
		setTypeName("[" + referenceType + "]");
	}
	
	@Override
	public String stringValue() {
		String ret = "[";
		for(int i = 0; i < elements.size(); i++) {
			ret += elements.get(i).stringValue();
			if(i != elements.size() - 1) {
				ret += ", ";
			}
		}
		return ret + "]";
	}
	
	public Variable getElement(int index) {
		return elements.get(index);
	}
	
	public int size() {
		return elements.size();
	}
	
	@Override
	public String toString() {
		return stringValue();
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof ArrayVariable) {
			ArrayVariable array = (ArrayVariable)x;
			if(getTypeName().equals(x.getTypeName())) {
				elements = new ArrayList<Variable>(array.size());
				for(int index = 0; index < array.size(); index++) {
					elements.add(array.getElement(index).passValue());
				}
				return this;
			}
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
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		if(!x.getTypeName().equals(getTypeName())) {
			throw new UnsupportedOperatorException();
		}
		ArrayVariable array = (ArrayVariable)x;
		if(array.size() != size()) {
			return new BooleanVariable(false);
		}
		for(int index = 0; index < size(); index++) {
			if(!array.getElement(index).equalTo(getElement(index)).getValue()) {
				return new BooleanVariable(false);				
			}
		}
		return new BooleanVariable(true);
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(!x.getTypeName().equals(getTypeName())) {
			throw new UnsupportedOperatorException();
		}
		ArrayVariable array = (ArrayVariable)x;
		if(array.size() != size()) {
			return new BooleanVariable(true);
		}
		for(int index = 0; index < size(); index++) {
			if(!array.getElement(index).equalTo(getElement(index)).getValue()) {
				return new BooleanVariable(true);				
			}
		}
		return new BooleanVariable(false);
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
		if(right == null) {
			throw new InvalidCallParametersException(this, left, right, null);
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable passValue() {
		return this;
	}

}
