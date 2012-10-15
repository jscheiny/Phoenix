// PhoenixRuntimeException.java
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

import java.util.*;

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;

import com.sun.tools.javac.util.*;

/**
 * Represents an error that gets thrown in the course of Phoenix execution that is a result of an
 * error in the code. This is the root class of all Phoenix runtime errors. Any exception that gets
 * thrown that is not a child of this class is considered an internal error, and gets its own
 * special message reporting an internal error occuring. Any Phoenix runtime error is handled to
 * build up a Phoenix stack trace and can be caught by Phoenix try/catch blocks.
 * 
 * @author Jonah Scheinerman
 */
public class PhoenixRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** A one or two word description giving the type of error. */
	private String errorType = "Error";
	
	/** A one line message explaining the error. */
	private String message;
	
	/** The line on which the error occurred. */
	private SourceCode.Line sourceLine;
	
	/**
	 * The trace of function calls that were made to get to the line of the error. Each element
	 * is pair consisting of the line on which a function call was made, and the name of the
	 * function being called.
	 */
	private static Queue<Pair<SourceCode.Line, String>> trace = new LinkedList<Pair<Line,String>>();
	
	/**
	 * Construct a new Phoenix runtime error from a given line with a message description.
	 * @param message the description of the error that occurred
	 * @param sourceLine the line on which the error occurred
	 */
	public PhoenixRuntimeException(String message, SourceCode.Line sourceLine) {
		this.message = message;
		this.sourceLine = sourceLine;
	}
	
	/**
	 * Construct an empty Phoenix error.
	 */
	public PhoenixRuntimeException() {
		
	}
	
	/**
	 * Sets the description of this error.
	 * @param message the new description of the error
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Sets the line on which the error occurred
	 * @param sourceLine the line on which the error occurred
	 */
	public void setSourceLine(SourceCode.Line sourceLine) {
		if(this.sourceLine == null)
			this.sourceLine = sourceLine;
	}
	
	/**
	 * Returns the line on which the error occurred.
	 * @return the line on which the error occurred
	 */
	public SourceCode.Line getSourceLine() {
		return sourceLine;
	}
	
	/**
	 * Sets the type of error for this runtime exception.
	 * @param errorType the new error type
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	
	/**
	 * Prints the Phoenix stack trace to standard error.
	 */
	public void printPhoenixTrace() {
		System.err.println(errorType + ": " + message);
		System.err.println("        " + sourceLine);
		while(!trace.isEmpty()) {
			Pair<Line, String> traceElement = trace.remove();
			System.err.println("... in " + traceElement.snd + " " + traceElement.fst.getLocationString());
		}
	}
	
	/**
	 * Adds a function call to the trace of this error. 
	 * @param line the line on which the function is called
	 * @param interpreter the function interpreter being called
	 */
	public void addFunctionTrace(Line line, FunctionInterpreter interpreter) {
		trace.add(new Pair<SourceCode.Line, String>(line, interpreter.getName()));
	}
}
