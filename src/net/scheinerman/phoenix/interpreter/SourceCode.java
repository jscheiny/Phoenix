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

/**
 * Contains the Phoenix source code for a single Phoenix source file. Each line is represnted by an
 * an instances of the {@link Line} class.
 *
 * @author Jonah Scheinerman
 */
public class SourceCode {

	/**
	 * A line of code. This class automatically extracts the indent from a line and strips it of
	 * comments, and will tokenize the line when needed. Additionally, various data can be stored
	 * with the line so that a line can be interpreted once, and then executed in the future more
	 * quickly using the stored data in the line from the initial interpretation.
	 *
	 * @author Jonah Scheinerman 
	 */
	public static class Line {
		/**
		 * Extracts and returns the leading whitespace of the line (the indent).
		 * @param line the line from which to extract the indent
		 * @return the leading whitespace of the line
		 */
		public static String extractIndent(String line) {
			int index;
			for(index = 0; index < line.length(); index++) {
				if(!Character.isWhitespace(line.charAt(index))) {
					break;
				}
			}
			return line.substring(0, index);
		}
		
		/**
		 * Strips the comments from the line, and returns the stripped version of the line.
		 * @param line the line from which to stirp comments
		 * @return the line stripped of comments
		 */
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
		
		/** The source code containing this line. */
		private SourceCode source;

		/** The original line content. */
		private String line;

		/** The line number of this line (0 based). */
		private int lineNumber;

		/** The indent of the line, the whitespace before the first non-whitespace character. */
		private String indent;

		/** The content of the line, stripped of comments and trailing and leading whitespace. */
		private String content;

		/** Whether this line has no content. */
		private boolean empty;

		/** The tokenization of this line's content. */
		private ArrayList<Token> tokenization = null;

		/** The statement type of this line, initialy undefined. */
		private Statement statement = Statement.UNDEFINED;

		/** Any metadata to be stored with this line to help execute it.*/
		private Object data;

		/** The next line that should be executed after executing this one. */
		private int continuationLineIndex;

		/** Any exception that was thrown when attempting to parse this line the first time. */
		private PhoenixRuntimeException setupException;

		/**
		 * Create a new source code line.
		 * @param source the source code containing this line
		 * @param line the original line content
		 * @param lineNumber the index of the line (0 based)
		 */
		public Line(SourceCode source, String line, int lineNumber) {
			this.source = source;
			this.line = line;
			this.lineNumber = lineNumber;
			indent = extractIndent(line);
			content = stripComments(line).trim();
			empty = content.isEmpty();
		}
		
		/**
		 * Returns the tokenized version of the content of this string. See {@link Tokenizer} for
		 * information on how this works.
		 * @return the tokenized content of this string
		 */
		public ArrayList<Token> tokenize() {
			if(tokenization == null) {
				tokenization = Tokenizer.tokenize(getLineContent(), this);
			}
			return tokenization;
		}
		
		/**
		 * Returns the statement type of this line. If this line has not been processed, then this
		 * will have type {@link Statement#UNDEFINED}.
		 * @return the statement type of this line
		 */
		public Statement getStatement() {
			return statement;
		}

		/**
		 * Set the statement type of this line. This is to be used to set up a line for quicker 
		 * execution in the future.
		 * @param statement the statement type for this line
		 */		
		public void setStatment(Statement statement) {
			this.statement = statement;
		}
		
		/**
		 * Returns the object data associated with this line.
		 * @return the object data associated with this line
		 */
		public Object getData() {
			return data;
		}

		/**
		 * Sets the object data associated with this line.
		 * @param data the object data to be associated with this line
		 */
		public void setData(Object data) {
			this.data = data;
		}
		
		/**
		 * Returns the line index on which execution should continue after executing this line.
		 * @return the next line to execute after this one
		 */
		public int getContinuationLineIndex() {
			return continuationLineIndex;
		}

		/**
		 * Sets the line index on which execution should continue after executing this line.
		 * @param continuationLineIndex the next line to execute after this one
		 */
		public void setContinuationLineIndex(int continuationLineIndex) {
			this.continuationLineIndex = continuationLineIndex;
		}

		/**
		 * Returns the setup exception for this line.
		 * @return the setup exception for this line
		 */
		public PhoenixRuntimeException getSetupException() {
			return setupException;
		}

