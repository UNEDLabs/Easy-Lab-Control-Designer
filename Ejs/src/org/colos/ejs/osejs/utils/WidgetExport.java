package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.ResourceLoader;


public class WidgetExport {
  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private final ResourceUtil res = new ResourceUtil("Resources");

  /**
   * Compresses a compiled simulation into a MacOSX widget
   * @param _ejs
   */
  static public void convertToWidgetXMLSimulation(Osejs _ejs) {
//    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
//
//    String sourceName = FileChooserUtil.chooseFilename(_ejs, _ejs.getExportDirectory(), "ZIP", new String[]{"zip"}, false);
//    if (sourceName==null) {
//      _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
//      return;
//    }
//    File sourceFile = new File(sourceName);
    
    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    if (_ejs.checkChangesAndContinue(false) == false) return; // The user canceled the action
    
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't existz

    // Choose target
    String targetName = FileUtils.getPlainNameAndExtension(xmlFile).getFirstString() + ".wdgt";
    File targetFolder = new File(_ejs.getExportDirectory(), targetName);
    targetName = FileChooserUtil.chooseFoldername(_ejs, targetFolder, "WIDGET", new String[]{"wdgt"}, true);
    if (targetName==null) return;
    boolean warnBeforeOverwritting = true;
    if (! (targetName.toLowerCase().endsWith(".wdgt")) ) targetName = targetName + ".wdgt";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists

    targetFolder = new File(targetName);
    if (warnBeforeOverwritting && targetFolder.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFolder.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }
    
    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFolder.getName());

