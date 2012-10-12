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

public class DataNode extends ParseTreeNode {

	private Variable value;
	
	public DataNode(Variable value, SourceCode.Line source) {
		super(ParseTreeNode.Type.NONARY, source);
		this.value = value;
	}
	
	@Override
	public int precedence() {
		return -1;
	}

	@Override
	protected DataNode operate(ParseTreeNode left, ParseTreeNode right) {
		return this;
	}
	
	public Variable getValue() {
		return value;
	}
	
	public String toString() {
		return value.toString();
	}
}
