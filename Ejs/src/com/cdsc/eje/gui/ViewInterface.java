package com.cdsc.eje.gui;

import java.util.*;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.view.ViewElement;

/*
 * Paco Esquembre
 *
 */

/**
 * Define the interface for Ejs predefined methods
 */

@SuppressWarnings("serial")
public class ViewInterface extends JavaInterface {
  private static int ViewRowLength = 0;
  static private final ResourceUtil res = new ResourceUtil("Resources");

  /**
   * Try to find a class with the name className in the classpath and fill the
   * object with methods and attributes.
   */
  @SuppressWarnings("unchecked")
  public ViewInterface(ModelEditor modelEditor, ViewEditor viewEditor) {
    ViewRowLength = 0;
    for (ViewElement viewElement : viewEditor.getTree().getViewElements()) {
      String name = viewElement.getName();
      String type = viewElement.getElement().getObjectClassname();
      type = viewElement.getClassname();
      MemberLine memberLine = new MemberLine(name, type);
      this.addElement(memberLine);
      int memberLength = memberLine.toString().length();
      if (memberLength > ViewRowLength) ViewRowLength = memberLength;
    }
    for (int i = 0; i < this.size(); i++) {
      this.setElementAt( ( (MemberLine) elementAt(i)).formatToString(), i);
    }
    Collections.sort(this);
    int inserted = 0;
    String[] predefinedActions = modelEditor.getPredefinedActions();
    for (int i = 0; i < predefinedActions.length; i++) {
      String name = predefinedActions[i];
      if (!name.startsWith("view.")) continue;
      String prefix = name;
      int index = name.indexOf('(');
      if (index > 0) prefix = prefix.substring(0, index);
      MemberLine memberLine = new MemberLine(name.substring(5),res.getString("EditorForVariables." +prefix));
      this.insertElementAt(memberLine,inserted);
      inserted++;
      int memberLength = memberLine.toString().length();
      if (memberLength > ViewRowLength) ViewRowLength = memberLength;
    }
    for (int i = 0; i < inserted; i++) {
      setElementAt( ( (MemberLine) elementAt(i)).formatToString(), i);
    }
//    Collections.sort(this);
  }
}
