/**
 * Copyright (c) February 2006 F. Esquembre
 * Last updated: April 2008
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import org.colos.ejs.control.editors.EditorForFile;
import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.html.HtmlEditor;
import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.tools.minijar.PathAndFile;

import org.colos.ejs.control.editors.EditorForString;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.INFORMATION;
import org.colos.ejss.xml.SimulationXML.MODEL;

public class SimInfoEditor {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");
  static private final String TITLE = res.getString("SimInfoEditor.InfoFor").trim()+" ";
  static private final int COLUMNS = res.getInteger("SimInfoEditor.FieldWidth");
  static private final String sBASIC_MANIFEST="Permissions: sandbox\nCodebase: *\nCaller-Allowable-Codebase: *\n";

  private boolean changed=false, editable=true;
  private JTextComponent titleField, levelField, copyrightField, keyField, passwordField, abstractField, languageField, execPasswordField, manifestField;
  private JComboBox<String> auxFilesCB, detectedAuxFilesCB; // base64ImagesCB;
  private JFrame editorDialog = null;
  private Osejs ejs;
  
  private JButton jarsAddButton, jarsEditButton, jarsRemoveButton;
  private JButton importsEditButton, importsRemoveButton;
  private JButton classesEditButton, classesRemoveButton;
//  private JButton authorAddButton, authorEditButton, authorLogoEditButton, authorRemoveButton;
  private JButton removeAuxFilesB; // removeBase64ImagesB;
  private JComboBox<String> jarsCombo, importsCombo, logoCombo, authorCombo, classesCombo;
  private JCheckBox translatorToolCB, dataToolCB, captureToolCB,interpreterCB, macMenuBarCB, /*CJB*/ appletColCB, 
    browserFirstCB, deltaEquationCB, fixedNavbarCB, pauseOnPageExitCB; //, asHtmlCB;
  private JTabbedPane tabbedPanel;
  private String[] importStatements = new String[] { "java.lang.*" };
//  private JComboBox<TwoStrings> htmlViewMenuCombo;
  private JTextComponent modelTabField, cssField, modelTabTitleField, modelNameField, headField;
  private JButton logoButton, authorLogoButton;
  private JLabel authorLogoLabel;
  private ArrayList<String> authorLogoList = new ArrayList<String>();

  private JComboBox<String> staticImagesCombo;
  private JButton staticImagesAddButton, staticImagesRemoveButton;
  
  public boolean hasAuthor() {
    return getAuthor().trim().length()>0;
  }
  
  private String getAuthor() {
    StringBuffer buffer = new StringBuffer();
    boolean firstAuthor=true;
    for (int i=0, n=authorCombo.getItemCount(); i<n; i++) {
      String author = authorCombo.getItemAt(i);
      if (author.trim().length()>0) {
        if (firstAuthor) buffer.append(author);
        else buffer.append("; "+author);
        firstAuthor = false;
      }
    }
    return buffer.toString();
//    return authorField.getText().trim(); 
  }

  public String getAbstract() {
    return abstractField.getText();
  }

  public String getCopyright() {
    return copyrightField.getText();
  }

  // ----------------------------------------------
  // Static method required to build the interface
  // ----------------------------------------------
  
  static final javax.swing.border.EmptyBorder BORDER_BOTTOM = new javax.swing.border.EmptyBorder(4,2,0,2);
  static final javax.swing.border.EmptyBorder BORDER_STD = new javax.swing.border.EmptyBorder(0,0,0,0);
  
  public static JPanel makeLabel (String _label, Set<JComponent> _labelSet) {
    JLabel label = new JLabel(res.getString(_label), SwingConstants.RIGHT);
    //label.setPreferredSize(LABEL_SIZE);
    if (_labelSet!=null) _labelSet.add(label);
    label.setBorder(BORDER_BOTTOM);
    JPanel toppanel = new JPanel(new BorderLayout());
    toppanel.add(label, BorderLayout.NORTH);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BORDER_STD);
    panel.add(toppanel, BorderLayout.WEST);
    return panel;
  }

  public static JTextComponent makeField (Container _parent, DocumentListener _dl, int _lines, int _cols) {
    if (_lines<2) {
      JTextField field = new JTextField(_cols);
      if (_dl!=null) field.getDocument().addDocumentListener(_dl);
      _parent.add(field, BorderLayout.CENTER);
      return field;
    }
    JTextArea area = new JTextArea(_lines, _cols);
    if (_dl!=null) area.getDocument().addDocumentListener(_dl);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane (area);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
    panel.add(scroll, BorderLayout.CENTER);
    //scroll.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    _parent.add(panel, BorderLayout.CENTER);
    return area;
  }

  /**
   * Constructor
   * @param _ejs
   * @param _button
   */
  public SimInfoEditor (Osejs _ejs) {
    ejs = _ejs;

    DocumentListener dl = new DocumentListener() {
      public void changedUpdate (DocumentEvent evt) { changed = true; }
      public void insertUpdate (DocumentEvent evt)  { changed = true; }
      public void removeUpdate (DocumentEvent evt)  { changed = true; }
    };

    Set<JComponent> labelSet = new HashSet<JComponent>();
    Set<JComponent> labelSet2 = new HashSet<JComponent>();
    Set<JComponent> labelSet3 = new HashSet<JComponent>();

    Insets nullInset = new Insets(0,0,0,0);

    ActionListener comboListener = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (cmd.equals("deleteLogo") && logoCombo.getItemCount()>0) {
          logoCombo.removeItemAt(logoCombo.getSelectedIndex());
//          logoEditButton.setEnabled(logoCombo.getItemCount()>0);
//          logoRemoveButton.setEnabled(logoCombo.getItemCount()>0);
          showLogo();
          changed = true;
        }
        else if (cmd.equals("addLogo") || cmd.equals("editLogo")) {
          String filename =  org.colos.ejs.control.editors.EditorForFile.edit (ejs,logoCombo, "Logo");
          if (filename==null) return;
          if (!filename.toLowerCase().endsWith(".png")) {
            JOptionPane.showMessageDialog(logoCombo, 
                res.getString("SimInfoEditor.RequiredPNGimage")+"\n"+filename, 
                res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
            return;
          }
          if (cmd.equals("editLogo") && logoCombo.getSelectedIndex()>=0) {
            int index = logoCombo.getSelectedIndex();
            logoCombo.removeItemAt(index);
            logoCombo.insertItemAt(filename, index);
          }
          else logoCombo.addItem(filename);
          logoCombo.setSelectedItem(filename);
//          logoEditButton.setEnabled(true);
//          logoRemoveButton.setEnabled(true);
          showLogo();
          changed = true;
        }
        
        else if (cmd.equals("deleteAuthor") && authorCombo.getItemCount()>1) {
          int index = authorCombo.getSelectedIndex();
          if (index>=0) {
            authorCombo.removeItemAt(index);
            authorLogoList.remove(index);
            authorCombo.setSelectedIndex(Math.max(0, index-1));
            showAuthorLogo();
            changed = true;
          }
        }
        else if (cmd.equals("editAuthor")) {
          int index = authorCombo.getSelectedIndex();
          if (index<0) return;
          String author = EditorForString.edit(res.getString("SimInfoEditor.Author"),authorCombo,authorCombo.getItemAt(index).toString());
          if (author!=null && author.trim().length()>0) {
            authorCombo.removeItemAt(index);
            authorCombo.insertItemAt(author, index);
            authorCombo.setSelectedIndex(index);
            showAuthorLogo();
            changed = true;
          }
        }
        else if (cmd.equals("addAuthor")) {
          String author = EditorForString.edit(res.getString("SimInfoEditor.Author"),authorCombo,"");
          if (author!=null && author.trim().length()>0) {
            authorCombo.addItem(author);
            authorLogoList.add("");
            authorCombo.setSelectedItem(author);
            showAuthorLogo();
            changed = true;
          }
        }
        else if (cmd.equals("editAuthorLogo")) {
          int index = authorCombo.getSelectedIndex();
          if (index<0) return;
          String filename =  org.colos.ejs.control.editors.EditorForFile.edit (ejs,authorCombo, "Logo");
          if (filename==null) return;
          if (!filename.toLowerCase().endsWith(".png")) {
            JOptionPane.showMessageDialog(authorCombo, 
                res.getString("SimInfoEditor.RequiredPNGimage")+"\n"+filename, 
                res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
            return;
          }
          authorLogoList.set(index, filename);
          authorCombo.setSelectedIndex(index);
          showAuthorLogo();
          changed = true;
        }

        else if (cmd.equals("deleteJar") && jarsCombo.getItemCount()>0) {
          jarsCombo.removeItemAt(jarsCombo.getSelectedIndex());
          jarsEditButton.setEnabled(jarsCombo.getItemCount()>0);
          jarsRemoveButton.setEnabled(jarsCombo.getItemCount()>0);
          changed = true;
        }
        else if (cmd.equals("addJar") || cmd.equals("editJar")) {
          String[] files =  EditorForFile.edit (ejs,jarsAddButton,"library",true,javax.swing.JFileChooser.FILES_ONLY);
          if (files!=null) {
            if (cmd.equals("editJar")) jarsCombo.removeItem(jarsCombo.getSelectedItem());
            for (int i=0; i<files.length;i++) addToJarsCombo(files[i]);
            if (files.length>0) jarsCombo.setSelectedItem(files[0]); // Make the first one visible
            jarsEditButton.setEnabled(true);
            jarsRemoveButton.setEnabled(true);
            changed = true;
          }
        }

        else if (cmd.equals("deleteImport") && importsCombo.getItemCount()>0) {
          importsCombo.removeItemAt(importsCombo.getSelectedIndex());
          importsEditButton.setEnabled(importsCombo.getItemCount()>0);
          importsRemoveButton.setEnabled(importsCombo.getItemCount()>0);
          updateImportStatements();
          ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
          changed = true;
        }
        else if (cmd.equals("addImport") || cmd.equals("editImport")) {
          String newImport="";
//          if (cmd.equals("editImport")) newImport = JOptionPane.showInputDialog(importsAddButton,res.getString("Model.Library.ImportsEdit"),importsCombo.getSelectedItem());
//          else newImport = JOptionPane.showInputDialog(importsAddButton,res.getString("Model.Library.NewImport"));

          if (cmd.equals("editImport")) newImport = EditorForString.edit(res.getString("Model.Library.ImportsEdit"),importsCombo,importsCombo.getSelectedItem().toString());
          else newImport = EditorForString.edit(res.getString("Model.Library.NewImport"),importsCombo,"");
          
          if (newImport!=null && newImport.trim().length()>0) {
            if (cmd.equals("editImport")) importsCombo.removeItem(importsCombo.getSelectedItem());
            importsCombo.addItem(newImport);
            importsCombo.setSelectedItem(newImport);
            importsEditButton.setEnabled(true);
            importsRemoveButton.setEnabled(true);
            updateImportStatements();
            System.out.println(this.getClass()+" calling updateControlValues");

            ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
            changed = true;
          }
        } 

        else if (cmd.equals("deleteClass") && classesCombo.getItemCount()>0) {
          classesCombo.removeItemAt(classesCombo.getSelectedIndex());
          classesEditButton.setEnabled(classesCombo.getItemCount()>0);
          classesRemoveButton.setEnabled(classesCombo.getItemCount()>0);
          changed = true;
        }
        else if (cmd.equals("addClass") || cmd.equals("editClass")) {
          String newClass="";
          if (cmd.equals("editClass")) newClass = EditorForString.edit(res.getString("Model.Library.RequiredClassEdit"),classesCombo,classesCombo.getSelectedItem().toString());
          else newClass = EditorForString.edit(res.getString("Model.Library.RequiredClassAdd"),classesCombo,"");
          if (newClass!=null && newClass.trim().length()>0) {
            if (cmd.equals("editClass")) classesCombo.removeItem(classesCombo.getSelectedItem());
            classesCombo.addItem(newClass);
            classesCombo.setSelectedItem(newClass);
            classesEditButton.setEnabled(true);
            classesRemoveButton.setEnabled(true);
            changed = true;
          }
        }

        

      }
    };
    
    JPanel titlePanel = makeLabel ("SimInfoEditor.Title",labelSet);
    titleField = makeField (titlePanel,dl,1,COLUMNS);
    titleField.setText(ejs.getCurrentXMLFilename());
//    titleField.getDocument().addDocumentListener(new DocumentListener() {
//      private void setLogoText() {
//        String text = titleField.getText().trim();
//        if (text.length()<=0) text = ejs.getCurrentXMLFilename();
//        titleLogoLabel.setText(text);
//      }
//      public void changedUpdate (DocumentEvent evt) { setLogoText(); }
//      public void insertUpdate (DocumentEvent evt)  { setLogoText(); }
//      public void removeUpdate (DocumentEvent evt)  { setLogoText(); }
//    });

    JPanel copyrightPanel = makeLabel ("SimInfoEditor.Copyright",labelSet);
    copyrightField = makeField (copyrightPanel,dl,1,COLUMNS);
    
    JPanel keyPanel = makeLabel ("SimInfoEditor.Keywords",labelSet);
    keyField = makeField (keyPanel,dl,1,COLUMNS);

    JPanel passwordPanel = makeLabel ("SimInfoEditor.Password",null);
    passwordField = makeField (passwordPanel,dl,1,COLUMNS);

    JPanel execPasswordPanel = makeLabel ("SimInfoEditor.ExecutionPassword",null);
    execPasswordField = makeField (execPasswordPanel,dl,1,COLUMNS);

    JPanel levelPanel = makeLabel ("SimInfoEditor.Level",labelSet);
    levelField = makeField (levelPanel,dl,1,COLUMNS);

    JPanel languagePanel = makeLabel ("SimInfoEditor.Language",labelSet);
    languageField = makeField (languagePanel,dl,1,COLUMNS);

    JPanel abstractPanel = makeLabel ("SimInfoEditor.Abstract",labelSet);
    abstractField = makeField (abstractPanel,dl,7,COLUMNS);

    // ---------------------------
    // logos
    // ---------------------------
    
    logoCombo = new JComboBox<String>();
    logoCombo.setEditable(false);
    logoCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { showLogo(); }
    });

    JButton logoAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    logoAddButton.setToolTipText(res.getString("Add"));
    logoAddButton.setActionCommand("addLogo");
    logoAddButton.addActionListener(comboListener);
    logoAddButton.setMargin(nullInset);

