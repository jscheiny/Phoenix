package net.scheinerman.phoenix.test;

import java.io.*;

import net.scheinerman.phoenix.interpreter.*;

public class Main {

	public static void main(String[] args) {
		try {
			Interpreter interpreter = new Interpreter("phx/test.phx");
			long start = System.currentTimeMillis();
			interpreter.interpret();
			System.out.println("time: " + (System.currentTimeMillis() - start) + " ms");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
