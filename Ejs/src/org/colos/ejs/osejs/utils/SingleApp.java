package org.colos.ejs.osejs.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejss.xml.JSObfuscator;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.tools.JarTool;

public class SingleApp {
  static private final ResourceUtil res = new ResourceUtil("Resources");

  /**
   * Recursive copy from directory to directory
   **/
  static private void copyDirectory(File src, File dest) {
    // initial checks
    if(src == null || dest == null) return;
    if(!src.isDirectory()) return;
    if(src.listFiles() == null || src.listFiles().length == 0) return;

    // creates destination directory if not exist
    if(dest.exists()) { 
      if(!dest.isDirectory()) return;
    } else {
        dest.mkdir();
    }

    // recursive copy
    for(File file: src.listFiles()){
        File fileDest = new File(dest, file.getName());
        if(file.isDirectory()) {
            copyDirectory(file, fileDest);
        } else {
            if(fileDest.exists()) continue; // not overwrite
            try {
                FileUtils.copy(file, fileDest);
            } catch (Exception e) { } // continue
        }
    }
} 
  
  /**
   * Compresses a compiled simulation into material appropriated to create an Ionic App
   * NOTE: This is prepared to take a model ZIP, but it is actually regenerating the current simulation.
   * This way, one can benefit from extra information. 
   * YES: Resorting to the metadata file could be minimized, and the generation of files simplified...
   * @param _ejs
   */
  static public void convertToSingleApp(Osejs _ejs) {
//    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
//
//    String modelName = FileChooserUtil.chooseFilename(_ejs, _ejs.getExportDirectory(), "ZIP", new String[]{"zip"}, false);
//    if (modelName==null) {
//      _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
//      return;
//    }
    
    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    if (_ejs.checkChangesAndContinue(false) == false) return; // The user canceled the action
    
    // get template options
    SingleAppDialog.SingleAppOptions optionsInfo = SingleAppDialog.getSingleAppOptions(_ejs, _ejs.getMainFrame());
    if (optionsInfo==null) return;

    _ejs.getExportDirectory().mkdirs(); // In case it doesn't existz
    // Choose target    
    String targetName = "ejss_app_"+FileUtils.getPlainNameAndExtension(xmlFile).getFirstString() + ".zip";
    File targetFile = new File(_ejs.getExportDirectory(), targetName);
    targetName = FileChooserUtil.chooseFilename(_ejs, targetFile, "ZIP", new String[]{"zip"}, true);
    if (targetName==null) return;
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".zip")) targetName = targetName + ".zip";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists

    targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }

    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFile.getName());

    // Create a working temporary directory
    File zipFolder=null;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
