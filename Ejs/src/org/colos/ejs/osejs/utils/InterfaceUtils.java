/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.colos.ejs.library.control.ConstantParserUtil;

/**
 * Some utility functions
 */
public class InterfaceUtils {

  static private final ResourceUtil res    = new ResourceUtil("Resources");

  /**
   * Make all components in the a set the same dimension
   * @param componentSet
   */
  static public void makeSameDimension (java.util.Set<JComponent> componentSet) {
    int maxWidth = 0, maxHeight=0;
    for (JComponent component : componentSet) {
      maxWidth  = Math.max(maxWidth,  component.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, component.getPreferredSize().height);
    }
    Dimension dim = new Dimension (maxWidth,maxHeight);
    for (JComponent label : componentSet) label.setPreferredSize(dim);
  }

  static public Font font (Font _currentFont, String _value) {
    Font font = ConstantParserUtil.fontConstant(_currentFont,_value);
    if (font==null) return _currentFont;
    return font;
  }

  static public Color color (String _value) {
    if (_value==null) return Color.black;
    Color color = ConstantParserUtil.colorConstant(_value);
    if (color==null) return Color.black;
    return color;
  }

  static public void showTempDialog (final JDialog _tmpDialog, String _message, final Thread _thread) {
    JLabel label = new JLabel (_message);
    label.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
    label.setFont(label.getFont().deriveFont(14f));

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { 
        _tmpDialog.setVisible(false);
        _thread.interrupt(); 
      }
    });
    JPanel panel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    panel.add(cancelButton);

    _tmpDialog.getContentPane().setLayout(new BorderLayout());
    _tmpDialog.getContentPane().add(label,BorderLayout.CENTER);
    _tmpDialog.getContentPane().add(panel,BorderLayout.SOUTH);
    _tmpDialog.pack();

    Window owner = _tmpDialog.getOwner();
    if (owner!=null){
      Point loc = owner.getLocation();
      Dimension size = owner.getSize();
      Dimension mysize = _tmpDialog.getSize();
      _tmpDialog.setLocation(loc.x+(size.width-mysize.width)/2,loc.y+(size.height-mysize.height)/2);
    }
  }

  static private Font getFont(JComponent component) {
    if (component instanceof JTextPane) {
      JTextPane textPane = (JTextPane) component;
      return textPane.getStyledDocument().getFont(textPane.getInputAttributes());
    }
    return component.getFont();
  }
  
  static public void increaseFont(JComponent component, int delta) {
    if (delta==0) return;
    Font font = getFont(component);
//    if (component instanceof JTextPane) System.out.println ("Font for "+component.getName()+" has size "+font.getSize());
    if (delta>0) font = font.deriveFont((float) font.getSize()+delta);
    else font = font.deriveFont((float) Math.max(-delta,font.getSize()+delta));
//    if (component instanceof JTextPane) System.out.println ("Font for "+component.getName()+" will change to size "+font.getSize());
    if (component instanceof JTextPane) setJTextPaneFont((JTextPane) component, font);
    else component.setFont(font);
  }

  /**
   * Utility method for setting the font and color of a JTextPane. The
   * result is roughly equivalent to calling setFont(...) and 
   * setForeground(...) on an AWT TextArea.
   */
  public static void setJTextPaneFont(final JTextPane textPane, final Font font) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { 
        StyledDocument doc = textPane.getStyledDocument();
        MutableAttributeSet attrs = textPane.getInputAttributes();
        
        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
//      StyleConstants.setFontFamily(attrs, font.getFamily());
//    System.out.println ("Setting size to "+font.getSize());
        StyleConstants.setFontSize(attrs, font.getSize());
//      StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
//      StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
        
        // Set the font color
        //StyleConstants.setForeground(attrs, c);
        
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
//     System.out.println ("Font  has now size "+getFont(textPane).getSize());
      }
    });

  }
  
  static public JComponent createZoomPanel (final JComponent compList[], final int delta, final ActionListener listener) {
    EmptyBorder border = new EmptyBorder(0,0,0,0);
    JLabel increaseFontButton = new JLabel (org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/zoomIn.gif"));
    increaseFontButton.setBorder(border);
    increaseFontButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent _evt) {
        for (JComponent comp : compList) increaseFont(comp, delta);
        if (listener!=null) listener.actionPerformed(null);
      }
    });

    JLabel decreaseFontButton = new JLabel (org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/zoomOut.gif"));
    decreaseFontButton.setBorder(border);
    decreaseFontButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent _evt) {
        for (JComponent comp : compList) increaseFont(comp, -delta);
        if (listener!=null) listener.actionPerformed(null);
      }
    });

    JPanel zoomPanel = new JPanel(new GridLayout(1,2));
    zoomPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
    zoomPanel.add(decreaseFontButton);
    zoomPanel.add(increaseFontButton);
    return zoomPanel;
  }

} // end of class
