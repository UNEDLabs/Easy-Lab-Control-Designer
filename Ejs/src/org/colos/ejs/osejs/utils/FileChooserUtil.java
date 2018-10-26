package org.colos.ejs.osejs.utils;

import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.colos.ejs.osejs.Osejs;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;

public class FileChooserUtil {
  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private final ResourceUtil res = new ResourceUtil("Resources");

  static private FileChooserUtil sEjsSFileChooser;
  static private FileChooserUtil sHtmlFileChooser;
  static private FileChooserUtil sDirectoryFileChooser;
  static private FileChooserUtil sLibraryFileChooser;

  static public FileChooserUtil getEjsSFileChooser(Osejs ejs) {
    if (sEjsSFileChooser==null) {
      String description = res.getString("Osejs.File.Description").toLowerCase()+ " ("+sysRes.getString("Osejs.File.Extension")+")";
      sEjsSFileChooser = new FileChooserUtil(ejs,ejs.getSourceDirectory(),description,sysRes.getString("Osejs.File.Extension").split(","));
    }
    return sEjsSFileChooser;
  }
  
  static public FileChooserUtil getDirectoryFileChooser(Osejs ejs) {
    if (sDirectoryFileChooser==null) {
      sDirectoryFileChooser = new FileChooserUtil(ejs,ejs.getSourceDirectory()); //new String[]{"*.*"});
    }
    return sDirectoryFileChooser;
  }
  
  static public FileChooserUtil getHtmlFileChooser(Osejs ejs) {
    if (sHtmlFileChooser==null) {
      sHtmlFileChooser = new FileChooserUtil(ejs,ejs.getSourceDirectory().getParentFile(),"HTML,XHTML",new String[]{"html","htm","xhtml"});
    }
    return sHtmlFileChooser;
  }

  static public FileChooserUtil getLibraryFileChooser(Osejs ejs) {
    if (sLibraryFileChooser==null) {
      if (ejs.supportsJava()) sLibraryFileChooser = new FileChooserUtil(ejs,ejs.getSourceDirectory().getParentFile(),"Java",new String[]{"java"});
      else sLibraryFileChooser = new FileChooserUtil(ejs,ejs.getSourceDirectory().getParentFile(),"Javascript",new String[]{"js"});
    }
    return sLibraryFileChooser;
  }

  /**
   * Choose a file. 
   * @param ejs
   * @param targetFile if a file, then used as the proposed target file. If a directory, then used only as base dir
   * @param description
   * @param extensions
   * @param toSave
   * @return
   */
  static public String chooseFilename(Osejs ejs, File targetFile, String description, String[] extensions, boolean toSave) {
    String targetName;
    File parentFile = targetFile.isDirectory() ? targetFile : targetFile.getParentFile();
    if (ejs.useNativeFileChooser()) {
      FileChooserUtil chooser = new FileChooserUtil(ejs, parentFile, description, extensions);
      if (!targetFile.isDirectory()) chooser.setCurrentFile(targetFile);
      File file = toSave ? chooser.showSaveDialog(ejs.getMainPanel()) : chooser.showOpenDialog(ejs.getMainPanel());
      targetName = (file==null) ? null : file.getAbsolutePath();
    }
    else {
      JFileChooser chooser = OSPRuntime.createChooser(description,extensions,parentFile); 
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setCurrentDirectory(parentFile);
      if (!targetFile.isDirectory()) chooser.setSelectedFile(targetFile);
      targetName = OSPRuntime.chooseFilename(chooser,ejs.getMainPanel(), toSave);
    }
    return targetName;
  }

