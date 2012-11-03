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

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.parser.*;

/**
 * Represents a variable within Phoenix. All variable types within Phoenix should be subclasses of
 * this class. Operators on a variable are defined by the methods in this class. By default, all
 * variables are literals. When initializing a variable, it should be set as not literal.<br />
 * <br />
 *
 * If a variable does not support a specific operator (or it doesn't support the operator operation
 * with the passed value), then in that method, it should call:
 * {@link UnsupportedOperatorException#UnsupportedOperatorException()}. Those calling these methods
 * are responsible for catching this error and rethrowing it with extra relevant information.
 *
 * @author Jonah Scheinerman
 */
public abstract class Variable {

	public static abstract class TypeDefinition<T extends Variable> {
		private String typeName;
		
		public TypeDefinition(String typeName) {
			this.typeName = typeName;
		}
		
		public String getTypeName() {
			return typeName;
		}

		public abstract T createDefaultVariable(Interpreter interpreter);
		
		public abstract T createFromLiteral(Interpreter interpreter, String literal, Line source);
	}
	
	/** The type name for this variable. */
	private String typeName;
	
	/** Whether this variable holds a literal value. */
	public boolean literal = true;

	/**
	 * Construct a new empty Variable with no type name. This super constructor should almost never
	 * be used. In general, all variables should call the {@link Variable#Variable(String)} super
	 * constructor, with the same type name every time. The exception to this is array variables
	 * whose type names change depending on what the array is holding.
	 */
	public Variable() {
		this.typeName = null;
	}
	
