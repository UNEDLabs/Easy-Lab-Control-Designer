//******************************************************************************
//
// File:    OvalFig01.java
// Package: ---
// Unit:    Class OvalFig01
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

public class OvalFig01
	{
	private static final double in = 72.0;

	public static void main
		(String[] args)
		throws Exception
		{
		new Oval()
			.width (in*3/2) .height (in*1/2)
			.outline (new SolidOutline().width(3)) .add();
		Drawing.write ("OvalFig01.dwg");
		}
	}
