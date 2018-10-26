package org.colos.ejs.osejs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.XMLTransformerJava;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class EPub {
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final String CONTENT_FOLDER = "models";  
  static private enum CHANGE_TYPE { FULL_CHANGE, MIDDLE_CHANGE, PLAIN_CHANGE }; 
  
  /**
   * Compresses a list of simulations in a single ZIP file
   * @param _ejs
   * @param _listInfo 
   * @param _targetFile
   */
  static public void epubSeveralXMLSimulations(Osejs _ejs, CreateListDialog.ListInformation _listInfo, File _targetFile) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    //System.out.println ("packaging "+_targetFile.getAbsolutePath());
    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());

    // Create a working temporary directory
    File zipFolder=null;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
    } catch (Exception exc) {  
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      JarTool.remove(zipFolder);
      return;
    }

    { // Copy standard ePub files
      String[] cssFilenames = 
        { "OEBPS/stylesheet.css", "OEBPS/cover-image.png", "OEBPS/CC_icon.png", "OEBPS/media_error_page.xhtml", 
          "META-INF/com.apple.ibooks.display-options.xml",  "META-INF/container.xml"   };
      File javascriptDir = new File(_ejs.getBinDirectory(),"javascript");
      for (String cssFilename : cssFilenames) {
        if (!FileUtils.copy(new File (javascriptDir,cssFilename), new File(zipFolder,cssFilename))) {
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+cssFilename);
        }
        // Now copy user-defined stylesheet, if any
        File userFile = new File(_ejs.getConfigDirectory(),"javascript/"+cssFilename);
        if (userFile.exists()) FileUtils.copy(userFile, new File (zipFolder,cssFilename));
      }
    }
    
    int foldersStructure = _listInfo.getFoldersStructure();
    boolean ignoreHTML = !_listInfo.getIncludeHTMLFiles();
    boolean separateChapters = _listInfo.getSeparateChapters();

    String title = _listInfo.getName().trim();
    if (title.length()<=0) title = "Untitled";
    String author = _listInfo.getSubtitle();
    if (author.length()<=0) author = "Uknown author";

    createCoverFile(_ejs,zipFolder, title, _listInfo.getImagefile());
    createTitleFile(_ejs, zipFolder, title, author);
    {
      String logoFilename;
      switch (foldersStructure) {
        case CreateListDialog.STRUCTURE_FLAT : logoFilename = CONTENT_FOLDER+"/_ejs_library/images/Gyroscope.gif"; break;
        case CreateListDialog.STRUCTURE_STRICT : logoFilename = "Images/_ejs_library_images_Gyroscope.gif"; break;
        default : 
        case CreateListDialog.STRUCTURE_NESTED : logoFilename = "model1/_ejs_library/images/Gyroscope.gif"; break;
      }
      createCopyrightFile(_ejs, zipFolder, logoFilename);
    }

    if (foldersStructure==CreateListDialog.STRUCTURE_STRICT) { // Create standard directories for strict ePub structure
      new File(zipFolder,"OEBPS/Audio").mkdirs();
      new File(zipFolder,"OEBPS/Fonts").mkdirs();
      new File(zipFolder,"OEBPS/Images").mkdirs();
      new File(zipFolder,"OEBPS/Misc").mkdirs();
      new File(zipFolder,"OEBPS/Styles").mkdirs();
      new File(zipFolder,"OEBPS/Text").mkdirs();
      new File(zipFolder,"OEBPS/Video").mkdirs();
    }

    // Prepare buffers for the processing
    StringBuffer manifestBuffer = new StringBuffer();
    StringBuffer spineBuffer = new StringBuffer();
    ArrayList<TwoStrings> tocList = new ArrayList<TwoStrings>();
    Hashtable<String,String> filesItem = new Hashtable<String,String>();
    HashSet<String> repeatedFiles = new HashSet<String>();

    Hashtable<String,TwoStrings> renamedFiles = new Hashtable<String,TwoStrings>(); // Files that were renamed (like from ./folderName/filename.png to Images/folderName_filename.png)
    // Set of files that go to TEXT and might contain references to renamedFiles
    HashSet<File> textFiles = null; 


    manifestBuffer.append("    <item id=\"media_error_page\" href=\"media_error_page.xhtml\" media-type=\"application/xhtml+xml\" />\n");
    if (foldersStructure==CreateListDialog.STRUCTURE_STRICT) {
      textFiles = new HashSet<File>();                              

      // old reference ---> Substring present in full file name + new reference
      renamedFiles.put("_ejs_library/"+JSObfuscator.LIB_MIN_FILENAME, new TwoStrings("_Simulation.", "../Misc/_ejs_library_"+JSObfuscator.LIB_MIN_FILENAME));
      renamedFiles.put("_ejs_library/"+JSObfuscator.LIB_MAX_FILENAME, new TwoStrings("_Simulation.", "../Misc/_ejs_library_"+JSObfuscator.LIB_MAX_FILENAME));
      renamedFiles.put("_ejs_library/scripts/common_script.js", new TwoStrings("_Simulation.", "../Misc/_ejs_library_scripts_common_script.js"));
      renamedFiles.put("_ejs_library/css/ejsSimulation.css",    new TwoStrings("_Simulation.", "../Styles/_ejs_library_css_ejsSimulation.css")); 
      renamedFiles.put("_ejs_library/css/ejsPage.css",          new TwoStrings("_Simulation.", "../Styles/_ejs_library_css_ejsPage.css")); 
      renamedFiles.put("_ejs_library/css/ejss.css",             new TwoStrings("_Simulation.", "../Styles/_ejs_library_css_ejss.css")); 
      renamedFiles.put("_ejs_library/images/cc_icon.png",       new TwoStrings("_Simulation.", "../Images/_ejs_library_images_cc_icon.png"));
      //renamedFiles.put("../images/EjsLogo.gif",                 new TwoStrings("_ejs_library_css", "../Images/_ejs_library_images_EjsLogo.gif")); // in EjsS css
    }

    java.util.List<PathAndFile> listOfZipFiles = _listInfo.getList();
    String baseDirPath = FileUtils.getPath(new File(zipFolder,"OEBPS"));

    // Uncompress all zip files into the zip directory
    for (int modelCounter=1,n=listOfZipFiles.size(); modelCounter<=n; modelCounter++) {
      PathAndFile paf = listOfZipFiles.get(modelCounter-1);
      String modelName = "model"+modelCounter;
      File modelFolder = new File(zipFolder,"OEBPS/"+modelName);
      modelFolder.mkdirs();
      JarTool.unzip(paf.getFile(), modelFolder);
      File metadataFile = new File(modelFolder,"_metadata.txt");
      if (!metadataFile.exists()) { // This is not an ejss_model file
        _ejs.getOutputArea().println("Warning: ZIP file "+paf.getPath()+" does not contain an EJS Javascript model. Ignored!");
        JarTool.remove(modelFolder);
        continue;
      }

      // process metadata file
      int pageCounter = 1;
      int simCounter = 1;

      String modelTitle = null;
      String modelAuthor ="";
      String lastPageTitle=null;
      String htmlMain=null;
      HashSet<String> modelPagesSet = new HashSet<String>();
      ArrayList<String[]> modelPages = new ArrayList<String[]>();
      String metadata = FileUtils.readTextFile(metadataFile, null);
      StringTokenizer tkn = new StringTokenizer(metadata,"\n");
      while (tkn.hasMoreTokens()) {
        String line = tkn.nextToken();
        if (line.startsWith("title:")) {
          modelTitle = line.substring(6).trim();
        }
        else if (line.startsWith("author:")) {
          if (modelAuthor.length()>0) modelAuthor += ";"+line.substring(7).trim();
          else modelAuthor = line.substring(7).trim();
        }
        else if (line.startsWith("html-main:")) {
          htmlMain = modelName+"/"+line.substring(11).trim();
        }
        else if (line.startsWith("page-title:")) {
          lastPageTitle = line.substring(11).trim();
        }
        else if (line.startsWith("page-index:")) {
          String pageIndex = line.substring(11).trim();
          String targetFilename;
          switch (foldersStructure) {
            case CreateListDialog.STRUCTURE_FLAT : 
              if (pageIndex.indexOf("_Simulation.")>=0) targetFilename = CONTENT_FOLDER+"/"+ modelName+"_"+ pageIndex; 
              else targetFilename = CONTENT_FOLDER+"/"+ pageIndex;
              break;
            case CreateListDialog.STRUCTURE_STRICT : targetFilename = "Text/"+modelName+"_"+pageIndex.replace('/', '_'); break;
            default : 
            case CreateListDialog.STRUCTURE_NESTED : targetFilename = modelName+"/"+pageIndex; break;
          }

          if (pageIndex.endsWith("_Simulation.html") || pageIndex.endsWith("_Simulation.xhtml")) {
            modelPages.add(new String[] { modelName+"_sim"+simCounter, lastPageTitle, targetFilename });
            modelPagesSet.add(modelName+"/"+pageIndex);
            simCounter++;
          }
          else {
            if (! (ignoreHTML && pageIndex.toLowerCase().endsWith(".html"))) { 
              modelPages.add(new String[] { modelName+"_"+pageCounter, lastPageTitle, targetFilename });
              modelPagesSet.add(modelName+"/"+pageIndex);
              pageCounter++;
            }
          }
        } // Metadata processed
      } // end of processing metadata
      // Process each extracted directory

      int imgCounter = 1;
      //      int jsCounter = 1;
      int othersCounter = 1;

      String metadataFilename = modelName+"/_metadata.txt";
      String modelDirPath = FileUtils.getPath(modelFolder); 

      String[] ignoreFilesWithSuffix = {
          "_ejs_library/html/EjsLauncher.html",
          "_ejs_library/css/ejsGroupPage.css",
          "_ejs_library/css/ejsContentsLeft.css",
          "_ejs_library/css/ejsContentsTop.css",
          "_ejs_library/images/EjsMainIcon.gif",
          "_ejs_library/images/EjsLogo.gif",
          "_ejs_library/images/EjsIcon.gif",
          "_ejs_library/images/cc_icon.png",
          ".DS_Store",
          "_ejs_README.txt",
          "index.html"
      };
      
      for (File file : JarTool.getContents(modelFolder)) {
        String filename = FileUtils.getRelativePath(file, baseDirPath, false);
        boolean mustDelete = false;
        String extension = FileUtils.getPlainNameAndExtension(file).getSecondString().toLowerCase();

        // Delete EjsS generated files that are not needed in the ePub 
        if (filename.equals(metadataFilename)) mustDelete = true;
        else if (htmlMain!=null && filename.equals(htmlMain)) {
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
        else if (filename.endsWith("_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT)) { // replace common script with the common script for ePubs
//          File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/lib");
//          File correctFile  = new File(javascriptDir,"scripts/common_script_ePub.js");
//          JarTool.copy(correctFile, file); // replace the file in origin
          String scriptContent = FileUtils.readTextFile(file, XMLTransformerJava.UTF8_CHARSET);
//          FileUtils.replaceString(scriptContent, "_isEPub = false", "_isEPub = true");
          scriptContent += "\n_isEPub = true;\n";
          try {
            FileUtils.saveToFile(file, XMLTransformerJava.UTF8_CHARSET, scriptContent);
          } catch (IOException exc) {
            File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/lib");
            File correctFile  = new File(javascriptDir,"scripts/common_script_ePub.js");
            JarTool.copy(correctFile, file); // replace the file in origin
            exc.printStackTrace();
          }
//          if (foldersStructure==CreateListDialog.STRUCTURE_STRICT) JarTool.copy(correctFile, new File(zipFolder,"OEBPS/Misc/_ejs_library_scripts_common_script.js"));
//          else JarTool.copy(correctFile, file);
        }
        else if (ignoreHTML && extension.equals("html")) {
          System.err.println(res.getString("Package.EPUBIgnoringFile")+ ": "+filename+ " ("+modelTitle+")");
          mustDelete = true;
        }
        else if (extension.equals("xhtml")) { // There may be XHTML as HTMLArea content
//          if (!modelPagesSet.contains(filename)) { 
//            System.err.println(res.getString("Package.EPUBIgnoringFile")+ ": "+filename+ " ("+modelTitle+")");
//            mustDelete = true;
//          }
        }
        else if (OsejsCommon.isEJSfile(file)) mustDelete = true;
        else {
          for (String ignoreSuffix : ignoreFilesWithSuffix) {
            if (filename.endsWith(ignoreSuffix)) mustDelete = true;
          }
        }
        if (mustDelete) {
          file.delete();
          continue;
        }

        // Decide the target filename for a given file
        MimeInfo fileInfo = MimeInfo.infoFor(extension);

        String targetFilename;
        String originalName;
        File targetFile=null;
        switch (foldersStructure) {
          default : 
          case CreateListDialog.STRUCTURE_NESTED : 
            targetFilename = null; // it is not necessary to move it 
            break;
          case CreateListDialog.STRUCTURE_FLAT : 
            originalName = FileUtils.getRelativePath(file, modelDirPath, false);
            if (originalName.endsWith("_Simulation.xhtml")) targetFilename = CONTENT_FOLDER + "/"+ modelName+"_"+ originalName;
            else targetFilename = CONTENT_FOLDER + "/"+ originalName;
            break;
          case CreateListDialog.STRUCTURE_STRICT : 
            originalName = FileUtils.getRelativePath(file, modelDirPath, false);
            String newName = originalName.replace('/', '_');
//            if (newName.endsWith("_Simulation.html") || newName.endsWith("_Simulation.xhtml")) targetFilename = fileInfo.targetFolder + "/" + modelName + "_ "+ newName;
//            else targetFilename = fileInfo.targetFolder + "/" + newName;
            if (fileInfo.getTargetFolder().equals("Text")) targetFilename = fileInfo.getTargetFolder() + "/" + modelName+"_"+ newName; 
            else targetFilename = fileInfo.getTargetFolder() + "/" + newName;
            // Signal the file as renamed
            if (targetFilename.equals("Misc/_ejs_README.txt")) break; // No need to reference this one
            if (targetFilename.endsWith("_Simulation.xhtml")) break; // No need to reference this one
            if (targetFilename.endsWith("Misc/_ejs_library_scripts_common_script.js")) break; // Already added
            // if (fileInfo.getTargetFolder().equals("Text")) break; // HTML files are not referenced by other files // YES, HTMLArea can reference a Text file
            // Add it to the list of renamed files
            if (originalName.startsWith("_ejs_library/images/org")) { // Only those in org/
              String entryName = originalName.substring(19); 
              if (renamedFiles.get(entryName)==null) renamedFiles.put(entryName, new TwoStrings("_Simulation.","../"+targetFilename));
            }
            else { // It's a user file
              int folderIndex = originalName.lastIndexOf('/'); // Find directory structure
              String folderHint;
              if (folderIndex<0) folderHint = null; // Same folder
              else folderHint = originalName.substring(0, folderIndex+1).replace('/', '_');
              if (renamedFiles.get(originalName)==null) {
                System.err.println ("Adding "+originalName +" to renamed ("+folderHint+", "+"../"+targetFilename);
                renamedFiles.put(originalName, new TwoStrings(folderHint,"../"+targetFilename));
              }
            }
            break;
        }

        // Copy the files in place
        if (targetFilename==null) {
          targetFilename = filename;
          targetFile = file;
        }
        else { // File needs to be copied somewhere else
          targetFile = new File(zipFolder,"OEBPS/"+targetFilename);
          if (targetFile.exists()) {
            String removePrefix = targetFilename.substring(targetFilename.indexOf('/')+1);
            if (!removePrefix.startsWith("_ejs_library")) {
              //              _ejs.getOutputArea().println(res.getString("Package.EPUBIgnoringFile")+ ": "+filename + " ("+modelTitle+")");
              System.err.println(res.getString("Package.EPUBIgnoringFile")+ ": "+filename + " ("+modelTitle+")");
              repeatedFiles.add(targetFilename);
            }
            else if (removePrefix.indexOf("common_script.js")>=0) { // merge all common_script.js files (to combine all base64 images)
              combineCommonScript(file, targetFile, targetFilename, modelTitle);
            }
            continue;
          }
          else if (!(extension.equals("html")) ) { // Should HTML files be copied to Misc?
            //System.err.println("Copying file "+filename+" to "+targetFile.getAbsolutePath());
            JarTool.copy(file,targetFile);
          }
        }

        // Create the correct manifest entry and maybe process the file

        if (fileInfo.getTargetFolder().equals("Images")) {
          String entryName = modelName+"_img_"+imgCounter;
          manifestBuffer.append("    <item id=\""+entryName+"\" href=\""+targetFilename+"\" media-type=\""+fileInfo.getMimeType()+"\" />\n");
          filesItem.put(targetFilename, entryName);
          imgCounter++;
        }
        //        else if (extension.equals("js")) { Removed following Mario suggestion.
        //          manifestBuffer.append("    <item id=\""+modelFolderName+"_js_"+jsCounter+"\" href=\""+filename+"\" media-type=\"text/javascript\"  properties=\"scripted\" />\n");
        //          jsCounter++;
        //        }
        else if (fileInfo.getTargetFolder().equals("Text")) {
          String[] refFound = null;
          for (String[] entry : modelPages) {
            if (entry[2].equals(targetFilename)) {
              refFound = entry;
              break;
            }
          }
          if (extension.equals("html")) {
            //            _ejs.getOutputArea().println(res.getString("Package.EPUBConvertingHTML")+": "+filename+" ("+modelTitle+")");
            System.err.println(res.getString("Package.EPUBConvertingHTML")+": "+filename+" ("+modelTitle+")");
            try {
              String xFilename = targetFilename.substring(0, targetFilename.length()-4)+"xhtml";
              //              saveAsXHTML(new FileInputStream(file),new FileOutputStream(new File(zipFolder,"OEBPS/"+xFilename)));
              saveAsXHTML(file, targetFile = new File(zipFolder,"OEBPS/"+xFilename),OsejsCommon.getUTF8());
              file.delete();
              if (refFound!=null) refFound[2] = xFilename;
              targetFilename = xFilename;
            } catch (Exception e) {
              //              _ejs.getOutputArea().println("Error trying to convert to XHTML file : "+filename+"!");
              System.err.println("Error trying to convert to XHTML file : "+filename+"!");
              e.printStackTrace();
            }
          }
          boolean addMathmlProperty = file.exists() && isMathMLFile(file);
          if (refFound!=null) {
            if (refFound[0].indexOf("_sim")>=0) {
              String propertiesStr =  addMathmlProperty ? " properties=\"scripted mathml\" " : " properties=\"scripted\" ";
              manifestBuffer.append("    <item id=\""+refFound[0]+"\" href=\""+targetFilename+"\" media-type=\"application/xhtml+xml\" "+propertiesStr+" />\n");
              removeMetadataDiv(file, targetFile, OsejsCommon.getUTF8());
              if (textFiles!=null) textFiles.add(targetFile);            }
            else {
              String propertiesStr =  addMathmlProperty ? " properties=\"mathml\" " : "";
              manifestBuffer.append("    <item id=\""+refFound[0]+"\" href=\""+targetFilename+"\" media-type=\"application/xhtml+xml\" "+propertiesStr+" />\n");
              if (textFiles!=null) textFiles.add(targetFile);            
            }
            filesItem.put(targetFilename, refFound[0]);
          }
          else { // No reference found
            String propertiesStr =  addMathmlProperty ? " properties=\"mathml\" " : "";
            String entryName = modelName+"_other_"+othersCounter;
            manifestBuffer.append("    <item id=\""+entryName+"\" href=\""+targetFilename+"\" media-type=\""+fileInfo.getMimeType()+"\" "+propertiesStr+ " />\n");
            filesItem.put(targetFilename, entryName);
            if (textFiles!=null) textFiles.add(targetFile);
            othersCounter++;
          }
        } // end of fileInfo.targetFolder.equals("Text")
        else {
          if (textFiles!=null) {
            if (extension.equals("js")) { // Check all JS files except the EjsS libraries. These have no file references in them
              if (! (targetFilename.endsWith(JSObfuscator.LIB_MIN_FILENAME) || targetFilename.endsWith(JSObfuscator.LIB_MAX_FILENAME) ))
                textFiles.add(targetFile);
            }
            else if (extension.equals("css")) textFiles.add(targetFile); // check all CSS files
          }
          String entryName = modelName+"_other_"+othersCounter;
          if (extension.equals("pdf")) { // In order to add a fallback pdf page
            manifestBuffer.append("    <item id=\""+entryName+"\" href=\""+targetFilename+"\" media-type=\""+fileInfo.getMimeType()+"\" fallback=\"media_error_page\" />\n");
            spineBuffer.append("    <itemref idref=\""+entryName+"\" linear=\"no\" />\n");
          }
          else if (fileInfo.getTargetFolder().equals("Audio")) {
            manifestBuffer.append("    <item id=\""+entryName+"\" href=\""+targetFilename+"\" media-type=\""+fileInfo.getMimeType()+"\" fallback=\"media_error_page\" />\n");
            spineBuffer.append("    <itemref idref=\""+entryName+"\" linear=\"no\" />\n");
          }
          else manifestBuffer.append("    <item id=\""+entryName+"\" href=\""+targetFilename+"\" media-type=\""+fileInfo.getMimeType()+"\" />\n");
          filesItem.put(targetFilename, entryName);
          othersCounter++;
          
        }
      }

      if (foldersStructure!=CreateListDialog.STRUCTURE_NESTED) JarTool.remove(modelFolder);

      // Add main section entry
      if (separateChapters) {
        createModelFile(_ejs, zipFolder, modelTitle, modelAuthor, modelName); // add main model page
        manifestBuffer.append("    <item id=\""+modelName+"\" href=\""+modelName+"_toc.xhtml\" media-type=\"application/xhtml+xml\" />\n");
        spineBuffer.append("    <itemref idref=\""+modelName+"\" />\n");
        tocList.add(new TwoStrings("model:"+modelCounter+" "+modelTitle,modelName+"_toc.xhtml"));
      }
      for (int page=1,nPages=modelPages.size(); page<=nPages; page++) {
        String[] entry = modelPages.get(page-1);
        String reference=null;
        //        System.err.println ("Checking repeated file for "+entry[2]);
        if (repeatedFiles.contains(entry[2])) { //          System.err.println ("File repeated "+entry[2]);
          reference = filesItem.get(entry[2]); //          System.err.println ("Found as "+reference);
          if (reference==null) System.err.println ("Warning: Reference for repeated file not found! : "+entry[2]);
          // Do not add a repeated entry!
          //else spineBuffer.append("    <itemref idref=\""+reference+"\" />\n");
        }
        if (reference==null) {
          spineBuffer.append("    <itemref idref=\""+entry[0]+"\" />\n");
          switch(_listInfo.getNumberingOption()) {
            default : 
            case CreateListDialog.NUMBERING_OPTION_NONE : 
              tocList.add(new TwoStrings("page:"+entry[1],entry[2]));
              break;
            case CreateListDialog.NUMBERING_OPTION_CHAPTER : 
              tocList.add(new TwoStrings("page:"+modelCounter+" "+entry[1],entry[2]));
              break;
            case CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION : 
              tocList.add(new TwoStrings("page:"+modelCounter+"."+page+" "+entry[1],entry[2]));
              break;
          }
        }
      }
    } // end of for loop for each model

    // strict folder cross-referencing
    // Correct links inside HTML files for files that were renamed
    if (textFiles!=null) { // Only if Strict folder structure forces to do this
//      for (String key : renamedFiles.keySet()) {
//        TwoStrings twoStr = renamedFiles.get(key);
//        System.err.println ("Replace key "+key+ " in "+twoStr.getFirstString()+"  to : "+twoStr.getSecondString());
//      }
      System.err.println ("\n");
      HashSet<TwoStrings> changesInFile = new HashSet<TwoStrings>();
      for (File textFile : textFiles) {
        String textFilename = textFile.getName();
        System.err.println ("\nChanging references in "+textFilename);
        String content = FileUtils.readTextFile(textFile, OsejsCommon.getUTF8());
        String modifiedContent=content;
        changesInFile.clear();
        for (String key : renamedFiles.keySet()) {
          TwoStrings twoStr = renamedFiles.get(key);
          String fileHint = twoStr.getFirstString();
          if (fileHint==null) {
//            System.err.println ("Checking reference \""+key +"\" in  file "+ textFilename);
            modifiedContent = changesInContent (modifiedContent,key, twoStr.getSecondString(), changesInFile,CHANGE_TYPE.FULL_CHANGE);
          }
          else { // fileHint not null
            if (fileHint.equals("_Simulation.") || fileHint.equals("_ejs_library_css")) { // FileHint must be in the file name
              if (!key.startsWith("_ejs_library/css")) {
                if (textFilename.indexOf(fileHint)<0 && textFilename.indexOf("common_script.js")<0) { // Files with _Simulation hint may be referenced in common_script.js 
                  //                System.err.println ("Ignoring reference "+key +" in  file "+ textFilename);
                  continue; // This reference is not for this file
                }
              }
//              System.err.println ("Checking reference "+key +" in  file "+ textFilename);
              modifiedContent = changesInContent (modifiedContent, key, twoStr.getSecondString(), changesInFile, CHANGE_TYPE.PLAIN_CHANGE);
            }
            else { // fileHint indicates the folder in which the file is located
//              System.err.println ("Checking reference "+key +" in  file "+ textFilename);
              modifiedContent = changesInContent (modifiedContent, key, twoStr.getSecondString(), changesInFile,CHANGE_TYPE.FULL_CHANGE);
              if (textFilename.indexOf(fileHint)>=0) {
                String checkKey = key.substring(fileHint.length());
//                System.err.println ("Checking reference "+checkKey +" in  file "+ textFilename);
                modifiedContent = changesInContent (modifiedContent, checkKey, twoStr.getSecondString(), changesInFile, CHANGE_TYPE.FULL_CHANGE);
              }
              else { // One more twist. Look for files as in FOLDER/subfolder/image.png referenced by a file FOLDER/file.xhtml as "subfolder/image.png".
                // This can be dangerous if the author used files of the form  subfolder/image.png in two different FOLDERs
                int firstIndex = fileHint.indexOf('_');
                int lastIndex = fileHint.lastIndexOf('_');
                if (firstIndex>=0 && lastIndex>firstIndex) {
                  String folderInImage = fileHint.substring(0,firstIndex+1);
                  firstIndex = textFilename.indexOf('_');
                  if (firstIndex>=0) {
                    String folderInFilename = textFilename.substring(firstIndex+1);
                    if (folderInFilename.startsWith(folderInImage)) {
                      String checkKey = key.substring(folderInImage.length());
//                    System.err.println ("Checking reference "+checkKey +" in  file "+ textFilename);
                      modifiedContent = changesInContent (modifiedContent, checkKey, twoStr.getSecondString(), changesInFile,CHANGE_TYPE.MIDDLE_CHANGE);
                    }
                  }
                }
              }
            }
          }
        }
        if (!modifiedContent.equals(content)) try {
          if (!textFile.getName().startsWith("_ejs_library_")) { // Do not report internal changes
            System.err.println ("The following references changed in file : "+textFile.getName());
            for (TwoStrings twoStr : changesInFile) {
              System.err.println (" - "+twoStr.getFirstString()+" ---> "+ twoStr.getSecondString());
            }
            System.err.println ("\n");
          }
          FileUtils.saveToFile(textFile, OsejsCommon.getUTF8(), modifiedContent);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          _ejs.getOutputArea().println("Error : Cannot change references in file : "+textFile.getName());
        }
      }
    } // End of strict folder cross-referencing

    createOpfFile(_ejs, zipFolder, title, author, manifestBuffer, spineBuffer);
    createNavigationFile(_ejs, zipFolder, title, author, tocList);
    createTOCFile(_ejs, zipFolder, title, author, tocList);

    //    { // save a README file
    //      StringBuffer buffer = new StringBuffer();
    //      Generate.addToReadMe(buffer,true);
    //      try {
    //        FileUtils.saveToFile (new File (zipFolder,"README.txt"), null, buffer.toString());
    //      } catch (IOException e) {
    //        e.printStackTrace();
    //      }
    //    }
    // Compress and remove working folder
    compress(_ejs,zipFolder, _targetFile);
    JarTool.remove(zipFolder);
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+_targetFile.getName());
  }

  static public boolean isMathMLFile (File file) {
    String content = FileUtils.readTextFile(file, OsejsCommon.getUTF8());
    if (content.indexOf("<math")>=0 && content.indexOf("</math>")>=0) return true;
    return false;
  }

  static public void combineCommonScript(File _sourceFile, File _targetFile, String _targetFilename, String _modelTitle) {
    String firstScript = FileUtils.readTextFile(_targetFile, XMLTransformerJava.UTF8_CHARSET);
    if (firstScript!=null) {
      StringBuffer combinedScriptBuffer = new StringBuffer();
      combinedScriptBuffer.append(firstScript);
      String secondScript = FileUtils.readTextFile(_sourceFile, XMLTransformerJava.UTF8_CHARSET);
      String keyword = XMLTransformerJava.sBASE64IMAGES+"[";
      StringTokenizer scriptTkn = new StringTokenizer (secondScript,"\n");
      while (scriptTkn.hasMoreTokens()) {
        String token = scriptTkn.nextToken();
        if (token.indexOf(keyword)>=0) { // It is base64 array line
          int index = token.indexOf("=");
          if (index>0) { // Has a "=" sign
            String entry = token.substring(0,index);
            if (firstScript.contains(entry)) { // Make sure we don't repeat entries
              System.err.println(res.getString("Package.EPUBIgnoringEntry")+ ": "+_targetFilename + " ("+_modelTitle+") : "+entry);
            }
            else combinedScriptBuffer.append(token+"\n");
          }
        }
      }
      try {
        FileUtils.saveToFile(_targetFile, XMLTransformerJava.UTF8_CHARSET, combinedScriptBuffer.toString());
        System.err.println("Merged common_script file: "+_targetFilename);
      } catch (IOException exc) {
        exc.printStackTrace();
      }
    }
  }

  // Create cover image and xhtml file
  static public void createCoverFile(Osejs _ejs, File _workingFolder, String _title, File _imageFile) {
    StringBuffer coverBuffer = new StringBuffer();
    coverBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    coverBuffer.append("<!DOCTYPE html>\n");
    coverBuffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    coverBuffer.append("  <head>\n");
    coverBuffer.append("    <meta charset=\"utf-8\" />\n");
    coverBuffer.append("    <meta name=\"generator\" content=\"EjsS\" />\n");
    coverBuffer.append("    <title>"+_title+"</title>\n");
    coverBuffer.append("    <link rel=\"stylesheet\" href=\"stylesheet.css\" />\n");
    coverBuffer.append("  </head>\n");
    coverBuffer.append("  <body>\n");
    coverBuffer.append("    <div id=\"cover-image\">\n");
    coverBuffer.append("      <img src=\"cover-image.png\" alt=\"cover image\" />\n");
    coverBuffer.append("    </div>\n");
    coverBuffer.append("  </body>\n");
    coverBuffer.append("</html>\n");
    try {
      FileUtils.saveToFile(new File(_workingFolder,"OEBPS/cover.xhtml"), OsejsCommon.getUTF8(), coverBuffer.toString());
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/cover.xhtml", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
    if (_imageFile!=null && !FileUtils.copy(_imageFile, new File(_workingFolder,"OEBPS/cover-image.png"))) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\n"+_imageFile, 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static private String changesInContent(String content, String oldKey, String newKey, HashSet<TwoStrings> changesInFile, CHANGE_TYPE type) {
    if (content.indexOf(oldKey)<0) return content; // No need to even try it!
    String newContent;
    if (type==CHANGE_TYPE.PLAIN_CHANGE) {
      newContent = FileUtils.replaceString (content,oldKey,newKey);
    }
    else {
      newContent = FileUtils.replaceString (content,    "\""       + oldKey + "\""  , "\""    + newKey + "\"");
      newContent = FileUtils.replaceString (newContent, "\\\""     + oldKey + "\\\"", "\\\""  + newKey + "\\\"");
      newContent = FileUtils.replaceString (newContent, "\"./"     + oldKey + "\""  , "\""    + newKey + "\"");
      newContent = FileUtils.replaceString (newContent, "\\\"./"   + oldKey + "\\\"", "\\\""  + newKey + "\\\"");
      newContent = FileUtils.replaceString (newContent, "'"        + oldKey + "'"   , "'"     + newKey + "'");
      newContent = FileUtils.replaceString (newContent, "'./"      + oldKey + "'"   , "'"     + newKey + "'");
      if (type==CHANGE_TYPE.FULL_CHANGE) {
        newContent = FileUtils.replaceString (newContent, "\"../"     + oldKey + "\""  , "\""    + newKey + "\"");
        newContent = FileUtils.replaceString (newContent, "\\\"../"   + oldKey + "\\\"", "\\\""  + newKey + "\\\"");
        newContent = FileUtils.replaceString (newContent, "'../"      + oldKey + "'"   , "'"     + newKey + "'");
      }
    }
    if (!newContent.equals(content)) changesInFile.add(new TwoStrings(oldKey ,newKey));
    return newContent;
  }

  static public void createTitleFile(Osejs _ejs, File _workingFolder, String _title, String _author) {
    File outputFile = new File (_workingFolder,"OEBPS/title_page.xhtml");
    File userFile = new File (_ejs.getConfigDirectory(),"javascript/OEBPS/title_page.xhtml");
    if (userFile.exists()) {
      if (JarTool.copy(userFile, outputFile)) return;
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<!DOCTYPE html>\n");
    buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta charset=\"utf-8\" />\n");
    buffer.append("    <meta name=\"generator\" content=\"EjsS\" />\n");
    buffer.append("    <title>"+_title+"</title>\n");
    buffer.append("    <link rel=\"stylesheet\" href=\"stylesheet.css\" />\n");
    buffer.append("  </head>\n");
    buffer.append("  <body>\n");
    buffer.append("      <h1 class=\"title\">"+_title+"</h1>\n");
    buffer.append("      <h2 class=\"author\">"+_author+"</h2>\n");
    buffer.append("      <h3 class=\"date\">"+OsejsCommon.getLongDate()+"<br />Created with EjsS</h3>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/title_page.xhtml", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  //  static private class FolderAndReference {
  //    File folder;
  //    String reference;
  //    
  //    public FolderAndReference(File aFolder, String aReference) {
  //      folder = aFolder;
  //      reference = aReference;
  //    }
  //  }


  static public void createCopyrightFile(Osejs _ejs, File _workingFolder, String _logoFilename) {
    File outputFile = new File (_workingFolder,"OEBPS/copyright_page.xhtml");
    File userFile = new File (_ejs.getConfigDirectory(),"javascript/OEBPS/copyright_page.xhtml");
    if (userFile.exists()) {
      if (JarTool.copy(userFile, outputFile)) return;
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<!DOCTYPE html>\n");
    buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta charset=\"utf-8\" />\n");
    buffer.append("    <meta name=\"generator\" content=\"EjsS\" />\n");
    buffer.append("    <title>Copyright and conditions of use</title>\n");
    buffer.append("    <link rel=\"stylesheet\" href=\"stylesheet.css\" />\n");
    buffer.append("  </head>\n");
    buffer.append("  <body>\n");
    buffer.append("      <h1 class=\"title\">Copyright and conditions of use</h1>\n");
    buffer.append("      <p>\n");
    buffer.append("      This ePub was created by its author using the Easy Java/Javascript Simulations modeling and authoring tool, ");
    buffer.append("      and has been released under a Creative Commons Attribution-NonCommercial-ShareAlike license. ");
    buffer.append("      <img src=\"CC_icon.png\" alt=\"Creative Commons Attribution-NonCommercial-ShareAlike\" />\n");
    buffer.append("      </p>\n");
    buffer.append("      <p>\n");
    buffer.append("      This material is free for non-commercial purposes and lets others remix, tweak, and build upon this work non-commercially,");
    buffer.append("      as long as they credit the author and license their new creations under the identical terms. ");
    buffer.append("      For other uses, permission must be obtained both from the ePub author and from the creators of the EjsS authoring tool and code library.\n");
    buffer.append("      </p>\n");
    //    buffer.append("        This ePub document, and the simulations included in it, were created using the <a href=\"http://www.um.es/fem/EjsWiki\">Easy Java/Javascript Simulations</a> (EjsS) modeling and authoring tool.\n");
    buffer.append("      <p>\n");
    buffer.append("        For more information on Easy Java/Javascript Simulations, please visit the <a href=\"http://www.um.es/fem/EjsWiki\">EjsS website</a>.\n");
    buffer.append("      </p>\n");
    buffer.append("      <div id=\"cover-image\">\n");
    buffer.append("        <img src=\""+_logoFilename+"\" alt=\"EjsS logo\" />\n");
    buffer.append("      </div>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/copyright_page.xhtml", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void createModelFile(Osejs _ejs, File _workingFolder, String _title, String _author, String _model) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<!DOCTYPE html>\n");
    buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta charset=\"utf-8\" />\n");
    buffer.append("    <meta name=\"generator\" content=\"EjsS\" />\n");
    buffer.append("    <title>"+_title+"</title>\n");
    buffer.append("    <link rel=\"stylesheet\" href=\"stylesheet.css\" />\n");
    buffer.append("  </head>\n");
    buffer.append("  <body>\n");
    buffer.append("    <div id=\""+_model+"\" class=\"section level1\">\n");
    buffer.append("      <h1>"+_title+"</h1>\n");
    buffer.append("      <h2 class=\"author\">"+_author+"</h2>\n");
    buffer.append("    </div>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    File outputFile = new File (_workingFolder,"OEBPS/"+_model+"_toc.xhtml");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/"+_model+"_toc.xhtml", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void createOpfFile(Osejs _ejs, File _workingFolder, String _title, String _author, 
      StringBuffer _manifestBuffer, StringBuffer _spineBuffer) {
    StringBuffer buffer = new StringBuffer();
    String today = OsejsCommon.getDate();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<package version=\"3.0\" xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookId\">\n");
    buffer.append("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n");
    buffer.append("  <dc:title>"+_title+"</dc:title>\n");
    buffer.append("  <dc:language>en-US</dc:language>\n");
    buffer.append("  <dc:identifier id=\"BookId\">EjsS "+_title+" "+today+"</dc:identifier>\n");
    buffer.append("  <dc:creator>"+_author+"</dc:creator>\n");
    buffer.append("  <dc:date>"+OsejsCommon.getLongDate()+"</dc:date>\n");
    buffer.append("  <meta property=\"dcterms:modified\">"+OsejsCommon.getLongDate()+"T"+OsejsCommon.getLongTime()+"Z</meta>\n");
    buffer.append("  <meta name=\"cover\" content=\"cover-image\" />\n");
    buffer.append("</metadata>\n");
    buffer.append("  <manifest>\n");
    buffer.append("    <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\" />\n");
    buffer.append("    <item id=\"style\" href=\"stylesheet.css\" media-type=\"text/css\" />\n");
    buffer.append("    <item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\" />\n");
    buffer.append("    <item id=\"cover\" href=\"cover.xhtml\" media-type=\"application/xhtml+xml\" />\n");
    buffer.append("    <item id=\"cc-icon\" href=\"CC_icon.png\" media-type=\"image/png\" />\n");
    buffer.append("    <item id=\"cover-image\" href=\"cover-image.png\" media-type=\"image/png\" />\n");
    buffer.append("    <item id=\"title_page\" href=\"title_page.xhtml\" media-type=\"application/xhtml+xml\" />\n");
    buffer.append("    <item id=\"copyright_page\" href=\"copyright_page.xhtml\" media-type=\"application/xhtml+xml\" />\n");
    buffer.append(_manifestBuffer);
    buffer.append("  </manifest>\n");
    buffer.append("  <spine toc=\"ncx\">\n");
    buffer.append("    <itemref idref=\"cover\" linear=\"no\" />\n");
    buffer.append("    <itemref idref=\"title_page\" linear=\"yes\" />\n");
    //    buffer.append("    <itemref idref=\"nav\" linear=\"no\" />\n"); Removed by Mario's suggestion: 
    // it creates search errors on the iPad (the iPad searches the nav as well as the book and crashes when selecting a search result related to the nav.
    buffer.append(_spineBuffer);
    buffer.append("    <itemref idref=\"copyright_page\" linear=\"yes\" />\n");
    buffer.append("  </spine>\n");
    buffer.append("</package>\n");
    File outputFile = new File (_workingFolder,"OEBPS/content.opf");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/content.opf", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void createNavigationFile(Osejs _ejs, File _workingFolder, String _title, String _author, ArrayList<TwoStrings> _tocList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <title>"+_title+"</title>\n");
    buffer.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
    buffer.append("  </head>\n");
    buffer.append("  <body>\n");
    buffer.append("    <nav epub:type=\"toc\">\n");
    buffer.append("      <h1>"+_title+"</h1>\n");
    buffer.append("      <ol class=\"toc\">\n");

    int counter = 0;
    boolean firstModel = true;
    for (TwoStrings ts : _tocList) {
      String entry = ts.getFirstString();
      if (entry.startsWith("model:")) {
        if (!firstModel) {
          buffer.append("          </ol>\n");
          buffer.append("        </li>\n");
        }
        firstModel = false;
        buffer.append("        <li id=\"toc-li-"+counter+"\">\n");
        buffer.append("          <a href=\""+ ts.getSecondString()+"\">\n");
        buffer.append("            <span>" + entry.substring(6)+"</span>\n");
        buffer.append("          </a>\n");
        buffer.append("          <ol class=\"toc\">\n");
      }
      else { // "page:xxx"
        buffer.append("            <li id=\"toc-li-"+counter+"\">\n");
        buffer.append("              <a href=\""+ ts.getSecondString()+"\">\n");
        buffer.append("                <span>" + entry.substring(5)+"</span>\n");
        buffer.append("              </a>\n");
        buffer.append("            </li>\n");
      }
      counter++;
    }
    if (!firstModel) {
      buffer.append("          </ol>\n");
      buffer.append("        </li>\n");
    }

    buffer.append("        <li id=\"toc-li-"+counter+"\">\n");
    buffer.append("          <a href=\"copyright_page.xhtml\">\n");
    buffer.append("            <span>Copyright and conditions of use</span>\n");
    buffer.append("          </a>\n");
    buffer.append("        </li>\n");

    buffer.append("      </ol>\n");
    buffer.append("    </nav>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");

    File outputFile = new File (_workingFolder,"OEBPS/nav.xhtml");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/nav.xhtml", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void createTOCFile(Osejs _ejs, File _workingFolder, String _title, String _author, ArrayList<TwoStrings> _tocList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    buffer.append("<ncx version=\"2005-1\" xmlns=\"http://www.daisy.org/z3986/2005/ncx/\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta name=\"dtb:uid\" content=\"EjsS "+ _title+" "+OsejsCommon.getDate() + "\" />\n");
    buffer.append("    <meta name=\"dtb:depth\" content=\"1\" />\n");
    buffer.append("    <meta name=\"dtb:totalPageCount\" content=\"0\" />\n");
    buffer.append("    <meta name=\"dtb:maxPageNumber\" content=\"0\" />\n");
    buffer.append("  </head>\n");
    buffer.append("  <docTitle>\n");
    buffer.append("    <text>"+_title+"</text>\n");
    buffer.append("  </docTitle>\n");
    buffer.append("  <navMap>\n");
    buffer.append("    <navPoint id=\"navPoint-0\">\n");
    buffer.append("      <navLabel>\n");
    buffer.append("        <text>"+_title+"</text>\n");
    buffer.append("      </navLabel>\n");
    buffer.append("      <content src=\"title_page.xhtml\" />\n");
    buffer.append("    </navPoint>\n");


    int counter = 1;
    boolean firstModel = true;
    for (TwoStrings ts : _tocList) {
      String entry = ts.getFirstString();
      if (entry.startsWith("model:")) {
        if (!firstModel) {
          buffer.append("    </navPoint>\n");
        }
        firstModel = false;
        buffer.append("    <navPoint id=\"navPoint-"+counter+"\" playOrder=\""+counter+"\">\n");
        buffer.append("      <navLabel>\n");
        buffer.append("        <text>" + entry.substring(6)+"</text>\n");
        buffer.append("      </navLabel>\n");
        buffer.append("      <content src=\""+ ts.getSecondString()+"\" />\n");
      }
      else { // "page:xxx"
        buffer.append("      <navPoint id=\"navPoint-"+counter+"\" playOrder=\""+counter+"\">\n");
        buffer.append("        <navLabel>\n");
        buffer.append("          <text>" + entry.substring(5)+"</text>\n");
        buffer.append("        </navLabel>\n");
        buffer.append("        <content src=\""+ ts.getSecondString()+"\" />\n");
        buffer.append("      </navPoint>\n");
      }
      counter++;
    }
    if (!firstModel) buffer.append("    </navPoint>\n");

    buffer.append("    <navPoint id=\"navPoint-"+counter+"\" playOrder=\""+counter+"\">\n");
    buffer.append("      <navLabel>\n");
    buffer.append("        <text>Copyright and conditions of use</text>\n");
    buffer.append("      </navLabel>\n");
    buffer.append("      <content src=\"copyright_page.xhtml\" />\n");
    buffer.append("    </navPoint>\n");

    buffer.append("  </navMap>\n");
    buffer.append("</ncx>\n");

    File outputFile = new File (_workingFolder,"OEBPS/toc.ncx");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nOEBPS/toc.ncx", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void removeMetadataDiv(File inputFile, File outputFile, Charset charset) {
    try {
      File newFile = File.createTempFile("EjsS", ".tmp");
      Writer writer = new OutputStreamWriter(new FileOutputStream(newFile),charset);

      Reader reader = new InputStreamReader(new FileInputStream(inputFile),charset);
      LineNumberReader l = new LineNumberReader(reader);

      int state = 0;
      int depth = 1;
      String sl = l.readLine();
      while (sl != null) {
        switch (state) {
          default : 
          case 0 : // not yet found 
            int begin = sl.indexOf("<div id=\"metadata\"");
            if (begin<0) writer.write(sl+"\n");
            else state = 1;
            break;
          case 1 : // found and inside it
            if (sl.indexOf("<div ")>=0)  depth++;
            if (sl.indexOf("</div>")>=0) depth--;
            if (depth<=0) state = 2;
            break;
          case 2 : // done
            writer.write(sl+"\n");
            break;
        }
        sl = l.readLine(); 
      }
      reader.close();
      writer.flush();
      writer.close();
      FileUtils.copy(newFile, outputFile);
      newFile.delete();
    }
    catch (Exception exc) {
      //      _ejs.getOutputArea().println("Error trying to remove copyright div from file "+FileUtils.getPath(inputFile));
      System.err.println("Error trying to remove copyright div from file "+FileUtils.getPath(inputFile));
      exc.printStackTrace();
    }
  }

  /**
   * This is a special compression of a folder, that adds - AS A FIRST FILE - an ePub mimetype file
   * @param source
   * @param target
   * @return
   */
  static private boolean compress(Osejs _ejs, File source, File target) {
    try {
      if(!(source.exists()&source.isDirectory())) {
        System.err.println("Source file does not exist!: "+source);
        return false;
      }
      if(target.exists()) target.delete();    // Remove the previous JAR file
      ZipOutputStream output = new ZipOutputStream(new FileOutputStream(target));
      // Get the list of files
      java.util.Collection<File> list = JarTool.getContents(source);
      String baseDir = source.getAbsolutePath().replace('\\', '/');
      if(!baseDir.endsWith("/")) baseDir = baseDir+"/";   //$NON-NLS-1$ $NON-NLS-2$
      writeMimeType(output);
      // Copy all other files
      int baseDirLength = baseDir.length();
      byte[] buffer = new byte[1024];                                  // Allocate a buffer for reading entry data.
      int bytesRead;
      // Create the compressed file
      for(File file : list) {
        InputStream f_in = new FileInputStream(file);
        // Read the entry and make it relative
        String filename = file.getAbsolutePath().replace('\\', '/');
        if(filename.startsWith(baseDir)) filename = filename.substring(baseDirLength);
        //        // Write the entry to the new compressed file.
        output.putNextEntry(new ZipEntry(filename));
        while((bytesRead = f_in.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        f_in.close();
        output.closeEntry();
      }
      output.close();
    } catch(Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  static private void writeMimeType(ZipOutputStream zip) throws IOException {
    byte[] content = "application/epub+zip".getBytes("UTF-8");
    ZipEntry entry = new ZipEntry("mimetype");
    entry.setMethod(ZipEntry.STORED);
    entry.setSize(20);
    entry.setCompressedSize(20);
    entry.setCrc(0x2CAB616F); // pre-computed
    zip.putNextEntry(entry);
    zip.write(content);
    zip.closeEntry();
  }


  //  static private void saveAsXHTML (InputStream input, OutputStream output) throws Exception {
  //    org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
  //    tidy.setXHTML(true);
  //    tidy.setForceOutput(true);
  //    tidy.setQuiet(true);
  //    tidy.parseDOM(input,output);
  //  }

  static private void saveAsXHTML (File inputFile, File outputFile, Charset charset) throws Exception {
    StringBuffer buffer = new StringBuffer();
    String content = FileUtils.readTextFile(inputFile, charset);
    int begin = content.indexOf("<html");
    if (begin>=0) {
      int end = content.indexOf(">", begin);
      if (end>0) {
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        buffer.append("<!DOCTYPE html>\n");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
        buffer.append(content.substring(end+1));
        FileUtils.saveToFile(outputFile, charset, buffer.toString());
        return;
      }
    }
  }

}
