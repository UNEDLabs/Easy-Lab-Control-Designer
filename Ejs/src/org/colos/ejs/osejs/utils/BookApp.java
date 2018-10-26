package org.colos.ejs.osejs.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejss.xml.JSObfuscator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class BookApp {
  static private final ResourceUtil res = new ResourceUtil("Resources");
  
  static private class TocEntry {
    static private enum TYPE { CHAPTER, SECTION };
    
    String title, url, description;
    TYPE type;
    ArrayList<TocEntry> sections;
    
    private TocEntry() {};
    
    static TocEntry createChapter(String _title, String _desc) {
      TocEntry entry = new TocEntry();
      entry.title = _title;
      entry.type = TYPE.CHAPTER;
      entry.description = _desc;
      entry.sections = new ArrayList<TocEntry>();
      return entry;
    }

    static TocEntry createSection(String _title, String _url) {
      TocEntry entry = new TocEntry();
      entry.title = _title;
      entry.type = TYPE.SECTION;
      entry.url = _url;
      return entry;
    }
    
    public boolean addSection(TocEntry _section) {
      if (type == TYPE.CHAPTER) {
        sections.add(_section);
        return true;
      }
      return false;
    }

  };

  static public class RefEntry {
    int id;
    String title;
    ArrayList<TwoStrings> references = new ArrayList<TwoStrings>();
    
    public RefEntry(int _id, String _title) {
      id = _id;
      title = _title;
    };
    
    public void addReferences(ArrayList<TwoStrings> _refs) {
      references.addAll(_refs);
    }

  };

  static public boolean hasReferences (ArrayList<RefEntry> refList) {
    for (RefEntry ref : refList) {
      if (!ref.references.isEmpty()) return true;
    }
    return false;
  }
  
  
  /**
   * Compresses a list of simulations in a single ZIP file
   * @param _ejs
   * @param _listInfo 
   * @param _targetFile
   */
  static public void packageSeveralXMLSimulations(Osejs _ejs, CreateListDialog.ListInformation _listInfo, File _targetFile) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());

    // Create a working temporary directory
    File zipFolder=null;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
      new File(zipFolder,"css").mkdirs();
      new File(zipFolder,"js").mkdirs();
    }
    catch (Exception exc) {  
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      JarTool.remove(zipFolder);
      return;
    }
    
    //Algo había aquí que había que poner filenam EqualsIgnoreCase.._ejs.

    ArrayList<String> ignoreFilesWithName = new ArrayList<String>();
//    ignoreFilesWithSuffix.add("_ejs_library/html/EjsLauncher.html");
//    ignoreFilesWithSuffix.add("_ejs_library/css/ejsGroupPage.css");
//    ignoreFilesWithSuffix.add("_ejs_library/css/ejsContentsLeft.css");
//    ignoreFilesWithSuffix.add("_ejs_library/css/ejsContentsTop.css");
//    ignoreFilesWithSuffix.add("_ejs_library/images/EjsMainIcon.gif");
//    ignoreFilesWithSuffix.add("_ejs_library/images/EjsLogo.gif");
//    ignoreFilesWithSuffix.add("_ejs_library/images/EjsIcon.gif");
//    ignoreFilesWithSuffix.add("_ejs_library/images/cc_icon.png");
    ignoreFilesWithName.add(".DS_Store");
    ignoreFilesWithName.add("_metadata.txt");
    ignoreFilesWithName.add("_ejs_README.txt");
