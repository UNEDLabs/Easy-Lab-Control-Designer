package test;

/*
* Open Source Physics software is free software as described near the bottom of this code file.
*
* For additional information and documentation on Open Source Physics please see:
* <http://www.opensourcephysics.org/>
*/

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;

public class FontSizerTest extends JFrame {

   JDialog nonsenseDialog;

   FontSizerTest() {

       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   JPanel panel = new JPanel();
   setContentPane(panel);

   String path = "/org/opensourcephysics/resources/media/images/play.gif"; //$NON-NLS-1$
    // the following method returns a ResizableIcon
   Icon playIcon = ResourceLoader.getIcon(path);
   JButton playButton = new JButton("Click to show dialog", playIcon);
   panel.add(playButton);
   playButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               // JOptionPane dialogs are automatically resized
               int result = JOptionPane.showConfirmDialog(FontSizerTest.this, "Choose yes to show another dialog");
               if (result==JOptionPane.YES_OPTION) {
                   nonsenseDialog.setVisible(true);
               }
           }
   });

   JLabel label = new JLabel("Choose a font level:");
   panel.add(label);

   // combobox to set font size level
   final JComboBox<String> dropdown = new JComboBox<String>();
   panel.add(dropdown);
   dropdown.addItem("0");
   dropdown.addItem("1");
   dropdown.addItem("2");
   dropdown.addItem("3");
   dropdown.addItem("4");
   dropdown.addItem("5");
   dropdown.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               int level = Integer.valueOf(dropdown.getSelectedItem().toString());
               // set new font level--FontSizer will fire propertyChangeEvent to listeners
               FontSizer.setLevel(level);
           }
   });

   // add listener to get events from FontSizer
   FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() {
     public void propertyChange(PropertyChangeEvent e) {
         int level = ((Integer) e.getNewValue()).intValue();
         setFontLevel(level);
     }
   });

   pack();
   // center on screen
   Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
   int x = (dim.width-getBounds().width)/2;
   int y = (dim.height-getBounds().height)/2;
   setLocation(x, y);

   // this dialog is not resized automatically--see the setFontLevel() method below
   nonsenseDialog = new JDialog(this, true);
   JPanel content = new JPanel();
   nonsenseDialog.setContentPane(content);
   content.add(new JLabel("nonsense"));
   nonsenseDialog.setSize(300, 100);
   // center nonsenseDialog on screen
   x = (dim.width-nonsenseDialog.getBounds().width)/2;
   y = (dim.height-nonsenseDialog.getBounds().height)/2;
   nonsenseDialog.setLocation(x, y);

   }

   void setFontLevel(int level) {
       FontSizer.setFonts(this, level);
       pack();
       // nonsenseDialog must also be resized here since it is not in the container hierarchy
       FontSizer.setFonts(nonsenseDialog, level);
   }

 public static void main(String[] args) {
   new FontSizerTest().setVisible(true);
 }

}

/*
* Open Source Physics software is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public License (GPL) as
* published by the Free Software Foundation; either version 2 of the License,
* or(at your option) any later version.

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