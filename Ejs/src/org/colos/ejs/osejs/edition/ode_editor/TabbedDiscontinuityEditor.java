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

public class TabbedDiscontinuityEditor extends TabbedEditor {
  private EquationEditor mEquationEditor;

  public TabbedDiscontinuityEditor (org.colos.ejs.osejs.Osejs _ejs, EquationEditor anEquationEditor) {
    super (_ejs, Editor.DISCONTINUITY_EDITOR, "EquationEditor.DiscontinuityCode");
    mEquationEditor = anEquationEditor;
//    getComponent().setPreferredSize(res.getDimension("EquationEditor.Errors.Size"));
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new DiscontinuityEditor (ejs,this,mEquationEditor);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  protected void addPage (String _typeOfPage, String _name, String _code, boolean _enabled, boolean _advanced) {
    super.addPage(_typeOfPage, _name, _code, _enabled, _advanced);
    mEquationEditor.checkAdvancedPanel();
  }
  
  protected void removeCurrentPage () {
    super.removeCurrentPage();
    mEquationEditor.checkAdvancedPanel();
  }

  public void fillSimulationXML(SimulationXML _simXML, Element _ode) {
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) {
      DiscontinuityEditor page = (DiscontinuityEditor) e.nextElement();
      if (!page.isInternal()) page.fillSimulationXML(_simXML,_ode);
    }
  }


}