//    for (TwoStrings ts : specialFiles) ignoreFilesWithSuffix.add("_ejs_library/"+ts.getFirstString());

    { // Copy standard BookApp files
      String [] copyList = { 
          "img/Gyroscope.png",  "img/ReaderFree.png", "img/ReaderInterface.png", "img/ReaderPro.png", 
          "css/style.css",  "js/app.js", "js/common_script.js", "index.html" 
          };

      File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/BOOK_APP");
      for (String filename : copyList) 
        if (!FileUtils.copy(new File (javascriptDir,filename), new File(zipFolder,filename))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+filename);
    }
    
    { // Copy CSS files
      File userLibraryCSS = new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
      if (userLibraryCSS.exists()) {
        if (!FileUtils.copy(userLibraryCSS, new File(zipFolder,"css/ejss.css"))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+userLibraryCSS.getAbsolutePath());             
      }
      else {
        File libraryCSS = new File(_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/"+OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
        if (libraryCSS.exists()) {
          if (!FileUtils.copy(libraryCSS, new File(zipFolder,"css/ejss.css"))) 
            System.err.println(res.getString("Osejs.File.SavingError")+"\n"+libraryCSS.getAbsolutePath());             
        } 
      }
    }
    { // Copy javascript files
      ArrayList<TwoStrings> jsFiles = new ArrayList<TwoStrings>();
      jsFiles.add(new TwoStrings("scripts/textresizedetector.js","js/textresizedetector.js")); 
      jsFiles.add(new TwoStrings(JSObfuscator.LIB_MIN_FILENAME,  "js/"+JSObfuscator.LIB_MIN_FILENAME));

      File javascriptLibDir = new File(_ejs.getBinDirectory(),"javascript/lib");
      for (TwoStrings ts : jsFiles) {
        File sourceFile = new File (javascriptLibDir,ts.getFirstString());
        if (!FileUtils.copy(sourceFile, new File(zipFolder,ts.getSecondString()))) 
          System.err.println(res.getString("Osejs.File.SavingError")+"\n"+sourceFile.getAbsolutePath());
      }
    }
    
    boolean ignoreHTML = !_listInfo.getIncludeHTMLFiles();
    boolean separateChapters = _listInfo.getSeparateChapters();

    File sectionsFolder = new File(zipFolder,"sections");
    sectionsFolder.mkdirs();
    
    ArrayList<TwoStrings> relocateFiles = new ArrayList<TwoStrings>();
    relocateFiles.add(new TwoStrings("_ejs_library/images/Gyroscope.gif",         "../img/Gyroscope.png")); 
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejss.css",                 "../css/ejss.css"));
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejsPage.css",              "../css/ejss.css"));
    relocateFiles.add(new TwoStrings("_ejs_library/css/ejsSimulation.css",        "../css/ejss.css")); 
    relocateFiles.add(new TwoStrings("_ejs_library/scripts/textresizedetector.js",   "../js/textresizedetector.js")); 
    relocateFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT,    "../js/common_script.js"));
    relocateFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.LIB_MIN_FILENAME,  "../js/"+JSObfuscator.LIB_MIN_FILENAME));

    // Prepare buffers for the processing
    ArrayList<TocEntry> tocList = new ArrayList<TocEntry>();
