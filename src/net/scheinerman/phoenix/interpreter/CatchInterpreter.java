// CatchInterpreter.java
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
 * Interprets and executes a catch block. A catch block executes if there is a Phoenix error thrown
 * in the corresponding try block which is interpreted by a {@link TryInterpreter}.
 *
 * @author Jonah Scheinerman
 */
public class CatchInterpreter extends Interpreter {

    /**
     * Construct a new catch interpreter.
     * @param parent the interpreter that is instantiating and running this interpreter
     * @param source the source code that is being interpreted
     * @param start the line on which to start interpreting
     * @param end the last line to interpret (the line at this index will be interpreted)
     */	
	public CatchInterpreter(Interpreter parent, SourceCode source, int start, int end) {
		super(parent, source, start, end);
	}

    /**
     * Provide this intepreter with the error that was thrown within the corresponding 
     * {@link TryInterpreter}.
     * @param thrown the error that was thrown to spring this catch
     */
	public void setThrown(PhoenixRuntimeException thrown) {

	}
	
}
