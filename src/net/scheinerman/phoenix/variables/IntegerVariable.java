// IntegerVariable.java
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

import java.util.regex.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;

public class IntegerVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.INTEGER;
	
	/** A pattern that matches integer literals. */
	private static final Pattern INTEGER_LITERAL = Pattern.compile("\\d+");
	
	public static class Definition extends TypeDefinition<IntegerVariable> {

		public Definition() {
			super(TYPE_NAME);
		}

		@Override
		public IntegerVariable createDefaultVariable(Interpreter interpreter) {
			return new IntegerVariable();
		}

		@Override
		public IntegerVariable createFromLiteral(Interpreter interpreter, String literal,
				Line source) {
			if(!INTEGER_LITERAL.matcher(literal).matches())
				return null;
			long value = Long.parseLong(literal);
			if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
				return null;
			return new IntegerVariable((int)value);
		}

	}

	private int value;
	
	public IntegerVariable() {
		this(0);
	}
	
	public IntegerVariable(int value) {
		super(TYPE_NAME);
		this.value = value;
	}
	
	@Override
	public String stringValue() {
		return "" + value;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof IntegerVariable) {
			value = ((IntegerVariable)x).getValue();
			return this;
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable add(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable(value + ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value + ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new LongVariable(value + ((LongVariable)x).getValue());
		} else if(x instanceof StringVariable) {
			return new StringVariable(value + x.stringValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable subtract(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable(value - ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value - ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new LongVariable(value - ((LongVariable)x).getValue());
		}
		throw new PhoenixRuntimeException();
	}

	@Override
	public Variable multiply(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable(value * ((IntegerVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new LongVariable(value * ((LongVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value * ((DoubleVariable)x).getValue());
		} else if(x instanceof StringVariable) {
			if(value < 0) {
				throw new SyntaxException("String repetition integer must be nonnegative", null);
			} else {
				String xValue = x.stringValue();
				String out = "";
				for(int i = 0; i < value; i++) {
					out += xValue;
				}
				return new StringVariable(out);
			}
		}
		throw new PhoenixRuntimeException();
	}

	@Override
	public Variable divide(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable(value / ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value / ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new LongVariable(value / ((LongVariable)x).getValue());
		}
		throw new PhoenixRuntimeException();
	}

	@Override
	public Variable mod(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable(value % ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value % ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new LongVariable(value % ((LongVariable)x).getValue());
		}
		throw new PhoenixRuntimeException();
	}

	@Override
	public Variable exponentiate(Variable x) {
		if(x instanceof IntegerVariable) {
			return new IntegerVariable((int)Math.pow(value, ((IntegerVariable)x).getValue()));
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(Math.pow(value, ((DoubleVariable)x).getValue()));
		} else if(x instanceof LongVariable) {
			return new LongVariable((long)Math.pow(value, ((LongVariable)x).getValue()));
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable negate() {
		return new IntegerVariable(-value);
	}
	
	@Override
	public BooleanVariable equalTo(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value == ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value == ((DoubleVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value != ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value != ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value != ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThan(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value < ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value < ((DoubleVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value <= ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value <= ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value <= ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value > ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value > ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value > ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value >= ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value >= ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value >= ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalAnd(Variable x) {
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable logicalOr(Variable x) {
		throw new PhoenixRuntimeException();
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
		return new IntegerVariable(value);
	}
	
}
