// DataNode.java
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

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

/**
 * A node which wraps a {@link Variable}. The result of any operation on a {@link ParseTreeNode}
 * should be a DataNode holding the Phoenix variable result of the operation.
 *
 * @author Jonah Scheinerman
 */
public class DataNode extends ParseTreeNode {

	/** The value being wrapped. */
	private Variable value;
	
	/**
	 * Creates a new data node with the given variable value and the source line.
	 * @param value the variable to wrap in this node
	 * @param source the line from which this node was generated
	 */
	public DataNode(Variable value, SourceCode.Line source) {
		super(ParseTreeNode.Type.NONARY, source);
		this.value = value;
	}

	/**
	 * Implemented to simply return this node.
	 * @param left the left-hand subtree operand
	 * @param right the right-hand subtree operand
	 * @return this
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		return this;
	}
	
	/**
	 * Returns the variable value being held in this node.
	 * @return the variable value of this node
	 */
	public Variable getValue() {
		return value;
	}

	public String toString() {
		return value.toString();
	}
}
