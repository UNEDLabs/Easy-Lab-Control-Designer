/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.util.*;
import java.awt.*;
import java.io.File;

import javax.swing.*;

import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.Osejs;
import org.opensourcephysics.display.OSPRuntime;

//--------------------

public class EditorForFile {
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  static JFileChooser fileDialog;
  static ArrayList<String> auxList;
  
  static private Hashtable<String,JFileChooser> dialogs = new Hashtable<String,JFileChooser>();

  public static String edit (Osejs _ejs, Component parent, String _extension) {
    String[] sel = edit (_ejs, parent, _extension, false, javax.swing.JFileChooser.FILES_ONLY);
    if (sel==null) return null;
    return sel[0];
  }

  public static String edit (Osejs _ejs, Component parent, String _extension, String _title) {
    String[] sel = edit (_ejs, parent, _extension, false, javax.swing.JFileChooser.FILES_ONLY, _title);
    if (sel==null) return null;
    return sel[0];
  }

  public static String edit (Osejs _ejs, Component parent, String _extension, String _title,int _fileSelectionMode) {
    String[] sel = edit (_ejs, parent, _extension, false, _fileSelectionMode, _title);
    if (sel==null) return null;
    return sel[0];
  }

  public static String[] edit (Osejs _ejs, Component parent, String _extension, 
      boolean _multipleFiles, int _fileSelectionMode) {
    return edit (_ejs, parent, _extension,_multipleFiles, _fileSelectionMode,null);
  }
  
  static private boolean isImageFile (String _extensions) {
    if (_extensions.equals("image") || _extensions.equals("imageFile") || _extensions.equals("selectedimage")) return true;
    if (_extensions.equals("imageOn") || _extensions.equals("imageOff")) return true;
    if (_extensions.equals("ImageUrl") || _extensions.equals("ImageOnUrl") || _extensions.equals("ImageOffUrl")) return true;
    if (_extensions.equals("Logo")) return true;
    return false;
  }

  /** 
   * Added for EJS informaton provider feature
   * 
   * @param _ejs
   * @param _title
   * @param parent
   * @param _extensions
   * @param _descriptions
   * @return
   */
  public static String edit (Osejs _ejs, String _title, Component parent, String _description, String _extensions) {
    _ejs.saveXMLFileFirst(parent);
    fileDialog = dialogs.get(_extensions);
    if (fileDialog==null) {
      fileDialog = OSPRuntime.createChooser(_description, _extensions.split(","),_ejs.getSourceDirectory().getParentFile());
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(fileDialog);
      fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
      fileDialog.setMultiSelectionEnabled(false);
      dialogs.put(_extensions,fileDialog);
    }
    fileDialog.setDialogTitle (_title);
    fileDialog.setCurrentDirectory(_ejs.getCurrentDirectory());
    if (isImageFile(_extensions)) { // add the OSP icons
      fileDialog.setAccessory(OSPImagesAccesory.getScrollPanel());
    }
    fileDialog.setMultiSelectionEnabled(false);
    fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
    int option = fileDialog.showDialog(parent, res.getString("EditorFor.Ok"));
    if (option != JFileChooser.APPROVE_OPTION) return null;
    return correctFilename(_ejs,fileDialog.getSelectedFile(),_extensions);
  }
  
  public static String[] edit (Osejs _ejs, Component parent, String _extension, boolean _multipleFiles, int _fileSelectionMode, String _title) {
    _ejs.saveXMLFileFirst(parent);
    String descr=null, exten=null;
    if (_extension==null) {
      _extension = "_any_";
    }
    else if (_extension.equals("Ejsfile")) {
      descr = res.getString("Osejs.File.Description");
      exten = sysRes.getString("Osejs.File.Extension");
    }
    else {
      descr = res.getString("View.FileDescription."+_extension);
      exten = sysRes.getString("View.FileExtension."+_extension);
    }
    String[] filenameList = null;
    fileDialog = dialogs.get(_extension);
    if (fileDialog==null) {
      if (exten==null) fileDialog = OSPRuntime.getChooser();
      else fileDialog = OSPRuntime.createChooser(descr, exten.split(","),_ejs.getSourceDirectory().getParentFile());
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(fileDialog);
      fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
      fileDialog.setMultiSelectionEnabled(false);
      dialogs.put(_extension,fileDialog);
    }
    if (_title!=null) fileDialog.setDialogTitle (_title);
    else if (descr!=null) fileDialog.setDialogTitle (descr);
    fileDialog.setCurrentDirectory(_ejs.getCurrentDirectory());
    auxList = new ArrayList<String>();
    if (isImageFile(_extension)) { // add the OSP icons
      fileDialog.setAccessory(OSPImagesAccesory.getScrollPanel());
    }
    fileDialog.setMultiSelectionEnabled(_multipleFiles);
//    if (_allowDirectories) fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
//    else fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
    fileDialog.setFileSelectionMode(_fileSelectionMode);
    int option = fileDialog.showDialog(parent, res.getString("EditorFor.Ok"));
    if (auxList.size()>0) return auxList.toArray(new String[0]);
    if (option != JFileChooser.APPROVE_OPTION) return null;
    if (_multipleFiles) {
      File[] fileList = fileDialog.getSelectedFiles();
      filenameList = new String[fileList.length];
      for (int i=0; i<fileList.length; i++) filenameList[i] = correctFilename(_ejs,fileList[i],exten);
    }
    else filenameList = new String[] { correctFilename(_ejs,fileDialog.getSelectedFile(),exten) };
    return filenameList;
  }

