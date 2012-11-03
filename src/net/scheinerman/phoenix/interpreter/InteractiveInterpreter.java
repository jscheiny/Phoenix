// InteractiveInterpreter.java
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

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.library.*;
import net.scheinerman.phoenix.runner.*;
import net.scheinerman.phoenix.variables.*;

public class InteractiveInterpreter extends Interpreter {

	public static class InteractiveModule extends BuiltinModule {
		
		public static class About extends BuiltinFunction {
			public About(Interpreter interpreter) {
				super(interpreter, "about");
				setReturnType("void");
			}

			@Override
			public Variable call(Map<String, Variable> parameters) {
				Scanner sc = new Scanner(getClass().getResourceAsStream("about.txt"));
				while(sc.hasNextLine()) {
					System.out.println("    " + sc.nextLine());
				}
				return null;
			}
		}
		
		public static class License extends BuiltinFunction {
			public License(Interpreter interpreter) {
				super(interpreter, "license");
				setReturnType("void");
			}

			@Override
			public Variable call(Map<String, Variable> parameters) {
				Scanner sc = new Scanner(getClass().getResourceAsStream("license.txt"));
				while(sc.hasNextLine()) {
					System.out.println("    " + sc.nextLine());
				}
				return null;
			}
		}
		
		public static class Copyright extends BuiltinFunction {
			public Copyright(Interpreter interpreter) {
				super(interpreter, "copyright");
				setReturnType("void");
			}

			@Override
			public Variable call(Map<String, Variable> parameters) {
				Scanner sc = new Scanner(getClass().getResourceAsStream("copyright.txt"));
				while(sc.hasNextLine()) {
					System.out.println("    " + sc.nextLine());
				}
				return null;
			}
		}
	}
	
	private static class InteractiveSourceCode extends SourceCode {
		private static class InteractiveLine extends Line {
			public InteractiveLine(SourceCode source, String line, int lineNumber) {
				super(source, line, lineNumber);
			}
			
			public String getLocationString() {
				return "(sys.in:" + (getNumber() + 1) + ")";
			}
		}
		
		public InteractiveSourceCode() {
		}

		public void addCode(List<String> lines) {
			for(String line : lines) {
				addCodeLine(line);
			}
		}
		
		private void addCodeLine(String line) {
			Line codeLine = new InteractiveLine(this, line, code.size());
			code.add(codeLine);
		}
	}
	
	private InteractiveSourceCode interactiveSource;
	
	private int lastLineWithValue = -1;
	private Variable lastValue = null;
	
	public InteractiveInterpreter() {
		this(new RunConfigurations.Builder()
			.standardConfigurations()
			.addModule(new InteractiveModule())
			.build());
	}
	
	public InteractiveInterpreter(RunConfigurations config) {
		super(new InteractiveSourceCode(), config);
		interactiveSource = (InteractiveSourceCode) getSourceCode();
		
		getVAT().pushStackFrame();
	}
	
	public void run(List<String> lines) {
		int start = interactiveSource.size();
		interactiveSource.addCode(lines);
		int end = interactiveSource.size() - 1;
		
		setStartLine(start);
		setEndLine(end);
		
		interpret();
	}
	
	public EndCondition interpret() {
		returnVariable = null;
		
		try {			
			for(int index = getStartLine(); index <= getEndLine(); index++) {
				Line line = getSourceCode().line(index);
				if(line.isEmpty())
					continue;

				Statement statement = line.getStatement();
				
				// If the current line is indented more than the previous line, and we are not at
				// the start of the interpretation, then this is an indentation error.
				if(index != getStartLine() &&
				   line.indentGreaterThan(getSourceCode().line(index - 1).getIndent())) {
					throw new IndentException("Unexpected indented block", line);
				}	
	
				if(statement == Statement.UNDEFINED) {
					try {
						setupUndefinedStatement(line, index);
					} catch(PhoenixRuntimeException exception) {
						line.setSetupException(exception);
					}
				}
				
				index = handleDefinedStatement(line, index);
			}
			
			handleEndCondition(endCondition);

		} catch(PhoenixRuntimeException phoenixException) {
			phoenixException.printPhoenixTrace(getConfiguration());
		} catch(Exception exception) {
			getConfiguration().getErrorStream().println(
				"An internal error has occurred. Please send the following output\n"+
				"and the code that produced this error to jonah@scheinerman.net");
			exception.printStackTrace(getConfiguration().getErrorStream());
			System.exit(1);
		}

		return endCondition;
	}
	
	@Override
	protected Variable handleParse(Line line) {
		Variable value = super.handleParse(line);
		lastValue = value;
		lastLineWithValue = line.getNumber();
		return value;
	}
	
	public Variable getLastComputedValue() {
		if(lastLineWithValue == getEndLine()) {
			return (lastValue == null || lastValue instanceof VoidVariable ? null : lastValue);
		}
		return null;
	}
	

}
