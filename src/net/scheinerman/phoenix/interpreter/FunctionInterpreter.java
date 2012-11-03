// FunctionInterpreter.java
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
import net.scheinerman.phoenix.variables.*;

import com.sun.tools.javac.util.*;

/**
 * Interprets and executes a function. Each function interpreter is paired with a corresponding
 * {@link FunctionVariable} as functions are effectively variables in the Phoenix language.
 *
 * @author Jonah Scheinerman
 */
public class FunctionInterpreter extends Interpreter {

	/** The VAT of the parent interpreter. */
	private VariableAllocationTable parentVAT;

	/** The declared return type of this function. */
	protected String returnType;
	
	/** The name of the function. */
	protected String name;
	
	/** The declared left-hand arguments of the function as a list of type-name pairs. */
	protected ArrayList<Pair<String, String>> leftArgs;

	/** The declared right-hand arguments of the function as a list of type-name pairs. */
	protected ArrayList<Pair<String, String>> rightArgs; 
	
	/**
	 * Creates a new function interpreter. If there are no arguments on a side, then the
	 * corresponding parameter (<code>leftArgs</code> or </code>rightArgs</code>) should be either
	 * <code>null</code> or an empty list.
	 * @param parent the interpreter creating this interpreter (not the one from which this function
	 * might be called)
	 * @param start the line on which to start interpreting
	 * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param returnType the return type of the function
	 * @param name the name of the function
	 * @param leftArgs the left-hand arguments as a list of type-name pairings
	 * @param rightArgs the right-hand arguments as a list of type-name pairings
	 */
	public FunctionInterpreter(Interpreter parent, int start, int end, String returnType,
			String name, ArrayList<Pair<String, String>> leftArgs,
			ArrayList<Pair<String, String>> rightArgs) {
		super(parent, start, end);
		parentVAT = parent.getVAT();
	
		this.returnType = returnType;
		this.name = name;
		this.leftArgs = leftArgs;
		this.rightArgs = rightArgs;
	}
	
	/**
	 * Returns the function definition as a string. For example, for a function that was declared as
	 * <code>function int (int) factorial:</code> this method would return a string
	 * <code>function int (int) factorial</code>.
	 * @return the function definition
	 */
	public String getDefinition() {
		String def = Statement.FUNCTION.getKeyword() + " " + returnType + " ";
		if(leftArgs != null && leftArgs.size() != 0) {
			def += "(";
			for(int index = 0; index < leftArgs.size(); index++) {
				Pair<String, String> pair = leftArgs.get(index);
				def += pair.fst;
				if(index != leftArgs.size() - 1) {
					def += ", ";
				}
			}
			def += ") ";
		}
		def += name;
		if(rightArgs != null && rightArgs.size() != 0) {
			def += " (";
			for(int index = 0; index < rightArgs.size(); index++) {
				Pair<String, String> pair = rightArgs.get(index);
				def += pair.fst;
				if(index != rightArgs.size() - 1) {
					def += ", ";
				}
			}
			def += ")";
		}
		
		return def;
	}
	
	/**
	 * Assigns the passed arguments to the vat using the names from the function declaration. It is
	 * assumed that these arguments have already been checked for validity.
	 * @param vat the VAT that the arguments are being allocated in
	 * @param passed the passed arguments
	 * @param declared the declared arguments
	 */
	private void assignArguments(VariableAllocationTable vat, Variable passed,
			ArrayList<Pair<String, String>> declared) {
		if(passed == null) return;
		if(!(passed instanceof TupleVariable)) {
			Pair<String, String> typeNamePair = declared.get(0);
			passed.setLiteral(false);
			vat.allocate(typeNamePair.snd, passed);
		} else {
			TupleVariable tuple = (TupleVariable)passed;
			for(int index = 0; index < tuple.size(); index++) {
				Pair<String, String> typeNamePair = declared.get(index);
				Variable value = tuple.getElement(index);
				value.setLiteral(false);
				vat.allocate(typeNamePair.snd, value);
			}
		}
	}
	