    JarTool.remove(targetFolder);
    targetFolder.mkdirs();
    
//    JarTool.unzip(sourceFile, targetFolder);
    { // generate the simulation from EjsS
      String simulationName = _ejs.getSimInfoEditor().getSimulationName();
      if (simulationName==null) simulationName = _ejs.getCurrentXMLFilename();
      if (!GenerateJS.prepackageXMLSimulation(_ejs, xmlFile, simulationName, targetFolder, true, null)) { // true: simplified, null: Not SCORM
        _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+targetName);
        return;
      }
    }

    File metadataFile = new File(targetFolder,"_metadata.txt");
    if (!metadataFile.exists()) { // This is not an ejss_model file
      _ejs.getOutputArea().println("Warning: generated files do not include an EJS Javascript model. Ignored!");
      JarTool.remove(targetFolder);
      return;
    }
    
    ArrayList<String> ignoreList = new ArrayList<String>();
    ignoreList.add("_ejs_library/html/EjsLauncher.html");
    ignoreList.add("_ejs_library/css/ejsGroupPage.css");
    ignoreList.add("_ejs_library/css/ejsContentsLeft.css");
    ignoreList.add("_ejs_library/css/ejsContentsTop.css");
    ignoreList.add("_ejs_library/css/ejsPage.css");
    ignoreList.add("_ejs_library/images/EjsMainIcon.gif");
    ignoreList.add("_ejs_library/images/EjsLogo.gif");
    ignoreList.add("_ejs_library/images/EjsIcon.gif");
    ignoreList.add("_ejs_library/images/Gyroscope.gif");
    ignoreList.add("_ejs_library/images/cc_icon.png");
    ignoreList.add(".DS_Store");
    ignoreList.add("_ejs_README.txt");
    ignoreList.add("_metadata.txt");
    ignoreList.add("_opensocial.xml");
    ignoreList.add(".ejss");

    String title = "Unnamed";
    String authorName = "";
    String logoFilename = null;
    String simulationFilename = null;
    String preferredWidth = null;
    String preferredHeight = null;
    // process metadata file
    String metadata = FileUtils.readTextFile(metadataFile, null);
    StringTokenizer tkn = new StringTokenizer(metadata,"\n");
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      if (line.startsWith("title:")) {
        title = line.substring(6).trim();
      }
      else if (line.startsWith("logo-image:")) {
        logoFilename = line.substring(11).trim();
      }
      else if (line.startsWith("author:")) {
        if (authorName.length()>0) authorName += ";"+line.substring(7).trim();
        else authorName = line.substring(7).trim();
      }
      else if (line.startsWith("main-simulation:")) {
        simulationFilename = line.substring(16).trim();
      }
      
      else if (line.startsWith("preferred-width:")) {
        preferredWidth = line.substring(16).trim();
      }
      else if (line.startsWith("preferred-height:")) {
        preferredHeight = line.substring(18).trim();
      }
      // Ignore these
      else if (line.startsWith("source:")) {
        ignoreList.add(line.substring(7).trim());
      }
      else if (line.startsWith("html-main:")) {
        ignoreList.add(line.substring(11).trim());
      }
      else if (line.startsWith("html-contents:")) {
        ignoreList.add(line.substring(14).trim());
      }
      else if (line.startsWith("page-index:")) {
        ignoreList.add(line.substring(11).trim());
      } // Metadata processed
    } // end of processing metadata
    if (simulationFilename==null) {
      _ejs.getOutputArea().println("Warning: generated files do not include an EJS Javascript model. Ignored!");
      JarTool.remove(targetFolder);
      return;
    }
    ignoreList.remove(simulationFilename);
    File simulationFile = new File(targetFolder,simulationFilename);
    EPub.removeMetadataDiv(simulationFile, simulationFile,  OsejsCommon.getUTF8());
    // Remove files we know we don't need
    for (File file : JarTool.getContents(targetFolder)) {
      String filename = file.getAbsolutePath();
      for (String suffix : ignoreList) {
        if (filename.endsWith(suffix)) file.delete();
      }
    }
    File defaultImageFile = new File(targetFolder,"Default.png");
    File iconImageFile = new File(targetFolder,"Icon.png");
    if (logoFilename!=null && logoFilename.trim().length()>0) {
      JarTool.copy(new File(targetFolder,logoFilename),defaultImageFile);
      JarTool.copy(new File(targetFolder,logoFilename),iconImageFile);
    }
    TwoStrings sizes = correctDefaultValues(_ejs,defaultImageFile,iconImageFile, preferredWidth, preferredHeight);
    if (sizes==null) {
      JarTool.remove(targetFolder);
      return;
    }
    createInfoPListFile(_ejs,targetFolder,title,simulationFilename,sizes,0,0);
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+targetFolder.getName());
  } 
  
  static private void createInfoPListFile(Osejs _ejs, File _workingFolder, 
      String _title, String _mainHtml, 
      TwoStrings _size, int _insetX, int _insetY) {
    String folderName = FileUtils.getPlainNameAndExtension(_workingFolder).getFirstString();
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
    buffer.append("<plist version=\"1.0\">\n");
    buffer.append("<dict>\n");
    buffer.append("  <key>AllowNetworkAccess</key>\n");
    buffer.append("  <true/>\n");
    buffer.append("  <key>CFBundleIdentifier</key>\n");
    buffer.append("  <string>org.colos.ejss</string>\n");
    buffer.append("  <key>CFBundleName</key>\n");
    buffer.append("  <string>"+folderName+"</string>\n");
    buffer.append("  <key>CFBundleVersion</key>\n");
    buffer.append("  <string>1.0</string>\n");
    buffer.append("  <key>CFBundleDisplayName</key>\n");
    buffer.append("  <string>"+_title+"</string>\n");
    buffer.append("  <key>MainHTML</key>\n");
    buffer.append("  <string>"+_mainHtml+"</string>\n");
    if (_size.getFirstString().length()>0 && _size.getSecondString().length()>0) {
      buffer.append("  <key>Width</key>\n");
      buffer.append("  <integer>"+_size.getFirstString()+"</integer>\n");
      buffer.append("  <key>Height</key>\n");
      buffer.append("  <integer>"+_size.getSecondString()+"</integer>\n");
    }
    if (_insetX>0 && _insetY>0) {
      buffer.append("  <key>CloseBoxInsetX</key>\n");
      buffer.append("  <integer>"+_insetX+"</integer>\n");
      buffer.append("  <key>CloseBoxInsetY</key>\n");
      buffer.append("  <integer>"+_insetY+"</integer>\n");
    }
    buffer.append("</dict>\n");
    buffer.append("</plist>\n");
    File outputFile = new File (_workingFolder,"Info.plist");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nInfo.plist", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static private class ReturnValue {
    boolean value = false;
  }
  
  static private TwoStrings correctDefaultValues(final Osejs ejs, File defaultImageFile, File iconImageFile,
      String preferredWidth, String preferredHeight) {
    
    final ReturnValue returnValue=new ReturnValue();
    final JDialog dialog=new JDialog(ejs.getMainFrame(),res.getString("Package.PackageForWidget.Title"),true);

    Insets nullInset = new Insets(0,0,0,0);
    EmptyBorder labelBorder = new EmptyBorder(0,5,0,5);
    EmptyBorder panelBorder = new EmptyBorder(10,0,10,0);

    final JButton defaultButton = new JButton();
    defaultButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    defaultButton.setHorizontalAlignment(JLabel.CENTER);
    defaultButton.setBackground(Color.WHITE);
    defaultButton.setActionCommand(null);
    if (defaultImageFile.exists()) showImage(defaultButton,defaultImageFile.getAbsolutePath());
    else {
      defaultButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
      defaultButton.setPreferredSize(new Dimension(400,400));
    }
    defaultButton.setToolTipText(res.getString("Edit"));
    defaultButton.setMargin(nullInset);
    
    final JButton iconButton = new JButton();
    iconButton.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
    iconButton.setHorizontalAlignment(JLabel.CENTER);
    iconButton.setBackground(Color.WHITE);
    iconButton.setActionCommand(null);
    if (iconImageFile.exists()) showImage(iconButton,iconImageFile.getAbsolutePath());
    else {
      iconButton.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
      iconButton.setPreferredSize(new Dimension(75,75));
    }
    iconButton.setToolTipText(res.getString("Edit"));
    iconButton.setMargin(nullInset);

    ActionListener actionListener = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        JButton button = (JButton) evt.getSource();
        String filename = FileChooserUtil.chooseFilename(ejs, ejs.getExportDirectory(), "PNG", new String[]{"png"}, false);
        if (filename==null) return;
        if (!filename.toLowerCase().endsWith(".png")) {
          JOptionPane.showMessageDialog(defaultButton, 
              res.getString("SimInfoEditor.RequiredPNGimage")+"\n"+filename, 
              res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          return;
        }
        showImage(button,filename);
      }
   };
   
   defaultButton.addActionListener(actionListener);
   iconButton.addActionListener(actionListener);
   
   JButton okButton = new JButton (res.getString("EditorFor.Ok"));

   JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
   cancelButton.addActionListener (new ActionListener() {
     public void actionPerformed (java.awt.event.ActionEvent evt) {
       returnValue.value = false;
       dialog.setVisible (false);
     }
   });
   JPanel buttonsPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
   buttonsPanel.add (cancelButton);
   buttonsPanel.add (okButton);
   
   JLabel widthLabel = new JLabel(res.getString("EditorForPoint.Width"));
   final JTextField widthField = new JTextField(5);
   widthField.setText(preferredWidth);
   JPanel widthPanel = new JPanel (new BorderLayout());
   widthPanel.add(widthLabel,BorderLayout.WEST);
   widthPanel.add(widthField,BorderLayout.CENTER);
  
   JLabel heightLabel = new JLabel(res.getString("EditorForPoint.Height"));
   final JTextField heightField = new JTextField(5);
   heightField.setText(preferredHeight);
   JPanel heightPanel = new JPanel (new BorderLayout());
   heightPanel.add(heightLabel,BorderLayout.WEST);
   heightPanel.add(heightField,BorderLayout.CENTER);
  
   JLabel sizesLabel = new JLabel(res.getString("Package.PackageForWidget.Size"));
   sizesLabel.setFont(sizesLabel.getFont().deriveFont(Font.BOLD));
   sizesLabel.setBorder(labelBorder);

   JPanel sizesCenterPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
   sizesCenterPanel.add (widthPanel);
   sizesCenterPanel.add (heightPanel);

   JLabel sizesTipLabel = new JLabel(res.getString("Package.PackageForWidget.SizeTip"),SwingConstants.CENTER);
   sizesTipLabel.setBorder(labelBorder);
   sizesTipLabel.setFont(sizesTipLabel.getFont().deriveFont(Font.ITALIC));

   JPanel sizesPanel = new JPanel (new BorderLayout());
   sizesPanel.setBorder(panelBorder);
   sizesPanel.add (sizesLabel,BorderLayout.NORTH);
   sizesPanel.add (sizesCenterPanel,BorderLayout.CENTER);
   sizesPanel.add (sizesTipLabel,BorderLayout.SOUTH);

   JLabel defaultLabel = new JLabel(res.getString("Package.PackageForWidget.Default"));
   defaultLabel.setFont(defaultLabel.getFont().deriveFont(Font.BOLD));
   defaultLabel.setBorder(labelBorder);
   JLabel defaultRecommendedSize = new JLabel(res.getString("SimInfoEditor.RecommendedSize")+ " (320 x 180)",SwingConstants.CENTER);
   defaultRecommendedSize.setFont(defaultRecommendedSize.getFont().deriveFont(Font.ITALIC));
   defaultRecommendedSize.setBorder(labelBorder);

   JPanel defaultTopPanel = new JPanel (new BorderLayout());
   defaultTopPanel.add(defaultLabel,BorderLayout.NORTH);
   defaultTopPanel.add(defaultRecommendedSize,BorderLayout.SOUTH);
   defaultTopPanel.setBorder(panelBorder);

   JScrollPane defaultScroll = new JScrollPane(defaultButton);
   defaultScroll.setPreferredSize(new Dimension(400,200));
   JPanel defaultPanel = new JPanel (new BorderLayout());
   defaultPanel.add(defaultTopPanel, BorderLayout.NORTH);
   defaultPanel.add(defaultScroll, BorderLayout.CENTER);

   JLabel iconLabel = new JLabel(res.getString("Package.PackageForWidget.Icon"));
   iconLabel.setFont(iconLabel.getFont().deriveFont(Font.BOLD));
   iconLabel.setBorder(labelBorder);
   JLabel iconRecommendedSize = new JLabel(res.getString("SimInfoEditor.RecommendedSize")+ " (75 x 75)",SwingConstants.CENTER);
   iconRecommendedSize.setFont(iconRecommendedSize.getFont().deriveFont(Font.ITALIC));
   iconRecommendedSize.setBorder(labelBorder);

   JPanel iconTopPanel = new JPanel (new BorderLayout());
   iconTopPanel.add(iconLabel,BorderLayout.NORTH);
   iconTopPanel.add(iconRecommendedSize,BorderLayout.SOUTH);
   iconTopPanel.setBorder(panelBorder);

   JScrollPane iconScroll = new JScrollPane(iconButton);
   iconScroll.setPreferredSize(new Dimension(100,100));
   JPanel iconPanel = new JPanel (new BorderLayout());
   iconPanel.add(iconTopPanel, BorderLayout.NORTH);
   iconPanel.add(iconScroll, BorderLayout.CENTER);
   
   JPanel topPanel = new JPanel (new BorderLayout());
   topPanel.add(sizesPanel,BorderLayout.CENTER);
   topPanel.add(new JSeparator(),BorderLayout.SOUTH);

   JSplitPane centerPanel = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
   centerPanel.add(iconPanel);
   centerPanel.add(defaultPanel);

   JPanel bottomPanel = new JPanel (new BorderLayout());
   bottomPanel.add(new JSeparator(),BorderLayout.NORTH);
   bottomPanel.add(buttonsPanel,BorderLayout.CENTER);

   dialog.getContentPane().setLayout(new BorderLayout());
   dialog.getContentPane().add(topPanel,BorderLayout.NORTH);
   dialog.getContentPane().add(centerPanel,BorderLayout.CENTER);
   dialog.getContentPane().add(bottomPanel,BorderLayout.SOUTH);
