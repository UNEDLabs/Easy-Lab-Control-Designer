package org.colos.ejs.osejs.utils;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.*;

import org.colos.ejs.osejs.Generate;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class UnzipUtility implements Runnable {

  static private final ResourceUtil res = new ResourceUtil("Resources");

  private Osejs ejs;
  private InputStream inputStream;
  private JLabel label;
  private JDialog tmpDialog;

  /**
   * Retrieves a ZIP file from a URL addres, uncompresses it in a given 
   * directory, and opens it with EJS (if sucessfull).
   */
  static public void unzipAndChoose (Osejs _ejs, Window _parentWindow, String _name, InputStream _inputStream) {
//    JDialog tmpDialog;
//    if (_parentWindow instanceof Frame) tmpDialog = new JDialog((Frame)_parentWindow,res.getString("Information"));
//    else if (_parentWindow instanceof Dialog) tmpDialog = new JDialog((Dialog)_parentWindow,res.getString("Information"));
//    else tmpDialog = new JDialog((Frame)null,res.getString("Information"));
    
    
//    JLabel label = new JLabel (res.getString("Osejs.Init.ReadingFile")+" "+_name+"      ");
//    label.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
//    label.setFont(label.getFont().deriveFont(14f));
    
    final UnzipUtility runnable = new UnzipUtility(_ejs,_inputStream, null, null); //,label,tmpDialog);
    final Thread thread = new Thread(runnable);
    thread.setPriority(Thread.NORM_PRIORITY);

//    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
//    cancelButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent _evt) { TemporaryFilesManager.cancelProcess(); thread.interrupt(); }
//    });
//    JPanel panel = new JPanel (new FlowLayout(FlowLayout.CENTER));
//    panel.add(cancelButton);
//
//    tmpDialog.getContentPane().setLayout(new BorderLayout());
//    tmpDialog.getContentPane().add(label,BorderLayout.CENTER);
//    tmpDialog.getContentPane().add(panel,BorderLayout.SOUTH);
//    tmpDialog.pack();
//    if (_parentWindow!=null) {
//      Dimension size = _parentWindow.getSize();
//      Dimension mysize = tmpDialog.getSize();
//      Point loc = _parentWindow.getLocation();
//      tmpDialog.setLocation(loc.x+(size.width-mysize.width)/2,loc.y+(size.height-mysize.height)/2);
//    }
//    tmpDialog.setVisible(true); 
    thread.start();
  }
  
  private UnzipUtility(Osejs _ejs, InputStream _inputStream, JLabel _label, JDialog _tmpDialog) {
    ejs = _ejs;
    inputStream = _inputStream;
    label = _label;
    tmpDialog = _tmpDialog;
  }

  public void run() {
    java.util.List<File> extractedFiles = null;
//    File tmpDir = TemporaryFilesManager.createTemporaryDirectory("ZipModel", ejs.getTemporaryDirectory());
//    if (tmpDir!=null) extractedFiles = TemporaryFilesManager.expandZip(inputStream, tmpDir, label, res.getString("Osejs.Init.ReadingFile")+" ");
    File tmpDir = ejs.chooseFolderUnderSource(); 
    if (tmpDir!=null) {
      extractedFiles = ejsUnzipWithWarning(inputStream, tmpDir, label, res.getString("Osejs.Init.ReadingFile")+" ");
      if (extractedFiles!=null) Collections.sort(extractedFiles);
    }
    if (tmpDialog!=null) tmpDialog.setVisible(false);
    if (extractedFiles==null) {
      JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("Osejs.File.NotReadOK"), res.getString("Osejs.File.ErrorReadingFile"), JOptionPane.ERROR_MESSAGE);
      return;
    }
    // Locate the EJS files
    String tmpDirPath = FileUtils.getPath(tmpDir);
    ArrayList<PathAndFile> pafList = new ArrayList<PathAndFile>();
    for (File file : extractedFiles) {
      if (OsejsCommon.isEJSfile(file)) pafList.add(new PathAndFile(FileUtils.getRelativePath(file, tmpDirPath, false),file));
    }
    // Select the model to read
    File modelFile=null;
    switch (pafList.size()) {
      case 0 : // no EJS files detected
        JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("DigitalLibrary.NoXMLFound"), res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        break;
      case 1 : // Only one model was detected 
        modelFile = pafList.get(0).getFile(); 
        break;
      default : // There are more than one models
        modelFile = chooseOne(ejs.getMainPanel(),res.getDimension("Package.ConfirmList.Size"), res.getString("Package.ChooseOneModel"),res.getString("EditorFor.ChooseOne"), pafList, null);
        break;
    }
    if (modelFile!=null) {
      final File file = modelFile;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { ejs.readFile(file, false); }
      });
    }
  }

  /**
   * Choose one file out of a list
   * @return
   */
  static public File chooseOne (Component _parentComponent, Dimension _size, 
      String _message, String _title, java.util.List<PathAndFile> _list, JComponent _bottomComponent) {
    class ReturnValue { boolean value = false; }
    final ReturnValue returnValue=new ReturnValue();

    DefaultListModel<PathAndFile> listModel = new DefaultListModel<PathAndFile>();
    for (int i=0,n=_list.size(); i<n; i++) listModel.addElement(_list.get(i));
    JList<PathAndFile> list = new JList<PathAndFile>(listModel);
    list.setEnabled(true);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    list.setSelectionInterval(0,listModel.getSize()-1);
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(_size);

    final JDialog dialog = new JDialog();

    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if      (aCmd.equals("ok"))      { returnValue.value = true;  dialog.setVisible (false); } //$NON-NLS-1$
        else if (aCmd.equals("cancel"))  { returnValue.value = false; dialog.setVisible (false); } //$NON-NLS-1$
      }
    };

    JButton okButton = new JButton (DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
    okButton.setActionCommand ("ok"); //$NON-NLS-1$
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
    cancelButton.setActionCommand ("cancel"); //$NON-NLS-1$
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (cancelButton);

    JPanel topPanel1 = new JPanel (new BorderLayout ());

    JTextArea textArea   = new JTextArea (_message);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setBackground(topPanel1.getBackground());
    textArea.setBorder(new javax.swing.border.EmptyBorder(5,5,10,5));

    topPanel1.setBorder(new javax.swing.border.EmptyBorder(5,10,5,10));
    topPanel1.add (textArea,BorderLayout.NORTH);
    topPanel1.add (scrollPane,BorderLayout.CENTER);
    if (_bottomComponent!=null) topPanel1.add (_bottomComponent,BorderLayout.SOUTH);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (topPanel1,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
        new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent event) { returnValue.value = false; }
        }
    );

    //  dialog.setSize (_size);
    dialog.validate();
    dialog.pack();
    dialog.setTitle (_title);
    dialog.setLocationRelativeTo (_parentComponent);
    dialog.setModal(true);

    dialog.setVisible (true);
    if (!returnValue.value) return null;
    PathAndFile paf = list.getSelectedValue();
    if (paf==null) return null;
    return paf.getFile();
  }
  
  // ---- Adapted from JarTool for the special case of possible ejss_model_xxx.zip files
  
  /**
   * Uncompresses a ZIP or JAR file into a given directory, 
   * but if it is a ZIP generated by EJSS, it will ONLY extract the files required by models in the ZIP.
   * Requires a version of EjsS later than 150807.
   * If not, every file in the ZIP will be extracted before searching for EjsS files. 
   * @param zipStream The InputStream to read from
   * @param targetDirectory File The target directory
   * @param label An optional JLabel to display messages
   * @param prefix A prefix to add to the extracted file in order to create the message
   * @return java.util.List<File> the set of files extracted, null if cancelled
   */
  static private java.util.List<File> ejsUnzipWithWarning(InputStream zipStream, File targetDirectory, javax.swing.JLabel label, String prefix) {
    // Create a working temporary directory
    File tempFolder=null;
    try {
      tempFolder = File.createTempFile("EjsExtracted", ".tmp", targetDirectory); // Get a unique name for our temporary directory
      tempFolder.delete();        // remove the created file
      tempFolder.mkdirs();
    } catch (Exception exc) { 
      exc.printStackTrace();
      JarTool.remove(tempFolder);
      return null;
    }
    if (!blindUnzip(zipStream, tempFolder)) {
      JarTool.remove(tempFolder);
      return null;
    }

    Set<String> requiredFiles=new HashSet<String>();
    
    // First, get the ZIP root _metadata file, if it exists
    String metadataStr = FileUtils.readTextFile(new File(tempFolder,"_metadata.txt"),null);
    if (metadataStr!=null) {
      boolean createdWithEjsS=false;
      boolean isPackage = false;
      Set<String> modelsList=new HashSet<String>();

      StringTokenizer tkn = new StringTokenizer(metadataStr,"\n");
      while (tkn.hasMoreTokens()) {
        String line = tkn.nextToken();
        if      (line.startsWith(Generate.sVersionInfo)) createdWithEjsS = true;
        else if (line.startsWith("package:")) isPackage = true;
        else if (line.startsWith("model:")) {
          String modelName = line.substring(6).trim();
          int index = modelName.indexOf('|');
          if (index>0) modelsList.add(modelName.substring(0,index).trim());
          else modelsList.add(modelName);
        }
        else if (line.startsWith("source:")) requiredFiles.add(line.substring(7).trim());
        else if (line.startsWith("resource:")) requiredFiles.add(line.substring(9).trim());
      }
      if (createdWithEjsS) { 
        if (isPackage) { // read the _metadata.txt files in all models
          for (String modelFolderName : modelsList) {
//            System.err.println("Trying to read metadata from "+modelFolderName+"/_metadata.txt");
            String childMetadataStr = FileUtils.readTextFile(new File(tempFolder,modelFolderName+"/_metadata.txt"),null);
            if (childMetadataStr!=null) {
              StringTokenizer tkn2 = new StringTokenizer(childMetadataStr,"\n");
              while (tkn2.hasMoreTokens()) {
                String line = tkn2.nextToken();
                if (line.startsWith("source:")) requiredFiles.add(modelFolderName+"/"+line.substring(7).trim());
                else if (line.startsWith("resource:")) requiredFiles.add(modelFolderName+"/"+line.substring(9).trim());
              }
            }
          }
        }
      }
      else requiredFiles.clear(); // Not created by EjsS
    }
    boolean copyAll = requiredFiles.isEmpty();
//    if (copyAll) System.out.println("Will extract ALL files");
//    else for (String filename : requiredFiles) System.out.println("Will extract "+filename);
    
    int overwritePolicy = JarTool.NO;
    ArrayList<File> fileSet = new ArrayList<File>();
    for (File file : JarTool.getContents(tempFolder)) {
      String filename = FileUtils.getRelativePath(file, tempFolder, false);
      if (copyAll || requiredFiles.contains(filename)) {
        File newFile = new File(targetDirectory, filename);
        if (newFile.exists()) {
          switch (overwritePolicy) {
             case JarTool.NO_TO_ALL :
               continue;
             case JarTool.YES_TO_ALL :
               break;       // will overwrite
             default :
               switch (overwritePolicy = JarTool.confirmOverwrite(filename,true)) {
                  case JarTool.NO_TO_ALL :
                  case JarTool.NO :
                    continue;
                  case JarTool.CANCEL : return null;
                  default : // Do nothing, i.e., will overwrite the file
               }
          }
        }
        if (JarTool.copy(file, newFile)) fileSet.add(newFile);
      }
    }
    JarTool.remove(tempFolder);
    return fileSet;
  }
  
  static private boolean blindUnzip(InputStream zipStream, File targetDirectory) {
    try {
      BufferedInputStream bufIn = new BufferedInputStream(zipStream);
      ZipInputStream input = new ZipInputStream(bufIn);
      ZipEntry zipEntry = null;

      byte[] buffer = new byte[1024];
      while((zipEntry = input.getNextEntry())!=null) {
        if (zipEntry.isDirectory()) continue;
        File newFile = new File(targetDirectory, zipEntry.getName());
        newFile.getParentFile().mkdirs();
        int bytesRead;
        FileOutputStream output = new FileOutputStream(newFile);
        while((bytesRead = input.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.closeEntry();
      }
      input.close();
      return true;
    } catch (Exception exc) {
      exc.printStackTrace();
      return false;
    }
  }


}
