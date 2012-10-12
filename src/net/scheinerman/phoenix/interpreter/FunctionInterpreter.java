package net.scheinerman.phoenix.interpreter;

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.variables.*;

import com.sun.tools.javac.util.*;

public class FunctionInterpreter extends Interpreter {

	private VariableAllocationTable parentVAT;
	private String returnType;
	private String name;
	private ArrayList<Pair<String, String>> leftArgs;
	private ArrayList<Pair<String, String>> rightArgs; 
	
	
	public FunctionInterpreter(Interpreter parent, SourceCode source, int start, int end,
			String returnType, String name, ArrayList<Pair<String, String>> leftArgs,
			ArrayList<Pair<String, String>> rightArgs) {
		super(parent, source, start, end);
		parentVAT = parent.getVAT();

		this.returnType = returnType;
		this.name = name;
		this.leftArgs = leftArgs;
		this.rightArgs = rightArgs;
	}
	
	public String getDefinition() {
		String def = returnType + " ";
		if(leftArgs != null && leftArgs.size() != 0) {
			def += "(";
			for(int index = 0; index < leftArgs.size(); index++) {
				Pair<String, String> pair = leftArgs.get(index);
				def += pair.fst + " " + pair.snd;
				if(index != leftArgs.size() - 1) {
					def += ", ";
				}
			}
			def += ") ";
		}
		def += name;
		if(rightArgs != null && rightArgs.size() != 0) {
			def += " (";
			for(int index = 0; index < rightArgs.size(); index++) {
				Pair<String, String> pair = rightArgs.get(index);
				def += pair.fst + " " + pair.snd;
				if(index != rightArgs.size() - 1) {
					def += ", ";
				}
			}
			def += ")";
		}
		
		return def;
	}

	public void interpretWithVAT(VariableAllocationTable vat) {
		VariableAllocationTable previousVAT = getVAT();
		interpret();
		setVAT(previousVAT);
	}
	
	private void assignArguments(VariableAllocationTable vat, Variable passed,
			ArrayList<Pair<String, String>> declared) {
		if(passed == null) return;
		if(!(passed instanceof TupleVariable)) {
			Pair<String, String> typeNamePair = declared.get(0);
			vat.allocate(typeNamePair.snd, passed);
		} else {
			TupleVariable tuple = (TupleVariable)passed;
			for(int index = 0; index < tuple.size(); index++) {
				Pair<String, String> typeNamePair = declared.get(index);
				Variable value = tuple.getElement(index);
				vat.allocate(typeNamePair.snd, value);
			}
		}
	}
	
	private void checkArguments(Variable callee, Variable left, Variable right,
			Variable passed, ArrayList<Pair<String, String>> declared) {
		if(declared == null || declared.size() == 0) {
		   if(passed != null) {
			   	throw new InvalidCallParametersException(callee, left, right, null);
		   }
		} else if(declared.size() == 1) {
			if(passed == null) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}
			if(passed instanceof TupleVariable) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}
			if(!declared.get(0).fst.equals(passed.getTypeName())) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}			
		} else {
			if(passed == null) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}
			if(!(passed instanceof TupleVariable)) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}
			TupleVariable tuple = (TupleVariable)passed;
			if(tuple.size() != declared.size()) {
				throw new InvalidCallParametersException(callee, left, right, null);
			}
			for(int index = 0; index < declared.size(); index++) {
				if(!tuple.getElement(index).getTypeName().equals(declared.get(index).fst)) {
					throw new InvalidCallParametersException(callee, left, right, null);
				}
			}
		}
	}
	
	public void call(Variable callee, Variable left, Variable right) {
		checkArguments(callee, left, right, left, leftArgs);
		checkArguments(callee, left, right, right, rightArgs);
		VariableAllocationTable currVAT = getVAT();
		VariableAllocationTable executionVAT = parentVAT.getGlobal();
		executionVAT.pushStackFrame();
		setVAT(executionVAT);
		assignArguments(executionVAT, left, leftArgs);
		assignArguments(executionVAT, right, rightArgs);
		interpret();
		executionVAT.popStackFrame();
		setVAT(currVAT);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Pair<String, String>> getLeftArgs() {
		return leftArgs;
	}
	
	public ArrayList<Pair<String, String>> getRightArgs() {
		return rightArgs;
	}

	public String getReturnType() {
		return returnType;
	}
	
	@Override
	protected void handleEndCondition(EndCondition endCondition) {
		if(endCondition == EndCondition.NORMAL) {
			return;
		}
		
		if(endCondition == EndCondition.BREAK) {
			throw new SyntaxException("Cannot have " + Strings.BREAK +
				" statement outside of a loop.", getEndConditionLine());
		}
		
		if(endCondition == EndCondition.CONTINUE) {
			throw new SyntaxException("Cannot have " + Strings.CONTINUE + 
				" statement outside of a loop.", getEndConditionLine());
		}
		
		if(endCondition == EndCondition.RETURN) {
			if(getReturnType().equals(Strings.VOID) && getReturnVariable() != null) {
				throw new SyntaxException("Void function cannot return a value.",
					getEndConditionLine());
			}
			
			if(!getReturnType().equals(getReturnVariable().getTypeName())) {
				throw new SyntaxException("Function expected to return " + getReturnType() +
					" but returned " + getReturnVariable().getTypeName(), getEndConditionLine());
			}
		}
	}
	
}
