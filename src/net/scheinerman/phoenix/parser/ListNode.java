// ListNode.java
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

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

/**
 * Represents a list of comma separated expressions as a list of subtrees, which when operated upon
 * will generate either an array or a tuple depending on the {@link Surround}. When enclosed in
 * brackets, this will return a {@link DataNode} wrapping an {@link ArrayVariable}. Otherwise, this
 * will generate a {@link DataNode} wrapping a {@link TupleVariable}.
 *
 * @author Jonah Scheinerman
 */
public class ListNode extends ParseTreeNode {

	/** This list of comma separated expressions as subtrees. */
	ArrayList<ParseTreeNode> nodes;
	
	/** 
	 * Creates a new list node from the source line and a list of subtrees. This list of subtrees
	 * must be comma separated. That is, every other element should be an instance of
	 * {@link OperatorNode#ArgSeparator}. If this is not the case this throws a
	 * {@link SyntaxException}.
	 * @param source the source line from which this node was generated
 	 * @param node the comma separated subtrees
 	 * @throws SyntaxException if the list of nodes is not comma separated
	 */
	public ListNode(SourceCode.Line source, ArrayList<ParseTreeNode> nodes) {
		super(Type.NONARY, source);
		this.nodes = new ArrayList<ParseTreeNode>(nodes.size() / 2 + 1);
		for(int index = 0; index < nodes.size(); index++) {
			if(index % 2 == 0) {
				this.nodes.add(nodes.get(index));
			} else {
				if(!(nodes.get(index) instanceof OperatorNode.ArgSeparator)) {
					throw new SyntaxException("Arguments not comma separated.", source);
				}
			}
		}
	}

	/**
	 * Returns the list of nodes either as a tuple or an array. If the {@link Surround} for this
	 * node is brackets, then an array will be returned. Otherwise, a tuple will be returned.
	 * @param left the left-hand subtree operand
	 * @param right the right-hand subtree operand
	 * @param a {@link DataNode} wrapping either an {@link ArrayVariable} or a {@link TupleVariable}
	 * depending on the {@link Surround} for this node.
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		try {
			ArrayList<Variable> elements = new ArrayList<Variable>(nodes.size());
			for(ParseTreeNode node : nodes) {
				elements.add(node.operate().getValue());
			}
			
			Variable ret;
			if(getSurround() == Surround.BRACKETS)
				ret = new ArrayVariable(elements);
			else
				ret = new TupleVariable(elements);
			
			return new DataNode(ret, getSourceLine());
		} catch(PhoenixRuntimeException e) {
			throw e;
		}
	}

}
