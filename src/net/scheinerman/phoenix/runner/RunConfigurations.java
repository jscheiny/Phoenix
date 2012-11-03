// RunConfigurations.java
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

package net.scheinerman.phoenix.runner;

import java.io.*;
import java.util.*;

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.interpreter.SourceCode.Line;
import net.scheinerman.phoenix.library.*;
import net.scheinerman.phoenix.variables.*;
import net.scheinerman.phoenix.variables.Variable.TypeDefinition;

public class RunConfigurations {

	public static class Factory {
		
		public static RunConfigurations createWithStandardConfigurations() {
			return new Builder().standardConfigurations().build();
		}

	}
	
	public static class Builder {
		
		private ArrayList<TypeDefinition<? extends Variable>> types
			= new ArrayList<TypeDefinition<? extends Variable>>();

		private ArrayList<BuiltinModule> modules = new ArrayList<BuiltinModule>();

		private PrintStream outputStream = System.out;
		private PrintStream errorStream = System.err;
		private InputStream inputStream = System.in;

		public Builder() {
			
		}
		
		public Builder standardConfigurations() {
			addStandardTypes();
			addStandardModules();
			setStandardIO();
			return this;
		}
		
		public Builder addTypeDefinition(TypeDefinition<? extends Variable> definition) {
			types.add(definition);
			return this;
		}
		
		public Builder addStandardTypes() {
			types.add(new BooleanVariable.Definition());
			types.add(new DoubleVariable.Definition());
			types.add(new FunctionVariable.Definition());
			types.add(new IntegerVariable.Definition());
			types.add(new LongVariable.Definition());
			types.add(new StringVariable.Definition());
			types.add(new TupleVariable.Definition());
			types.add(new TypeVariable.Definition());
			types.add(new VoidVariable.Definition());
			return this;
		}
		
		public Builder addModule(BuiltinModule module) {
			modules.add(module);
			return this;
		}
		
		public Builder addStandardModules() {
			modules.add(new MathModule());
			return this;
		}

		public Builder setOutputStream(PrintStream outputStream) {
			this.outputStream = outputStream;
			return this;
		}

		public Builder setErrorStream(PrintStream errorStream) {
			this.errorStream = errorStream;
			return this;
		}

		public Builder setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}
		
		public Builder setIO(PrintStream outputStream,
				 			 PrintStream errorStream,
				 			 InputStream inputStream) {
			this.outputStream = outputStream;
			this.errorStream = errorStream;
			this.inputStream = inputStream;
			return this;
		}
		
		public Builder setStandardIO() {
			this.outputStream = System.out;
			this.errorStream = System.err;
			this.inputStream = System.in;
			return this;
		}
		
		public RunConfigurations build() {
			return new RunConfigurations(types, modules, outputStream, errorStream, inputStream);
		}
		
	}
	
	private ArrayList<TypeDefinition<? extends Variable>> types
		= new ArrayList<TypeDefinition<? extends Variable>>();
	
	private HashMap<String, TypeDefinition<? extends Variable>> typeNames
		= new HashMap<String, TypeDefinition<? extends Variable>>();
	
	private ArrayList<BuiltinModule> modules = new ArrayList<BuiltinModule>();
	
	private PrintStream outputStream = System.out;
	private PrintStream errorStream = System.err;
	private InputStream inputStream = System.in;
	
	public RunConfigurations(ArrayList<TypeDefinition<? extends Variable>> types,
			ArrayList<BuiltinModule> modules,
			PrintStream outputStream,
			PrintStream errorStream,
			InputStream inputStream) {
		for(TypeDefinition<? extends Variable> def : types) {
			addTypeDefinition(def);
		}
		
		this.modules = modules;
		
		this.outputStream = outputStream;
		this.errorStream = errorStream;
		this.inputStream = inputStream;
	}
	
	public void insertModules(Interpreter interpreter) {
		for(BuiltinModule module : modules) {
			module.insertFunctionsInto(interpreter);
		}
	}
	
	private void addTypeDefinition(TypeDefinition<? extends Variable> definition) {
		types.add(definition);
		if(definition.getTypeName() != null) {
			typeNames.put(definition.getTypeName(), definition);
		}
	}

	public Variable createVariableFromLiteral(Interpreter interpreter, String literal,
			Line source) {
		for(TypeDefinition<? extends Variable> definition : types) {
			Variable value = definition.createFromLiteral(interpreter, literal, source);
			if(value != null) {
				return value;
			}
		}
		return null;
	}
	
	public Variable createDefaultVariable(Interpreter interpreter, String typeName) {
		if(typeNames.containsKey(typeName)) {
			return typeNames.get(typeName).createDefaultVariable(interpreter);
		}
		return null;
	}
	
	public boolean isTypeName(String name) {
		return typeNames.containsKey(name);
	}
	
	public PrintStream getOutputStream() {
		return outputStream;
	}

	public PrintStream getErrorStream() {
		return errorStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
}
