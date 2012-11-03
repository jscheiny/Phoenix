// ForInterpreter.java
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

package net.scheinerman.phoenix.interpreter;

import java.util.*;

import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

/**
 * Interprets and executes a while loop. A for loop is a loop that runs an intialization before
 * the loop begins, waits for the predicate to become false, and runs an increment clause at the
 * end of every loop. A for loop checks the predicate at the beginning.
 *
 * @author Jonah Scheinerman
 */
public class ForInterpreter extends LoopInterpreter {

	/** Whether the intializer is a variable declaration. */
	private boolean isVariableDeclaration = false;
	
	/** If the initializer is a declaration, contains the type of variable being declared. */
	private String initializerType = null;

	/** If the initializer is a declaration, contains the name of the variable being declared. */
	private String initializerName = null;

	/** Contains the parse tree generated from the initializer clause. */
	private ParseTreeNode initializerTree;

	/** Contains the parse tree generated from the increment clause. */
	private ParseTreeNode incrementTree;
	
	/**
	 * Creates a new for loop interpreter
	 * @param parent the interpreter that is instantiating and running this interpreter
	 * @param start the line on which to start interpreting
	 * @param end the last line to interpret (the line at this index will be interpreted)
	 * @param statementLine the line containing the for statement
	 * @param statementTokens the tokenization of the for statement
	 * @param firstSeparator the index of the first semicolon in the for statement
	 * @param secondSeparator the index of the second semicolon token in the for statment
	 * @param otherwise the interpreter for executing the otherwise block (null if there is none)
	 */
	public ForInterpreter(Interpreter parent, int start, int end, SourceCode.Line statementLine,
			ArrayList<Token> statementTokens, int firstSeparator, int secondSeparator,
			OtherwiseInterpreter otherwise) {		
		super(parent, start, end, statementLine, statementTokens, firstSeparator + 1,
			  secondSeparator - 1, true, false, otherwise);		

		setupInitializer(statementTokens, firstSeparator);
		setupIncrement(statementTokens, secondSeparator);
	}
	
	/**
	 * Builds the initializer parse tree.
	 * @param statementTokens the tokens containing the for statement
	 * @param firstSeparator the index of the first semicolon token in the statement
	 */
	private void setupInitializer(ArrayList<Token> statementTokens, int firstSeparator) {
		if(firstSeparator > 1) {
			ArrayList<Token> initializerTokens = new ArrayList<Token>();
			for(int index = 1; index < firstSeparator; index++) {
				initializerTokens.add(statementTokens.get(index));
			}
			
			isVariableDeclaration = isInitialization(getConfiguration(), initializerTokens);
			if(isVariableDeclaration) {
				ArrayList<Token> typeTokens = getTypeName(getConfiguration(), initializerTokens, 0);
				initializerType = concatenate(typeTokens);
				initializerName = initializerTokens.get(typeTokens.size()).getToken();

				isNameValid(initializerName, getPredicateLine());
				
				initializerTree = Parser.getParseTree(this, getPredicateLine(), initializerTokens,
					typeTokens.size() + 2);
			} else {
				initializerTree = Parser.getParseTree(this, getPredicateLine(), initializerTokens);
			}
		} else {
			initializerTree = null;
		}
		
	}

	/**
	 * Builds the increment parse tree.
	 * @param statementTokens the tokens containing the for statement
	 * @param secondSeparator the index of the second semicolon token in the statement
	 */
	private void setupIncrement(ArrayList<Token> statementTokens, int secondSeparator) {
		if(secondSeparator + 1 <= statementTokens.size() - 2) {
			incrementTree = Parser.getParseTree(this, getPredicateLine(), statementTokens,
					secondSeparator + 1, statementTokens.size() - 2);
		} else {
			incrementTree = null;
		}
	}
	
	/**
	 * Performs the foor loop interpretation. The only difference between this and
	 * {@link LoopInterpreter#interpret()} is that this creates a new stack frame (the loop frame)
	 * an runs the initialization before the loop starts. After the loop ends this then pops the
	 * loop stack frame and returns the end condition returned by 
	 * {@link LoopInterpreter#interpret()}.
	 * @return the condition under which the loop ended (cannot be a break or continue)
	 */
	@Override
	public EndCondition interpret() {
		getVAT().pushStackFrame();
		
		if(initializerTree != null) {
			Variable value = initializerTree.operate().getValue();
			if(isVariableDeclaration) {
				doInitialization(initializerType, initializerName, value, getPredicateLine());
			}
		}
		
		EndCondition endCondition = super.interpret();
		getVAT().popStackFrame();
		return endCondition;
	}
	
	/**
	 * Overridden to execute the increment clause at the end of every loop.
	 */
	@Override
	protected void performAtLoopEnd() {
		if(incrementTree != null) {
			incrementTree.operate().getValue();			
		}
	}	
}
