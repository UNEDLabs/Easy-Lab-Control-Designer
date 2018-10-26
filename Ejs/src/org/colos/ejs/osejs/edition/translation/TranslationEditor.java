/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.colos.ejs.osejs.edition.translation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.library.utils.LocaleListener;
import org.colos.ejs.library.utils.LocaleSelector;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;
import org.opensourcephysics.tools.FontSizer;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;

/**
 * An Editor to translate simulation properties
 * 
 * @author Francisco Esquembre
 * @version 1.0
 */
public class TranslationEditor implements LocaleListener {
  static private ResourceUtil res = new ResourceUtil ("Resources");
//  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");
  static private String[] columnNames = new String[] { res.getString("TranslationEditor.Property"), res.getString("TranslationEditor.Value") };

  static private Color LIGHT_GRAY = new Color (240,240,240);
  
  private Osejs ejs;
  private JTable table;
  private AbstractTableModel tableModel;
  private LocaleItem currentLocaleItem = LocaleItem.getDefaultItem();
  private LocaleSelector localeSelector;
  private JDialog dialog;
  private JButton removeTranslationButton, addButton, removeButton;
  private boolean firstTime=true, changed=false;
  
  private java.util.List<TranslatableProperty> keywordList = new ArrayList<TranslatableProperty>(); // all properties
  private Set<AddedTranslatableProperty> addedProperties = new HashSet<AddedTranslatableProperty>(); // properties added by the user

  
  static public String removeQuotes(String _value) {
    switch (VariablesEditor.numberOfQuotes(_value)) {
      case 1 : return FileUtils.removeQuotes(_value);
      case 2 : if (_value.startsWith("\"") && _value.endsWith("\"")) return _value.substring(1,_value.length()-1);
        //$FALL-THROUGH$
      default : 
      case 0 : return _value;
    }
  }
  
  
  
  public TranslationEditor (Osejs _ejs) {
    ejs = _ejs;

    tableModel  = new AbstractTableModel () {
      public String getColumnName(int col) { return columnNames[col].toString(); }
      
      public boolean isCellEditable(int rowIndex, int columnIndex) {
        TranslatableProperty tp = keywordList.get(rowIndex);
        if (tp.isNameEditable()) { // added properties 
          if (columnIndex==1) return true; // value is always editable
          return currentLocaleItem.isDefaultItem(); // name only in default locale
        }
        // View properties
        if (columnIndex==1) return !currentLocaleItem.isDefaultItem(); // value only for non-default locales
        return false;  // name is not editable
      }
      
      public int getColumnCount() { return columnNames.length; }
      
      public int getRowCount() { return keywordList.size(); }
      
      public Object getValueAt(int rowIndex, int columnIndex) {
        TranslatableProperty tp = keywordList.get(rowIndex);
        if (columnIndex==0) return tp.getName();
        return tp.getValue(currentLocaleItem);
      }
      
      public void setValueAt(Object value, int row, int col) {
        TranslatableProperty tp = keywordList.get(row);
        if (col==0) {
          tp.setName(value.toString());
          refresh(); // to order the entries
        }
        else {
          tp.setValue(currentLocaleItem,value.toString());
          fireTableCellUpdated(row, col);
          if (tp instanceof AddedTranslatableProperty && currentLocaleItem==LocaleItem.getDefaultItem()) { // default value of an added property has changed
//            System.out.println(this.getClass()+" setValueAt calling updateControlValues");
            ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
          }
        }
        changed = true;
      }
    };

    table = new JTable(tableModel) {
      public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if (c instanceof JComponent) {
          TranslatableProperty tp = keywordList.get(rowIndex);
          JComponent jc = (JComponent)c;
          jc.setToolTipText(tp.getTooltip());
          if (isCellEditable(rowIndex,vColIndex)) {
            jc.setBackground(Color.WHITE);
            jc.setForeground(Color.BLACK);
          }
          else { // non editables
            jc.setBackground(LIGHT_GRAY);
            jc.setForeground(Color.BLACK);
          }
        }
        return c;
      }
    };
    table.setShowGrid(true);
    table.setGridColor(Color.BLACK);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener (new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent lse) {
        if (!currentLocaleItem.isDefaultItem()) return;
        ListSelectionModel lsm = table.getSelectionModel();
        int selected = lsm.getMinSelectionIndex();
        if (selected<0) removeButton.setEnabled(false);
        else {
          TranslatableProperty tp = keywordList.get(selected);
          removeButton.setEnabled(tp.isNameEditable());
        }
      }
    });

    JScrollPane scrollPanel = new JScrollPane (table);
