// LoopInterpreter.java
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
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

public class LoopInterpreter extends Interpreter {

	private SourceCode.Line predicateLine;
	private ParseTreeNode predicateParseTree;
	private boolean predicateCheckedAtBeginning;
	private boolean predicateLoopEndValue;
	private OtherwiseInterpreter otherwise;
	
	private boolean loopDone = false;
	
	public LoopInterpreter(Interpreter parent, SourceCode source, int start, int end,
			SourceCode.Line predicateLine, String predicate, boolean predicateCheckedAtBeginning,
			boolean predicateLoopEndValue) {

		this(parent, source, start, end, predicateLine, predicate, predicateCheckedAtBeginning,
			 predicateLoopEndValue, null);
	}
	
	public LoopInterpreter(Interpreter parent, SourceCode source, int start, int end, 
			SourceCode.Line predicateLine, String predicate, boolean predicateCheckedAtBeginning,
			boolean predicateLoopEndValue, OtherwiseInterpreter otherwise) {

		super(parent, source, start, end, true);
		this.predicateLine = predicateLine;
		this.predicateCheckedAtBeginning = predicateCheckedAtBeginning;
		this.predicateLoopEndValue = predicateLoopEndValue;
		this.otherwise = otherwise;

		predicateParseTree = Parser.getParseTree(this, predicateLine, predicate);
	}
	
	public LoopInterpreter(Interpreter parent, SourceCode source, int start, int end,
			SourceCode.Line predicateLine, ArrayList<Token> predicateTokens,
			int predicateStartToken, int predicateEndToken, boolean predicateCheckedAtBeginning,
			boolean predicateLoopEndValue) {
		this(parent, source, start, end, predicateLine, predicateTokens, predicateStartToken,
			 predicateEndToken, predicateCheckedAtBeginning, predicateLoopEndValue, null);
	}
	
	public LoopInterpreter(Interpreter parent, SourceCode source, int start, int end,
			SourceCode.Line predicateLine, ArrayList<Token> predicateTokens,
			int predicateStartToken, int predicateEndToken, boolean predicateCheckedAtBeginning,
			boolean predicateLoopEndValue, OtherwiseInterpreter otherwise) {
		super(parent, source, start, end, true);
		
		this.predicateLine = predicateLine;
		this.predicateCheckedAtBeginning = predicateCheckedAtBeginning;
		this.predicateLoopEndValue = predicateLoopEndValue;
		this.otherwise = otherwise;
		
		predicateParseTree = Parser.getParseTree(this, predicateLine, predicateTokens,
			predicateStartToken, predicateEndToken);
	}
	
	public void interpret() {
		if(isPredicateCheckedAtBeginning() && isLoopDone()) {
			if(otherwise != null) {
				otherwise.interpret();
			}
			return;
		}
		do {
			super.interpret();
		} while(!isLoopDone());
	}
	
	public final boolean isPredicateCheckedAtBeginning() {
		return predicateCheckedAtBeginning;
	}
	
	public final boolean checkPredicate() {
		Variable predicateEvaluation = predicateParseTree.operate().getValue();
		if(predicateEvaluation instanceof BooleanVariable) {
			return ((BooleanVariable)predicateEvaluation).getValue();
		}
		throw new SyntaxException("Loop predicate must evaluate to type bool.", predicateLine);
	}
	
	public final boolean isLoopDone() {
		if(checkPredicate() == predicateLoopEndValue) {
			loopDone = true;
		}
		return loopDone;
	}
	
	@Override
	protected int handleBreak(Line line, int index) {
		loopDone = true;
		return getEndLine();
	}
	
	@Override
	protected int handleContinue(Line line, int index) {
		System.out.println("CONTINUE");
		return getEndLine();
	}

}