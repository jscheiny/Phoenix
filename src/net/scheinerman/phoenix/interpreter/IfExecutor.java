// IfExecutor.java
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

public class IfExecutor {

	private LinkedList<IfConditionInterpreter> conditions
		= new LinkedList<IfConditionInterpreter>();
	private ElseInterpreter elseInterpreter = null;
	
	public IfExecutor() {
		
	}
	
	public void addCondition(IfConditionInterpreter condition) {
		conditions.add(condition);
	}
	
	public void setElseIntepreter(ElseInterpreter elseInterpreter) {
		this.elseInterpreter = elseInterpreter;
	}
	
	public void execute() {
		for(IfConditionInterpreter condition : conditions) {
			if(condition.isPredicateTrue()) {
				condition.interpret();
				return;
			}
		}
		if(elseInterpreter != null) {
			elseInterpreter.interpret();
		}
	}

}