//    JButton logoEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
//    logoEditButton.setToolTipText(res.getString("Edit"));
//    logoEditButton.setActionCommand("editLogo");
//    logoEditButton.addActionListener(comboListener);
//    logoEditButton.setMargin(nullInset);
//    logoEditButton.setEnabled(false);

    JButton logoRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    logoRemoveButton.setToolTipText(res.getString("Remove"));
    logoRemoveButton.setActionCommand("deleteLogo");
    logoRemoveButton.addActionListener(comboListener);
    logoRemoveButton.setMargin(nullInset);
//    logoRemoveButton.setEnabled(false);
 
    logoButton = new JButton();
    logoButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    logoButton.setHorizontalAlignment(JLabel.CENTER);
    logoButton.setBackground(Color.WHITE);
    logoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    logoButton.setPreferredSize(new Dimension(320,180));
    logoButton.setToolTipText(res.getString("Edit"));
    logoButton.setActionCommand("editLogo");
    logoButton.addActionListener(comboListener);
    logoButton.setMargin(nullInset);

    JLabel logoRecommendedSize = new JLabel(res.getString("SimInfoEditor.RecommendedSize")+ " (320 x 180)");
    logoRecommendedSize.setFont(logoRecommendedSize.getFont().deriveFont(Font.ITALIC));

    JPanel logoButtonsPanel = new JPanel (new GridLayout(1,0));
    logoButtonsPanel.add(logoAddButton);
//    logoButtonsPanel.add(logoEditButton);
    logoButtonsPanel.add(logoRemoveButton);
    
    JPanel logoComboPanel = new JPanel(new BorderLayout());
    logoComboPanel.add(logoCombo,BorderLayout.CENTER);
    logoComboPanel.add(logoButtonsPanel,BorderLayout.EAST);
    
    JPanel logoImagePanel = new JPanel(new BorderLayout());
    logoImagePanel.add(logoButton, BorderLayout.WEST);
    logoImagePanel.add(logoRecommendedSize,BorderLayout.SOUTH);
    
    JPanel logoCenterPanel = new JPanel(new BorderLayout());
    logoCenterPanel.add(logoComboPanel, BorderLayout.NORTH);
    logoCenterPanel.add(logoImagePanel, BorderLayout.CENTER);
    
    JPanel logoPanel = makeLabel ("SimInfoEditor.SimulationLogo",labelSet);
    logoPanel.add(logoCenterPanel, BorderLayout.CENTER);

    // ---------------------------
    // author logos
    // ---------------------------
    
    authorLogoLabel = new JLabel("");
    authorLogoLabel.setPreferredSize(new Dimension(197,25));
    authorLogoLabel.setBorder(new EmptyBorder(0,3,0,0));
    authorLogoLabel.setVerticalAlignment(SwingConstants.TOP);

    authorLogoButton = new JButton();
    authorLogoButton.setHorizontalAlignment(JLabel.CENTER);
    authorLogoButton.setBackground(Color.WHITE);
    authorLogoButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    authorLogoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    authorLogoButton.setPreferredSize(new Dimension(50,50));
    authorLogoButton.setToolTipText(res.getString("Edit"));
    authorLogoButton.setActionCommand("editAuthorLogo");
    authorLogoButton.addActionListener(comboListener);
    authorLogoButton.setMargin(nullInset);
    authorLogoButton.setEnabled(true);

    authorCombo = new JComboBox<String>();
    authorCombo.setEditable(false);
    authorCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) { showAuthorLogo(); }
    });

    JButton authorAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    authorAddButton.setToolTipText(res.getString("Add"));
    authorAddButton.setActionCommand("addAuthor");
    authorAddButton.addActionListener(comboListener);
    authorAddButton.setMargin(nullInset);

    JButton authorEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
    authorEditButton.setToolTipText(res.getString("Edit"));
    authorEditButton.setActionCommand("editAuthor");
    authorEditButton.addActionListener(comboListener);
    authorEditButton.setMargin(nullInset);

    JButton authorRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    authorRemoveButton.setToolTipText(res.getString("Remove"));
    authorRemoveButton.setActionCommand("deleteAuthor");
    authorRemoveButton.addActionListener(comboListener);
    authorRemoveButton.setMargin(nullInset);
//    authorRemoveButton.setEnabled(false);
 
    JPanel authorButtonsPanel = new JPanel (new GridLayout(1,0));
    authorButtonsPanel.add(authorAddButton);
    authorButtonsPanel.add(authorEditButton);
    authorButtonsPanel.add(authorRemoveButton);
    
    JPanel authorComboPanel = new JPanel(new BorderLayout());
    authorComboPanel.add(authorCombo,BorderLayout.CENTER);
    authorComboPanel.add(authorButtonsPanel,BorderLayout.EAST);
    
//    JButton authorLogoEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
//    authorLogoEditButton.setToolTipText(res.getString("Edit"));
//    authorLogoEditButton.setActionCommand("editAuthorLogo");
//    authorLogoEditButton.addActionListener(comboListener);
//    authorLogoEditButton.setMargin(nullInset);
//    authorLogoEditButton.setEnabled(true);
    
    JLabel authorLogoRecommendedSize = new JLabel(res.getString("SimInfoEditor.RecommendedSize")+ " (50 x 50)");
    authorLogoRecommendedSize.setFont(authorLogoRecommendedSize.getFont().deriveFont(Font.ITALIC));

    JPanel authorInfoPanel = new JPanel(new BorderLayout());
    authorInfoPanel.add(authorLogoButton, BorderLayout.WEST);
    authorInfoPanel.add(authorLogoLabel, BorderLayout.CENTER);
