package net.scheinerman.phoenix.interpreter;

import java.util.*;

import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.*;

public class ForInterpreter extends LoopInterpreter {
	
	private ArrayList<Token> initializationTokens;
	private ParseTreeNode increment;
	private ArrayList<Token> statementTokens;
	private int secondSeparator;
	
	public ForInterpreter(Interpreter parent, SourceCode source, int start, int end,
			SourceCode.Line statementLine, ArrayList<Token> statementTokens,
			int firstSeparator, int secondSeparator) {		
		super(parent, source, start, end, statementLine, statementTokens, firstSeparator + 1,
			  secondSeparator - 1, false, false, null);		
		
		if(firstSeparator > 1) {
			initializationTokens = new ArrayList<Token>();
			for(int index = 1; index < firstSeparator; index++) {
				initializationTokens.add(statementTokens.get(index));
			}
		} else {
			initializationTokens = null;
		}

		this.statementTokens = statementTokens;
		this.secondSeparator = secondSeparator;
	}
	
	@Override
	public EndCondition interpret() {
		getVAT().pushStackFrame();
		doInitialization();
		EndCondition endCondition = super.interpret();
		getVAT().popStackFrame();
		return endCondition;
	}
	
	@Override
	protected void performAtLoopEnd() {
		if(increment == null) {
			if(secondSeparator + 1 <= statementTokens.size() - 2) {
				increment = Parser.getParseTree(this, getPredicateLine(), statementTokens,
							secondSeparator + 1, statementTokens.size() - 2);
			}
		}
		
		if(increment != null) {
			increment.operate().getValue();			
		}
	}

	private void doInitialization() {
		if(initializationTokens != null) {
			if(isInitialization(initializationTokens)) {
				handleInitialization(initializationTokens, getPredicateLine());
			} else {
				Parser.parse(this, getPredicateLine(), initializationTokens);
			}
		}
	}
	
}
