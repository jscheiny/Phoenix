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

import net.scheinerman.phoenix.interpreter.Interpreter.EndCondition;
import net.scheinerman.phoenix.variables.*;

/**
 * Executes an if/else if/else clause. This executes the first {@link IfConditionInterpreter} that
 * has a true predicate, or if none has a true predicate, and there is one, executes the
 * {@link ElseInterpreter}.
 *
 * @author Jonah Scheinerman
 */
public class IfExecutor {

	/** The list of conditions containing the if/else if statements. */
	private List<IfConditionInterpreter> conditions = new LinkedList<IfConditionInterpreter>();
	
	/** The else block, or null if none. */
	private ElseInterpreter elseInterpreter = null;
	
	/** The condition under which the execution ended. */
	private EndCondition endCondition;
	
	/** The line at which the execution ended, if execution ended abnormally. */
	private SourceCode.Line endConditionLine;
	
	/** The returned variable, if a return was called with a value. */
	private Variable returnVariable;
	
	/**
	 * Constructs an if executor with no conditional statements, and no else statement.
	 */
	public IfExecutor() {	
	}
	
	/**
	 * Adds a conditional statement to this executor. The order in which conditions are added to
	 * the executor determine the order in which they will be checked. 
	 * @param condition the condition to add to this executor
	 */
	public void addCondition(IfConditionInterpreter condition) {
		conditions.add(condition);
	}
	
	/**
	 * Sets the else interpreter for this executor. The else statement is executed if none of the
	 * conditions evaluate to true.
	 * @param elseInterpreter the interpreter for the else statement
	 */
	public void setElseIntepreter(ElseInterpreter elseInterpreter) {
		this.elseInterpreter = elseInterpreter;
	}
	
	/**
	 * The line on which this executor ended abnormally. This is analagous to the
	 * {@link Interpreter#getEndConditionLine()} method.
	 * @return the line on which this executor ended abnormally (or null if execution ended
	 * normally)
	 */
	public SourceCode.Line getEndConditionLine() {
		return endConditionLine;
	}
	
	/**
	 * If a return was called within the execution of this executor, then this contains the value
	 * passed to that return. This is analagous to the {@link Interpreter#getReturnVariable()}
	 * method.
	 * @return the variable that was passed to the return if one was called in the execution of
	 * this if block
	 */
	public Variable getReturnVariable() {
		return returnVariable;
	}
	
	/**
	 * Executes the if/else if/else block. This will interpret at most a single condition/else, so
	 * the end conditions of the interpreted block are returned. If no condition/else is
	 * interpreted, then this returns {@link EndCondition#NORMAL}.
	 * @return the end condition of the interpreted block, or {@link EndCondition#NORMAL} if no
	 * block was interpreted.
	 */
	public EndCondition execute() {
		for(IfConditionInterpreter condition : conditions) {
			if(condition.isPredicateTrue()) {
				endCondition = condition.interpret();
				endConditionLine = condition.getEndConditionLine();
				returnVariable = condition.getReturnVariable();
				return endCondition;
			}
		}
		if(elseInterpreter != null) {
			endCondition = elseInterpreter.interpret();
			endConditionLine = elseInterpreter.getEndConditionLine();
			returnVariable = elseInterpreter.getReturnVariable();
			return endCondition;
		}
		return EndCondition.NORMAL;
	}

}
