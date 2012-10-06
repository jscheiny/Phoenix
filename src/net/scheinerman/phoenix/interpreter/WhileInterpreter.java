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

public class WhileInterpreter extends LoopInterpreter {

	public WhileInterpreter(Interpreter parent, SourceCode source, int start, int end,
			String predicate) {
		this(parent, source, start, end, predicate, null);
	}

	public WhileInterpreter(Interpreter parent, SourceCode source, int start, int end,
			String predicate, OtherwiseInterpreter otherwise) {
		super(parent, source, start, end, source.line(start - 1), predicate, true, false,
			  otherwise);
	}
	
	public WhileInterpreter(Interpreter parent, SourceCode source, int start, int end,
			ArrayList<Token> predicateTokens, int predicateTokenStart, int predicateTokenEnd,
			OtherwiseInterpreter otherwise) {
		super(parent, source, start, end, source.line(start - 1), predicateTokens,
			  predicateTokenStart, predicateTokenEnd, true, false, otherwise);
	}
}
