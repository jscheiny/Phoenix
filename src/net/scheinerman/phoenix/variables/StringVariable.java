// StringVariable.java
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

public class StringVariable extends Variable {

	private static HashMap<Character, Character> ESCAPABLE = new HashMap<Character, Character>();
	static {
		ESCAPABLE.put('n', '\n');
		ESCAPABLE.put('t', '\t');
		ESCAPABLE.put('r', '\r');
		ESCAPABLE.put('b', '\b');
		ESCAPABLE.put('\\', '\\');
		ESCAPABLE.put('"', '"');
		ESCAPABLE.put('\'', '\'');
	}
	
	private static final String TYPE_NAME = Interpreter.Strings.STRING;

	private String value;
	
	public StringVariable() {
		this("", false, null);
	}
	
	public StringVariable(String value, boolean literal, SourceCode.Line source) {
		super(TYPE_NAME);
		if(literal) {
			this.value = scrubLiteral(value, source);
		} else {
			this.value = value;
		}
	}
	
	public StringVariable(String value) {
		super("str");
		this.value = value;
	}
	
	public static String scrubLiteral(String literal, SourceCode.Line source) {
		String scrubbed = "";
		for(int index = 1; index < literal.length() - 1; index++) {
			if(literal.charAt(index) == '\\') {
				if(index == literal.length() - 2)
					throw new SyntaxException("Illegal character \\ in string literal.", source);
				char next = literal.charAt(index + 1);
				Character escaped = ESCAPABLE.get(next);
				if(escaped == null)
					throw new SyntaxException("Illegal escaped character " + next + 
						" in string literal.", source);
				scrubbed += escaped;
				index++;
			} else {
				scrubbed += literal.charAt(index);
			}
		}
		return scrubbed;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return value;
	}

	@Override
	public String stringValue() {
		return value;
	}
	
	@Override
	public Variable add(Variable x) {
		return new StringVariable(value + x.stringValue());
	}

	@Override
	public Variable subtract(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable multiply(Variable x) {
		if(x instanceof IntegerVariable) {
			int xValue = ((IntegerVariable)x).getValue();
			if(xValue < 0) {
				throw new SyntaxException("String repetition integer must be nonnegative", null);
			} else {
				String out = "";
				for(int i = 0; i < xValue; i++) {
					out += value;
				}
				return new StringVariable(out);
			}
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable divide(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof StringVariable) {
			value = x.stringValue();
			return this;
		}
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
		if(x instanceof StringVariable) {
			return new BooleanVariable(value.equals(x.stringValue()));
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(x instanceof StringVariable) {
			return new BooleanVariable(!value.equals(x.stringValue()));
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThan(Variable x) {
		if(x instanceof StringVariable) {
			return new BooleanVariable(value.compareTo(x.stringValue()) < 0);
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		if(x instanceof StringVariable) {
			return new BooleanVariable(value.compareTo(x.stringValue()) <= 0);
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		if(x instanceof StringVariable) {
			return new BooleanVariable(value.compareTo(x.stringValue()) > 0);
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		if(x instanceof StringVariable) {
			return new BooleanVariable(value.compareTo(x.stringValue()) >= 0);
		}
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
	public Variable copy() {
		return new StringVariable(value);
	}
}
