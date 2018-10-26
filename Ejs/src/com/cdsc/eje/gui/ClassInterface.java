package com.cdsc.eje.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

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
 * Lists the Java elements that conform the interface of a class.
 * This includes only static methods and attributes.
 */

public class ClassInterface
    extends JavaInterface {

  /**
   * Lists the static methods and attributes of a given class
   */
  @SuppressWarnings("unchecked")
  public ClassInterface(Class<?> objectClass) {
    rowLength = 0;
    Method[] methods = objectClass.getMethods();
    for (int i = 0; i < methods.length; i++) {
      if (Modifier.isStatic(methods[i].getModifiers())) {
        String name = methods[i].getName();
        Class<?>[] classParameters = methods[i].getParameterTypes();
        String[] stringClassParameters = new String[classParameters.length];
        for (int j = 0; j < classParameters.length; ++j) {
          stringClassParameters[j] = classParameters[j].toString();
        }
        String methodReturnType = methods[i].getReturnType().toString();
        MemberLine memberLine = new MemberLine(name,
                                               stringClassParameters,
                                               methodReturnType);
        this.addElement(memberLine);
        int memberLength = memberLine.toString().length();
        if (memberLength > rowLength) {
          rowLength = memberLength;
        }
      }
    }
    Field[] fields = objectClass.getFields();
    for (int i = 0; i < fields.length; i++) {
      if (Modifier.isStatic(fields[i].getModifiers())) {
        String type = fields[i].getType().toString();
        this.addElement(new MemberLine(fields[i].getName(), type));
      }
    }
    for (int i = 0; i < this.size(); i++) {
      this.setElementAt( ( (MemberLine) elementAt(i)).formatToString(), i);
    }
    Collections.sort(this);
  }
}
