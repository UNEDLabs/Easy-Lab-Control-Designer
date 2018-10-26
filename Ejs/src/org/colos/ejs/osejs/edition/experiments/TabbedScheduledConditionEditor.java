/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.experiments;

import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;

public class TabbedScheduledConditionEditor extends TabbedEditor {

  public TabbedScheduledConditionEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.SCHEDULED_CONDITION_EDITOR, "Experiment.ScheduledCondition");
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new ScheduledConditionEditor (ejs.getModelEditor());
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  public void adjust () {
    for (int i=0,n=pageList.size(); i<n; i++) ((ScheduledConditionEditor) pageList.get(i)).adjust();
  }

}
