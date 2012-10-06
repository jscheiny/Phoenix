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

	private static HashMap<String, Class<? extends OperatorNode>> operators = 
			new HashMap<String, Class<? extends OperatorNode>>();
	static {
		operators.put(Interpreter.Strings.ASSIGN, Assign.class);
		operators.put(Interpreter.Strings.ASSIGN_ADD, AssignAdd.class);
		operators.put(Interpreter.Strings.ASSIGN_SUBTRACT, AssignSubtract.class);
		operators.put(Interpreter.Strings.ASSIGN_MULTIPLY, AssignMultiply.class);
		operators.put(Interpreter.Strings.ASSIGN_DIVIDE, AssignDivide.class);
		operators.put(Interpreter.Strings.ASSIGN_MOD, AssignMod.class);
		operators.put(Interpreter.Strings.ASSIGN_EXPONENTIATE, AssignExponentiate.class);
		operators.put(Interpreter.Strings.ASSIGN_ROUND, AssignRound.class);
		operators.put(Interpreter.Strings.ADD, Add.class);
		operators.put(Interpreter.Strings.SUBTRACT, Subtract.class);
		operators.put(Interpreter.Strings.MULTIPLY, Multiply.class);
		operators.put(Interpreter.Strings.DIVIDE, Divide.class);
		operators.put(Interpreter.Strings.MOD, Mod.class);
		operators.put(Interpreter.Strings.EXPONENTIATE, Exponentiate.class);
		operators.put(Interpreter.Strings.ROUND, Round.class);
		operators.put(Interpreter.Strings.EQUAL, EqualTo.class);
		operators.put(Interpreter.Strings.NOT_EQUAL, NotEqualTo.class);
		operators.put(Interpreter.Strings.LESS_THAN, LessThan.class);
		operators.put(Interpreter.Strings.LESS_THAN_EQUAL, LessThanOrEqual.class);
		operators.put(Interpreter.Strings.GREATER_THAN, GreaterThan.class);
		operators.put(Interpreter.Strings.GREATER_THAN_EQUAL, GreaterThanOrEqual.class);
		operators.put(Interpreter.Strings.LOGICAL_AND, LogicalAnd.class);
		operators.put(Interpreter.Strings.LOGICAL_OR, LogicalOr.class);
		operators.put(Interpreter.Strings.LOGICAL_NOT, LogicalNot.class);
		operators.put(Interpreter.Strings.ARG_SEPARATOR, ArgSeparator.class);
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
			e.setSourceLine(getSourceLine());
			throw e;
		}
	}
	
	public String getSymbol() { return symbol; }
	
	@Override
	public String toString() {
		return getSymbol();
	}
	
	public static Set<String> getOperatorSymbols() {
		return operators.keySet();
	}

	public static Class<? extends OperatorNode> getOperator(String symbol) {
		return operators.get(symbol);
	}
	
	public static boolean isOperator(String symbol) {
		return operators.containsKey(symbol);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_ADD, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_SUBTRACT, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_MULTIPLY, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_DIVIDE, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_MOD, source);
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
			super(Type.BINARY, Interpreter.Strings.ASSIGN_EXPONENTIATE, source);
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
	
	public static class AssignRound extends OperatorNode {

		public AssignRound(SourceCode.Line source) {
			super(Type.BINARY, Interpreter.Strings.ASSIGN_ROUND, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			if(left.isLiteral())
				throw new SyntaxException("Cannot assign to literal value.", getSourceLine());
			return left.assignRound(right);
		}

		@Override
		public int precedence() {
			return 0;
		}
		
	}
	
	public static class Add extends OperatorNode {

		public Add(SourceCode.Line source) {
			super(Type.BINARY, Interpreter.Strings.ADD, source);
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
			super(Type.PREFIX_UNARY, Interpreter.Strings.SUBTRACT, source);
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
			super(Type.BINARY, Interpreter.Strings.SUBTRACT, source);
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
			super(Type.BINARY, Interpreter.Strings.MULTIPLY, source);
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
			super(Type.BINARY, Interpreter.Strings.DIVIDE, source);
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
			super(Type.BINARY, Interpreter.Strings.MOD, source);
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
			super(Type.BINARY, Interpreter.Strings.EXPONENTIATE, source);
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
	
	public static class Round extends OperatorNode {

		public Round(SourceCode.Line source) {
			super(Type.BINARY, Interpreter.Strings.ROUND, source);
		}

		@Override
		protected Variable doOperation(Variable left, Variable right) {
			return left.round(right);
		}

		@Override
		public int precedence() {
			return 5;
		}
		
	}
	
	public static class EqualTo extends OperatorNode {

		public EqualTo(SourceCode.Line source) {
			super(Type.BINARY, Interpreter.Strings.EQUAL, source);
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
			super(Type.BINARY, Interpreter.Strings.NOT_EQUAL, source);
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
			super(Type.BINARY, Interpreter.Strings.LESS_THAN, source);
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
			super(Type.BINARY, Interpreter.Strings.LESS_THAN_EQUAL, source);
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
			super(Type.BINARY, Interpreter.Strings.GREATER_THAN, source);
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
			super(Type.BINARY, Interpreter.Strings.GREATER_THAN_EQUAL, source);
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
			super(Type.BINARY, Interpreter.Strings.LOGICAL_AND, source);
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
			super(Type.BINARY, Interpreter.Strings.LOGICAL_OR, source);
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
			super(Type.PREFIX_UNARY, Interpreter.Strings.LOGICAL_NOT, source);
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
			super(Type.NONARY, Interpreter.Strings.ARG_SEPARATOR, source);
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

}
