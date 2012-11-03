// DoubleVariable.java
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

/**
 * A variable that contains a double precision number value. The type name of this variable is
 * 'double'.
 *
 * @author Jonah Scheinerman
 */
public class DoubleVariable extends Variable {

	/** The variable type name, 'double'. */
	private static final String TYPE_NAME = Interpreter.Strings.DOUBLE;

	/** First of the patterns that matches double literals. */
	private static final Pattern DOUBLE_LITERAL_1 = Pattern.compile("\\d+[dD]");

	/** Second of the patterns that matches double literals. */
	private static final Pattern DOUBLE_LITERAL_2 = Pattern.compile("\\.\\d+[dD]?");

	/** Third of the patterns that matches double literals. */
	private static final Pattern DOUBLE_LITERAL_3 = Pattern.compile("\\d+\\.\\d*[dD]?");
	
	public static class Definition extends TypeDefinition<DoubleVariable> {

		public Definition() {
			super(TYPE_NAME);
		}

		@Override
		public DoubleVariable createDefaultVariable(Interpreter interpreter) {
			return new DoubleVariable();
		}

		@Override
		public DoubleVariable createFromLiteral(Interpreter interpreter, String literal,
				Line source) {
			if(DOUBLE_LITERAL_1.matcher(literal).matches() ||
			   DOUBLE_LITERAL_2.matcher(literal).matches() ||
			   DOUBLE_LITERAL_3.matcher(literal).matches()) {
				if(literal.endsWith("d")) {
					literal = literal.substring(0, literal.length() - 1);
				}
				return new DoubleVariable(Double.parseDouble(literal));
			}
			return null;	
		}

	}
	
	/** The double value of this variable. */
	private double value;
	
	/** Create a new double variable with value 0. */
	public DoubleVariable() {
		this(0);
	}
	
	/**
     * Create a new double variable with a given value. 
 	 * @param value the value of the variable
	 */
	public DoubleVariable(double value) {
		super(TYPE_NAME);
		this.value = value;
	}
	
	@Override
	public String stringValue() {
		return "" + value;
	}

	/**
	 * Returns the double value of this variable.
	 * @return the double value of this variable
	 */
	public double getValue() {
		return value;
	}

	@Override
	public Variable assign(Variable x) {
		if(x instanceof IntegerVariable) {
			value = ((IntegerVariable)x).getValue();
			return this;
		} else if(x instanceof DoubleVariable) {
			value = ((DoubleVariable)x).getValue();
			return this;
		} else if(x instanceof LongVariable) {
			value = ((LongVariable)x).getValue();
			return this;
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable add(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(value + ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value + ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(value + ((LongVariable)x).getValue());
		} else if(x instanceof StringVariable) {
			return new StringVariable(value + x.stringValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable subtract(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(value - ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value - ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(value - ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable multiply(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(value * ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value * ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(value * ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable divide(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(value / ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value / ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(value / ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable mod(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(value % ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value % ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(value % ((LongVariable)x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable exponentiate(Variable x) {
		if(x instanceof IntegerVariable) {
			return new DoubleVariable(Math.pow(value, ((IntegerVariable)x).getValue()));
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(Math.pow(value, ((DoubleVariable)x).getValue()));
		} else if(x instanceof LongVariable) {
			return new DoubleVariable(Math.pow(value, ((LongVariable)x).getValue()));
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable negate() {
		return new DoubleVariable(-value);
	}
	
	@Override
	public BooleanVariable equalTo(Variable x) {
		if(x instanceof IntegerVariable) {
			return new BooleanVariable(value == ((IntegerVariable)x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value == ((DoubleVariable)x).getValue());
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value == ((LongVariable)x).getValue());
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
		} else if(x instanceof LongVariable) {
			return new BooleanVariable(value < ((LongVariable)x).getValue());
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

	/** Unsupported operator. */
	@Override
	public Variable call(Variable left, Variable right) {
		throw new UnsupportedOperatorException();
	}

	/** Pass by value. */
	@Override
	public Variable passValue() {
		return new DoubleVariable(value);
	}
	
}
