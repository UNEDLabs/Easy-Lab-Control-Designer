/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class CreateListDialog {
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  static public final int NUMBERING_OPTION_NONE = 0;
  static public final int NUMBERING_OPTION_CHAPTER = 1;
  static public final int NUMBERING_OPTION_CHAPTER_AND_SECTION = 2;

  static public final int STRUCTURE_NESTED = 0;
  static public final int STRUCTURE_FLAT = 1;
  static public final int STRUCTURE_STRICT = 2;

  static private String configFilename = "ListConfig.xml";
  
  static public class ListInformation {
    boolean asFolder=true;
    boolean readOnly=true;
    String name="";
    String shortName= "";
    String subtitle="";
    String about="";
    File imageFile=null;
    File infoFile=null;
    boolean includeHTMLFiles=true;
    boolean separateChapters=false;
    int foldersStructure=STRUCTURE_NESTED;
    int numberingOption = 0;
    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();

    public boolean isReadOnly() { return readOnly; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getSubtitle() { return subtitle; }
    public String getAbout() { return about; }
    public File getImagefile() { return imageFile; }
    public File getInfoFile() { return infoFile; }
    public java.util.List<PathAndFile> getList() { return list; }
    public boolean getIncludeHTMLFiles() { return includeHTMLFiles; }
    public boolean getSeparateChapters() { return separateChapters; }
    public int getNumberingOption() { return numberingOption; }
    public int getFoldersStructure() { return foldersStructure; }

    public void saveToFile(File file, JComponent button) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n");
      buffer.append("<EjsSListConfig>\n");
      buffer.append("<AsFolder>"+asFolder+"</AsFolder>\n");
      buffer.append("<ReadOnly>"+readOnly+"</ReadOnly>\n");
      buffer.append("<Name><![CDATA["+name+"]]></Name>\n");
      buffer.append("<Subtitle><![CDATA["+subtitle+"]]></Subtitle>\n");
      buffer.append("<About><![CDATA["+about+"]]></About>\n");
      if (imageFile==null) buffer.append("<ImageFile></ImageFile>\n");
      else buffer.append("<ImageFile>"+FileUtils.getRelativePath(imageFile,file.getParentFile(),true)+"</ImageFile>\n");
      if (infoFile==null) buffer.append("<InfoFile></InfoFile>\n");
      else buffer.append("<InfoFile>"+FileUtils.getRelativePath(infoFile,file.getParentFile(),true)+"</InfoFile>\n");
      buffer.append("<IncludeHTML>"+includeHTMLFiles+"</IncludeHTML>\n");
      buffer.append("<ModelsAsChapters>"+separateChapters+"</ModelsAsChapters>\n");
      switch(foldersStructure) {
        case STRUCTURE_FLAT :   buffer.append("<FolderStructure>FLAT</FolderStructure>\n"); break;
        case STRUCTURE_STRICT : buffer.append("<FolderStructure>STRICT</FolderStructure>\n"); break;
        default : 
        case STRUCTURE_NESTED : buffer.append("<FolderStructure>NESTED</FolderStructure>\n"); break;
      }
      switch(numberingOption) {
        case CreateListDialog.NUMBERING_OPTION_CHAPTER :             buffer.append("<Numbering>CHAPTER</Numbering>\n");  break;
        case CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION : buffer.append("<Numbering>CHAPTER_AND_SECTION</Numbering>\n");  break;
        default : 
        case CreateListDialog.NUMBERING_OPTION_NONE :                buffer.append("<Numbering>NONE</Numbering>\n");  break;
      }
      buffer.append("<ModelList>\n");
      for (int i=0,n=list.size(); i<n; i++) {
        buffer.append(FileUtils.getRelativePath(list.get(i).getFile(),file.getParentFile(),true)+"\n");
      }
      buffer.append("</ModelList>\n");
      buffer.append("</EjsSListConfig>\n");
      try {
        FileUtils.saveToFile(file, OsejsCommon.getUTF16(), buffer.toString());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        JOptionPane.showMessageDialog(button, 
            res.getString("Osejs.File.SavingError")+"\n"+file.getName(), 
            res.getString("Error"), JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }    
    }

    static private String readEntry (String _input, String _key, boolean cData) {
      try {
        String piece;
        if (cData) piece = OsejsCommon.getPiece(_input,"<"+_key+"><![CDATA[","]]></"+_key+">",false);
        else       piece = OsejsCommon.getPiece(_input,"<"+_key+">","</"+_key+">",false);
        return piece;
      }
      catch (Exception exc) {
        return null;
      }
    }

    public void readFromFile(File file, JComponent component) {
      String input = readEntry(FileUtils.readTextFile(file,OsejsCommon.getUTF16()),"EjsSListConfig",false);
      if (input==null || input.trim().length()<=0) {
        JOptionPane.showMessageDialog(component, 
            res.getString("Osejs.File.InvalidFile")+"\n"+file.getName(), 
            res.getString("Error"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      String piece = readEntry(input,"AsFolder",false);
      if (piece!=null) asFolder = (piece.indexOf("true")>=0);
      piece = readEntry(input,"ReadOnly",false);
      if (piece!=null) readOnly = (piece.indexOf("true")>=0);
      piece = readEntry(input,"Name",true);
      if (piece!=null) name = piece;
      else name = "";
      piece = readEntry(input,"Subtitle",true);
      if (piece!=null) subtitle = piece;
      else subtitle = "";
      piece = readEntry(input,"About",true);
      if (piece!=null) about = piece;
      else about = "";
      piece = readEntry(input,"ImageFile",false);
      if (piece!=null && piece.trim().length()>0) imageFile = new File(file.getParentFile(),piece);
      else imageFile = null;
      piece = readEntry(input,"InfoFile",false);
      if (piece!=null && piece.trim().length()>0) infoFile = new File(file.getParentFile(),piece);
      else infoFile = null;
      piece = readEntry(input,"IncludeHTML",false);
      if (piece!=null) includeHTMLFiles = (piece.indexOf("true")>=0);
      piece = readEntry(input,"ModelsAsChapters",false);
      if (piece!=null) separateChapters = (piece.indexOf("true")>=0);
      piece = readEntry(input,"FolderStructure",false);
      if (piece!=null) {
        if (piece.equals("FLAT"))        foldersStructure = STRUCTURE_FLAT;
        else if (piece.equals("STRICT")) foldersStructure = STRUCTURE_STRICT;
        else                             foldersStructure = STRUCTURE_NESTED;
      }
      piece = readEntry(input,"Numbering",false);
      if (piece!=null) {
        if (piece.equals("CHAPTER"))                  numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER;
        else if (piece.equals("CHAPTER_AND_SECTION")) numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION;
        else                                          numberingOption = CreateListDialog.NUMBERING_OPTION_NONE;
      }
      piece = readEntry(input,"ModelList",false);
      if (piece!=null) {
        StringBuffer buffer = new StringBuffer();
        StringTokenizer tkn = new StringTokenizer(piece.trim(),"\n");
        while (tkn.hasMoreTokens()) {
          String model = tkn.nextToken();
          File modelFile = new File(file.getParentFile(),model);
          if (modelFile.exists()) list.add(new PathAndFile(model,modelFile));
          else buffer.append(model+"\n");
        }
        String errorList = buffer.toString();
        if (errorList.length()>0) JOptionPane.showMessageDialog(component, 
            res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+errorList, 
            res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
      }
    }
    
  } // End of inner class
  
  static public interface FileValidator {
    public boolean acceptFile(File file);
  }

  static private org.colos.ejs.osejs.utils.ResourceUtil res = new org.colos.ejs.osejs.utils.ResourceUtil("Resources");

  static public enum LIST_TYPE { EPUB, BOOK_APP, OTHER };
  
  static public ListInformation createZIPFileList (Osejs ejs, final Component parentComponent, LIST_TYPE listType) {
    FileValidator validator = new FileValidator() {
      public boolean acceptFile(File file) {
        String filename = file.getName().toLowerCase();
        return filename.endsWith(".zip");
      }
    };
    return createFileList(ejs,parentComponent, "ZIP", new String[]{"zip"}, validator, listType);
  }
  
  static private boolean listContainsFile(DefaultListModel<PathAndFile> listModel, File file) {
    for (int i=0,n=listModel.size(); i<n; i++) if (listModel.get(i).getFile().equals(file)) return true;
    return false;
  }
  
  static private ListInformation createFileList (final Osejs ejs, final Component parentComponent, 
      final String description, final String[] extensions, final FileValidator validator, LIST_TYPE listType) {
    final ListInformation info = new ListInformation();
    final JDialog dialog=new JDialog();
    final DefaultListModel<PathAndFile> listModel = new DefaultListModel<PathAndFile>();
    final JList<PathAndFile> list = new JList<PathAndFile>(listModel);

//    list.setDragEnabled(true);
//    list.setDropMode(DropMode.ON_OR_INSERT);
//    list.setTransferHandler(new CreateListDialogTransferHandler(ejs));
    
    JButton addButton = new JButton (res.getString("Add"));
    addButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        File[] files = FileChooserUtil.chooseFilenames(ejs, ejs.getExportDirectory(), description, extensions);
        if (files!=null) { 
          int selected = list.getMaxSelectionIndex();
          for (File targetFile : files) {
            if (!validator.acceptFile(targetFile)) {
              JOptionPane.showMessageDialog(parentComponent,res.getString("Osejs.File.InvalidFile"), res.getString("Warning"),JOptionPane.INFORMATION_MESSAGE);
            }
            else if (listContainsFile(listModel,targetFile)) {
              JOptionPane.showMessageDialog(parentComponent,res.getString("Osejs.File.FileRepeated"), res.getString("Warning"),JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              String path = FileUtils.getRelativePath(targetFile, ejs.getExportDirectory(), true);
              if (selected<0) listModel.addElement(new PathAndFile(path,targetFile));
              else listModel.insertElementAt(new PathAndFile(path,targetFile), ++selected);
            }
          }
          list.clearSelection();
        }  
      }      
    });

    JButton upButton = new JButton (res.getString("Tree.MoveUp"));
    upButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        int[] selected = list.getSelectedIndices();
        int minIndex = listModel.getSize()-1;
        for (int i=0,n=selected.length; i<n ; i++) if (minIndex>selected[i]) minIndex = selected[i];
        if (minIndex<=0) return;
        for (int i=0,n=selected.length; i<n ; i++) {
          int pos = selected[i];
          if (pos>0) {
            PathAndFile paf = listModel.elementAt(pos);
            listModel.remove(pos);
            listModel.insertElementAt(paf, pos-1);
            selected[i]--;
//            list.setSelectedIndex(pos-1);
          }
        }
        list.setSelectedIndices(selected);
//        list.clearSelection();
      }      
    });

    JButton downButton = new JButton (res.getString("Tree.MoveDown"));
    downButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        int max = listModel.getSize()-1;
        int[] selected = list.getSelectedIndices();
        int maxIndex = 0;
        for (int i=0,n=selected.length; i<n ; i++) if (maxIndex<selected[i]) maxIndex = selected[i];
        if (maxIndex>=max) return;
        for (int i=selected.length-1; i>=0 ; i--) {
          int pos = selected[i];
          if (pos<max) {
            PathAndFile paf = listModel.elementAt(pos);
            listModel.remove(pos);
            listModel.insertElementAt(paf, pos+1);
            selected[i]++;
//            list.setSelectedIndex(pos+1);
          }
        }
        list.setSelectedIndices(selected);
//        list.clearSelection();
      }      
    });

    JButton removeButton = new JButton (res.getString("Remove"));
    removeButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        int[] selected = list.getSelectedIndices();
        for (int i=selected.length-1; i>=0 ; i--) listModel.remove(selected[i]);
        list.clearSelection();
      }      
    });

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        info.list.clear();
        for (int i=0,n=listModel.size(); i<n; i++) info.list.add(listModel.get(i));
        dialog.setVisible (false);
      }      
    });

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addMouseListener (new MouseAdapter() {
      public void mousePressed (MouseEvent evt) {
        info.list.clear();
        dialog.setVisible (false);
      }      
    });
    
    JPanel buttonPanel = new JPanel (new GridLayout(2,0));
    buttonPanel.add (addButton);
    buttonPanel.add (upButton);
    buttonPanel.add (okButton);
    buttonPanel.add (removeButton);
    buttonPanel.add (downButton);
    buttonPanel.add (cancelButton);

    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(new Dimension(400,150) );

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel listControlPanel = new JPanel (new BorderLayout());
    listControlPanel.add (sep1,BorderLayout.NORTH);
    listControlPanel.add (buttonPanel,BorderLayout.SOUTH);

    // --- Folder information
    
    // Folder image
    JLabel imageLabel = new JLabel();
    imageLabel.setHorizontalAlignment(JLabel.RIGHT);

    final JButton imageButton = new JButton(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    imageButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    imageButton.setHorizontalAlignment(JLabel.CENTER);
    imageButton.setBackground(Color.WHITE);
    imageButton.setPreferredSize(listType==LIST_TYPE.EPUB ? new Dimension(400,567) : new Dimension(320,180));
    imageButton.setToolTipText(res.getString("Edit"));
    imageButton.setMargin(new Insets(0,0,0,0));

    JScrollPane imageScroll = new JScrollPane(imageButton);

    JLabel imageRecommendedSize = new JLabel();
    imageRecommendedSize.setFont(imageRecommendedSize.getFont().deriveFont(Font.ITALIC));
    
    switch (listType) {
      case EPUB     : 
        imageLabel.setText(res.getString("Package.EPUBCover"));
        imageButton.setPreferredSize(new Dimension(400,567)); 
        imageButton.setEnabled(true);
        imageScroll.setPreferredSize(new Dimension(420,150));
        imageRecommendedSize.setText(res.getString("SimInfoEditor.RecommendedSize") + " (400 x 567)");
        break;
      case BOOK_APP : 
        imageLabel.setText(res.getString("Package.Splash"));
        imageButton.setPreferredSize(new Dimension(1200,1200)); 
        imageButton.setEnabled(true);
        imageScroll.setPreferredSize(new Dimension(420,150));
        imageRecommendedSize.setText(res.getString("SimInfoEditor.RecommendedSize") + " (2208 x 2208)");
        break;
      default:
      case OTHER    : 
        imageLabel.setText(res.getString("Package.FolderImage"));
        imageButton.setPreferredSize(new Dimension(320,180)); 
        imageButton.setEnabled(false);
        imageScroll.setPreferredSize(new Dimension(320,180));
        imageRecommendedSize.setText(res.getString("SimInfoEditor.RecommendedSize") + " (320 x 180)");
        break;
    }
    
    JPanel imageCenterPanel = new JPanel (new BorderLayout());
    imageCenterPanel.add(imageScroll, BorderLayout.CENTER);
    imageCenterPanel.add(imageRecommendedSize, BorderLayout.SOUTH);

    JPanel imagePanel = new JPanel (new BorderLayout());
    imagePanel.setBorder(new EmptyBorder(2,0,2,0));
    imagePanel.add(imageLabel, BorderLayout.WEST);
    imagePanel.add(imageCenterPanel, BorderLayout.CENTER);
    
    // Information
    
    JLabel infoLabel = new JLabel(res.getString("SimInfoEditor.Title"));
    infoLabel.setHorizontalAlignment(JLabel.RIGHT);
    
    JLabel authorLabel = new JLabel(res.getString("SimInfoEditor.Author"));
    authorLabel.setHorizontalAlignment(JLabel.RIGHT);

    JPanel infoLeftPanel = new JPanel(new GridLayout(0,1));
    infoLeftPanel.add(infoLabel);

    final JTextField nameField = new JTextField(); 
    nameField.setEditable(true);

    final JTextField authorField = new JTextField(); 
    authorField.setEditable(true);

    final JButton infoImageButton = new JButton(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    infoImageButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    infoImageButton.setHorizontalAlignment(JLabel.CENTER);
    infoImageButton.setBackground(Color.WHITE);
    infoImageButton.setToolTipText(res.getString("Edit"));
    infoImageButton.setPreferredSize(new Dimension(50,50));
    infoImageButton.setMargin(new Insets(0,0,0,0));
    infoImageButton.setEnabled(false);

    JPanel infoFieldsPanel = new JPanel (new GridLayout(0,1));
    infoFieldsPanel.add(nameField);
    infoFieldsPanel.add(authorField);

    JPanel infoCenterPanel = new JPanel (new BorderLayout());
    infoCenterPanel.add(infoFieldsPanel, BorderLayout.CENTER);

    switch (listType) {
      case EPUB     : 
        infoLeftPanel.add(authorLabel);
        break;
      case BOOK_APP : 
        infoLeftPanel.add(authorLabel);
        break;
      default:
      case OTHER    : 
        infoLabel.setText(res.getString("Package.FolderInfo"));
        nameField.setEditable(false);
        authorField.setEditable(false);
        infoCenterPanel.add(infoImageButton, BorderLayout.WEST);
        {
          JLabel infoRecommendedSize = new JLabel(res.getString("SimInfoEditor.RecommendedSize") + " (50 x 50)");
          infoRecommendedSize.setFont(infoRecommendedSize.getFont().deriveFont(Font.ITALIC));
          infoCenterPanel.add(infoRecommendedSize, BorderLayout.SOUTH);
        }
        break;
    }
    
    JPanel infoPanel = new JPanel (new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(2,0,2,0));
    infoPanel.add(infoLeftPanel, BorderLayout.WEST);
    infoPanel.add(infoCenterPanel, BorderLayout.CENTER);

    // --- Add actions and align labels
    
    Set<JComponent> labelSet = new HashSet<JComponent>();
    labelSet.add(infoLabel);
    labelSet.add(imageLabel);

    InterfaceUtils.makeSameDimension(labelSet);

    ActionListener imageListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!(e.getSource() instanceof JButton)) return;
        JButton button = (JButton) e.getSource();
        String filename = FileChooserUtil.chooseFilename(ejs, ejs.getExportDirectory(), "PNG", new String[]{"png"}, false);
        if (filename==null) {
          if (button==infoImageButton) {
            info.infoFile = null;
            infoImageButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
          }
          return;
        }
        if (!filename.toLowerCase().endsWith(".png")) {
          JOptionPane.showMessageDialog(button, 
              res.getString("SimInfoEditor.RequiredPNGimage")+"\n"+filename, 
              res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          return;
        }
        File file = new File(filename);
        if (!file.exists()) {
          JOptionPane.showMessageDialog(button, 
              res.getString("Package.FileMissing")+"\n"+filename, 
              res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          if (button==imageButton) info.imageFile = null;
          else info.infoFile = null;
          return;
        }
        if (button==imageButton) info.imageFile = file;
        else info.infoFile = file;
        Icon icon = ResourceLoader.getIcon(filename);
        button.setIcon(icon);
      }
    };
    imageButton.addActionListener(imageListener);
    infoImageButton.addActionListener(imageListener);

    // Folder checkboxes
    
    final JCheckBox readOnlyCB = new JCheckBox (res.getString("Package.ReadOnly"),true);
    readOnlyCB.setEnabled(false);
    
    final JCheckBox folderCB = new JCheckBox (res.getString("Package.SaveAsFolder"),false);
    folderCB.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        readOnlyCB.setEnabled(folderCB.isSelected());
        nameField.setEditable(folderCB.isSelected());
        authorField.setEditable(folderCB.isSelected());
        infoImageButton.setEnabled(folderCB.isSelected());
        imageButton.setEnabled(folderCB.isSelected());
      }
    });

    final JCheckBox htmlCB = new JCheckBox (res.getString("Package.EPUBIncludeHTML"),false);

    final JCheckBox chaptersCB = new JCheckBox (res.getString("Package.EPUBSeparateChapters"),false);

    final JRadioButton nestedStructureCB = new JRadioButton (res.getString("Package.EPUBNestedStructure"),false);
    final JRadioButton flatStructureCB = new JRadioButton (res.getString("Package.EPUBFlatStructure"),false);
    final JRadioButton strictStructureCB = new JRadioButton (res.getString("Package.EPUBStrictStructure"),true);

    final JRadioButton numberNoRB = new JRadioButton (res.getString("Package.NumberingNone"),true);
    final JRadioButton numberChapterRB = new JRadioButton (res.getString("Package.NumberingChapter"),false);
    final JRadioButton numberChapterAndSectionRB = new JRadioButton (res.getString("Package.NumberingChapterAndSection"),false);

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(numberNoRB);
    buttonGroup.add(numberChapterRB);
    buttonGroup.add(numberChapterAndSectionRB);

    JPanel checkboxPanel = new JPanel (new BorderLayout());
    
    switch (listType) {
      case EPUB     : 
        checkboxPanel.add(htmlCB, BorderLayout.WEST); 
        break;
      case BOOK_APP : 
        htmlCB.setText(res.getString("Package.IncludeHTML"));
        htmlCB.setSelected(true);
        chaptersCB.setSelected(true);
        checkboxPanel.add(htmlCB, BorderLayout.WEST); 
        break;
      default:
      case OTHER    : 
        checkboxPanel.add(folderCB, BorderLayout.WEST);
        checkboxPanel.add(readOnlyCB, BorderLayout.EAST);
        break;
    }
    
    // Put all folder info together
    
    Box folderPanel = Box.createVerticalBox();
    folderPanel.setBorder(new EmptyBorder(2,2,2,2));
    
    if (listType==LIST_TYPE.EPUB) {
      ButtonGroup group = new ButtonGroup();
      group.add(nestedStructureCB);
      group.add(flatStructureCB);
      group.add(strictStructureCB);
      
      JLabel structureLabel = new JLabel(res.getString("Package.EPUBDirectoryStructure"));
      JPanel structureLabelPanel = new JPanel(new BorderLayout());
      structureLabelPanel.setBorder(new EmptyBorder(2,5,0,0));
      structureLabelPanel.add(structureLabel, BorderLayout.WEST);

      JPanel structureButtonsPanel = new JPanel(new GridLayout(0,1));
      structureButtonsPanel.setBorder(new EmptyBorder(0,10,5,0));
      structureButtonsPanel.add(nestedStructureCB);
      structureButtonsPanel.add(flatStructureCB);
      structureButtonsPanel.add(strictStructureCB);

      JPanel structurePanel = new JPanel(new BorderLayout());
      structurePanel.add(structureLabel, BorderLayout.NORTH);
      structurePanel.add(structureButtonsPanel, BorderLayout.CENTER);

      folderPanel.add(structurePanel);
    }

    folderPanel.add(checkboxPanel);

    if (listType!=LIST_TYPE.OTHER) {
      JPanel checkbox2Panel = new JPanel (new BorderLayout());
      checkbox2Panel.add(chaptersCB, BorderLayout.WEST);
      folderPanel.add(checkbox2Panel);

      JLabel radioLabel = new JLabel(res.getString("Package.NumberingType"));
      JPanel radioPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
      radioPanel.add(radioLabel);
      radioPanel.add(numberNoRB);
      radioPanel.add(numberChapterRB);
      radioPanel.add(numberChapterAndSectionRB);
      folderPanel.add(radioPanel);
    }
    
    if (listType!=LIST_TYPE.BOOK_APP) folderPanel.add(imagePanel);
    folderPanel.add(infoPanel);

    // About panel    
    final JTextArea aboutArea = new JTextArea();
    
    // Save and read
    final JButton saveButton = new JButton(res.getString("ListDialog.SaveConfig"));
    final JButton readButton = new JButton(res.getString("ListDialog.ReadConfig"));
    
    ActionListener configListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!(e.getSource() instanceof JButton)) return;
        JButton button = (JButton) e.getSource();
        String filename = FileChooserUtil.chooseFilename(ejs, new File(ejs.getExportDirectory(),configFilename), "XML", new String[]{"xml"}, button==saveButton);
        if (filename==null) return;
        File file = new File(filename);
        if (button==saveButton) {
          info.asFolder = folderCB.isSelected();
          info.readOnly = readOnlyCB.isSelected();
          info.name = nameField.getText();
          info.subtitle = authorField.getText();
          info.about = aboutArea.getText();
          info.includeHTMLFiles = htmlCB.isSelected();
          info.separateChapters = chaptersCB.isSelected();
          if (flatStructureCB.isSelected())        info.foldersStructure = STRUCTURE_FLAT;
          else if (strictStructureCB.isSelected()) info.foldersStructure = STRUCTURE_STRICT;
          else                                     info.foldersStructure = STRUCTURE_NESTED;
          if (numberChapterRB.isSelected())                info.numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER;
          else if (numberChapterAndSectionRB.isSelected()) info.numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION;
          else                                             info.numberingOption = CreateListDialog.NUMBERING_OPTION_NONE;
          info.list.clear();
          for (int i=0,n=listModel.size(); i<n; i++) info.list.add(listModel.get(i));
          info.saveToFile(file,saveButton);
        }
        else if (button==readButton) {
          if (!file.exists()) {
            JOptionPane.showMessageDialog(button, 
                res.getString("Package.FileMissing")+"\n"+filename, 
                res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
            return;
          }
          configFilename = file.getName();
          info.readFromFile(file,readButton);
          // Fill the fields
          folderCB.setSelected(info.asFolder);
          readOnlyCB.setSelected(info.readOnly);
          nameField.setText(info.name);
          authorField.setText(info.subtitle);
          aboutArea.setText(info.about);

          if (info.imageFile!=null) {
            Icon icon = ResourceLoader.getIcon(info.imageFile.getAbsolutePath());
            if (icon!=null) imageButton.setIcon(icon);
            else {
              info.imageFile = null;  
              imageButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
            }
          }
          else imageButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));

          if (info.infoFile!=null) {
            Icon icon = ResourceLoader.getIcon(info.infoFile.getAbsolutePath());
            if (icon!=null) infoImageButton.setIcon(icon);
            else {
              info.infoFile = null;  
              infoImageButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
            }
          }
          else infoImageButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));


          htmlCB.setSelected(info.includeHTMLFiles);
          chaptersCB.setSelected(info.separateChapters);
          switch(info.foldersStructure) {
            case STRUCTURE_FLAT : flatStructureCB.setSelected(true); break;
            case STRUCTURE_STRICT : strictStructureCB.setSelected(true); break;
            default : 
            case STRUCTURE_NESTED : nestedStructureCB.setSelected(true); break;
          }
          switch(info.numberingOption) {
            case CreateListDialog.NUMBERING_OPTION_CHAPTER : numberChapterRB.setSelected(true); break;
            case CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION : numberChapterAndSectionRB.setSelected(true); break;
            default : 
            case CreateListDialog.NUMBERING_OPTION_NONE : numberNoRB.setSelected(true); break;
          }
          listModel.clear();
          for (int i=0,n=info.list.size(); i<n; i++) listModel.addElement(info.list.get(i));
        }
      };
    };
    saveButton.addActionListener(configListener);
    readButton.addActionListener(configListener);
    
    JPanel configButtonsPanel = new JPanel(new GridLayout(1,0));
    configButtonsPanel.setBorder(new EmptyBorder(2,5,0,0));
    configButtonsPanel.add(readButton);
    configButtonsPanel.add(saveButton);

    JPanel configPanel = new JPanel(new BorderLayout());
    configPanel.add(new JSeparator(JSeparator.HORIZONTAL),BorderLayout.NORTH);
    configPanel.add(configButtonsPanel,BorderLayout.CENTER);
    
    // Put everything together
    
    JPanel centralPanel = new JPanel (new BorderLayout());
    centralPanel.add(listControlPanel,BorderLayout.NORTH);
    centralPanel.add(scrollPane,BorderLayout.CENTER);
    centralPanel.add(folderPanel,BorderLayout.SOUTH);

    // Put everything in the dialog
    
    dialog.getContentPane().setLayout (new BorderLayout(5,0));
    dialog.getContentPane().add (configPanel,BorderLayout.SOUTH);

    if (listType!=LIST_TYPE.BOOK_APP) {
      dialog.getContentPane().add (centralPanel,BorderLayout.CENTER);
    }
    else {
      JScrollPane aboutScroll = new JScrollPane(aboutArea);
      JTabbedPane tabbedPanel = new JTabbedPane();
      tabbedPanel.add(res.getString("Package.Configuration"), centralPanel);
      tabbedPanel.add(res.getString("Package.About"), aboutScroll);
      
      dialog.getContentPane().add (tabbedPanel,BorderLayout.CENTER);
    }
    
    
    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { info.list.clear(); }
      }
    );

    dialog.validate();
    dialog.pack();
    dialog.setModal(true);
    dialog.setTitle (listType==LIST_TYPE.EPUB ? res.getString("Package.EPUBSeveralXMLSimulationsMessage") : res.getString("Package.PackageSeveralXMLSimulationsMessage"));
