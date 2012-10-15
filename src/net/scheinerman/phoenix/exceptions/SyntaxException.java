// SyntaxException.java
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

package net.scheinerman.phoenix.exceptions;

import net.scheinerman.phoenix.interpreter.SourceCode.Line;

/**
 * Represents an Phoenix error that occurs due to an invalid syntax
 *
 * @author Jonah Scheinerman
 */
public class SyntaxException extends PhoenixRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new syntax error.
	 * @param message the description of the error
	 * @param sourceLine the line on which the error occurred
	 */
	public SyntaxException(String message, Line sourceLine) {
		super(message, sourceLine);
		setErrorType("Syntax error");
	}
	
}