//    HashSet<String> repeatedFiles = new HashSet<String>();
    ArrayList<RefEntry> refList = new ArrayList<RefEntry>();
    int refCounter = 0;

    java.util.List<PathAndFile> listOfZipFiles = _listInfo.getList();

    // Uncompress all zip files into the zip directory
    for (int modelCounter=1,n=listOfZipFiles.size(); modelCounter<=n; modelCounter++) {
      PathAndFile paf = listOfZipFiles.get(modelCounter-1);
      String modelName = "_model_"+modelCounter;
      File modelFolder = new File(sectionsFolder,modelName);
      modelFolder.mkdirs();
      
      JarTool.unzip(paf.getFile(), modelFolder);
      File metadataFile = new File(modelFolder,"_metadata.txt");
      if (!metadataFile.exists()) { // This is not an ejss_model file
        _ejs.getOutputArea().println("Warning: ZIP file "+paf.getPath()+" does not contain an EJS Javascript model. Ignored!");
        JarTool.remove(modelFolder);
        continue;
      }

      // process metadata file
      String modelTitle = null;
      String modelAuthor ="";
      String lastPageTitle=null;
      String htmlMain=null;
      HashSet<String> modelPagesSet = new HashSet<String>();
      ArrayList<TwoStrings> modelPages = new ArrayList<TwoStrings>();
      
      StringTokenizer tkn = new StringTokenizer(FileUtils.readTextFile(metadataFile, null),"\n");
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
          boolean isSimulation = (pageIndex.endsWith("_Simulation.html") || pageIndex.endsWith("_Simulation.xhtml"));
          File pageFile = new File(modelFolder,pageIndex);
          String targetFilename = isSimulation ? modelName+"_"+ pageIndex : pageIndex;
          if (isSimulation || !ignoreHTML) {
            modelPages.add(new TwoStrings(lastPageTitle, targetFilename));
            modelPagesSet.add(pageIndex);
            RefEntry refEntry = new RefEntry(refCounter,lastPageTitle);
            refList.add(refEntry);
            if (isSimulation) { // process the file and see if there is a js file
              TwoStrings ts = FileUtils.getPlainNameAndExtension(pageFile);
              File jsFile = new File(modelFolder,ts.getFirstString()+".js");
              if (jsFile.exists()) BookApp.processReferences(refEntry, jsFile,refCounter);
              else BookApp.processReferences(refEntry, pageFile,refCounter);
            }
            else BookApp.processReferences(refEntry, pageFile,refCounter);
            BookApp.relocateFiles(pageFile,isSimulation,relocateFiles);
            refCounter++;
          }
        } // Metadata processed
      } // end of processing metadata
      
      // Process each extracted directory
      String modelDirPath = FileUtils.getPath(modelFolder); 

      String baseDirPath = FileUtils.getPath(modelFolder);
      String commonScriptFilename = "_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT;

      for (File file : JarTool.getContents(modelFolder)) {
        String filename = FileUtils.getRelativePath(file, baseDirPath, false);
        if (filename.startsWith("/")) filename = filename.substring(1);
        boolean mustDelete = false;
        String extension = FileUtils.getPlainNameAndExtension(file).getSecondString().toLowerCase();

        // Delete EjsS generated files that are not needed in the BookApp 
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
        else if (filename.endsWith(commonScriptFilename)) { // replace common script with the common script for bookapps
          EPub.combineCommonScript(file, new File(zipFolder,"js/common_script.js"), filename, modelTitle);
          mustDelete = true;
        }
        else if (filename.equals("_ejs_library/css/ejss.css")) { // replace system CSS with user defined CSS
          if (!FileUtils.copy(file, new File(zipFolder,"css/ejss.css"))) 
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

        String targetFilename;
        String originalName = FileUtils.getRelativePath(file, modelDirPath, false);
        if (originalName.endsWith("_Simulation.xhtml")) targetFilename = modelName+"_"+ originalName;
        else targetFilename = originalName;
        File targetFile = new File(sectionsFolder,targetFilename);
        if (targetFile.exists()) { // repeated, ignore
          String removePrefix = filename.substring(filename.indexOf('/')+1);
          if (!removePrefix.startsWith("_ejs_library")) {
            System.err.println(res.getString("Package.EPUBIgnoringFile")+ ": "+targetFilename + " ("+modelTitle+")");
            //              repeatedFiles.add(targetFilename);
          }
          continue;
        }
        else JarTool.copy(file,targetFile);

      } // end processing model folder contents

      JarTool.remove(modelFolder);

      // Add main section entry
      TocEntry modelTocEntry=null;
      if (separateChapters) {
        switch(_listInfo.getNumberingOption()) {
          default : 
          case CreateListDialog.NUMBERING_OPTION_NONE : 
            modelTocEntry = TocEntry.createChapter(modelTitle,res.getString("SimInfoEditor.Author")+" "+modelAuthor);
            break;
          case CreateListDialog.NUMBERING_OPTION_CHAPTER : 
          case CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION : 
            modelTocEntry = TocEntry.createChapter(modelCounter+" ."+modelTitle,res.getString("SimInfoEditor.Author")+" "+modelAuthor);
            break;
        }
        tocList.add(modelTocEntry);
      }
      // Add section entries
      for (int page=1,nPages=modelPages.size(); page<=nPages; page++) {
        TwoStrings entry = modelPages.get(page-1);
        String textForEntry;
        switch(_listInfo.getNumberingOption()) {
          default : 
          case CreateListDialog.NUMBERING_OPTION_NONE : 
          case CreateListDialog.NUMBERING_OPTION_CHAPTER : 
            textForEntry = entry.getFirstString();
            break;
          case CreateListDialog.NUMBERING_OPTION_CHAPTER_AND_SECTION : 
            textForEntry = modelCounter+"."+page+" "+entry.getFirstString();
            break;
        }
        if (modelTocEntry==null) tocList.add(TocEntry.createSection(textForEntry,entry.getSecondString()));
        else modelTocEntry.addSection(TocEntry.createSection(textForEntry,entry.getSecondString()));
      }
      
    } // end of for loop for each model

   // Create main files
    
    String title = _listInfo.getName().trim();
    if (title.length()<=0) title = "Untitled";
    String author = _listInfo.getSubtitle();
    if (author.length()<=0) author = "Uknown author";
    String about = _listInfo.getAbout();
    
    createBookStructureFile(_ejs, zipFolder, title, author, tocList);
    createAboutFile(_ejs, zipFolder, title, author, about);
    createReferencesFile(_ejs, zipFolder, refList);
    
    // Compress and remove working folder
    JarTool.compress(zipFolder, _targetFile,null);
    JarTool.remove(zipFolder);
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+_targetFile.getName());
  }

  
