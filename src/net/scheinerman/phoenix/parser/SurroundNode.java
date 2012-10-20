// Variable.java
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

import net.scheinerman.phoenix.interpreter.SourceCode.Line;

/**
 * A parse node which simply encapsulates another node, allowing one to add multiple surround types
 * to a single node. This is useful for example to represent a list of numbers enclosed in brackets
 * which are in turn enclosed in parentheses. Thus its possible through repeated enclosures by
 * SurroundNodes to add as many different surrounds as necessary.
 *
 * @author Jonah Scheinerman
 */
public class SurroundNode extends ParseTreeNode {

	/** The node being wrapped. */
	private ParseTreeNode internal;
	
	/**
	 * Creates a new node with the given node being encapsulated.
	 * @param internal the node that is being held within this surround
	 * @param source the line of source code from which this node was generated
	 */
	public SurroundNode(ParseTreeNode internal, Line source) {
		super(Type.NONARY, source);
		this.internal = internal;
	}

	@Override
	public String toString() {
		return "(" + internal.toString() + ")";
	}

	/**
	 * Returns the operation for the element being held by this surround.
	 * @param left the left-hand subtree operand
	 * @param right the right-hand subtree operand
	 * @return the operation result for the internal node.
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		return internal.operate();
	}
	
	/**
	 * Returns whether the internal node's operands have been set.
	 * @return whether the internal node's operands have been set
	 */
	@Override
	public boolean areOperandsSet() {
		return internal.areOperandsSet();
	}

}