	/**
	 * Checks the passed arguments against the declared arguments for validity. If the passed
	 * arguments do not match the number and types of the declared parameters then the passed
	 * arguments are not valid, and false is returned.
	 * @param passed the passed arguments
	 * @param declared the declared arguments
	 * @return true if the passed arguments match the declared arguments
	 */
	protected boolean checkArguments(Variable passed, ArrayList<Pair<String, String>> declared) {
		if(declared == null || declared.size() == 0) {
		   if(passed != null) {
			   return false;
		   }
		} else if(declared.size() == 1) {
			if(passed == null) {
			   return false;
			}
			if(passed instanceof TupleVariable) {
			   return false;
			}
			if(!declared.get(0).fst.equals(passed.getTypeName())) {
				return false;
			}			
		} else {
			if(passed == null) {
				return false;
			}
			if(!(passed instanceof TupleVariable)) {
				return false;
			}
			TupleVariable tuple = (TupleVariable)passed;
			if(tuple.size() != declared.size()) {
				return false;
			}
			for(int index = 0; index < declared.size(); index++) {
				if(!tuple.getElement(index).getTypeName().equals(declared.get(index).fst)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Makes the call to this function, intepreting the body of the function with the passed 
	 * arguments.
	 * @param callee the function variable that was called (corresponds to this interpreter)
	 * @param left the left-hand arguments to the function
	 * @param right the right-hand arguments to the function
	 */
	public void call(Variable callee, Variable left, Variable right) {
		if(!checkArguments(left, leftArgs) || !checkArguments(right, rightArgs)) {
			throw new InvalidCallParametersException(callee, left, right, null);
		}
		
		// Callee save the current variables in the execution environment
		VariableAllocationTable currVAT = getVAT();
		
		// Set up a new VAT for the execution
		VariableAllocationTable executionVAT = parentVAT.getGlobal();
		
		// Assign argument variables
		executionVAT.pushStackFrame();
		assignArguments(executionVAT, left, leftArgs);
		assignArguments(executionVAT, right, rightArgs);
		setVAT(executionVAT);
		
		interpret();
		
		executionVAT.popStackFrame();
		
		// Callee restore the current variables in the execution environment
		setVAT(currVAT);
		
		if(returnVariable == null) {
			returnVariable = new VoidVariable();
		}
	}
	
	/**
	 * Returns the name of the function.
	 * @return the name of the function
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the function.
	 * @param name the name of the function
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the left-hand arguments to the function. The arguments are formatted as a list of
	 * type-name pairs. If there are no left-hand arguments this may either return an empty list
	 * or <code>null</code>.
	 * @return the left-hand arguments to the function
	 */
	public ArrayList<Pair<String, String>> getLeftArgs() {
		return leftArgs;
	}
	
	/**
	 * Returns the right-hand arguments to the function. The arguments are formatted as a list of
	 * type-name pairs. If there are no right-hand arguments this may either return an empty list
	 * or <code>null</code>.
	 * @return the right-hand arguments to the function
	 */
	public ArrayList<Pair<String, String>> getRightArgs() {
		return rightArgs;
	}

	/**
	 * Returns the return type of the function.
	 * @return the return type of the function
	 */
	public String getReturnType() {
		return returnType;
	}
	
	/**
	 * This is overridden to act like a top level interpreter, and check the return value of the
	 * function. If a break or continue hits this function, then an error gets thrown. If a return
	 * gets called within the function, then its value is checked against the return type of the
	 * function and if they do not match, an error gets thrown. If the interpetation ends normally,
	 * and this is a non-void function, then an error gets thrown.
	 */
	@Override
	protected void handleEndCondition(EndCondition endCondition) {
		if(endCondition == EndCondition.NORMAL) {
			if(!getReturnType().equals(Strings.VOID)) {
				throw new SyntaxException("Non-void function must return a value",
					getEndConditionLine());
			}
			return;
		}
		
		if(endCondition == EndCondition.BREAK) {
			throw new SyntaxException("Cannot have " + Statement.BREAK +
				" statement outside of a loop.", getEndConditionLine());
		}
		
		if(endCondition == EndCondition.CONTINUE) {
			throw new SyntaxException("Cannot have " + Statement.CONTINUE + 
				" statement outside of a loop.", getEndConditionLine());
		}
		
		if(endCondition == EndCondition.RETURN) {
			if(getReturnType().equals(Strings.VOID) && getReturnVariable() != null) {
				throw new SyntaxException("Void function cannot return a value.",
					getEndConditionLine());
			}
			if(getReturnVariable() == null) {
				throw new SyntaxException("Non-void function must return a value",
					getEndConditionLine());
			}
			if(!getReturnType().equals(getReturnVariable().getTypeName())) {
				throw new SyntaxException("Function expected to return " + getReturnType() +
					" but returned " + getReturnVariable().getTypeName(), getEndConditionLine());
			}
		}
	}
	
}
