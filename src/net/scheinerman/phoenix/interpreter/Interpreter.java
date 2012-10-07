// Interpreter.java
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

package net.scheinerman.phoenix.interpreter;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

/**
 * Interprets and executes a piece of {@link SourceCode}. This class is the root of all
 * intepreters for Phoenix. Subclasses should handle specific interpretation situations such as
 * conditional structures, loops, functions, etc. Each time this needs to enter a new block, 
 * a new Interpeter will be created and its {@link Interpreter#interpret()} method will get called.
 * 
 * @author Jonah Scheinerman
 */
public class Interpreter {

	/**
	 * Contains constant strings that are useful for interpretation of Phoenix code. This includes
	 * keywords, operators, type names, and more.
	 * 
	 * TODO Extract these strings into XML, and make XML passable to constructor. That way,
	 * different naming schemes can be used by the user.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Strings {

		/** Line separator characters. */
		public static final HashSet<String> LINE_SEP = new HashSet<String>();
		static {
			LINE_SEP.add("\n");
			LINE_SEP.add("\r");
		}
		
		/** The comment start character. */
		public static final String COMMENT_START = "//";
		
		// Operators
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
		
		// Keywords
		public static final String IF = "if";
		public static final String ELSE = "else";
		public static final String SWITCH = "switch";
		public static final String CASE = "case";
		public static final String DEFAULT = "default";
		public static final String DO = "do";
		public static final String WHILE = "while";
		public static final String UNTIL = "until";
		public static final String FOR = "for";
		public static final String OTHERWISE = "otherwise";
		public static final String BREAK = "break";
		public static final String CONTINUE = "continue";
		public static final String TRUE = "true";
		public static final String FALSE = "false";
		public static final String INTEGER = "int";
		public static final String DOUBLE = "double";
		public static final String STRING = "str";
		public static final String BOOLEAN = "bool";
		public static final String TUPLE = "tuple";
		public static final String TYPE = "type";
		
		/** Contains all keywords (includes all named types). */
		public static final HashSet<String> KEYWORDS = new HashSet<String>();

		/** Contains all named types. */
		public static final HashSet<String> TYPES = new HashSet<String>();

		/** Contains all operator symbols. */
		public static final HashSet<String> OPERATORS = new HashSet<String>();

