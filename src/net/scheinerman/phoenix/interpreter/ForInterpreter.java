package net.scheinerman.phoenix.interpreter;

import java.util.*;

import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

public class ForInterpreter extends LoopInterpreter {
	
	private ArrayList<Token> initializerTokens;
	private boolean isVariableInitialization = false;
	private String initializerType = null;
	private String initializerName = null;
	private ParseTreeNode initializerTree;
	private ParseTreeNode incrementTree;
	private ArrayList<Token> statementTokens;
	private int secondSeparator;
	
	public ForInterpreter(Interpreter parent, SourceCode source, int start, int end,
			SourceCode.Line statementLine, ArrayList<Token> statementTokens,
			int firstSeparator, int secondSeparator, OtherwiseInterpreter otherwise) {		
		super(parent, source, start, end, statementLine, statementTokens, firstSeparator + 1,
			  secondSeparator - 1, true, false, otherwise);		
		
		this.statementTokens = statementTokens;
		this.secondSeparator = secondSeparator;
		setupInitializer(statementTokens, firstSeparator);
	}
	
	private void setupInitializer(ArrayList<Token> tokens, int firstSeparator) {
		if(firstSeparator > 1) {
			initializerTokens = new ArrayList<Token>();
			for(int index = 1; index < firstSeparator; index++) {
				initializerTokens.add(statementTokens.get(index));
			}
			
			isVariableInitialization = isInitialization(initializerTokens);
			if(isVariableInitialization) {
				ArrayList<Token> typeTokens = getTypeName(initializerTokens, 0);
				initializerType = concatenate(typeTokens);
				initializerName = initializerTokens.get(typeTokens.size()).getToken();

				isNameValid(initializerName, getPredicateLine());
				
				initializerTree = Parser.getParseTree(this, getPredicateLine(), initializerTokens,
					typeTokens.size() + 2);
			} else {
				initializerTree = Parser.getParseTree(this, getPredicateLine(), initializerTokens);
			}
		} else {
			initializerTokens = null;
		}
		
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
		if(incrementTree == null) {
			if(secondSeparator + 1 <= statementTokens.size() - 2) {
				incrementTree = Parser.getParseTree(this, getPredicateLine(), statementTokens,
							secondSeparator + 1, statementTokens.size() - 2);
			}
		}
		
		if(incrementTree != null) {
			incrementTree.operate().getValue();			
		}
	}

	private void doInitialization() {
		if(initializerTokens != null) {
			Variable value = initializerTree.operate().getValue();
			if(isVariableInitialization) {
				doInitialization(initializerType, initializerName, value, getPredicateLine());
			}
		}
	}
	
}