//    authorInfoPanel.add(authorLogoEditButton,BorderLayout.EAST);
    authorInfoPanel.add(authorLogoRecommendedSize,BorderLayout.SOUTH);

    JPanel authorCenterPanel = new JPanel(new BorderLayout());
    authorCenterPanel.add(authorComboPanel, BorderLayout.NORTH);
    authorCenterPanel.add(authorInfoPanel, BorderLayout.SOUTH);
    
    JPanel authorPanel = makeLabel ("SimInfoEditor.Author",labelSet);
    authorPanel.add(authorCenterPanel, BorderLayout.CENTER);
        
    setAuthors(null);

    // ---------------------------
    // Run options
    // ---------------------------


    detectedAuxFilesCB = new JComboBox<String>();
    detectedAuxFilesCB.setEditable(false);

    JButton detectedRefreshB = new JButton (ResourceLoader.getIcon(sysRes.getString("SimInfoEditor.Refresh.Icon")));
    detectedRefreshB.setToolTipText(res.getString("SimInfoEditor.FindAuxiliaryFiles"));
    detectedRefreshB.setRequestFocusEnabled(false);
    detectedRefreshB.setMargin(nullInset);
    detectedRefreshB.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent _evt) { detectAuxiliaryFiles(false); }
    });
    
    JPanel detectedPanel = makeLabel ("SimInfoEditor.AuxiliaryFiles",labelSet2);
    detectedPanel.add(detectedAuxFilesCB,BorderLayout.CENTER);
    detectedPanel.add(detectedRefreshB,BorderLayout.EAST);

    JPanel headPanel = makeLabel ("SimInfoEditor.Head",labelSet2);
    headField = makeField (headPanel,dl,7,COLUMNS);      
    
    // ---------------------------

    auxFilesCB = new JComboBox<String>();
    auxFilesCB.setEditable(false);
    
    JButton addAuxFilesB = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon")));
    addAuxFilesB.setToolTipText(res.getString("SimInfoEditor.AddAuxiliaryFile"));
    addAuxFilesB.setRequestFocusEnabled(false);
    addAuxFilesB.setMargin(nullInset);
    addAuxFilesB.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        String[] files =  org.colos.ejs.control.editors.EditorForFile.edit (ejs,auxFilesCB,null,true,javax.swing.JFileChooser.FILES_AND_DIRECTORIES); // true = multipleFiles + allowDirs
        if (files==null) return;
        for (int i=0; i<files.length; i++) {
          auxFilesCB.removeItem(files[i]); // So that not to repeat
          auxFilesCB.addItem(files[i]);
        }
        if (files.length>0) auxFilesCB.setSelectedItem(files[0]); // Make the last one visible
        removeAuxFilesB.setEnabled(editable);
        changed = true;
      }
    });

    removeAuxFilesB = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon")));
    removeAuxFilesB.setToolTipText(res.getString("SimInfoEditor.RemoveAuxiliaryFile"));
    removeAuxFilesB.setRequestFocusEnabled(false);
    removeAuxFilesB.setMargin(nullInset);
    removeAuxFilesB.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        auxFilesCB.removeItemAt(auxFilesCB.getSelectedIndex());
        removeAuxFilesB.setEnabled(editable && auxFilesCB.getItemCount()>0);
        changed = true;
      }
    });
    removeAuxFilesB.setEnabled(false);

    JPanel auxFilesButtonsPanel = new JPanel (new GridLayout(1,0));
    auxFilesButtonsPanel.add(addAuxFilesB);
    auxFilesButtonsPanel.add(removeAuxFilesB);
    
    JPanel auxPanel = makeLabel ("SimInfoEditor.UserAdded",labelSet2);
    auxPanel.add(auxFilesCB,BorderLayout.CENTER);
    auxPanel.add(auxFilesButtonsPanel,BorderLayout.EAST);

    // ---------------------------------
    
    staticImagesCombo = new JComboBox<String>();
    staticImagesCombo.setEditable(false);

    staticImagesAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    staticImagesAddButton.setToolTipText(res.getString("Add"));
    addAuxFilesB.setRequestFocusEnabled(false);
    staticImagesAddButton.setMargin(nullInset);
    staticImagesAddButton.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        String[] files =  EditorForFile.edit (ejs,staticImagesAddButton,"image",true,javax.swing.JFileChooser.FILES_ONLY);
        if (files==null) return;
        for (int i=0; i<files.length; i++) {
          staticImagesCombo.removeItem(files[i]); // So that not to repeat
          staticImagesCombo.addItem(files[i]);
        }
        if (files.length>0) staticImagesCombo.setSelectedItem(files[0]); // Make the last one visible
        staticImagesRemoveButton.setEnabled(editable);
        detectAuxiliaryFiles(false);
        changed = true;
      }
    });
    
    staticImagesRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    staticImagesRemoveButton.setToolTipText(res.getString("Remove"));
    staticImagesRemoveButton.setRequestFocusEnabled(false);
    staticImagesRemoveButton.setMargin(nullInset);
    staticImagesRemoveButton.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        staticImagesCombo.removeItemAt(staticImagesCombo.getSelectedIndex());
        staticImagesRemoveButton.setEnabled(editable && staticImagesCombo.getItemCount()>0);
        detectAuxiliaryFiles(false);
        changed = true;
      }
    });
    staticImagesRemoveButton.setEnabled(false);
    
    JPanel staticImagesButtonsPanel = new JPanel (new GridLayout(1,0));
    staticImagesButtonsPanel.add(staticImagesAddButton);
    staticImagesButtonsPanel.add(staticImagesRemoveButton);
    
    JPanel staticImagesPanel = makeLabel ("SimInfoEditor.StaticImages",labelSet2);
    staticImagesPanel.add(staticImagesCombo,BorderLayout.CENTER);
    staticImagesPanel.add(staticImagesButtonsPanel,BorderLayout.EAST);


    // --------------------------
    
    /*
    base64ImagesCB = new JComboBox<String>();
    base64ImagesCB.setEditable(false);
    
    JButton addBase64FilesB = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon")));
    addBase64FilesB.setToolTipText(res.getString("SimInfoEditor.AddBase64File"));
    addBase64FilesB.setRequestFocusEnabled(false);
    addBase64FilesB.setMargin(nullInset);
    addBase64FilesB.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        String[] files =  org.colos.ejs.control.editors.EditorForFile.edit (ejs,base64ImagesCB,null,true,javax.swing.JFileChooser.FILES_AND_DIRECTORIES); // true = multipleFiles + allowDirs
        if (files==null) return;
        for (int i=0; i<files.length; i++) {
          base64ImagesCB.removeItem(files[i]); // So that not to repeat
          base64ImagesCB.addItem(files[i]);
        }
        if (files.length>0) base64ImagesCB.setSelectedItem(files[0]); // Make the last one visible
        removeBase64ImagesB.setEnabled(editable);
        changed = true;
      }
    });

    removeBase64ImagesB = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon")));
    removeBase64ImagesB.setToolTipText(res.getString("SimInfoEditor.RemoveBase64File"));
    removeBase64ImagesB.setRequestFocusEnabled(false);
    removeBase64ImagesB.setMargin(nullInset);
    removeBase64ImagesB.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        base64ImagesCB.removeItemAt(base64ImagesCB.getSelectedIndex());
        removeBase64ImagesB.setEnabled(editable && base64ImagesCB.getItemCount()>0);
        changed = true;
      }
    });
    removeBase64ImagesB.setEnabled(false);

    JPanel base64FilesButtonsPanel = new JPanel (new GridLayout(1,0));
    base64FilesButtonsPanel.add(addBase64FilesB);
    base64FilesButtonsPanel.add(removeBase64ImagesB);
    
    JPanel base64Panel = makeLabel ("SimInfoEditor.Base64Images",labelSet2);
    base64Panel.add(base64ImagesCB,BorderLayout.CENTER);
    base64Panel.add(base64FilesButtonsPanel,BorderLayout.EAST);
*/
    // ----------------------------------
    // The bottom line at the 'Custom' panel that allows to add libraries to the class path
    
    jarsCombo = new JComboBox<String>();
    jarsCombo.setEditable(false);

    jarsAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    jarsAddButton.setToolTipText(res.getString("Model.Library.JarsAdd"));
    jarsAddButton.setActionCommand("addJar");
    jarsAddButton.addActionListener(comboListener);
    jarsAddButton.setMargin(nullInset);

    jarsEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
    jarsEditButton.setToolTipText(res.getString("Model.Library.JarsEdit"));
    jarsEditButton.setActionCommand("editJar");
    jarsEditButton.addActionListener(comboListener);
    jarsEditButton.setMargin(nullInset);
    jarsEditButton.setEnabled(false);

    jarsRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    jarsRemoveButton.setToolTipText(res.getString("Model.Library.JarsRemove"));
    jarsRemoveButton.setActionCommand("deleteJar");
    jarsRemoveButton.addActionListener(comboListener);
    jarsRemoveButton.setMargin(nullInset);
    jarsRemoveButton.setEnabled(false);
 
    JPanel jarsButtonsPanel = new JPanel (new GridLayout(1,0));
    jarsButtonsPanel.add(jarsAddButton);
    jarsButtonsPanel.add(jarsEditButton);
    jarsButtonsPanel.add(jarsRemoveButton);
    
    JPanel jarsPanel = makeLabel ("Model.Library.JarsLabel",labelSet2);
    jarsPanel.add(jarsCombo,BorderLayout.CENTER);
    jarsPanel.add(jarsButtonsPanel,BorderLayout.EAST);

    // -----------------------------------
    // The line that allows to add imports to the code

    importsCombo = new JComboBox<String>();
    importsCombo.setEditable(false);

    JButton importsAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    importsAddButton.setToolTipText(res.getString("Model.Library.ImportsAdd"));
    importsAddButton.setActionCommand("addImport");
    importsAddButton.addActionListener(comboListener);
    importsAddButton.setMargin(nullInset);

    importsEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
    importsEditButton.setToolTipText(res.getString("Model.Library.ImportsEdit"));
    importsEditButton.setActionCommand("editImport");
    importsEditButton.addActionListener(comboListener);
    importsEditButton.setMargin(nullInset);
    importsEditButton.setEnabled(false);

    importsRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    importsRemoveButton.setToolTipText(res.getString("Model.Library.ImportsRemove"));
    importsRemoveButton.setActionCommand("deleteImport");
    importsRemoveButton.addActionListener(comboListener);
    importsRemoveButton.setMargin(nullInset);
    importsRemoveButton.setEnabled(false);

    JPanel importsButtonsPanel = new JPanel (new GridLayout(1,0));
    importsButtonsPanel.add(importsAddButton);
    importsButtonsPanel.add(importsEditButton);
    importsButtonsPanel.add(importsRemoveButton);
    
    JPanel importsPanel = makeLabel ("Model.Library.ImportsLabel",labelSet2);
    importsPanel.add(importsCombo,BorderLayout.CENTER);
    importsPanel.add(importsButtonsPanel,BorderLayout.EAST);
    
    // -----------------------------------
    // The line that allows to add class files to be included in packages

    classesCombo = new JComboBox<String>();
    classesCombo.setEditable(false);

    JButton classesAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon"))); // " + ");
    classesAddButton.setToolTipText(res.getString("Model.Library.RequiredClassAdd"));
    classesAddButton.setActionCommand("addClass");
    classesAddButton.addActionListener(comboListener);
    classesAddButton.setMargin(nullInset);

    classesEditButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
    classesEditButton.setToolTipText(res.getString("Model.Library.RequiredClassEdit"));
    classesEditButton.setActionCommand("editClass");
    classesEditButton.addActionListener(comboListener);
    classesEditButton.setMargin(nullInset);
    classesEditButton.setEnabled(false);

    classesRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    classesRemoveButton.setToolTipText(res.getString("Remove"));
    classesRemoveButton.setActionCommand("deleteClass");
    classesRemoveButton.addActionListener(comboListener);
    classesRemoveButton.setMargin(nullInset);
    classesRemoveButton.setEnabled(false);

    JPanel classesButtonsPanel = new JPanel (new GridLayout(1,0));
    classesButtonsPanel.add(classesAddButton);
    classesButtonsPanel.add(classesEditButton);
    classesButtonsPanel.add(classesRemoveButton);
    
    JPanel classesPanel = makeLabel ("Model.Library.RequiredClassesLabel",labelSet2);
    classesPanel.add(classesCombo,BorderLayout.CENTER);
    classesPanel.add(classesButtonsPanel,BorderLayout.EAST);
    
    // -----------------------------------
    // The field for extra entries in the manifest file

    JPanel manifestPanel = makeLabel ("SimInfoEditor.Manifest",null);
    manifestField = makeField (manifestPanel,dl,7,COLUMNS);
    manifestField.setText(sBASIC_MANIFEST);

    // ---------------------------
    // Tools
    // ---------------------------

    java.awt.event.ItemListener itemListener = new java.awt.event.ItemListener() {
      public void itemStateChanged (java.awt.event.ItemEvent _e) { changed = true; }
    };

    dataToolCB  = new JCheckBox(res.getString("EjsOptions.AddDataTools"), false);
    dataToolCB.setRequestFocusEnabled(false);
    dataToolCB.addItemListener(itemListener);

    translatorToolCB  = new JCheckBox(res.getString("EjsOptions.AddTranslatorTool"), false);
    translatorToolCB.setRequestFocusEnabled(false);
    translatorToolCB.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged (java.awt.event.ItemEvent _e) {
        if (!translatorToolCB.isSelected()) ejs.getTranslationEditor().setVisible(false);
        Vector<Editor> pageList = ejs.getDescriptionEditor().getPages();
        for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
          HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
          htmlEditor.setAllowChangeLocale(translatorToolCB.isSelected());
        }
        changed = true; 
      }
    });

    captureToolCB  = new JCheckBox(res.getString("EjsOptions.AddCaptureTools"), false);
    captureToolCB.setRequestFocusEnabled(false);
    captureToolCB.addItemListener(itemListener);

    interpreterCB  = new JCheckBox(res.getString("EjsOptions.UseInterpreter"), true);
    interpreterCB.setRequestFocusEnabled(false);
    interpreterCB.addItemListener(itemListener);
    
    deltaEquationCB = new JCheckBox(res.getString("EjsOptions.ShowDeltaForODEs"), false);
    deltaEquationCB.setRequestFocusEnabled(false);
    deltaEquationCB.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged (java.awt.event.ItemEvent _e) { 
        changed = true;
        ejs.getModelEditor().setAlgebraBasedEquations(deltaEquationCB.isSelected());
      }
    });

    //CJB for collaborative
    appletColCB = new JCheckBox(res.getString("EjsOptions.appletColEnabled"), false);
    appletColCB.setRequestFocusEnabled(false);
    appletColCB.addItemListener(itemListener);
    
    macMenuBarCB = new JCheckBox(res.getString("EjsOptions.MacScreenMenuBar"), false);
    macMenuBarCB.setRequestFocusEnabled(false);
    macMenuBarCB.addItemListener(itemListener);
    
    browserFirstCB = new JCheckBox(res.getString("EjsOptions.UseBrowserFirstGlobal"), false);
    browserFirstCB.setRequestFocusEnabled(false);
    browserFirstCB.addItemListener(itemListener);
    
    JPanel browserFirstPanel = new JPanel(new BorderLayout());
    browserFirstPanel.setBorder(BORDER_STD);
    browserFirstPanel.add(browserFirstCB, BorderLayout.WEST);
    browserFirstPanel.add(new JLabel("         "+res.getString("EjsOptions.UseBrowserFirstGlobalLine2")), BorderLayout.SOUTH);

    pauseOnPageExitCB = new JCheckBox(res.getString("EjsOptions.PauseOnPageExit"), false);
    pauseOnPageExitCB.setRequestFocusEnabled(false);
    pauseOnPageExitCB.addItemListener(itemListener);
    
    JPanel pauseOnPageExitPanel = new JPanel(new BorderLayout());
    pauseOnPageExitPanel.setBorder(BORDER_STD);
    pauseOnPageExitPanel.add(pauseOnPageExitCB, BorderLayout.WEST);

    fixedNavbarCB = new JCheckBox(res.getString("EjsOptions.FixedNavigationBar"), false);
    fixedNavbarCB.setRequestFocusEnabled(false);
    fixedNavbarCB.addItemListener(itemListener);

    JPanel fixedNavbarPanel = new JPanel(new BorderLayout());
    fixedNavbarPanel.setBorder(BORDER_STD);
    fixedNavbarPanel.add(fixedNavbarCB, BorderLayout.WEST);

