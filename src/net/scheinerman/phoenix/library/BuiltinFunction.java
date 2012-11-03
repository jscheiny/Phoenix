package net.scheinerman.phoenix.library;

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;
import net.scheinerman.phoenix.variables.*;

import com.sun.tools.javac.util.*;

public abstract class BuiltinFunction extends FunctionInterpreter {
	
	public BuiltinFunction(Interpreter interpreter, String name) {
		super(interpreter, -1, -1, null, name, null, null);
		setReturnType("void");
	}
	
	public final BuiltinFunction setLeftArgs(String ... args) {
		leftArgs = createArgsList(args);
		return this;
	}
	
	public final BuiltinFunction setRightArgs(String ... args) {
		rightArgs = createArgsList(args);
		return this;
	}
	
	private ArrayList<Pair<String, String>> createArgsList(String[] args) {
		if(args.length % 2 == 1)
			throw new IllegalArgumentException("There must be an even number of arguments.");
		
		ArrayList<Pair<String, String>> argsList = new ArrayList<Pair<String,String>>();
		for(int index = 0; index < args.length; index += 2) {
			String type = args[index];
			ArrayList<Token> typeTokens = null;
			try {
				typeTokens = Tokenizer.tokenize(type, null);
			} catch(PhoenixRuntimeException e) {
				throw new IllegalArgumentException("Type name " + type + " is not tokenizable.");
			}
			ArrayList<Token> typeNameTokens = getTypeName(getConfiguration(), typeTokens, 0);
			if(typeNameTokens == null) {
				throw new IllegalArgumentException("Type name " + type + " is not valid.");
			}
			if(typeNameTokens.size() != typeTokens.size()) {
				throw new IllegalArgumentException("Type name " + type + " is not valid.");				
			}
			
			String name = args[index + 1];
			try {
				isNameValid(name, null);
			} catch(PhoenixRuntimeException e) {
				throw new IllegalArgumentException("Parameter name " + name + " is not valid.");
			}

			argsList.add(new Pair<String, String>(type, name));
		}
		
		return argsList;
	}
	
	public final BuiltinFunction setReturnType(String returnType) {
		this.returnType = returnType;
		if(returnType.equals(Interpreter.Strings.VOID)) {
			return this;
		}
		ArrayList<Token> tokens = null;
		try {
			tokens = Tokenizer.tokenize(returnType, null);
		} catch(PhoenixRuntimeException e) {
			throw new IllegalArgumentException("Type name " + returnType + " is not tokenizable.");
		}
		
		ArrayList<Token> typeNameTokens = getTypeName(getConfiguration(), tokens, 0);
		if(typeNameTokens == null) {
			throw new IllegalArgumentException("Type name " + returnType + " is not valid.");
		}
		if(typeNameTokens.size() != tokens.size()) {
			throw new IllegalArgumentException("Type name " + returnType + " is not valid.");				
		}
		
		return this;
	}
	
	private Map<String, Variable> createParameterMap(Variable left, Variable right) {
		Map<String, Variable> paramMap = new HashMap<String, Variable>();
		
		if(left != null) {
			if(left instanceof TupleVariable) {
				for(int index = 0; index < leftArgs.size(); index++) {
					Pair<String, String> typeNamePair = leftArgs.get(index);
					Variable value = ((TupleVariable) left).getElement(index);
					
					paramMap.put(typeNamePair.snd, value);
				}
			} else {
				Pair<String, String> typeNamePair = leftArgs.get(0);
				paramMap.put(typeNamePair.snd, left);
			}
		}
		
		if(right != null) {
			if(right instanceof TupleVariable) {
				for(int index = 0; index < rightArgs.size(); index++) {
					Pair<String, String> typeNamePair = rightArgs.get(index);
					Variable value = ((TupleVariable) right).getElement(index);
					
					paramMap.put(typeNamePair.snd, value);
				}
			} else {
				Pair<String, String> typeNamePair = rightArgs.get(0);
				paramMap.put(typeNamePair.snd, right);
			}
		}
		
		
		return paramMap;
	}
	
	@Override
	public final void call(Variable callee, Variable left, Variable right) {
		if(!checkArguments(left, leftArgs) || !checkArguments(right, rightArgs)) {
			throw new InvalidCallParametersException(callee, left, right, null);
		}
		
		returnVariable = call(createParameterMap(left, right));
	}
	
	public abstract Variable call(Map<String, Variable> parameters);
	
	public FunctionVariable getFunctionVariable() {
		return new FunctionVariable(this);
	}
	
}
