// OperatorNode.java
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

package net.scheinerman.phoenix.parser;

import java.lang.reflect.*;
import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.Interpreter.*;
import net.scheinerman.phoenix.variables.*;

/**
 * A node that performs an operation between up to two variables and returns the result. All
 * enclosed classes of this class are subclasses whose operate method call one of the operator
 * methods in the {@link Variable} class.
 *
 * @author Jonah Scheinerman
 */
public abstract class OperatorNode extends ParseTreeNode {

	/**
	 * Class containing static string constants for all of the operator symbols. The only operators
	 * not included here are the logical and, or and not which are not symbols but keywords, and are
	 * put in the {@link Interpreter#Strings} class.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Symbols { 
		public static final String ASSIGN = "=";
		public static final String ASSIGN_ADD = "+=";
		public static final String ASSIGN_SUBTRACT = "-=";
		public static final String ASSIGN_MULTIPLY = "*=";
		public static final String ASSIGN_DIVIDE = "/=";
		public static final String ASSIGN_MOD = "%=";
		public static final String ASSIGN_ROUND = "#=";
		public static final String ASSIGN_EXPONENTIATE = "^=";
		public static final String ADD = "+";
		public static final String SUBTRACT = "-";
		public static final String MULTIPLY = "*";
		public static final String DIVIDE = "/";
		public static final String MOD = "%";
		public static final String ROUND = "#";
		public static final String EXPONENTIATE = "^";
		public static final String EQUAL = "==";
		public static final String NOT_EQUAL = "!=";
		public static final String LESS_THAN = "<";
		public static final String LESS_THAN_EQUAL = "<=";
		public static final String GREATER_THAN = ">";
		public static final String GREATER_THAN_EQUAL = ">=";
		public static final String ARG_SEPARATOR = ",";
		public static final String FUNCTION_REF = "@";
	}

	/** The set of all operator symbols that act as delimiters in expressions. */
	public static final HashSet<String> OPERATOR_DELIMITERS = new HashSet<String>();
	
	/**
	 * A map from the operator symbol/keyword to the class for that operator. This makes it easy to
	 * instantiate an operator node of the correct type based on the operator symbol.
	 */
	public static final HashMap<String, Class<? extends OperatorNode>> OPERATORS = 
			new HashMap<String, Class<? extends OperatorNode>>();
	
	/**
	 * Puts an operator symbol and class pair into the {@link OPERATORS} and also adds the symbol
	 * {@link OPERATOR_DELIMITERS} if it is a delimiter.
	 * @param symbol the operator symbol
	 * @param operatorClass the class for the associated operator node
	 * @param delimiter whether the symbol is a delimiter.
	 */
	private static void putOperator(String symbol, Class<? extends OperatorNode> operatorClass,
			boolean delimiter) {
		OPERATORS.put(symbol, operatorClass);
		if(delimiter) {
			OPERATOR_DELIMITERS.add(symbol);
		}
	}
	
	static {
		putOperator(Symbols.ASSIGN, Assign.class, true);
		putOperator(Symbols.ASSIGN_ADD, AssignAdd.class, true);
		putOperator(Symbols.ASSIGN_SUBTRACT, AssignSubtract.class, true);
		putOperator(Symbols.ASSIGN_MULTIPLY, AssignMultiply.class, true);
		putOperator(Symbols.ASSIGN_DIVIDE, AssignDivide.class, true);
		putOperator(Symbols.ASSIGN_MOD, AssignMod.class, true);
		putOperator(Symbols.ASSIGN_EXPONENTIATE, AssignExponentiate.class, true);
		putOperator(Symbols.ADD, Add.class, true);
		putOperator(Symbols.SUBTRACT, Subtract.class, true);
		putOperator(Symbols.MULTIPLY, Multiply.class, true);
		putOperator(Symbols.DIVIDE, Divide.class, true);
		putOperator(Symbols.MOD, Mod.class, true);
		putOperator(Symbols.EXPONENTIATE, Exponentiate.class, true);
		putOperator(Symbols.EQUAL, EqualTo.class, true);
		putOperator(Symbols.NOT_EQUAL, NotEqualTo.class, true);
		putOperator(Symbols.LESS_THAN, LessThan.class, true);
		putOperator(Symbols.LESS_THAN_EQUAL, LessThanOrEqual.class, true);
		putOperator(Symbols.GREATER_THAN, GreaterThan.class, true);
		putOperator(Symbols.GREATER_THAN_EQUAL, GreaterThanOrEqual.class, true);
		putOperator(Strings.AND, LogicalAnd.class, false);
		putOperator(Strings.OR, LogicalOr.class, false);
		putOperator(Strings.NOT, LogicalNot.class, false);
		putOperator(Symbols.ARG_SEPARATOR, ArgSeparator.class, true);
		putOperator(Symbols.FUNCTION_REF, FunctionReference.class, true);
	}
	