//    asHtmlCB = new JCheckBox(res.getString("EjsOptions.GenerateAsHTML"), false);
//    asHtmlCB.setRequestFocusEnabled(false);
//    asHtmlCB.addItemListener(itemListener);
//
//    JPanel asHtmlPanel = new JPanel(new BorderLayout());
//    asHtmlPanel.setBorder(BORDER_STD);
//    asHtmlPanel.add(asHtmlCB, BorderLayout.WEST);

    // ---------------------------
    // Position of the HTML pages menu on the reader
    // ---------------------------

//    htmlViewMenuCombo = new JComboBox<TwoStrings>();
//    htmlViewMenuCombo.addItem(new TwoStrings(res.getString("SimInfoEditor.MenuPosition.TOP"),"TOP"));
//    htmlViewMenuCombo.addItem(new TwoStrings(res.getString("SimInfoEditor.MenuPosition.BOTTOM"),"BOTTOM"));
//    htmlViewMenuCombo.addItem(new TwoStrings(res.getString("SimInfoEditor.MenuPosition.LEFT"),"LEFT"));
//    htmlViewMenuCombo.addItem(new TwoStrings(res.getString("SimInfoEditor.MenuPosition.RIGHT"),"RIGHT"));
//    htmlViewMenuCombo.setSelectedIndex(0); // TOP by default
//    
//    JPanel htmlViewMenuWestPanel = makeLabel ("SimInfoEditor.MenuPosition",null);
//    htmlViewMenuWestPanel.add(htmlViewMenuCombo,BorderLayout.CENTER);
//
//    JPanel htmlViewMenuPanel = new JPanel(new BorderLayout());
//    htmlViewMenuPanel.setBorder(BORDER_STD);
//    htmlViewMenuPanel.add(htmlViewMenuWestPanel, BorderLayout.WEST);

    // ---------------------------
    // index of the model tab in the HTML Reader
    // ---------------------------

    JPanel modelTabWestPanel = makeLabel ("SimInfoEditor.ModelTab",labelSet2);
    modelTabField = makeField (modelTabWestPanel,dl,1,3);
    if (modelTabField instanceof JTextField) ((JTextField) modelTabField).setColumns(5);
    
    JPanel modelTabPanel = new JPanel(new BorderLayout());
    modelTabPanel.setBorder(BORDER_STD);
    modelTabPanel.add(modelTabWestPanel, BorderLayout.WEST);
    
    // ---------------------------
    // title of the model tab in the HTML Reader
    // ---------------------------

//    JPanel modelTabTitleWestPanel = makeLabel ("SimInfoEditor.ModelTabTitle",labelSet2);
//    modelTabTitleField = makeField (modelTabTitleWestPanel,dl,1,3);
//    if (modelTabTitleField instanceof JTextField) ((JTextField) modelTabTitleField).setColumns(5);
//    
//    JPanel modelTabTitlePanel = new JPanel(new BorderLayout());
//    modelTabTitlePanel.setBorder(BORDER_STD);
//    modelTabTitlePanel.add(modelTabTitleWestPanel, BorderLayout.WEST);
    
    JPanel modelTabTitlePanel = makeLabel ("SimInfoEditor.ModelTabTitle",labelSet2);
    modelTabTitleField = makeField (modelTabTitlePanel,dl,1,3);
    modelTabTitlePanel.setBorder(BORDER_STD);

    JPanel modelNamePanel = makeLabel ("SimInfoEditor.ModelName",labelSet2);
    modelNameField = makeField (modelNamePanel,dl,1,3);
    modelNamePanel.setBorder(BORDER_STD);

    // -------------------
    // CSS folder

    JButton cssButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon"))); // " + ");
    cssButton.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        String folderName =  org.colos.ejs.control.editors.EditorForFile.edit (ejs,cssField,"css",res.getString("SimInfoEditor.CSSFile"),javax.swing.JFileChooser.FILES_ONLY);
        if (folderName==null) return;
        if (folderName.trim().length()>0) cssField.setText(folderName);
        else cssField.setText("");
        detectAuxiliaryFiles(false);
//        changed = true;
//        ejs.getHtmlViewEditor().refreshEmulator();
//        ejs.getDescriptionEditor().refreshCss();
      }
    });
    JButton cssClearButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon"))); // " + ");
    cssClearButton.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) {
        cssField.setText("");
        detectAuxiliaryFiles(false);
//        changed = true;
//        ejs.getHtmlViewEditor().refreshEmulator();
//        ejs.getDescriptionEditor().refreshCss();
      }
    });

    JPanel cssButtonsPanel = new JPanel (new GridLayout(1,0));
    cssButtonsPanel.add(cssButton);
    cssButtonsPanel.add(cssClearButton);

    JPanel cssFolderWestPanel = makeLabel ("SimInfoEditor.CSSFile",labelSet2);
    cssField = makeField (cssFolderWestPanel,dl,1,3);
    cssField.setEditable(false);
    cssField.getDocument().addDocumentListener(new DocumentListener(){
      public void insertUpdate(DocumentEvent e) {
        changed = true;
        ejs.getHtmlViewEditor().refreshEmulator();
        ejs.getDescriptionEditor().refreshCss();
//        System.err.println("Inserted");        
      }
      public void removeUpdate(DocumentEvent e) {
        changed = true;
        ejs.getHtmlViewEditor().refreshEmulator();
        ejs.getDescriptionEditor().refreshCss();
//        System.err.println("Removed");        
      }
      public void changedUpdate(DocumentEvent e) {
        changed = true;
        ejs.getHtmlViewEditor().refreshEmulator();
        ejs.getDescriptionEditor().refreshCss();
//        System.err.println("Cahnged");
      }
      
    });

    cssFolderWestPanel.add(cssButtonsPanel,BorderLayout.EAST);
    
    // -----------------------------------
    
    // Final adjustments
    // ---------------------------------
    
    // Make all labels in the first set the same dimension
    InterfaceUtils.makeSameDimension(labelSet);
    InterfaceUtils.makeSameDimension(labelSet2);
    InterfaceUtils.makeSameDimension(labelSet3);

//    int maxWidth = 0, maxHeight=0;
//    for (JComponent label : labelSet) {
//      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
//      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
//    }
//    Dimension dim = new Dimension (maxWidth,maxHeight);
//    for (JComponent label : labelSet) label.setPreferredSize(dim);

    // Make all labels in the second set the same dimension
//    maxWidth = 0;
//    maxHeight=0;
//    for (JComponent label : labelSet2) {
//      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
//      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
//    }
//    dim = new Dimension (maxWidth,maxHeight);
//    for (JComponent label : labelSet2) label.setPreferredSize(dim);

    // Make all labels in the second set the same dimension
//    maxWidth = 0;
//    maxHeight=0;
//    for (JComponent label : labelSet3) {
//      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
//      maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
//    }
//    dim = new Dimension (maxWidth,maxHeight);
//    for (JComponent label : labelSet3) label.setPreferredSize(dim);

    Box infoPanel = Box.createVerticalBox();
    infoPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    infoPanel.add(titlePanel);
    infoPanel.add(authorPanel);
    infoPanel.add(copyrightPanel);
    infoPanel.add(keyPanel);
    infoPanel.add(levelPanel);
    infoPanel.add(languagePanel);

    Box logosPanel = Box.createVerticalBox();
    logosPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    logosPanel.add(logoPanel);