		// Initialize keywords, types, and operators.
		static {
			KEYWORDS.add(IF);
			KEYWORDS.add(ELSE);
			KEYWORDS.add(SWITCH);
			KEYWORDS.add(CASE);
			KEYWORDS.add(DEFAULT);
			KEYWORDS.add(DO);
			KEYWORDS.add(WHILE);
			KEYWORDS.add(UNTIL);
			KEYWORDS.add(FOR);
			KEYWORDS.add(OTHERWISE);
			KEYWORDS.add(BREAK);
			KEYWORDS.add(CONTINUE);
			KEYWORDS.add(INTEGER);
			KEYWORDS.add(DOUBLE);
			KEYWORDS.add(STRING);
			KEYWORDS.add(BOOLEAN);
			KEYWORDS.add(TYPE);
			KEYWORDS.add(TRUE);
			KEYWORDS.add(FALSE);

			TYPES.add(INTEGER);
			TYPES.add(DOUBLE);
			TYPES.add(STRING);
			TYPES.add(BOOLEAN);
			TYPES.add(TYPE);
			TYPES.add(TUPLE);

			OPERATORS.add(ASSIGN);
			OPERATORS.add(ASSIGN_ADD);
			OPERATORS.add(ASSIGN_SUBTRACT);
			OPERATORS.add(ASSIGN_MULTIPLY);
			OPERATORS.add(ASSIGN_DIVIDE);
			OPERATORS.add(ASSIGN_MOD);
			OPERATORS.add(ASSIGN_ROUND);
			OPERATORS.add(ASSIGN_EXPONENTIATE);
			OPERATORS.add(ADD);
			OPERATORS.add(SUBTRACT);
			OPERATORS.add(MULTIPLY);
			OPERATORS.add(DIVIDE);
			OPERATORS.add(MOD);
			OPERATORS.add(ROUND);
			OPERATORS.add(EXPONENTIATE);
			OPERATORS.add(EQUAL);
			OPERATORS.add(NOT_EQUAL);
			OPERATORS.add(LESS_THAN);
			OPERATORS.add(LESS_THAN_EQUAL);
			OPERATORS.add(GREATER_THAN);
			OPERATORS.add(GREATER_THAN_EQUAL);
			OPERATORS.add(LOGICAL_AND);
			OPERATORS.add(LOGICAL_OR);
			OPERATORS.add(LOGICAL_NOT);
			OPERATORS.add(ARG_SEPARATOR);
		}

	}

	/** A pattern which matches only valid variable names. */
	private static final Pattern VALID_NAME = Pattern.compile("[\\w_][\\w\\d_]*");

	/** The source code that is being interpreted. */
	private SourceCode source;
	
	/** The line on which to start interpreting. */
	private int start;
	
	/** The line after which to stop interpreting. */
	private int end;
	
	/** Whether this is the top level interpreter. */
	private boolean topLevel;
	
	/** The parent interpreter for this interpreter. */
	private Interpreter parent;

	/** The VAT containing all of the variables for this interpretation. */
	private VariableAllocationTable vat;

	/** Whether this is the child of a loop interpreter. */
	private boolean loopChild = false;
	
	/**
	 * Constructs a new interpreter that interprets the code at a given file path.
	 * @param path the path to the file to interpret
	 * @throws FileNotFoundException if the file path does not exist
	 */
	public Interpreter(String path) throws FileNotFoundException {
		this(new SourceCode(path));
		topLevel = true;
	}

	/**
	 * Constructs a new interpreter that interprets the code in a given file.
	 * @param file the file to interpret
	 * @throws FileNotFoundException if the file does not exist
	 */
	public Interpreter(File file) throws FileNotFoundException {
		this(new SourceCode(file));
		topLevel = true;
	}

	/**
	 * Constructs a new interpreter that interprets all of the given source code.
	 * @param source the source code to interpret
	 */
	public Interpreter(SourceCode source) {
		this(null, source, 0, source.size() - 1, false);
		topLevel = true;
	}

	/**
	 * Constructs a new interpreter that is interprets a section of the given source code for the
	 * parent interpreter, before returning control to that parent.
	 * @param parent the parent of this interpreter that is delegating interpretation of a section
	 * of code to this interpreter
	 * @param source the source code from which the interpreted code will be taken
	 * @param start the index of the start line for interpretation
	 * @param end the index of the last line for interpretation (this line will be interpreted)
	 * @param loopChild whether this is a descendant of a loop interpreter
	 */
	public Interpreter(Interpreter parent, SourceCode source, int start, int end,
			boolean loopChild) {
		this.parent = parent;
		this.source = source;
		this.start = start;
		this.end = end;
		this.loopChild = loopChild;
		if(this.parent == null) {
			topLevel = true;
			vat = new VariableAllocationTable();
		} else {
			vat = parent.getVAT();
		}
	}
	
	/**
	 * Retrieves the variable allocation table associated with this interpration. This VAT is shared
	 * by this interpreter, all of its parents and any children it may have.
	 * @return the VAT associated with this interpretation
	 */
	public final VariableAllocationTable getVAT() {
		return vat;
	}

	/**
	 * Returns the line that this interpretation started on.
	 * @return the index of the first line of interpretation
	 */
	public final int getStartLine() {
		return start;
	}
	
	/**
	 * Returns the line that this interpretation ends on.
	 * @return the index of the last line of interpretation
	 */
	public final int getEndLine() {
		return end;
	}
	
	/**
	 * Returns the source code that is being interpreted by this interpreter.
	 * @return the source code being interpreted
	 */
	public final SourceCode getSourceCode() {
		return source;
	}
	
	/**
	 * Interprets and executes the code that this interpreter has been delegated.
	 */
	public void interpret() {
		vat.pushStackFrame();

		try {			
			for(int index = start; index <= end; index++) {
				SourceCode.Line line = source.line(index);
				String content = line.getLineContent();
				ArrayList<Token> tokenization = Tokenizer.tokenize(content, line);
				// If the current line is indented more than the previous line, and we are not at
				// the start of the interpretation, then this is an indentation error.
				if(index != start && line.indentGreaterThan(source.line(index - 1).getIndent())) {
					throw new IndentException("Unexpected indented block", line);
				}
	
				if(isIf(tokenization)) {
					index = handleIf(tokenization, line, index);

				} else if(isDo(tokenization)) {
					index = handleDo(tokenization, line, index);
				
				} else if(isFor(tokenization)) {
					index = handleFor(tokenization, line, index);
				
				} else if(isWhile(tokenization)) {
					index = handleWhile(tokenization, line, index);
				
				} else if(isUntil(tokenization)) {
					index = handleUntil(tokenization, line, index);
				
				} else if(content.equals(Strings.BREAK)) {
					index = handleBreak(line, index);
	
				} else if(content.equals(Strings.CONTINUE)) {
					index = handleContinue(line, index);
	
				} else if(isElse(tokenization)) {
					throw new SyntaxException("Cannot have " + Strings.ELSE +
						" block outside of " + Strings.IF + " clause.", line);
					
				} else if(isElseIf(tokenization)) {
					throw new SyntaxException("Cannot have " + Strings.ELSE + " " + Strings.IF + 
						" block outside of " + Strings.IF + " clause.", line);
				
				} else if(isOtherwise(tokenization)) {
					throw new SyntaxException("Cannot have " + Strings.OTHERWISE +
						" block outside of loop clause.", line);
				
				} else if(isCase(tokenization)) {
					throw new SyntaxException("Cannot have " + Strings.CASE +
						" statement outside of " + Strings.SWITCH + " block.", line);
			
				} else if(isDefault(tokenization)) {
					throw new SyntaxException("Cannot have " + Strings.DEFAULT +
						" statement outside of " + Strings.SWITCH + " block.", line);
					
				} else if(isInitialization(tokenization)) {
					handleInitialization(tokenization, line);

				} else if(tokenization.size() > 0 && tokenization.get(0).getToken().equals("print")) {
					Variable var = Parser.parse(this, line, tokenization, 1);
					System.out.println(var + (var.isLiteral() ? "\t[l]" : ""));
					
				} else if(!content.isEmpty()) {
					Parser.parse(this, line, tokenization);
				}
				
			}
		} catch(PhoenixRuntimeException phoenixException) {
			if(topLevel) {
				phoenixException.printTrace();
			} else {
				throw phoenixException;
			}
		} catch(Exception exception) {
			System.err.println("An internal error has occurred. Please send the following output\n"+
							   "and the code that produced this error to jonah@scheinerman.net");
			exception.printStackTrace();
			System.exit(1);
		}
		
		vat.popStackFrame();
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an if statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an if statement
	 */
	private boolean isIf(ArrayList<Token> tokens) {
		return tokens.size() >= 1 && tokens.get(0).getToken().equals(Strings.IF);
	}

	/**
	 * Returns whether the line represented by the tokenization is an else if statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an else if statement
	 */
	private boolean isElseIf(ArrayList<Token> tokens) {
		return tokens.size() >= 2 &&
			   tokens.get(0).getToken().equals(Strings.ELSE) &&
			   tokens.get(1).getToken().equals(Strings.IF);
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an else statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an else statement
	 */
	private boolean isElse(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Strings.ELSE) &&
			   tokens.get(1).getToken().equals(":");
	}

	/**
	 * Returns whether the line represented by the tokenization is a do statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a do statement
	 */
	protected static final boolean isDo(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Strings.DO) &&
			   tokens.get(1).getToken().equals(":");
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an otherwise statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an otherwise statement
	 */
	protected static final boolean isOtherwise(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Strings.OTHERWISE) &&
			   tokens.get(1).getToken().equals(":");
	}

	/**
	 * Returns whether the line represented by the tokenization is a for statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a for statement
	 */
	protected static final boolean isFor(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Strings.FOR);
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an until statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an until statement
	 */
	protected static final boolean isUntil(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Strings.UNTIL);
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a while statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a while statement
	 */
	protected static final boolean isWhile(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Strings.WHILE);
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a case statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a case statement
	 */
	protected static final boolean isCase(ArrayList<Token> tokens) {
		return tokens.size() > 1 &&
			   tokens.get(0).getToken().equals(Strings.CASE);
	}

	/**
	 * Returns whether the line represented by the tokenization is a default statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a default statement
	 */
	private boolean isDefault(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Strings.DEFAULT) &&
			   tokens.get(1).getToken().equals(":");
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an initialization statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an initialization statement
	 */
	protected static final boolean isInitialization(ArrayList<Token> tokens) {
		return tokens.size() >= 3 &&
			   Strings.TYPES.contains(tokens.get(0).getToken()) &&
			   tokens.get(2).getToken().equals("=");
	}
	
	/**
	 * Handles an if block. This checks to make sure the if statement is valid, collects any
	 * else-if/else blocks that are associated with them and interprets them using an
	 * {@link IfExecutor}. This then returns the line number on which the execution finished.
	 * @param tokens the tokenization of the line containing the if statement
	 * @param line the source line containing the if statement
	 * @param index the index of the line
	 * @return the line on which execution in this interpretation should continue
	 */
	protected int handleIf(ArrayList<Token> tokens, SourceCode.Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Strings.IF + " statements must end in a colon.", line);
		}
		IfExecutor ifExec = new IfExecutor();
		
		boolean firstLoop = true;;
		boolean done = false;
		int currentLine = index;
		
		while(!done) {
			SourceCode.Line current = source.line(currentLine);
			String currentContent = current.getLineContent();
			ArrayList<Token> lineTokenization;
			int startToken = 1;
			if(firstLoop) {
				firstLoop = false;
				lineTokenization = tokens;
				startToken = 1;
			} else {
				lineTokenization = Tokenizer.tokenize(currentContent, current);
				if(isElseIf(lineTokenization)) {
					if(!lineTokenization.get(lineTokenization.size() - 1).getToken().equals(":")) {
						throw new SyntaxException(Strings.ELSE + " " + Strings.IF + 
							" statements must end in a colon.", line);
					}
					startToken = 2;
				} else if(isElse(lineTokenization)) {
					done = true;
				// We've hit a block that isn't an elif or else, so we must have reached the end
				// if the if/elif/else block.	
				} else {
					break;
				}
			}

			int endCurrentBlock = source.getBlockEnd(currentLine);
			
			// If we're not done then we're still in an if/elif block. Add this to the executor.
			if(!done) {
				IfConditionInterpreter conditionInterpreter = new IfConditionInterpreter(this,
					source, currentLine + 1, endCurrentBlock, loopChild, lineTokenization,
					startToken, lineTokenization.size() - 2);
				ifExec.addCondition(conditionInterpreter);

			// We're done if we've hit an else, so deal with that.
			} else {
				ElseInterpreter elseInterpreter = new ElseInterpreter(this, source,
					currentLine + 1, endCurrentBlock, loopChild);
				ifExec.setElseIntepreter(elseInterpreter);
			}				
			currentLine = endCurrentBlock + 1;
		}

		ifExec.execute();
		
		return currentLine - 1;
	}
	
	/**
	 * Handles a do loop. This checks to make sure the do statement is valid by looking for
	 * a while or an until at the end of the block. This then calls either
	 * {@link #handleDoUntil(ArrayList, net.scheinerman.phoenix.interpreter.SourceCode.Line, net.scheinerman.phoenix.interpreter.SourceCode.Line)}
	 * or
	 * {@link #handleDoWhile(ArrayList, net.scheinerman.phoenix.interpreter.SourceCode.Line, net.scheinerman.phoenix.interpreter.SourceCode.Line)}
	 * as is appropriate. This then returns the line on which the execution of the loop finished.
	 * @param tokens the tokenization of the line containing the do statement
	 * @param line the source line containing the do statement
	 * @param index the index of the line
	 * @return the line on which execution in this interpretation should continue
	 */
	protected int handleDo(ArrayList<Token> tokens, SourceCode.Line line, int index) {
		int doEnd = source.getBlockEnd(index);
		SourceCode.Line endLine = source.line(doEnd + 1);
		String endLineContent = endLine.getLineContent();
		ArrayList<Token> endTokens = Tokenizer.tokenize(endLineContent, endLine);
		// If the do loop ends with a while:
		if(isWhile(endTokens)) {
			handleDoWhile(endTokens, line, endLine);

		// If the do loop ends with a unti:
		} else if(isUntil(endTokens)) {
			handleDoUntil(endTokens, line, endLine);

		// If none of the above, the end of the loop is invalid.
		} else {
			throw new SyntaxException("Invalid end of " + Strings.DO + " loop.", endLine);
		}
		
		return doEnd + 1;
	}

	/**
	 * Handles a do-while loop. This creates and executes a {@link DoWhileInterpreter}.
	 * @param whileTokens the tokens on the line containing the while statement
	 * @param doLine the line containing the do statement
	 * @param whileLine the line containing the while statement
	 */
	private void handleDoWhile(ArrayList<Token> whileTokens, SourceCode.Line doLine,
			SourceCode.Line whileLine) {
		DoWhileInterpreter doWhileInterpreter = new DoWhileInterpreter(this, source,
				doLine.getNumber() + 1, whileLine.getNumber() - 1, whileLine, whileTokens, 1,
				whileTokens.size() - 1);
		doWhileInterpreter.interpret();
	}

	/**
	 * Handles a do-until loop. This creates and executes a {@link DoUntilInterpreter}.
	 * @param whileTokens the tokens on the line containing the while statement
	 * @param doLine the line containing the do statement
	 * @param whileLine the line containing the while statement
	 */
	private void handleDoUntil(ArrayList<Token> untilTokens, SourceCode.Line doLine,
			SourceCode.Line untilLine) {
		DoUntilInterpreter doUntilInterpreter = new DoUntilInterpreter(this, source,
				doLine.getNumber() + 1, untilLine.getNumber() - 1, untilLine, untilTokens, 1,
				untilTokens.size() - 1);
		doUntilInterpreter.interpret();
	}
	
	/**
	 * Handles a for loop. This checks to make sure the for statement is correctly formulated, and
	 * then executes a ForInterpreter. This then returns the line on which execution of the for loop
	 * finished.
	 * @param tokens the tokenization of the line containing the for statement
	 * @param line the source line containing the for statement
	 * @param index the index of the line
	 * @return the line on which execution in this interpretation should continue
	 */
	protected int handleFor(ArrayList<Token> tokens, SourceCode.Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Strings.FOR + " statements must end in a colon.", line);
		}
		
		int semicolonCount = 0;
		for(int tokenIndex = 1; tokenIndex <= tokens.size() - 2; tokenIndex++) {
			if(tokens.get(tokenIndex).getToken().equals(";")) {
				semicolonCount++;
			}
		}
		if(semicolonCount != 2) {
			throw new SyntaxException(Strings.FOR + " predicates must have three components.",
				line);
		}
		
		int forEnd = source.getBlockEnd(index);

		// TODO Instantiate and run ForIntepreter.
		
		return forEnd;
	}

	/**
	 * Handles a while loop. This checks to make sure the while statement is correctly formulated,
	 * and then executes a {@link WhileInterpreter}. This then returns the line on which execution
	 * of the for loop finished.
	 * @param tokens the tokenization of the line containing the while statement
	 * @param line the source line containing the while statement
	 * @param index the index of the line
	 * @return the line on which execution in this interpretation should continue
	 */
	protected int handleWhile(ArrayList<Token> tokens, SourceCode.Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Strings.WHILE + " statements must end in a colon.", line);
		}

		int whileEnd = source.getBlockEnd(index);
		int interpetationContineLine = whileEnd;
		SourceCode.Line whileEndLine = source.line(whileEnd + 1);
		ArrayList<Token> whileEndTokens = Tokenizer.tokenize(whileEndLine.getLineContent(),
			whileEndLine);
		OtherwiseInterpreter otherwise = null;

		if(isOtherwise(whileEndTokens)) {
			int otherwiseEnd = source.getBlockEnd(whileEnd + 1);
			otherwise = new OtherwiseInterpreter(this, source, whileEnd + 2, otherwiseEnd,
				loopChild);
			interpetationContineLine = otherwiseEnd;
		}

		WhileInterpreter whileInterpreter = new WhileInterpreter(this, source, index + 1, whileEnd,
			tokens, 1, tokens.size() - 2, otherwise);
		whileInterpreter.interpret();

		return interpetationContineLine;
	}

	/**
	 * Handles an until loop. This checks to make sure the until statement is correctly formulated,
	 * and then executes an {@link UntilInterpreter}. This then returns the line on which execution
	 * of the for loop finished.
	 * @param tokens the tokenization of the line containing the until statement
	 * @param line the source line containing the until statement
	 * @param index the index of the line
	 * @return the line on which execution in this interpretation should continue
	 */
	protected int handleUntil(ArrayList<Token> tokens, SourceCode.Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Strings.UNTIL + " statements must end in a colon.", line);
		}
	
		int untilEnd = source.getBlockEnd(index);
		int interpetationContineLine = untilEnd;
		SourceCode.Line untilEndLine = source.line(untilEnd + 1);
		ArrayList<Token> untilEndTokens = Tokenizer.tokenize(untilEndLine.getLineContent(),
				untilEndLine);
		OtherwiseInterpreter otherwise = null;

		if(isOtherwise(untilEndTokens)) {
			int otherwiseEnd = source.getBlockEnd(untilEnd + 1);
			otherwise = new OtherwiseInterpreter(this, source, untilEnd + 2, otherwiseEnd,
				loopChild);
			interpetationContineLine = otherwiseEnd;
		}

		UntilInterpreter untilInterpreter = new UntilInterpreter(this, source, index + 1, untilEnd,
				tokens, 1, tokens.size() - 2, otherwise);
		untilInterpreter.interpret();
		
		return interpetationContineLine;
	}
	
	protected int handleBreak(SourceCode.Line line, int index) {
		if(!loopChild) {
			throw new SyntaxException("Keyword " + Strings.BREAK +
				" can only be used in loops and switches." , line);
		}
		return parent.handleBreak(line, index);
	}
	
	protected int handleContinue(SourceCode.Line line, int index) {
		if(!loopChild) {
			throw new SyntaxException("Keyword " + Strings.CONTINUE + "can only be used in loops.",
				line);
		}
		return parent.handleContinue(line, index);
	}
	
	protected void handleInitialization(ArrayList<Token> tokens, SourceCode.Line line) {
		String type = tokens.get(0).getToken();
		String name = tokens.get(1).getToken();
		if(!VALID_NAME.matcher(name).matches()) {
			throw new SyntaxException("Illegal variable name.", line);
		}
		if(vat.hasVariable(name)) {
			System.out.println(vat.getVariable(name));
			throw new SyntaxException("Variable name '" + name + "' already in use.", line);
		}
		
		Variable value = Parser.parse(this, line, tokens, 3);
		if(!value.getTypeName().equals(type)) {
			throw new SyntaxException("Expected type " + type + " but recieved type " +
					value.getTypeName() + ".", line);
		}
		value.setLiteral(false);
		
		vat.allocate(name, value);
	}
	
}
