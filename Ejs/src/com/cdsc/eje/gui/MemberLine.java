package com.cdsc.eje.gui;

import java.awt.Component;

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

public class MemberLine extends Component {
  private static final long serialVersionUID = 1L;

        private String name;

        private String attributes;

        private String returnType;

        private boolean isMethod;

//        private String gap = "";

        /** This constructor is used for fields instance */
        public MemberLine(String name, String type) {
                this.setNameAndReturnType(name, type);
        }

        /** This constructor is used for methods instance */
        public MemberLine(String name, String attributes[], String returnType) {
                this.setNameAttributesAndReturnType(name, attributes, returnType);
                isMethod = true;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public void setNameAndReturnType(String name, String returnType) {
                this.setName(name);
                this.setReturnType(returnType);
        }

        public void setNameAttributesAndReturnType(String name,
                        String attributes[], String returnType) {
                this.setNameAndReturnType(name, returnType);
                this.setAttributes(attributes);
        }

        public String getAttributes() {
                return attributes;
        }

        public void setAttributes(String[] attributes) {
                String strings = "";
                int length = attributes.length;
                for (int i = 0; i < length; i++) {
//			System.out.print(attributes[i] + "-");
                        attributes[i] = parseArrayParameters(attributes[i]);
//			System.out.print(attributes[i] + "*");
                        int j = -1;
                        if ((j = attributes[i].lastIndexOf(".")) != -1) {
                                strings += attributes[i].substring(j + 1, attributes[i]
                                                .length());
                                if (i + 1 != length)
                                        strings += ",";
                        } else {
                                strings += attributes[i];
                                if (i + 1 != length)
                                        strings += ",";
                        }
                }
//		System.out.println(strings);
                this.attributes = strings;
        }

        public String getGap() {//to be dinamic
                return " : ";
        }

        public String formatToString() {
                if (isMethod)
                        return this.getName() + "(" + this.getAttributes() + ")" + getGap()
                                        + this.getReturnType();
                return this.getName() + getGap() + this.getReturnType();
        }

        public String getReturnType() {
                return returnType;
        }

        public void setReturnType(String returnType) {
                int i = -1;
                if ((i = returnType.lastIndexOf(".")) != -1) {
                        returnType = returnType.substring(i + 1, returnType.length());
                }
                this.returnType = returnType;
        }

        public String getType() {
                return this.getReturnType().toString();
        }

        public String toString() {
                if (isMethod)
                        return this.getName() + "(" + this.getAttributes() + ")" + " "
                                        + this.getReturnType();
                return this.getName() + " " + this.getReturnType();
        }

        public String parseArrayParameters(String param) {
                StringBuffer tmpParam = new StringBuffer();
                if (param != null && param.startsWith("class [")) {
                        int indexOfLastBrace = param.lastIndexOf('[');
                        char typeEncoded = param.charAt(indexOfLastBrace + 1);
                        switch (typeEncoded) {
                        case 'B':
                                tmpParam.append("byte[]");
                                break;
                        case 'C':
                                tmpParam.append("char[]");
                                break;
                        case 'D':
                                tmpParam.append("double[]");
                                break;
                        case 'F':
                                tmpParam.append("float[]");
                                break;
                        case 'I':
                                tmpParam.append("int[]");
                                break;
                        case 'J':
                                tmpParam.append("long[]");
                                break;
                        case 'L':
                                String type = param.substring(indexOfLastBrace + 1, param
                                                .length() - 1);
                                tmpParam.append(type + "[]");
                                break;
                        case 'S':
                                tmpParam.append("short[]");
                                break;
                        case 'Z':
                                tmpParam.append("boolean[]");
                                break;
                        case 'V':
                                tmpParam.append("void");
                                break;
                        default:
                        //assert false : "tipo non conosciuto!";
                        }
                        int indexOfFirstBrace = param.indexOf('[');
                        if (indexOfFirstBrace != -1) {
                                while (indexOfFirstBrace <= indexOfLastBrace) {
                                        indexOfFirstBrace++;
                                        if (param.charAt(indexOfFirstBrace) == '[') {
                                                tmpParam.append("[]");
                                        }
                                }
                        }
                        param = tmpParam.toString();
                }
                return param;
        }
}
