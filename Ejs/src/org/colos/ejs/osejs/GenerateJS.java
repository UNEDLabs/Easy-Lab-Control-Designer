package org.colos.ejs.osejs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.colos.ejs._EjsSConstants;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.html.HtmlEditor;
import org.colos.ejs.osejs.edition.html.OneHtmlPage;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileChooserUtil;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.ScormUtility;
import org.colos.ejs.osejs.utils.TwoStrings;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.XMLTransformerJava;
import org.colos.ejss.xml.SimulationXML.INFORMATION;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class GenerateJS {
  static private final ResourceUtil res    = new ResourceUtil("Resources");
  
  static public void saveJavaScriptSimulation (Osejs _ejs, String _targetName, boolean _asXML) {
    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    if (_ejs.checkChangesAndContinue(false) == false) return; // The user canceled the action

    String filename = _ejs.getSimInfoEditor().getSimulationName();
    if (filename==null) filename = _ejs.getCurrentXMLFilename();

    boolean warnBeforeOverwritting = true;
    String description = _asXML ? "XML" : "JSON";
    String extension   = _asXML ? ".xml" : ".json";

    File finalFile;
    if (_targetName!=null) { 
      finalFile = new File(_ejs.getExportDirectory(),_targetName);
    }
    else { // Select the target
      String tentativeName = "ejss_simulation_"+filename+ extension;
      File currentFile = new File(_ejs.getExportDirectory(),tentativeName);
      _targetName = FileChooserUtil.chooseFilename(_ejs, currentFile, description, new String[]{extension}, true);
      if (_targetName==null) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
      if (! (_targetName.toLowerCase().endsWith(extension)) ) _targetName = _targetName + extension;
      else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
      finalFile = new File(_targetName);
      if (warnBeforeOverwritting && finalFile.exists()) {
        int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
            finalFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (selected != JOptionPane.YES_OPTION) {
          _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
          return;
        }
      }
    }

    try {
      SimulationXML simulation = _ejs.getSimulationXML(filename);
      if (simulation.getInformation(INFORMATION.TITLE)==null) simulation.setInformation(INFORMATION.TITLE,filename);
      if (_asXML) FileUtils.saveToFile (finalFile, null, simulation.toXMLString()); 
      else FileUtils.saveToFile (finalFile, null, simulation.toJSON());
    } catch (Exception exc) {
      exc.printStackTrace();
      _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetName);
      return;
    }
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+finalFile.getName());
  }
  
  /**
   * This compresses the simulation and its auxiliary files in EjsS XML format 
   * @param _ejs Osejs
   */
  static public boolean prepackageXMLSimulation (Osejs _ejs, File _xmlFile, String _filename, File _zipFolder, boolean _simplified, ScormUtility.ScormOptions _scormOptions) {

    java.util.List<LocaleItem> localesDesired = Generate.getLocalesDesired(_ejs);
    boolean hasView = !_ejs.getHtmlViewEditor().getPages().isEmpty();
    
    // Prepare the simulation
    SimulationXML simulation = _ejs.getSimulationXML(_filename);
    if (simulation.getInformation(INFORMATION.TITLE)==null) simulation.setInformation(INFORMATION.TITLE,_filename);

    // Prepare the metadata information
    ArrayList<TwoStrings> metaFile = new ArrayList<TwoStrings>();
    { // author information
      String versionInfo = "Easy Java/Javascript Simulations, version "+_EjsSConstants.VERSION+", build "+_EjsSConstants.VERSION_DATE+". Visit "+_EjsSConstants.WEB_SITE;
      metaFile.add(new TwoStrings("generated-with", versionInfo));
      if (_ejs.getSimInfoEditor().fixedNavigationBar()) metaFile.add(new TwoStrings("fixed-navbar", "true"));
      metaFile.add(new TwoStrings("title", simulation.getInformation(INFORMATION.TITLE)));
      String image = simulation.getInformation(INFORMATION.LOGO_IMAGE);
      Generate.addToMetafile(metaFile,"logo-image", image!=null ? image : "");
      metaFile.add(new TwoStrings("author", simulation.getInformation(INFORMATION.AUTHOR)));
      metaFile.add(new TwoStrings("copyright", simulation.getInformation(INFORMATION.COPYRIGHT)));
      image = simulation.getInformation(INFORMATION.AUTHOR_IMAGE);
      
      if (hasView) {
        OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
        String width = oneViewPage.getPreferredWidth();
        String height = oneViewPage.getPreferredHeight();
        if (width.length()>0 && height.length()>0) {
          metaFile.add(new TwoStrings("preferred-width", width));
          metaFile.add(new TwoStrings("preferred-height", height));
        }
      }
      
      Generate.addToMetafile(metaFile,"author-image", image!=null ? image : "");
      
      if (!_simplified) {
        if (_ejs.getOptions().includeModel()) {
          TwoStrings fileNameAndAxtension = FileUtils.getPlainNameAndExtension(_xmlFile);
          String modelName = fileNameAndAxtension.getFirstString()+"."+fileNameAndAxtension.getSecondString(); // Use same extension as the original
          Generate.addToMetafile(metaFile,"source", modelName);
          JarTool.copy(_xmlFile,new File(_zipFolder,modelName));
        }
      }
    }

    File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/lib");
    File viewFolder = _zipFolder;
    if (hasView) { // Tailor the view and create the Simulation files
      if (_ejs.supportsJava()) simulation.setViewOnly("8800"); // prepare for web server version
      if (_ejs.getOptions().autoSelectView()) simulation.setInformation(INFORMATION.AUTOSELECT_VIEW, "true");
      if (_ejs.supportsJava()) { // generate the view in a _view subfolder
          viewFolder = new File(_zipFolder,"_view");
          viewFolder.mkdirs();
      }

      // Generate the HTML file for the currently selected HTML View and Locales
      OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
      String viewDesired = oneViewPage.getName();
      String libPath = FileUtils.getPath(javascriptDir);
      { // Main simulation and script
        String localeString;
        String available_languages = "";
        for (LocaleItem locale : localesDesired) {
          String localeSuffix ="";
          if (locale.isDefaultItem()) {
            localeString = SimulationXML.sDEFAULT_LOCALE;
          }
          else {
            localeString = locale.getKeyword();
            localeSuffix = "_" + localeString;
            if (available_languages.length()>0) available_languages += ","+localeString;
            else available_languages = localeString; 
          }

          boolean separatedJSFile = _simplified || _ejs.getOptions().separateJSfile();
          boolean useFullLibrary  = _simplified || _ejs.getOptions().useFullLibrary();
          
          // Name of simulation file *_Simulation (index.html could be added as well)
          String simFilename = simulation.getName()+ (JSObfuscator.isGenerateXHTML() ? "_Simulation"+localeSuffix+".xhtml" : "_Simulation"+localeSuffix+".html");
          File htmlFile = new File (viewFolder,simFilename);          
          boolean ok = XMLTransformerJava.saveHTMLFile(_ejs,libPath,htmlFile, // output info
              simulation, viewDesired, localeString, "_ejs_library/css/ejss.css","_ejs_library", null, separatedJSFile, useFullLibrary); // separate JS and use full library
          if (!ok) return false;
          if (_ejs.getOptions().indexSimFile()) { // also index.html
            File indexFile = new File(viewFolder,"index"+localeSuffix+".html");
            FileUtils.copy(htmlFile, indexFile); 
          }
          String filepath = separatedJSFile ? org.colos.ejs.library.utils.FileUtils.getPlainName(htmlFile) + ".js" : "";
          // add main script entry to metadata file
          if (locale.isDefaultItem()) {
            Generate.addToMetafile(metaFile, "main-script", filepath);
            Generate.addToMetafile(metaFile, "main-simulation", htmlFile.getName());
          }
          else {
            Generate.addToMetafile(metaFile, locale.getKeyword()+"-main-script", filepath);
            Generate.addToMetafile(metaFile, locale.getKeyword()+"-main-simulation", htmlFile.getName());
          }
        }
        Generate.addToMetafile(metaFile, "available-languages", available_languages);
      }
    }

    // Copy auxiliary files
    Set<PathAndFile> list = new HashSet<PathAndFile>();

    if ( _ejs.supportsJava()) { 
      { // need to add the JAR for the server
        File jarFolder = new File(_zipFolder,"_jar");
        jarFolder.mkdirs();
        File jarFile = new File (jarFolder,"model.jar");
        _ejs.forceCompilation();
        if (_ejs.firstCompile(8800)) Generate.packageCurrentSimulation(_ejs, jarFile);
        else JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Generate.CantCompileError"),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
      }
      { // create index.html file
        StringBuffer buffer = new StringBuffer();
        if (JSObfuscator.isGenerateXHTML()) {
          buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
          buffer.append("<!DOCTYPE html>\n");
          buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
        }
        else buffer.append("<html>\n");
        buffer.append("  <head>\n");
        buffer.append("    <title>"+simulation.getInformation(INFORMATION.TITLE)+"</title>\n");
        buffer.append("  </head>\n");
        buffer.append("  <body> \n");
        buffer.append("    <strong>"+simulation.getInformation(INFORMATION.TITLE)+"</strong>\n");
        String abstractStr = simulation.getInformation(INFORMATION.ABSTRACT);
        if (abstractStr!=null) {
          StringTokenizer tkn = new StringTokenizer(abstractStr,"\n");
          while (tkn.hasMoreTokens()) {
            buffer.append("    <p>\n");
            buffer.append("      "+tkn.nextToken()+"\n");
            buffer.append("    </p>\n");
          }
        }
        String logo = simulation.getInformation(INFORMATION.LOGO_IMAGE);
        if (logo!=null) {
          buffer.append("    <p align=\"center\">\n");
          buffer.append("      <img src=\"_view/"+logo+"\"> ");
          buffer.append("    </p>\n");
        }

        buffer.append("    <p>\n");
        buffer.append("      <strong>&copy; "+Calendar.getInstance().get(Calendar.YEAR)+" "+simulation.getInformation(INFORMATION.AUTHOR)+"</strong>\n");
        buffer.append("    </p>\n");
        String authorLogo = simulation.getInformation(INFORMATION.AUTHOR_IMAGE);
        if (authorLogo!=null) {
          buffer.append("    <p align=\"left\">\n");
          buffer.append("      <img src=\"_view/"+authorLogo+"\">\n");
          buffer.append("    </p>\n");
        }
        buffer.append("</body>\n");
        buffer.append("</html>\n");
        String indexFilename = "index" +(JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html");
        try {
          FileUtils.saveToFile(new File (_zipFolder,indexFilename), null, buffer.toString());
        } catch (IOException exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileNotCreated")+" : "+ indexFilename,
              res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }

    if (!_simplified) { // <OpenSocial XML>
      StringBuffer buffer_os = new StringBuffer();
      buffer_os.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      buffer_os.append("<Module>\n");
      buffer_os.append("<ModulePrefs\n");
      buffer_os.append("title=\""+simulation.getName()+"\"\n");
      buffer_os.append("author=\""+simulation.getInformation(INFORMATION.AUTHOR)+"\"\n");
      buffer_os.append("description=\""+simulation.getInformation(INFORMATION.ABSTRACT)+"\"\n");
      buffer_os.append("screenshot=\""+simulation.getInformation(INFORMATION.LOGO_IMAGE)+"\"\n");
      buffer_os.append("thumbnail=\""+simulation.getInformation(INFORMATION.LOGO_IMAGE)+"\">\n");
      buffer_os.append("<Require feature=\"opensocial-0.9\"/>\n");
      buffer_os.append("<Require feature=\"dynamic-height\"/>\n");
      buffer_os.append("</ModulePrefs>\n");
      String simFilename = simulation.getName()+"_Simulation.xhtml";
      buffer_os.append("<Content type=\"html\" href=\""+simFilename+"\"/>\n");
      buffer_os.append("</Module>\n");
      String opensocialFilename = simulation.getName() + "_opensocial.xml";
      try {
        FileUtils.saveToFile(new File (_zipFolder,opensocialFilename), null, buffer_os.toString());
      } catch (IOException exc) {
        exc.printStackTrace();
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileNotCreated")+" : "+ opensocialFilename,
            res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    } // </OpenSocial XML>

    // Add description files
    String fileSuffix = JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html";
    Hashtable<String,Boolean> isXhtmlTable = new Hashtable<String,Boolean>();
    
    for (LocaleItem item : localesDesired)  {
      String localePrefix = item.isDefaultItem() ? "" : item.getKeyword()+"-";
      String localeSuffix = item.isDefaultItem() ? "" : "_"+item.getKeyword();
      Hashtable<String,StringBuffer> htmlTable = new Hashtable<String,StringBuffer>();
      Generate.addFramesHtml(htmlTable, _ejs,item,_filename,null,simulation.getInformation(SimulationXML.INFORMATION.MODEL_TAB_TITLE),"",null,hasView); 
      Generate.addDescriptionHtml(htmlTable, _ejs,item,_filename,"");
      // Now, save them all
      try {
        for (String key : htmlTable.keySet()) {
          String htmlCode = htmlTable.get(key).toString();
          String postFix;
          if (htmlCode.startsWith("<?xml ")) {
            postFix = key + localeSuffix + ".xhtml";
            isXhtmlTable.put(key, true);
          }
          else {
            postFix = key + localeSuffix + ".html";
            isXhtmlTable.put(key, false);
          }
          FileUtils.saveToFile (new File (viewFolder,_filename+postFix), null,htmlCode);
          String metadataCode;
          if (key.length()==0) metadataCode = localePrefix+"html-main";
          else if (key.startsWith("_Contents"))  metadataCode = localePrefix+"html-contents";
          else metadataCode = localePrefix+"html-description";
          Generate.addToMetafile(metaFile,metadataCode, _filename+postFix);
        }
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }

      // Add table of contents
      Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
      int counter = 0;
      int simTab = _ejs.getSimInfoEditor().getSimulationTab();
      boolean simulationAdded = false;

      String firstDescriptionFilename=null;
      for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
        HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
        if (htmlEditor.isActive() && !htmlEditor.isInternal()) {
          counter++;
          if (counter==simTab && hasView) {
            simulationAdded = true;
            String link = _filename+"_Simulation"+ localeSuffix + fileSuffix;
            Generate.addToMetafile(metaFile,localePrefix+"page-title", simulation.getInformation(INFORMATION.MODEL_TAB_TITLE)) ; //res.getString("Generate.HtmlSimulation"));
            Generate.addToMetafile(metaFile,localePrefix+"page-index", link);
          }

          LocaleItem actualItem = item;
          OneHtmlPage htmlPage = htmlEditor.getHtmlPage(actualItem);
          if (htmlPage==null) {
            if (!actualItem.isDefaultItem()) htmlPage =  htmlEditor.getHtmlPage(actualItem = LocaleItem.getDefaultItem());
          }
          if (htmlPage==null || htmlPage.isEmpty()) continue;
          String link;
          if (htmlPage.isExternal()) link = htmlPage.getPlainCode();
          else {
            String key = Generate.getIntroductionPageKey(counter,null);
            if (actualItem==item) link = _filename + key + localeSuffix;
            else link = _filename + key;
//            System.err.println("Checking key "+key+ " for locale "+item.getKeyword());
            if (isXhtmlTable.get(key)) link += ".xhtml";
            else link += ".html";
          }
          Generate.addToMetafile(metaFile,localePrefix+"page-title", htmlPage.getTitle());
          Generate.addToMetafile(metaFile,localePrefix+"page-index", link);
          if (firstDescriptionFilename==null) firstDescriptionFilename = link;
        }
      } // end of for
      if (hasView) {
        if (!simulationAdded) {
          String link = _filename+"_Simulation"+ localeSuffix + fileSuffix;
          Generate.addToMetafile(metaFile,localePrefix+"page-title", simulation.getInformation(INFORMATION.MODEL_TAB_TITLE)) ; //res.getString("Generate.HtmlSimulation"));
          Generate.addToMetafile(metaFile,localePrefix+"page-index", link);
        }
      }
      else { // has no view
        Generate.addToMetafile(metaFile,localePrefix+"main-script", "");
        if (firstDescriptionFilename!=null) Generate.addToMetafile(metaFile,localePrefix+"main-simulation", firstDescriptionFilename);
        else Generate.addToMetafile(metaFile,localePrefix+"main-simulation", "");
      }
    } // end of for Locales

    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    for (PathAndFile paf : _ejs.getSimInfoEditor().getAuxiliaryPathAndFiles(true)) {
      if (paf.getFile().isDirectory()) { // It is a complete directory
        if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
        else {
          String prefix = paf.getPath();
          if (!prefix.endsWith("/")) prefix += "/";
          if (prefix.startsWith("./")) prefix = prefix.substring(2);
          else absoluteAuxFiles.add(prefix);
          for (File file : JarTool.getContents(paf.getFile())) {
            String filepath = prefix+FileUtils.getRelativePath(file,paf.getFile(),false);
            //            System.out.println("Adding "+filepath);
            list.add(new PathAndFile(filepath,file));
            Generate.addToMetafile(metaFile,"resource", filepath);
          }
        }
      }
      else {
        if (paf.getPath().startsWith("/")) {
          String absolutePath = "images"+paf.getPath();
          //          System.out.println ("Copying absolute aux file "+absolutePath);
          File absoluteFile = new File (javascriptDir,absolutePath);
          if (!absoluteFile.exists()) {
            System.err.println ("Absolute file does not exist "+absoluteFile.getAbsolutePath());
            missingAuxFiles.add(paf.getPath());
          }
          else list.add(new PathAndFile("_ejs_library/"+absolutePath,absoluteFile));
        }
        else {
          if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
          else {
            list.add(paf);
            Generate.addToMetafile(metaFile,"resource", paf.getPath());
          }
        }
      }
    }
     
    { // Add common script
      File originalScriptFile = new File(javascriptDir,JSObfuscator.sCOMMON_SCRIPT);
      String destScriptFilename = "_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT;
      boolean done = false;
      if (_simplified || _ejs.getOptions().convertUserFilesToBase64()) { // Add common script together with Base64Images, if any
        Set<String> base64Images = _ejs.getSimInfoEditor().getBase64Images();
//        for (String image64 : base64Images) System.out.println("File to convert to base64 = "+image64);
        if (base64Images!=null && !base64Images.isEmpty()) { 
          try {
            String base64Text = XMLTransformerJava.getJavascriptForBase64Images(_ejs, base64Images, FileUtils.readTextFile(originalScriptFile, null));
            FileUtils.saveToFile(new File(viewFolder,destScriptFilename), XMLTransformerJava.UTF8_CHARSET, base64Text);
            done = true;
          } 
          catch (IOException exc) { exc.printStackTrace(); }
        }
      }
      if (!done) list.add(new PathAndFile(destScriptFilename,originalScriptFile)); // Just copy the standard common script
    }
    list.add(new PathAndFile("_ejs_library/"+JSObfuscator.sTEXTRESIZEDETECTOR_SCRIPT,new File(javascriptDir,JSObfuscator.sTEXTRESIZEDETECTOR_SCRIPT))); // Just copy the resize font detector script
    
    if (_simplified || _ejs.getOptions().useFullLibrary()) {
      String whichJSLib = JSObfuscator.whichJSLibrary(javascriptDir);
      list.add(new PathAndFile("_ejs_library/"+whichJSLib,new File(javascriptDir,whichJSLib)));
    }
    
    // First add files in the library directory with user defined files (if any)
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      String destName = FileUtils.getRelativePath(file, configDirPath, false);
      list.add(new PathAndFile(destName,file));
    }

    // If not there, add all the files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      String destName = FileUtils.getRelativePath(file, binConfigDirPath, false);
      list.add(new PathAndFile(destName,file));
    }

    // Finally of all, copy the simulation CSS files, if any
    String cssFilename = _ejs.getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) {
      File cssFile = new File(_ejs.getCurrentDirectory(),cssFilename);
      Generate.removeFromPafList(list,"_ejs_library/css/ejss.css");
      list.add(new PathAndFile("_ejs_library/css/ejss.css",cssFile));
    }
        
/*
    String cssPath = _ejs.getSimInfoEditor().getCSSFolder();
    if (cssPath.length()>0) {
      File cssFolder = new File(_ejs.getCurrentDirectory(),cssPath);
      for (File file : JarTool.getContents(cssFolder)) {
        String destName = "_ejs_library/css/"+FileUtils.getRelativePath(file, cssFolder, false);
        list.add(new PathAndFile(destName,file));
      }
    }
*/
    // Now copy all files in the list
    for (PathAndFile paf : list){
//            System.out.println ("Copying file "+paf.getFile().getAbsolutePath() +" to " + paf.getPath());
      JarTool.copy(paf.getFile(),new File(viewFolder,paf.getPath()));
    }

    // Add files from model elements
    String resources = _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString();
    if (resources != null) {
      StringTokenizer tkn = new StringTokenizer(resources,";");    
      File modelsDir = new File(_ejs.getBinDirectory(), "javascript/model_elements");
      while(tkn.hasMoreTokens()) {
        File resFolder = new File(modelsDir, tkn.nextToken().trim());
        for (File file : JarTool.getContents(resFolder)) { 
          String destName = FileUtils.getRelativePath(file, modelsDir, false);
          JarTool.copy(file, new File(viewFolder,destName));
        }
      }
    }
    
    // Add the SCORM stuff
    if (_scormOptions!=null) {
      File scormFolder = new File(_ejs.getBinDirectory(),ScormUtility.getLibraryPath(_scormOptions));
      for (File file : JarTool.getContents(scormFolder)) { 
        String destName = ScormUtility.getDestinationPath()+FileUtils.getRelativePath(file, scormFolder, false);
        JarTool.copy(file,new File(viewFolder,destName));
      }
      // Manifest file name and content:
      TwoStrings scormManifest = ScormUtility.createScormManifest(_zipFolder,simulation.getName(),metaFile,_scormOptions);
      try {
        FileUtils.saveToFile(new File (_zipFolder,scormManifest.getFirstString()), null, scormManifest.getSecondString());
      } catch (IOException exc) {
        exc.printStackTrace();
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileNotCreated")+" : " + scormManifest.getFirstString(),
            res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
    
    if (!_ejs.supportsJava()) { // save the metadata file
      StringBuffer buffer = new StringBuffer();
      for (TwoStrings ts : metaFile) {
        buffer.append(ts.getFirstString()+": "+ts.getSecondString()+"\n");
      }
      try {
        FileUtils.saveToFile (new File (viewFolder,"_metadata.txt"), null, buffer.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (!_simplified) { // save a README file
      StringBuffer buffer = new StringBuffer();
      Generate.addToReadMe(buffer,false);
//      for (TwoStrings ts : metaFile) {
//        buffer.append(ts.getFirstString()+": "+ts.getSecondString()+"\n");
//      }
      try {
        FileUtils.saveToFile (new File (viewFolder,"_ejs_README.txt"), null, buffer.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Done
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    return true;
  }
  
  
  /**
   * This compresses the simulation and its auxiliary files in EjsS XML format 
   * @param _ejs Osejs
   */
  static public void packageXMLSimulation (Osejs _ejs, String _targetName) {
    packageXMLSimulation (_ejs, _targetName, false);
  }
  
  /**
   * This compresses the simulation and its auxiliary files in EjsS XML format 
   * @param _ejs Osejs
   */
  static public void packageXMLSimulation (Osejs _ejs, String _targetName, boolean _isScorm) {
    boolean hasView = !_ejs.getHtmlViewEditor().getPages().isEmpty();

    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    if (_ejs.checkChangesAndContinue(false) == false) return; // The user canceled the action

    // Choose scorm options, if _isScorm
    ScormUtility.ScormOptions scormOptions = null;
    if (_isScorm) {
      scormOptions = ScormUtility.chooseOptions(_ejs);
      if (scormOptions==null) return;
    }
    
    String filename = _ejs.getSimInfoEditor().getSimulationName();
    if (filename==null) filename = _ejs.getCurrentXMLFilename();

    boolean warnBeforeOverwritting = true;
    File finalFile;
    if (_targetName!=null) { 
      if (!hasView) {
        if (_targetName.startsWith("ejss_model_")) {
          System.out.print("Package name changed from "+_targetName);
          _targetName = "ejss_info_" + _targetName.substring(11);
          System.out.println(" to "+_targetName);
        }
      }
      finalFile = new File(_ejs.getExportDirectory(),_targetName);
    }
    else { // Select the target
      String tentativeName = "ejss_model_"+filename+".zip";
      if (hasView)  {
        if (_ejs.supportsJava()) tentativeName = "ejsh_model_"+filename+".zip";
        if (_isScorm) tentativeName = "ejss_scorm_"+filename+".zip"; 
      }
      else tentativeName = "ejss_info_"+filename+".zip";
      File currentFile = new File(_ejs.getExportDirectory(),tentativeName);
      _targetName = FileChooserUtil.chooseFilename(_ejs, currentFile, "ZIP", new String[]{"zip"}, true);
      if (_targetName==null) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
      if (! (_targetName.toLowerCase().endsWith(".zip")) ) _targetName = _targetName + ".zip";
      else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
      finalFile = new File(_targetName);
      if (warnBeforeOverwritting && finalFile.exists()) {
        int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
            finalFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (selected != JOptionPane.YES_OPTION) {
          _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
          return;
        }
      }
    }

    // Create a working temporary directory
    File zipFolder;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
    } catch (Exception exc) { 
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    
    if (!GenerateJS.prepackageXMLSimulation(_ejs, xmlFile, filename, zipFolder, false, scormOptions)) { // false: non-simplified
      _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetName);
      return;
    }

    // Compress and remove working folder
    JarTool.compress(zipFolder, finalFile, null);
    JarTool.remove(zipFolder);
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+finalFile.getName());
  }


}