//  static private void replaceJSandCSSLinks(File _file, boolean _isSimulation, ArrayList<TwoStrings> _commonFiles) {
//    if (!_file.exists()) {
//      System.err.println("Content file does not exist: "+_file.getName());
//      return;
//    }
//    String content = FileUtils.readTextFile(_file, null);
//    String newContent = content;
//    
//    for (TwoStrings ts : _commonFiles) {
//      newContent = FileUtils.replaceString (newContent,ts.getFirstString(),ts.getSecondString());
//    }
//    
//    if (newContent.equals(content)) return;
//    try {
//      FileUtils.saveToFile(_file, null, newContent);
//    } catch (IOException e) {
//      System.err.println("Warning: Error trying to save corrected Anchor tags for file : "+_file.getName());
//      e.printStackTrace();
//    }
//  }
  
    public static void main(String[] args) throws IOException {
      java.util.Collection<File> files = JarTool.getContents(new File("/Users/Paco/UMUbox/Documents/Programming/MyEjsWorkspace/export/ejss_model_bookapp/sections"));
      ArrayList<TwoStrings> specialFiles = new ArrayList<TwoStrings>();
      specialFiles.add(new TwoStrings("_ejs_library/images/Gyroscope.gif",         "../img/Gyroscope.png")); 
      specialFiles.add(new TwoStrings("_ejs_library/css/ejss.css",                 "../css/ejss.css"));
      specialFiles.add(new TwoStrings("_ejs_library/css/ejsPage.css",              "../css/ejss.css"));
      specialFiles.add(new TwoStrings("_ejs_library/css/ejsSimulation.css",        "../css/ejss.css")); 
      specialFiles.add(new TwoStrings("_ejs_library/other_pages/copyright.html",      "../other_pages/copyright.html")); 
      specialFiles.add(new TwoStrings("_ejs_library/other_pages/cover.html",          "../other_pages/cover.html")); 
      specialFiles.add(new TwoStrings("_ejs_library/other_pages/title_page.html",     "../other_pages/title_page.html")); 
      specialFiles.add(new TwoStrings("_ejs_library/scripts/textresizedetector.js",   "../js/textresizedetector.js")); 
      specialFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT,    "../js/common_script.js"));
      specialFiles.add(new TwoStrings("_ejs_library/"+JSObfuscator.LIB_MIN_FILENAME,  "../js/"+JSObfuscator.LIB_MIN_FILENAME));
      for (File file : files) {
        if (file.getName().endsWith("html")) relocateFiles(file,true,specialFiles);
      }
    }

    static private boolean changeProperty(Element element, String property, ArrayList<TwoStrings> commonFiles) {
      String value = element.attr(property);
      for (TwoStrings ts : commonFiles) {
        if (ts.getFirstString().equals(value)) {
          element.attr(property,ts.getSecondString());
          return true;
        }
      }
      return false;
    }
    
    
  /**
   * Relocate files references
   * @param _refEntry
   * @param _file
   * @param _counterRef
   */
  static public void relocateFiles(File file, boolean isSimulation, ArrayList<TwoStrings> commonFiles) {
    System.out.println("Must process CSS and JS for file: "+file.getName());
    try {
      Document doc = Jsoup.parse(FileUtils.readTextFile(file, null));
      Element head = doc.head();
      Elements imports = head.select("link[href]");
      boolean changed = false;
      for (Element link : imports) {
        if ("stylesheet".equalsIgnoreCase(link.attr("rel")) || "text/css".equalsIgnoreCase(link.attr("type")) ) {
          if (changeProperty(link,"href",commonFiles)) changed = true;
          else {
            String href = link.attr("href");
            if (href.equals("ejss.css") || href.endsWith("/ejss.css")) {
              link.attr("href","../css/ejss.css");
              changed = true;
            }
          }
        }
      }
      Elements scripts = head.select("script[src]");
      for (Element script : scripts) {
        if (changeProperty(script,"src",commonFiles)) {
          script.attr("type","text/javascript");
          changed = true; 
        }
      }
      if (isSimulation) {
        Element metadata = doc.body().getElementById("metadata");
        if (metadata!=null) {
          metadata.remove();
          changed = true;
        }
      }
      if (changed) {
//        String content = FileUtils.readTextFile(file, null);
//        int index = content.indexOf("<head");
//        if (index>0) {
//          int endIndex = content.indexOf("</head",index);
//          if (endIndex>index) {
//            StringBuffer buffer = new StringBuffer();
//            buffer.append(content.substring(0, index));
//            buffer.append(head.outerHtml());
//            buffer.append(content.substring(endIndex));
//            FileUtils.saveToFile(new File(file.getParent(),filename), null, buffer.toString());
//            return;
//          }
//        }
//        doc.outputSettings().charset("UTF-8");
//        doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml); //This will ensure the validity

//        TwoStrings ts = FileUtils.getPlainNameAndExtension(file);
//        String filename = ts.getFirstString()+"_copy."+ts.getSecondString();
//        FileUtils.saveToFile(new File(file.getParent(),filename), null, doc.toString());
//        System.out.println("File saved "+filename);
        FileUtils.saveToFile(file, null, doc.toString());
        
      }
      
    } catch (Exception e) {
      System.err.println("Warning: Error trying to modify CSS and JS links for file : "+file.getName());
      e.printStackTrace();
    }
  }
  /**
   * Finds all Anchor tags and adds them to a RefEntry list
   * @param _refEntry
   * @param _file
   * @param _counterRef
   */
  static public void processReferences(RefEntry _refEntry, File _file, int _counterRef) {
//    System.out.println("Must process references for file: "+_file.getName());
    if (!_file.exists()) {
      System.err.println("References file does not exist: "+_file.getName());
      return;
    }
    String anchorTag = "<a";
    int tagLength = anchorTag.length();
    StringBuffer buffer = new StringBuffer ();
    ArrayList<TwoStrings> references = new ArrayList<TwoStrings>();

    String source = FileUtils.readTextFile(_file, null);
    int start = source.indexOf(anchorTag);
    boolean changed = false;
    while (start>=0) {
      buffer.append(source.substring(0,start));
      source = source.substring(start);
      buffer.append(anchorTag);

      char nextChar = source.charAt(tagLength); 
      if (nextChar==' ' || nextChar=='\t' || nextChar=='\n') {
        int end = findAnchorEnd(source);
        if (end<0) { // This is actually a syntax error
          System.err.println("Warning: Anchor syntax not ended with </a> in file : "+_file.getName());
          return;
        }
        if (replaceAnchorLink(references,buffer,source.substring(2,end),_counterRef,_file.getName())) changed = true;
        source = source.substring(end);
      }
      else { // This seems to be another <a tag, such as <annotation
        System.err.println("Warning: <a is not an anchor tag in file : "+_file.getName());
        source = source.substring(tagLength);
      }
      start = source.indexOf(anchorTag);
    }
    if (!changed) return;
    buffer.append(source);
    try {
      FileUtils.saveToFile(_file, null, buffer.toString());
      _refEntry.addReferences(references);
//      System.err.println("REFERENCES changed in file : "+_file.getName());
    } catch (IOException e) {
      System.err.println("Warning: Error trying to save corrected Anchor tags for file : "+_file.getName());
      e.printStackTrace();
    }
  }

  static private boolean replaceAnchorLink(ArrayList<TwoStrings> references, StringBuffer buffer, String notEndedAnchorText, int _refCounter, String _filename) {
    String hRef = "href";
    int index = notEndedAnchorText.indexOf(hRef);
    if (index>=0) {
      String prev = notEndedAnchorText.substring(0,index);
      String rest = notEndedAnchorText.substring(index+4).trim();
      if (rest.startsWith("=") && rest.length()>2) {
        rest = rest.substring(1).trim();
        String delim;
        if (rest.startsWith("\\\"")) {
          delim = "\\\"";
          rest = rest.substring(2);
        }
        else {
          delim = rest.substring(0, 1);
          rest = rest.substring(1);
        }
        int end = rest.indexOf(delim);
        if (end>=0) { // found correctly
          String url = rest.substring(0,end);
          rest = rest.substring(end+delim.length());
          index = rest.lastIndexOf('>');
          if (index>=0) { // Find title
            buffer.append(prev);
            buffer.append("href="+delim+delim+" onclick="+delim+"parent.selectRefs("+_refCounter+")"+delim);
            buffer.append(rest);
            String title = rest.substring(index+1);
            references.add(new TwoStrings(title,url));
            System.err.println ("REFERENCE: "+title+" for "+url+" processed ok in file "+_filename);
            return true;
          }
        }
      }
    }
    buffer.append(notEndedAnchorText);
    return false;
  }

  static private int findAnchorEnd(String source) {
    int index = source.indexOf("</");
    try {
      while (index>=0) {
        String rest = source.substring(index+2).trim();
        StringTokenizer tkn = new StringTokenizer(rest," \t");
        String firstToken = tkn.nextToken(); 
        if (firstToken.startsWith("a>")) return index;
        if (firstToken=="a" && tkn.nextToken().startsWith(">")) return index;
        index = source.indexOf(source, index+2);
      }
    } catch(Exception exc) { }
    return -1;
  }
  
  
  static private void createAboutFile(Osejs _ejs, File _workingFolder, String _title, String _author, String _about) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<ion-view>\n");
    buffer.append("  <ion-content class=\"padding\">\n");
    if (_about.length()>0) {
      buffer.append(_about);
    }
    else {
      buffer.append("    <h1 class=\"title\">"+res.getString("Package.About")+"</h1>\n");
      buffer.append("    <p>\n");
      buffer.append("      Name: "+_title+",\n");
      buffer.append("      Author: "+_author+",\n");
      buffer.append("   </p>\n");
    }
    buffer.append("  </ion-content>\n");
    buffer.append("</ion-view>\n");

    File outputFile = new File (_workingFolder,"other_pages/about.html");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nother_pages/about.html", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  static public void createReferencesFile(Osejs _ejs, File _workingFolder, ArrayList<RefEntry> _references) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<ion-nav-view delegate-handle=\"ref_list\">\n");
    buffer.append("  <ion-content on-scroll=\"scrollEvent()\" delegate-handle=\"scrollHandle\" scroll=\"true\" overflow-scroll=\"true\" class=\"padding\">\n");
    buffer.append("    <h1>"+res.getString("Package.References")+"</h1>\n");
    
    for (RefEntry section : _references) {
      if (section.references.isEmpty()) continue; // No references for this section
      buffer.append("    <hr />\n");
      buffer.append("    <section id=\""+section.id+"\">\n");
      buffer.append("      <h3 style=\"margin-top:.7em\">"+section.title+"\n");
      buffer.append("      <a class=\"button button-small icon ion-pin ion-light\" ng-click=\"selectPage("+section.id+")\"></a>\n");
      buffer.append("        </h3>\n");
      buffer.append("        <ul>\n");
      for (TwoStrings reference : section.references) {
        buffer.append("          <li>\n");
        buffer.append("            <a style=\"padding-left: 15px;\" href=\"#\"\n");
        buffer.append("               onclick=\"window.open('"+reference.getSecondString()+"','_system', 'location=yes'); return false;\">"+reference.getFirstString()+"</a>\n");
        buffer.append("          </li>\n");
      }
      buffer.append("        </ul>\n");
      buffer.append("    </section>\n");
    }

    buffer.append("    <hr />\n");
    buffer.append("    <section id=\"EjsS\">\n");
    buffer.append("      <h3 style=\"margin-top:.7em\">Easy Java/JavaScript Simulations</h3>\n");
    buffer.append("      <p>\n");
    buffer.append("        The simulations in this App were created using the Easy Java/JavaScript Simulations (EjsS) modeling and authoring tool.\n");
    buffer.append("        For information on how to use this tool to create your own simulations, visit the\n");
    addLinkToBuffer(buffer,"http://www.um.es/fem/EjsWiki","EjsS Website");
    buffer.append("        <figure align=\"center\" >\n");
    buffer.append("          <img src=\"./img/Gyroscope.png\" height=\"100px\" alt=\"EjsS logo\" />\n");
    buffer.append("          <figcaption>Easy Java/JavaScript Simulations logo.</figcaption>\n");
    buffer.append("        </figure>\n");
    buffer.append("      </p>\n");
    buffer.append("      <p>\n");
    buffer.append("        Additional JavaScript simulations can be downloaded from the AAPT\n");
    addLinkToBuffer(buffer,"http://www.compadre.org/osp","ComPADRE National Science Digital Library");
    buffer.append("        to your mobile device by downloading the\n");
    addLinkToBuffer(buffer,"http://www.um.es/fem/EjsWiki/Main/ReaderApp","EjsS Reader App");
    buffer.append("        from the Apple's iTunes or Google's Play stores.\n");
    buffer.append("        The EjsS Reader App lets you organize and run, in your tablet or smartphone, Javascript simulations created with\n");
    buffer.append("        EjsS 5.0 and later.\n");
    buffer.append("        <figure align=\"center\">\n");
    buffer.append("          <img src=\"./img/ReaderInterface.png\" width=\"90%\" alt=\"EjsS Reader\" />\n");
    buffer.append("          <figcaption>Main page of the Android version of the EjsS Reader App. Clicking one of the simulations logos, the Reader runs that simulation.</figcaption>\n");
    buffer.append("        </figure>\n");
    buffer.append("      </p>\n");
    buffer.append("      <p>\n");
    buffer.append("        There is a Free version of the Reader and a Pro version of it. Both versions come with a number of demo simulations\n");
    buffer.append("        in what will be your personal library. The Free version allows you to add up to five (5) more simulations to your\n");
    buffer.append("        library. Once you reach this limit, you need to remove one of these downloaded simulations to make room for a new one.\n");
    buffer.append("        The Pro version has no limit in the number of simulations you can add to your personal library.\n");
    buffer.append("      </p>\n");
    buffer.append("      <ion-list>\n");
    buffer.append("        <ion-item ng-if=\"isIOS\">\n");
    buffer.append("          <img src=\"./img/ReaderFree.png\" width=\"60px\" alt=\"EjsS Reader Free\" />\n");
    addLinkToBuffer(buffer,"https://itunes.apple.com/es/app/ejss-reader-free/id870091579","EjsS Reader App Free");
    buffer.append("          on Apple's iTunes Store\n");
    buffer.append("        </ion-item>\n");
    buffer.append("        <ion-item ng-if=\"isIOS\">\n");
    buffer.append("          <img src=\"./img/ReaderPro.png\" width=\"60px\" alt=\"EjsS Reader Pro\" />\n");
    addLinkToBuffer(buffer,"https://itunes.apple.com/es/app/ejss-reader/id870091262","EjsS Reader App Pro");
    buffer.append("          on Apple's iTunes Store\n");
    buffer.append("        </ion-item>\n");
    buffer.append("        <ion-item ng-if=\"isAndroid\">\n");
    buffer.append("          <img src=\"./img/ReaderFree.png\" width=\"60px\" alt=\"EjsS Reader Free\" />\n");
    addLinkToBuffer(buffer,"https://play.google.com/store/apps/details?id=org.colos.ejssreader.free","EjsS Reader App Free");
    buffer.append("          on Google's Play Store\n");
    buffer.append("        </ion-item>\n");
    buffer.append("        <ion-item ng-if=\"isAndroid\">\n");
    buffer.append("          <img src=\"./img/ReaderPro.png\" width=\"60px\" alt=\"EjsS Reader Pro\" />\n");
    addLinkToBuffer(buffer,"https://play.google.com/store/apps/details?id=org.colos.ejssreader.pro","EjsS Reader App Pro");
    buffer.append("           on Google's Play Store\n");
    buffer.append("        </ion-item>\n");
    buffer.append("      </ion-list>\n");
    buffer.append("    </section>\n");

    buffer.append("  </ion-content>\n");
    buffer.append("</ion-nav-view>\n");
    

    File outputFile = new File (_workingFolder,"other_pages/references.html");
    try {
      FileUtils.saveToFile(outputFile, OsejsCommon.getUTF8(), buffer.toString());
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Osejs.File.SavingError")+"\nother_pages/references.html", 
          res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }
  
  static private void addLinkToBuffer (StringBuffer buffer, String _url, String _text) {
    buffer.append("          <a href=\"#\" onclick=\"window.open('"+_url+"','_system', 'location=yes'); return false;\">");
    buffer.append(_text+"</a>\n");
  }
  
  static public void createBookStructureFile(Osejs _ejs, File _workingFolder, String _title, String _author, ArrayList<TocEntry> _tocList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("var book_title = \""+_title+"\";\n");
    buffer.append("var book_author = \""+_author+"\";\n\n");
    
    buffer.append("var book_menu_title = \""+res.getString("Package.Contents")+"\";\n");
    buffer.append("var book_toc_title = \""+res.getString("Package.TableOfContents")+"\";\n");
    buffer.append("var book_about_title = \""+res.getString("Package.About")+"\";\n");
    buffer.append("var book_references_title = \""+res.getString("Package.References")+"\";\n\n");

    buffer.append("var book_cover_title = \"Cover\";\n");
    buffer.append("var book_title_page_title = \"Title Page\";\n");
    buffer.append("var book_copyright_title = \"Copyright\";\n\n");

    buffer.append("var book_toc = [");
    int counter = 1;
    for (TocEntry entry : _tocList) {
      if (counter==1) buffer.append("\n");
      else buffer.append(",\n");
      buffer.append("  {\n");
      buffer.append("    title: \""+entry.title+"\",\n");
      if (entry.url!=null)         buffer.append("    url:\"sections/"+entry.url+"\",\n");
      if (entry.description!=null) buffer.append("    description:\""+entry.description+"\",\n");
      if (entry.type==TocEntry.TYPE.CHAPTER) { // Chapter
        buffer.append("    type: \"chapter\",\n");
        buffer.append("    sections : [");
        int sectionCounter = 1;
        for (TocEntry section : entry.sections) {
          if (sectionCounter==1) buffer.append("\n");
          else buffer.append(",\n");
          buffer.append("      {\n");
          buffer.append("        title: \""+section.title+"\",\n");
          if (section.url!=null)         buffer.append("        url: \"sections/"+section.url+"\",\n");
          if (section.description!=null) buffer.append("        description:\""+section.description+"\",\n");
          buffer.append("        type: \"section\"\n");
          buffer.append("      }\n");
          sectionCounter++;
        }
        buffer.append("    ]");
      }
      else {
        buffer.append("    type: \"section\"\n");
      }
      buffer.append("\n  }");
      counter++;
    }
    buffer.append("\n  ];\n");

    File outputFile = new File (_workingFolder,"js/book.js");
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
