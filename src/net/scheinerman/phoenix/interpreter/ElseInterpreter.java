// ElseInterpreter.java
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
 * An interpreter which executes an else block. This is a block that only executes if all of the
 * other if/else if blocks in an if clause fail to execute.
 *
 * @author Jonah Scheinerman
 */
public class ElseInterpreter extends Interpreter {

    /**
     * Construct a new else interpreter.
     * @param parent the parent interpreter that is instantiating and running this one
     * @param start the line on which to start interpreting
     * @param end the last line to interpret (the line at this index will be interpreted)
     */
	public ElseInterpreter(Interpreter parent, int start, int end) {
		super(parent, start, end);
	}

}
