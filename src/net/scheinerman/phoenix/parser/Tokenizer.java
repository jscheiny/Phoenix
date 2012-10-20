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

/**
 * Provides methods to turn strings of code into lists of tokens, separated by delimiters and
 * whitespace. Delimiters are defined by the operators in {@link OperatorNode#OPERATOR_DELIMITERS}
 * and some other symbols such as (, ), [, ], :, and ;.
 *
 * @author Jonah Scheinerman
 */
public class Tokenizer {
	
	/**
	 * Represents a string token which may be a delimiter, or just a regular token. Effectively,
	 * this is a wrapper for a String and a boolean - the token and whether it is a delimiter.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Token {
		
		/** The string token. */
		private String token;

		/** Whether this token is a delimiter. */
		private boolean delimiter = false;
		
		/**
		 * Create a new token that is not a delimiter.
		 * @param token the string token
		 */
		public Token(String token) {
			this(token, false);
		}
		
		/**
		 * Create a new token.
		 * @param token the string token
		 * @param delimiter whether this is a delimiter
		 */
		public Token(String token, boolean delimiter) {
			this.token = token;
			this.delimiter = delimiter;
		}

		/**
		 * Creates a new token from a delimiter.
		 * @param delimiter the delimiter from which to extract the token
		 */
		public Token(Delimiter delimiter) {
			this.token = delimiter.getDelimiter();
			this.delimiter = true;
		}

		/**
		 * Returns the string token.
		 * @return the string token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * Returns whether this token is a delimiter.
		 * @return whether this token is a delimiter
		 */
		public boolean isDelimiter() {
			return delimiter;
		}
		
		@Override
		public String toString() {
			return token;
		}
		
	}

	/**
	 * Represents a delimiter that is used to parse the set of tokens. The comparison between
	 * delimiters sorts first by the length of the delimiter, and then by the delimiter string
	 * ifself. Thus, longer delimiters will be checked first.
	 *
	 * @author Jonah Scheinerman
	 */
	public static class Delimiter implements Comparable<Delimiter> {

		/** The string delimiter symbol. */
		private String delimiter;

		/**
		 * Creates a new delimiter with a given delimiter symbol.
		 * @param delimiter the string delimiter symbol
		 */
		public Delimiter(String delimiter) {
			this.delimiter = delimiter;
		}

		/**
		 * Returns the delimiter symbol.
		 * @return the delimiter symbol
		 */
		public String getDelimiter() {
			return delimiter;
		}
		
		/**
		 * Returns the length of the delimiter symbol.
		 * @return the length of the delimiter symbol
		 */
		public int length() {
			return delimiter.length();
		}
		
		/**
		 * Returns whether the given delimiter occurs in the phrase at the given starting index.
		 * @param phrase the phrase to search in
		 * @param startIndex the index at which to check for the delimiter
		 * @return true if the phrase has the delimiter at the given starting index
		 */
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
		
	/** The sorted set of delimiters that will be used to tokenize the string. */
	private static final TreeSet<Delimiter> DELIMITERS = new TreeSet<Delimiter>();
	static {
		DELIMITERS.add(new Delimiter("("));
		DELIMITERS.add(new Delimiter(")"));
		DELIMITERS.add(new Delimiter("["));
		DELIMITERS.add(new Delimiter("]"));
		DELIMITERS.add(new Delimiter(":"));
		DELIMITERS.add(new Delimiter(";"));
		for(String operator : OperatorNode.OPERATOR_DELIMITERS) {
			DELIMITERS.add(new Delimiter(operator));
		}
	}
	
	/** 
	 * Separates a string into an array of tokens. This separates the string based on three
	 * different factors: string literals, whitespace, and delimiters (found in
	 * {@link Tokenizer#DELIMITERS}). Whitespace tokens are not returned. String literals,
	 * delimiters, and any in between tokens will be returned.
	 * @param phrase the phrase to tokenize
	 * @param source the source line from which this phrase was generated.
	 * @return the tokenized representation of the phrase
	 */
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
