// SourceCode.java
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

import java.io.*;
import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.Interpreter.*;
import net.scheinerman.phoenix.parser.*;
import net.scheinerman.phoenix.parser.Tokenizer.Token;

public class SourceCode {

	public static class Line {
		public static String extractIndent(String line) {
			int index;
			for(index = 0; index < line.length(); index++) {
				if(!Character.isWhitespace(line.charAt(index))) {
					break;
				}
			}
			return line.substring(0, index);
		}
		
		public static String stripComments(String line) {
			boolean inQuote = false;
			for(int index = 0; index < line.length(); index++) {
				char current = line.charAt(index);
				if(current == '"') {
					inQuote = !inQuote;
					continue;
				}
				if(index < line.length() - 1 &&
				   line.substring(index, index + 2).equals("\\\"") &&
				   inQuote) {
					index++;
					continue;
				}
				if(!inQuote &&
				   line.substring(index).startsWith(Interpreter.Strings.COMMENT_START)) {
					return line.substring(0, index);
				}
			}
			return line;
		}
		
		private SourceCode source;
		private String line;
		private int lineNumber;
		private String indent;
		private String content;
		private boolean empty;
		private ArrayList<Token> tokenization = null;
		private Statement statement = Statement.UNDEFINED;
		private Object data;
		private int continuationLineIndex;
		private PhoenixRuntimeException setupException;

		public Line(SourceCode source, String line, int lineNumber) {
			this.source = source;
			this.line = line;
			this.lineNumber = lineNumber;
			indent = extractIndent(line);
			content = stripComments(line).trim();
			empty = content.isEmpty();
		}
		
		public ArrayList<Token> tokenize() {
			if(tokenization != null) {
				return tokenization;
			}
			tokenization = Tokenizer.tokenize(getLineContent(), this);
			return tokenization;
		}
		
		public Statement getStatement() {
			return statement;
		}
		
		public void setStatment(Statement statement) {
			this.statement = statement;
		}
		
		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
		
		public int getContinuationLineIndex() {
			return continuationLineIndex;
		}

		public void setContinuationLineIndex(int continuationLineIndex) {
			this.continuationLineIndex = continuationLineIndex;
		}

		public PhoenixRuntimeException getSetupException() {
			return setupException;
		}

		public void setSetupException(PhoenixRuntimeException setupException) {
			this.setupException = setupException;
		}	

		public String getUnchangedLine() {
			return line;
		}
		
		public String getLineContent() {
			return content;
		}
		
		public String getIndent() {
			return indent;
		}
		
		public int getNumber() {
			return lineNumber;
		}
		
		public boolean isEmpty() {
			return empty;
		}
		
		public SourceCode getSourceCode() {
			return source;
		}
		
		public boolean indentAtLeast(String baseIndent) {
			return getIndent().startsWith(baseIndent);
		}
		
		public boolean indentGreaterThan(String baseIndent) {
			return indentAtLeast(baseIndent) && !getIndent().equals(baseIndent);
		}
		
		public String toString() {
			return getLineContent() + " " + getLocationString();
		}
		
		public String getLocationString() {
			return "(" + source.getFile().getPath() + ":" + (lineNumber + 1) + ")";
		}
	}
	
	private File file;
	private ArrayList<Line> code = new ArrayList<Line>();
	
	public SourceCode(String path) throws FileNotFoundException {
		this(new File(path));
	}
	
	public SourceCode(File file) throws FileNotFoundException {
		this.file = file;
		Scanner scanner = new Scanner(file);
		int index = 0;
		while(scanner.hasNextLine()) {
			code.add(new Line(this, scanner.nextLine(), index));
			index++;
		}
		code.add(new Line(this, "", index));
	}
	
	public Line line(int index) {
		return code.get(index);
	}
	
	public int size() {
		return code.size();
	}
	
	public File getFile() {
		return file;
	}
	
	public int getBlockEnd(int start) {
		String baseIndent = line(start).getIndent();
		for(int index = start + 1; index < size(); index++) {
			if(!line(index).indentGreaterThan(baseIndent)) {
				return index - 1;
			}
		}
		return size() - 1;
	}	
}
