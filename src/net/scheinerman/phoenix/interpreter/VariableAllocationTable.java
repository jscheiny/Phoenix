// VariableAllocationTable.java
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

import net.scheinerman.phoenix.variables.*;

/**
 * Holds all variable names, values, and scopes for an Phoenix interpretation. Each scope in a
 * Phoenix program is associated with a {@link StackFrame} instance. When starting a new scope,
 * simply call {@link VariableAllocationTable#pushStackFrame()} and when finished with that scope
 * call {@link VariableAllocationTable#popStackFrame()}. In addition there is also the global scope
 * which contains variables that have been declared as global and all top level variables. Looking
 * up a variable name will start searching through the most local scope, and go outward, finally
 * looking in the global scope if the name is not found in an inner scope.
 *
 * Variable allocation table will frequently be abbreviated VAT.
 *
 * @author Jonah Scheinerman
 */
public class VariableAllocationTable {
	
	/**
	 * Contains a single scope of variables for a Phoenix interpretation. This effectively a wrapper
	 * for a mapping from strings to variables.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class StackFrame {
		/** The mapping containing the variables in this scope. */
		private HashMap<String, Variable> variables;
		
		/** Creates a new empty stack frame. */
		public StackFrame() {
			variables = new HashMap<String, Variable>();
		}
		
		/**
		 * Returns whether this scope contains the given variable name.
		 * @param name the name of the variable being allocated
		 * @return true if the variable with the given name has been allocated in this scope
		 */
		public boolean hasVariable(String name) {
			return variables.containsKey(name);
		}
		
		/**
		 * Returns the variable with the given name.
		 * @param name the name of the variable being to be retrieved
		 * @return the variable with this name
		 */
		public Variable getVariable(String name) {
			return variables.get(name);
		}
		
		/**
		 * Puts a variable with a given name and value in to the mapping for this scope. If a
		 * variable of this name already exists, it is overridden.
		 * @param name the name of the variable
		 * @param value the variable value
		 */
		public void putVariable(String name, Variable value) {
			if(value == null) {
				throw new NullPointerException("Cannot allocate variable with value null.");
			}
			variables.put(name, value);
		}
		
		public String toString() {
			return variables.keySet().toString();
		}
	}

	/** The global scope. */
	private StackFrame globalNamespace = new StackFrame();

	/** The list of internal scopes, the first element is the most inner scope. */
	private LinkedList<StackFrame> stack = new LinkedList<StackFrame>();
	
	/**
	 * Creates a new VAT with a global namespace and an empty stack.
	 */
	public VariableAllocationTable() {
	}
	
	/**
	 * Creates a new VAT that shares the same global scope as this VAT but has an empty stack.
	 * @return a new VAT with the same global scope and an empty stack
	 */
	public VariableAllocationTable getGlobal() {
		VariableAllocationTable vat = new VariableAllocationTable();
		vat.globalNamespace = globalNamespace;
		return vat;
	}
	
	/** Pushes a new scope on to the stack of scopes. */
	public void pushStackFrame() {
		stack.addFirst(new StackFrame());
	}
	
	/** Pops the inner most scope from the stack of scopes. */
	public void popStackFrame() {
		stack.removeFirst();
	}
	
	/**
	 * Allocates a variable with a given name and value in the local scope (top of the stack).
	 * @param label the name of the variable
	 * @param value the variable value
	 */
	public void allocate(String label, Variable value) {
		stack.getFirst().putVariable(label, value);
	}
	
	/**
	 * Allocates a variable with a given name and value in the global scope.
	 * @param label the name of the variable
	 * @param value the variable value
	 */
	public void allocateGlobal(String label, Variable value) {
		globalNamespace.putVariable(label, value);
	}
	
	/**
	 * Returns whether the variable of the given name exists in any scope, local to global.
	 * @param name the name of the variable to search for
	 * @return true if such a variable exists, false otherwise
	 */
	public boolean hasVariable(String name) {
		return getVariable(name) != null;
	}
	
	/**
	 * Returns the variable of the given name. This searches through all scopes and finds the
	 * innermost variable of the given name. If no such variable can be found, returns null.
	 * @param name the name of the variable to search for
	 * @return the variable if it can be found, null otherwise
	 */
	public Variable getVariable(String name) {
		for(StackFrame stackFrame : stack) {
			if(stackFrame.hasVariable(name)) {
				return stackFrame.getVariable(name);
			}
		}
		if(globalNamespace.hasVariable(name)) {
			return globalNamespace.getVariable(name);
		}
		return null;
	}
}
