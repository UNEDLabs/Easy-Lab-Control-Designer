/**
 * The html package contains generic tools to view and edit HTML pages
 * Copyright (c) August 2010 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * @version August 2010
 */

package org.colos.ejs.osejs.edition.html;

import java.awt.*;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.event.*;
import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.tools.FontSizer;

import com.hexidec.ekit.EkitCore;

/**
 * This class uses an EkitCore object to display and edit an HTML
 * @author Paco
 *
 */
class BasicEditor implements HtmlComponent {
  static protected ResourceUtil res = new ResourceUtil ("Resources");

  static private String baseText=""; // "<html><head><link type=\"textPane/css\" rel=\"stylesheet\" href=\"_library/css/ejss.css\"></head><body> </body></html>";

  private HtmlEditor htmlEditor;
  private EkitCore ekitCore;
  private JPanel topPanel, mainPanel;

  BasicEditor (HtmlEditor _editor) {
    htmlEditor = _editor;
    // Create and configure the EkitCore editor
    ekitCore=new EkitCore(htmlEditor.getEjs());
    ekitCore.getTextPane().requestFocus();
    ekitCore.getTextPane().setText(baseText);
    if (htmlEditor.getFont()!=null) ekitCore.getTextPane().setFont(htmlEditor.getFont());
    ekitCore.getTextPane().getDocument().addDocumentListener(new DocumentListener(){
      public void changedUpdate(DocumentEvent e) { htmlEditor.setChanged(true); }
      public void insertUpdate(DocumentEvent e)  { htmlEditor.setChanged(true); }
      public void removeUpdate(DocumentEvent e)  { htmlEditor.setChanged(true); }
    });
    // Report changes if resized. This is necessary because description pages need to have the (last) size of this panel
    ekitCore.getTextPane().addComponentListener(new java.awt.event.ComponentAdapter(){
      public void componentResized(java.awt.event.ComponentEvent _evt) {htmlEditor.setChanged(true); }
    });
    
    //custom menuBar
    Vector<String> vcMenus = new Vector<String>();
    vcMenus.add(EkitCore.KEY_MENU_FILE);
    vcMenus.add(EkitCore.KEY_MENU_EDIT);
//    vcMenus.add(EkitCore.KEY_MENU_VIEW);
    vcMenus.add(EkitCore.KEY_MENU_FONT);
    vcMenus.add(EkitCore.KEY_MENU_FORMAT);
    vcMenus.add(EkitCore.KEY_MENU_INSERT);
    vcMenus.add(EkitCore.KEY_MENU_TABLE);
    vcMenus.add(EkitCore.KEY_MENU_FORMS);
    vcMenus.add(EkitCore.KEY_MENU_SEARCH);
    vcMenus.add(EkitCore.KEY_MENU_TOOLS);
    vcMenus.add(EkitCore.KEY_MENU_HELP);
    JMenuBar menubar=ekitCore.getCustomMenuBar(vcMenus);

    // custom toolBar
    Vector<String> vcTools = new Vector<String>();
//    vcTools.add(EkitCore.KEY_TOOL_NEW);
    vcTools.add(EkitCore.KEY_TOOL_SOURCE);
    vcTools.add(EkitCore.KEY_TOOL_SEP);

    vcTools.add(EkitCore.KEY_TOOL_CUT);
    vcTools.add(EkitCore.KEY_TOOL_COPY);
    vcTools.add(EkitCore.KEY_TOOL_PASTE);
//    vcTools.add(EkitCore.KEY_TOOL_SEP);

    vcTools.add(EkitCore.KEY_TOOL_UNDO);
    vcTools.add(EkitCore.KEY_TOOL_REDO);
    vcTools.add(EkitCore.KEY_TOOL_SEP);

    vcTools.add(EkitCore.KEY_TOOL_UNICODE);
    vcTools.add(EkitCore.KEY_TOOL_UNIMATH);
    vcTools.add(EkitCore.KEY_TOOL_ANCHOR);
    vcTools.add(EkitCore.KEY_TOOL_SEP);

    vcTools.add(EkitCore.KEY_TOOL_BOLD);
    vcTools.add(EkitCore.KEY_TOOL_ITALIC);
    vcTools.add(EkitCore.KEY_TOOL_UNDERLINE);
    vcTools.add(EkitCore.KEY_TOOL_STRIKE);
    vcTools.add(EkitCore.KEY_TOOL_SUPER);
    vcTools.add(EkitCore.KEY_TOOL_SUB);
    vcTools.add(EkitCore.KEY_TOOL_ULIST);
    vcTools.add(EkitCore.KEY_TOOL_OLIST);
    vcTools.add(EkitCore.KEY_TOOL_SEP);

    vcTools.add(EkitCore.KEY_TOOL_ALIGNL);
    vcTools.add(EkitCore.KEY_TOOL_ALIGNC);
    vcTools.add(EkitCore.KEY_TOOL_ALIGNR);
    vcTools.add(EkitCore.KEY_TOOL_ALIGNJ);

//    vcTools.add(EkitCore.KEY_TOOL_CLEAR);
//    vcTools.add(EkitCore.KEY_TOOL_STYLES);

    // Build the panel
    topPanel = new JPanel (new GridLayout(2,1));
    topPanel.add(menubar);
    JToolBar taskbar = ekitCore.customizeToolBar(EkitCore.TOOLBAR_MAIN, vcTools, true);
    topPanel.add(taskbar);

    mainPanel = new JPanel (new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add (ekitCore,BorderLayout.CENTER);
    
    FontSizer.setFonts(taskbar, FontSizer.getLevel());
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(topPanel, level);
  }
  
  // ------------------------------
  // Basic operation
  // ------------------------------

  public Component getComponent () { return mainPanel; }

  public JTextComponent getTextComponent () { return ekitCore.getTextPane(); }
  
  public void setFont (Font _font) { ekitCore.getTextPane().setFont(_font); }

  public void show(int _position) {
    ekitCore.getTextPane().setEditable(true);
    topPanel.setVisible(true);
    ekitCore.getTextPane().setCaretPosition(_position);
  }

  public void setHtml (String _code) {
    ekitCore.getTextPane().setText(_code); 
    ekitCore.getTextPane().setCaretPosition (0); 
  }
  
  public String getHtml () {
    if (!SwingUtilities.isEventDispatchThread()) try {
      SwingUtilities.invokeAndWait(new Runnable() { // 080512 Paco did that because the computer hanged when getting the textPane...
        public void run () { 
          ekitCore.refreshOnUpdate();
        }
      });
    } catch (Exception exc) {}
    return ekitCore.getTextPane().getText();
  }

  public void setEditable(boolean _editable) { 
    ekitCore.getTextPane().setEditable(_editable);
    topPanel.setVisible(_editable);
  }
  
  public void switchEditable() { 
    setEditable(!ekitCore.getTextPane().isEditable()); 
  }

  public void clear () { ekitCore.getTextPane().setText(baseText); }
  
  public boolean isEmpty () { return getHtml().trim().length()<=0; }

  public void refreshCss () {
    ekitCore.refreshCss(htmlEditor.getEjs());
  }
} // end of class
