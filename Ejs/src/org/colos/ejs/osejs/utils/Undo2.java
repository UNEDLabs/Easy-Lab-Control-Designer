/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import java.awt.event.*;
import java.util.StringTokenizer;

import org.colos.ejs.osejs.edition.ModelEditor;
import org.opensourcephysics.display.OSPRuntime;

public class Undo2 {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private final String TAB = "  ";
  static private final int CONTROL_OR_COMMAND = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); // InputEvent.CTRL_MASK
  private JMenuItem undoAction, redoAction;
  private javax.swing.text.JTextComponent wizardTA=null;
  private UndoManager undoManager;
  private ModelEditor modelEditor=null;

  public void clear () {}

  public void setModelEditor (ModelEditor _modelEditor) { modelEditor = _modelEditor; }

  public Undo2 (javax.swing.text.JTextComponent _textArea, ModelEditor _modelEditor) {
    modelEditor = _modelEditor;
    final JPopupMenu popup = new JPopupMenu ();
    AL al = new AL();

    undoManager = new UndoManager();
    _textArea.getDocument().addUndoableEditListener(new UEL());

    undoAction = new JMenuItem(res.getString("Utils.undo"));
    undoAction.setEnabled(false);
    undoAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND));
    undoAction.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent e) { undoOne(); }
    });
//    undoAction = new UndoAction(res.getString("Utils.undo"));
    redoAction = new JMenuItem(res.getString("Utils.redo"));
    redoAction.setEnabled(false);
    redoAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK));
    redoAction.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent e) { redoOne(); }
    });

    popup.add(undoAction);
    popup.add(redoAction);
    popup.addSeparator();
    setMenu (_textArea,popup);

    _textArea.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed (java.awt.event.KeyEvent _e) {
        if (_e.getModifiers()==CONTROL_OR_COMMAND) {
          switch (_e.getKeyCode()) {
            case KeyEvent.VK_Z : undoOne(); break;
            case KeyEvent.VK_Y : redoOne(); break;
          }
        }
        else if (_e.getModifiers()==(CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK)) {
          switch (_e.getKeyCode()) {
            case KeyEvent.VK_Z : redoOne(); break;
          }
        }
      }
    });
//    javax.swing.text.Keymap map = _textArea.getKeymap();
//    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND), new AbstractAction(){
//      public void actionPerformed(ActionEvent e) { undoOne(); }
//    });
//    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK), new AbstractAction(){
//      public void actionPerformed(ActionEvent e) { redoOne(); }
//    });
    


    JMenuItem wizard = new JMenuItem(res.getString("Utils.wizard"));
    wizard.setActionCommand("wizard");
    wizard.addActionListener(al);
//    wizard.setEnabled(true);

    wizardTA = _textArea;

    JMenuItem custom = new JMenuItem(res.getString("Utils.CustomMethods"));
    custom.setActionCommand("predefined");
    custom.addActionListener(al);

    JMenuItem predefined = new JMenuItem(res.getString("Utils.ModelMethods"));
    predefined.setActionCommand("predefined_");
    predefined.addActionListener(al);

    JMenuItem view = new JMenuItem(res.getString("Utils.ViewMethods"));
    view.setActionCommand("predefined_view.");
    view.addActionListener(al);

    JMenuItem tools = new JMenuItem(res.getString("Utils.ToolsMethods"));
    tools.setActionCommand("predefined_tools.");
    tools.addActionListener(al);

    JMenu methodsMenu = new JMenu(res.getString("Utils.Methods"));
    methodsMenu.add(custom);
    methodsMenu.add(predefined);
    methodsMenu.add(view);
    methodsMenu.add(tools);

    JMenuItem formatAction = new JMenuItem(res.getString("Utils.format"));
    formatAction.setEnabled(true);