//    dialog.setTitle (title);
    dialog.setLocationRelativeTo (parentComponent);
    dialog.setVisible (true);

    if (info.list.isEmpty()) return info;
    
    if (listType==LIST_TYPE.OTHER) {
      if (folderCB.isSelected()) {
        info.readOnly = readOnlyCB.isSelected();
        info.name = nameField.getText().trim();
        info.subtitle = authorField.getText().trim();
      }
      else {
        info.name = null;
        info.imageFile = null;
      }
      return info;
    }
    // EPUB and BOOK_APP
    info.name = nameField.getText().trim();
    info.subtitle = authorField.getText().trim();
    info.about = aboutArea.getText().trim();
    info.includeHTMLFiles = htmlCB.isSelected();
    info.separateChapters = chaptersCB.isSelected();
    if      (flatStructureCB.isSelected())   info.foldersStructure = STRUCTURE_FLAT;
    else if (strictStructureCB.isSelected()) info.foldersStructure = STRUCTURE_STRICT;
    else                                     info.foldersStructure = STRUCTURE_NESTED;

    if (numberChapterRB.isSelected()) info.numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER;
    else if (numberChapterAndSectionRB.isSelected()) info.numberingOption = CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION;
    else info.numberingOption = CreateListDialog.NUMBERING_OPTION_NONE;

    return info;
  }

  
}