//   editorDialog.setModal(false);
   dialog.setResizable(true);
   dialog.validate();
   dialog.pack();
   centerPanel.setDividerLocation(iconPanel.getHeight());
   dialog.setLocationRelativeTo(ejs.getMainPanel());
   
   okButton.addActionListener (new ActionListener() {
     public void actionPerformed (java.awt.event.ActionEvent evt) {
       if (defaultButton.getActionCommand()==null) {
         JOptionPane.showMessageDialog(dialog, 
             res.getString("Package.PackageForWidget.RequiredDefault"),
             res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
         return;
       }
       if (iconButton.getActionCommand()==null) {
         JOptionPane.showMessageDialog(dialog, 
             res.getString("Package.PackageForWidget.RequiredIcon"),
             res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
       }
       String widthStr = widthField.getText().trim();
       String heightStr = heightField.getText().trim();
       if (widthStr.length()>0 || heightStr.length()>0) {
         try { 
           int width = Integer.parseInt(widthField.getText());
           int height = Integer.parseInt(heightField.getText());
           if (width<=0 || height<=0) {
             JOptionPane.showMessageDialog(defaultButton, 
                 res.getString("HtmlView.InvalidSize"),
                 res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
             return;
           }
         }
         catch (Exception exc) {
           JOptionPane.showMessageDialog(defaultButton, 
               res.getString("HtmlView.InvalidSize"),
               res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
           return;
         }
       }
       returnValue.value = true;
       dialog.setVisible (false);
     }
   });

   returnValue.value = false;
   dialog.setVisible(true);
   if (!returnValue.value) return null;
   
   String defaultFilename = defaultButton.getActionCommand();
   if (!defaultFilename.equals(defaultImageFile.getAbsolutePath())) {
     JarTool.copy(new File(defaultFilename), defaultImageFile);
   }
   String iconFilename = iconButton.getActionCommand();
   if (!iconFilename.equals(iconImageFile.getAbsolutePath())) {
     JarTool.copy(new File(iconFilename), iconImageFile);
   }
   return new TwoStrings(widthField.getText().trim(),heightField.getText().trim());
  }
  
  static private void showImage(JButton button, String filename) {
    Icon icon = ResourceLoader.getIcon(filename);
    if (icon!=null) {
      button.setIcon(icon);
      button.setActionCommand(filename);
      button.setPreferredSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));

    }
    else {
      JOptionPane.showMessageDialog(button, 
          res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+filename, 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
//      button.setActionCommand(null);
//      button.setIcon(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    }
    
  }

  
}