//    table.setFillsViewportHeight(true); This is 1.6!!!

    localeSelector = new LocaleSelector(this);

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) { dialog.setVisible (false); }
    });
    
    removeTranslationButton = new JButton (res.getString("TranslationEditor.RemoveTranslation"));
    removeTranslationButton.setEnabled(false);
    removeTranslationButton.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (currentLocaleItem.isDefaultItem()) return;
        localeSelector.removeLocaleItem(currentLocaleItem); // This sets the currentLocale to the default
//        int __option = JOptionPane.showConfirmDialog(removeTranslationButton,res.getString("UndoableOkToProceed"),
//            res.getString("Warning"), JOptionPane.YES_NO_OPTION);
//        if (__option==javax.swing.JOptionPane.YES_OPTION) {
//          for (TranslatableProperty tp : addedProperties) tp.setValue(currentLocaleItem, null);
//          ejs.getViewEditor().getTree().removeTranslation(currentLocaleItem);
//          localeSelector.removeLocaleItem(currentLocaleItem); // This sets the currentLocale to the default
//        }
      }
    });

    addButton = new JButton (res.getString("TranslationEditor.AddProperty"));
    addButton.setEnabled(true);
    addButton.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (!currentLocaleItem.isDefaultItem()) return;
        for (TranslatableProperty tp : addedProperties) {
          if (tp.getName().equals("")) {
            JOptionPane.showMessageDialog(addButton, res.getString("TranslationEditor.EmptyPropertyExists"),
              res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
            return;
          }
        }
        AddedTranslatableProperty tp = new AddedTranslatableProperty("");
        addedProperties.add(tp);
        keywordList.add(0, tp);
        tableModel.fireTableDataChanged();
//        refresh();
      }
    });

    removeButton = new JButton (res.getString("TranslationEditor.RemoveProperty"));
    removeButton.setEnabled(false);
    removeButton.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (!currentLocaleItem.isDefaultItem()) return;
        int row = table.getSelectedRow();
        if (row<0) return;
        int __option = JOptionPane.showConfirmDialog(removeButton,res.getString("UndoableOkToProceed"),
            res.getString("Warning"), JOptionPane.YES_NO_OPTION);
        if (__option==javax.swing.JOptionPane.YES_OPTION) {
          TranslatableProperty tp = keywordList.get(row);
          addedProperties.remove(tp);
          keywordList.remove(row);
          tableModel.fireTableDataChanged();
//          refresh();
        }
      }
    });

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonsPanel.add(okButton);
    buttonsPanel.add(addButton);
    buttonsPanel.add(removeButton);
    buttonsPanel.add(removeTranslationButton);
    
    JPanel localePanel = new JPanel(new BorderLayout());
    localePanel.add(localeSelector.getComponent(),BorderLayout.WEST);
    localePanel.setBorder(new javax.swing.border.EmptyBorder(0,5,0,0));
    
    dialog = new JDialog();
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(localePanel,BorderLayout.NORTH);
    dialog.getContentPane().add(scrollPanel,BorderLayout.CENTER);
    dialog.getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
    dialog.setResizable(true);
    dialog.setModal(false);
    Dimension size = res.getDimension("TranslationEditor.Size"); 
    table.getColumnModel().getColumn(0).setPreferredWidth(size.width/3);
    table.getColumnModel().getColumn(1).setPreferredWidth(2*size.width/3);
    dialog.setSize (size);
    dialog.validate();
    
    setFontLevel(FontSizer.getLevel());
  }

  public void setFontLevel(int level) { 
    FontSizer.setFonts(dialog, level);
    if (level>0) dialog.pack();
    else {
      Dimension size = res.getDimension("TranslationEditor.Size"); 
      table.getColumnModel().getColumn(0).setPreferredWidth(size.width/3);
      table.getColumnModel().getColumn(1).setPreferredWidth(2*size.width/3);
      dialog.setSize(size);      
    }
  }
  
  /**
   * Shows the requested locale, implementation of LocaleListener
   * @param _item
   */
  public void setLocaleItem(LocaleItem _item) {
    currentLocaleItem = _item;
    removeTranslationButton.setEnabled(!currentLocaleItem.isDefaultItem());
    addButton.setEnabled(currentLocaleItem.isDefaultItem());
    removeButton.setEnabled(false);
    table.getSelectionModel().clearSelection();
    tableModel.fireTableDataChanged();
  }

  public LocaleItem getLocaleItem() {
    return currentLocaleItem;
  }

  public LocaleSelector getLocaleSelector() { return this.localeSelector; }
  
  // --------------------------------------------------------------------
  // Added properties  
  // --------------------------------------------------------------------

