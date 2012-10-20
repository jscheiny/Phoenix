// ParseTreeNode.java
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

/**
 * Represents a node in the tree built from an expression in the Phoenix language. Nodes in the tree
 * can be things like variables, functions, operators, argument lists, etc. Different node types
 * should be implemented as sublasses of this class.
 *
 * @author Jonah Scheinerman
 */
public abstract class ParseTreeNode {

	/**
	 * The different types of operations a node can represent. A {@link Type#BINARY} type indicates
	 * operands on both the left and the right of the operator. A {@link Type#PREFIX_UNARY} type
	 * indicates an operation with a single operand on the right. A {@link Type#POSTFIX_UNARY} type
	 * indicates an operation with a single operand on the left. A {@link Type#NONARY} type iis an
	 * operation with no operands (e.g. variables).
	 *
	 * @author Jonah Scheinerman
	 */
	public enum Type {
		BINARY,	
		PREFIX_UNARY,
		POSTFIX_UNARY,
		NONARY
	}
	
	/**
	 * The different possible ways this node could have been enclosed in the expression it was
	 * parsed from. A {@link Surround#PARENTHESES} surround is enclosed by ( and ). A
	 * {@link Surround#BRACKETS} surround is enclosed by [ and ]. A {@link Surround#NONE} has no
	 * surround (the expression it was derived was probably the original expression which was not
	 * enclosed by parentheses).
	 *
	 * @author Jonah Scheinerman
	 */
	public enum Surround {
		PARENTHESES,
		BRACKETS,
		NONE
	}
	
	/** The line of code containing expression from which this parse tree was generated. */
	private SourceCode.Line source;
	
	/** The type of operation represented by this node. */
	private Type operationType;

	/** The left-hand operand to this node. */
	private ParseTreeNode left = null;

	/** The right-hand operand to this node. */
	private ParseTreeNode right = null;
	
	/** The type of surround of this node. */
	private Surround surround = Surround.NONE;
	
	/**
	 * Construct a node with a given type from an expression on the given source line.
	 * @param type the type of node operation
	 * @param source the line containing the expression from which this node was generated
	 */
	public ParseTreeNode(Type type, SourceCode.Line source) {
		this.operationType = type;
		this.source = source;
	}
	
	/**
	 * Returns the left subtree of this node. If no left subtree has been set, returns
	 * <code>null</code>
	 * @param the left subtree of this node
	 */
	public final ParseTreeNode left() {
		return left;
	}
	
	/**
	 * Sets the left subtree of this node if the type of this node allows left-hand operands. That
	 * is if the type of this node is either {@link Type#POSTFIX_UNARY} or {@link Type#BINARY} then
	 * the left subtree will be updated. Otherwise, the value remains null.
	 * @param left the new left subtree for this node
	 */
	public final void setLeft(ParseTreeNode left) {
		if(operationType != Type.PREFIX_UNARY && operationType != Type.NONARY) {
			this.left = left;
		}
	}

	/**
	 * Returns the right subtree of this node. If no right subtree has been set, returns
	 * <code>null</code>
	 * @param the right subtree of this node
	 */
	public final ParseTreeNode right() {
		return right;
	}
	
	/**
	 * Sets the right subtree of this node if the type of this node allows right-hand operands. That
	 * is if the type of this node is either {@link Type#PREFIX_UNARY} or {@link Type#BINARY} then
	 * the right subtree will be updated. Otherwise, the value remains null.
	 * @param right the new right subtree for this node
	 */
	public final void setRight(ParseTreeNode right) {
		if(operationType != Type.POSTFIX_UNARY && operationType != Type.NONARY) { 
			this.right = right;
		}
	}
	
	/**
	 * Based on the type of operation this node represents, returns whether its operands have been
	 * set. Thus if the type of this node is {@link Type#BINARY} then this will return true if both
	 * the left and right subtrees are not null.
	 * @return whether the operands necessary for this node have been set
	 */
	public boolean areOperandsSet() {
		if(operationType == Type.PREFIX_UNARY) {
			return right != null;
		}
		if(operationType == Type.POSTFIX_UNARY) {
			return left != null;
		}
		if(operationType == Type.BINARY) {
			return left != null && right != null;
		}
		return true;
	}
	
	/**
	 * Retrieves the surround type for this node
	 * @see Surround
	 * @return the surround for this node
	 */
	public final Surround getSurround() {
		return surround;
	}

	/**
	 * Sets the surround type for this node
	 * @see Surround
	 * @param surround the new surround type
	 */
	public final void setSurround(Surround surround) {
		this.surround = surround;
	}
	
	/**
	 * Retrieves the line containing the expression whose parsing generated this node.
	 * @return the line from which this node was created
	 */
	public final SourceCode.Line getSourceLine() {
		return source;
	}
	
	/**
	 * Returns the operation type that this node represents.
	 * @return the operation type that this node represents
	 */
	public final Type getOperationType() {
		return operationType;
	}
	
	/**
	 * Performs the operation of this node using the given left and right hand operands. This should
	 * be implemented by subclasses to perform the action of this tree node. Note that either left
	 * or right may be null based on the operation type of this node.
	 * @param left the left hand operand subtree
	 * @param right the right hand operand subtree
	 * @return the result of the operation as a {@link DataNode} (which just encapsulates a
	 * Variable).
	 */
	protected abstract DataNode operate(ParseTreeNode left, ParseTreeNode right);
	
	/**
	 * Performs the operation of this node using the left and right hand operands that have been set
	 * for this particular node.
	 * @return the result of the operation of this node using the set left and right hand operands.
	 */
	public final DataNode operate() {
		return operate(left, right);
	}
}
