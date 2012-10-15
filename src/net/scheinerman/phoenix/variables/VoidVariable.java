package net.scheinerman.phoenix.variables;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;

public class VoidVariable extends Variable {

	private static final String TYPE_NAME = Interpreter.Strings.VOID;
	
	public VoidVariable() {
		super(TYPE_NAME);
	}
	
	@Override
	public String stringValue() {
		return TYPE_NAME;
	}
	
	@Override
	public String toString() {
		return stringValue();
	}

	@Override
	public Variable copy() {
		throw new SyntaxException("Cannot pass void type variable.", null);
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

}