//      new File(zipFolder,"css").mkdirs();
//      new File(zipFolder,"js").mkdirs();
    }
    catch (Exception exc) {  
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      JarTool.remove(zipFolder);
      return;
    }
    
    // Create packing according to Ionic version
    String IonicTemplateDirectory;
    if(optionsInfo.getFramework() == SingleAppDialog.IONIC_V1) {
      // www->model_pages
      //    ->js
      //    ->img
      //    ->css
      //    ->other_pages
      IonicTemplateDirectory = "javascript/SINGLE_APP/IONIC1/";
    } else { // Ionic v2
      // www->model_pages
      //    ->js
      //    ->img
      //    ->css
      // src->app
      //    ->pages->about
      //           ->home
      IonicTemplateDirectory = "javascript/SINGLE_APP/IONIC2/";            
    }

    // directory www->model_pages
    File pagesFolder = new File(zipFolder,"www/model_pages");
    pagesFolder.mkdirs();

    { // generate the simulation from EjsS
      // needed HTML simulation to avoid errors in Ionic serve
      Boolean previous = JSObfuscator.isGenerateXHTML();
      JSObfuscator.setGenerateXHTML(false);
      String simulationName = _ejs.getSimInfoEditor().getSimulationName();
      if (simulationName==null) simulationName = _ejs.getCurrentXMLFilename();
      if (!GenerateJS.prepackageXMLSimulation(_ejs, xmlFile, simulationName, pagesFolder, true, null)) { // true: simplified, null: Not SCORM
        _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+targetName);
        JSObfuscator.setGenerateXHTML(previous);
        return;
      }
      JSObfuscator.setGenerateXHTML(previous);      
    }
    
    ArrayList<String> ignoreFilesWithName = new ArrayList<String>();
    ignoreFilesWithName.add(".DS_Store");
    ignoreFilesWithName.add("_ejs_README.txt");

    { // Copy standard App files from Ionic template directory
      File javascriptDir = new File(_ejs.getBinDirectory(),IonicTemplateDirectory);
      String [] copyList = {
          "www/img/Gyroscope.png",  "www/img/ReaderFree.png", "www/img/ReaderInterface.png", 
          "www/img/ReaderPro.png", "www/js/common_script.js" };
      for (String copyName : copyList) 
        if (!FileUtils.copy(new File (javascriptDir,copyName), new File(zipFolder,copyName))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+copyName);

      // Copy specific App template files from Ionic template directory
      String [] copyTemplateDirs = {};
      if(optionsInfo.getFramework() == SingleAppDialog.IONIC_V1) {
        copyTemplateDirs = new String[] { "www" };
      } else { // Ionic v2
        copyTemplateDirs = new String[] { "src" };        
      }
      String templateDir = "";
      switch(optionsInfo.getTemplate()) {
        case SingleAppDialog.TEMPLATE_SIDE:
          templateDir = "SIDE/";
          break;
        case SingleAppDialog.TEMPLATE_SLIDES:
          templateDir = "SLIDES/";
          break;
        case SingleAppDialog.TEMPLATE_TABS:
          templateDir = "TABS/";
          break;
        default:
        case SingleAppDialog.TEMPLATE_CARD:
          templateDir = "CARD/";
          break;
      }
      for (String copyName : copyTemplateDirs) 
        copyDirectory(new File(javascriptDir,templateDir+copyName), new File(zipFolder,copyName)); 
    }
    
    { // Copy CSS files
      File userLibraryCSS = new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
      if (userLibraryCSS.exists()) {
        if (!FileUtils.copy(userLibraryCSS, new File(zipFolder,"www/css/ejss.css"))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+userLibraryCSS.getAbsolutePath());             
      }
      else {
        File libraryCSS = new File(_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/"+OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
        if (libraryCSS.exists()) {
          if (!FileUtils.copy(libraryCSS, new File(zipFolder,"www/css/ejss.css"))) 
            System.err.println(res.getString("Osejs.File.SavingError")+"\n"+libraryCSS.getAbsolutePath());             
        } 
      }
    }
    { // Copy javascript files
      ArrayList<TwoStrings> jsFiles = new ArrayList<TwoStrings>();
      jsFiles.add(new TwoStrings("scripts/textresizedetector.js","www/js/textresizedetector.js")); 
      jsFiles.add(new TwoStrings(JSObfuscator.LIB_MIN_FILENAME,  "www/js/"+JSObfuscator.LIB_MIN_FILENAME));

      File javascriptLibDir = new File(_ejs.getBinDirectory(),"javascript/lib");
      for (TwoStrings ts : jsFiles) {
        File srcFile = new File (javascriptLibDir,ts.getFirstString());
        if (!FileUtils.copy(srcFile, new File(zipFolder,ts.getSecondString()))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+srcFile.getAbsolutePath());
      }
    }
    
    ArrayList<TwoStrings> relocateFiles = new ArrayList<TwoStrings>();
    relocateFiles.add(new TwoStrings("_ejs_library/images/Gyroscope.gif",         "../img/Gyroscope.png")); 
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejss.css",                 "../css/ejss.css"));
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejsPage.css",              "../css/ejss.css"));
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejsSimulation.css",        "../css/ejss.css")); 
    relocateFiles.add(new TwoStrings("_ejs_library/scripts/textresizedetector.js",   "../js/textresizedetector.js")); 
    relocateFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT,    "../js/common_script.js"));
    relocateFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.LIB_MIN_FILENAME,  "../js/"+JSObfuscator.LIB_MIN_FILENAME));

    //process metadata file
    String modelTitle = null;
    String modelAuthor ="";
    String modelAuthorImage ="";
    String logoImage="";
    ArrayList<TwoStrings> modelPages = new ArrayList<TwoStrings>();
    int simulationPageIndex = 0;

    // Uncompress model zip files into the temporary zip directory
    // and process the files in it
    {
//      File modelFile = new File(modelName);
//      JarTool.unzip(modelFile, pagesFolder);
      File metadataFile = new File(pagesFolder,"_metadata.txt");
      if (!metadataFile.exists()) { // This is not an ejss_model file
        _ejs.getOutputArea().println("Warning: generated files do not include an EJS Javascript model. Ignored!");
        JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
            res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
        JarTool.remove(zipFolder);
        return;
      }

      String lastPageTitle=null;
      String htmlMain=null;
      HashSet<String> modelPagesSet = new HashSet<String>();
      StringTokenizer tkn = new StringTokenizer(FileUtils.readTextFile(metadataFile, null),"\n");
      while (tkn.hasMoreTokens()) {
        String line = tkn.nextToken();
        if (line.startsWith("title:")) {
          modelTitle = line.substring(6).trim();
        }
        else if (line.startsWith("logo-image:")) {
          logoImage = line.substring(11).trim();
        }
        else if (line.startsWith("author:")) {
          if (modelAuthor.length()>0) modelAuthor += ";"+line.substring(7).trim();
          else modelAuthor = line.substring(7).trim();
        }
        else if (line.startsWith("author-image:")) {
          if (modelAuthorImage.length()>0) modelAuthorImage += ";"+line.substring(13).trim();
          else modelAuthorImage = line.substring(13).trim();
        }
        else if (line.startsWith("html-main:")) {
          htmlMain = line.substring(11).trim();
        }
        else if (line.startsWith("page-title:")) {
          lastPageTitle = line.substring(11).trim();
        }
        else if (line.startsWith("page-index:")) {
          String pageIndex = line.substring(11).trim();
          boolean isSimulation = (pageIndex.endsWith("_Simulation.html") || pageIndex.endsWith("_Simulation.xhtml"));
          modelPages.add(new TwoStrings(lastPageTitle, pageIndex));
          modelPagesSet.add(pageIndex);
          if (isSimulation) { // see if there is a js file
            simulationPageIndex = modelPages.size()-1;
          }
          File pageFile = new File(pagesFolder,pageIndex);          
          BookApp.relocateFiles(pageFile,isSimulation,relocateFiles);          
        } // Metadata processed
      } // end of processing metadata

      // Process each extracted directory
      String pagesFolderPath = FileUtils.getPath(pagesFolder); 

      String commonScriptFilename = "_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT;

      for (File file : JarTool.getContents(pagesFolder)) {
        String filename = FileUtils.getRelativePath(file, pagesFolderPath, false);
        if (filename.startsWith("/")) filename = filename.substring(1);
        boolean mustDelete = false;
        String extension = FileUtils.getPlainNameAndExtension(file).getSecondString().toLowerCase();

        // Delete EjsS generated files that are not needed 
        if (htmlMain!=null && filename.equals(htmlMain)) {
          String content = FileUtils.readTextFile(file, OsejsCommon.getUTF8());
          if (content.indexOf("<frameset cols=")>=0) mustDelete = true;
        }
        else if (filename.endsWith("_Contents.html") || filename.endsWith("_Contents.xhtml")) {
          String content = FileUtils.readTextFile(file, OsejsCommon.getUTF8());
          if (content.indexOf("<div class=\"contents\"")>=0) mustDelete = true;
        }
        else if (filename.endsWith("_opensocial.xml")) {
          String content = FileUtils.readTextFile(file, OsejsCommon.getUTF8());
          if (content.indexOf("<Module>")>0 && content.indexOf("<ModulePrefs")>0) mustDelete = true;
        }
        else if (filename.endsWith(commonScriptFilename)) { // replace common script with the common script needed
          EPub.combineCommonScript(file, new File(zipFolder,"www/js/common_script.js"), filename, modelTitle);
          //JarTool.copy(file, new File(zipFolder,"js/common_script.js"));
          mustDelete = true;
        }
        else if (filename.equals("_ejs_library/css/ejss.css")) { // replace system CSS with user defined CSS
          if (!FileUtils.copy(file, new File(zipFolder,"www/css/ejss.css"))) 
            System.err.println(res.getString("Osejs.File.SavingError")+"\n"+file.getAbsolutePath());             
          mustDelete = true;
        }
        else if (filename.startsWith("_ejs_library/")) { // remove all library files, common_script.js will do the rest
          mustDelete = true;
        }
        else if (extension.equals("html") || extension.equals("xhtml")) {
//          if (!modelPagesSet.contains(filename)) { // There may be XHTML as HTMLArea content
//            System.err.println(res.getString("Package.EPUBIgnoringFile")+ ": "+filename+ " ("+modelTitle+")");
//            mustDelete = true;
//          }
        }
        else if (OsejsCommon.isEJSfile(file)) mustDelete = true;
        else {
          for (String ignoreFilename : ignoreFilesWithName) {
            if (filename.equals(ignoreFilename)) mustDelete = true;
          }
        }
        if (mustDelete) {
          file.delete();
          continue;
        }

      } // end processing model folder contents
    }

   // Create main files

    if (modelTitle.length()<=0) modelTitle = "Untitled";
    if (modelAuthor.length()<=0) modelAuthor = "Unknown author";
    
    createAppStructureFile(_ejs, zipFolder, 
        logoImage, getAuthorInfo(modelAuthor, modelAuthorImage),
        modelTitle, modelPages, simulationPageIndex, optionsInfo);
    
    // Compress and remove working folder
    JarTool.compress(zipFolder, targetFile,null);
    JarTool.remove(zipFolder);
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+targetFile.getName());
    int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),
        res.getString("Generate.JarFileCreated")+ " : "+targetFile.getName()+"\n"+
        res.getString("SingleApp.WantHelp"),
        res.getString("Information"), JOptionPane.YES_NO_OPTION);
    if (selected == JOptionPane.YES_OPTION) {
//      String localPage = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(_ejs.getBinDirectory())+
//          "/javascript/SINGLE_APP/HowToCreateAnApp.xhtml");
      String urlPage = "http://www.um.es/fem/EjsWiki/Main/CreatingSingleApps";
      org.opensourcephysics.desktop.OSPDesktop.displayURL(urlPage);
    }
  }

  static public  java.util.List<TwoStrings> getAuthorInfo(String authorNames, String authorImages) {
    ArrayList<TwoStrings> authorInfo = new ArrayList<TwoStrings>();
    String[] names = authorNames.split(";");
    String[] images = authorImages.split(";");
    for (int i=0; i<names.length; i++) {
      authorInfo.add(new TwoStrings(names[i],(i<images.length) ? images[i] : null));
    }
    return authorInfo;
  }
  
  static public void createAppStructureFile(Osejs _ejs, File _workingFolder, String _logoImage, java.util.List<TwoStrings> _authorInfo,
      String _title, ArrayList<TwoStrings> _pagesList, int _simulationIndex, SingleAppDialog.SingleAppOptions _options) {

    StringBuffer buffer = new StringBuffer();
    // Collecting about information
    buffer.append("var about_about = \""+res.getString("Package.About")+"\";\n");
    buffer.append("var about_info = \""+res.getString("Information")+"\";\n");
    buffer.append("var about_logoImage = \""+_logoImage+"\";\n");
    String abstractTxt = _ejs.getSimInfoEditor().getAbstract().trim();
    buffer.append("var about_abstractTxt = \"");
    if (abstractTxt.length()>0) {
      boolean hasTag = (abstractTxt.charAt(0)=='<'); 
      if (!hasTag) {
        buffer.append("<p>");
        buffer.append(abstractTxt.replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
        buffer.append("</p>");
      } else {
        buffer.append(abstractTxt.replaceAll("(\r\n|\n\r|\r|\n)", " "));
      }
    }
    buffer.append("\";\n");
    buffer.append("var about_copyright = \""+res.getString("Package.Copyright")+"\";\n");
    String copyrightTxt = _ejs.getSimInfoEditor().getCopyright().trim();
    buffer.append("var about_copyrightTxt = \"");    
    if (copyrightTxt.length()>0) {
      boolean hasTag = (copyrightTxt.charAt(0)=='<'); 
      if (!hasTag) {
        buffer.append("<p>");
        buffer.append(copyrightTxt.replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
        buffer.append("</p>");
      } else {
        buffer.append(copyrightTxt.replaceAll("(\r\n|\n\r|\r|\n)", " "));
      }
    }
    buffer.append("\";\n");
    buffer.append("var about_authorInfo = \"");    
    for (TwoStrings ts : _authorInfo) {
      buffer.append("<p>");
      if (ts.getSecondString()!=null) buffer.append("<img src='model_pages/"+ts.getSecondString()+"' alt='Photo of "+ts.getFirstString()+"' /> ");
      buffer.append(ts.getFirstString()+"</p>");
    }
    buffer.append("\";\n");
    
    // Collecting app information
    buffer.append("var app_title = \""+_title+"\";\n");
    buffer.append("var app_menu_title = \""+res.getString("Package.Contents")+"\";\n");
    buffer.append("var app_simulation_first = "+_options.isSimulationFirst()+";\n");
    buffer.append("var app_simulation_index = "+_simulationIndex+";\n");
    buffer.append("var app_full_screen = "+_options.isFullScreen()+";\n");
    buffer.append("var app_locking = "+_options.getLocking()+";\n");
    
    buffer.append("\n");
    buffer.append("var app_toc = [");
    int counter = 1;
    for (TwoStrings page : _pagesList) {
      if (counter==1) buffer.append("\n");
      else buffer.append(",\n");
      buffer.append("  {\n");
      buffer.append("    title: \""+page.getFirstString()+"\",\n");
      buffer.append("    url: \"model_pages/"+page.getSecondString()+"\",\n");
      buffer.append("    type: \"model_page\"\n");
      buffer.append("  }");
      counter++;
    }
    buffer.append(",\n");
    buffer.append("  {\n");
    buffer.append("    title: \""+res.getString("Package.About")+"\",\n");
    buffer.append("    url: \"other_pages/about.html\",\n");
    buffer.append("    type: \"other_page\"\n");
    buffer.append("  }");    
    buffer.append("\n];\n");

    File outputFile = new File (_workingFolder,"www/js/pages.js");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\njs/book.js", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

}
