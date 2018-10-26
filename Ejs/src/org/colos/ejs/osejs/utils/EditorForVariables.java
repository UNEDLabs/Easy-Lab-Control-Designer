/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.colos.ejs.osejs.edition.*;

public class EditorForVariables {
  static private JDialog dialog;
  static private JList<String> list=null;
  static private String returnValue=null;
//  static private JRadioButton leftButton,centerButton,rightButton;

  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final String constantEntry = res.getString("EditorForVariables.ConstantEntry");
  static private MyCellRenderer rendererForActions=null, rendererForVariables=null;

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (evt.getSource() instanceof JList) {
          if (evt.getClickCount()>1) {
            returnValue = finalValue();
            dialog.setVisible (false);
          }
          return;
        }
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = finalValue();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("default"))  {
          returnValue = "<default>";
          dialog.setVisible (false);
        }
        else if (aCmd.equals("cancel")) {
          returnValue = null;
          dialog.setVisible (false);
        }
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (mouseListener);

    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setActionCommand ("default");
    defaultButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
//    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);


    list = new JList<String>();
    rendererForActions   = new MyCellRenderer(false);
    rendererForVariables = new MyCellRenderer(true);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener (mouseListener);
    list.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));

    JScrollPane scrollPanel = new JScrollPane(list);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (scrollPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    dialog.setSize (res.getDimension("EditorForVariables.Size"));
    dialog.validate();
    dialog.setModal(true);

  }

  public static String finalValue () {
    String txt = list.getSelectedValue();
    if (txt!=null) {
      int index = txt.indexOf(" : ");
      if (index<0) return txt;
      return txt.substring(0,index);
    }
    return null;
  }

  public static String finalDimensionedValue () {
    String txt = list.getSelectedValue();
    if (txt!=null) {
      int index = txt.indexOf(" : ");
      if (index<0) return txt;
      int index2 = txt.indexOf('[',index+3);
      if (index2>=0 && index2 <txt.indexOf(':',index+3)) return txt.substring(0,index)+"[i]";
      return txt.substring(0,index);
    }
    return null;
  }

  private static void addToData(Vector<String> data, String text) {
    String prefix = text, tailText="";
    int index = text.indexOf('(');
    if (index>0) {
      char[] tail = prefix.substring(index).toCharArray();
      prefix = prefix.substring(0,index);
      for (int i=0,n=tail.length; i<n; i++) if (tail[i]==',') tailText+=",";
    }
    String resText = res.getOptionalString("EditorForVariables."+prefix+tailText);
    if (resText==null) resText = res.getString("EditorForVariables."+prefix);
    data.add("_"+text+" : "+resText);
  }

//  private static String edit (ModelEditor modelEditor, String _property, String _basicType, JTextField returnField) {
//    return edit (modelEditor, _property, _basicType, returnField, returnField.getText().trim(),null);
//  }

