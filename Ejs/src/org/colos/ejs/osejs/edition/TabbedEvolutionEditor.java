/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;

public class TabbedEvolutionEditor extends TabbedEditor {
  protected JLabel secondButton;
  protected MenuItem addPage2;

  public TabbedEvolutionEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.EVOLUTION_EDITOR, "Model.Evolution");
    addPage2.setFont (addPageMI.getFont());
  }

  protected void customMenuItems (java.awt.event.ActionListener actionListener) {
    addPageMI = createMenuItem ("addPage",defaultHeader,actionListener);
    addPage2  = createMenuItem ("addPageODE",defaultHeader,new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        Object obj = JOptionPane.showInputDialog(getComponent(),
          res.getString("TabbedEditor.NewName"),res.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (defaultString));
        if (obj==null) return;
        String txt = obj.toString().trim();
        if (txt.length()>0) addPage (Editor.ODE_EDITOR,txt,null,true,false);
        else addPage (Editor.ODE_EDITOR,defaultString, null,true,false);
//        if (defaultType.equals(Editor.EVOLUTION_EDITOR)) ejs.getExperimentEditor().updateAllOdeList("addPageODE");
      }
    });
  }

  protected JPanel createFirstPanel () {
    JPanel firstPanel = super.createFirstPanel();
    secondButton = new JLabel (res.getString("TabbedEditor.ClickHereODE"));
    secondButton.setFont (secondButton.getFont().deriveFont(TabbedEditor.BUTTON_FONT_SIZE));
    JPanel secondButtonPanel = new JPanel (new GridBagLayout());
    secondButtonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    secondButtonPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked (MouseEvent _evt) {
        Object obj = JOptionPane.showInputDialog(getComponent(),
          res.getString("TabbedEditor.NewName"),res.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (defaultString));
        if (obj==null) return;
        String txt = obj.toString().trim();
        if (txt.length()>0) addPage (Editor.ODE_EDITOR,txt,null,true,false);
        else addPage (Editor.ODE_EDITOR,defaultString, null,true,false);
//        ejs.getExperimentEditor().updateAllOdeList("addPageODE");
      }
    });
    secondButtonPanel.add(secondButton);
    firstPanel.add (secondButtonPanel);
    return firstPanel;
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page;
    if (_type.equals(Editor.ODE_EDITOR)) page = new EquationEditor (ejs,this);
    else page = new CodeEditor (ejs,this);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  public void setColor (Color _color) {
    super.setColor(_color);
    secondButton.setForeground (_color);
  }

  protected String typeOfPage (Editor page) {
    if (page instanceof EquationEditor) return Editor.ODE_EDITOR;
    return Editor.EVOLUTION_EDITOR;
  }
  
  public boolean hasODEpages () {
    for (Editor page : getPages()) if (page instanceof EquationEditor) return true;
    return false;
  }

  public java.util.List<EquationEditor> getODEpages() {
    java.util.ArrayList<EquationEditor> list = new java.util.ArrayList<EquationEditor>();
    for (Editor page : getPages()) {
      if (page instanceof EquationEditor) list.add((EquationEditor) page);
    }
    return list;
  }
  

  public void addToInterpreter () {
    for (Editor page : getPages()) if (page instanceof EquationEditor) ((EquationEditor) page).addToInterpreter();
  }
  
  private java.util.HashSet<String> indepVarsSet = new java.util.HashSet<String>();
  
  public StringBuffer generateCode (int _type, String _info) {
    if (_type==Editor.GENERATE_CODE) indepVarsSet.clear();
    return super.generateCode(_type, _info);
  }
  
  public void checkIndependentVariable(String independentVariable) {
    if (indepVarsSet.contains(independentVariable)) { // Error
      JOptionPane.showMessageDialog(getComponent(),res.getString("Osejs.Evolution.RepeatedIndependentVariable"),
          res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
    }
    indepVarsSet.add(independentVariable);
  }

}
