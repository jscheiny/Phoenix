// CallNode.java
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
import net.scheinerman.phoenix.variables.*;

/** 
 * A node whose operation is to perform the call operation on a callee node. Thus this node is
 * effectively a ternary operation: the callee and the left and right hand operands. This calls the
 * call method on the variable retrieved by operating on the callee.
 *
 * @author Jonah Scheinerman
 */
public class CallNode extends ParseTreeNode {

	/** The node that is being called. */
	private ParseTreeNode callee;
	
	/**
	 * Constructs a new call operation using the call node, the type of call (left hand, right hand,
	 * or both), and the source line that this node was generated from.
	 * @param callee the node being called
	 * @param type the type of operation
	 * @param source the line from which this node was generated
	 */
	public CallNode(ParseTreeNode callee, Type type, SourceCode.Line source) {
		super(type, source);
		this.callee = callee;
	}
	
	@Override
	public String toString() {
		return (left() != null ? left().toString() : "") +
			   callee.toString() +
			   (right() != null ? right().toString() : "");
	}

	/**
	 * Calls the call method on the callee value using the left and right hand operands.
	 * @param left the left-hand subtree
	 * @param right the right-hand subtree
	 * @return the result of the call operation on the callee using the left and right parameters
	 */
	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		Variable calleeValue = callee.operate().getValue();
		try {
			Variable leftValue = (left == null ? null : left.operate().getValue().passValue());
			Variable rightValue = (right == null ? null : right.operate().getValue().passValue());
			
			return new DataNode(calleeValue.call(leftValue, rightValue), getSourceLine());
			
		} catch(UnsupportedOperatorException e) {
			e.setSourceLine(getSourceLine());
			if(!(calleeValue instanceof FunctionVariable))
				e.setMessage(calleeValue.getTypeName() + " type variables are not callable.");
			throw e;
		} catch(PhoenixRuntimeException e) {
			e.setSourceLine(getSourceLine());
			if(calleeValue instanceof FunctionVariable) {
				e.addFunctionTrace(getSourceLine(),
					((FunctionVariable)calleeValue).getInterpreter());
			}
			throw e;
		}
	}

}
