// OtherwiseInterpreter.java
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

/**
 * An interpreter which executes an otherwise block. That is, an otherwise is a block that only
 * executes after a loop that never executed.
 *
 * @author Jonah Scheinerman
 */
public class OtherwiseInterpreter extends Interpreter {

    /**
     * Construct a new otherwise interpreter.
     * @param parent the interpreter that is instantiating and running this interpreter
     * @param source the source code that is being interpreted
     * @param start the line on which to start interpreting
     * @param end the last line to interpret (the line at this index will be interpreted)
     */
	public OtherwiseInterpreter(Interpreter parent, SourceCode source, int start, int end) {
		super(parent, source, start, end);
	}

}