//    infoPanel.add(abstractPanel);

    JPanel metadataTopPanel = new JPanel (new BorderLayout());
    metadataTopPanel.add(infoPanel,BorderLayout.NORTH);
    metadataTopPanel.add(abstractPanel,BorderLayout.CENTER);
    metadataTopPanel.add(logosPanel,BorderLayout.SOUTH);

    JPanel javaToolsPanel=new JPanel (new java.awt.GridLayout(0,1));
    javaToolsPanel.setBorder(new EmptyBorder(5,5,10,0));
    javaToolsPanel.add(captureToolCB);
    javaToolsPanel.add(dataToolCB);
    javaToolsPanel.add(translatorToolCB);
    javaToolsPanel.add(appletColCB); //CJB for collaborative
    javaToolsPanel.add(macMenuBarCB);

    Box runOptionsPanel = Box.createVerticalBox();
    runOptionsPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    if (_ejs.supportsHtml()) {
      runOptionsPanel.add(browserFirstPanel);
      runOptionsPanel.add(pauseOnPageExitPanel);
//      runOptionsPanel.add(asHtmlPanel);
      runOptionsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
      
//      runOptionsPanel.add(htmlViewMenuPanel);
      runOptionsPanel.add(modelTabPanel);
      runOptionsPanel.add(modelTabTitlePanel);
      runOptionsPanel.add(modelNamePanel);
      runOptionsPanel.add(fixedNavbarPanel);
//      runOptionsPanel.add(htmlViewMenuPanel);
    }
    if (_ejs.supportsJava()) {
      runOptionsPanel.add(execPasswordPanel);
      runOptionsPanel.add(javaToolsPanel);
      runOptionsPanel.add(importsPanel);
      runOptionsPanel.add(jarsPanel);
      runOptionsPanel.add(classesPanel);
    }
    runOptionsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
    runOptionsPanel.add(cssFolderWestPanel);
    runOptionsPanel.add(auxPanel);
    if (_ejs.supportsHtml()) runOptionsPanel.add(staticImagesPanel);
//    runOptionsPanel.add(base64Panel);
    runOptionsPanel.add(detectedPanel);
    
    if (_ejs.supportsHtml()) {
      runOptionsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));    
      runOptionsPanel.add(headPanel);
    }
    
    JPanel runOptionsTopPanel = new JPanel (new BorderLayout());
    runOptionsTopPanel.add(runOptionsPanel,BorderLayout.NORTH);

    if (_ejs.supportsJava()) {
      runOptionsTopPanel.add(manifestPanel, BorderLayout.CENTER);
    }

    JPanel editionOptionsPanel=new JPanel (new java.awt.GridLayout(0,1));
    editionOptionsPanel.setBorder(new EmptyBorder(5,5,10,0));
    editionOptionsPanel.add(interpreterCB);
    editionOptionsPanel.add(deltaEquationCB);

    Box editionPanel = Box.createVerticalBox();
    editionPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
    editionPanel.add(passwordPanel);
    editionPanel.add(editionOptionsPanel);

    JPanel editionTopPanel = new JPanel (new BorderLayout());
    editionTopPanel.add(editionPanel,BorderLayout.NORTH);

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) { editorDialog.setVisible (false); }
    });

    tabbedPanel = new JTabbedPane(SwingConstants.TOP);
    tabbedPanel.add(res.getString("SimInfoEditor.Metadata"),metadataTopPanel);
    tabbedPanel.add(res.getString("SimInfoEditor.RunOptions"),runOptionsTopPanel); // If you change this order, change tabbedPanel.setSelectedIndex(1); below
    tabbedPanel.add(res.getString("SimInfoEditor.EditionOptions"),editionTopPanel);
    
    editorDialog = new JFrame();
    editorDialog.getContentPane().setLayout(new BorderLayout());
    editorDialog.getContentPane().add(tabbedPanel,BorderLayout.CENTER);
    editorDialog.getContentPane().add(okButton,BorderLayout.SOUTH);
//    editorDialog.setModal(false);
    editorDialog.setResizable(true);
    editorDialog.validate();
//    editorDialog.pack();
    //editorDialog.setSize(editorDialog.getPreferredSize()); //res.getDimension("Osejs.CodeDialog.Size"));
    editorDialog.setLocationRelativeTo(ejs.getMainPanel());
    ejs.setMenuBar(editorDialog);
    clear(false);
    
    setFontLevel(FontSizer.getLevel());
  }

  private void setFontLevel(int level) { 
    FontSizer.setFonts(editorDialog, level);
    editorDialog.pack();
  }
  
  // ----------------------------------------------
  // Getters
  // ----------------------------------------------

  /**
   * Whether the generated simulation will offer DataTool and FourierTool
   */
  public boolean addToolsForData() { return dataToolCB.isSelected(); }
  /**
   * Whether the generated simulation will offer a TranslatorTool
   */
  public boolean addTranslatorTool() {
    if (this.ejs.supportsHtml()) return true;
    return translatorToolCB.isSelected(); 
  }
  
  public void showRunningTab() {
    tabbedPanel.setSelectedIndex(1);
    editorDialog.setLocationRelativeTo(ejs.getMainPanel());
    editorDialog.setVisible(true);
    
  }

  /**
   * Whether the generated simulation the Mac screen menu bar
   */
  public boolean useMacScreenMenuBar() {return macMenuBarCB.isSelected();}
  /**
   * Whether the navigation bar is fixed when running in the EjsS Reader
   */
  public boolean fixedNavigationBar() {return fixedNavbarCB.isSelected();}

//  public boolean generateHtmlFiles() {return asHtmlCB.isSelected();}
  /**
   * Whether the generated simulation will offer support for synchronous collaboration
   */
  public boolean addAppletColSupport() {return appletColCB.isSelected();} //CJB for collaborative
  /**
   * Whether the generated simulation will offer capture tools
   */
  public boolean addCaptureTools() { return captureToolCB.isSelected(); }
  /**
   * Whether to use the interpreter with this model (what limitations exactly depend on TabbedVariablesEditor.java)
   */
  public boolean useInterpreter() { return interpreterCB.isSelected(); }
  
  public boolean isDeltaEquation() { return deltaEquationCB.isSelected(); }

  public String getPassword() { return passwordField.getText().trim(); }
  
  public String getExecPassword() { return execPasswordField.getText().trim(); }

  public String getManifestEntries() { 
    StringBuffer buffer = new StringBuffer();
    StringTokenizer tkn = new StringTokenizer(manifestField.getText().trim(),"\n");
    while (tkn.hasMoreTokens()) {
      String input = tkn.nextToken().trim();
      if (input.length()<=0) continue;
      buffer.append(input+";");
    }
    return buffer.toString();
  }

  /**
   * Whether to use a browser as preferred Run method
   */
  public boolean runInBrowserFirst() {return browserFirstCB.isSelected();}

  /** 
   * The preferred order of the simulation tab
   *  
   * @return
   */
  public int getSimulationTab() {
    try {
      return Integer.parseInt(modelTabField.getText());
    }
    catch (Exception exc) { return -1; }
  }

  public String getSimulationTabTitle() {
    String txt = modelTabTitleField.getText().trim();
    if (txt.length()<=0) txt = titleField.getText().trim();
    if (txt.length()<=0) txt = res.getString("Generate.HtmlSimulation");
    return txt;
  }

  public String getSimulationName() {
    String txt = modelNameField.getText().trim();
    if (txt.length()<=0) return null;
    return txt.replace(' ', '_').replace('\t','_');
  }

  public String getCSSFile() { return cssField.getText().trim(); }

  public java.util.List<String> getMoreCSSFiles () {
    java.util.List<String> cssFiles = new java.util.ArrayList<String>();
    for (int i=0, n=auxFilesCB.getItemCount(); i<n; i++) {
      String filename = auxFilesCB.getItemAt(i).toString().trim();
      if (filename.toLowerCase().endsWith(".css")) cssFiles.add(filename);
    }
    return cssFiles;
  }
  
  public String getHTMLHead() {
    return headField.getText().trim();
  }

  /*
  private String getMoreCSSText() {
    StringBuffer buffer = new StringBuffer();
    for (String filename : getMoreCSSFiles ()) buffer.append(filename+";");
    return buffer.toString();
  }
  
  public java.util.List<String> getMoreCSSFiles () {
    java.util.List<String> cssFiles = new java.util.ArrayList<String>();
    for (int i=0, n=moreCSSCombo.getItemCount(); i<n; i++) cssFiles.add(moreCSSCombo.getItemAt(i));
    return cssFiles;
  }

  */
//  public void staticImagesAddToCombo(String _imagePath) {
//    staticImagesCombo.removeItem(_imagePath); // So that not to repeat
//    staticImagesCombo.addItem(_imagePath);
//    if (staticImagesAddButton.isEnabled()) {
//      staticImagesRemoveButton.setEnabled(true);
//    }
//  }