//    formatAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK));
    formatAction.addActionListener (new FormatCodeAction());

    popup.addSeparator();
    popup.add(methodsMenu);
    popup.add(formatAction);
    popup.add(wizard);
  }

  static private String getLabel (String name) {
    String label = res.getString("Utils."+name);
    int j=label.indexOf('_');
    if (j!=-1) { // Look for the mnemonic
      label = label.substring (0,j);
    }
    return label;
  }
  
  static public void setMenu (final javax.swing.text.JTextComponent textComponent, JPopupMenu aMenu) {
    final JPopupMenu popup;
    if (aMenu!=null) popup = aMenu;
    else popup = new JPopupMenu ();
    
    javax.swing.text.Keymap map = textComponent.getKeymap();
    
    Action[] act = textComponent.getActions ();
    JMenuItem miCut=null, miCopy=null, miPaste=null, miSelectAll=null;
    for (int i = 0; i < act.length; i++) {
      String name = (String) act[i].getValue(Action.NAME);
      if (name.equals("cut-to-clipboard")) {
        miCut = new JMenuItem(act[i]);
        miCut.setText(getLabel(name));
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_X,CONTROL_OR_COMMAND);
        miCut.setAccelerator(stroke);
        map.addActionForKeyStroke(stroke,act[i]);
      }
      else if (name.equals("copy-to-clipboard")) {
        miCopy = new JMenuItem(act[i]);
        miCopy.setText(getLabel(name));
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_C,CONTROL_OR_COMMAND);
        miCopy.setAccelerator(stroke);
        map.addActionForKeyStroke(stroke,act[i]);
      }
      else if (name.equals("paste-from-clipboard")) {
        miPaste = new JMenuItem(act[i]);
        miPaste.setText(getLabel(name));
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_P,CONTROL_OR_COMMAND);
        miPaste.setAccelerator(stroke);
        map.addActionForKeyStroke(stroke,act[i]);
      }
      else if (name.equals("select-all")) {
        miSelectAll = new JMenuItem(act[i]);
        miSelectAll.setText(getLabel(name));
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_A,CONTROL_OR_COMMAND);
        miSelectAll.setAccelerator(stroke);
        map.addActionForKeyStroke(stroke,act[i]);
      }
    }
    if (miCut!=null) popup.add(miCut);
    if (miCopy!=null) popup.add(miCopy);
    if (miPaste!=null) popup.add(miPaste);
    if (miSelectAll!=null) { popup.addSeparator(); popup.add(miSelectAll); }
    textComponent.addMouseListener (new java.awt.event.MouseAdapter() {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (OSPRuntime.isPopupTrigger(evt) //SwingUtilities.isRightMouseButton(evt) 
            && textComponent.isEditable()) popup.show (textComponent,evt.getX(),evt.getY());
      }
    });
  }

  public void undoOne() {
    try { undoManager.undo(); }
    catch (CannotUndoException ex) {
//      System.out.println("Unable to undo: " + ex);
//      ex.printStackTrace();
    }
    undoAction.setEnabled (undoManager.canUndo());
    redoAction.setEnabled (undoManager.canRedo());
  }

  public void redoOne() {
    try { undoManager.redo(); }
    catch (CannotRedoException ex) {
//      System.out.println("Unable to redo: " + ex);
//      ex.printStackTrace();
    }
    undoAction.setEnabled (undoManager.canUndo());
    redoAction.setEnabled (undoManager.canRedo());
  }

  protected class UEL implements UndoableEditListener {
      public void undoableEditHappened(UndoableEditEvent e) {
          UndoableEdit edit = e.getEdit();
          // discard style edits
          if (edit.getPresentationName().startsWith("style")) return;
          // Remember the edit and update the menus.
          undoManager.addEdit(edit);
          undoAction.setEnabled (undoManager.canUndo());
          redoAction.setEnabled (undoManager.canRedo());
      }
  }
/*
  private class UndoAction extends JMenuItem implements ActionListener {
    public UndoAction(String text) {
      super(text);
      setEnabled(false);
      addActionListener (this);
      setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND));
//      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, CONTROL_OR_COMMAND)); Doesn't work!
    }
    public void actionPerformed(ActionEvent e) {
      try { undoManager.undo(); }
      catch (CannotUndoException ex) {
        System.out.println("Unable to undo: " + ex);
        ex.printStackTrace();
      }
      setEnabled (undoManager.canUndo());
      redoAction.setEnabled (undoManager.canRedo());
    }
  }

  private class RedoAction extends JMenuItem implements ActionListener {
    public RedoAction(String text) {
      super(text);
      setEnabled(false);
      addActionListener(this);
      setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK));
//      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, CONTROL_OR_COMMAND | InputEvent.SHIFT_MASK)); Doesn't work!
    }
    public void actionPerformed(ActionEvent e) {
      try { undoManager.redo(); }
      catch (CannotRedoException ex) {
        System.out.println("Unable to redo: " + ex);
        ex.printStackTrace();
      }
      undoAction.setEnabled (undoManager.canUndo());
      setEnabled (undoManager.canRedo());
    }
  }
*/

