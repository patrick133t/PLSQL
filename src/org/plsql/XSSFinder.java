/* Copyright 2010 Patrick Higgins
 *
 * This file is part of PLSQL.
 *
 * PLSQL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * PLSQL is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with PLSQL.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.plsql;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class XSSFinder {

	private static Pattern safeFuncs = Pattern.compile("esc\\..*");

	private List<Boolean> callStack = new ArrayList<Boolean>();

	private String getName(CommonTree name) {
	    StringBuilder b = new StringBuilder();
	    for (int i = 0; i < name.getChildCount(); i++) {
	      b.append(name.getChild(i).toString());
	      b.append(".");
	    }
	    b.setLength(b.length()-1);
	    return b.toString();
	}

	private boolean isSafe(CommonTree name) {
	    return safeFuncs.matcher(getName(name).toLowerCase()).matches();
	}

	private void report(Token token, String msg) {
		String context = token.getInputStream().getSourceName()+":"+token.getLine()+":"+token.getCharPositionInLine();
		System.out.println(context+": "+msg);
	}

	public void enter(CommonTree name) {
	    callStack.add(isSafe(name));
	}

	public void exit(CommonTree name) {
	    callStack.remove(callStack.size()-1);
	}

	public void access(CommonTree name) {
	    for (boolean b : callStack) {
	        if (b) {
	            return;
	        }
	    }
	    report(((CommonTree) name.getChild(0)).token, "unsafe access: " + getName(name));
	}
	
	private static Set<String> declarationTokens = new HashSet<String>();
	static {
		declarationTokens.add("declare");
		declarationTokens.add("procedure");
		declarationTokens.add("function");
	}
	
	public void parseFile(File file) throws Exception {
		NoCaseFileStream input = new NoCaseFileStream(file.getAbsolutePath());
		PLSQLLexer lexer = new PLSQLLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		PLSQLParser parser = new PLSQLParser(tokens);
		CommonTree tree = (CommonTree) parser.sqlplus_file().getTree();
		//System.err.println(tree.toStringTree());
		//System.err.println("start: " + token + ", end: " + tokens.LT(1).toString());
		
		
		/*
		for (Token token = tokens.LT(1); token.getType() != -1; tokens.consume(), token = tokens.LT(1)) {
			String tokenText = token.getText().toLowerCase();			
			if ("htp".equals(tokenText)) {
				int marker = tokens.mark();
				PLSQLFunctionCall parser = new PLSQLFunctionCall(tokens);
				CommonTree tree = (CommonTree) parser.stmt().getTree();
				//System.err.println(tree.toStringTree());
				//System.err.println("start: " + token + ", end: " + tokens.LT(1).toString());
				CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
				nodes.setTokenStream(tokens);
				PLSQLFunctionCallXSS walker = new PLSQLFunctionCallXSS(nodes);
				walker.finder = this;
				walker.expr();
				
				tokens.rewind(marker);
				tokens.seek(tokens.index()+1);
			}
			else if (declarationTokens.contains(tokenText)) {
				int marker = tokens.mark();
				PLSQLDeclaration parser = new PLSQLDeclaration(tokens);
				CommonTree tree = (CommonTree) parser.declare().getTree();
				//System.err.println(tree.toStringTree());
				//System.err.println("start: " + token + ", end: " + tokens.LT(1).toString());

				CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
				nodes.setTokenStream(tokens);
				PLSQLDeclarationWalker walker = new PLSQLDeclarationWalker(nodes);
				walker.symbolTable = this;
				walker.expr();

				tokens.rewind(marker);
				tokens.seek(tokens.index()+1);				
			}
		}
		*/
	}
	
	private static void recurse(File dir, XSSFinder xss) throws Exception {
		File[] entries = dir.listFiles();
		Arrays.sort(entries);
		for (File file : entries) {
			if (file.isFile()) {
				xss.parseFile(file);
			}
			else if (file.isDirectory()) {
				recurse(file, xss);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		XSSFinder xss = new XSSFinder();

		for (String arg : args) {
			File f = new File(arg);
			if (f.isDirectory()) {
				recurse(f, xss);
			}
			else if (f.isFile()) {
				xss.parseFile(f);
			}
		}
	}

}
