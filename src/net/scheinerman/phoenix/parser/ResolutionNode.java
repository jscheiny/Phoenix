// ResolutionNode.java
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

package net.scheinerman.phoenix.parser;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.parser.OperatorNode.FunctionReference;
import net.scheinerman.phoenix.variables.*;

/**
 * A node which wraps a variable name, that is resolved to a variable value only when the tree is
 * operated upon. This is useful to make sure that when a tree is operated multiple times, it is
 * always retrieving the correct value associated with the given variable name.
 *
 * @author Jonah Scheinerman
 */
public class ResolutionNode extends ParseTreeNode {

	/** The interpreter from which the variable is being looked up. */
	private Interpreter interpreter;

	/** The name of the variable to be resolved. */
	private String name;

	/** Whether this resolution node has been wrapped in a function reference. */
	private boolean referenced = false;

	/**
	 * Creates a new resolution node with the interpreter, variable name, and the source code line.
	 * @param interpreter the interpreter which is used to look up the variable
	 * @param name the name of the variable to be resolved when operating on this node
	 * @param source the line of code from which this node was generated
	 */
	public ResolutionNode(Interpreter interpreter, String name, Line source) {
		super(Type.NONARY, source);
		this.interpreter = interpreter;
		this.name = name;
	}

	/**
	 * Implemented to look up the variable name in the interpreter's {@link VariableAllocationTable}
	 * and wrap it in a {@link DataNode}. If the name does not exist in the interpreter's VAT, then
	 * this throws a {@link SyntaxException} saying that an unexpected symbol was found.
	 * @param left the left-hand subtree
	 * @param right the right-hand subtree
	 * @return the variable resolved from the name wrapped in a {@link DataNode}.
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		if(interpreter.getVAT().hasVariable(name)) {
			Variable var = interpreter.getVAT().getVariable(name);
			if(!referenced && var instanceof FunctionVariable) {
				return new CallNode(new DataNode(var, getSourceLine()),
									Type.NONARY, getSourceLine()).operate();
			}
			return new DataNode(var, getSourceLine());
		}
		throw new SyntaxException("Unexpected symbol '" + name + "'", getSourceLine());
	}

	/**
	 * Sets whether this node has been enclosed in a {@link FunctionReference} or in a
	 * {@link CallNode}. If this node has been, then this node will simply return the resolution of
	 * its name value. If however, this node has not been referenced and if the resolution of the
	 * variable's name is a function, then this makes the call to that function with no parameters.
	 * @param referenced whether this node has been referenced by a {@link FunctionReference} or a
	 * {@link CallNode}.
	 */
	public void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}
	
	@Override
	public String toString() {
		return "res[" + name + "]";
	} 
	
}
