package com.cdsc.eje.gui;

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
/**
 * @author Claudio De Sio Cesari
 */
public class ArrayInterface extends JavaInterface {
        /**
         * Try to find a class with the name className in the classpath and fill the
         * object whit methods and attributes.
         */
        @SuppressWarnings("unchecked")
        public ArrayInterface() {
                rowLength = 0;
                this.addElement(new MemberLine("length", "int"));
                for (int i = 0; i < this.size(); i++) {
                        this.setElementAt(((MemberLine) elementAt(i)).formatToString(), i);
                }
                //this.addElement("<<no member>>");
        }
}
