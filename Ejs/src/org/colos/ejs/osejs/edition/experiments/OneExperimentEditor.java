/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.experiments;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.*;

class OneExperimentEditor extends CodeEditor {

  public OneExperimentEditor(Osejs _ejs, TabbedEditor aTabbedEditor){
    super(_ejs,aTabbedEditor);
  }

  public StringBuffer generateCode (int _type, String _info) {
    if (!isActive()) return new StringBuffer();
    if (_info==null) _info = "";
    StringBuffer code = new StringBuffer();
    if (_type==Editor.GENERATE_CODE) {
      _info = res.getString(_info);
      code.append("  public class _" + generateName + "Class extends org.colos.ejs.library.Experiment {\n\n");
      code.append("    public _" + generateName + "Class (String _name, String _description) { super (_name,_description); }\n\n");
      code.append("    public void run () {\n");
//      code.append("      _cSE._isAnyExperiment = true;\n");
      code.append("      _scheduledConditionsList.clear();\n");
      code.append("      _scheduledEventsList.clear();\n");
      code.append("      _input.clear();\n");
      code.append(CodeEditor.splitCode(getName(),textComponent.getText(),_info,"      "));
      code.append("      _scheduledConditionsList.clear();\n");
      code.append("      _scheduledEventsList.clear();\n");
      code.append("      _input.clear();\n");
//      code.append("      _cSE._isAnyExperiment = false;\n");
      code.append("    }\n\n");
      code.append("    public void _abortExperiment() {\n");
      code.append("      _pause();\n");
      code.append("      super._abortExperiment();\n");
      code.append("    }\n\n");
      code.append("  }"+"  // > " + _info + "." + getName()+"\n\n");//FKH 021020
/*
      code.append("  public void _start" + generateName +"Class(){\n");
      code.append("    if (!_cSE._isAnyExperiment) {\n");
      code.append("      org.colos.ejs.library.Experiment __"+generateName+"Class=_createExperiment(\"_" + generateName + "Class\");\n");
      code.append("      __"+ generateName +"Class._runExperiment();}\n");
      code.append("      _createExperiment(\"_" + generateName + "Class\")._runExperiment();\n");
      code.append("  }\n");
*/
    }
    else if (_type==Editor.GENERATE_DECLARATION) {
      String comment = commentField.getText().trim();
//      code.append("<input type=\"BUTTON\" value=\"" + name + "\"" +
//                  " onclick=\"document."+_info+"._simulation.runExperiment('" + generateName + "');\";>");
      code.append("      <div class=\"experiment\"><a href=\"javascript:document."+_info+"._simulation.runExperiment('" +
                     generateName + "');\">"+ name + "</a>");
      if (comment.length()>0) code.append (" <p>"+comment+"</p>");
      code.append("</div>"+System.getProperty("line.separator"));
    }
    else if (_type==Editor.GENERATE_LIST_ACTIONS) {
      String comment = commentField.getText().trim();
      code.append("    actions.add(new _"+ generateName +"Class (\""+name+"\",\""+comment+"\"));\n");
    }
    else if (_type==Editor.GENERATE_LIST_VARIABLES) {
      code.append("    if (_experimentName.equals(\""+generateName +"\")) return new _"+generateName+"Class(null,null);\n");
    }
    return code;
  }

} // end of class
