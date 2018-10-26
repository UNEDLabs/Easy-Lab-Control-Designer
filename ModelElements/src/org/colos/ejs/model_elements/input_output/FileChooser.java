package org.colos.ejs.model_elements.input_output;

import java.awt.Component;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.display.OSPRuntime;

/**
 * Encapsulates access to org.opensourcephysics.display.OSPRuntime methods to choose files
 * @author Francisco Esquembre
 * @version 1.0, August 2010
 *
 */
public class FileChooser {
  static boolean sCanAccessDisk = true;
  static {
    try { System.getProperty("user.home"); }
    catch (Exception exc) { sCanAccessDisk = false; }
  }
  
  private Model model;
  private String description;
  private String extensions;
  private javax.swing.JFileChooser fileChooser;
  private ExtensionFileFilter fileFilter;
  private String initialPath;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   */
  public FileChooser(Model _model, String _description, String _extensions) {
    this.model = _model;
    if (sCanAccessDisk) {
      this.fileChooser =  new javax.swing.JFileChooser();
      this.description = _description;
      this.extensions = _extensions;
      this.fileFilter = new ExtensionFileFilter();
      fileChooser.setFileFilter(fileFilter);
      File initialDir = new File(".");
      initialPath = initialDir.getAbsolutePath();
      fileChooser.setCurrentDirectory(initialDir);
    }
    else this.fileChooser = null;
    
  }

  public String chooseFilename(Component _parentComponent, boolean _toSave) {
    if (fileChooser==null) {
      JOptionPane.showMessageDialog(null,"This simulation does NOT have disk access!","Disk Access Error",JOptionPane.ERROR_MESSAGE);
      return null;
    }
    if (description!=null) fileFilter.setDescription(ModelElementsUtilities.getValue(model,description));
    if (extensions!=null) fileFilter.setExtensions(ModelElementsUtilities.getValue(model,extensions));
    String filename = OSPRuntime.chooseFilename(fileChooser, _parentComponent, _toSave);
    if (filename==null) return null;
    // Make it relative (if it is relative) to the initial path
    if (filename.startsWith(initialPath)) filename = "." + filename.substring(initialPath.length());
    return filename;
  }
  
  /**
   * Returns the description
   * @return
   */
  public String getDescription() {
    return ModelElementsUtilities.getValue(model,description);
  }

  /**
   * Returns the extensions
   * @return
   */
  public String getExtensions() {
    return ModelElementsUtilities.getValue(model,extensions);
  }
  
  /**
   * This file filter matches all files with a given set of
   * extensions.
   */
  static private class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
    private String description = ""; //$NON-NLS-1$
    private java.util.ArrayList<String> extensions = new java.util.ArrayList<String>();

    public void setExtensions(String _extensions) {
      extensions.clear();
      StringTokenizer tkn = new StringTokenizer(_extensions,",; ");
      while (tkn.hasMoreTokens()) {
        String ext = tkn.nextToken().trim(); 
        if (!ext.startsWith(".")) ext = "."+ext;
        extensions.add(ext.toLowerCase());
      }
    }

    public String toString() {
      return description;
    }
    
    public void setDescription(String aDescription) {
      description = aDescription;
    }

    public String getDescription() {
      return description;
    }

    public boolean accept(File f) {
      if(f.isDirectory()) {
        return true;
      }
      String name = f.getName().toLowerCase();
      // check if the file name ends with any of the extensions
      for(int i = 0; i<extensions.size(); i++) {
        if(name.endsWith(extensions.get(i))) {
          return true;
        }
      }
      return false;
    }

  }
}
