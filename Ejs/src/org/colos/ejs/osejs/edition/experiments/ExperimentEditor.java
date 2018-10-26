/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.experiments;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.*;

public class ExperimentEditor extends TabbedEditor {

  private JButton scheduledEventButton,scheduledConditionButton;
  private JTextField scheduledEventField,scheduledConditionField;
  private JPanel mainPanel;
  private JFrame scheduledEventDialog=null, scheduledConditionDialog=null;
  private TabbedScheduledEventEditor scheduledEventEditor=null;
  private TabbedScheduledConditionEditor scheduledConditionEditor=null;

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new OneExperimentEditor (ejs,this);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  public ExperimentEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super(_ejs,Editor.ONEEXPERIMENT_EDITOR, "Experiment");

   // --- Events row ---
    scheduledEventButton = new JButton(res.getString("Experiment.ScheduledEvent"));
    scheduledEventButton.setToolTipText (res.getString("Experiment.ScheduledEvent.Tooltip"));
    scheduledEventButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent evt) { scheduledEvents(true); }
    });

    scheduledEventField = new JTextField (2);
    scheduledEventField.setFont(scheduledEventField.getFont().deriveFont(Font.BOLD));
    scheduledEventField.setEditable(false);
    scheduledEventField.setText("0");
    scheduledEventField.setMargin (new Insets (0,0,0,0));
    scheduledEventField.setHorizontalAlignment(SwingConstants.RIGHT);

    JPanel scheduledEventPanel = new JPanel (new BorderLayout());
    scheduledEventPanel.setBorder (new EmptyBorder(0,0,1,0));
    scheduledEventPanel.add(scheduledEventButton,BorderLayout.WEST);
    scheduledEventPanel.add(scheduledEventField,BorderLayout.CENTER);

    // --- Conditions row ---
    scheduledConditionButton = new JButton(res.getString("Experiment.ScheduledCondition"));
    scheduledConditionButton.setToolTipText (res.getString("Experiment.ScheduledCondition.Tooltip"));
    scheduledConditionButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent evt) { scheduledConditions(true); }
    });

    scheduledConditionField = new JTextField (2);
    scheduledConditionField.setFont(scheduledConditionField.getFont().deriveFont(Font.BOLD));
    scheduledConditionField.setEditable(false);
    scheduledConditionField.setText("0");
    scheduledConditionField.setMargin (new Insets (0,0,0,0));
    scheduledConditionField.setHorizontalAlignment(SwingConstants.RIGHT);

    JPanel scheduledConditionPanel = new JPanel (new BorderLayout());
    scheduledConditionPanel.setBorder (new EmptyBorder(0,0,1,0));
    scheduledConditionPanel.add(scheduledConditionButton,BorderLayout.WEST);
    scheduledConditionPanel.add(scheduledConditionField,BorderLayout.CENTER);

    JPanel buttonsPanel= new JPanel (new BorderLayout());
    buttonsPanel.add(scheduledConditionPanel,BorderLayout.WEST);
    buttonsPanel.add(scheduledEventPanel,BorderLayout.EAST);

    mainPanel = new JPanel (new BorderLayout());
    mainPanel.setBorder (new EmptyBorder(0,0,1,0));
    mainPanel.add(buttonsPanel,BorderLayout.SOUTH);
    mainPanel.add(super.getComponent(),BorderLayout.CENTER);
  }

  public Component getComponent () { return mainPanel; }

  public TabbedEditor getscheduledEventEditor () { return scheduledEventEditor; }

  public boolean hasScheduleEvents() {
//    if (ejs.getOptions().experimentsEnabled() && scheduledEventEditor != null &&
//        scheduledEventEditor.getActivePageCount() > 0) return true;
    return false;
  }

  public void updateAllOdeList (String aCmd){
//    System.out.println("Update pages "+aCmd);
    if (scheduledEventEditor==null) return;
    java.util.Vector<Editor> eventList = scheduledEventEditor.getPages();
    for (int counter=1,n=eventList.size(); counter<=n; counter++) {
      ScheduledEventEditor anEvent = (ScheduledEventEditor) eventList.elementAt(counter-1);
      if (aCmd.equals("renamePage")) {
        int index=anEvent.odeList.getSelectedIndex();
        anEvent.updateOdeList();
        anEvent.odeList.setSelectedIndex(index);
      }
      else{
        String item=(String)anEvent.odeList.getSelectedItem();
        anEvent.updateOdeList();
        anEvent.odeList.setSelectedItem(item);
      }
    }
  }

  public void setFont (Font _font) {
    super.setFont(_font);
    if (scheduledEventEditor!=null) scheduledEventEditor.setFont(_font);
    if (scheduledConditionEditor!=null)  scheduledConditionEditor.setFont(_font);
  }

  public void setColor (Color _color) {
    super.setColor(_color);
    scheduledEventButton.setForeground(_color);
    scheduledConditionButton.setForeground(_color);
    if (scheduledEventEditor!=null) scheduledEventEditor.setColor(_color);
    if (scheduledConditionEditor!=null)  scheduledConditionEditor.setColor(_color);
  }

  private void scheduledEvents (boolean show) {
    if (scheduledEventDialog==null) {  // The first time
      scheduledEventDialog = new JFrame ();
      java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
      if (image!=null) scheduledEventDialog.setIconImage(image);
      scheduledEventDialog.setTitle(res.getString("Experiment.ScheduledEvent.Title"));
      scheduledEventDialog.setSize (res.getDimension("EquationEditor.Events.DialogSize"));
      scheduledEventDialog.setLocationRelativeTo (this.getComponent());
      scheduledEventDialog.getContentPane().setLayout (new BorderLayout());

      scheduledEventEditor = new TabbedScheduledEventEditor (ejs);
      scheduledEventEditor.setName("Event");
      scheduledEventEditor.setContentDelim ("EventContent");
      scheduledEventEditor.setColor(myColor);
      scheduledEventEditor.setFont(myFont);
      scheduledEventEditor.setPageCounterField(scheduledEventField,myColor,scheduledEventButton);
      scheduledEventDialog.getContentPane().add(scheduledEventEditor.getComponent(), BorderLayout.CENTER);

      scheduledEventDialog.getContentPane().addComponentListener (new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) { scheduledEventEditor.adjust(); }
      });
      scheduledEventDialog.validate();
    }
    if (show) {
      scheduledEventDialog.setVisible(true);
      if (scheduledEventDialog!=null) updateAllOdeList(""); // Create OdeList
    }
  }

  private void scheduledConditions (boolean show) {
    if (scheduledConditionDialog==null) {  // The first time
      scheduledConditionDialog = new JFrame ();
      scheduledConditionDialog.setTitle(res.getString("Experiment.ScheduledCondition.Title"));
      scheduledConditionDialog.setSize (res.getDimension("EquationEditor.Events.DialogSize"));
      scheduledConditionDialog.setLocationRelativeTo (this.getComponent());
      scheduledConditionDialog.getContentPane().setLayout (new BorderLayout());

      scheduledConditionEditor = new TabbedScheduledConditionEditor (ejs);
      scheduledConditionEditor.setName("Event");
      scheduledConditionEditor.setContentDelim ("EventContent");
      scheduledConditionEditor.setColor(myColor);
      scheduledConditionEditor.setFont(myFont);
      scheduledConditionEditor.setPageCounterField(scheduledConditionField,myColor,scheduledConditionButton);
      scheduledConditionDialog.getContentPane().add(scheduledConditionEditor.getComponent(), BorderLayout.CENTER);
      scheduledConditionDialog.getContentPane().addComponentListener (new ComponentAdapter() {
        public void componentResized(ComponentEvent evt) { scheduledConditionEditor.adjust();}
      });
      scheduledConditionDialog.validate();
    }
    if (show) scheduledConditionDialog.setVisible(true);
   }

  public StringBuffer saveStringBuffer () { //Gonzalo 070201
    StringBuffer save = new StringBuffer();
    save= super.saveStringBuffer();
    if (scheduledConditionEditor!=null) {
      save.append("<ScheduledCondition>");
      save.append(scheduledConditionEditor.saveStringBuffer());
      save.append("</ScheduledCondition>\n");
    }
    if (scheduledEventEditor!=null) {
      save.append("<ScheduledEvents>");
      save.append(scheduledEventEditor.saveStringBuffer());
      save.append("</ScheduledEvents>\n");
    }
    return (save);
  }

  public void clear() {  //Gonzalo 070209
    super.clear();
    if (scheduledConditionDialog!=null) {
      scheduledConditionDialog.dispose();
      scheduledConditionDialog = null;
      scheduledConditionEditor.clear();
      scheduledConditionEditor = null;
    }
    if (scheduledEventDialog!=null) {
      scheduledEventDialog.dispose();
      scheduledEventDialog = null;
      scheduledEventEditor.clear();
      scheduledEventEditor = null;
    }
  }

