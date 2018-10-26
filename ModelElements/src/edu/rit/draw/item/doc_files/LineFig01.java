//******************************************************************************
//
// File:    LineFig01.java
// Package: ---
// Unit:    Class LineFig01
//
// This Java source file is copyright (C) 2006 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package edu.rit.draw.item.doc_files;

import edu.rit.draw.*;
import edu.rit.draw.item.*;
import java.awt.Font;

public class LineFig01
	{
	private static final double in = 72.0;

	public static void main
		(String[] args)
		throws Exception
		{
		Line line;
		OutlinedItem.defaultOutline (new SolidOutline().width(0.5f));
		Text.defaultFont (new Font ("serif", Font.ITALIC, 12));
		line = new Line()
			.to (0, 0) .hby (in*1) .vby (in*1) .round (in*1/2)
			.outline (new SolidOutline().width(3)) .add();
		line = new Line()
			.to (in*1/2, -in*1/16) .vby (-in*1/4) .add();
		line = new Line()
			.to (in*1, in*1/4) .vby (-in*9/16) .add();
		line = new Line()
			.to (in*1/2, -in*3/16) .hby (in*1/2)
			.startArrow (Arrow.SOLID) .endArrow (Arrow.SOLID) .add();
		new Text() .text ("d") .s (line.c()) .add();
		line = new Line()
			.to (in*3/4, 0) .hby (in*9/16) .add();
		line = new Line()
			.to (in*17/16, in*1/2) .hby (in*1/4) .add();
		line = new Line()
			.to (in*19/16, 0) .vby (in*1/2)
			.startArrow (Arrow.SOLID) .endArrow (Arrow.SOLID) .add();
		new Text() .text ("d") .w (line.c().e(in*1/32)) .add();
		Drawing.write ("LineFig01.dwg");
		}
	}