//  /**
//   * Returns all available translations, both from the view and the user-added ones
//   */
//  public Set<LocaleItem> getAvailableTranslations() {
//    Set<LocaleItem> translations = ejs.getViewEditor().getTree().getAvailableTranslations();
//    for (AddedTranslatableProperty tp : addedProperties)  translations.addAll(tp.getAvailableLocaleItems());
//    return translations;
//  }
  
  /**
   * Returns the list of translations desired by the user
   */
  public java.util.List<LocaleItem> getDesiredTranslations() { 
    java.util.List<LocaleItem> list = new java.util.ArrayList<LocaleItem>();
    list.add(LocaleItem.getDefaultItem());
    for (LocaleItem item : localeSelector.getAvailableLocaleItems()) 
      if (!item.isDefaultItem()) list.add(item);
    return list;    
  }
  
  /**
   * Lists all translatable properties, both from the view and the user-added ones
   */
  private java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>(addedProperties);
    list.addAll(ejs.getViewEditor().getTree().getTranslatableProperties());
    list.addAll(ejs.getHtmlViewEditor().getTranslatableProperties());
    return list;
  }
  
  /**
   * Creates a string ready to save to file
   * @return
   */
  public String saveString () {
    StringBuffer buffer = new StringBuffer();
    java.util.List<LocaleItem> desiredTranslations = getDesiredTranslations();
    for (LocaleItem item : desiredTranslations) {
      if (item.isDefaultItem()) continue; // This is obvious
      buffer.append("<DesiredTranslation name=\""+item.getKeyword()+"\">");
      buffer.append("</DesiredTranslation>\n");
    }
    // Now all added properties
    for (AddedTranslatableProperty tp : addedProperties) {
      // See if there are desired translations for this added property
      StringBuffer tmpBuffer = new StringBuffer();
      for (LocaleItem item : desiredTranslations) {
        String value = tp.getValue(item);
        if (value!=null) {
          tmpBuffer.append("<Translation name=\""+ item.getKeyword()+"\">");
          tmpBuffer.append("<![CDATA["+value+"]]>");
          tmpBuffer.append("</Translation>\n");
        }
      }
      if (tmpBuffer.toString().length()>0) {
        buffer.append("<AddedProperty name=\""+ tp.getName()+"\">\n");
        buffer.append(tmpBuffer);
        buffer.append("</AddedProperty>\n");
      }
    }
    return buffer.toString();
  }
  
  /**
   * Reads the corresponding string as extracted from a file
   * @param _input
   */
  public void readString (String _input) {
    readDesiredTranslations(_input);
    boolean added = false;
    String propertyBlock = OsejsCommon.getPiece(_input,"<AddedProperty name=","</AddedProperty>",true);
    while (propertyBlock!=null) {
      added = true;
      String name = OsejsCommon.getPiece(_input,"<AddedProperty name=\"","\">",false);
      AddedTranslatableProperty tp = new AddedTranslatableProperty(name);
      readPropertyBlock(tp,propertyBlock);
      addedProperties.add(tp);
      _input = _input.substring(_input.indexOf("</AddedProperty>")+16);
      propertyBlock = OsejsCommon.getPiece(_input,"<AddedProperty name=","</AddedProperty>",true);
    }
    refresh();
//    System.out.println(this.getClass()+" calling updateControlValues");
    if (added) ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
  }

  private void readDesiredTranslations (String _input) {
    String desiredBlock = OsejsCommon.getPiece(_input,"<DesiredTranslation name=","</DesiredTranslation>",true);
    while (desiredBlock!=null) {
      String language = OsejsCommon.getPiece(_input,"<DesiredTranslation name=\"","\">",false);
      LocaleItem item = LocaleItem.getLocaleItem(language);
      if (item!=null) localeSelector.addLocaleItem(item);
      else ejs.print("Warning! Translation editor is ignoring unrecognized locale name <"+language+">\n");
      _input = _input.substring(_input.indexOf("</DesiredTranslation>")+21);
      desiredBlock = OsejsCommon.getPiece(_input,"<DesiredTranslation name=","</DesiredTranslation>",true);
    }
  }
  
  private void readPropertyBlock(AddedTranslatableProperty _tp, String _block) {
    String propertyLine = OsejsCommon.getPiece(_block,"<Translation name=","</Translation>",true);
    while (propertyLine!=null) {
      String language = OsejsCommon.getPiece(propertyLine,"<Translation name=\"","\">",false);
      LocaleItem item = LocaleItem.getLocaleItem(language);
      if (item!=null) {
        String value = OsejsCommon.getPiece(propertyLine,"<![CDATA[","]]></Translation>",false);
        _tp.setValue(item, value);
      }
      else ejs.print("Warning! Translation editor is ignoring unrecognized locale name <"+language+"> for property <"+ _tp.getName()+">\n");
      _block = _block.substring(_block.indexOf("</Translation>")+14);
      propertyLine = OsejsCommon.getPiece(_block,"<Translation name=","</Translation>",true);
    }
  }

  // --------------------------------------------------------------------
  // Normal operation
  // --------------------------------------------------------------------

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  /**
   * Refreshes an existing translation, probably because a view elementy has changed name,
   * a new user-added property has been added,
   * or a copy and paste from other examples may have added new elements or translations
   */
  public void refresh() {
    if (firstTime) return;
    keywordList.clear();
    keywordList.addAll(getTranslatableProperties());
    Collections.sort(keywordList);
    ListSelectionModel lsm = table.getSelectionModel();
    lsm.clearSelection();
    tableModel.fireTableDataChanged();
  }
  
  public void clear() {
    dialog.setVisible(false);
    currentLocaleItem = LocaleItem.getDefaultItem();
    localeSelector.clear();
    keywordList.clear();
    addedProperties.clear();
    removeTranslationButton.setEnabled(false);
    addButton.setEnabled(true);
    table.getSelectionModel().clearSelection();
    removeButton.setEnabled(false);
    firstTime = true;
  }

  public void setVisible(boolean _visible) {
    if (_visible) {
      if (firstTime) {
        currentLocaleItem = LocaleItem.getDefaultItem();
        firstTime = false;
        refresh();
        dialog.setLocationRelativeTo(ejs.getMainPanel());
      }
    }
    dialog.setVisible(_visible);
  }

  public void setActive(boolean _active) { table.setEnabled(_active); }

  public void setFont(Font font) { table.setFont(font); }

  public void setName(String _name) {
    dialog.setTitle(res.getString("TranslationEditor.Title")+" "+_name);
    firstTime = true;
  }
  
  /**
   * Returns a text with the resource file for this locale
   * @param _item
   * @return
   */
  public String getResources(LocaleItem _item) {
    StringBuffer buffer = new StringBuffer();
    for (TranslatableProperty trProp : getTranslatableProperties()) {
      buffer.append(trProp.getName()+"="+removeDoubleSlash(trProp.getValue(_item))+"\n");
    }
    return buffer.toString();
  }
  
  static private String removeDoubleSlash(String _value) {
    int l = _value.length();
    String txt = "";
    for (int i=0; i<l; i++) {
      char c = _value.charAt(i);
      if (c=='\\') {
        if (i+1<l) {
          char nextChar = _value.charAt(i+1);
          if (nextChar=='\\') { txt += "\\"; i++; }
          else if (nextChar=='\"') { txt += "\""; i++; }
          else txt += "\\";
        }
        else txt += "\\"; 
      }
      else txt += c;
    }
    return txt;
  }
  
  /**
     * Returns the default values of all added properties
     * @return
     */
    public Set<TwoStrings> getResourceDefaultPairs() {
        Set<TwoStrings> set = new HashSet<TwoStrings>();
        for (AddedTranslatableProperty tp : addedProperties) {
            set.add(new TwoStrings(tp.getName(),tp.getValue(LocaleItem.getDefaultItem())));
        }
        return set;

    }

    /**
     * Returns the selected locale values of all added properties
     * @param localeItem
     * @return
     */
    public Set<TwoStrings> getResourceLocalePairs(LocaleItem localeItem) {
        Set<TwoStrings> set = new HashSet<TwoStrings>();
        for (AddedTranslatableProperty tp : addedProperties) {
            set.add(new TwoStrings(tp.getName(),tp.getValue(localeItem)));
        }
        return set;

    }
  
}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
