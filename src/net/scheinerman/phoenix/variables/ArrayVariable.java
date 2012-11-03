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

/**
 * A variable that contains an array of variables all of the same type. This is the only variable
 * that does not have a fixed type name, its type name varies depending on the elements that it
 * contains. The type name of this variable is '[x]' where 'x' is the type name of the elements in
 * the array.
 *
 * @author Jonah Scheinerman
 */
public class ArrayVariable extends Variable {

	/** The elements of the array. */
	private ArrayList<Variable> elements;
	
	/**
	 * Construct a new array variable with a given type name. This construct is used when
	 * initializing new array variables. The type name must be enclosed in square brackets.
	 * @param typeName the type name of the array
	 */
	public ArrayVariable(String typeName) {
		setTypeName(typeName);
	}

	/**
	 * Construct a new array variable from an array of variables. The type name of this array will
	 * be determine based on the contents of the array. If there are multiple variable types within
	 * the array, a {@link SyntaxException} will be thrown.
	 * @param variables the elements of the array
	 */
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
			Variable value = var.passValue();
			value.setLiteral(false);
			elements.add(value);
		}
		setTypeName("[" + referenceType + "]");
	}
	
	public void setLiteral(boolean literal) {
		super.setLiteral(literal);
		for(Variable v : elements) {
			v.setLiteral(false);
		}
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
	
	/**
	 * Retrieves the element at the given index of the array.
	 * @param index the index from which to retrieve an element
	 * @return the element at that index
	 */
	public Variable getElement(int index) {
		return elements.get(index);
	}
	
	/**
	 * Returns the number of elements in the array.
	 * @return the number of elements in the array
	 */
	public int size() {
		return elements.size();
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof ArrayVariable) {
			ArrayVariable array = (ArrayVariable)x;
			if(getTypeName().equals(x.getTypeName())) {
				elements = new ArrayList<Variable>(array.size());
				for(int index = 0; index < array.size(); index++) {
					Variable value = array.getElement(index).passValue();
					value.setLiteral(false);
					elements.add(value);
				}
				return this;
			}
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

	/** Unsupported operator. */
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

	/** Unsupported operator. */
	@Override
	public Variable logicalAnd(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable logicalOr(Variable x) {
		throw new UnsupportedOperatorException();
	}

	/** Unsupported operator. */
	@Override
	public Variable logicalNot() {
		throw new UnsupportedOperatorException();
	}

	/**
	 * Retrieves the value in the array at the index of the value given by the right argument, only
	 * if the right argument is an integer.
	 */
	@Override
	public Variable call(Variable left, Variable right) {
		if(right == null && !(right instanceof IntegerVariable)) {
			throw new InvalidCallParametersException(this, left, right, null);
		}
		int index = ((IntegerVariable) right).getValue();
		if(index < 0)
			return getElement((size() + (index % size())) % size());
		return getElement(index % size());
	}

	/** Pass by reference. */
	@Override
	public Variable passValue() {
		return this;
	}

}
