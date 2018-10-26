package com.cdsc.eje.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

//import com.cdsc.eje.jdk.JVM;

/*
 * EJE 2005 - version 2.5 - "Everyone's Java Editor"
 * 
 * Copyright (C) 2003 Claudio De Sio Cesari
 * 
 * Require JDK 1.4
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Info, Questions, Suggestions & Bugs Report to eje@claudiodesio.com
 *  
 */

public class JavaViewFactory implements ViewFactory {

	public static final String KEYWORD = "keyword";

	public static final String OPERATOR = "operator";

	//public static final String CLASS = "class"; not used anymore since
	// version 1.7.3

	public static final String RESERVED_WORD = "reserved_word";

	private static Map<String, String> javaSpecialWordsMap;

	public JavaViewFactory() {
		super();
		/**
		 * TODO: bisogna invocare un metodo che setta la lista delle classi nel
		 * classpath, al momento della colorazione delle parole in
		 * checkTextToComment si JavaDocument (bisogna valutare se si deve
		 * passare qualcosa di EJEArea in JavaDocument o viceversa)
		 */
		//		URL classListURL =
		// getClass().getResource("/resources/classlist.txt");
		//		if (keywordMap == null)
		//			keywordMap = getJavaSpecialWordsMap(classListURL);
		getJavaSpecialWordsMap();
	}

