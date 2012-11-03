// Main.java
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
import net.scheinerman.phoenix.variables.*;

public class Main {

	public static void main(String[] args) {
		if(args.length == 0) {
			interactive();
		} else {
			String path = args[0];
			try {
				Interpreter interpreter = new Interpreter(path);
				interpreter.interpret();
			} catch (FileNotFoundException e) {
				System.err.println("Could not read file '" + path + "'.");
				System.exit(1);
			}
		}
	}
	
	private static void interactive() {
		System.out.println("Phoenix: Basilisk (v.2.0.1 Beta) Interactive Shell");
		System.out.println("Type \"about\", \"license\", or \"copyright\" for additional information or \"exit\" to quit.");
		InteractiveInterpreter interpreter = new InteractiveInterpreter();
		
		Scanner scanner = new Scanner(System.in);
		while(true) {
			
			System.out.print(">>> ");
			String line = scanner.nextLine();
			List<String> lines = new LinkedList<String>();
			lines.add(line);

			if(line.trim().endsWith(":")) {
				do {
					System.out.print("... ");
					line = scanner.nextLine();
					lines.add(line);
				} while(!line.trim().equals(""));
			}
			
			interpreter.run(lines);
			Variable value = interpreter.getLastComputedValue();
			if(value != null) {
				System.out.println("  = " + value.stringValue());
			}
		}
	}

}
