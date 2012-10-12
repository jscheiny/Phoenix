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


public abstract class ParseTreeNode {

	public enum Type {
		BINARY,			// operand * operand
		PREFIX_UNARY,	// * operand
		POSTFIX_UNARY,	// operand *
		NONARY			// *
	}
	
	public enum Surround {
		PARENTHESES,
		BRACKETS,
		NONE
	}
	
	private SourceCode.Line source;
	
	private Type operationType;
	private ParseTreeNode left = null;
	private ParseTreeNode right = null;
	
	private Surround surround = Surround.NONE;
	
	public ParseTreeNode(Type type, SourceCode.Line source) {
		this.operationType = type;
		this.source = source;
	}
	
	public final ParseTreeNode left() {
		return left;
	}
	
	public final void setLeft(ParseTreeNode left) {
		if(operationType != Type.PREFIX_UNARY && operationType != Type.NONARY) {
			this.left = left;
		}
	}

	public final ParseTreeNode right() {
		return right;
	}
	
	public final void setRight(ParseTreeNode right) {
		if(operationType != Type.POSTFIX_UNARY && operationType != Type.NONARY) { 
			this.right = right;
		}
	}
	
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
	
	public final Surround getSurround() {
		return surround;
	}
	
	public final void setSurround(Surround surround) {
		this.surround = surround;
	}
	
	public final SourceCode.Line getSourceLine() {
		return source;
	}
	
	public final Type getOperationType() {
		return operationType;
	}
	
	public abstract int precedence();
	
	protected abstract DataNode operate(ParseTreeNode left, ParseTreeNode right);
	
	public final DataNode operate() {
		return operate(left, right);
	}
}