	public static Map<String, String> getJavaSpecialWordsMap() {
		javaSpecialWordsMap = new HashMap<String, String>();
		javaSpecialWordsMap.put("abstract", KEYWORD);
		javaSpecialWordsMap.put("assert", KEYWORD);
		javaSpecialWordsMap.put("boolean", KEYWORD);
		javaSpecialWordsMap.put("break", KEYWORD);
		javaSpecialWordsMap.put("byte", KEYWORD);
		javaSpecialWordsMap.put("case", KEYWORD);
		javaSpecialWordsMap.put("catch", KEYWORD);
		javaSpecialWordsMap.put("char", KEYWORD);
		javaSpecialWordsMap.put("class", KEYWORD);
		javaSpecialWordsMap.put("const", RESERVED_WORD);
		javaSpecialWordsMap.put("continue", KEYWORD);
		javaSpecialWordsMap.put("default", KEYWORD);
		javaSpecialWordsMap.put("do", KEYWORD);
		javaSpecialWordsMap.put("double", KEYWORD);
		javaSpecialWordsMap.put("else", KEYWORD);
		//String versionSubstring = JVM.getJVM().getVersion();
	//	System.out.println("versionSubstring:" + versionSubstring);
		/*if (versionSubstring.equals("1.5")) {
			javaSpecialWordsMap.put("enum", KEYWORD);
			javaSpecialWordsMap.put(((char) 64 + "interface"), KEYWORD);
			// is simboli non funzionano!
		}*/
		javaSpecialWordsMap.put("extends", KEYWORD);
		javaSpecialWordsMap.put("false", KEYWORD);
		javaSpecialWordsMap.put("final", KEYWORD);
		javaSpecialWordsMap.put("finally", KEYWORD);
		javaSpecialWordsMap.put("float", KEYWORD);
		javaSpecialWordsMap.put("for", KEYWORD);
		javaSpecialWordsMap.put("goto", RESERVED_WORD);
		javaSpecialWordsMap.put("if", KEYWORD);
		javaSpecialWordsMap.put("implements", KEYWORD);
		javaSpecialWordsMap.put("import", KEYWORD);
		javaSpecialWordsMap.put("instanceof", KEYWORD);
		javaSpecialWordsMap.put("int", KEYWORD);
		javaSpecialWordsMap.put("interface", KEYWORD);
		javaSpecialWordsMap.put("long", KEYWORD);
		javaSpecialWordsMap.put("native", KEYWORD);
		javaSpecialWordsMap.put("new", KEYWORD);
		javaSpecialWordsMap.put("null", KEYWORD);
		javaSpecialWordsMap.put("package", KEYWORD);
		javaSpecialWordsMap.put("private", KEYWORD);
		javaSpecialWordsMap.put("protected", KEYWORD);
		javaSpecialWordsMap.put("public", KEYWORD);
		javaSpecialWordsMap.put("return", KEYWORD);
		javaSpecialWordsMap.put("short", KEYWORD);
		javaSpecialWordsMap.put("static", KEYWORD);
		javaSpecialWordsMap.put("strictfp", KEYWORD);
		javaSpecialWordsMap.put("super", KEYWORD);
		javaSpecialWordsMap.put("switch", KEYWORD);
		javaSpecialWordsMap.put("synchronized", KEYWORD);
		javaSpecialWordsMap.put("this", KEYWORD);
		javaSpecialWordsMap.put("throw", KEYWORD);
		javaSpecialWordsMap.put("throws", KEYWORD);
		javaSpecialWordsMap.put("transient", KEYWORD);
		javaSpecialWordsMap.put("true", KEYWORD);
		javaSpecialWordsMap.put("try", KEYWORD);
		javaSpecialWordsMap.put("void", KEYWORD);
		javaSpecialWordsMap.put("volatile", KEYWORD);
		javaSpecialWordsMap.put("while", KEYWORD);

		/*
		 * if (classListURL != null) try { BufferedReader in = new
		 * BufferedReader(new InputStreamReader( classListURL.openStream()));
		 * String className; while ((className = in.readLine()) != null)
		 * javaSpecialWordsMap.put(className, CLASS); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		return javaSpecialWordsMap;
	}

	public View create(Element elem) {
		return new JavaView(elem);
	}

	class JavaView extends PlainView {

		public JavaView(Element elem) {
			super(elem);
		}

		protected int drawUnselectedText(Graphics g, int x, int y,
				int startOffset, int endOffset) throws BadLocationException {
			int length = (endOffset < getDocument().getLength() ? endOffset
					: getDocument().getLength())
					- startOffset;
			return scanParagraph(g, x, y, startOffset, length);
		}

		private boolean isParagraphInComment(int startOffset) {
			if (startOffset > 0) {
				Element root = getDocument().getDefaultRootElement();
				Element previousElement = root.getElement(root
						.getElementIndex(startOffset - 1));
				if (previousElement.getAttributes().containsAttribute(
						JavaDocument.MULTILINE_COMMENT,
						JavaDocument.MULTILINE_COMMENT))
					return true;
			}
			return false;
		}

		private int scanParagraph(Graphics g, int x, int y, int startPosition,
				int length) throws BadLocationException {
			JavaSegment content = new JavaSegment();
			JTextComponent host = (JTextComponent) getContainer();

			getDocument().getText(startPosition, length, content);
			boolean inComment = isParagraphInComment(startPosition);
			for (int wordIndex = 0; wordIndex < content.length();) {
				char indexedChar = content.charAt(wordIndex);

				if (Character.isJavaIdentifierStart(indexedChar) && !inComment) {
					String scannedIdentifier = scanIdentifier(content,
							wordIndex);
					String tokenType = javaSpecialWordsMap.get(scannedIdentifier);
					int scannedIdentifierLength = scannedIdentifier.length();

					Color color = getColorForTokenType(tokenType);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedIdentifierLength, text);
					g.setColor(color);
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedIdentifierLength;
				} else if (Character.isDigit(indexedChar) && !inComment) {
					String scannedNumericLiteral = scanNumericLiteral(content,
							wordIndex);

					int scannedNumericLiteralLength = scannedNumericLiteral
							.length();

					Color color = (Color) host
							.getClientProperty(EJEArea.NUMERIC_LITERAL_COLOR);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedNumericLiteralLength, text);
					g.setColor(color);
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedNumericLiteralLength;
				} else if (indexedChar == '\"' && !inComment) {
					int scannedStringLength = scanStringLiteral(content,
							wordIndex);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedStringLength, text);
					g.setColor((Color) host
							.getClientProperty(EJEArea.STRING_LITERAL_COLOR));
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedStringLength;
				} else if (indexedChar == '\'' && !inComment) {
					int scannedCharLength = scanCharLiteral(content, wordIndex);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedCharLength, text);
					g.setColor((Color) host
							.getClientProperty(EJEArea.CHAR_LITERAL_COLOR));
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedCharLength;
				} else if (isSingleLineCommentStart(content, wordIndex)
						&& !inComment) {
					int scannedCommentLength = scanSingleLineComment(content,
							wordIndex);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedCommentLength, text);
					g
							.setColor((Color) host
									.getClientProperty(EJEArea.SINGLE_LINE_COMMENT_COLOR));
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedCommentLength;
				} else if (isMultiLineCommentEnd(content, wordIndex)
						&& inComment) {
					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex, 2, text);
					g
							.setColor((Color) host
									.getClientProperty(EJEArea.MULTI_LINE_COMMENT_COLOR));
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					inComment = false;
					wordIndex = wordIndex + 2;
				} else if (isMultiLineCommentStart(content, wordIndex)) {
					int scannedCommentLength = scanMultiLineComment(content,
							wordIndex);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex,
							scannedCommentLength, text);
					g
							.setColor((Color) host
									.getClientProperty(EJEArea.MULTI_LINE_COMMENT_COLOR));
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + scannedCommentLength;
					//				} else if (isClass(indexedChar, wordIndex) && !inComment)
					// {
					//					Color color = (Color) host
					//							.getClientProperty(EJEArea.COMMON_WORD_COLOR);
					//
					//					Segment text = getLineBuffer();
					//					getDocument().getText(startPosition + wordIndex, 1,
					// text);
					//					g.setColor(color);
					//					x = Utilities.drawTabbedText(text, x, y, g, this,
					//							startPosition + wordIndex);
					//
					//					wordIndex = wordIndex + 1;
				} else if (isOperator(indexedChar) && !inComment) {
					Color color = (Color) host
							.getClientProperty(EJEArea.OPERATOR_COLOR);

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex, 1, text);
					g.setColor(color);
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);

					wordIndex = wordIndex + 1;
				} else {
					Color color;
					if (inComment)
						color = (Color) host
								.getClientProperty(EJEArea.MULTI_LINE_COMMENT_COLOR);
					else
						color = host.getForeground();

					Segment text = getLineBuffer();
					getDocument().getText(startPosition + wordIndex, 1, text);
					g.setColor(color);
					x = Utilities.drawTabbedText(text, x, y, g, this,
							startPosition + wordIndex);
					wordIndex++;
				}
			}
			return x;
		}

		//		private boolean isClass(char indexedChar, int wordIndex) {
		//			Element element = getDocument().getDefaultRootElement(); //the
		// document
		//			int rowNumber = element.getElementIndex(wordIndex);
		//			Element row = element.getElement(rowNumber);
		//			int firstColumnInRow = row.getStartOffset();
		//			String rowContent = null;
		//			String className = null;
		//			try {
		//				rowContent = (getDocument()).getText(
		//						firstColumnInRow, wordIndex - firstColumnInRow);
		//				className = getLastToken(rowContent);
		//			} catch (BadLocationException e1) {
		//				e1.printStackTrace();
		//			}
		//			if (className != null) {
		//				try {
		//					Class.forName(className);
		//					return true;
		//				} catch (ClassNotFoundException e) {
		//					System.out.println("Class name: " + className
		//							+ " not found, verifying imports");
		//					try {
		//						className = verifyImport(className, wordIndex);
		//						return true;
		//	/* } catch (FileNotFoundException exc) {
		//						System.out.println(className
		//								+ " is not an imported Class");
		//					*/} catch (Exception exc) {
		//						System.out.println("Exception?!?!");
		//						e.printStackTrace();
		//					}
		//				}
		//			}
		//			return false;
		//		}
		//
		//		private String getLastToken(String rowSegment)
		//				throws BadLocationException {
		//			StringTokenizer st = new StringTokenizer(rowSegment,
		//					"; \n\"()[]$|!,+-/=?^:<>\f\'\t\r%&~ ", false);
		//			String lastToken = null;
		//			while (st.hasMoreTokens()) {
		//				lastToken = st.nextToken();
		//			}
		//			return lastToken;
		//		}
		//
		//		private String verifyImport(String classToFind, int wordIndex)
		//				throws FileNotFoundException, IOException, BadLocationException {
		//
		//			Element element = getDocument().getDefaultRootElement();
		//			int rowNumber = element.getElementIndex(wordIndex);
		//			Element row = element.getElement(rowNumber);
		//
		//			String classInPackage = null;
		///* if ((classInPackage = verifyPackage(classToFind)) != null) {
		//				return classInPackage;
		//			}
		//*/ System.out.println("Verifying imports...");
		//			Pattern p = Pattern.compile("\\bimport\\b");
		//			String content = getDocument().getText(0, getDocument()
		//					.getLength());
		//			Matcher m = p.matcher(content);
		//			//java.lang.* is always imported
		//			try {
		//				String langClass = "java.lang." + classToFind;
		//				Class.forName(langClass);
		//				return langClass;
		//			} catch (ClassNotFoundException e) {
		//				System.out.println(classToFind + "is not a java.lang class");
		//			}
		//			return null;
		///* while (m.find()) {
		//				int end = m.end();
		//				System.out.println("Keyword import found at line " + end);
		//				String importLine[] = getImportLine(end);
		//				String packageName = importLine[0];
		//				String className = importLine[1];
		//				if (className.equals(classToFind))
		//					return packageName + "." + className;
		//				else if (className.equals("*")) {
		//					try {
		//						Class.forName(packageName + "." + classToFind);
		//						return packageName + "." + classToFind;
		//					} catch (ClassNotFoundException e) {
		//						System.out.println(packageName + "." + classToFind
		//								+ "is not a true class!");
		//					}
		//				}
		//			}
		//			throw new FileNotFoundException("Class not found: " + classToFind);*/
		//		}
		//
		///*
		//		private String verifyPackage(String className)
		//				throws FileNotFoundException, IOException, BadLocationException {
		//			System.out.println("Verifying if class is in package...");
		//			Pattern p = Pattern.compile("\\bpackage\\b");
		//			String content = ((AbstractDocument) getDocument()
		//					.getDefaultRootElement()).getText();
		//			Matcher m = p.matcher(content);
		//			if (m.find()) {
		//				int end = m.end();
		//				System.out.println("Keyword package found at line " + end);
		//				//String packageName = importLine[0];
		//				Element row = getRowAt(end);
		//				int lastColumnInRow = row.getEndOffset();
		//				String packageName = getLastTokenNoDot(EJEArea.this.getText(
		//						end, lastColumnInRow - end));
		//				try {
		//					Class.forName(packageName + "." + className);
		//					return packageName + "." + className;
		//				} catch (ClassNotFoundException e) {
		//					System.out.println(className + " is not in this package");
		//				}
		//			}
		//			return null;
		//		}
		//
		//		private String[] getImportLine(int importEndOffset)
		//				throws BadLocationException {
		//			Element row = getRowAt(importEndOffset);
		//			int lastColumnInRow = row.getEndOffset();
		//			String[] importLine = new String[2];
		//			importLine[1] = getLastToken(EJEArea.this.getText(importEndOffset,
		//					lastColumnInRow - importEndOffset));
		//			int packageEndOffset = lastColumnInRow
		//					- (importLine[1].length() + 3);
		//			importLine[0] = EJEArea.this.getText(importEndOffset,
		//					packageEndOffset - importEndOffset).trim();
		//			return importLine;
		//		}
		//*/
		private Color getColorForTokenType(String tokenType) {
			JTextComponent host = (JTextComponent) getContainer();
			if (tokenType == KEYWORD)
				return (Color) host.getClientProperty(EJEArea.KEYWORD_COLOR);
			//			else if (tokenType == CLASS)
			//				return (Color) host
			//						.getClientProperty(EJEArea.COMMON_WORD_COLOR);
			return host.getForeground();
		}

		private boolean isSingleLineCommentStart(JavaSegment string, int index) {
			if (string.charAt(index) == '/' && index + 1 < string.length()
					&& string.charAt(index + 1) == '/')
				return true;

			return false;
		}

		private int scanSingleLineComment(JavaSegment string, int index) {
			int commentLength = 0;
			for (commentLength = 0; commentLength < (string.length() - index); commentLength++) {
				char commentChar = string.charAt(index + commentLength);
				if (commentChar == '\n')
					break;
			}
			return commentLength;
		}

		private boolean isMultiLineCommentStart(JavaSegment string, int index) {
			if (string.charAt(index) == '/' && index + 1 < string.length()
					&& string.charAt(index + 1) == '*')
				return true;

			return false;
		}

		private boolean isMultiLineCommentEnd(JavaSegment string, int index) {
			if (string.charAt(index) == '*' && index + 1 < string.length()
					&& string.charAt(index + 1) == '/')
				return true;

			return false;
		}

//		private Element getRowAt(int offset) {
//			Element element = getDocument().getDefaultRootElement();
//			int rowNumber = element.getElementIndex(offset);
//			return element.getElement(rowNumber);
//		}

		private int scanMultiLineComment(JavaSegment string, int index) {
			int commentLength = 0;
			boolean starFound = false;
			for (commentLength = 0; commentLength < (string.length() - index); commentLength++) {
				char commentChar = string.charAt(index + commentLength);
				if (starFound && commentChar == '/') {
					commentLength++;
					break;
				}
				starFound = false;
				if (commentChar == '\n')
					break;
				else if (commentChar == '*')
					starFound = true;
			}
			return commentLength;
		}

		private int scanStringLiteral(JavaSegment string, int index) {
			int stringLength = 1;
			boolean backslash = false;
			for (stringLength = 1; stringLength < (string.length() - index); stringLength++) {
				char stringChar = string.charAt(index + stringLength);
				if (stringChar == '\\')
					backslash = true;
				else if (stringChar == '\n')
					break;
				else if (stringChar == '"' && !backslash) {
					stringLength++;
					break;
				} else if (backslash)
					backslash = false;
			}
			return stringLength;
		}

		private int scanCharLiteral(JavaSegment string, int index) {
			int charLength = 1;
			boolean backslash = false;
			for (charLength = 1; charLength < (string.length() - index); charLength++) {
				char charChar = string.charAt(index + charLength);
				if (charChar == '\\')
					backslash = true;
				else if (charChar == '\n')
					break;
				else if (charChar == '\'' && !backslash) {
					charLength++;
					break;
				} else if (backslash)
					backslash = false;
			}
			return charLength;
		}

		private String scanIdentifier(JavaSegment stringSegment, int index) {
			String string = new String(stringSegment.array,
					stringSegment.offset, stringSegment.count);
			int identifierLength = 0;
			if (!Character.isJavaIdentifierStart(string.charAt(index)))
				return "";
			for (identifierLength = 1; identifierLength < (string.length() - index); identifierLength++) {
//					char identifierChar = string.charAt(index+ identifierLength);
					if (!Character.isJavaIdentifierPart(string.charAt(index+ identifierLength)))
						break;
				}

			return string.substring(index, index + identifierLength);
		}

		private String scanNumericLiteral(JavaSegment stringSegment, int index) {
			String string = new String(stringSegment.array,
					stringSegment.offset, stringSegment.count);
			int identifierLength = 0;
			for (identifierLength = 0; identifierLength < (string.length() - index); identifierLength++) {
				//char identifierChar = string.charAt(index + identifierLength);
				if (!Character.isDigit(string.charAt(index + identifierLength)))
					break;
			}
			return string.substring(index, index + identifierLength);
		}

		private boolean isOperator(char c) {
			return (c == '-' || c == '+' || c == '*' || c == '/' || c == '<'
					|| c == '>' || c == '!' || c == '~' || c == '%' || c == '^'
					|| c == '&' || c == '|' || c == '=' || c == '.');
		}
	}
}