public void readString (String _input) { //Gonzalo 070201
  super.readString(_input);
  String txt;
  // read the execution events, if any
  txt = OsejsCommon.getPiece(_input,"<ScheduledCondition>","</ScheduledCondition>",false);
  if (txt!=null) {
    scheduledConditions(false); // create the editor and window
    scheduledConditionEditor.readString(txt);
  }
  // read the schedule events, if any
  txt = OsejsCommon.getPiece(_input,"<ScheduledEvents>","</ScheduledEvents>",false);
  if (txt!=null) {
    scheduledEvents(false); // create the editor and window
    scheduledEventEditor.readString(txt);
  }
}

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    code.append(super.generateCode(_type,_info));
    if (_type==Editor.GENERATE_DECLARATION) return code;
    if (_type==Editor.GENERATE_CODE) {
      code.append("  //---- Scheduled Conditions ----\n\n");
      code.append("  private static interface _ScheduledConditionClass {\n");
      code.append("    public boolean condition ();\n");
      code.append("    public void action ();\n");
      code.append("  }\n\n");
      code.append("  public void _scheduleCondition (String id){\n");
      code.append("    id=id.trim();\n");
      if (scheduledConditionEditor != null) {
        int counter = 1;
        for (Enumeration<Editor> e = scheduledConditionEditor.getPageEnumeration();e.hasMoreElements(); counter++) {
          Editor editor = e.nextElement();
          String conditionName = (editor.getName()).trim();
          code.append("    if (id.equals(\"" + conditionName + "\")) { _scheduledConditionsList.add (new _ScheduledCondition_" + counter + "()); return; }\n");
        }
      }
      code.append("    _alert (null,\"Scheduled Condition Error\",\"Scheduled condition not found: \"+id);\n");
      code.append("  }\n\n");
      if (scheduledConditionEditor != null) {
        String passName;
        if (_info != null && _info.trim().length() > 0) passName = _info;
        else {
          passName = scheduledConditionEditor.getName();
          if (passName.startsWith("Osejs.")) passName = passName.substring(6);
        }
        int counter = 1;
        for (java.util.Enumeration<Editor>  e = scheduledConditionEditor.getPageEnumeration(); e.hasMoreElements(); counter++) {
          Editor editor = e.nextElement();
          code.append("  private class _ScheduledCondition_" + counter + " implements _ScheduledConditionClass {\n");
          code.append("    public boolean condition () {\n");
          code.append(editor.generateCode(ScheduledConditionEditor.BOOLEAN_CONDITION,passName));
          code.append("    }\n");
          code.append("    public void action () {\n");
          code.append(editor.generateCode(ScheduledConditionEditor.ACTION, passName));
          code.append("    }\n");
          code.append("  }\n");
        }
      }
      code.append("  //---- End of Scheduled Conditions ----\n\n");

      code.append("  //---- Scheduled Events ----\n\n");

      code.append("  public void _scheduleEvent (String id){\n");
      code.append("    id=id.trim();\n");
      if (scheduledEventEditor != null) {
        int counter = 1;
        for (Enumeration<Editor> e = scheduledEventEditor.getPageEnumeration();e.hasMoreElements(); counter++) {
          ScheduledEventEditor editor = (ScheduledEventEditor) e.nextElement();
          String eventName = (editor.getName()).trim();
          String odeName = editor.getOdePage();
          code.append("    if (id.equals(\"" + eventName + "\")) {\n");
          code.append("      java.util.ArrayList list = (java.util.ArrayList) _scheduledEventsList.get(\""+odeName+"\");\n");
          code.append("      if (list==null) _scheduledEventsList.put(\""+odeName+"\",list = new java.util.ArrayList());\n");
          code.append("      list.add(new Integer(" + counter + ")); // Points to the event counter\n");
          code.append("      _automaticResetSolvers();\n");
          code.append("      return;\n");
          code.append("    }\n");
        }
      }
      code.append("    _alert (null,\"Scheduled Event Error\",\"Scheduled event not found: \"+id);\n");
      code.append("  }\n\n");
      code.append("    // Actual code for scheduled events appear next to normal events\n\n");

      code.append("  //---- End of Scheduled Events ----\n\n");

      //_scheduleAt Method
    }

    return code;
  }

  } // end of class