  /**
   * Choose a folder. 
   * @param ejs
   * @param targetFile if a file, then used as the proposed target file. If a directory, then used only as base dir
   * @param description
   * @param extensions
   * @param toSave
   * @return
   */
  static public String chooseFoldername(Osejs ejs, File targetFile, String description, String[] extensions, boolean toSave) {
    String targetName;
    File parentFile = targetFile.getParentFile();
    if (!toSave && ejs.useNativeFileChooser()) {
      FileChooserUtil chooser = new FileChooserUtil(ejs, parentFile);
      chooser.setFilefilter(description, extensions);
      chooser.setCurrentFile(targetFile);
      File file = chooser.showOpenDialog(ejs.getMainPanel());
      targetName = (file==null) ? null : file.getAbsolutePath();
    }
    else {
      JFileChooser chooser = OSPRuntime.createChooser(description,extensions,parentFile); 
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setCurrentDirectory(parentFile);
      chooser.setSelectedFile(targetFile);
      targetName = OSPRuntime.chooseFilename(chooser,ejs.getMainPanel(), toSave);
    }
    return targetName;
  }

  /**
   * Choose several files. 
   * @param ejs
   * @param targetFile if a file, then used as the proposed target file. If a directory, then used only as base dir
   * @param description
   * @param extensions
   * @param toSave
   * @return
   */
  static public File[] chooseFilenames(Osejs ejs, File parentFile, String description, String[] extensions) {
    if (ejs.useNativeFileChooser()) {
      FileChooserUtil chooser = new FileChooserUtil(ejs, parentFile, description, extensions);
      return chooser.showOpenMultipleDialog(ejs.getMainPanel());
    }
    JFileChooser chooser = OSPRuntime.createChooser(description,extensions,parentFile); 
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.setCurrentDirectory(parentFile);
    chooser.setDialogTitle(res.getString("EditorFor.ChooseOne"));
    int result = chooser.showOpenDialog(ejs.getMainPanel());
    if (result==JFileChooser.APPROVE_OPTION) return chooser.getSelectedFiles();
    return null;
  }

  // -------------------------------------------------
  // Non-static part
  // -------------------------------------------------

  private Osejs mEjs;
  private File mHomeDir;
  private String mDescription;
  private FileDialog mFileChooser;
  private boolean mDirsOnly;

  /**
   * Creates a FileChooser for directories only 
   * @param ejs
   * @param homeDir
   */
  public FileChooserUtil(Osejs ejs,File homeDir) {
    mEjs = ejs;
    mHomeDir = homeDir;
    mDescription = "";
    System.setProperty("apple.awt.fileDialogForDirectories", "true");
    mFileChooser = new FileDialog(mEjs.getMainFrame(),sysRes.getString("Osejs.Title")+" - "+res.getString("Osejs.File.ChooseDirectory"),FileDialog.LOAD);
    mFileChooser.pack();
    mFileChooser.setPreferredSize(res.getDimension("FileChooser.Size"));
    System.setProperty("apple.awt.fileDialogForDirectories", "false");
    this.mDirsOnly = true;
  }
  
  /**
   * Creates a FileChooser for files with the given extension 
   * @param ejs
   * @param homeDir
   * @param description
   * @param extensions
   */
  public FileChooserUtil(Osejs ejs,File homeDir, String description, String[] extensions) {
    mEjs = ejs;
    mHomeDir = homeDir;
    if (description!=null) mDescription = " " + description;
    else mDescription = "";
    mFileChooser = new FileDialog(mEjs.getMainFrame());
    mFileChooser.setMultipleMode(false);
    if (extensions!=null) mFileChooser.setFilenameFilter(new ExtensionFileFilter(extensions));
    mFileChooser.pack();
    mFileChooser.setPreferredSize(res.getDimension("FileChooser.Size"));
    mDirsOnly = false;
  }
  
  public void clear() {
    mFileChooser.setFile("");
  }

  private void prepareChooser() {
    File currentDir = mEjs.getCurrentDirectory();
    if (!FileUtils.isRelative(currentDir, mHomeDir)) currentDir = mHomeDir;
    mFileChooser.setDirectory(currentDir.getAbsolutePath());
    mFileChooser.setMultipleMode(false);
    mFileChooser.pack();
    mFileChooser.setLocationRelativeTo(mEjs.getMainPanel());
    if (mDirsOnly) System.setProperty("apple.awt.fileDialogForDirectories", "true");
    else System.setProperty("apple.awt.fileDialogForDirectories", "false");
  }

