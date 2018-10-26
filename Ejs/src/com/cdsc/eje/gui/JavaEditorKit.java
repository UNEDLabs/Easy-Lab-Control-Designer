package com.cdsc.eje.gui;

import java.awt.event.ActionEvent;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import javax.swing.text.ViewFactory;

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

public class JavaEditorKit extends DefaultEditorKit {

	private ViewFactory javaViewFactory = new JavaViewFactory();

	public String getContentType() {
		return "text/java";
	}

	public Document createDefaultDocument() {
		Document doc = new JavaDocument();
		return doc;
	}

	public final ViewFactory getViewFactory() {
		return javaViewFactory;
	}

	public static class InsertBreakAction extends TextAction {

		public InsertBreakAction() {
			super(DefaultEditorKit.insertBreakAction);
		}

		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			if (target != null) {
				if ((!target.isEditable()) || (!target.isEnabled())) {
					target.getToolkit().beep();
					return;
				}
				Boolean insertOnBreak = (Boolean) target
						.getClientProperty(EJEArea.INDENT_ON_INSERT_BREAK);
				if (insertOnBreak != null && insertOnBreak.booleanValue()) {
					Document document = target.getDocument();
					int selectionStart = target.getSelectionStart();
					int selectionEnd = target.getSelectionEnd();
					int lineNumber = document.getDefaultRootElement()
							.getElementIndex(selectionStart);
					Element lineElement = document.getDefaultRootElement()
							.getElement(lineNumber);
					int lastLineNumber = document.getDefaultRootElement()
							.getElementIndex(selectionEnd);
					Element lastLineElement = document.getDefaultRootElement()
							.getElement(lastLineNumber);
					int startOffset = lineElement.getStartOffset();
					int endOffset = selectionStart;
					int length = endOffset - startOffset;
					try {
						Segment lineHead = new Segment();
						target.getDocument().getText(startOffset, length,
								lineHead);
						StringBuffer spaces = new StringBuffer();
						for (int i = lineHead.offset; i < lineHead.offset
								+ lineHead.count; i++) {
							if (lineHead.array[i] == ' ')
								spaces.append(" ");
							else if (lineHead.array[i] == '\t')
								spaces.append("\t");
							else
								break;
						}
						String newLine = document.getText(selectionEnd, Math
								.min(lastLineElement.getEndOffset(), document
										.getLength())
								- selectionEnd);
						newLine = newLine.trim();
						String lastChar = document.getText(selectionEnd - 1, 1);
						String nextChar = null;
						try {
							nextChar = document.getText(selectionEnd, 1);
						} catch (StringIndexOutOfBoundsException strexc) {
							System.out.println(strexc);
						}
						target.select(target.getSelectionStart(), Math.min(
								lastLineElement.getEndOffset(), document
										.getLength()));
						if (lastChar.equals("{")
								&& (nextChar == null || !nextChar.equals("}"))) {
							spaces.append(EJEArea.TAB_SPACES); //.EditorOptionsPanel.getTab());
						}
						target.replaceSelection("\n" + spaces + newLine + "\n");
						target
								.setCaretPosition(endOffset + spaces.length()
										+ 1);
					} catch (Exception exc) {
						target.replaceSelection("\n");
					}
				} else
					target.replaceSelection("\n");
			}
		}
	}
}
