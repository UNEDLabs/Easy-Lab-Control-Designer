/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejss.xml.SimulationXML;
import org.w3c.dom.Element;

public class TabbedErrorEditor extends TabbedEditor {
  private EquationEditor equationEditor;

  public TabbedErrorEditor (org.colos.ejs.osejs.Osejs _ejs, EquationEditor anEquationEditor) {
    super (_ejs, Editor.ERROR_EDITOR, "EquationEditor.ErrorCode");
    equationEditor = anEquationEditor;
    getComponent().setPreferredSize(res.getDimension("EquationEditor.Errors.Size"));
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new ErrorEditor (ejs,this,equationEditor);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  /**
   * Reads backwards compatible code for a single page of error handling code
   * @param _input
   */
  public void readBackwardsString (String _input) {
    if (_input.trim().length()<=0) return; // Do not add it if it is empty
    StringBuffer buffer = new StringBuffer();
    buffer.append("<ErrorType>"+ErrorEditor.ANY_ERROR+"</ErrorType>\n");
    buffer.append(_input);
    addPage (Editor.ERROR_EDITOR,res.getString("EquationEditor.ErrorEditor"),buffer.toString(),true,false);
    showFirstPage();
    setChanged(false);
  }
  
  protected void addPage (String _typeOfPage, String _name, String _code, boolean _enabled, boolean _advanced) {
    super.addPage(_typeOfPage, _name, _code, _enabled, _advanced);
    equationEditor.checkAdvancedPanel();
  }
  
  protected void removeCurrentPage () {
    super.removeCurrentPage();
    equationEditor.checkAdvancedPanel();
  }

  public void fillSimulationXML(SimulationXML _simXML, Element _ode) {
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) {
      ErrorEditor page = (ErrorEditor) e.nextElement();
      if (!page.isInternal()) page.fillSimulationXML(_simXML,_ode);
    }
  }


}
