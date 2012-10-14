// Parser.java
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

import java.util.*;
import java.util.regex.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.parser.ParseTreeNode.Surround;
import net.scheinerman.phoenix.parser.ParseTreeNode.Type;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

public class Parser {
	
	private static final Pattern INTEGER_LITERAL = Pattern.compile("\\d+");
	private static final Pattern DOUBLE_LITERAL_1 = Pattern.compile("\\d+d");
	private static final Pattern DOUBLE_LITERAL_2 = Pattern.compile("\\.\\d+d?");
	private static final Pattern DOUBLE_LITERAL_3 = Pattern.compile("\\d+\\.\\d*d?");
	
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			String expression) {
		return parse(interpreter, source, Tokenizer.tokenize(expression, source));
	}
	
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens) {
		return parse(interpreter, source, tokens, 0);
	}

	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex) {
		return parse(interpreter, source, tokens, startIndex, tokens.size() - 1);
	}
	
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex, int endIndex) {
		return getParseTree(interpreter, source, tokens, startIndex, endIndex).operate()
			   .getValue();
	}
	
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			String expression) {
		return getParseTree(interpreter, source, Tokenizer.tokenize(expression, source));
	}
	
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens) {
		return getParseTree(interpreter, source, tokens, 0);
	}

	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex) {
		return getParseTree(interpreter, source, tokens, startIndex, tokens.size() - 1);
	}
	
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex, int endIndex) {
		if(!parensValid(tokens, startIndex, endIndex)) {
			throw new SyntaxException("Mismatching parentheses.", source);
		}
		return parse(interpreter, source, tokens, startIndex, endIndex, Surround.NONE);
	}
	
	private static ParseTreeNode parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int start, int end, Surround surround) {
		if(end < start) {
			if(surround != Surround.NONE) {
				throw new SyntaxException("Cannot parse empty parenthetical.", source);
			} else {
				throw new SyntaxException("Cannot parse empty expression.", source);
			}
		}
		
		ArrayList<ParseTreeNode> nodes = new ArrayList<ParseTreeNode>(end - start);
		for(int index = start; index <= end; index++) {
			Token token = tokens.get(index);
			if(token.getToken().equals("(")) {
				int open = 1;
				int close;
				for(close = index + 1; close <= end; close++) {
					if(tokens.get(close).getToken().equals("(")) open++;
					if(tokens.get(close).getToken().equals(")")) open--;
					if(open == 0) break;
				}
				nodes.add(parse(interpreter, source, tokens, index + 1, close - 1,
								Surround.PARENTHESES));
				index = close;
			} else if(token.getToken().equals("[")) {
				int open = 1;
				int close;
				for(close = index + 1; close <= end; close++) {
					if(tokens.get(close).getToken().equals("[")) open++;
					if(tokens.get(close).getToken().equals("]")) open--;
					if(open == 0) break;
				}
				nodes.add(parse(interpreter, source, tokens, index + 1, close - 1,
								Surround.BRACKETS));
				index = close;
			} else if(token.isDelimiter()) {
				if((nodes.size() == 0 || nodes.get(nodes.size() - 1) instanceof OperatorNode) && 
				   token.getToken().equals(OperatorNode.Symbols.SUBTRACT)) {
					nodes.add(new OperatorNode.Negate(source));
				} else {
					nodes.add(parseOperator(token, source));
				}
			} else {
				nodes.add(parseToken(token, interpreter, source));
			}
		}
		setUpReferences(nodes, source);
		setUpCalls(nodes, source);
		ParseTreeNode reduced = reduce(nodes, source, surround);
		return reduced;
	}
	
	private static void setUpReferences(ArrayList<ParseTreeNode> nodes, SourceCode.Line source) {
		for(int index = 0; index < nodes.size(); index++) {
			ParseTreeNode curr = nodes.get(index);
			if(curr instanceof OperatorNode.FunctionReference) {
				if(index == nodes.size() - 1) {
					throw new SyntaxException("@ operator requires right hand operand.", source);
				}
				ParseTreeNode next = nodes.get(index + 1);
				if(!(next instanceof DataNode)) {
					throw new SyntaxException("@ operator must take a function type variable.",
						source);
				}
				if(!(((DataNode)next).getValue() instanceof FunctionVariable)) {
					throw new SyntaxException("@ operator must take a function type variable.",
						source);
				}
				curr.setRight(next);
				nodes.remove(index + 1);
			}
		}
	}
	
	private static void setUpCalls(ArrayList<ParseTreeNode> nodes, SourceCode.Line source) {
		for(int index = 0; index < nodes.size(); index++) {
			ParseTreeNode callee = nodes.get(index);
			if(!(callee instanceof OperatorNode)) {
				ParseTreeNode left = null, right = null;
				if(index != 0) {
					left = nodes.get(index - 1);
				}
				if(index != nodes.size() - 1) {
					right = nodes.get(index + 1);
				}

				boolean leftOperandValid = (left != null &&
											left.getSurround() == Surround.PARENTHESES);
				boolean rightOperandValid = (right != null &&
											 right.getSurround() == Surround.PARENTHESES);
				
				if(leftOperandValid || rightOperandValid) {
					ParseTreeNode.Type type;
					if(leftOperandValid && rightOperandValid) {
						type = Type.BINARY;
					} else if(leftOperandValid) {
						type = Type.POSTFIX_UNARY;
					} else {
						type = Type.PREFIX_UNARY;
					}
					
					CallNode call = new CallNode(callee, type, source);
					nodes.set(index, call);

					if(leftOperandValid) {
						call.setLeft(left);
						nodes.remove(index - 1);
						index--;
					}
					if(rightOperandValid) {
						nodes.remove(index + 1);
						call.setRight(right);
						index--;
					}
				} else if(callee instanceof DataNode) {
					if(((DataNode)callee).getValue() instanceof FunctionVariable) {
						CallNode call = new CallNode(callee, Type.NONARY, source);
						nodes.set(index, call);
					}
				}
			}
		}
	}
	
	private static ParseTreeNode reduce(ArrayList<ParseTreeNode> nodes, SourceCode.Line source,
			Surround surround) {
		while(nodes.size() > 1) {
			int maxPrecedence = -2;
			ParseTreeNode maxOperator = null;
			int maxOperatorIndex = -1;
			for(int index = 0; index < nodes.size(); index++) {
				ParseTreeNode node = nodes.get(index);
				if(node instanceof OperatorNode && !node.areOperandsSet()) {
					if(node.precedence() > maxPrecedence) {
						maxOperator = node;
						maxPrecedence = node.precedence();
						maxOperatorIndex = index;
					}
				}
			}

			if(maxOperator == null) {
				break;
			}
			
			if(maxOperator.getOperationType() == Type.BINARY) {
				if(maxOperatorIndex == 0) {
					throw new SyntaxException("No left-hand operand for operator " + 
							((OperatorNode)maxOperator).getSymbol(), source);
				}	
				if(maxOperatorIndex == nodes.size() - 1) {
					throw new SyntaxException("No right-hand operand for operator " + 
							((OperatorNode)maxOperator).getSymbol(), source);
				}
				maxOperator.setLeft(nodes.get(maxOperatorIndex - 1));
				maxOperator.setRight(nodes.get(maxOperatorIndex + 1));
				
				nodes.remove(maxOperatorIndex - 1);
				maxOperatorIndex--;
				nodes.remove(maxOperatorIndex + 1);
			} else if(maxOperator.getOperationType() == Type.POSTFIX_UNARY) {
				if(maxOperatorIndex == 0)
					throw new SyntaxException("No left-hand operand for operator " + 
							((OperatorNode)maxOperator).getSymbol(), source);
	
				maxOperator.setLeft(nodes.get(maxOperatorIndex - 1));
				nodes.remove(maxOperatorIndex - 1);
			} else if(maxOperator.getOperationType() == Type.PREFIX_UNARY) {
				if(maxOperatorIndex == nodes.size() - 1)
					throw new SyntaxException("No right-hand operand for operator " + 
							((OperatorNode)maxOperator).getSymbol(), source);
	
				maxOperator.setRight(nodes.get(maxOperatorIndex + 1));
				nodes.remove(maxOperatorIndex + 1);
			}
		}
		
		if(nodes.size() != 1 || surround == Surround.BRACKETS) {
			try {
				ListNode node = new ListNode(source, nodes);
				node.setSurround(surround);
				return node;
			} catch(PhoenixRuntimeException e) {
				throw new SyntaxException("Invalid expression", source);
			}
		}
		
		if(surround != Surround.NONE) {
			SurroundNode encapsulate = new SurroundNode(nodes.get(0), source);
			encapsulate.setSurround(surround);
			return encapsulate;
		}
		return nodes.get(0);
	}
	
	private static ParseTreeNode parseToken(Token token, Interpreter interpreter,
			SourceCode.Line source) {
		String phrase = token.getToken();
		// Parse string literal...
		if(phrase.charAt(0) == '"' || phrase.charAt(0) == '\'') {
			StringVariable var = new StringVariable(phrase, true, source);
			return new DataNode(var, source);
		}
		// Parse type literal...
		if(Interpreter.Strings.TYPES.contains(phrase)) {
			TypeVariable var = new TypeVariable(phrase);
			return new DataNode(var, source);
		}
		// Parse integral literal...
		if(INTEGER_LITERAL.matcher(phrase).matches()) {
			IntegerVariable var = new IntegerVariable(Integer.parseInt(phrase));
			var.setLiteral(true);
			return new DataNode(var, source);
		}
		// Parse double literal...
		if(DOUBLE_LITERAL_1.matcher(phrase).matches() ||
		   DOUBLE_LITERAL_2.matcher(phrase).matches() ||
		   DOUBLE_LITERAL_3.matcher(phrase).matches()) {
			DoubleVariable var;
			if(phrase.endsWith("d")) {
				phrase = phrase.substring(0, phrase.length() - 1);
			}
			var = new DoubleVariable(Double.parseDouble(phrase));
			return new DataNode(var, source);
		}
		// Parse boolean literal...
		if(phrase.equals(Interpreter.Strings.TRUE) || phrase.equals(Interpreter.Strings.FALSE)) {
			BooleanVariable var = new BooleanVariable(phrase.equals(Interpreter.Strings.TRUE));
			return new DataNode(var, source);
		}
		// Throw error if keyword...
		if(Interpreter.KEYWORDS.contains(phrase)) {
			throw new SyntaxException("Unexpected keyword '" + phrase + "'.", source);
		}
		return new ResolutionNode(interpreter, phrase, source);
	}
	
	private static OperatorNode parseOperator(Token token, SourceCode.Line source) {
		String symbol = token.getToken();
		if(OperatorNode.isOperator(symbol)) {
			return OperatorNode.instantiateOperator(symbol, source);
		}
		return null;
	}
	
	public static boolean parensValid(ArrayList<Token> tokens, int startIndex, int endIndex) {
		Stack<String> parens = new Stack<String>();
		for(int index = startIndex; index <= endIndex; index++) {
			String token = tokens.get(index).getToken();
			if(token.equals("(")) {
				parens.push(")");
			}
			if(token.equals("[")) {
				parens.push("]");
			}
			if(token.equals(")") || token.equals("]")) {
				if(parens.isEmpty())
					return false;
				if(!parens.pop().equals(token))
					return false;
			}
		}
		return parens.isEmpty();
	}
}
