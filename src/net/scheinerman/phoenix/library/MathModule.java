package net.scheinerman.phoenix.library;

import java.util.*;

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

public class MathModule extends BuiltinModule {

	public static class Pi extends BuiltinFunction {
		public Pi(Interpreter interpreter) {
			super(interpreter, "PI");
			setReturnType("double");
		}

		@Override
		public Variable call(Map<String, Variable> parameters) {
			return new DoubleVariable(Math.PI);
		}
	}
	
	public static class Sin extends BuiltinFunction {
		public Sin(Interpreter interpreter) {
			super(interpreter, "sin");
			setRightArgs("double", "x");
			setReturnType("double");
		}

		@Override
		public Variable call(Map<String, Variable> parameters) {
			DoubleVariable param = (DoubleVariable) parameters.get("x");
			return new DoubleVariable(Math.sin(param.getValue()));
		}
	}
	
	public static class Cos extends BuiltinFunction {
		public Cos(Interpreter interpreter) {
			super(interpreter, "cos");
			setRightArgs("double", "x");
			setReturnType("double");
		}

		@Override
		public Variable call(Map<String, Variable> parameters) {
			DoubleVariable param = (DoubleVariable) parameters.get("x");
			return new DoubleVariable(Math.cos(param.getValue()));
		}
	}
	
	public static class Factorial extends BuiltinFunction {
		public Factorial(Interpreter interpreter) {
			super(interpreter, "factorial");
			setLeftArgs("int", "x");
			setReturnType("long");
		}

		@Override
		public Variable call(Map<String, Variable> parameters) {
			IntegerVariable param = (IntegerVariable) parameters.get("x");
			long prod = 1;
			for(int n = 2; n <= param.getValue(); n++) {
				prod *= n;
			}
			return new LongVariable(prod);
		}
	}
}