		/**
		 * Sets the setup execption associated with this line. If when first processing this line,
		 * this is an error that gets thrown parsing the line, it can be saved with the line so
		 * that when the line is executed in the future, that error can be thrown.
		 * @param setupException the setup exception for this line
		 */
		public void setSetupException(PhoenixRuntimeException setupException) {
			this.setupException = setupException;
		}	

		/**
		 * Returns the line as it appeared in the source code.
		 * @return the original, unprocessed line
		 */
		public String getUnchangedLine() {
			return line;
		}

		/**
		 * Returns the line content: the line without comments and surrounding whitespace.
		 * @return the line content
		 */		
		public String getLineContent() {
			return content;
		}
		
		/**
		 * Returns the line's indent: the leading whitespace.
		 * @return the line's indent
		 */
		public String getIndent() {
			return indent;
		}
		
		/**
		 * Returns the line number of this line.
		 * @return the line number of this line
		 */
		public int getNumber() {
			return lineNumber;
		}
		
		/**
		 * Returns whether this line's content is empty. The line may have whitespace, and comments,
		 * but no code to be considered empty.
		 * @return true if the line content is empty, false otherwise
		 */
		public boolean isEmpty() {
			return empty;
		}
		
		/**
		 * Gets the source code containing this line.
		 * @return the source code containing this line
		 */
		public SourceCode getSourceCode() {
			return source;
		}
		
		/**
		 * Returns true if indent of this line is at least that of the given indent. That is, the
		 * indent of this line must start with the indent of the base indent.
		 * @param baseIndent the indent to compare this line's indent against
		 * @return whether this line's indent is at least (if not greater than) that of the base
		 * indent
		 */
		public boolean indentAtLeast(String baseIndent) {
			return getIndent().startsWith(baseIndent);
		}
		
		/**
		 * Returns whether the the indent of this line is strictly greater than that of the base
		 * indent. The indent of this line must be at least that of the base indent, and the indents
		 * cannot be equal
		 * @param baseIndent the indent to compare to this line's indent against
		 * @return whether this line's indent is greater than that of the base indent
		 */
		public boolean indentGreaterThan(String baseIndent) {
			return indentAtLeast(baseIndent) && !getIndent().equals(baseIndent);
		}
		
		public String toString() {
			return getLineContent() + " " + getLocationString();
		}
		
		/**
		 * Returns a string representation of this line's location. The string is of the form:
		 * <code>(file/path.phx:##)</code> where file/path.phx is the path of the source code
		 * containing this file and ## is the standard line number of this line (1 based).
		 * @return a string representation of this line's location.
		 */
		public String getLocationString() {
			return "(" + source.getFile().getPath() + ":" + (lineNumber + 1) + ")";
		}
	}
	
	/** The file containing the source code. */
	private File file;

	/** The list of lines of code in this source. */
	protected ArrayList<Line> code = new ArrayList<Line>();
	
	public SourceCode() {
		
	}
	
	/**
	 * Creates a new source code object from a file at a given path.
	 * @param path the path of the file to read from
	 * @throws FileNotFoundException if the file at the path cannot be found or read from
	 */
	public SourceCode(String path) throws FileNotFoundException {
		this(new File(path));
	}
	
	/**
	 * Creates a new source code object from a given file.
	 * @param file the file from which to read the source code
	 * @throws FileNotFoundException if the file at the path cannot be found or read from
	 */
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
	
	/**
	 * Returns the line at the given index. The first line is at index 0. The last line is at index
	 * size - 1.
	 * @param index the index of the line
	 * @return the line at that index
	 */
	public Line line(int index) {
		return code.get(index);
	}
	
	/**
	 * Returns the number of lines in the source code.
	 * @return the number of lines in the source code
	 */
	public int size() {
		return code.size();
	}
	
	/**
	 * Returns the file from which the source code was generated.
	 * @return the file from which the source code was generated
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Gets the end of the block starting after a given index. A block of code consists of all lines
	 * that are either empty, or whose indent is greater than that of the preceeding line. In this
	 * case the preceeding line is the line at the index of start. This returns the index of the
	 * last line in the block following the line at start.
	 * @param start the line preceeding the block
	 * @return the index of the last line of the block preceeded by the start line
	 */
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