//  public static String edit (ModelEditor modelEditor, String _property, String _basicType, JTextComponent returnField) {
//    return edit (modelEditor, _property, _basicType, returnField, returnField.getText().trim(),null);
//  }

  public static boolean isPredefinedAction (ModelEditor modelEditor,String _action) {
    String[] predefinedActions = modelEditor.getPredefinedActions();
    for (int i=0; i<predefinedActions.length; i++) if (predefinedActions[i].equals(_action)) return true;
    return false;
  }

  @SuppressWarnings("unchecked")
  public static String edit (ModelEditor modelEditor, String _property, String _basicType, Component _target, String _value, String _constant) {
    Vector<String> data= new Vector<String>();
    Vector<String> possibleTypes=new Vector<String>();
    Vector<String> customMethods=new Vector<String>();
    String txt;
    if (_property.equals("Actions")) {
      list.setCellRenderer(rendererForActions);
      // for (int i=0; i<predefinedActions.length; i++) addToData(data,predefinedActions[i]);
      if (modelEditor==null) txt = "";
      else txt = modelEditor.getLibraryEditor().generateCode(Editor.GENERATE_LIST_ACTIONS,"no_type").toString();
    }
    else {
      list.setCellRenderer(rendererForVariables);
//      _property = "Variables";
      txt = modelEditor.getVariablesEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
      if (_basicType!=null) {
        StringTokenizer typeTkn = new StringTokenizer(_basicType,"| ");
        while (typeTkn.hasMoreTokens()) possibleTypes.add(typeTkn.nextToken());
      }
      if (_constant!=null) {
        if (_constant.trim().length()>0) {
          data.add (_constant+constantEntry + res.getOptionalString("EditorForVariables.ConstantValue"));
        }
      }
      else for (String type : possibleTypes) {
        if (type.equals("boolean")) {
          if (modelEditor.getEJS().supportsHtml()) {
            data.add("_isPlaying : "+res.getOptionalString("EditorForVariables.isPlaying"));
            data.add("_isPaused : "+res.getOptionalString("EditorForVariables.isPaused"));
            data.add("_isMobile : "+res.getOptionalString("EditorForVariables.isMobile"));
          }
          else {
            data.add("_isPlaying() : "+res.getOptionalString("EditorForVariables.isPlaying"));
            data.add("_isPaused() : "+res.getOptionalString("EditorForVariables.isPaused"));
            data.add("_isApplet() : "+res.getOptionalString("EditorForVariables.isApplet"));
          }
        }
        // List custom methods
        customMethods.addAll(modelEditor.getCustomMethods(type)); 
      }
    }

    StringTokenizer tkn = new StringTokenizer (txt,"\n");
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
//      System.out.println("Comparing "+_basicType+" to "+token);
      if (possibleTypes.size()<=0) data.add(token);
      else {
        int index = token.indexOf(':');
        String type = token.substring(0,index);
        if (type.startsWith("java.awt.")) type = type.substring(9);  // Special clase where java.awt.Color is compared to Color
        else if (type.startsWith("java.lang.")) type = type.substring(10); // Special clase where java.lang.Object is compared to Object
        for (String typeDesired : possibleTypes) {
          if (typeDesired.equals(type)) {
            data.add(token.substring(index+1));
            break;
          }
//          if (modelEditor.getVariablesEditor().isVariableDefined(name, typeDesired)) { // doesn't work for Color (always returns true)
//            System.out.println("YES! "+name+" is a "+typeDesired);
//            data.add(name);
//            break;
//          }
        }
      }
    }
    data.addAll(customMethods);
    if (_property.equals("Actions")) { // Append predefined method to the end of the list
      String[] predefinedActions = modelEditor.getPredefinedActions();
      for (int i=0; i<predefinedActions.length; i++) addToData(data,predefinedActions[i]);
    }

    list.setListData(data);

    if (_value.length()>0) {
      for (int i=0; i<data.size(); i++) {
        String item = data.elementAt(i);
        if (item.startsWith(_value)) {
//        System.out.println("Item "+item+" matches with "+value);
          list.setSelectedIndex(i);
          list.setVisibleRowCount(i);
          break;
        }
//        else  System.out.println("Item NO "+item+" matches with "+value);
      }
    }
    dialog.setLocationRelativeTo (_target);
    dialog.setTitle (res.getString("EditorForVariables.Title"+_property));
    dialog.setVisible (true);
    return returnValue;
  }

  @SuppressWarnings("unchecked")
  public static String editPredefinedMethods (JComponent _target, ModelEditor _modelEditor, String _prefix) {
    Vector<String> data= new Vector<String>();
    list.setCellRenderer(rendererForActions);
//    System.out.println ("Prefix is "+_prefix);
    String title;
    if (_prefix.equals("_")) {
      title = res.getString("Utils.ModelMethods");
      String[] predefinedActions = _modelEditor.getPredefinedActions();
      for (int i=0; i<predefinedActions.length; i++) {
        if (predefinedActions[i].startsWith("view.")) continue;
        else if (predefinedActions[i].startsWith("tools.")) continue;
        else addToData(data,predefinedActions[i]);
      }
      Collections.sort(data);
    }
    else if (_prefix.equals("_view.")) {
      title = res.getString("Utils.ViewMethods");
      String[] predefinedActions = _modelEditor.getPredefinedActions();
      for (int i=0; i<predefinedActions.length; i++) if (predefinedActions[i].startsWith("view.")) addToData(data,predefinedActions[i]);
      Collections.sort(data);
    }
    else if (_prefix.equals("_tools.")) {
      title = res.getString("Utils.ToolsMethods");
      String[] predefinedActions = _modelEditor.getPredefinedActions();
      for (int i=0; i<predefinedActions.length; i++) if (predefinedActions[i].startsWith("tools.")) addToData(data,predefinedActions[i]);
      Collections.sort(data);
    }
    else { //     if (_prefix.length()==0) { // only custom methods
      title = res.getString("Utils.CustomMethods");
      if (_modelEditor!=null) {
        String txt = _modelEditor.getLibraryEditor().generateCode(Editor.GENERATE_LIST_ACTIONS,"no_type").toString();
        StringTokenizer tkn = new StringTokenizer (txt,"\n");
        while (tkn.hasMoreTokens()) data.add(tkn.nextToken());
      }
    }

    list.setListData(data);
    dialog.setLocationRelativeTo (_target);
    dialog.setTitle (title);
    dialog.setVisible (true);
    return returnValue;
  }
  
  @SuppressWarnings("unchecked")
  public static String edit (ViewEditor viewEditor, Class<?> _classname, Component _target, String _value) {
    Vector<String> possibleElements=new Vector<String>();
    list.setCellRenderer(rendererForActions);
    for (org.colos.ejs.library.control.ControlElement element : viewEditor.getTree().getControl().getElements()) {
//      System.err.println ("Comparing class "+_classname+" to "+element.getObject().getClass());
      if (_classname.isInstance(element.getObject())) {
//      if (element.getObject().getClass().equals(_classname)) {
        possibleElements.add(element.toString());
      }
    }

    list.setListData(possibleElements);

    dialog.setLocationRelativeTo (_target);
    dialog.setTitle (res.getString("EditorForVariables.TitleViewElements"));
    dialog.setVisible (true);
    return returnValue;
  }
  
  @SuppressWarnings("unchecked")
  public static String edit (Vector<String> _options, String _property, String _basicType, JComponent _target, String _value) {
//    if (_options.size()>0 && _options.get(0) instanceof org.colos.ejs.external.SimulinkBrowser) {
//      return editForSimulink ((org.colos.ejs.external.SimulinkBrowser) _options.get(0), _target, _value);
//    }
    list.setCellRenderer(rendererForActions);
    list.setListData(_options);
    if (_value.length()>0) {
      for (int i=0; i<_options.size(); i++) {
        String item = _options.elementAt(i);
        if (item.startsWith(_value)) {
//        System.out.println("Item "+item+" matches with "+value);
          list.setSelectedIndex(i);
          list.setVisibleRowCount(i);
          break;
        }
//        else  System.out.println("Item NO "+item+" matches with "+value);
      }
    }
    dialog.setLocationRelativeTo (_target);
    dialog.setTitle (res.getString("EditorForVariables.Title"+_property));
    dialog.setVisible (true);
    return returnValue;
  }

