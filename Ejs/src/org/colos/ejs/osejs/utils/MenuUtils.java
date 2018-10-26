/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.event.*;
import javax.swing.*;

public class MenuUtils {
  static private ResourceUtil res    = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");
//  static java.awt.Color PANEL_BACKGROUND=javax.swing.UIManager.getColor("Panel.background");

  static public JRadioButton[] createRadioGroup (String[] _keys, String _prefix, ActionListener _al, boolean _tryShortForms) {
    ButtonGroup group = new ButtonGroup();
    JRadioButton[] buttons = new JRadioButton[_keys.length];
    for (int i=0; i<_keys.length; i++) {
      if (_keys[i]==null || _keys[i].equals("-")) { buttons[i] = null; continue; }
      String text=null;
      if (_tryShortForms) text = res.getOptionalString(_prefix+_keys[i]+".Short");
      if (text==null) text = res.getString(_prefix+_keys[i]);
      if (text==null) text = _keys[i];
      buttons[i] = new JRadioButton (text);
      buttons[i].setRequestFocusEnabled(false);
      //buttons[i].setMargin(new java.awt.Insets(0,3,0,10));
      buttons[i].setToolTipText(res.getString(_prefix+_keys[i]+".ToolTip"));//FKH 021031
      buttons[i].setActionCommand(_keys[i]);
      buttons[i].addActionListener (_al);
      group.add(buttons[i]);
    }
    buttons[0].setSelected (true);
    return buttons;
  }

  /*
  static public JButton[] createIconGroup (String[] _keys, String _prefix, ActionListener _al) {
    JButton[] buttons = new JButton[_keys.length];
    javax.swing.border.Border border = BorderFactory.createEmptyBorder(2,0,0,0);// createEtchedBorder(javax.swing.border.EtchedBorder.RAISED); 
    for (int i=0; i<_keys.length; i++) {
      if (_keys[i]==null || _keys[i].equals("-")) { buttons[i] = null; continue; }
      String filename = sysRes.getString (_prefix+_keys[i]);
      ImageIcon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(filename);
      if (icon==null) {
        System.out.println ("Could not read icon " + filename);
        buttons[i] = new JButton (_keys[i]);
      }
      else buttons[i] = new JButton(icon);
      buttons[i].setRequestFocusEnabled(false);
      buttons[i].setBorder(border);
      buttons[i].setActionCommand(_keys[i]);
      buttons[i].addActionListener(_al);
      buttons[i].setToolTipText(res.getString(_prefix+_keys[i]));
    }
    return buttons;
  }
*/
  
  static public JMenu createMenu (String _header, String[] _keys, String _prefix, ActionListener _al) {
//    System.out.println("Adding menu for keys = ");
//    for (int k=0; k<_keys.length; k++) System.out.println("keys["+k+"] = "+_keys[k]);
    int j;
    char mnemonic = '\0';
    String label = res.getString(_prefix+_header);
    if ((j=label.indexOf('_'))>=0) { // Look for the mnemonic
      mnemonic = label.charAt(j+1);
      label = label.substring (0,j);
    }
    JMenu menu = new JMenu (label);
    if (mnemonic!='\0') menu.setMnemonic (mnemonic);
    for (int i=0; i<_keys.length; i++) {
      if (_keys[i]==null || _keys[i].equals("-")) { menu.addSeparator(); continue; }
      mnemonic = '\0';
      JMenuItem mi;
      label = res.getString(_prefix+_keys[i]);
      if ((j=label.indexOf('_'))!=-1) { // Look for the mnemonic
        mnemonic = label.charAt(j+1);
        label = label.substring (0,j);
      }
      if (_keys[i].endsWith("CB_true")) {
        mi = new JCheckBoxMenuItem(label);
        ((JCheckBoxMenuItem)mi).setState (true);
      }
      else if (_keys[i].endsWith("CB")) mi = new JCheckBoxMenuItem(label);
      else mi = new JMenuItem(label);
      if (mnemonic!='\0') mi.setMnemonic (mnemonic);
      mi.setActionCommand(_keys[i]);
      mi.addActionListener (_al);
      menu.add(mi);
    }
    return menu;
  }


  static public JMenuBar createMenubar (String[] _headers, String _prefix, ActionListener _al) {
    JMenuBar menubar = new JMenuBar();
    for (int i=0; i<_headers.length; i++) {
      String[] keys = ResourceUtil.tokenizeString(sysRes.getString(_prefix+_headers[i]));
      menubar.add (createMenu (_headers[i], keys, _prefix, _al));
    }
    return menubar;
  }

} // end of class EjsUtil
