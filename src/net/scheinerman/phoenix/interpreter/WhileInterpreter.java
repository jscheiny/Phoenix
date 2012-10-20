// WhileInterpreter.java
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

import net.scheinerman.phoenix.parser.Tokenizer.Token;

/**
 * Interprets and executes a while loop. A while loop is a loop that waits for the predicate to
 * become false, and checks the predicate at the beginning.
 *
 * @author Jonah Scheinerman
 */
public class WhileInterpreter extends LoopInterpreter {
	
	/**
	 * Creates a new while loop interpreter.
	 * @param parent the interpreter that is instantiating and running this interpreter
	 * @param source the source code that is being interpreted
	 * @param start the line on which to start interpreting
	 * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param predicateTokens the tokenization of the predicate
	 * @param predicateStartToken the starting index of the predicate in the tokenization
	 * @param predicateEndToken the ending index of the predicate in the tokenization (inclusive)
	 * @param otherwise the interpreter for executing the otherwise block (null if there is none)
	 */
	public WhileInterpreter(Interpreter parent, SourceCode source, int start, int end,
			ArrayList<Token> predicateTokens, int predicateStartToken, int predicateEndToken,
			OtherwiseInterpreter otherwise) {
		super(parent, source, start, end, source.line(start - 1), predicateTokens,
				predicateStartToken, predicateEndToken, true, false, otherwise);
	}
}