	/**
	 * Creates a new variable with the given type name. This super constructor should be used by
	 * subclass constructors and should be almost always be passed the same string regardless of the
	 * variable value (except for arrays).
	 * @param typeName the type name of the variable
	 */
	public Variable(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Sets this variables type name.
	 * @param typeName the type name of the variable
	 */
	protected final void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	/**
	 * Gets the type name of this variable
	 * @return the type name of this variable
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * Returns whether this is a literal value. Literal values cannot be assigned to, though this
	 * error is handled by the expression parser.
	 * @return whether this is a literl value
	 * @see Parser
	 */
	public boolean isLiteral() {
		return literal;
	}

	/**
	 * Sets whether this variable is a literal value.
	 * @param literal
	 */
	public void setLiteral(boolean literal) {
		this.literal = literal;
	}

	/**
	 * The string representation of this variable.
	 * @return the string representation of the variable value
	 */
	public abstract String stringValue();
	
	@Override
	public final String toString() {
		return stringValue();
	}
	
	/**
	 * Returns the version of this variable that should be passed through function parameters. This
	 * should be implemented to make a variable either pass by value or pass by reference. If the
	 * variable is passed by value, then this method should return a copy of the variable. If this
	 * variable is passed by reference, then this method should just return <code>this</code>.
	 * @return a copy of this variable
	 */
	public abstract Variable passValue();

	/**
	 * Performs the assignment operation (=) for this variable.
	 * @param x the right-hand operand for the assignment operation
	 * @return the result of the assignment (should be <code>this</code>)
	 */
	public abstract Variable assign(Variable x);

	/**
	 * Performs the addition operation (+) for this variable.
	 * @param x the right-hand operand for the addition operation
	 * @return the result of the addition 
	 */
	public abstract Variable add(Variable x);

	/**
	 * Performs the subtraction operation (-) for this variable.
	 * @param x the right-hand operand for the subtraction operation
	 * @return the result of the subtraction 
	 */
	public abstract Variable subtract(Variable x);

	/**
	 * Performs the multiplication operation (*) for this variable.
	 * @param x the right-hand operand for the multiplication operation
	 * @return the result of the multiplication 
	 */
	public abstract Variable multiply(Variable x);

	/**
	 * Performs the division operation (/) for this variable.
	 * @param x the right-hand operand for the division operation
	 * @return the result of the division 
	 */
	public abstract Variable divide(Variable x);

	/**
	 * Performs the modulus operation (%) for this variable.
	 * @param x the right-hand operand for the modulus operation
	 * @return the result of the modulus 
	 */
	public abstract Variable mod(Variable x);

	/**
	 * Performs the exponentiation operation (^) for this variable.
	 * @param x the right-hand operand for the exponentiation operation
	 * @return the result of the exponentiation 
	 */
	public abstract Variable exponentiate(Variable x);
	
	
	public abstract Variable negate();
	
	/**
	 * Performs the equality operation (==) for this variable.
	 * @param x the right-hand operand for the equality operation
	 * @return the result of the equality as a boolean variable
	 */
	public abstract BooleanVariable equalTo(Variable x);

	/**
	 * Performs the inequality operation (!=) for this variable.
	 * @param x the right-hand operand for the inequality operation
	 * @return the result of the inequality as a boolean variable
	 */
	public abstract BooleanVariable notEqualTo(Variable x);

	/**
	 * Performs the less than operation (<) for this variable.
	 * @param x the right-hand operand for the less than operation
	 * @return the result of the less than as a boolean variable
	 */
	public abstract BooleanVariable lessThan(Variable x);

	/**
	 * Performs the less than or equal operation (<=) for this variable.
	 * @param x the right-hand operand for the less than or equal operation
	 * @return the result of the less than or equal as a boolean variable
	 */
	public abstract BooleanVariable lessThanOrEqual(Variable x);

	/**
	 * Performs the greater than operation (>) for this variable.
	 * @param x the right-hand operand for the greater than operation
	 * @return the result of the greater than as a boolean variable
	 */
	public abstract BooleanVariable greaterThan(Variable x);

	/**
	 * Performs the greater than or equal operation (>=) for this variable.
	 * @param x the right-hand operand for the greater than or equal operation
	 * @return the result of the greater than or equal as a boolean variable
	 */
	public abstract BooleanVariable greaterThanOrEqual(Variable x);
	
	/**
	 * Performs the logical and operation (and) for this variable.
	 * @param x the right-hand operand for the logical and operation
	 * @return the result of the logical and as a boolean variable
	 */
	public abstract Variable logicalAnd(Variable x);

	/**
	 * Performs the logical or operation (or) for this variable.
	 * @param x the right-hand operand for the logical or operation
	 * @return the result of the logical or as a boolean variable
	 */
	public abstract Variable logicalOr(Variable x);
	
	/**
	 * Performs the logical not operation (not) for this variable.
	 * @return the result of the logical not as a boolean variable
	 */
	public abstract Variable logicalNot();
	
	/**
	 * Performs a call on this variable with the given left and right-hand arguments. If this
	 * variable type is not callable at all, then throw a {@link UnsupportedOperatorException}.
	 * If this does accept calls but the variables are of the wrong types, then throw a
	 * {@link InvalidCallParametersException}. If multiple parameters are passed on either side,
	 * they will be given as a {@link TupleVariable}. If a single parameter is passed to either
	 * side, it will be given as a variable of that type. If no parameter is passed to a side it
	 * will be given as <code>null</code>.
	 * @param left the left-hand call parameters
	 * @param right the right-hand call parameters
	 * @return the return value of the call
	 */
	public abstract Variable call(Variable left, Variable right);
	
	/**
	 * Performs the add and assign operation (+=) for this variable.
	 * @param x the right hand operatond for the add and assign operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignAdd(Variable x) {
		return assign(add(x));
	}

	/**
	 * Performs the subtract and assign operation (-=) for this variable.
	 * @param x the right hand operatond for the subtract and assign operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignSubtract(Variable x) {
		return assign(subtract(x));
	}
	
	/**
	 * Performs the multiply and assign operation (*=) for this variable.
	 * @param x the right hand operatond for the multiply and assign operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignMultiply(Variable x) {
		return assign(multiply(x));
	}

	/**
	 * Performs the divide and assign operation (/=) for this variable.
	 * @param x the right hand operatond for the divide and assign operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignDivide(Variable x) {
		return assign(divide(x));
	}
	
	/**
	 * Performs the modulus and assign operation (%=) for this variable.
	 * @param x the right hand operatond for the modulus and assign operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignMod(Variable x) {
		return assign(mod(x));
	}

	/**
	 * Performs the add and exponentiate operation (^=) for this variable.
	 * @param x the right hand operatond for the add and exponentiate operation
	 * @return the new value of this variable after the operation
	 */
	public final Variable assignExponentiate(Variable x) {
		return assign(exponentiate(x));
	}

}
