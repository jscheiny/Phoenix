// LoopInterpreter.java
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

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

/**
 * Interprets and executes a loop block. This class is the root class of all loop interpretations.
 * Loops contain a predicate (a code phrase that returns true or false) that can be checked either
 * at the beginning or end of the loop. The loop will end either when the predicate becomes true or
 * false. Loops can also have an otherwise statement which will execute only if the loop fails to
 * execute the first time.
 *
 * @author Jonah Scheinerman
 */
public class LoopInterpreter extends Interpreter {

	/** The code line containing the loop predicate. */
	private SourceCode.Line predicateLine;

	/** The tokenization of the predicate. */
	private ArrayList<Token> predicateTokens;

	/** The starting token index of the predicate. */
	private int predicateStartToken;

	/** The ending toke index of the predicate (inclusive). */
	private int predicateEndToken;

	/** The parse tree built from the predicate. This will be built only once. */
	private ParseTreeNode predicateParseTree;

	/**
	 * Whether the predicate is checked at the beginning of the loop. If this is false, then the
	 * loop will always execute at least once, and thus the otherwise statement (if there is one)
	 * will never execute.
	 */
	private boolean predicateCheckedAtBeginning;

	/** The value that the predicate should become to end the loop. */
	private boolean predicateLoopEndValue;

	/** 
	 * The otherwise interpreter that should execute if the loop fails to execute. If there is no
	 * otherwise block, this should be null.
	 */
	private OtherwiseInterpreter otherwise;
	
	/** Whether the loop has finished executing. */
	private boolean loopDone = false;

	/**
	 * Constructs a new loop interpreter with no otherwise block.
	 * @param parent the interpreter that is instantiating and running this interpreter
	 * @param start the line on which to start interpreting
	 * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param predicateLine the line containing the loop predicate
	 * @param predicateTokens the tokenization of the predicate
	 * @param predicateStartToken the starting index of the predicate in the tokenization
	 * @param predicateEndToken the ending index of the predicate in the tokenization (inclusive)
	 * @param predicateCheckedAtBeginning whether the loop predicate is checked at the beginning
	 * @param predicateLoopEndValue the value that the predicate should become to end the loop
	 */
	public LoopInterpreter(Interpreter parent, int start, int end, SourceCode.Line predicateLine,
			ArrayList<Token> predicateTokens, int predicateStartToken, int predicateEndToken,
			boolean predicateCheckedAtBeginning, boolean predicateLoopEndValue) {
		this(parent, start, end, predicateLine, predicateTokens, predicateStartToken,
			 predicateEndToken, predicateCheckedAtBeginning, predicateLoopEndValue, null);
	}

	/**
	 * Constructs a new loop interpreter with no otherwise block.
	 * @param parent the interpreter that is instantiating and running this interpreter
	 * @param start the line on which to start interpreting
	 * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param predicateLine the line containing the loop predicate
	 * @param predicateTokens the tokenization of the predicate
	 * @param predicateStartToken the starting index of the predicate in the tokenization
	 * @param predicateEndToken the ending index of the predicate in the tokenization (inclusive)
	 * @param predicateCheckedAtBeginning whether the loop predicate is checked at the beginning
	 * @param predicateLoopEndValue the value that the predicate should become to end the loop
	 * @param otherwise the interpreter for executing the otherwise block (null if there is none)
	 */
	public LoopInterpreter(Interpreter parent, int start, int end, SourceCode.Line predicateLine,
			ArrayList<Token> predicateTokens, int predicateStartToken, int predicateEndToken,
			boolean predicateCheckedAtBeginning, boolean predicateLoopEndValue,
			OtherwiseInterpreter otherwise) {
		super(parent, start, end);
		
		this.predicateLine = predicateLine;
		this.predicateTokens = predicateTokens;
		this.predicateStartToken = predicateStartToken;
		this.predicateEndToken = predicateEndToken;
		this.predicateCheckedAtBeginning = predicateCheckedAtBeginning;
		this.predicateLoopEndValue = predicateLoopEndValue;
		this.otherwise = otherwise;
	}
	
	/**
	 * Executes the loop block until the predicate becomes the predicate end value. If the loop
	 * does not execute, then this runs the interpretation of the otherwise block (if there is one).
	 * This will return conditions under which the loop ended. However, if a continue or break is
	 * executed within the loop, then the interpretation does not end, and those conditions are
	 * handled accordingly. Thus the only possible end conditions are {@link EndCondition#NORMAL} or
	 * {@link EndCondition#RETURN}.
	 * @return the condition under which the loop ended (cannot be a break or continue)
	 */
	public EndCondition interpret() {
		loopDone = false;
		
		if(isPredicateCheckedAtBeginning() && isLoopDone()) {
			if(otherwise != null) {
				EndCondition condition = otherwise.interpret();
				endConditionLine = otherwise.getEndConditionLine();
				returnVariable = otherwise.getReturnVariable();
				return condition;
			}
			return EndCondition.NORMAL;
		}
		do {
			EndCondition endCondition = super.interpret();
			if(endCondition == EndCondition.BREAK) {
				return EndCondition.NORMAL;
			}
			if(endCondition == EndCondition.RETURN) {
				return endCondition;
			}
			performAtLoopEnd();
		} while(!isLoopDone());
		return EndCondition.NORMAL;
	}
	
	/**
	 * Returns the line on which the predicate occured.
	 * @return the line on which the predicate occured
	 */
	public final SourceCode.Line getPredicateLine() {
		return predicateLine;
	}
	
	/**
	 * Returns whether the predicate is checked at the beginning of the loop.
	 * @return whether the predicate is checked at the beginning of the loop
	 */
	public final boolean isPredicateCheckedAtBeginning() {
		return predicateCheckedAtBeginning;
	}
	
	/**
	 * Executes the predicate parse tree and gets the result. If the predicate parses to a
	 * non-boolean variable, then this throws a {@link SyntaxException}.
	 * @return the boolean result of evaluating the predicate
	 */
	public final boolean checkPredicate() {
		if(predicateParseTree == null) {
			predicateParseTree = Parser.getParseTree(this, predicateLine, predicateTokens,
				predicateStartToken, predicateEndToken);
		}
		Variable predicateEvaluation = predicateParseTree.operate().getValue();
		if(predicateEvaluation instanceof BooleanVariable) {
			return ((BooleanVariable)predicateEvaluation).getValue();
		}
		throw new SyntaxException("Loop predicate must evaluate to type bool.", predicateLine);
	}
	
	/**
	 * Provides a method that will be executed at the end of each loop iteration. This can be
	 * overridden by subclasses to perform action at the end of every loop iteration (such as
	 * incrementing a loop variable).
	 */
	protected void performAtLoopEnd() {}
	
	/**
	 * Checks whether the predicate evaluates to a loop end value, updates the loop end variable,
	 * and returns whether the loop has ended.
	 * @return whether the loop has ended
	 */
	public final boolean isLoopDone() {
		if(checkPredicate() == predicateLoopEndValue) {
			loopDone = true;
		}
		return loopDone;
	}
	
	/**
	 * Handles a break by setting the loop to done, and moving interpretation to the end of the
	 * loop.
	 * @param line the line on which the break was found
	 * @return the last line of execution, so that execution of the loop will stop
	 */
	@Override
	protected int handleBreak(Line line) {
		loopDone = true;
		return getEndLine();
	}
	
	/**
	 * Handles a continue by simply moving interpretation to the end of the loop.
	 * @return the last line of execution, so that execution of the loop will continue onto the next
	 * iteration
	 */
	@Override
	protected int handleContinue(Line line) {
		return getEndLine();
	}

}