// TupleVariable.java
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
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;

public class TupleVariable extends Variable {
	
	public static class Definition extends TypeDefinition<TupleVariable> {
		public Definition() {
			super(null);
		}

		@Override
		public TupleVariable createDefaultVariable(Interpreter interpreter) {
			return null;
		}

		@Override
		public TupleVariable createFromLiteral(Interpreter interpreter, String literal,
				Line source) {
			return null;
		}
	}
	
	private ArrayList<Variable> elements;
	
	public TupleVariable(ArrayList<Variable> elements) {
		super(null);
		if(elements == null) {
			this.elements = new ArrayList<Variable>();
		} else {
			this.elements = elements;
		}
	}
	
	@Override
	public String stringValue() {
		String ret = "";
		for(int i = 0; i < elements.size(); i++) {
			ret += elements.get(i).stringValue();
			if(i != elements.size() - 1) {
				ret += " ";
			}
		}
		return ret;
	}
	
	public String typeString() {
		String ret = "(";
		for(int i = 0; i < elements.size(); i++) {
			ret += elements.get(i).getTypeName();
			if(i != elements.size() - 1) {
				ret += ", ";
			}
		}
		return ret + ")";
	}
	
	public Variable getElement(int index) {
		return elements.get(index);
	}
	
	public int size() {
		return elements.size();
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
	public Variable exponentiate(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable negate() {
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
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
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable passValue() {
		ArrayList<Variable> copy = new ArrayList<Variable>(elements.size());
		for(Variable v : elements) {
			copy.add(v.passValue());
		}
		return new TupleVariable(copy);
	}
	
}
