package net.scheinerman.phoenix.parser;

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

public class ListNode extends ParseTreeNode {

	ArrayList<ParseTreeNode> nodes;
	
	public ListNode(SourceCode.Line source, ArrayList<ParseTreeNode> nodes) {
		super(Type.NONARY, source);
		this.nodes = new ArrayList<ParseTreeNode>();
		for(int index = 0; index < nodes.size(); index++) {
			if(index % 2 == 0) {
				this.nodes.add(nodes.get(index));
			} else {
				if(!(nodes.get(index) instanceof OperatorNode.ArgSeparator)) {
					throw new PhoenixRuntimeException("Arguments not comma separated.", source);
				}
			}
		}
	}

	@Override
	public int precedence() {
		return -1;
	}

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