	/**
	 * Returns whether the given symbol is an operator.
	 * @param symbol the symbol to check
	 * @return whether the symbol is an operator
	 */
	public static boolean isOperator(String symbol) {
		return OPERATORS.containsKey(symbol);
	}

	public static OperatorNode instantiateOperator(String symbol, SourceCode.Line source) {
		if(!isOperator(symbol)) {
			throw new IllegalArgumentException("This operator does not exist");
		}
		
		Class<? extends OperatorNode> operatorClass = OPERATORS.get(symbol);
		try {
			Constructor<? extends OperatorNode> constructor = operatorClass
					.getConstructor(SourceCode.Line.class);
			return constructor.newInstance(source);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** The symbol of this operator. */
	private String symbol;
	
	/**
	 * Constructs a new operator with a given type, symbol, and source line from which the node was
	 * created.
	 * @param type the type of operation
	 * @param symbol the symbol of the operator.
	 * @param source the line from which this node was generated
	 */
	public OperatorNode(Type type, String symbol, SourceCode.Line source) {
		super(type, source);
		this.symbol = symbol;
	}

	/**
	 * Returns the operation between the left and right-hand variable operands.
	 * @param left the left-hand operand
	 * @param right the right-hand operand
	 */
	protected abstract Variable doOperation(Variable left, Variable right);

	/**
	 * Returns the precedence of this operator. Higher precedences will bind tighter and therefore
	 * execute sooner. Lower precedences bind looser and execute later.
	 * @return the precedence of this operator
	 */
	public abstract int precedence();

	/**
	 * Performs the operation for this node, and returns the result as a {@link DataNode}.
	 * @param left the left-hand subtree operand
	 * @param right the right-hand subtree operand
	 * @return the result of the operation
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		Variable leftValue = null, rightValue = null;
		if(getOperationType() == Type.BINARY || getOperationType() == Type.PREFIX_UNARY) {
			rightValue = right.operate().getValue();
		}
		if(getOperationType() == Type.BINARY || getOperationType() == Type.POSTFIX_UNARY) {
			leftValue = left.operate().getValue();
		}
		try {
			return new DataNode(doOperation(leftValue, rightValue), getSourceLine());
		} catch(UnsupportedOperatorException e) {
			throw new UnsupportedOperatorException(getSymbol(), leftValue,
				rightValue, getSourceLine());
		} catch(PhoenixRuntimeException e) {
			throw e;
		}
	}
	
	/**
	 * Returns the symbol for this operator.
	 * @return the symbol for this operator
	 */
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return (left() != null ? left().toString() : "") + getSymbol() +
			   (right() != null ? right().toString() : "");
	}
	
	/**
	 * An operator node which executes the assign operation (=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Assign extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Assign(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {			
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assign(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}
	
	/**
	 * An operator node which executes the add and assign operation (+=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignAdd extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignAdd(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_ADD, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignAdd(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}
	
	/**
	 * An operator node which executes the subtract and assign operation (-=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignSubtract extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignSubtract(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_SUBTRACT, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignSubtract(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}
	
	/**
	 * An operator node which executes the multiply and assign operation (*=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignMultiply extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignMultiply(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_MULTIPLY, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignMultiply(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}
	
	/**
	 * An operator node which executes the divide and assign operation (/=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignDivide extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignDivide(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_DIVIDE, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignDivide(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}
	
	/**
	 * An operator node which executes the modulus and assign operation (%=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignMod extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignMod(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_MOD, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignMod(right);
		}

		@Override
		public int precedence() {
			return 0;
		}	
	}
	
	/**
	 * An operator node which executes the exponentiate and assign operation (^=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class AssignExponentiate extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public AssignExponentiate(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ASSIGN_EXPONENTIATE, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignExponentiate(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
	}

	/**
	 * An operator node which executes the add operation (+).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Add extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Add(SourceCode.Line source) {
			super(Type.BINARY, Symbols.ADD, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.add(right);
		}

		@Override
		public int precedence() {
			return 5;
		}
	}
	
	/**
	 * An operator node which executes the negate operation (-).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Negate extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Negate(SourceCode.Line source) {
			super(Type.PREFIX_UNARY, Symbols.SUBTRACT, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return right.negate();
		}

		@Override
		public int precedence() {
			return 8;
		}
	}
	
	/**
	 * An operator node which executes the subtract operation (-).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Subtract extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Subtract(SourceCode.Line source) {
			super(Type.BINARY, Symbols.SUBTRACT, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.subtract(right);
		}

		@Override
		public int precedence() {
			return 5;
		}
	}
	
	/**
	 * An operator node which executes the multiply operation (*).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Multiply extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Multiply(SourceCode.Line source) {
			super(Type.BINARY, Symbols.MULTIPLY, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.multiply(right);
		}

		@Override
		public int precedence() {
			return 6;
		}
	}
	
	/**
	 * An operator node which executes the divide operation (/).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Divide extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Divide(SourceCode.Line source) {
			super(Type.BINARY, Symbols.DIVIDE, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.divide(right);
		}

		@Override
		public int precedence() {
			return 6;
		}
	}
	
	/**
	 * An operator node which executes the mod operation (%).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Mod extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Mod(SourceCode.Line source) {
			super(Type.BINARY, Symbols.MOD, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.mod(right);
		}

		@Override
		public int precedence() {
			return 6;
		}
	}

	/**
	 * An operator node which executes the exponentiate operation (^).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Exponentiate extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public Exponentiate(SourceCode.Line source) {
			super(Type.BINARY, Symbols.EXPONENTIATE, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.exponentiate(right);
		}

		@Override
		public int precedence() {
			return 7;
		}
	}

	/**
	 * An operator node which executes the equality operation (==).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class EqualTo extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public EqualTo(SourceCode.Line source) {
			super(Type.BINARY, Symbols.EQUAL, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.equalTo(right);
		}

		@Override
		public int precedence() {
			return 3;
		}
	}

	/**
	 * An operator node which executes the inequality operation (!=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class NotEqualTo extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public NotEqualTo(SourceCode.Line source) {
			super(Type.BINARY, Symbols.NOT_EQUAL, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.notEqualTo(right);
		}

		@Override
		public int precedence() {
			return 3;
		}
	}
	
	/**
	 * An operator node which executes the less than operation (&lt;).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class LessThan extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public LessThan(SourceCode.Line source) {
			super(Type.BINARY, Symbols.LESS_THAN, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.lessThan(right);
		}

		@Override
		public int precedence() {
			return 4;
		}
	}
	
	/**
	 * An operator node which executes the less than or equal operation (&lt;=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class LessThanOrEqual extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public LessThanOrEqual(SourceCode.Line source) {
			super(Type.BINARY, Symbols.LESS_THAN_EQUAL, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.lessThanOrEqual(right);
		}

		@Override
		public int precedence() {
			return 4;
		}
	}

	/**
	 * An operator node which executes the greater than operation (&gt;).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class GreaterThan extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public GreaterThan(SourceCode.Line source) {
			super(Type.BINARY, Symbols.GREATER_THAN, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.greaterThan(right);
		}

		@Override
		public int precedence() {
			return 4;
		}
	}
	
	/**
	 * An operator node which executes the greater than or equal operation (&gt;=).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class GreaterThanOrEqual extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public GreaterThanOrEqual(SourceCode.Line source) {
			super(Type.BINARY, Symbols.GREATER_THAN_EQUAL, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.greaterThanOrEqual(right);
		}

		@Override
		public int precedence() {
			return 4;
		}	
	}

	/**
	 * An operator node which executes the logical and operation (and).
	 *
	 * @author Jonah Scheinerman
	 */	
	public static class LogicalAnd extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public LogicalAnd(SourceCode.Line source) {
			super(Type.BINARY, Strings.AND, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.logicalAnd(right);
		}

		@Override
		public int precedence() {
			return 2;
		}
	}
	
	/**
	 * An operator node which executes the logical or operation (or).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class LogicalOr extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public LogicalOr(SourceCode.Line source) {
			super(Type.BINARY, Strings.OR, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.logicalOr(right);
		}

		@Override
		public int precedence() {
			return 1;
		}
	}
	
	/**
	 * An operator node which executes the logical not operation (not).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class LogicalNot extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public LogicalNot(SourceCode.Line source) {
			super(Type.PREFIX_UNARY, Strings.NOT, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return right.logicalNot();
		}

		@Override
		public int precedence() {
			return 8;
		}
	}	

	/**
	 * An operator node which represents a separation between parameters or elements in an array.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class ArgSeparator extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public ArgSeparator(SourceCode.Line source) {
			super(Type.NONARY, Symbols.ARG_SEPARATOR, source);
		}
		
		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return null;
		}

		@Override
		public int precedence() {
			return 8;
		}
	}
	
	/**
	 * An operator node which executes the function reference operation (@).
	 *
	 * @author Jonah Scheinerman
	 */
	public static class FunctionReference extends OperatorNode {
		/**
		 * Construct the new operator from the source line.
		 * @param source the line of source code from which this node is generated
		 */
		public FunctionReference(SourceCode.Line source) {
			super(Type.PREFIX_UNARY, Symbols.FUNCTION_REF, source);
		}
		
		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(!(right instanceof FunctionVariable)) {
				throw new SyntaxException("@ operator must take a function type variable.", null);
			}
			return right;
		}

		@Override
		public int precedence() {
			return 9;
		}
	}

}