// Format code action implementation
// **********************************************
  class FormatCodeAction extends AbstractAction {
        private StringBuffer entersTyped;
        private StringBuffer formattedCode;
        private StringBuffer tab;
        private final byte CLOSE = 0;
        private final byte OPEN = 1;
        private final byte STATEMENT = 2;
        private byte last;

        public void actionPerformed(ActionEvent ev) {
            last = -1;
            String code = wizardTA.getText();
            if (code == null) return;
            entersTyped = new StringBuffer();
            tab = new StringBuffer();
            StringTokenizer codeTokenized = new StringTokenizer(code, "\n{}", true);
            formattedCode = new StringBuffer();
            try {
                /*
                 * if (codeTokenized.hasMoreTokens()) { String firstToken =
                 * codeTokenized.nextToken(); formattedCode.append("" +
//		     * firstToken); System.out.println("firstToken"); }
                 */
                while (codeTokenized.hasMoreTokens()) {
                    String token = codeTokenized.nextToken();
                    if (token.equals("\n")) {
                        entersTyped.append("\n");
                        continue;
                    }
                    token = token.trim();
                    handleToken(token);
                }
                String formattedCodeString = formattedCode.toString();
                /*
                 * int position =
                 * code.indexOf(ejeTab.textArea.getCaretPosition(),
                 * ejeTab.textArea.getCaretPosition());
                 * System.out.println(position);
                 */
                /* Point p = ejeTab.textArea.getCaret().getMagicCaretPosition(); */
                wizardTA.setText(formattedCodeString);
                /*
                undoManager.discardAllEdits();
                undoAction.setEnabled (undoManager.canUndo());
                redoAction.setEnabled (undoManager.canRedo());
*/
                /* ((DefaultCaret)ejeTab.textArea.getCaret()).setLocation(p); */
                /* ejeTab.textArea.getCaret().setMagicCaretPosition(p); */
                /* ejeTab.textArea.setCaretPosition(position); */
            } catch (StringIndexOutOfBoundsException exc) {
                //		    System.out.println("parentesi non pari");
                JOptionPane.showMessageDialog(wizardTA, res.getString("Utils.formatError"),
                                              res.getString("Warning"),JOptionPane.ERROR_MESSAGE);
            }
        }

        //TO BE DONE: handle token in string and in comment
        public void handleToken(String token)
            throws StringIndexOutOfBoundsException {
            String tabString = " ";
            if (token == null)
                return;
            if (token.equals("{")) {
                if (last == OPEN || last == CLOSE) {
                    entersTyped = new StringBuffer("\n");
                    tabString = tab.toString();
                } else {
                    entersTyped = new StringBuffer();
                }
//                formattedCode.append((true ? ""
//                                      + entersTyped + tabString : "\n" + tab.toString())
//                                     + token);
                formattedCode.append( "" + entersTyped + tabString + token);
                tab.append(TAB);
                entersTyped = new StringBuffer();
                last = OPEN;
            } else if (token.equals("}")) {
                if (entersTyped.length() == 0 || last == CLOSE) {
                    entersTyped = new StringBuffer("\n");
                }
                int length = tab.length();
                tab = tab.delete(length - TAB.length(),length);
                formattedCode.append("" + entersTyped + tab + token);
                entersTyped = new StringBuffer();
                last = CLOSE;
            } else {
                tabString = tab.toString();
                if (token.equals(";")) {
                    tabString = "";
                    entersTyped = new StringBuffer();
                }
                if (last == OPEN) {
                    entersTyped = new StringBuffer("\n");
                } else if (last == CLOSE && entersTyped.length() == 0) {
                    tabString = " ";
                }
                if (token.equals("")) {
                    return;
                }
                formattedCode.append("" + entersTyped + tabString + token);
                entersTyped = new StringBuffer();
                last = STATEMENT;
            }

        }
    };

    private class AL implements java.awt.event.ActionListener {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        String aCmd=evt.getActionCommand();
        if (aCmd.equals("wizard")) {
          String txt = org.colos.ejs.osejs.edition.wizards.CodeWizard.edit (wizardTA);
          if (txt!=null) try {
            wizardTA.getDocument().insertString (wizardTA.getCaretPosition(),txt,null);
          }
          catch (Exception exc) { exc.printStackTrace();}
        }
        else if (aCmd.startsWith("predefined")) {
          String prefix = aCmd.substring(10); // "" = custom, "_view", "_tools", "_"
          String txt = EditorForVariables.editPredefinedMethods (wizardTA,modelEditor,prefix);
          if (txt!=null) try {
            wizardTA.getDocument().insertString (wizardTA.getCaretPosition(),txt+";",null);
          }
          catch (Exception exc) { exc.printStackTrace();}
        }
      }
    }

  }