/*
  public static String editForSimulink (org.colos.ejs.external.SimulinkBrowser _browser, JComponent _target, String _value) {
    javax.swing.Timer timer = new javax.swing.Timer (10,_browser);
    _browser.configure(dialog,list,_value);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setCellRenderer(rendererForActions);
    timer.start();

    dialog.setLocationRelativeTo (_target);
    //dialog.setTitle (res.getString("EditorForVariables.Title"+_property));
    dialog.setVisible (true);

    timer.stop();
    _browser.done();
    _browser = null;
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    return returnValue;
  }
*/

}  // end of class

@SuppressWarnings("rawtypes")
class MyCellRenderer extends JLabel implements ListCellRenderer {
  private static final long serialVersionUID = 1L;
  static final private ResourceUtil res = new ResourceUtil("Resources");
  static private final String constantEntry = res.getString("EditorForVariables.ConstantEntry");
  static final private Color normalColor =
    InterfaceUtils.color(res.getString("EditorForVariables.Color"));
  static final private Color modelColor    = InterfaceUtils.color(res.getString("Model.Color"));
  static final private Color selectedColor = InterfaceUtils.color(res.getString("View.Color"));
//  static final private Font bigFont = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
  private boolean variableMode=true;

  public MyCellRenderer(boolean _mode) { variableMode = _mode; }

  public Component getListCellRendererComponent(JList list,Object value,
                     int index,boolean isSelected,boolean cellHasFocus) {
    String txt = value.toString();
    setEnabled(list.isEnabled());
    if (isSelected) setForeground(selectedColor);
    else {
      if (variableMode) {
        if (txt.indexOf(constantEntry)>=0) setForeground(normalColor);
        else
        if (txt.startsWith("_")) setForeground(normalColor);
        else setForeground(modelColor);
      }
      else {
        if (txt.startsWith("_")) setForeground(normalColor);
        else setForeground(modelColor);
      }
//      setFont(bigFont);
    }
    setFont(list.getFont());
    setText(txt);
    return this;
  }

}