//  private void copyCssFolder() {
//    String cssPath = cssField.getText().trim();
//    Generate.copyEJSLibrary(ejs);
//    if (cssPath.length()>0) { // reset to default
//      // And overwrite CSS files with the simulation CSS files, if any
//      File cssFolder = new File(ejs.getCurrentDirectory(),cssPath);
//      for (File file : JarTool.getContents(cssFolder)) {
//        String destName = "_ejs_library/css/"+FileUtils.getRelativePath(file, cssFolder, false);
//        JarTool.copy(file, new File (ejs.getOutputDirectory(),destName));
//      }
//    }
//  }
  
  // ----------------------------------------------
  // Simple customization
  // ----------------------------------------------

  public void makeVisible () {
    editorDialog.setLocationRelativeTo(ejs.getMainPanel());
    editorDialog.setVisible(true);
  }
  
  public void hideEditor () { editorDialog.setVisible (false); }

  public void setTitle (String _title) {
    if (_title==null) _title = "Unnamed";
    editorDialog.setTitle(TITLE + _title);
  }

  public void clear (boolean beforeReading) {
    titleField.setText("");
    copyrightField.setText("");
    keyField.setText("");
    passwordField.setText("");
    execPasswordField.setText("");
    levelField.setText("");
    languageField.setText("");
    abstractField.setText("");
    logoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    setLogos("");
//    logoButton.setActionCommand(null);
    authorLogoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    setAuthors(null);
//    titleLogoLabel.setText("");
    authorLogoLabel.setText("");

//    authorLogoButton.setActionCommand(null);
    auxFilesCB.removeAllItems();
    removeAuxFilesB.setEnabled(false);
//    base64ImagesCB.removeAllItems();
//    removeBase64ImagesB.setEnabled(false);
    detectedAuxFilesCB.removeAllItems();
    
    jarsCombo.removeAllItems();
    jarsRemoveButton.setEnabled(false);
    importsCombo.removeAllItems();
    importsEditButton.setEnabled(false);
    importsRemoveButton.setEnabled(false);
    updateImportStatements();
    classesCombo.removeAllItems();
    classesEditButton.setEnabled(false);
    classesRemoveButton.setEnabled(false);
    manifestField.setText(sBASIC_MANIFEST);

    dataToolCB.setSelected(false);
    translatorToolCB.setSelected(false);
    appletColCB.setSelected(false); //CJB for collaborative
    macMenuBarCB.setSelected(false);
    fixedNavbarCB.setSelected(false);
//    asHtmlCB.setSelected(false);
    browserFirstCB.setSelected(false);
    pauseOnPageExitCB.setSelected(true);
    captureToolCB.setSelected(false);
    interpreterCB.setSelected(true);
    deltaEquationCB.setSelected(false);
    modelTabField.setText("");
    modelTabTitleField.setText("");
    modelNameField.setText("");
    cssField.setText("");
    headField.setText("");
    staticImagesCombo.removeAllItems();
    staticImagesRemoveButton.setEnabled(false);
  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  public void setFont (Font _font) { }

  public JComboBox<String> getJarsCombo() { return jarsCombo; }
  
  public StringBuffer getJarsStringBuffer () {
    StringBuffer code = new StringBuffer();
    for (int i=0, n=jarsCombo.getItemCount(); i<n; i++) code.append(jarsCombo.getItemAt(i)+";");
    return code;
  }
  
  public void addToJarsCombo(String _jarPath) {
    jarsCombo.removeItem(_jarPath); // So that not to repeat
    jarsCombo.addItem(_jarPath);
    if (jarsAddButton.isEnabled()) {
      jarsEditButton.setEnabled(true);
      jarsRemoveButton.setEnabled(true);
    }
    // Check for possible ModelElements in the jar files added
    ejs.getModelEditor().getElementsEditor().checkForUserElements(_jarPath);
  }

  private String getComboList(JComboBox<String> combo) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0, n=combo.getItemCount(); i<n; i++) {
      if (i>0) buffer.append(";");
      buffer.append(combo.getItemAt(i));
    }
    return buffer.toString();
  }

  private String getStringList(ArrayList<String> list) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0, n=list.size(); i<n; i++) {
      if (i>0) buffer.append(";");
      buffer.append(list.get(i));
    }
    return buffer.toString();
  }

  private void setLogos(String _logoPaths) {
    logoCombo.removeAllItems();
    boolean hasEntries = false;
    if (_logoPaths!=null) {
      StringTokenizer tkn = new StringTokenizer(_logoPaths,";");
      while (tkn.hasMoreTokens()) {
        String logo = tkn.nextToken();
        if (logo.trim().length()<=0) continue;
        hasEntries = true;
        logoCombo.addItem(logo);
      }
    }
//    logoEditButton.setEnabled(hasEntries);
//    logoRemoveButton.setEnabled(hasEntries);
    if (hasEntries) logoCombo.setSelectedIndex(0);
  }

  private void setAuthors(String _authors) {
    authorCombo.removeAllItems();
    authorLogoList.clear();
    if (_authors!=null && _authors.trim().length()>0) {
      StringTokenizer tkn = new StringTokenizer(_authors,";");
      while (tkn.hasMoreTokens()) {
        String author = tkn.nextToken();
        if (author.trim().length()<=0) continue;
        authorCombo.addItem(author.trim());
        authorLogoList.add("");
      }
    }
    else {
      authorCombo.addItem("Author name");
      authorLogoList.add("");
    }
    authorCombo.setSelectedIndex(0);
    showAuthorLogo();
  }
  
  private void setAuthorsLogos(String _logos) {
    if (_logos!=null) {
      int index = 0;
      StringTokenizer tkn = new StringTokenizer(_logos,";");
      while (tkn.hasMoreTokens()) {
        if (index>=authorLogoList.size()) {
          System.err.println("Too many author logos in : "+_logos);
          return;
        }
        authorLogoList.set(index,tkn.nextToken());
        index++;
      }
    }
    showAuthorLogo();
  }

  public void showLogo() {
    if (logoCombo.getItemCount()<=0) {
      logoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
      return;
    }
    String filename = (String) logoCombo.getSelectedItem();
    if (filename==null) return;
    Icon icon = ResourceLoader.getIcon(filename);
    if (icon!=null) logoButton.setIcon(icon);
    else {
      JOptionPane.showMessageDialog(logoButton, 
          res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+filename, 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
      logoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    }
  }

  public void showAuthorLogo() {
    int index = authorCombo.getSelectedIndex();
    if (index<0) return;
    authorLogoLabel.setText(authorCombo.getItemAt(index));
    if (index>=authorLogoList.size()) {
      authorLogoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
      return;
    }
    String filename = authorLogoList.get(index);
    if (filename==null || filename.trim().length()<=0) {
      authorLogoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
      return;
    }
    Icon icon = ResourceLoader.getIcon(filename);
    if (icon!=null) authorLogoButton.setIcon(icon);
    else {
      JOptionPane.showMessageDialog(authorLogoButton, 
          res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+filename, 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
      authorLogoButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    }
  }


//  public JComboBox<String> getImportsCombo() { return importsCombo; }
  
//  public StringBuffer getImportsStringBuffer () {
//    StringBuffer code = new StringBuffer();
//    for (int i=0, n=importsCombo.getItemCount(); i<n; i++) {
//      String oneImport = importsCombo.getItemAt(i).toString().trim();
//      if (!oneImport.endsWith(";")) oneImport += ";";
//      code.append("import "+oneImport+"\n");
//    }
//    return code;
//  }

  public StringBuffer getRequiredClasses () {
    java.util.Set<String> set = new HashSet<String>();
    for (int i=0, n=classesCombo.getItemCount(); i<n; i++) {
      set.add(classesCombo.getItemAt(i).toString().trim());
    }
    StringBuffer buffer = new StringBuffer();
    for (String oneClass : set) buffer.append(oneClass+";");
    return buffer;
  }
  
  public java.util.List<String> getImportsList () {
    java.util.List<String> list = new ArrayList<String>();
    for (int i=0, n=importsCombo.getItemCount(); i<n; i++) {
      String oneImport = importsCombo.getItemAt(i).toString().trim();
      if (!oneImport.endsWith(";")) oneImport += ";";
      list.add(oneImport);
    }
    return list;
  }

  public void addToImportsCombo(String oneImport) {
    importsCombo.removeItem(oneImport); // So that not to repeat
    importsCombo.addItem(oneImport);
    if (jarsAddButton.isEnabled()) {
      importsEditButton.setEnabled(true);
      importsRemoveButton.setEnabled(true);
    }
  }
  
  public void fillXMLSimulation(SimulationXML _simXML) {
    _simXML.setInformation(INFORMATION.TITLE, titleField.getText());
    _simXML.setInformation(INFORMATION.HTMLHEAD, headField.getText());
    _simXML.setInformation(INFORMATION.AUTHOR,getAuthor());
    _simXML.setInformation(INFORMATION.COPYRIGHT,copyrightField.getText());
    _simXML.setInformation(INFORMATION.KEYWORDS,keyField.getText());
    _simXML.setInformation(INFORMATION.EXECUTION_PASSWORD,execPasswordField.getText());
    _simXML.setInformation(INFORMATION.LEVEL,levelField.getText());
    _simXML.setInformation(INFORMATION.LANGUAGE,languageField.getText());
    _simXML.setInformation(INFORMATION.ABSTRACT,abstractField.getText());
//    _simXML.setInformation(INFORMATION.EXTRA_CSS_FILES,getMoreCSSText());

    {
      StringBuffer buffer = new StringBuffer();
//      for (String filename : getAuxiliaryFilenames(true)) {
//        buffer.append(filename+";");
//      }
//      System.out.println("Auxiliary basic = "+buffer.toString());
//      buffer = new StringBuffer();
      for (String filename : getExtendedUserFilenames()) {
        buffer.append(filename+";");
      }
//      System.out.println("Complete auxiliary = "+buffer.toString());
      String files = buffer.toString();
      if (files.length()<=0) _simXML.setInformation(INFORMATION.REQUIRED_FILES,"");
      else                   _simXML.setInformation(INFORMATION.REQUIRED_FILES,files.substring(0,files.length()-1));
    }
//    if (_addBase64Images) {
//      String base64Images = getJavascriptForBase64Images();
//      if (base64Images==null) _simXML.setInformation(INFORMATION.BASE64_IMAGES,"");
//      else                    _simXML.setInformation(INFORMATION.BASE64_IMAGES,base64Images);
//    }

    try {
      int modelTabIndex = Integer.parseInt(modelTabField.getText());
      _simXML.setInformation(INFORMATION.MODEL_TAB, ""+modelTabIndex);
    }
    catch (Exception exc) { _simXML.setInformation(INFORMATION.MODEL_TAB, "-1"); }
    _simXML.setModelConfiguration(MODEL.PAUSE_ON_PAGE_EXIT, pauseOnPageExitCB.isSelected() ? "true" : "false");
    _simXML.setInformation(INFORMATION.MODEL_TAB_TITLE, ""+getSimulationTabTitle());
    _simXML.setInformation(INFORMATION.LOGO_IMAGE,getComboList(logoCombo));
    _simXML.setInformation(INFORMATION.AUTHOR_IMAGE,getStringList(authorLogoList));
//    _simXML.setInformation(INFORMATION.MENU_LOCATION, ((TwoStrings) htmlViewMenuCombo.getSelectedItem()).getSecondString());
  }

  public String saveString () {
    detectAuxiliaryFiles(false); // Update the list of auxiliary files
    StringBuffer info = new StringBuffer();
    info.append("<Title><![CDATA["+titleField.getText()+"]]></Title>\n");
    info.append("<Copyright><![CDATA["+copyrightField.getText()+"]]></Copyright>\n");
    info.append("<Keywords><![CDATA["+keyField.getText()+"]]></Keywords>\n");
    info.append("<Password><![CDATA["+passwordField.getText()+"]]></Password>\n");
    info.append("<Level><![CDATA["+levelField.getText()+"]]></Level>\n");
    info.append("<Language><![CDATA["+languageField.getText()+"]]></Language>\n");
    info.append("<Abstract><![CDATA["+abstractField.getText()+"]]></Abstract>\n");

    info.append("<ExecPassword><![CDATA["+execPasswordField.getText()+"]]></ExecPassword>\n");
    info.append("<CaptureTools>"+captureToolCB.isSelected()+"</CaptureTools>\n");
    info.append("<DataTools>"+dataToolCB.isSelected()+"</DataTools>\n");
    info.append("<LanguageTools>"+translatorToolCB.isSelected()+"</LanguageTools>\n");
    info.append("<AppletColSupport>"+appletColCB.isSelected()+"</AppletColSupport>\n"); //CJB for collaborative
    info.append("<UseMacMenuBar>"+macMenuBarCB.isSelected()+"</UseMacMenuBar>\n");
    info.append("<FixedNavigationBar>"+fixedNavbarCB.isSelected()+"</FixedNavigationBar>\n");
//    info.append("<GenerateHtmlFiles>"+asHtmlCB.isSelected()+"</GenerateHtmlFiles>\n");
    info.append("<RunInBrowserFirst>"+browserFirstCB.isSelected()+"</RunInBrowserFirst>\n");
    info.append("<RunAlways>"+pauseOnPageExitCB.isSelected()+"</RunAlways>\n");
    info.append("<UseInterpreter>"+interpreterCB.isSelected()+"</UseInterpreter>\n");
    info.append("<UseDeltaForODE>"+deltaEquationCB.isSelected()+"</UseDeltaForODE>\n");
    info.append("<ModelTab>"+modelTabField.getText()+"</ModelTab>\n");
    info.append("<ModelTabTitle><![CDATA["+modelTabTitleField.getText()+"]]></ModelTabTitle>\n");
    info.append("<ModelName><![CDATA["+modelNameField.getText()+"]]></ModelName>\n");
//    info.append("<MenuPosition>"+htmlViewMenuCombo.getSelectedIndex()+"</MenuPosition>\n");
    info.append("<CSSFile>"+cssField.getText()+"</CSSFile>\n");
    info.append("<StaticImages>\n");
    for (int i=0, n=staticImagesCombo.getItemCount(); i<n; i++) info.append("<StaticImage><![CDATA["+staticImagesCombo.getItemAt(i)+"]]></StaticImage>\n");
    info.append("</StaticImages>\n");
    info.append("<HTMLHead><![CDATA["+headField.getText()+"]]></HTMLHead>\n");
    info.append("<Logo>"+getComboList(logoCombo)+"</Logo>\n");
    // Order is important for next two
    info.append("<Author><![CDATA["+getAuthor()+"]]></Author>\n");
    info.append("<AuthorLogo>"+getStringList(authorLogoList)+"</AuthorLogo>\n");

    info.append("<AdditionalLibraries>\n");
    for (int i=0, n=jarsCombo.getItemCount(); i<n; i++) info.append("<Library><![CDATA["+jarsCombo.getItemAt(i)+"]]></Library>\n");
    info.append("</AdditionalLibraries>\n");
    info.append("<ImportStatements>\n");
    for (int i=0, n=importsCombo.getItemCount(); i<n; i++) info.append("<Import><![CDATA["+importsCombo.getItemAt(i)+"]]></Import>\n");
    info.append("</ImportStatements>\n");
    info.append("<ClassesRequired>\n");
    for (int i=0, n=classesCombo.getItemCount(); i<n; i++) info.append("<Class><![CDATA["+classesCombo.getItemAt(i)+"]]></Class>\n");
    info.append("</ClassesRequired>\n");
    info.append("<ManifestLines><![CDATA["+manifestField.getText()+"]]></ManifestLines>\n");

    info.append("<DetectedFiles><![CDATA[");
    for (int i=0,n=detectedAuxFilesCB.getItemCount(); i<n; i++) info.append(detectedAuxFilesCB.getItemAt(i).toString()+";");
    info.append("]]></DetectedFiles>\n");
    info.append("<AuxiliaryFiles><![CDATA[");
    for (int i=0,n=auxFilesCB.getItemCount(); i<n; i++) info.append(auxFilesCB.getItemAt(i).toString()+";");
    info.append("]]></AuxiliaryFiles>\n");
//    info.append("<Base64Images><![CDATA[");
//    for (int i=0,n=base64ImagesCB.getItemCount(); i<n; i++) info.append(base64ImagesCB.getItemAt(i).toString()+";");
//    info.append("]]></Base64Images>\n");
    
    
    return info.toString();
  }

  public void readString (String _input, boolean _merging) {
    if (!_merging) {
      readEntry (_input,titleField,"Title");
      readEntry (_input,copyrightField,"Copyright");
      readEntry (_input,keyField,"Keywords");
      readEntry (_input,passwordField,"Password");
      readEntry (_input,levelField,"Level");
      readEntry (_input,languageField,"Language");
      readEntry (_input,abstractField,"Abstract");
      readEntry (_input,headField,"HTMLHead");

      readEntry (_input,execPasswordField,"ExecPassword");
      readBoolean (_input,captureToolCB,"CaptureTools",false);
      readBoolean (_input,dataToolCB,"DataTools",false);
      readBoolean (_input,translatorToolCB,"LanguageTools",false);
      readBoolean (_input,appletColCB,"AppletColSupport",false);
      readBoolean (_input,macMenuBarCB,"UseMacMenuBar",false);
      readBoolean (_input,fixedNavbarCB,"FixedNavigationBar",false);
//      readBoolean (_input,asHtmlCB,"GenerateHtmlFiles",false);
      readBoolean (_input,browserFirstCB,"RunInBrowserFirst",false);
      readBoolean (_input,pauseOnPageExitCB,"RunAlways",true);
      readBoolean (_input,interpreterCB,"UseInterpreter",true);
      readBoolean (_input,deltaEquationCB,"UseDeltaForODE",false);
      

      modelTabField.setText(OsejsCommon.getPiece(_input,"<ModelTab>","</ModelTab>",false));
      String tabTitle = OsejsCommon.getPiece(_input,"<ModelTabTitle><![CDATA[","]]></ModelTabTitle>",false);
      if (tabTitle!=null) modelTabTitleField.setText(tabTitle);
      else modelTabTitleField.setText("");
      String modelName = OsejsCommon.getPiece(_input,"<ModelName><![CDATA[","]]></ModelName>",false);
      if (modelName!=null) modelNameField.setText(modelName);
      else modelNameField.setText("");

      // Order is important for next two
      setAuthors(OsejsCommon.getPiece(_input,"<Author><![CDATA[","]]></Author>",false));
      setAuthorsLogos(OsejsCommon.getPiece(_input,"<AuthorLogo>","</AuthorLogo>",false));

//      try {
//        String option = OsejsCommon.getPiece(_input,"<MenuPosition>","</MenuPosition>",false);
//        htmlViewMenuCombo.setSelectedIndex(Integer.parseInt(option));
//      }
//      catch (Exception exc) {
//        htmlViewMenuCombo.setSelectedIndex(0);
//      }
      {
        String cssInfo = OsejsCommon.getPiece(_input,"<CSSFolder>","</CSSFolder>",false);
        if (cssInfo!=null) { // backwards compatibility
          File cssFile = new File (ejs.getCurrentXMLFile().getParentFile(),cssInfo+"/ejsSimulation.css");
          if (cssFile.exists()) cssField.setText(cssInfo+"/ejsSimulation.css");
        }
        else cssInfo = OsejsCommon.getPiece(_input,"<CSSFile>","</CSSFile>",false);
        if (cssInfo!=null) {
          cssField.setText(cssInfo);
//          copyCssFolder(); //"at reading");
        }
      }
      setLogos(OsejsCommon.getPiece(_input,"<Logo>","</Logo>",false));
    }
    
    StringTokenizer tkn;
    String files = OsejsCommon.getPiece(_input,"<DetectedFiles><![CDATA[","]]></DetectedFiles>",false);
    if (files!=null) {
      tkn = new StringTokenizer(files,";\n");
      while (tkn.hasMoreTokens()) detectedAuxFilesCB.addItem(tkn.nextToken());
    }

    files = OsejsCommon.getPiece(_input,"<AuxiliaryFiles><![CDATA[","]]></AuxiliaryFiles>",false);
    if (files!=null) {
      tkn = new StringTokenizer(files,";\n");
      while (tkn.hasMoreTokens()) auxFilesCB.addItem(tkn.nextToken());
    }
    removeAuxFilesB.setEnabled(auxFilesCB.getItemCount()>0);

    
    // Static Images, if any
    {
      String staticImageFiles = OsejsCommon.getPiece(_input,"<StaticImages>","</StaticImages>",false);
      if (staticImageFiles!=null) {
        int begin = staticImageFiles.indexOf("<StaticImage><![CDATA[");
        while (begin>=0) {
          int end = staticImageFiles.indexOf("]]></StaticImage>\n");
          String path = staticImageFiles.substring(begin+22,end).trim();
          staticImagesCombo.addItem(path);
          staticImageFiles = staticImageFiles.substring(end+18);
          begin = staticImageFiles.indexOf("<StaticImage>");
        }
      }
      staticImagesRemoveButton.setEnabled(staticImagesCombo.getItemCount()>0);
    }

    
//    files = OsejsCommon.getPiece(_input,"<Base64Images><![CDATA[","]]></Base64Images>",false);
//    if (files!=null) {
//      tkn = new StringTokenizer(files,";\n");
//      while (tkn.hasMoreTokens()) base64ImagesCB.addItem(tkn.nextToken());
//    }
//    removeBase64ImagesB.setEnabled(base64ImagesCB.getItemCount()>0);

    {
      String libs = OsejsCommon.getPiece(_input,"<AdditionalLibraries>","</AdditionalLibraries>",false);
      if (libs!=null) {
        int begin = libs.indexOf("<Library><![CDATA[");
        while (begin>=0) {
          int end = libs.indexOf("]]></Library>\n");
          String jarPath = libs.substring(begin+18,end).trim();
          //        if (jarFilename.startsWith("./")) jarsFound.add(new File (_xmlFile.getParentFile(),jarFilename.substring(2)));
          //        else jarsFound.add(new File (ejs.getSourceDirectory(),jarFilename));
          addToJarsCombo(jarPath);
          libs = libs.substring(end+14);
          begin = libs.indexOf("<Library>");
        }
      }
    }
    
    // Extract the import statements, if any
    {
      String imports = OsejsCommon.getPiece(_input,"<ImportStatements>","</ImportStatements>",false);
      if (imports!=null) {
        int begin = imports.indexOf("<Import><![CDATA[");
        while (begin>=0) {
          int end = imports.indexOf("]]></Import>\n");
          addToImportsCombo(imports.substring(begin+17,end));
          imports = imports.substring(end+13);
          begin = imports.indexOf("<Import>");
        }
        updateImportStatements();
      }
    }
    
    // Extract the required classes statements, if any
    {
      String classesRequired = OsejsCommon.getPiece(_input,"<ClassesRequired>","</ClassesRequired>",false);
      if (classesRequired!=null) {
        int begin = classesRequired.indexOf("<Class><![CDATA[");
        while (begin>=0) {
          int end = classesRequired.indexOf("]]></Class>\n");
          String oneClass = classesRequired.substring(begin+16,end);
          classesCombo.removeItem(oneClass); // So that not to repeat
          classesCombo.addItem(oneClass);
          classesRequired = classesRequired.substring(end+12);
          begin = classesRequired.indexOf("<Class><![CDATA[");
        }
        if (jarsAddButton.isEnabled()) {
          classesEditButton.setEnabled(true);
          classesRemoveButton.setEnabled(true);
        }
      }
    }
        
    String manifestLines = OsejsCommon.getPiece(_input,"<ManifestLines><![CDATA[","]]></ManifestLines>",false);
    if (manifestLines==null) manifestField.setText(sBASIC_MANIFEST);
    else manifestField.setText(manifestLines);
    manifestField.setCaretPosition(0);
  }

  static private void readEntry (String _input, JTextComponent _field, String _key) {
    try {
      _field.setText(OsejsCommon.getPiece(_input,"<"+_key+"><![CDATA[","]]></"+_key+">",false));
      _field.setCaretPosition(0);
    }
    catch (Exception exc) {
    }
  }

  static private void  readBoolean (String _input, JCheckBox _box, String _key, boolean _defaultValue) {
    String txt = OsejsCommon.getPiece(_input,"<"+_key+">","</"+_key+">",false);
    if (txt==null) _box.setSelected(_defaultValue); // True is the default
    else _box.setSelected(!txt.equals("false"));
  }

//  /**
//   * Reads the set of files required by the simulation as prescribed on the given file.
//   * This is used when importing a file from another directory.
//   */
//  static public Set<PathAndFile> readAuxiliaryFiles (Osejs _ejs, File _parentDir, String _input) {
//    Set<PathAndFile> set = new HashSet<PathAndFile>();
//    String auxFiles = OsejsCommon.getPiece(_input,"<AuxiliaryFiles><![CDATA[","]]></AuxiliaryFiles>",false);
//    String detectedFiles = OsejsCommon.getPiece(_input,"<DetectedFiles><![CDATA[","]]></DetectedFiles>",false);
//    if (detectedFiles!=null) auxFiles = detectedFiles + auxFiles;
//    String missingFiles = "";
//    StringTokenizer tkn = new StringTokenizer(auxFiles,";\n");
//    while (tkn.hasMoreTokens()) {
//      String filename = tkn.nextToken().trim();
//      if (filename.length()<=0) continue;
//      File file;
//      if (filename.startsWith("./")) file = new File (_parentDir,filename.substring(2));
//      else file = new File (_ejs.getSourceDirectory(),filename);
//      if (file.exists()) {
//        if (file.isDirectory()) {
//          String dirpath = FileUtils.getPath(_parentDir);
//          for (File subfile : JarTool.getContents(file)) set.add(new PathAndFile(FileUtils.getRelativePath(subfile,dirpath,true),subfile));
//        }
//        else set.add(new PathAndFile(filename,file));
//      }
//      else missingFiles += "  " + filename + "\n";
//    }
//    if (missingFiles.length()>0) JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
//        res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+missingFiles, 
//        res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
//    return set;
//  }

  /**
   * Returns the set of the auxiliary files indicated by the user or detected by EJS
   * @return
   */
  public Set<PathAndFile> getAuxiliaryPathAndFiles (boolean addOSPImages) {
    Set<PathAndFile> set = new HashSet<PathAndFile>();
    for (String filename : getAuxiliaryFilenames(addOSPImages)) set.add (new PathAndFile(filename,ejs.getRelativeFile(filename)));
    return set;
  }
  
  /**
   * Returns the set of auxiliary file names indicated by the user or detected by EJS
   * @return
   */
  public Set<String> getAuxiliaryFilenames (boolean addOSPImages) {
    detectAuxiliaryFiles (addOSPImages);
    Set<String> set = new HashSet<String>();
    for (int i=0,n=auxFilesCB.getItemCount(); i<n; i++) set.add(auxFilesCB.getItemAt(i).toString());
//    for (int i=0,n=base64ImagesCB.getItemCount(); i<n; i++) set.add(base64ImagesCB.getItemAt(i).toString());
    for (int i=0,n=detectedAuxFilesCB.getItemCount(); i<n; i++) set.add(detectedAuxFilesCB.getItemAt(i).toString());
//    for (int i=0,n=logoCombo.getItemCount(); i<n; i++) set.add(logoCombo.getItemAt(i).toString());
//    for (int i=0,n=authorLogoCombo.getItemCount(); i<n; i++) set.add(authorLogoCombo.getItemAt(i).toString());
    return set;
  }

  /**
   * Returns the set of user defined files names indicated by the user
   */
  private Set<String> getExtendedUserFilenames () {
    Set<String> set = new HashSet<String>();
    for (String filename : getAuxiliaryFilenames (true)) {
      if (filename.startsWith("/org/opensourcephysics/resources/")) {
        set.add(filename);
        continue;
      }
      File file = ejs.getRelativeFile(filename);
      if (!file.exists()) continue;
      if (file.isDirectory()) {
        for (File child : JarTool.getContents(file)) {
          String path = ejs.getRelativePath(child);
          if (!path.endsWith("/.DS_Store")) set.add(ejs.getRelativePath(child));
        }
      }
      else set.add(ejs.getRelativePath(file));
    }
    return set;
  }

  /**
   * Compiles the list of auxiliary files as indicated by the different EJS editors
   */
  public void detectAuxiliaryFiles (boolean addOSPImages) { 
    // Collect the information from the EJS editors
    StringBuffer buffer = new StringBuffer();
    buffer.append(ejs.getDescriptionEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED,""));  // required by the description
    buffer.append(ejs.getModelEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED,"")); // required by the model
    buffer.append(ejs.getViewEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED,""));  // required by the view
    buffer.append(ejs.getHtmlViewEditor().getResourcesNeeded());  // required by the HTML view
    for (int i=0,n=staticImagesCombo.getItemCount(); i<n; i++) buffer.append(staticImagesCombo.getItemAt(i).toString()+";");
    for (int i=0,n=logoCombo.getItemCount(); i<n; i++) buffer.append(logoCombo.getItemAt(i).toString()+";");
    for (int i=0,n=authorLogoList.size(); i<n; i++) {
      String logo = authorLogoList.get(i);
      if (logo!=null && logo.trim().length()>0) buffer.append(logo.trim()+";");
    }
    String cssFilename = getCSSFile();
    if (cssFilename.length()>0) buffer.append(cssFilename+";");

