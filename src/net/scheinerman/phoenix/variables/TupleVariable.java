package net.scheinerman.phoenix.variables;

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;

public class TupleVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.TUPLE;
	
	private ArrayList<Variable> elements;
	
	public TupleVariable(ArrayList<Variable> elements) {
		super(TYPE_NAME);
		if(elements == null) {
			this.elements = new ArrayList<Variable>();
		} else {
			this.elements = elements;
		}
	}
	
	public String toString() {
		return stringValue();
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
	public Variable copy() {
		ArrayList<Variable> copy = new ArrayList<Variable>(elements.size());
		for(Variable v : elements) {
			copy.add(v.copy());
		}
		return new TupleVariable(copy);
	}
	
}
