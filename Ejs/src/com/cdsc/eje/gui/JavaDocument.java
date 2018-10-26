package com.cdsc.eje.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;

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

public class JavaDocument extends PlainDocument {

	public static final Object START_COMMENT = new AttributeKey("CommentStart");

	public static final Object END_COMMENT = new AttributeKey("CommentEnd");

	public static final Object MULTILINE_COMMENT = new AttributeKey(
			"MultilineComment");

	static class AttributeKey {
		private String attributeName;

		public AttributeKey(String attributeName) {
			this.attributeName = attributeName;
		}

		public String toString() {
			return attributeName;
		}
	}

	public void insertUpdate(DefaultDocumentEvent ev, AttributeSet att) {
		super.insertUpdate(ev, att);
		DocumentEvent.ElementChange change = ev
				.getChange(getDefaultRootElement());
		int startOffset;
		int length;
		if (change == null) {
			startOffset = ev.getOffset();
			length = ev.getLength();
		} else {
			Element[] elements = change.getChildrenAdded();
			startOffset = elements[0].getStartOffset();
			length = elements[elements.length - 1].getEndOffset()
					- elements[0].getStartOffset();
		}
		try {
			checkTextToComment(startOffset, length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postRemoveUpdate(DefaultDocumentEvent ev) {
		super.postRemoveUpdate(ev);
		try {
			checkTextToComment(ev.getOffset(), ev.getLength());
		} catch (Exception e) {
		}
	}

	/**
	 * Controlla se siamo in un testo da commentare
	 * 
	 * @param offset
	 * @param len
	 * @throws BadLocationException
	 */
	// qui bisogna introdurre la chiamata per i settaggio dell classi in
	// classpath
	// dentro la classe JavaViewFactory (nell'else)
	private void checkTextToComment(int offset, int len)
			throws BadLocationException {

		Element root = getDefaultRootElement();
		int firstParagraph = root.getElementIndex(offset);
		int lastParagraph = root.getElementIndex(offset + len);

		boolean inComment = getCommentStatus(firstParagraph);
		for (int i = firstParagraph; i <= lastParagraph; i++)
			inComment = checkTextToComment(root.getElement(i), inComment);
		//root.getElement(i)) stampa l'elemento dall'inizio della riga dove si
		// sta scrivendo
		// escluso gli spazi saltati all'inizio se si proviene da un enter
		if (inComment) {
			commentFollowingText(lastParagraph + 1);
		} else {
			uncommentFollowingText(lastParagraph + 1);
		}
	}

	public boolean getCommentStatus(int paragraphNumber) {
		if (paragraphNumber > 0) {
			Element previousElement = getDefaultRootElement().getElement(
					paragraphNumber - 1);
			return containsMultilineCommentAttribute(previousElement);
		}
		return false;
	}

	private void commentFollowingText(int startParagraph) {
		int lastParagraphToScan = getDefaultRootElement().getElementCount();
		for (int i = startParagraph; i < lastParagraphToScan; i++) {
			if (containsEndComment(getDefaultRootElement().getElement(i))) {
				Element e = getDefaultRootElement().getElement(i);
				fireChangedUpdate(new AbstractDocument.DefaultDocumentEvent(e
						.getStartOffset(), e.getEndOffset()
						- e.getStartOffset(), DocumentEvent.EventType.CHANGE));
				break;
			} 
			addMultilineCommentAttribute(getDefaultRootElement().getElement(i));
		}
	}

	private void uncommentFollowingText(int startParagraph) {
		int lastParagraphToScan = getDefaultRootElement().getElementCount();
		for (int i = startParagraph; i < lastParagraphToScan; i++) {
			if (containsMultilineCommentAttribute(getDefaultRootElement()
					.getElement(i))
					&& !containsStartComment(getDefaultRootElement()
							.getElement(i)))
				removeMultilineCommentAttribute(getDefaultRootElement()
						.getElement(i));
			else {
				Element e = getDefaultRootElement().getElement(i);
				fireChangedUpdate(new AbstractDocument.DefaultDocumentEvent(e
						.getStartOffset(), e.getEndOffset()
						- e.getStartOffset(), DocumentEvent.EventType.CHANGE));
				break;
			}
		}
	}

	private boolean checkTextToComment(Element paragraph, boolean inComment)
			throws BadLocationException {
		int startPosition = paragraph.getStartOffset();
		int length = (paragraph.getEndOffset() < getLength() ? paragraph
				.getEndOffset() : getLength())
				- startPosition;
		JavaSegment content = new JavaSegment();
		getText(startPosition, length, content);

		boolean slashFound = false;
		boolean starFound = false;
		boolean commentStartFound = false;
		boolean commentEndFound = false;
		boolean stringLiteral = false;
		boolean charLiteral = false;

		for (int wordIndex = 0; wordIndex < content.length(); wordIndex++) {
			char indexedChar = content.charAt(wordIndex);
			if (!inComment && slashFound && indexedChar == '/')
				break;
			else if (!inComment && indexedChar == '\"')
				stringLiteral = !stringLiteral;
			else if (!inComment && indexedChar == '\'')
				charLiteral = !charLiteral;
			else if (!starFound && !stringLiteral && !charLiteral
					&& indexedChar == '/')
				slashFound = true;
			else if (!slashFound && !stringLiteral && !charLiteral
					&& indexedChar == '*')
				starFound = true;
			else if (starFound && !stringLiteral && !charLiteral
					&& indexedChar == '/') {
				inComment = false;
				commentEndFound = true;
				starFound = false;
			} else if (slashFound && !stringLiteral && !charLiteral
					&& indexedChar == '*') {
				inComment = true;
				commentStartFound = true;
				slashFound = false;
			} else {
				starFound = false;
				slashFound = false;
			}
		}

		if (inComment)
			addMultilineCommentAttribute(paragraph);
		else
			removeMultilineCommentAttribute(paragraph);

		if (commentStartFound)
			addStartComment(paragraph);
		else
			removeStartComment(paragraph);

		if (commentEndFound)
			addEndComment(paragraph);
		else
			removeEndComment(paragraph);

		return inComment;
	}

	public static boolean containsStartComment(Element e) {
		return e.getAttributes()
				.containsAttribute(START_COMMENT, START_COMMENT);
	}

	public static boolean containsMultilineCommentAttribute(Element e) {
		return e.getAttributes().containsAttribute(MULTILINE_COMMENT,
				MULTILINE_COMMENT);
	}

	public static boolean containsEndComment(Element e) {
		return e.getAttributes().containsAttribute(END_COMMENT, END_COMMENT);
	}

	public void addStartComment(Element e) {
		addAttribute(e, START_COMMENT, START_COMMENT);
	}

	public void removeStartComment(Element e) {
		removeAttribute(e, START_COMMENT);
	}

	public void addMultilineCommentAttribute(Element e) {
		addAttribute(e, MULTILINE_COMMENT, MULTILINE_COMMENT);
	}

	public void removeMultilineCommentAttribute(Element e) {
		removeAttribute(e, MULTILINE_COMMENT);
	}

	public void addEndComment(Element e) {
		addAttribute(e, END_COMMENT, END_COMMENT);
	}

	public void removeEndComment(Element e) {
		removeAttribute(e, END_COMMENT);
	}

	public final void addAttribute(Element e, Object name, Object value) {
		((MutableAttributeSet) e.getAttributes()).addAttribute(name, value);
		fireChangedUpdate(new AbstractDocument.DefaultDocumentEvent(e
				.getStartOffset(), e.getEndOffset() - e.getStartOffset(),
				DocumentEvent.EventType.CHANGE));
	}

	public final void removeAttribute(Element e, Object name) {
		((MutableAttributeSet) e.getAttributes()).removeAttribute(name);
		fireChangedUpdate(new AbstractDocument.DefaultDocumentEvent(e
				.getStartOffset(), e.getEndOffset() - e.getStartOffset(),
				DocumentEvent.EventType.CHANGE));
	}
}