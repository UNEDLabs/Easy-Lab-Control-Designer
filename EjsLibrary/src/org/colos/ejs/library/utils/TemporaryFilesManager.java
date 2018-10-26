package org.colos.ejs.library.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.ResourceLoader;


/**
 * This is a utility class used to extract files to temporary directories for use by a program.
 * When the program terminates, it should call the manager clear() method, to make sure it cleans
 * all temporary files and directories created
 * @author Paco
 *
 */
public class TemporaryFilesManager {

  static private final Set<File> toBeRemoved = new HashSet<File>();
  static private boolean cancelled = false;
  
  /*
   * Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
//        System.err.println("Quitting");
        if (ejs.closedNormally) return;
//        System.err.println("Actions on quit");
//        if (ejs.isChanged()) { // This hangs the program
//          Object[] options = new Object[] {res.getString("Osejs.File.SaveChanges"),res.getString("Osejs.File.IgnoreChanges")};
//          String message = ejs.getPathRelativeToSourceDirectory(FileUtils.getPath(ejs.currentXMLFile))+"\n"+res.getString("Osejs.WantToSaveBeforeExit");
//          int option = JOptionPane.showOptionDialog(ejs.mainPanel, message, res.getString("Osejs.File.SimulationChanged"), 
//              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//          if (option==0) ejs.saveFile(ejs.currentXMLFile);
//        }
        ejs.getOptions().save();
        org.colos.ejs.external.BrowserForSimulink.exitMatlab();
        ejs.getProcessDialog().killAllProcesses();
      }
    });
  }

   */
  
  /*
   * Clears all temporary files and directories extracted by the manager
   * Static method to call when your program exits.
   */
  static public void clear() {
    for (File file : toBeRemoved) {
      if (!file.exists()) continue;
      if (file.isDirectory()) JarTool.remove(file);
      else file.delete();
    }
    toBeRemoved.clear();
  }
  
  /**
   * Cancels whatever process is underway (that can be cancelled)
   */
  static public void cancelProcess() { cancelled = true; }

  
  /**
   * Creates a temporary directory under the given base directory. The directory will be completely removed when the clear() method is called
   * @param _prefix The prefix for the name of the temporary directory. If null or smaller than three characters, the prefix "_ejs_" will be used instead
   * @param _baseDir The base directory. If null, the system decides its location 
   * @return the created directory, null if it could not be created
   */
  static public File createTemporaryDirectory(String _prefix, File _baseDir) {
    try {
      if (_prefix==null || _prefix.length()<3) _prefix = "_ejs_";
      _baseDir.mkdirs();
      File tmpFile = File.createTempFile(_prefix, ".tmp", _baseDir);
//      System.out.println ("Created temp file "+tmpFile.getAbsolutePath());
      String name = tmpFile.getName();
      File parentDir = tmpFile.getParentFile();
      tmpFile.delete();
      tmpFile = new File(parentDir, name+"/temp");
      tmpFile.mkdirs();
      File tmpDir = tmpFile.getParentFile();
      tmpFile.delete();
      tmpDir.deleteOnExit();
      toBeRemoved.add(tmpDir); // this makes sure any subfile will also be removed
//      System.out.println ("Created dir "+tmpDir.getAbsolutePath());
      return tmpDir;
    }
    catch (Exception _exc) {
      _exc.printStackTrace();
      return null;
    }
  }

  /*
   * Extracts a file (located using ResourceLoader) into the given directory
   */
  static public File extractToDirectory(String _resourceString, File _targetDirectory, boolean _verbose) {
    try {
      File destFile = new File(_targetDirectory,_resourceString);
      destFile.deleteOnExit();
      destFile.getParentFile().mkdirs();
      InputStream inputStream = ResourceLoader.openInputStream(_resourceString);
      FileOutputStream fout = new FileOutputStream(destFile);
      BufferedInputStream in = new BufferedInputStream(inputStream);
      int c;
      while ((c = in.read()) != -1) fout.write(c);
      in.close();
      fout.close();
      if (_verbose) System.out.println("EJS library message: resource "+_resourceString+ " extracted into " + destFile.getAbsolutePath());
      return destFile;
    }
    catch(Exception exc) {
      System.out.println("EJS library warning: resource "+_resourceString+ " could NOT be extracted");
      exc.printStackTrace();
      return null;
    }
  }
  
  /*
   * Expands a ZIP file into the given directory.
   * Does not ask for possible overwrites
   * @return the Set of extracted files
   */
  static public Set<File> expandZip(File _zipFile, File _targetDirectory, javax.swing.JLabel _label, String _prefix) {
    try { return expandZip(new FileInputStream(_zipFile),_targetDirectory,_label,_prefix); }
    catch (Exception exc) { return null; }
  }

  /*
   * Expands a ZIP file into the given directory. Can be cancelled using the cancelProcess() method.
   * Does not ask for possible overwrites
   * @param _zipStream The InputStream to read from
   * @param _targetDirectory The target directory to save the extracted files
   * @param _label An optional JLabel to display messages
   * @param _prefix A prefix to add to the extracted file in order to create the message
   * @return the Set of extracted files
   */
  static public Set<File> expandZip(InputStream _zipStream, File _targetDirectory, javax.swing.JLabel _label, String _prefix) {
    try {
      BufferedInputStream bufIn = new BufferedInputStream(_zipStream);
      ZipInputStream input = new ZipInputStream(bufIn);
      ZipEntry zipEntry=null;
      Set<File> fileSet = new HashSet<File>();
      byte[] buffer = new byte[1024];
      cancelled = false;
      while ( (zipEntry=input.getNextEntry()) != null) {
        if (zipEntry.isDirectory()) continue;
        if (cancelled) {
          input.close();
          return null;
        }
        if (_label!=null) _label.setText(_prefix+zipEntry.getName());
        String filename = zipEntry.getName();
//        System.out.println("Expanding file: "+filename + " to: "+_targetDirectory);
        File newFile = new File(_targetDirectory,filename);
        newFile.getParentFile().mkdirs();
        newFile.deleteOnExit();
        int bytesRead;
        FileOutputStream output = new FileOutputStream (newFile);
        while ((bytesRead = input.read(buffer)) != -1) output.write(buffer, 0, bytesRead);
        output.close();
        input.closeEntry();
        fileSet.add(newFile);
      }
      input.close();
      return fileSet;
    }
    catch (Exception exc) { 
      exc.printStackTrace();
      return null;
    }    
  }  

}