  static private String correctFilename (Osejs _ejs, File _aFile, String _exten) {
    String filename  = FileUtils.getPath(_aFile);
    if (_exten!=null && filename.indexOf(".") < 0) filename += "." + _exten;
    return _ejs.getRelativePath(filename);
  }

  static private class OSPImagesAccesory  {
   
    static JComponent topPanel;
    
    static JComponent getScrollPanel(){ return new JScrollPane(topPanel); }
    
    static {
      JPanel panel = new JPanel(new GridLayout(0,3,2,2));
      panel.setBorder(new javax.swing.border.EmptyBorder(2,0,2,0));
      java.awt.event.MouseListener listener = new java.awt.event.MouseAdapter () {
        public void mouseClicked(java.awt.event.MouseEvent e) {
          auxList.add(e.getComponent().getName());
          fileDialog.approveSelection();
        }
      };
      panel.add (createLabel("controls/images/play.gif",listener));
      panel.add (createLabel("controls/images/pause.gif",listener));
      panel.add (createLabel("controls/images/stop.gif",listener));
      
      panel.add (createLabel("controls/images/stepforward.gif",listener));
      panel.add (createLabel("controls/images/stepback.gif",listener));
      panel.add (createLabel("controls/images/continue.gif",listener));
      
      panel.add (createLabel("controls/images/reset.gif",listener));
      panel.add (createLabel("controls/images/initial.gif",listener));
      panel.add (createLabel("controls/images/reset1.gif",listener));
      
      panel.add (createLabel("controls/images/reset2.gif",listener));
      panel.add (createLabel("controls/images/cycle.gif",listener));
      panel.add (createLabel("controls/images/clear.gif",listener));
      
      panel.add (createLabel("controls/images/forward.gif",listener));
      panel.add (createLabel("controls/images/time.gif",listener));
      panel.add (createLabel("controls/images/notime.gif",listener));
      
      panel.add (createLabel("controls/images/close.gif",listener));
      panel.add (createLabel("controls/images/folder.gif",listener));
      panel.add (createLabel("controls/images/inspectfolder.gif",listener));
      
      panel.add (createLabel("controls/images/window.gif",listener));
      panel.add (createLabel("controls/images/inspect.gif",listener));
      panel.add (createLabel("controls/images/value.gif",listener));
      
      panel.add (createLabel("controls/images/hilite.gif",listener));
      panel.add (createLabel("controls/images/erase.gif",listener));
      panel.add (createLabel("controls/images/i_erase.gif",listener));

      panel.add (createLabel("controls/images/caution.gif",listener));
      panel.add (createLabel("controls/images/wrench.gif",listener));
      panel.add (createLabel("controls/images/wrench_monkey.gif",listener));

      panel.add (createLabel("controls/images/pdf.gif",listener));
      panel.add (createLabel("controls/images/power_on.png",listener));
      panel.add (createLabel("controls/images/power_off.png",listener));
      
      // additional icons (no OSP icons)
      panel.add (createLabel("controls/images/expand.png",listener));
      panel.add (createLabel("controls/images/download.png",listener));
      panel.add (createLabel("controls/images/apple.png",listener));
      panel.add (createLabel("controls/images/android.png",listener));      
      
      topPanel = new JPanel(new BorderLayout());
      topPanel.add(panel,BorderLayout.NORTH);
      //scrollPanel = new JScrollPane(topPanel);
    }
    
    static private JLabel createLabel (String imageName, java.awt.event.MouseListener _listener) {
      JLabel label = new JLabel ();
      String iconName = "/org/opensourcephysics/resources/"+imageName;
      Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(iconName);
      label.setIcon(icon);
      String plainName = imageName;
      int index = plainName.lastIndexOf('/');
      if (index>0) plainName = plainName.substring(index+1);
      index = plainName.lastIndexOf('.');
      if (index>0) plainName = plainName.substring(0,index);
      label.setToolTipText(plainName);
      label.setName(iconName);
      label.addMouseListener(_listener);
      label.setHorizontalAlignment(SwingConstants.CENTER);
      return label;
    }
    
  }
}
