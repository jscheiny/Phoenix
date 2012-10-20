// TryInterpreter.java
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

import net.scheinerman.phoenix.exceptions.*;

/**
 * Interprets and executes a try block. If a {@link PhoenixRuntimeException} is thrown during the
 * execution of a try block, then the corresponding {@link CatchInterpreter} will be executed.
 *
 * @author Jonah Scheinerman
 */
public class TryInterpreter extends Interpreter {

	/** The corresponding catch block interpreter. */
	private CatchInterpreter catchIntepreter;
	
	/**
	 * Creates a new try block interpreter
     * @param parent the interpreter that is instantiating and running this interpreter
     * @param source the source code that is being interpreted
     * @param start the line on which to start interpreting
     * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param catchInterpreter the interpreter for the corresponding catch block
	 */
	public TryInterpreter(Interpreter parent, SourceCode source, int start, int end,
			CatchInterpreter catchInterpreter) {
		super(parent, source, start, end);
		this.catchIntepreter = catchInterpreter;
	}

	/**
	 * Overridden to catch a {@link PhoenixRuntimeException} in the interpretation and execute
	 * the correpsonding catch block. The end condition will either be that of the try block, if
	 * no error was thrown, or that of the catch block if an error was thrown.
	 * @return the end condition of either the try or catch blocks, depending on whether an error
	 * was thrown in the try block
	 */
	@Override
	public EndCondition interpret() {
		try {
			return super.interpret();
		} catch(PhoenixRuntimeException exception) {
			catchIntepreter.setThrown(exception);

			EndCondition condition = catchIntepreter.interpret();
			endConditionLine = catchIntepreter.getEndConditionLine();
			returnVariable = catchIntepreter.getReturnVariable();
			return condition;
		}
	}
	
}
