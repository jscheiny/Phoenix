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
import net.scheinerman.phoenix.variables.*;

public abstract class OperatorNode extends ParseTreeNode {

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
		public static final String LOGICAL_AND = "&";
		public static final String LOGICAL_OR = "|";
		public static final String LOGICAL_NOT = "!";
		public static final String ARG_SEPARATOR = ",";
		public static final String FUNCTION_REF = "@";
	}
	
	public static final HashMap<String, Class<? extends OperatorNode>> OPERATORS = 
			new HashMap<String, Class<? extends OperatorNode>>();
	static {
		OPERATORS.put(Symbols.ASSIGN, Assign.class);
		OPERATORS.put(Symbols.ASSIGN_ADD, AssignAdd.class);
		OPERATORS.put(Symbols.ASSIGN_SUBTRACT, AssignSubtract.class);
		OPERATORS.put(Symbols.ASSIGN_MULTIPLY, AssignMultiply.class);
		OPERATORS.put(Symbols.ASSIGN_DIVIDE, AssignDivide.class);
		OPERATORS.put(Symbols.ASSIGN_MOD, AssignMod.class);
		OPERATORS.put(Symbols.ASSIGN_EXPONENTIATE, AssignExponentiate.class);
		OPERATORS.put(Symbols.ADD, Add.class);
		OPERATORS.put(Symbols.SUBTRACT, Subtract.class);
		OPERATORS.put(Symbols.MULTIPLY, Multiply.class);
		OPERATORS.put(Symbols.DIVIDE, Divide.class);
		OPERATORS.put(Symbols.MOD, Mod.class);
		OPERATORS.put(Symbols.EXPONENTIATE, Exponentiate.class);
		OPERATORS.put(Symbols.EQUAL, EqualTo.class);
		OPERATORS.put(Symbols.NOT_EQUAL, NotEqualTo.class);
		OPERATORS.put(Symbols.LESS_THAN, LessThan.class);
		OPERATORS.put(Symbols.LESS_THAN_EQUAL, LessThanOrEqual.class);
		OPERATORS.put(Symbols.GREATER_THAN, GreaterThan.class);
		OPERATORS.put(Symbols.GREATER_THAN_EQUAL, GreaterThanOrEqual.class);
		OPERATORS.put(Symbols.LOGICAL_AND, LogicalAnd.class);
		OPERATORS.put(Symbols.LOGICAL_OR, LogicalOr.class);
		OPERATORS.put(Symbols.LOGICAL_NOT, LogicalNot.class);
		OPERATORS.put(Symbols.ARG_SEPARATOR, ArgSeparator.class);
		OPERATORS.put(Symbols.FUNCTION_REF, FunctionReference.class);
	}
	
	
	private String symbol;
	
	public OperatorNode(Type type, String symbol, SourceCode.Line source) {
		super(type, source);
		this.symbol = symbol;
	}
	
	

	protected abstract Variable doOperation(Variable left, Variable right);

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
	
	public String getSymbol() { return symbol; }
	
	@Override
	public String toString() {
		return (left() != null ? left().toString() : "") + getSymbol() +
			   (right() != null ? right().toString() : "");
	}
	
	public static Set<String> getOperatorSymbols() {
		return OPERATORS.keySet();
	}

	public static Class<? extends OperatorNode> getOperator(String symbol) {
		return OPERATORS.get(symbol);
	}
	
	public static boolean isOperator(String symbol) {
		return OPERATORS.containsKey(symbol);
	}
	
	public static OperatorNode instantiateOperator(String symbol, SourceCode.Line source) {
		if(!isOperator(symbol)) {
			throw new IllegalArgumentException("This operator does not exist");
		}
		
		Class<? extends OperatorNode> operatorClass = getOperator(symbol);
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
	
	public static class Assign extends OperatorNode {
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
	
	public static class AssignAdd extends OperatorNode {
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
	
	public static class AssignSubtract extends OperatorNode {
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
	
	public static class AssignMultiply extends OperatorNode {
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
	
	public static class AssignDivide extends OperatorNode {
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
	
	public static class AssignMod extends OperatorNode {
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
	
	public static class AssignExponentiate extends OperatorNode {
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

	public static class Add extends OperatorNode {
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
	
	public static class Negate extends OperatorNode {
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
	
	public static class Subtract extends OperatorNode {
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
	
	public static class Multiply extends OperatorNode {
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
	
	public static class Divide extends OperatorNode {
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
	
	public static class Mod extends OperatorNode {
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

	public static class Exponentiate extends OperatorNode {
		public Exponentiate(SourceCode.Line source) {
			super(Type.BINARY, Symbols.EXPONENTIATE, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.divide(right);
		}

		@Override
		public int precedence() {
			return 7;
		}
	}

	public static class EqualTo extends OperatorNode {
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

	public static class NotEqualTo extends OperatorNode {
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
	
	public static class LessThan extends OperatorNode {
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
	
	public static class LessThanOrEqual extends OperatorNode {
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

	public static class GreaterThan extends OperatorNode {
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
	
	public static class GreaterThanOrEqual extends OperatorNode {
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
	
	public static class LogicalAnd extends OperatorNode {
		public LogicalAnd(SourceCode.Line source) {
			super(Type.BINARY, Symbols.LOGICAL_AND, source);
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
	
	public static class LogicalOr extends OperatorNode {
		public LogicalOr(SourceCode.Line source) {
			super(Type.BINARY, Symbols.LOGICAL_OR, source);
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
	
	public static class LogicalNot extends OperatorNode {
		public LogicalNot(SourceCode.Line source) {
			super(Type.PREFIX_UNARY, Symbols.LOGICAL_NOT, source);
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

	public static class ArgSeparator extends OperatorNode {
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
	
	public static class FunctionReference extends OperatorNode {
		public FunctionReference(SourceCode.Line source) {
			super(Type.PREFIX_UNARY, Symbols.FUNCTION_REF, source);
		}
		
		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return right;
		}

		@Override
		public int precedence() {
			return 9;
		}
	}

}
