/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.colos.ejs.osejs.utils.ResourceUtil;
import org.opensourcephysics.tools.FontSizer;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.Osejs;
import java.awt.event.MouseAdapter;
import java.awt.font.TextAttribute;

public class SearchDialog {
  private Osejs ejs;
  private JDialog dialog;
  private JList<Object> list;
  private DefaultListModel<Object> listModel;
  private JTextField searchField;
  private JCheckBox caseCheckbox,descCB, modelCB, viewCB, htmlViewCB;

  static private ResourceUtil res = new ResourceUtil("Resources");

  public SearchDialog (Osejs _ejs, JComponent _parentComponent) {
    ejs = _ejs;
    MouseAdapter mouseListener =  new MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (evt.getSource()==list) {
          if (evt.getClickCount()>1) ((SearchResult) list.getSelectedValue()).show();
          return;
        }
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("close")) dialog.setVisible(false);
        else if (aCmd.equals("search")) doTheSearch();
      }
    };

    JButton closeButton = new JButton (res.getString("SearchDialog.Close"));
    closeButton.setActionCommand ("close");
    closeButton.addMouseListener (mouseListener);

    JButton searchButton = new JButton (res.getString("SearchDialog.Search"));
    searchButton.setActionCommand ("search");
    searchButton.addMouseListener (mouseListener);

    JLabel partsLabel = new JLabel (res.getString("SearchMenu.SearchIn"));
    partsLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));

    descCB = new JCheckBox (res.getString("Osejs.Main.Description"),false);
    modelCB = new JCheckBox (res.getString("Osejs.Main.Model"),true);
    viewCB = new JCheckBox (res.getString("Osejs.Main.View"),true);
    htmlViewCB = new JCheckBox (res.getString("Osejs.Main.HtmlView"),true);
    Box partsPanel = Box.createHorizontalBox(); // new JPanel (new FlowLayout(FlowLayout.LEFT));
    partsPanel.add (descCB);
    partsPanel.add (modelCB);
    if (ejs.supportsJava()) partsPanel.add (viewCB);
    if (ejs.supportsHtml()) partsPanel.add (htmlViewCB);

    JPanel partsFinalPanel = new JPanel(new BorderLayout());
    partsFinalPanel.add(partsLabel,BorderLayout.WEST);
    partsFinalPanel.add(partsPanel,BorderLayout.CENTER);

    JPanel buttonGridPanel = new JPanel (new GridLayout(1,0));
    buttonGridPanel.add (searchButton);
    buttonGridPanel.add (closeButton);

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(partsFinalPanel,BorderLayout.CENTER);
    buttonPanel.add(buttonGridPanel,BorderLayout.EAST);

    JLabel searchLabel = new JLabel (res.getString("SearchMenu.StringToSearch"));
    searchLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));

    searchField = new JTextField ();
    searchField.addActionListener (new ActionListener() {
      public void actionPerformed (ActionEvent _e) { doTheSearch(); }
    });

    caseCheckbox = new JCheckBox (res.getString("SearchMenu.CaseSensitive"),false);

    JPanel searchPanel = new JPanel (new BorderLayout());
    searchPanel.add (searchLabel,BorderLayout.WEST);
    searchPanel.add (searchField,BorderLayout.CENTER);
    searchPanel.add (caseCheckbox,BorderLayout.EAST);


    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.add(searchPanel,BorderLayout.NORTH);
    bottomPanel.add(buttonPanel,BorderLayout.SOUTH);

    listModel = new DefaultListModel<Object>();
    list = new JList<Object>(listModel);
    list.addMouseListener(mouseListener);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setFont(ejs.getCurrentFont());

    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize (res.getDimension("SearchDialog.Size"));

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.setTitle(res.getString("SearchDialog.Title"));
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (scrollPane,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);
    dialog.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e) { searchField.requestFocusInWindow(); }
    });

    dialog.validate();
    dialog.pack();
    dialog.setModal(false);
    dialog.setLocationRelativeTo (_parentComponent);
    setFontLevel(FontSizer.getLevel());
  }

  private void setFontLevel(int level) { 
    FontSizer.setFonts(dialog, level);
    dialog.pack();
  }
  
  public void setFont(java.awt.Font aFont) {
    Integer underline = TextAttribute.UNDERLINE_ON;
    HashMap<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
    map.put(TextAttribute.FOREGROUND, Color.BLUE);
    map.put(TextAttribute.UNDERLINE, underline);
    Font f = aFont.deriveFont(map);
    list.setFont(f);
  }

  public void show () {
    dialog.setVisible (true);
    searchField.requestFocusInWindow();
  }

  public void clear() {
    listModel.clear();
    searchField.setText("");
  }
  
  private void doTheSearch() {
    String searchStr = searchField.getText().trim();
    if (searchStr.length()<=0) return;
    int mode = SearchResult.CASE_INSENSITIVE;
    if (caseCheckbox.isSelected()) mode = SearchResult.CASE_SENSITIVE;
    int searchIn = 0;
    if (descCB.isSelected()) searchIn = searchIn | SearchResult.SEARCH_DESCRIPTION;
    if (modelCB.isSelected()) searchIn = searchIn | SearchResult.SEARCH_MODEL;
    if (ejs.supportsJava() && viewCB.isSelected()) searchIn = searchIn | SearchResult.SEARCH_VIEW;
    if (ejs.supportsHtml() && htmlViewCB.isSelected()) searchIn = searchIn | SearchResult.SEARCH_HTML_VIEW;
    java.util.List<SearchResult> foundList = ejs.search(searchStr,mode,searchIn);
    listModel.clear();
    if (foundList.size()<=0) listModel.addElement("String not found");
    else for (Iterator<SearchResult> it = foundList.iterator(); it.hasNext(); ) listModel.addElement(it.next());
  }

}
