// Tokenizer.java
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

package net.scheinerman.phoenix.parser;

import java.util.*;

import net.scheinerman.phoenix.exceptions.*;
import net.scheinerman.phoenix.interpreter.*;

public class Tokenizer {
	
	public static class Token {
		
		private String token;
		private boolean delimiter;
		
		public Token(String token) {
			this(token, false);
		}
		
		public Token(String token, boolean delimiter) {
			this.token = token;
			this.delimiter = delimiter;
		}

		public Token(Delimiter delimiter) {
			this.token = delimiter.getDelimiter();
			this.delimiter = true;
		}
		
		public String getToken() {
			return token;
		}

		public boolean isDelimiter() {
			return delimiter;
		}
		
		@Override
		public String toString() {
			return token;
		}
		
	}

	public static class Delimiter implements Comparable<Delimiter> {
		private String delimiter;

		public Delimiter(String delimiter) {
			this.delimiter = delimiter;
		}

		public String getDelimiter() {
			return delimiter;
		}
		
		public int length() {
			return delimiter.length();
		}
		
		public boolean occursAt(String phrase, int startIndex) {
			for(int delimiterIndex = 0; delimiterIndex < delimiter.length(); delimiterIndex++) {
				if(delimiterIndex + startIndex >= phrase.length()) {
					return false;
				}
				if(delimiter.charAt(delimiterIndex) != phrase.charAt(delimiterIndex + startIndex)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int compareTo(Delimiter o) {
			if(delimiter.length() != o.getDelimiter().length())
				return -new Integer(delimiter.length()).compareTo(o.getDelimiter().length());
			return delimiter.compareTo(o.getDelimiter());
		}
		
		@Override
		public String toString() {
			return delimiter;
		}
	}
		
	private static final TreeSet<Delimiter> DELIMITERS = new TreeSet<Delimiter>();
	static {
		DELIMITERS.add(new Delimiter("("));
		DELIMITERS.add(new Delimiter(")"));
		DELIMITERS.add(new Delimiter("["));
		DELIMITERS.add(new Delimiter("]"));
		DELIMITERS.add(new Delimiter(":"));
		DELIMITERS.add(new Delimiter(";"));
		for(String operator : Interpreter.Strings.OPERATORS) {
			DELIMITERS.add(new Delimiter(operator));
		}
	}
	
	public static ArrayList<Token> tokenize(String phrase, SourceCode.Line source) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		
		int lastToken = 0;
		int index;
		for(index = 0; index < phrase.length(); index++) {
			if(phrase.charAt(index) == '"') {
				if(index > lastToken) {
					String sub = phrase.substring(lastToken, index).trim();
					if(!sub.isEmpty())
						tokens.add(new Token(sub));
				}

				int endIndex = phrase.indexOf('"', index + 1);
				while( endIndex != -1 && phrase.charAt(endIndex - 1) == '\\' ) {
					endIndex = phrase.indexOf('"', endIndex + 1);
				}
				if(endIndex == -1) {
					throw new SyntaxException("String literal with no close quote.", source);
				}
				tokens.add(new Token(phrase.substring(index, endIndex + 1)));
				index = endIndex;
				lastToken = index+1;
				continue;
			}
			
			if(phrase.charAt(index) == '\'') {
				if(index > lastToken) {
					String sub = phrase.substring(lastToken, index).trim();
					if(!sub.isEmpty())
						tokens.add(new Token(sub));
				}

				int endIndex = phrase.indexOf('\'', index + 1);
				while( endIndex != -1 && phrase.charAt(endIndex - 1) == '\\' ) {
					endIndex = phrase.indexOf('\'', endIndex + 1);
				}
				if(endIndex == -1) {
					throw new SyntaxException("String literal with no close quote.", source);
				}
				tokens.add(new Token(phrase.substring(index, endIndex + 1)));
				index = endIndex;
				lastToken = index+1;
				continue;
			}
			
			if(Character.isWhitespace(phrase.charAt(index))) {
				if(index > lastToken) {
					String sub = phrase.substring(lastToken, index).trim();
					if(!sub.isEmpty())
						tokens.add(new Token(sub));
				}
				int endIndex;
				for(endIndex = index+1; endIndex < phrase.length(); endIndex++) {
					if(!Character.isWhitespace(phrase.charAt(endIndex)))
						break;
				}
				index = endIndex - 1;
				lastToken = endIndex;
			}
			
			for(Delimiter delimiter : DELIMITERS) {
				if(delimiter.occursAt(phrase, index)) {
					if(index > lastToken) {
						String sub = phrase.substring(lastToken, index).trim();
						if(!sub.isEmpty())
							tokens.add(new Token(sub));
					}
					tokens.add(new Token(delimiter));
					index += delimiter.length() - 1;
					lastToken = index + 1;
					break;
				}
			}
		}
		
		if(index > lastToken) {
			String sub = phrase.substring(lastToken, index).trim();
			if(!sub.isEmpty())
				tokens.add(new Token(sub));
		}
		
		return tokens;
	}
}
