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

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.parser.OperatorNode.Reference;
import net.scheinerman.phoenix.parser.ParseTreeNode.Surround;
import net.scheinerman.phoenix.parser.ParseTreeNode.Type;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

/**
 * Provides methods to translate Phoenix expressions into parse trees and then evaluate those
 * trees into the variable value of the expression. The two main public methods of this class
 * are
 * {@link Parser#parse(Interpreter, net.scheinerman.phoenix.interpreter.SourceCode.Line, ArrayList, int, int)}
 * and
 * {@link Parser#getParseTree(Interpreter, net.scheinerman.phoenix.interpreter.SourceCode.Line, ArrayList, int, int)}.
 * The latter builds a parse tree for an expression and returns this root of this parse tree.
 * The former builds the same tree but then executes that tree. Generating the parse tree is useful
 * for intepreting statements that will need to be executed multiple times, as this is a expensive
 * operation, and should be done as little as possible.
 *
 * @author Jonah Scheinerman
 */
public class Parser {
	
	/**
	 * Parses a string expression and returns its variable value.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param expression the string expression to parse
	 * @return the value of the expression
	 */
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			String expression) {
		return parse(interpreter, source, Tokenizer.tokenize(expression, source));
	}
	
	/**
	 * Parses a tokenized expression and returns its variable value.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @return the value of the expression
	 */
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens) {
		return parse(interpreter, source, tokens, 0);
	}

	/**
	 * Parses a tokenized expression starting from a given token and returns its variable value.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @param startIndex the index at which to start parsing
	 * @return the value of the expression starting at the start index
	 */
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex) {
		return parse(interpreter, source, tokens, startIndex, tokens.size() - 1);
	}
	
	/**
	 * Parses a tokenized expression starting from a given token and ending on another token and 
	 * returns its variable value.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @param startIndex the index at which to start parsing
	 * @param endIndex the index at which to stop parsing (inclusive)
	 * @return the value of the expression between the starting and ending indices (inclusive)
	 */
	public static Variable parse(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex, int endIndex) {
		return getParseTree(interpreter, source, tokens, startIndex, endIndex).operate()
			   .getValue();
	}
	
	/**
	 * Builds the parse tree for a given string expression. In order to find the value of the
	 * expression simply take the output and call the following:
	 * <code>Variable value = Parser.getParseTree( ... ).operate().getValue();</code>.
	 * This is useful for when an expression should be evaluated multiple times, and does not make
	 * sense to repeatedly build the tree.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param expression the string expression to parse
	 * @return the parse tree for the expression
	 */
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			String expression) {
		return getParseTree(interpreter, source, Tokenizer.tokenize(expression, source));
	}

	/**
	 * Builds the parse tree for a given tokenized expression.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @return the parse tree for the expression
	 */
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens) {
		return getParseTree(interpreter, source, tokens, 0);
	}

	/**
	 * Builds the parse tree for a given tokenized expression starting at a given token index.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @param startIndex the index at which to start parsing the expression
	 * @return the parse tree for the expression
	 */
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex) {
		return getParseTree(interpreter, source, tokens, startIndex, tokens.size() - 1);
	}

	/**
	 * Builds the parse tree for a given tokenized expression starting and ending at a given token
	 * indices.
	 * @param interpreter the interpreter that is being used to interpret the expression
	 * @param source the source line from which the expression was taken
	 * @param tokens the tokens containing the expression to parse
	 * @param startIndex the index at which to start parsing the expression
	 * @param endIndex the index at which to stop parsing the expression (inclusive)
	 * @return the parse tree for the expression between the starting and ending indices
	 * (inclusive).
	 */	
	public static ParseTreeNode getParseTree(Interpreter interpreter, SourceCode.Line source,
			ArrayList<Token> tokens, int startIndex, int endIndex) {
		if(!parensValid(tokens, startIndex, endIndex)) {
			throw new SyntaxException("Mismatching parentheses.", source);
		}
		return parse(interpreter, source, tokens, startIndex, endIndex, Surround.NONE);
	}
	
	/**
	 * Checks whether parentheses and brackets are formatted correctly within the tokenized
	 * expression.
	 * @param tokens the tokens to check
	 * @param startIndex the index at which to start checking.
	 * @param endIndex the index at which to stop checking (inclusive)
	 * @return true if the parentheses match, false if there is a mismatch
	 */
	private static boolean parensValid(ArrayList<Token> tokens, int startIndex, int endIndex) {
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

	/**
	 * Parses and builds a parse tree for a given expression. This is a recursive helper method that
	 * recurs on every nested parenthesized expression within the larger expression.
	 * @param interpreter the interpreter being used to parse the expression
	 * @param source the line from which the expression was taken
	 * @param tokens the tokens to parse into the expression tree
	 * @param start the index of the token at which to start parsing
	 * @param end the index of the token at which to end parsing (inclusive)
	 * @param surround the surround type of the given expression
	 * @return the parse tree built from the expression between the starting and ending indices
	 * (inclusive)
	 */
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
			} else if(OperatorNode.isOperator(token.getToken())) {
				if((nodes.size() == 0 || nodes.get(nodes.size() - 1) instanceof OperatorNode) && 
				   token.getToken().equals(OperatorNode.Symbols.SUBTRACT)) {
					nodes.add(new OperatorNode.Negate(source));
				} else {
					nodes.add(OperatorNode.instantiateOperator(token.getToken(), source));
				}
			} else {
				nodes.add(parseToken(token, interpreter, source));
			}
		}
		setUpReferences(nodes, source);
		setUpCalls(nodes, source);
		ParseTreeNode reduced = reduce(nodes, source, surround);
		if(!reduced.areOperandsSet()) {
			throw new SyntaxException("Missing operands.", source);
		}
		return reduced;
	}
	
	/** 
	 * Sets up function references in the list of nodes. If a {@link Reference}
	 * node is found in the list, then the next token after it is set as the function name that is
	 * being referenced.
	 * @param nodes the list of nodes in which to set up function references
	 */
	private static void setUpReferences(ArrayList<ParseTreeNode> nodes, SourceCode.Line source) {
		for(int index = 0; index < nodes.size(); index++) {
			ParseTreeNode curr = nodes.get(index);
			if(curr instanceof OperatorNode.Reference) {
				if(index == nodes.size() - 1) {
					throw new SyntaxException("@ operator requires right hand operand.", source);
				}
				ParseTreeNode next = nodes.get(index + 1);
				if(!(next instanceof ResolutionNode)) {
					throw new SyntaxException("@ operator must take a variable name.",
						source);
				}
				((ResolutionNode) next).setReferenced(true);
				curr.setRight(next);
				nodes.remove(index + 1);
			}
		}
	}
	
	/**
	 * Sets up call nodes so that when operating, proper function calls will occur.
	 * @param nodes the nodes in which to set up call nodes
	 * @param source the line from which the nodes were generated
	 */
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
					
					if(callee instanceof ResolutionNode) {
						((ResolutionNode) callee).setReferenced(true);
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
	
	/** 
	 * Reduces a list of {@link ParseTreeNode}s into a single node. This is done by setting up the
	 * operands to {@link OperatorNode} in the order of the operators' precedence. If after
	 * reducing, the list of nodes have not been reduced to a single variable, then it will be
	 * assumed that this is a list (either a list of parameters or an array) and the resulting list
	 * will be wrapped in a {@link ListNode}, and that will be returned.
	 * @param nodes the array of nodes to reduce
	 * @param source the line from which the list of nodes was generated
	 * @return a single node containing the tree built from the list of nodes
	 */
	private static ParseTreeNode reduce(ArrayList<ParseTreeNode> nodes, SourceCode.Line source,
			Surround surround) {
		while(nodes.size() > 1) {
			int maxPrecedence = -2;
			ParseTreeNode maxOperator = null;
			int maxOperatorIndex = -1;
			for(int index = 0; index < nodes.size(); index++) {
				ParseTreeNode node = nodes.get(index);
				if(node instanceof OperatorNode && !node.areOperandsSet()) {
					if(((OperatorNode) node).precedence() > maxPrecedence) {
						maxOperator = node;
						maxPrecedence = ((OperatorNode) node).precedence();
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
	
	/**
	 * Parses a token that is not an operator, and turns it into a {@link ParseTreeNode}. If the
	 * token is a variable literal, then a {@link DataNode} containing that literal value is
	 * returned. If the node is a language keyword, a {@link SyntaxException} is thrown. If none
	 * of the above, then the token is assumed to be a variable name and will be wrapped in a
	 * {@link ResolutionNode}. If it turns out this variable name does not exist, then at evaluation
	 * an error will get thrown.
	 * @param token the token to parse
	 * @param interpreter the interpreter being used to parse this token
	 * @param source the line from which this token was extracted 
	 * @return a node that represents this token
	 */
	private static ParseTreeNode parseToken(Token token, Interpreter interpreter,
			SourceCode.Line source) {
		String phrase = token.getToken();
		Variable literal = interpreter.getConfiguration().createVariableFromLiteral(interpreter,
			phrase, source);
		if(literal != null) {
			return new DataNode(literal, source);
		}
		if(Interpreter.KEYWORDS.contains(phrase)) {
			throw new SyntaxException("Unexpected keyword '" + phrase + "'.", source);
		}
		return new ResolutionNode(interpreter, phrase, source);
	}
}
