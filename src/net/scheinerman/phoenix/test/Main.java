package net.scheinerman.phoenix.test;

import java.io.*;

import net.scheinerman.phoenix.interpreter.*;

public class Main {

	public static void main(String[] args) {
		try {
			Interpreter interpreter = new Interpreter("phx/test.phx");
			interpreter.interpret();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