  public void setCurrentFile(File file) {
    mFileChooser.setDirectory(file.getParent());
    mFileChooser.setFile(file.getName());
  }
    
  public void setFilefilter (String description, String[] extensions) {
    if (description!=null) mDescription = " " + description;
    if (extensions!=null) mFileChooser.setFilenameFilter(new ExtensionFileFilter(extensions));
  }
  
  /**
   * Get file to be opened
   * @return File The file selected. Null if cancelled
   */
  public File showOpenDialog(Component parent) {
    prepareChooser();
    if (!mDirsOnly) mFileChooser.setTitle(sysRes.getString("Osejs.Title")+" - "+mEjs.getOpenString()+mDescription);
    mFileChooser.setMode(FileDialog.LOAD);
    mFileChooser.setLocationRelativeTo(parent);
    mFileChooser.setVisible(true);
    
    String filename = mFileChooser.getFile();
    if (filename==null) {
      if (mDirsOnly) System.setProperty("apple.awt.fileDialogForDirectories", "false");
      return null;
    }
    File file = new File(mFileChooser.getDirectory(),filename);
    if (mDirsOnly && !file.isDirectory()) file = file.getParentFile();
    if(!file.exists()) {
      JOptionPane.showMessageDialog(parent, DisplayRes.getString("GUIUtils.FileDoesntExist")+" "+file.getName(), //$NON-NLS-1$ //$NON-NLS-2$
          DisplayRes.getString("GUIUtils.FileChooserError"), //$NON-NLS-1$
          JOptionPane.ERROR_MESSAGE);
      if (mDirsOnly) System.setProperty("apple.awt.fileDialogForDirectories", "false");
      return null;
    }
    if (mDirsOnly) System.setProperty("apple.awt.fileDialogForDirectories", "false");
    return file;
  }

  public File[] showOpenMultipleDialog(Component parent) {
    prepareChooser();
    mFileChooser.setMultipleMode(true);
    mFileChooser.setMode(FileDialog.LOAD);
    mFileChooser.setTitle(sysRes.getString("Osejs.Title")+" - "+mEjs.getOpenString()+mDescription);
    mFileChooser.setLocationRelativeTo(parent);
    mFileChooser.setVisible(true);
    
    File[] files = mFileChooser.getFiles();
    return files;
  }

  /**
   * Get file to be saved
   * @return File The file selected. Null if cancelled
   */
  public File showSaveDialog(Component parent) {
    prepareChooser();
    if (!mDirsOnly) mFileChooser.setTitle(sysRes.getString("Osejs.Title")+" - "+res.getString("Osejs.File.Save")+ mDescription);
    mFileChooser.setMode(FileDialog.SAVE);
    mFileChooser.setLocationRelativeTo(parent);
    mFileChooser.setVisible(true);
    
    String filename = mFileChooser.getFile();
    if (filename==null) return null;
    File file = new File(mFileChooser.getDirectory(),filename);
    if (mDirsOnly) {
      if (!file.isDirectory()) file = file.getParentFile();
    }
//    else if (file.exists()) {
//      int selected = JOptionPane.showConfirmDialog(parent, DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+" "+file.getName() //$NON-NLS-1$ //$NON-NLS-2$
//          +DisplayRes.getString("DrawingFrame.QuestionMark"), DisplayRes.getString( "DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
//      if (selected!=JOptionPane.YES_OPTION) {
//        return null;
//      }
//    }
    return file;
  }
  
  /**
   * This file filter matches all files with a given set of
   * extensions.
   */
  static private class ExtensionFileFilter implements java.io.FilenameFilter {
    private java.util.ArrayList<String> mExtensions = new java.util.ArrayList<String>();

    ExtensionFileFilter(String... extensions) {
      for (String extension : extensions) {
        mExtensions.add(extension.toLowerCase());
      }
    }
    
    public boolean accept(File dir, String name) {
      if (name==null) return false;
      name = name.toLowerCase();
      // check if the file name ends with any of the extensions
      for (String extension : mExtensions) {
        if (name.endsWith(extension))  return true;
      }
      return false;
    }

  }

}
