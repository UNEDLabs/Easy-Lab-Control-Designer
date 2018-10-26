/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import java.util.*;
import java.awt.event.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.library.server.utils.models.MetadataActionModel;

class LibraryEditor extends CodeEditor {

  public LibraryEditor (Osejs _ejs, TabbedEditor aTabbedEditor) {
    super (_ejs, aTabbedEditor);
    textComponent.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        //        System.out.println("LibraryEditor calling updateControlValues");
        ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
      }
    });
  }

  public void clear () {
    super.clear();
    String methodName = getName().replace(' ','_');
    methodName = methodName.substring(0,1).toLowerCase() + methodName.substring(1);
    if (ejs.supportsJava()) textComponent.setText("public void "+methodName+" () {\n}");
    else textComponent.setText("function "+methodName+" () {\n}");
  }

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (!isActive()) return code;
    if (_type==Editor.GENERATE_CODE) {
      if (_info!=null) _info = res.getString(_info);
      code.append(CodeEditor.splitCode(getName(),textComponent.getText(), _info, "  "));
      code.append("\n"); //FKH 021020
    }
    else if (_type==Editor.GENERATE_LIST_ACTIONS) {
      for (String action : getMethods(ejs.supportsJava(),textComponent.getText(),_info)) code.append(action +" : "+commentField.getText()+"\n");
    }
    else if (_type==Editor.GENERATE_JSON_ACTIONS) {
      boolean first = true;
      code.append("[");
      for (String action : generateJSONMethodInfo()){
        MetadataActionModel jsonAction = new MetadataActionModel("","");
        JsonArrayBuilder params = Json.createArrayBuilder();
        for(String param : action.split("\\(")[1].split("\\)")[0].split(","))
          if(!param.equals(""))  params.add(param);
        jsonAction.loadAction("customMethod", action.split("\\(")[0], commentField.getText(), "text",
            false, "application/json", params.build(), Json.createObjectBuilder().build());
        if (!first)   code.append(",");
        else          first = false;
        code.append(jsonAction.getJSONModel());
      }
      code.append("]");
    }
    return code;
  }

  // -- Other methods

  
  /**
   * Removes all comments and quoted strings from the text
   */
  static private String removeStrings (String _text) {
    StringBuffer buffer = new StringBuffer();
    char[] chars = CodeEditor.removeComments(_text).toCharArray();
    boolean withinString=false;
    for (int i=0,n=chars.length; i<n; i++) {
      char c = chars[i];
      if (c=='\\') {
        if (withinString) i++; // this eliminates caveats such as \" or \\ 
        else buffer.append(c);
      }
      else if (c=='"') withinString = !withinString; // No need to add it
      else if (!withinString) buffer.append(c);
    }
    return buffer.toString();
  }

  /**
   * Removes blocks and returns a list of the rest
   */
  static private java.util.List<String> getBlocks (String _text) {
    java.util.List<String> blocks = new  java.util.ArrayList<String>();
    StringTokenizer tkn = new StringTokenizer (removeStrings(_text),"{};",true);
    int level = 0;
    String header=null;
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals("{")) level++; 
      else if (token.equals("}")) {
        level--;
        if (level==0 && header!=null) {
          blocks.add(header.trim());
        }
      }
      else if (level==0) {
        if (token.equals(";")) header = null; // a separate line of code
        else if (level==0) header = token;
      }
    }
    return blocks;
  }

  /**
   * Gets a list of all methods in a piece of code. 
   * @param _code
   * @param _type only methods of the given return type must be listed. "no_type" or null accepts all methods. 
   * "public" if only public methods must be listed
   * @return
   */
  static private java.util.List <String>  getMethods (boolean _javaSyntax, String _code, String _type) {
    //    if (!_javaSyntax) { // Javascript case
    //      return new  java.util.ArrayList<String>(); // TODO improve this. How to detect that a Javascript method returns a value of a given type?
    //    }
    java.util.List<String> actions = new  java.util.ArrayList<String>();

    if (!_javaSyntax) {
      for (String header : getBlocks(_code)) {
        //      System.out.println ("Processing header : "+header);
        if (!header.endsWith(")")) continue;
        String lastToken = null;
        boolean addThemAll = false;
        StringBuffer buffer = new StringBuffer();
        StringTokenizer tkn = new StringTokenizer (header," \t\n(),",true);
        while (tkn.hasMoreTokens()) {
          String token = tkn.nextToken();
          if (token.equals(" ") || token.equals("\t") || token.equals("\n")) continue;
          if (addThemAll) {
            buffer.append(token);
            continue; 
          }
          else if (token.equals("(")) {
            buffer.append(lastToken+"(");
            addThemAll=true;
          }
          else {
            lastToken = token;
          }
        }
        actions.add(buffer.toString());
      }
      return actions;
    }
    // Java syntax
    boolean onlyPublic = false;
    if (_type!=null) {
      if (_type.startsWith("public")) {
        onlyPublic = true;
        _type = _type.substring(6);
      }
      if (_type.equals("no_type")) _type = null;
      else if (_type.trim().length()<=0) _type = null; // no type
    }

    for (String header : getBlocks(_code)) {
      //      System.out.println ("Processing header : "+header);
      if (!header.endsWith(")")) continue;
      String lastToken = null, methodType=null;
      boolean isPublic = false, addThemAll = false, firstAfterComma=true, correctType=true;
      StringBuffer buffer = new StringBuffer();
      StringTokenizer tkn = new StringTokenizer (header," \t\n(),",true);
      while (tkn.hasMoreTokens()) {
        String token = tkn.nextToken();
        if (token.equals(" ") || token.equals("\t") || token.equals("\n")) continue;
        if (addThemAll) {
          if (firstAfterComma) {
            buffer.append(token+" ");
            firstAfterComma = false;
          }
          else if (token.equals(",")) {
            buffer.append(", ");
            firstAfterComma = true;
          }
          else buffer.append(token);
          continue; 
        }
        else if (token.equals("public")) isPublic = true;
        else if (token.equals("(")) {
          //          System.out.println ("Checking type = "+methodType + " against "+_type);
          if (_type!=null && !_type.equals(methodType)) {
            correctType = false; // non-public methods can also be linked to view properties
            break;
          }
          buffer.append(lastToken+"(");
          addThemAll=true;
        }
        else {
          if ("[".equals(lastToken) || "]".equals(lastToken) || "[]".equals(lastToken)) methodType += lastToken;
          else methodType = lastToken;
          lastToken = token;
        }
      }
      if (correctType) {
        if (onlyPublic) {
          if (isPublic) actions.add(buffer.toString());
        }
        else actions.add(buffer.toString());
      }
    }
    return actions;
  }

  public java.util.List <String> generateJSONMethodInfo(){
    //System.out.println("============ METADATA METHODS ==============");
    //System.out.println(getPublicMethods(textComponent.getText(), "public"));
    //System.out.println("============================================");
    return getPublicMethods(textComponent.getText(), "public");
  }
  
  
  /**
   * Gets a list of all methods in a piece of code. 
   * @param _code
   * @param _type only methods of the given return type must be listed. "no_type" or null accepts all methods. 
   * "public" if only public methods must be listed
   * @return
   */
  static private java.util.List <String>  getPublicMethods (String _code, String _type) {
    java.util.List<String> actions = new  java.util.ArrayList<String>();
    // Java syntax
    boolean onlyPublic = false;
    if (_type!=null) {
      if (_type.startsWith("public")) {
        onlyPublic = true;
        _type = _type.substring(6);
      }
      if (_type.equals("no_type")) _type = null;
      else if (_type.trim().length()<=0) _type = null; // no type
    }

    for (String header : getBlocks(_code)) {
      //System.out.println ("Processing header : "+header);
      if (!header.endsWith(")")) continue;
      String lastToken = null, methodType=null, methodReturns = null;
      boolean isPublic = false, addThemAll = false, firstAfterComma=true, correctType=true;
      StringBuffer buffer = new StringBuffer();
      StringTokenizer tkn = new StringTokenizer (header," \t\n(),",true);
      while (tkn.hasMoreTokens()) {
        String token = tkn.nextToken();
        if (token.equals(" ") || token.equals("\t") || token.equals("\n")) continue;
        if (addThemAll) {
          if (firstAfterComma) {
            buffer.append(token+" ");
            firstAfterComma = false;
          }
          else if (token.equals(",")) {
            buffer.append(", ");
            firstAfterComma = true;
          }
          else buffer.append(token);
          continue; 
        }
        else if (token.equals("int") || token.equals("boolean") ||
            token.equals("double") || token.equals("Object") || token.equals("String")){
          methodReturns = token;
        }
        else if (token.equals("public")) isPublic = true;
        else if (token.equals("(")) {
          //          System.out.println ("Checking type = "+methodType + " against "+_type);
          if (_type!=null && !_type.equals(methodType)) {
            correctType = false; // non-public methods can also be linked to view properties
            break;
          }
          buffer.append(lastToken+"(");
          addThemAll=true;
        }
        else {
          if ("[".equals(lastToken) || "]".equals(lastToken) || "[]".equals(lastToken)) methodType += lastToken;
          else methodType = lastToken;
          lastToken = token;
        }
      }
      buffer.append(" = " + methodReturns);
      if (correctType) {
        if (onlyPublic) {
          if (isPublic) actions.add(buffer.toString());
        }
        else actions.add(buffer.toString());
      }
    }
    return actions;
  }
  
  
} // end of class