//    String cssFolder = getCSSFolder();
//    if (cssFolder.length()>0) buffer.append(cssFolder+";");
    
    // Now make the list simplifying repeated entries
    ArrayList<String> list = new ArrayList<String>();
    String relativePath = FileUtils.getRelativePath(ejs.getCurrentDirectory(), ejs.getSourceDirectory(), false);
    int length = relativePath.length();
    String missingText = "";
    StringTokenizer tkn = new StringTokenizer(buffer.toString(),";");
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken().trim();
      if (token.length()<=0) continue;
      String resFile = FileUtils.uncorrectUrlString(token.replace('\\','/')); // Change separator char to '/'
//      System.out.println("PRocessing "+resFile);
      if (resFile.startsWith("/org/opensourcephysics/resources/")) {
        if (addOSPImages) {
          if (!list.contains(resFile)) list.add(resFile);
        }
        continue;
      }
      if (length>0 && resFile.startsWith(relativePath)) resFile = "./" + resFile.substring(length);
      else resFile = ejs.getRelativePath(resFile);
      if (!list.contains(resFile)) {
        list.add(resFile);
        if (!ejs.getRelativeFile(resFile).exists()) missingText += "  "+resFile +"\n";
      }
    }
    Collections.sort(list);
    
    // Fill the ComboBox
    detectedAuxFilesCB.removeAllItems();
    for (String resource : list) detectedAuxFilesCB.addItem(resource);
    if (detectedAuxFilesCB.getItemCount()>0) detectedAuxFilesCB.setSelectedIndex(0);
    
    // Issue a warning if there were missing files
    if (missingText.length()>0) JOptionPane.showMessageDialog(auxFilesCB, 
        res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+missingText, 
        res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Compiles the list of images that must be encoded as base64 
   * and encodes them in a JS object, as in
   *  "  __base64Images ["./images/MyImage.png"]="4hdytsnhbvdjjej", "./images/MyImage2.png": "4hdytsnhvcdsbvdjjej";"  
   */
  public Set<String> getBase64Images () { 
    // Collect the information from the EJS editors
    Set<String> base64Set = ejs.getHtmlViewEditor().getBase64Images();
//    for (int i=0,n=base64ImagesCB.getItemCount(); i<n; i++) { 
//      String filename = base64ImagesCB.getItemAt(i);
    for (int i=0,n=auxFilesCB.getItemCount(); i<n; i++) { 
      String filename = auxFilesCB.getItemAt(i);
      File file = ejs.getRelativeFile(filename);
      if (!file.exists()) continue;
      if (file.isDirectory()) {
        for (File child : JarTool.getContents(file)) if (MimeInfo.shouldBeConvertedToBase64(child)) base64Set.add(ejs.getRelativePath(child));
      }
      else if (MimeInfo.shouldBeConvertedToBase64(file)) base64Set.add(ejs.getRelativePath(file));
    }
        
    return nonStaticImages(base64Set);
  }
  
  private Set<String> nonStaticImages(Set<String> filenames) {
    Set<String> filePaths = new HashSet<String>();
    for (int i=0,n=staticImagesCombo.getItemCount(); i<n; i++) {
      String staticFilename = staticImagesCombo.getItemAt(i).toString().trim();
      File staticFile= ejs.getRelativeFile(staticFilename);
      try {
        filePaths.add(staticFile.getCanonicalPath());
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("Static file has invalid cannonical path : "+staticFilename);
      }
    }
    Set<String> nonStaticImageSet = new HashSet<String>();
    for (String filename : filenames) {
      File file = ejs.getRelativeFile(filename);
      try {
        if (filePaths.contains(file.getCanonicalPath())) ; // do nothing System.out.println("This file will not be converted to base 64: "+filename);
        else nonStaticImageSet.add(filename);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        nonStaticImageSet.add(filename);
        e.printStackTrace();
        System.err.println("Image file has invalid cannonical path : "+filename);
      }
    }
    return nonStaticImageSet;
  }
  
  
  public Set<String> getUserJars () {
    Set<String> set = new HashSet<String>();
    for (int i=0, n=jarsCombo.getItemCount(); i<n; i++) set.add(jarsCombo.getItemAt(i).toString());
    return set;
  }

  public void updateImportStatements() {
    int n = importsCombo.getItemCount();
    importStatements = new String[n+1];
    importStatements[0] = "java.lang.*";
    for (int i=1; i<=n; i++) importStatements[i] = importsCombo.getItemAt(i-1);
  }

  public String[] getImportStatements () { return importStatements; }

  public void fillAuthor(EjsOptions _options) {
    String author = _options.getAuthor();
    if (author.length()<=0) return;
    makeVisible();
    int option = JOptionPane.showConfirmDialog(editorDialog, res.getString("EjsOptions.UseDefaultAuthor"), res.getString("Warning"), JOptionPane.YES_NO_OPTION);
    if (option==JOptionPane.YES_OPTION) {
      String affiliation = _options.getAffiliation();
      if (affiliation.length()>0) setAuthors(author+" - "+affiliation);
      else setAuthors(author);
      abstractField.setText(abstractField.getText()+_options.getContact());
    }
  }
  
} // end of class
