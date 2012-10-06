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

import net.scheinerman.phoenix.interpreter.*;

public class PhoenixRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String errorType = "Error";
	private String message;
	private SourceCode.Line sourceLine;
	
	public PhoenixRuntimeException(String message, SourceCode.Line sourceLine) {
		this.message = message;
		this.sourceLine = sourceLine;
	}
	
	public PhoenixRuntimeException() {
		
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setSourceLine(SourceCode.Line sourceLine) {
		this.sourceLine = sourceLine;
	}
	
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	
	public void printTrace() {
		System.err.println(errorType + ": " + message);
		System.err.println("    " + sourceLine);
	}
}
