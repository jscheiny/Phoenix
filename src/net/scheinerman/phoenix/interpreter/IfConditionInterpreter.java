// IfConditionInterpreter.java
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
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.*;
import net.scheinerman.phoenix.variables.*;

public class IfConditionInterpreter extends Interpreter {

	private ArrayList<Token> predicateTokens = null;
	private int predicateStartToken;
	private int predicateEndToken;
	private ParseTreeNode predicateTree;
	
	public IfConditionInterpreter(Interpreter parent, SourceCode source, int start,
			int end, ArrayList<Token> predicateTokens, int predicateStartToken,
			int predicateEndToken) {
		super(parent, source, start, end);
		this.predicateTokens = predicateTokens;
		this.predicateStartToken = predicateStartToken;
		this.predicateEndToken = predicateEndToken;
	}
	
	public boolean isPredicateTrue() {
		setVAT(getParent().getVAT());
		
		SourceCode.Line predicateLine = getSourceCode().line(getStartLine() - 1);
		
		if(predicateTree == null) {
			predicateTree = Parser.getParseTree(this, predicateLine, predicateTokens,
				predicateStartToken, predicateEndToken);
		}
		
		Variable predicateEvaluation = predicateTree.operate().getValue();
		if(predicateEvaluation instanceof BooleanVariable) {
			return ((BooleanVariable)predicateEvaluation).getValue();
		}
		throw new SyntaxException("Condition predicate must evaluate to type bool.", predicateLine);
	}

}
