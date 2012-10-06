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

public class VariableAllocationTable {
	
	public static class StackFrame {
		private HashMap<String, Variable> variables;
		
		public StackFrame() {
			variables = new HashMap<String, Variable>();
		}
		
		public boolean hasVariable(String name) {
			return variables.containsKey(name);
		}
		
		public Variable getVariable(String name) {
			return variables.get(name);
		}
		
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

	private StackFrame globalNamespace = new StackFrame();
	private LinkedList<StackFrame> stack = new LinkedList<StackFrame>();
	
	public VariableAllocationTable() {

	}
	
	public void pushStackFrame() {
		stack.addFirst(new StackFrame());
	}
	
	public void popStackFrame() {
		stack.removeFirst();
	}
	
	public void allocate(String label, Variable value) {
		stack.getFirst().putVariable(label, value);
	}
	
	public void allocateGlobal(String label, Variable value) {
		globalNamespace.putVariable(label, value);
	}
	
	public boolean hasVariable(String name) {
		return getVariable(name) != null;
	}
	
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
	
	public void print() {
		String indent = "  ";
		for(StackFrame frame : stack) {
			System.out.println(indent + frame);
			indent += "  ";
		}
		System.out.println(indent + globalNamespace);
	}
}
