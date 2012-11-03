package net.scheinerman.phoenix.library;

import java.lang.reflect.*;
import java.util.*;

import net.scheinerman.phoenix.interpreter.*;
import net.scheinerman.phoenix.variables.*;

public class BuiltinModule {
	
	private ArrayList<Constructor<BuiltinFunction>> constructors
		= new ArrayList<Constructor<BuiltinFunction>>();
	
	@SuppressWarnings("unchecked")
	public BuiltinModule() {
		Class<?>[] classes = getClass().getDeclaredClasses();
		for(Class<?> c : classes) {
			if(c.getSuperclass() == BuiltinFunction.class) {
				Constructor<BuiltinFunction> constructor = null;
				try {
					constructor = (Constructor<BuiltinFunction>)c.getConstructor(Interpreter.class);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException("BuiltinFunctions in the module must have a " +
						"constructor from an interpreter.");
				}
				
				if(constructor != null) {
					constructors.add(constructor);
				}
			}
		}
	}
	
	public final void insertFunctionsInto(Interpreter interpreter) {
		for(Constructor<BuiltinFunction> constructor : constructors) {
			BuiltinFunction function = null;
			try {
				function = constructor.newInstance(interpreter);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			if(function != null) {
				Variable value = function.getFunctionVariable();
				interpreter.getVAT().allocateGlobal(function.getName(), value);
			}
		}
	}
}
