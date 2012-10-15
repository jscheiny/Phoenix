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
		if(x instanceof LongVariable) {
			return new LongVariable(value + ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value + ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new LongVariable(value + ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable subtract(Variable x) {
		if(x instanceof LongVariable) {
			return new LongVariable(value - ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value - ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new LongVariable(value - ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable multiply(Variable x) {
		if(x instanceof LongVariable) {
			return new LongVariable(value * ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value * ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new LongVariable(value * ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable divide(Variable x) {
		if(x instanceof LongVariable) {
			return new LongVariable(value / ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value / ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new LongVariable(value / ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable mod(Variable x) {
		if(x instanceof LongVariable) {
			return new LongVariable(value % ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new DoubleVariable(value % ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new LongVariable(value % ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public Variable exponentiate(Variable x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable negate() {
		return new LongVariable(-value);
	}

	@Override
	public BooleanVariable equalTo(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value == ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value == ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value == ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable notEqualTo(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value == ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value == ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value == ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThan(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value < ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value < ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value < ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable lessThanOrEqual(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value <= ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value <= ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value <= ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThan(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value > ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value > ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value > ((IntegerVariable) x).getValue());
		}
		throw new UnsupportedOperatorException();
	}

	@Override
	public BooleanVariable greaterThanOrEqual(Variable x) {
		if(x instanceof LongVariable) {
			return new BooleanVariable(value >= ((LongVariable) x).getValue());
		} else if(x instanceof DoubleVariable) {
			return new BooleanVariable(value >= ((DoubleVariable) x).getValue());
		} else if(x instanceof IntegerVariable) {
			return new BooleanVariable(value >= ((IntegerVariable) x).getValue());
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

}
