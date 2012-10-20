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
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

import com.sun.tools.javac.util.*;

/**
 * Interprets and executes a piece of {@link SourceCode}. This class is the root of all
 * intepreters for Phoenix. Subclasses should handle specific interpretation situations such as
 * conditional structures, loops, functions, etc. Each time this needs to enter a new block, 
 * a new Interpeter will be created and its {@link Interpreter#interpret()} method will get called.
 * 
 * @author Jonah Scheinerman
 */
public class Interpreter {

	/** Contains all keywords (includes all named types). */
	public static final HashSet<String> KEYWORDS = new HashSet<String>();	
	
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

		// Keywords that are not statments
		public static final String TRUE = "true";
		public static final String FALSE = "false";
		public static final String INTEGER = "int";
		public static final String LONG = "long";
		public static final String DOUBLE = "double";
		public static final String STRING = "str";
		public static final String BOOLEAN = "bool";
		public static final String TUPLE = "tuple";
		public static final String TYPE = "type";
		public static final String VOID = "void";
		public static final String AND = "and";
		public static final String OR = "or";
		public static final String NOT = "not";

		/** Contains all named types. */
		public static final HashSet<String> TYPES = new HashSet<String>();

		// Initialize keywords, types, and operators.
		static {
			KEYWORDS.add(INTEGER);
			KEYWORDS.add(DOUBLE);
			KEYWORDS.add(STRING);
			KEYWORDS.add(BOOLEAN);
			KEYWORDS.add(TYPE);
			KEYWORDS.add(LONG);
			KEYWORDS.add(TRUE);
			KEYWORDS.add(FALSE);
			KEYWORDS.add(VOID);
			KEYWORDS.add(AND);
			KEYWORDS.add(OR);
			KEYWORDS.add(NOT);

			TYPES.add(INTEGER);
			TYPES.add(DOUBLE);
			TYPES.add(STRING);
			TYPES.add(BOOLEAN);
			TYPES.add(LONG);
			TYPES.add(TYPE);
			TYPES.add(Statement.FUNCTION.getKeyword());
		}
	}

	/**
	 * Defines the different types of actions that can be taken by a {@link Statement}. There are
	 * three main types: A subinterpretation is the most common, and represents a statement that
	 * calls another interpreters interpret method (loops, conditionals, etc). The other two types
	 * are other and empty. Empty statements are just statements meant to wrap a keyword and have
	 * not inherent action. Other statements cover the other actions that can be performed
	 * (variable initialization, printing, function declaration, etc).
	 *
	 * @author Jonah Scheinerman
	 */
	public enum StatementType {
		/** Describes a statement that will call the interpret method on a subinterpreter. */
		SUBINTERPRETATION,
		/** Describes a statement that will do some other action besides a subinterpretation. */
		OTHER,
		/** Describes a statement that has no associated action. */
		EMPTY;
	}
	
	/**
	 * This enum defines different types of statements that lines in the source code can be. Before
	 * being processed, all lines are declared as {@link Statement#UNDEFINED}. Once they have been
	 * processed once, they will then have a new statement value, that can be used to more quickly
	 * execute the line. Thus, each line will only have to be interpreted once.
	 * 
	 * @author Jonah Scheinerman
	 */
	public static enum Statement {
		/** The empty statement does nothing. */
		EMPTY(null, StatementType.EMPTY),
		/** Try block subinterpretation. */
		TRY("try", StatementType.SUBINTERPRETATION),
		/** Catch block which is empty as this should never be found by itself. */
		CATCH("catch", StatementType.EMPTY),
		/** If/else if/else block execution. */
		IF("if", StatementType.OTHER),
		/** Else block which is empty as this should never be found by itself. */
		ELSE("else", StatementType.EMPTY),
		/** Do block which is empty as this should never be found by itself. */
		DO("do", StatementType.EMPTY),
		/** Do-while block subinterpretation. The keyword for this is <code>null</code> as there is
		 * no specific keyword that goes with this type of statement. */
		DO_WHILE(null, StatementType.SUBINTERPRETATION),
		/** Do-until block subinterpretation. The keyword for this is <code>null</code> as there is
		 * no specific keyword that goes with this type of statement. */
		DO_UNTIL(null, StatementType.SUBINTERPRETATION),
		/** While block subinterpretation. */
		WHILE("while", StatementType.SUBINTERPRETATION),
		/** Until block subinterpretation. */
		UNTIL("until", StatementType.SUBINTERPRETATION),
		/** For block subinterpretation. */
		FOR("for", StatementType.SUBINTERPRETATION),
		/** Otherwise block which is empty as this should never be found by itself. */
		OTHERWISE("otherwise", StatementType.EMPTY),
		/** Break statement. */
		BREAK("break", StatementType.OTHER),
		/** Continue statement. */
		CONTINUE("continue", StatementType.OTHER),
		/** Return statement. */
		RETURN("return", StatementType.OTHER),
		/** Function declaration. */
		FUNCTION("function", StatementType.OTHER),
		/** Variable initialization. */
		INITIALIZATION(null, StatementType.OTHER),
		/** Print statement. */
		PRINT("print", StatementType.OTHER),
		/** None of the above, line that should just be parsed as is. */
		PARSE(null, StatementType.OTHER),
		/** An undefined statement is one that has not yet been processed. */
		UNDEFINED(null, StatementType.EMPTY);

		/** The keyword associated with this statement (if there is one). */
		private String keyword;
		
		/** The type of statement. */
		private StatementType type;
		
		/**
		 * Creates a new Statement enum member. If the keyword is not null, it gets added to the
		 * {@link Interpreter#KEYWORDS} set.
		 * @param keyword the keyword for the statement
		 * @param type the type of statement
		 */
		private Statement(String keyword, StatementType type) {
			this.keyword = keyword;
			this.type = type;
			if(keyword != null)
				Interpreter.KEYWORDS.add(keyword);
		}
		
		/**
		 * Get this statement's keyword.
		 * @return the keyword for this statement
		 */
		public String getKeyword() {
			return keyword;
		}
		
		/**
		 * Get this statement's type.
		 * @return the type of the statement
		 */
		public StatementType getType() {
			return type;
		}

		public String toString() {
			return keyword;
		}
	}

	/**
	 * An enum giving the possible conditions under which an interpretation could finish.
	 * 
	 * @author Jonah Scheinerman
	 */
	public enum EndCondition {
		/** The interpretation ended normally. */
		NORMAL,
		/** A return was called in the interpretation, ending it. */
		RETURN,
		/** A break was called in the interpretation, ending it. */
		BREAK,
		/** A continue was called in the interpretation, ending it. */
		CONTINUE
	}

	/** 
	 * A pattern which matches only valid variable names. A valid variable name must start with a
	 * letter or an underscore and then most only consist of letters, underscores and numbers.
	 */
	private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z_][\\w]*");

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
	
	/** The condition under which this interpretation ended. */
	private EndCondition endCondition = EndCondition.NORMAL;
	
	/** The line on which this interpretation ended, if the ending was abnormal. */
	protected Line endConditionLine = null;
	
	/** 
	 * If a return statement with a value was called in this interpretation, the value given in that
	 * return statement.
	 */
	protected Variable returnVariable;

	/**
	 * Constructs a new interpreter that interprets the code at a given file path.
	 * @param path the path to the file to interpret
	 * @throws FileNotFoundException if the file path does not exist
	 */
	public Interpreter(String path) throws FileNotFoundException {
		this(new SourceCode(path));
	}

	/**
	 * Constructs a new interpreter that interprets the code in a given file.
	 * @param file the file to interpret
	 * @throws FileNotFoundException if the file does not exist
	 */
	public Interpreter(File file) throws FileNotFoundException {
		this(new SourceCode(file));
	}

	/**
	 * Constructs a new interpreter that interprets all of the given source code.
	 * @param source the source code to interpret
	 */
	public Interpreter(SourceCode source) {
		this(null, source, 0, source.size() - 1);
	}

	/**
	 * Constructs a new interpreter that is interprets a section of the given source code for the
	 * parent interpreter, before returning control to that parent.
	 * @param parent the parent of this interpreter that is delegating interpretation of a section
	 * of code to this interpreter
	 * @param source the source code from which the interpreted code will be taken
	 * @param start the index of the start line for interpretation
	 * @param end the index of the last line for interpretation (this line will be interpreted)
	 */
	public Interpreter(Interpreter parent, SourceCode source, int start, int end) {
		this.parent = parent;
		this.source = source;
		this.start = start;
		this.end = end;
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
	 * Set the VAT that will be used by this interpreter in its interpretation.
	 * @param vat the vat for this interpretation
	 */
	protected final void setVAT(VariableAllocationTable vat) {
		this.vat = vat;
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
	 * If the interpreter ends abnormally (the end condition is not {@link EndCondition#NORMAL}),
	 * then the line on which the interpreter ended. Otherwise, returns <code>null</code>.
	 * @return the line on which the interpreter ended its interpretation abnormally
	 */
	public final Line getEndConditionLine() {
		return endConditionLine;
	}
	
	/**
	 * If a return statement was called within this interpretation, gets the variable passed to
	 * that return statment.
	 * @return the return variable, if a return was called in this interpretation
	 */
	public final Variable getReturnVariable() {
		return returnVariable;
	}
	
	/**
	 * Returns the source code that is being interpreted by this interpreter.
	 * @return the source code being interpreted
	 */
	public final SourceCode getSourceCode() {
		return source;
	}

	/**
	 * Returns the parent interpreter to this interpreter.
	 * @return the parent of this interpreter.
	 */
	public final Interpreter getParent() {
		return parent;
	}
	
	/**
	 * Interprets and executes the code that this interpreter has been delegated. This returns the
	 * state under which this interpreter ended as an {@link EndCondition}.
	 * @return the condition under which this interpreter ended
	 */
	public EndCondition interpret() {
		returnVariable = null;
		vat.pushStackFrame();

		try {			
			for(int index = start; index <= end; index++) {
				Line line = source.line(index);
				if(line.isEmpty())
					continue;

				Statement statement = line.getStatement();

				// If the current line is indented more than the previous line, and we are not at
				// the start of the interpretation, then this is an indentation error.
				if(index != start && line.indentGreaterThan(source.line(index - 1).getIndent())) {
					throw new IndentException("Unexpected indented block", line);
				}	
	
				if(statement == Statement.UNDEFINED) {
					try {
						setupUndefinedStatement(line, index);
					} catch(PhoenixRuntimeException exception) {
						line.setSetupException(exception);
					}
				}
				
				index = handleDefinedStatement(line, index);
				
			}
			
			handleEndCondition(endCondition);

		} catch(PhoenixRuntimeException phoenixException) {
			if(topLevel) {
				phoenixException.printPhoenixTrace();
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
		
		return endCondition;
	}
	
	/**
	 * Sets up a line that is undefined for execution. This should only be called on lines that have
	 * not already been processed, that is their statement value is {@link Statement#UNDEFINED}.
	 * This does not execute the line, that must be done by the
	 * {@link Interpreter#handleDefinedStatement(Line, int)} method.
	 * @param line the line to be setup for execution
	 * @param index the index of the line
	 */
	private void setupUndefinedStatement(Line line, int index) {
		ArrayList<Token> tokenization = line.tokenize();
		String content = line.getLineContent();

		if(content.isEmpty()) {
			line.setStatment(Statement.EMPTY);
			
		} else if(isTry(tokenization)) {
			setupTry(tokenization, line, index);
			
		} else if(isIf(tokenization)) {
			setupIf(tokenization, line, index);

		} else if(isDo(tokenization)) {
			setupDo(tokenization, line, index);
		
		} else if(isFor(tokenization)) {
			setupFor(tokenization, line, index);
		
		} else if(isWhile(tokenization)) {
			setupWhile(tokenization, line, index);
		
		} else if(isUntil(tokenization)) {
			setupUntil(tokenization, line, index);
		
		} else if(content.equals(Statement.BREAK.getKeyword())) {
			setupBreak(line, index);

		} else if(content.equals(Statement.CONTINUE.getKeyword())) {
			setupContinue(line, index);
			
		} else if(isReturn(tokenization)) {
			setupReturn(tokenization, line, index);

		} else if(isFunctionDeclaration(tokenization)) {
			setupFunctionDeclaration(tokenization, line, index);
			
		} else if(isInitialization(tokenization)) {
			setupInitialization(tokenization, line);
			
		} else if(isPrint(tokenization)) {
			setupPrint(tokenization, line);
			
		} else if(isElse(tokenization)) {
			throw new SyntaxException("Cannot have " + Statement.ELSE +
				" block outside of " + Statement.IF + " clause.", line);
			
		} else if(isElseIf(tokenization)) {
			throw new SyntaxException("Cannot have " + Statement.ELSE + " " + Statement.IF + 
				" block outside of " + Statement.IF + " clause.", line);
		
		} else if(isOtherwise(tokenization)) {
			throw new SyntaxException("Cannot have " + Statement.OTHERWISE +
				" block outside of loop clause.", line);
			
		} else if(isCatch(tokenization)) {
			throw new SyntaxException("Cannot have " + Statement.CATCH +
				" block outside of a try clause.", line);
			
		} else {
			setupParse(tokenization, line);
		}
	}

	/**
	 * Executes a line of code that has already been processed. A processed line must have been
	 * passed to {@link Interpreter#setupUndefinedStatement(Line, int)}.
	 * @param line the line to execute
	 * @param index the index of the line
	 * @return the index of the line the execution should continue on
	 */
	private int handleDefinedStatement(Line line, int index) {
		if(line.getSetupException() != null) {
			throw line.getSetupException();
		}
		
		Statement statement = line.getStatement();
		if(statement.getType() == StatementType.SUBINTERPRETATION) {
			return runChildInterpretation((Interpreter)line.getData(),
				line.getContinuationLineIndex());

		} else if(statement == Statement.IF){
			return handleIf(line);
		
		} else if(statement == Statement.BREAK) {
			return handleBreak(line);
		
		} else if(statement == Statement.CONTINUE) {
			return handleContinue(line);
		
		} else if(statement == Statement.RETURN) {
			return handleReturn(line);
		
		} else if(statement == Statement.FUNCTION) {
			return handleFunctionDeclaration(line);
		
		} else if(statement == Statement.INITIALIZATION) {
			handleInitialization(line);
		
		} else if(statement == Statement.PRINT) {
			handlePrint(line);
		
		} else if(statement == Statement.PARSE) {
			handleParse(line);
		
		}
		return index;
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a try statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a try statement
	 */
	private boolean isTry(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Statement.TRY.getKeyword()) &&
			   tokens.get(1).getToken().equals(":");
	}

	/**
	 * Returns whether the line represented by the tokenization is a catch statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a catch statement
	 */
	private boolean isCatch(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Statement.CATCH.getKeyword()) &&
			   tokens.get(1).getToken().equals(":");
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an if statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an if statement
	 */
	private boolean isIf(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Statement.IF.getKeyword());
	}

	/**
	 * Returns whether the line represented by the tokenization is an else if statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an else if statement
	 */
	private boolean isElseIf(ArrayList<Token> tokens) {
		return tokens.size() >= 2 &&
			   tokens.get(0).getToken().equals(Statement.ELSE.getKeyword()) &&
			   tokens.get(1).getToken().equals(Statement.IF.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an else statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an else statement
	 */
	private boolean isElse(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Statement.ELSE.getKeyword()) &&
			   tokens.get(1).getToken().equals(":");
	}

	/**
	 * Returns whether the line represented by the tokenization is a do statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a do statement
	 */
	protected static final boolean isDo(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Statement.DO.getKeyword()) &&
			   tokens.get(1).getToken().equals(":");
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an otherwise statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an otherwise statement
	 */
	protected static final boolean isOtherwise(ArrayList<Token> tokens) {
		return tokens.size() == 2 &&
			   tokens.get(0).getToken().equals(Statement.OTHERWISE.getKeyword()) &&
			   tokens.get(1).getToken().equals(":");
	}

	/**
	 * Returns whether the line represented by the tokenization is a for statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a for statement
	 */
	protected static final boolean isFor(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Statement.FOR.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an until statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an until statement
	 */
	protected static final boolean isUntil(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Statement.UNTIL.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a while statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a while statement
	 */
	protected static final boolean isWhile(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Statement.WHILE.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is an initialization statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents an initialization statement
	 */
	protected static final boolean isInitialization(ArrayList<Token> tokens) {
		ArrayList<Token> typeTokens = getTypeName(tokens, 0);
		if(typeTokens == null)
			return false;
		
		return tokens.size() > typeTokens.size() + 2 &&
			   tokens.get(typeTokens.size() + 1).getToken().equals("=");
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a function declaration
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a function declaration
	 */
	protected static final boolean isFunctionDeclaration(ArrayList<Token> tokens) {
		return tokens.size() > 1 &&
			   tokens.get(0).getToken().equals(Statement.FUNCTION.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a return statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a rutnr statement
	 */
	protected static final boolean isReturn(ArrayList<Token> tokens) {
		return tokens.size() > 1 &&
			   tokens.get(0).getToken().equals(Statement.RETURN.getKeyword());
	}
	
	/**
	 * Returns whether the line represented by the tokenization is a print statement.
	 * @param tokens the tokenization of the line to check
	 * @return whether the line represents a print statement
	 */
	protected static final boolean isPrint(ArrayList<Token> tokens) {
		return tokens.size() >= 1 &&
			   tokens.get(0).getToken().equals(Statement.PRINT.getKeyword());
	}
	
	/**
	 * Sets up a line that contains a try statement for execution.
	 * @param tokens the tokenization of the try statement line
	 * @param line the line containing the try statement
	 * @param index the index of the line
	 */
	public void setupTry(ArrayList<Token> tokens, Line line, int index) {
		int tryEnd = source.getBlockEnd(index);
		Line tryEndLine = source.line(tryEnd + 1);
		ArrayList<Token> tryEndTokens = Tokenizer.tokenize(tryEndLine.getLineContent(),
				tryEndLine);
		
		if(!isCatch(tryEndTokens)) {
			throw new SyntaxException("Try block must be followed by catch block", tryEndLine);
		}
		
		int catchEnd = source.getBlockEnd(tryEnd + 1);
		
		
		CatchInterpreter catchInterpreter = new CatchInterpreter(this, source, tryEnd + 2, catchEnd);
		TryInterpreter tryInterpreter = new TryInterpreter(this, source, index + 1, tryEnd,
			catchInterpreter);
		
		line.setStatment(Statement.TRY);
		line.setContinuationLineIndex(catchEnd);
		line.setData(tryInterpreter);
	}
	
	/**
	 * Sets up a line that contains a if statement for execution.
	 * @param tokens the tokenization of the if statement line
	 * @param line the line containing the if statement
	 * @param index the index of the line
	 */
	private void setupIf(ArrayList<Token> tokens, Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Statement.IF.getKeyword() + " statements must end in a colon.", line);
		}
		IfExecutor ifExec = new IfExecutor();
		
		boolean firstLoop = true;;
		boolean done = false;
		int currentLine = index;
		
		while(!done) {
			Line current = source.line(currentLine);
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
						throw new SyntaxException(Statement.ELSE.getKeyword() + " " + Statement.IF.getKeyword() + 
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
					source, currentLine + 1, endCurrentBlock, lineTokenization,
					startToken, lineTokenization.size() - 2);
				ifExec.addCondition(conditionInterpreter);

			// We're done if we've hit an else, so deal with that.
			} else {
				ElseInterpreter elseInterpreter = new ElseInterpreter(this, source, currentLine + 1,
					endCurrentBlock);
				ifExec.setElseIntepreter(elseInterpreter);
			}				
			currentLine = endCurrentBlock + 1;
		}
		
		line.setStatment(Statement.IF);
		line.setData(ifExec);
		line.setContinuationLineIndex(currentLine - 1);
	}
	
	/**
	 * Executes a line that has been preprocessed and contains an if statement.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected int handleIf(Line line) {
		IfExecutor ifExec = (IfExecutor)line.getData();
		
		EndCondition childEndCondition = ifExec.execute();
		if(childEndCondition == EndCondition.NORMAL) {
			return line.getContinuationLineIndex();
		}
		
		this.endCondition = childEndCondition;
		this.endConditionLine = ifExec.getEndConditionLine();
		if(endCondition == EndCondition.RETURN) {
			this.returnVariable = ifExec.getReturnVariable();
		}

		return getEndLine();
	}

	/**
	 * Sets up a line that contains a do statement for execution.
	 * @param tokens the tokenization of the do statement line
	 * @param line the line containing the do statement
	 * @param index the index of the line
	 */
	private void setupDo(ArrayList<Token> tokens, Line line, int index) {
		int doEnd = source.getBlockEnd(index);
		Line endLine = source.line(doEnd + 1);
		String endLineContent = endLine.getLineContent();
		ArrayList<Token> endTokens = Tokenizer.tokenize(endLineContent, endLine);
		// If the do loop ends with a while:
		if(isWhile(endTokens)) {
			setupDoWhile(endTokens, line, endLine, doEnd + 1);

		// If the do loop ends with a unti:
		} else if(isUntil(endTokens)) {
			setupDoUntil(endTokens, line, endLine, doEnd + 1);

		// If none of the above, the end of the loop is invalid.
		} else {
			throw new SyntaxException("Invalid end of " + Statement.DO.getKeyword() + " loop.",
				endLine);
		}
	}

	/**
	 * Finishes set up of a do-while loop for the line containing the do statement.
	 * @param whileTokens the tokens on the line containing the while statement
	 * @param doLine the line containing the do statement
	 * @param whileLine the line containing the while statement
	 * @param resumeLine the line that should be executed after the loop ends
	 */
	private void setupDoWhile(ArrayList<Token> whileTokens, Line doLine,
			Line whileLine, int resumeLine) {		
		DoWhileInterpreter doWhileInterpreter = new DoWhileInterpreter(this, source,
				doLine.getNumber() + 1, whileLine.getNumber() - 1, whileLine, whileTokens, 1,
				whileTokens.size() - 1);

		doLine.setStatment(Statement.DO_WHILE);
		doLine.setData(doWhileInterpreter);
		doLine.setContinuationLineIndex(resumeLine);
	}

	/**
	 * Finishes set up of a do-until loop for the line containing the do statement.
	 * @param untilTokens the tokens on the line containing the until statement
	 * @param doLine the line containing the do statement
	 * @param untilLine the line containing the while statement
	 * @param resumeLine the line that should be executed after the loop ends
	 */
	private void setupDoUntil(ArrayList<Token> untilTokens, Line doLine,
			Line untilLine, int resumeLine) {
		DoUntilInterpreter doUntilInterpreter = new DoUntilInterpreter(this, source,
				doLine.getNumber() + 1, untilLine.getNumber() - 1, untilLine, untilTokens, 1,
				untilTokens.size() - 1);

		doLine.setStatment(Statement.DO_UNTIL);
		doLine.setData(doUntilInterpreter);
		doLine.setContinuationLineIndex(resumeLine);
	}
	
	/**
	 * Sets up a line that contains a for statement for execution.
	 * @param tokens the tokenization of the line containing the for statement
	 * @param line the source line containing the for statement
	 * @param index the index of the line
	 */
	private void setupFor(ArrayList<Token> tokens, Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Statement.FOR + " statements must end in a colon.", line);
		}
		
		int semicolonCount = 0;
		int firstSeparator = 0, secondSeparator = 0;
		for(int tokenIndex = 1; tokenIndex <= tokens.size() - 2; tokenIndex++) {
			if(tokens.get(tokenIndex).getToken().equals(";")) {
				semicolonCount++;
				if(semicolonCount == 1)
					firstSeparator = tokenIndex;
				if(semicolonCount == 2)
					secondSeparator = tokenIndex;
				if(semicolonCount == 3)
					throw new SyntaxException(Statement.FOR +
						" predicates must have three components.", line);
			}
		}
		
		int forEnd = source.getBlockEnd(index);
		int interpetationContineLine = forEnd;
		Line forEndLine = source.line(forEnd + 1);
		ArrayList<Token> forEndTokens = Tokenizer.tokenize(forEndLine.getLineContent(),
				forEndLine);
		OtherwiseInterpreter otherwise = null;
		
		if(isOtherwise(forEndTokens)) {
			int otherwiseEnd = source.getBlockEnd(forEnd + 1);
			otherwise = new OtherwiseInterpreter(this, source, forEnd + 2, otherwiseEnd);
			interpetationContineLine = otherwiseEnd;
		}
		
		ForInterpreter forInterpreter = new ForInterpreter(this, getSourceCode(), index + 1, forEnd,
			line, tokens, firstSeparator, secondSeparator, otherwise);

		line.setStatment(Statement.FOR);
		line.setData(forInterpreter);
		line.setContinuationLineIndex(interpetationContineLine);
	}

	/**
	 * Sets up a line that contains a while statement for execution.
	 * @param tokens the tokenization of the line containing the while statement
	 * @param line the source line containing the while statement
	 * @param index the index of the line
	 */
	private void setupWhile(ArrayList<Token> tokens, Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Statement.WHILE + " statements must end in a colon.", line);
		}

		int whileEnd = source.getBlockEnd(index);
		int interpetationContineLine = whileEnd;
		Line whileEndLine = source.line(whileEnd + 1);
		ArrayList<Token> whileEndTokens = Tokenizer.tokenize(whileEndLine.getLineContent(),
			whileEndLine);
		OtherwiseInterpreter otherwise = null;

		if(isOtherwise(whileEndTokens)) {
			int otherwiseEnd = source.getBlockEnd(whileEnd + 1);
			otherwise = new OtherwiseInterpreter(this, source, whileEnd + 2, otherwiseEnd);
			interpetationContineLine = otherwiseEnd;
		}

		WhileInterpreter whileInterpreter = new WhileInterpreter(this, source, index + 1, whileEnd,
			tokens, 1, tokens.size() - 2, otherwise);

		line.setStatment(Statement.WHILE);
		line.setData(whileInterpreter);
		line.setContinuationLineIndex(interpetationContineLine);
	}

	/**
	 * Sets up a line that contains an until statement for execution.
	 * @param tokens the tokenization of the line containing the until statement
	 * @param line the source line containing the until statement
	 * @param index the index of the line
	 */
	private void setupUntil(ArrayList<Token> tokens, Line line, int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException(Statement.UNTIL + " statements must end in a colon.", line);
		}
	
		int untilEnd = source.getBlockEnd(index);
		int interpetationContineLine = untilEnd;
		Line untilEndLine = source.line(untilEnd + 1);
		ArrayList<Token> untilEndTokens = Tokenizer.tokenize(untilEndLine.getLineContent(),
				untilEndLine);
		OtherwiseInterpreter otherwise = null;

		if(isOtherwise(untilEndTokens)) {
			int otherwiseEnd = source.getBlockEnd(untilEnd + 1);
			otherwise = new OtherwiseInterpreter(this, source, untilEnd + 2, otherwiseEnd);
			interpetationContineLine = otherwiseEnd;
		}

		UntilInterpreter untilInterpreter = new UntilInterpreter(this, source, index + 1, untilEnd,
				tokens, 1, tokens.size() - 2, otherwise);

		line.setStatment(Statement.UNTIL);
		line.setData(untilInterpreter);
		line.setContinuationLineIndex(interpetationContineLine);
	}
	
	/**
	 * Sets up a line that contains a break statement for execution.
	 * @param line the source line containing the break statement
	 * @param index the index of the line
	 */
	private void setupBreak(Line line, int index) {
		line.setStatment(Statement.BREAK);
		line.setContinuationLineIndex(getEndLine());
	}

	/**
	 * Executes a line that has been preprocessed and contains a break statement.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected int handleBreak(Line line) {
		endCondition = EndCondition.BREAK;
		endConditionLine = line;
		
		return getEndLine();
	}
	
	/**
	 * Sets up a line that contains a continue statement for execution.
	 * @param line the source line containing the continue statement
	 * @param index the index of the line
	 */
	private void setupContinue(Line line, int index) {
		line.setStatment(Statement.CONTINUE);
		line.setContinuationLineIndex(getEndLine());
	}

	/**
	 * Executes a line that has been preprocessed and contains a continue statement.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected int handleContinue(Line line) {
		endCondition = EndCondition.CONTINUE;
		endConditionLine = line;
		
		return getEndLine();
	}
	
	/**
	 * Sets up a line that contains a return statement for execution.
	 * @param tokens the tokenization of the line containing the return statement
	 * @param line the source line containing the break statement
	 * @param index the index of the line
	 */
	private void setupReturn(ArrayList<Token> tokens, Line line, int index) {
		line.setStatment(Statement.RETURN);
		line.setContinuationLineIndex(getEndLine());
		
		if(tokens.size() == 1){
			line.setData(null);
		} else {
			line.setData(Parser.getParseTree(this, line, tokens, 1));
		}
	}
	
	/**
	 * Executes a line that has been preprocessed and contains a return statement.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected int handleReturn(Line line) {
		endCondition = EndCondition.RETURN;
		endConditionLine = line;		
		if(line.getData() != null) {
			returnVariable = ((ParseTreeNode)line.getData()).operate().getValue();
		} else {
			returnVariable = null;
		}

		return getEndLine();
	}
	
	/**
	 * Sets up a line that contains a function declaration for execution.
	 * @param tokens the tokenization of the line containing the function declaration
	 * @param line the source line containing the function declaration
	 * @param index the index of the line
	 */
	protected void setupFunctionDeclaration(ArrayList<Token> tokens, Line line,
			int index) {
		if(!tokens.get(tokens.size() - 1).getToken().equals(":")) {
			throw new SyntaxException("Function declarations must end in a colon.", line);
		}
		
		String returnType;
		String name;
		ArrayList<Pair<String, String>> leftArgs = null;
		ArrayList<Pair<String, String>> rightArgs = null;
		
		int currToken = 1;
		if(tokens.get(currToken).getToken().equals(Strings.VOID)) {
			returnType = Strings.VOID;
			currToken++;
		} else {
			ArrayList<Token> typeTokens = getTypeName(tokens, currToken);
			returnType = concatenate(typeTokens);
			currToken += typeTokens.size();
		}
		
		if(tokens.size() - 1 <= currToken) {
			throw new SyntaxException("Incomplete function declaration.", line);
		}
		
		if(tokens.get(currToken).getToken().equals("(")) {
			boolean closeFound = false;
			int start = currToken + 1;
			for(currToken = start; currToken < tokens.size(); currToken++) {
				if(tokens.get(currToken).getToken().equals(")")) {
					closeFound = true;
					break;
				}
			}
			if(!closeFound) {
				throw new SyntaxException("Incomplete function declaration.", line);
			}
			leftArgs = getArgumentList(tokens, start, currToken, line);
		} else {
			currToken--;
		}
		
		currToken++;
		if(tokens.size() - 1 <= currToken) {
			throw new SyntaxException("Incomplete function declaration.", line);
		}
		
		name = tokens.get(currToken).getToken();
		isNameValid(name, line);
		currToken++;

		if(tokens.get(currToken).getToken().equals("(")) {
			boolean closeFound = false;
			int start = currToken + 1;
			for(currToken = start; currToken < tokens.size(); currToken++) {
				if(tokens.get(currToken).getToken().equals(")")) {
					closeFound = true;
					break;
				}
			}
			if(!closeFound) {
				throw new SyntaxException("Incomplete function declaration.", line);
			}
			
			rightArgs = getArgumentList(tokens, start, currToken, line);
		}
		
		
		int functionEnd = source.getBlockEnd(index);

		FunctionInterpreter functionInterpreter = new FunctionInterpreter(this, getSourceCode(),
			index + 1, functionEnd, returnType, name, leftArgs, rightArgs);
		FunctionVariable functionVariable = new FunctionVariable(functionInterpreter);
		
		line.setStatment(Statement.FUNCTION);
		line.setContinuationLineIndex(functionEnd);
		line.setData(new Pair<String, Variable>(name, functionVariable));
	}
	
	/**
	 * Executes a line that has been preprocessed and contains a function declaration.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected int handleFunctionDeclaration(Line line) {
		@SuppressWarnings("unchecked")
		Pair<String, Variable> pair = (Pair<String, Variable>)line.getData();
		if(topLevel) {
			vat.allocateGlobal(pair.fst, pair.snd);
		} else {
			vat.allocate(pair.fst, pair.snd);
		}

		return line.getContinuationLineIndex();
	}
	
	/**
	 * Parses a list of tokens that represent a function argument list an returns that argument
	 * list as a ordered list of type-name pairings.
	 * @param tokens the tokenization of the argument list
	 * @param start the starting token of the arguments
	 * @param end the ending token of the arguments (non-inclusive)
	 * @param line the line containing the argument list
	 * @return an ordered list of type-name pairings based on the argument list
	 */
	private ArrayList<Pair<String, String>> getArgumentList(ArrayList<Token> tokens, int start,
			int end, Line line) {
		ArrayList<Pair<String, String>> arguments = new ArrayList<Pair<String, String>>();
		for(int index = start; index < end; index++) {
			ArrayList<Token> typeTokens = getTypeName(tokens, index);
			if(typeTokens == null) {
				throw new SyntaxException("Unknown parameter type " + tokens.get(index).getToken(),
					line);
			}
			String type = concatenate(typeTokens);
			index += typeTokens.size();
			String name = tokens.get(index).getToken();
			arguments.add(new Pair<String, String>(type, name));

			if(index == end - 1) {
				break;
			}
			index++;
			if(!tokens.get(index).getToken().equals(",")) {
				throw new SyntaxException("Function parameters must be comma separated.", line);
			}			
		}
		return arguments;
	}
	
	/**
	 * Sets up a line that contains a variable initialization for execution.
	 * @param tokens the tokenization of the line containing the variable initialization
	 * @param line the source line containing the variable initialization
	 * @param index the index of the line
	 */
	private void setupInitialization(ArrayList<Token> tokens, Line line) {
		ArrayList<Token> typeTokens = getTypeName(tokens, 0);
		String type = concatenate(typeTokens);
		String name = tokens.get(typeTokens.size()).getToken();
		
		isNameValid(name, line);
		
		ParseTreeNode tree = Parser.getParseTree(this, line, tokens, typeTokens.size() + 2);
		line.setStatment(Statement.INITIALIZATION);
		line.setData(new Object[] {type, name, tree});
	}
	
	/**
	 * Executes a line that has been preprocessed and contains a variable initialization.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected void handleInitialization(Line line) {
		Object[] storedValues = (Object[])line.getData();
		String type = (String)storedValues[0];
		String name = (String)storedValues[1];		
		ParseTreeNode tree = (ParseTreeNode)storedValues[2];
		Variable value = tree.operate().getValue();
		doInitialization(type, name, value, line);
	}
	
	/**
	 * Creates a new variable with the given type, name, and value in the local scope.
	 * @param type the type name of the variable
	 * @param name the name of the variable
	 * @param value the value of the variable
	 * @param line the line containing the initialization
	 */
	protected void doInitialization(String type, String name, Variable value, Line line) {
		Variable store = null;
		if(type.equals(Strings.BOOLEAN)) {
			store = new BooleanVariable();
		} else if(type.equals(Strings.INTEGER)) {
			store = new IntegerVariable();
		} else if(type.equals(Strings.DOUBLE)) {
			store = new DoubleVariable();
		} else if(type.equals(Strings.LONG)) {
			store = new LongVariable();
		} else if(type.equals(Strings.STRING)) {
			store = new StringVariable();
		} else if(type.equals(Strings.TYPE)) {
			store = new TypeVariable();
		} else if(type.startsWith("[")) {
			store = new ArrayVariable(type);
		}
		
		try {
			store.assign(value);
		} catch(UnsupportedOperatorException e) {
			e.setMessage("Variable of type " + type + " cannot be assigned a value with type " +
				value.getTypeName());
			e.setSourceLine(line);
			throw e;
		}
		
		store.setLiteral(false);
		
		if(topLevel) {
			vat.allocateGlobal(name, store);
		} else {
			vat.allocate(name, store);
		}
	}
	
	/**
	 * Sets up a line that contains an print statement for execution.
	 * @param tokens the tokenization of the line containing the print statement
	 * @param line the source line containing the print statement
	 */
	private void setupPrint(ArrayList<Token> tokens, Line line) {
		line.setStatment(Statement.PRINT);
		if(tokens.size() == 1) {
			line.setData(null);
		} else {
			line.setData(Parser.getParseTree(this, line, tokens, 1));
		}
	}
	
	/**
	 * Executes a line that has been preprocessed and contains a print statement.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected void handlePrint(Line line) {
		if(line.getData() == null) {
			System.out.println();
		} else {
			System.out.println(((ParseTreeNode)line.getData()).operate().getValue());
		}
	}
	
	/**
	 * Sets up a line that contains an phrase to parse for execution.
	 * @param tokens the tokenization of the line containing the phrase
	 * @param line the source line containing the phrase
	 */
	private void setupParse(ArrayList<Token> tokens, Line line) {
		line.setStatment(Statement.PARSE);
		line.setData(Parser.getParseTree(this, line, tokens));
	}
	
	/**
	 * Executes a line that has been preprocessed and contains a phrase to be parsed.
	 * @param line the line to execute
	 * @return the line that should be next executed after this line
	 */
	protected void handleParse(Line line) {
		((ParseTreeNode)line.getData()).operate().getValue();
	}
	
	/**
	 * Handles the end condition of this intepreter. This interpreter is implemented to throw errors
	 * if it is a top level intepreter and the end condition is not normal.
	 * @param endCondition the end condition of this interpreter
	 */
	protected void handleEndCondition(EndCondition endCondition) {
		if(endCondition == EndCondition.NORMAL)
			return;
		
		if(topLevel) {
			if(endCondition == EndCondition.BREAK) {
				throw new SyntaxException("Cannot have " + Statement.BREAK +
					" statement outside of a loop.", endConditionLine);
			} else if(endCondition == EndCondition.CONTINUE) {
				throw new SyntaxException("Cannot have " + Statement.CONTINUE + 
					" statement outside of a loop.", endConditionLine);
			} else if(endCondition == EndCondition.RETURN) {
				throw new SyntaxException("Cannot have " + Statement.RETURN +
					" statement outside of a function.", endConditionLine);
			}
		}
	}	
	
	/**
	 * Executes the interpretation of a child interpreter. This involves setting up the VAT for the
	 * child intepreter, running the interpreter, then handling the end condition of the child
	 * interpretation.
	 * @param child the child interpreter to execute
	 * @param resumeLine the line on which execution should continue under a normal end condition
	 * @return the line on which execution should continue
	 */
	private int runChildInterpretation(Interpreter child, int resumeLine) {
		child.setVAT(getVAT());

		EndCondition childEndCondition = child.interpret();
		if(childEndCondition == EndCondition.NORMAL) {
			return resumeLine;
		}
		
		this.endCondition = childEndCondition;
		this.endConditionLine = child.getEndConditionLine();
		if(endCondition == EndCondition.RETURN) {
			this.returnVariable = child.getReturnVariable();
		}

		return getEndLine();
	}
	
	/**
	 * Checks if the given name is valid. A valid name cannot be an already used name, cannot be a
	 * keyword, and must start with a letter or underscore and then consist of only letters, digits,
	 * and underscores. If it is valid, then nothing happens. If the name is not valid, then an
	 * appropriate {@link SyntaxException} will be thrown. Functions are variables and their names
	 * must be checked using this method.
	 * @param name the name of the variable to check if valid
	 * @param line the line containing the initialization of the variable
	 */
	protected void isNameValid(String name, Line line) {
		if(!VALID_NAME.matcher(name).matches()) {
			throw new SyntaxException("Illegal variable name '" + name + "'.", line);
		}
		if(Interpreter.KEYWORDS.contains(name)) {
			throw new SyntaxException("Variable name cannot be keyword '" + name + "'.", line);
		}
		if(vat.hasVariable(name)) {
			throw new SyntaxException("Variable name '" + name + "' already in use.", line);
		}
	}
	
	/**
	 * If the list of tokens contains a type name starting at the start token, this will return
	 * that type name. This is particularly useful, becuase it will pull out the type name even
	 * if it is an array type. If there is no type name at the given starting index, a null value
	 * is returned.
	 * @param tokens the tokenization of the line containing the possible type name
	 * @param start the token at which the type name is thought to occur
	 * @return the tokens of the type name, or null if there is no type name at the start index
	 */
	protected static ArrayList<Token> getTypeName(ArrayList<Token> tokens, int start) {
		if(tokens.size() <= start)
			return null;
		ArrayList<Token> typeTokens = new ArrayList<Token>();
		if(Strings.TYPES.contains(tokens.get(start).getToken())) {
			typeTokens.add(tokens.get(start));
			return typeTokens;
		}
		if(!tokens.get(start).getToken().equals("[")) {
			return null;
		}
		int openBrackets = 0;
		for(int index = start; index < tokens.size(); index++) {
			if(tokens.get(index).getToken().equals("[")) {
				openBrackets++;
				typeTokens.add(tokens.get(index));
			} else {
				break;
			}
		}
		if(tokens.size() <= openBrackets + start) {
			return null;
		}
		if(Strings.TYPES.contains(tokens.get(openBrackets + start).getToken())) {
			typeTokens.add(tokens.get(openBrackets + start));
		} else {
			return null;
		}
		
		for(int index = openBrackets + start + 1; index < tokens.size(); index++) {
			if(tokens.get(index).getToken().equals("]")) {
				openBrackets--;
				typeTokens.add(tokens.get(index));
			} else {
				break;
			}
			
			if(openBrackets == 0) {
				return typeTokens;
			}
		}	
		
		if(openBrackets != 0) {
			return null;
		}
		
		return typeTokens;
	}
	
	/**
	 * Concatenates a set of tokens together into a single string with no delimiter.
	 * @param tokens the tokens to concatenate together
	 * @return the concatenated string of tokens
	 */
	protected static String concatenate(ArrayList<Token> tokens) {
		String ret = "";
		for(Token token : tokens) {
			ret += token.getToken();
		}
		return ret;
	}
}
