// IfConditionInterpreter.java
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
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

/**
 * Interprets a condition in a if/else if block if a given condition predicate evaluates to true.
 * The predicate is the phrase given to the if/else if statement, and this will interpreter
 * will be executed if all previous intepreters evaluate to false, and this one evaluates to true.
 * 
 * @see IfExecutor
 * @author Jonah Scheinerman
 */
public class IfConditionInterpreter extends Interpreter {
	
	/** The line containing the predicate for this conditional. */
	private SourceCode.Line predicateLine;
	
	/** The parse tree containing that evaluates the predicate of the conditional. */
	private ParseTreeNode predicateTree;
	
	/**
	 * Creates a new conditional interpreter
     * @param parent the interpreter that is instantiating and running this interpreter
     * @param start the line on which to start interpreting
     * @param end the last line to interpret (the line at this index will be interpreted)
     * @param predicateTokens the tokenization of the predicate
	 * @param predicateStartToken the starting index of the predicate in the tokenization
	 * @param predicateEndToken the ending index of the predicate in the tokenization (inclusive)
	 */
	public IfConditionInterpreter(Interpreter parent, int start, int end,
			ArrayList<Token> predicateTokens, int predicateStartToken, int predicateEndToken) {
		super(parent, start, end);

		predicateLine = getSourceCode().line(getStartLine() - 1);
		predicateTree = Parser.getParseTree(this, predicateLine, predicateTokens,
			predicateStartToken, predicateEndToken);
	}
	
	/**
	 * Evaluates the predicate and returns its boolean value. If the predicate does not evaluate
	 * to a boolean, then a {@link SyntaxException} is thrown.
	 * @return the boolean result of evaluating the predicate
	 */
	public boolean isPredicateTrue() {
		setVAT(getParent().getVAT());
		
		Variable predicateEvaluation = predicateTree.operate().getValue();
		if(predicateEvaluation instanceof BooleanVariable) {
			return ((BooleanVariable)predicateEvaluation).getValue();
		}
		throw new SyntaxException("Condition predicate must evaluate to type bool.", predicateLine);
	}

}
