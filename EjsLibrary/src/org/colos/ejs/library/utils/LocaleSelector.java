/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.colos.ejs.library.utils;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import org.colos.ejs.library.Simulation;

/**
 * A combobox used for displaying and selecting a LocaleItem
 * 
 * @author Francisco Esquembre
 * @author based on code by Douglas Brown
 * @version 1.0
 */
public class LocaleSelector {

  private LocaleListener listener;  // the listener for locale changes
  private JComboBox localeCB;
  private JComponent localePanel;
  private JLabel localeLabel;
  private Set<LocaleSelector> slaveSelectors = new HashSet<LocaleSelector>();
  private LocaleSelector masterSelector=null;
  private boolean isActive = true;

  public LocaleSelector (LocaleListener _listener) {
    listener = _listener;
    
    // ComboBox selector
    localeCB = new JComboBox();
    localeCB.addItem(LocaleItem.getDefaultItem());
    localeCB.setSelectedItem(LocaleItem.getDefaultItem());
    localeCB.setEditable(true);

    localeCB.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent e) {
        if (!isActive) {
          isActive = true;
          return;
        }
        Object selected = localeCB.getSelectedItem();
        if (selected==null) return;
        if (selected instanceof LocaleItem) {
          listener.setLocaleItem((LocaleItem) selected);
          return;
        }
        if (masterSelector!=null) return; // a slave cannot add locale items
        // Add a new Locale or find an existing one
        LocaleItem newItem = LocaleItem.getLocaleItem(selected.toString());
        if (newItem==null) { // The entered string is not valid
          JOptionPane.showMessageDialog(localeCB,Simulation.getEjsString("LocaleSelector.InvalidLocale")+" <"+selected.toString()+">", 
              Simulation.getEjsString("Error"),JOptionPane.ERROR_MESSAGE);
          localeCB.setSelectedItem(LocaleItem.getDefaultItem());
          return;
        }
        // See if the locale is already there
        for (int i = 0; i<localeCB.getItemCount(); i++) {
          LocaleItem next = (LocaleItem) localeCB.getItemAt(i);
          if (next.equals(newItem)) { localeCB.setSelectedIndex(i); return; }
        }
       // Add the new locale and select it
        addLocaleItem(newItem); 
        localeCB.setSelectedItem(newItem);
      }
    });

    localeLabel = new JLabel(Simulation.getEjsString("LocaleSelector.Language"), SwingConstants.RIGHT);
   
    JPanel fieldPanel = new JPanel(new BorderLayout());
    fieldPanel.add(localeLabel, BorderLayout.WEST);
    fieldPanel.add(localeCB, BorderLayout.CENTER);
    fieldPanel.validate();

    localePanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill   = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    localePanel.add(fieldPanel,gbc);
    
    //    localePanel.setPreferredSize(new Dimension(200,20));
  }
  
  /**
   * Makes sure to use the current locale for its labels
   */
  public void refreshGUI(LocaleItem _item) {
    localeLabel.setText(Simulation.getEjsString("LocaleSelector.Language"));
    int n = localeCB.getItemCount();
    for (int i = 0; i<n; i++) {
      LocaleItem next = (LocaleItem) localeCB.getItemAt(i);
      if (_item.equals(next)) {
        isActive = false;
        localeCB.setSelectedIndex(i);
        return;
      }
    }
  }

  /**
   * Setting a master for a selector makes it unable to add or remove locales. 
   * Only the master can add or remove locale items.
   * But the slave can select a locale independently of the master.
   * @param _selector
   */
  public void setMasterSelector(LocaleSelector _selector) {
    if (masterSelector!=null) masterSelector.slaveSelectors.remove(this);
    this.masterSelector = _selector;
    if (masterSelector!=null) {
//      System.out.println("Making "+this+" slave of "+masterSelector);
      masterSelector.slaveSelectors.add(this);
      localeCB.setEditable(false);
      for (LocaleItem item : masterSelector.getAvailableLocaleItems()) {
        addLocaleItem(item);
      }
    }
    else localeCB.setEditable(true);
  }
  
  /**
   * Allows editing the locale
   * @param editable
   */
  public void setEditable (boolean editable) {
    localeCB.setEditable(editable);
  }
  
  /**
   * The component that holds it
   * @return
   */
  public JComponent getComponent() { return localePanel; }
  
  public void setEnabled(boolean _enabled) { this.localeCB.setEnabled(_enabled); }
  
  /**
   * Sets the color of the label
   * @param _color
   */
  public void setColor (Color _color) { localeLabel.setForeground(_color); }
  
  /**
   * Clears the selector (except from the default item)
   */
  public void clear() {
    localeCB.removeAllItems();
    localeCB.addItem(LocaleItem.getDefaultItem());
    localeCB.setSelectedItem(LocaleItem.getDefaultItem());
    for (LocaleSelector slave : slaveSelectors) slave.clear();
  }
  
  /**
   * Returns the set of used LocaleItem 
   * @return
   */
   public Set<LocaleItem> getAvailableLocaleItems() {
     Set<LocaleItem> set = new HashSet<LocaleItem> ();
     for (int i = 0, n = localeCB.getItemCount(); i<n; i++) set.add((LocaleItem) localeCB.getItemAt(i));
     return set; 
   }

  /**
   * Adds a locale item to the selector
   * @param _item
   * @return
   */
  public LocaleItem addLocaleItem(LocaleItem _item) {
    if (_item.isDefaultItem()) return LocaleItem.getDefaultItem();
    boolean done = false;
    for (int i = 1, n = localeCB.getItemCount(); i<n; i++) {
      LocaleItem next = (LocaleItem) localeCB.getItemAt(i);
      if (_item.equals(next)) { // if already there, do not add it
        done = true;
        break;
      }
      if (_item.compareTo(next)<0) {
        localeCB.insertItemAt(_item, i);
        done = true;
        break;
      }
    }
    if (!done) localeCB.addItem(_item);
    for (LocaleSelector slave : slaveSelectors) slave.addLocaleItem(_item);
    return _item;
  }
  
  /**
   * Removes the locale item from the list of choices
   * @param _item
   */
  public void removeLocaleItem(LocaleItem _item) {
    if (_item.isDefaultItem()) return; // The default item cannot be removed
    boolean wasSelected = _item.equals(localeCB.getSelectedItem());
    for (int i = 1, n = localeCB.getItemCount(); i<n; i++) {
      LocaleItem next = (LocaleItem) localeCB.getItemAt(i);
      if (_item.equals(next)) {
        localeCB.removeItemAt(i);
        for (LocaleSelector slave : slaveSelectors) slave.removeLocaleItem(_item);
        if (wasSelected) setLocaleItem(LocaleItem.getDefaultItem());
        return;
      }
    }
  }

  /**
   * Sets a given locale item from the list of choices
   * @param _item
   */
  public void setLocaleItem(LocaleItem _item) {
    int n = localeCB.getItemCount();
    for (int i = 0; i<n; i++) {
      LocaleItem next = (LocaleItem) localeCB.getItemAt(i);
      if (_item.equals(next)) {
        localeCB.setSelectedIndex(i);
        listener.setLocaleItem(_item);
        return;
      }
    }
    // If not found, ignored
    System.out.println ("Warning! : Locale "+_item+" NOT found");
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
