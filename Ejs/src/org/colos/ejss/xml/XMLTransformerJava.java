package org.colos.ejss.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
//import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.Generate;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.utils.MimeInfo;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejss.xml.SimulationXML.INFORMATION;
import org.colos.ejss.xml.SimulationXML.MODEL;
import org.opensourcephysics.tools.Resource;
import org.w3c.dom.Element;

import com.myjeeva.image.ImageManipulation;

public class XMLTransformerJava {  
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static public final String sEJSS_PREFIX = "EJsS";
  static public final Charset UTF8_CHARSET = java.nio.charset.Charset.forName("UTF-8"); //$NON-NLS-1$
  static public final String sBASE64IMAGES = "__base64Images";
  static private final String sDUMMYSCRIPT  = "dummy.js";

  
//  static public java.util.List<String> getCssFilenameListNO (String cssFilename, SimulationXML simulation) {
//    java.util.List<String> cssFilenameList = new java.util.ArrayList<String>();
//    if (cssFilename!=null) cssFilenameList.add(cssFilename);
//    String extraCss = simulation.getInformation(INFORMATION.EXTRA_CSS_FILES);
//    if (extraCss!=null && extraCss.trim().length()>0) {
//      StringTokenizer tkn = new StringTokenizer(extraCss,";");
//      while (tkn.hasMoreTokens()) {
//        cssFilenameList.add(tkn.nextToken());
//      }
//    }
//    return cssFilenameList;
//  }

  static public java.util.List<String> getCssFilenameList (String cssFilename, SimulationXML simulation) {
    java.util.List<String> cssFilenameList = new java.util.ArrayList<String>();
    if (cssFilename!=null) cssFilenameList.add(cssFilename);
    String extraCss = simulation.getInformation(INFORMATION.REQUIRED_FILES);
    if (extraCss!=null && extraCss.trim().length()>0) {
      StringTokenizer tkn = new StringTokenizer(extraCss,";");
      while (tkn.hasMoreTokens()) {
        String filename = tkn.nextToken();
        if (filename.toLowerCase().endsWith(".css")) cssFilenameList.add(filename);
      }
    }
    return cssFilenameList;
  }

  
  static public boolean saveHTMLFile(Osejs ejs, String libraryPath, File outputFile, 
      SimulationXML simulation, String viewDesired, String locale, String cssFilename, String libPrefix, String htmlPath,
      boolean separatedJSFile, boolean useFullLibrary) {

    JSObfuscator.Level obfuscationLevel = ejs.getOptions().fullJSObfuscation() ? JSObfuscator.Level.FULL : JSObfuscator.Level.HALF;
    XMLTransformerJava transformer = new XMLTransformerJava(ejs,libraryPath,simulation, obfuscationLevel,useFullLibrary);

    Element viewSelected = simulation.getViewSelected(viewDesired);
    try {
      outputFile.getParentFile().mkdirs();
      return transformer.saveSimulationHTML(outputFile,viewSelected,locale, getCssFilenameList(cssFilename,simulation), libPrefix, htmlPath);
    } 
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  // ------------------------------------
  // Non-static part
  // ------------------------------------

  private SimulationXML mSimulation;
  private Osejs mEjs;
  private JSObfuscator mObfuscator;

  /**
   * XML to HTML transformer
   * @param simulation
   * @param output
   */
  public XMLTransformerJava(Osejs ejs, String libraryPath, SimulationXML simulation, JSObfuscator.Level obfuscationLevel, boolean useFullLibrary) {
    mEjs = ejs;
    mSimulation = simulation;
    mObfuscator = new JSObfuscator(libraryPath,useFullLibrary,obfuscationLevel);
  }

  // ------------------------------------
  // HTML generation
  //-------------------------------------

  /**
   * Plain JS for the Emulator. Uses a Dummy script to define basic variables. Strip out parts that offend Lint unnecessarily
   * @param viewDesired
   * @param locale
   * @param librariesPath
   * @param codebase
   * @return
   */
  String getJavascriptForEmulator(Element viewDesired, String locale, String librariesPath, String codebasePath) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(readTextFile(new File(librariesPath, sDUMMYSCRIPT)));    
    buffer.append(getScriptsImport(codebasePath,false));
    buffer.append(getViewStringBuffer(codebasePath,viewDesired,locale, false));    
    if (!mSimulation.isViewOnly()) buffer.append(getModelStringBuffer(viewDesired,locale,librariesPath,false));
    return buffer.toString();
  }

  /**
   * Returns a self contained HTML code, ready to be run by the Emulator
   * @param viewDesired
   * @param locale
   * @param librariesPath
   * @param codebase
   * @return
   */
  String toSimulationHTMLForEmulator(Element viewDesired, String locale, java.util.List<String> cssFilenameList, String libraryPrefix, String codebase) {
    String code = createSimulationHTML(viewDesired, locale, cssFilenameList, libraryPrefix, codebase, null); // false = no Scorm
//        try {  // save it for debugging purposes
//          String outputFilepath = mSimulation.getName()+"_debug.html";
//          File outputFile = new File (outputFilepath);
//          saveToFile(outputFile, code);
//          mErrorOutput.println("Debug output file generated "+outputFile.getAbsolutePath());
//        } 
//        catch (Exception e) {
//          e.printStackTrace();
//        }
    return code;
  }

  /**
   * Generated a visible HTML code and saves it to the output directory of EJS
   * @param outputFile the file to create
   * @param viewDesired
   * @param locale
   * @param cssPrefix The prefix to use to find css files in the generated file
   * @param libraryPrefix The prefix to use to find library files in the generated file
   * @param codebase The prefix for auxiliary files
   * @return File the same output file, null 
   */
  boolean saveSimulationHTML(File outputFile, Element viewDesired, String locale, java.util.List<String> cssFilenameList, String libraryPrefix, String codebase) {
    try {
      outputFile.getParentFile().mkdirs();
      if (mEjs.getOptions().separateJSfile()) {
        String title = mSimulation.getInformation(INFORMATION.TITLE);
        String htmlhead = mSimulation.getInformation(INFORMATION.HTMLHEAD);
        String name = org.colos.ejs.library.utils.FileUtils.getPlainName(outputFile);
        String scriptsImport = getScriptsImport(codebase,true);
        saveToFile(outputFile, mObfuscator.generatePlainHTML(title, name, htmlhead, scriptsImport, getMetadataCode(codebase,libraryPrefix), libraryPrefix, codebase, 
            cssFilenameList));
        File jsFile = new File(outputFile.getParentFile(),name+".js");
        if (locale==null) locale = SimulationXML.sDEFAULT_LOCALE;
        String modelCode = (mSimulation.isViewOnly()) ? null : getModelStringBuffer(viewDesired, locale,libraryPrefix,true).toString();
        String viewCode = getViewStringBuffer(codebase, viewDesired,locale,true).toString();
        saveToFile(jsFile, mObfuscator.generatePlainJS(mSimulation.getName(), modelCode, viewCode,mSimulation.getServerLocalPort(), libraryPrefix, codebase));
        //createSimulationPlainJS(viewDesired, locale, libraryPrefix, codebase));
      }
      else {
        saveToFile(outputFile, createSimulationHTML(viewDesired, locale, cssFilenameList, libraryPrefix, codebase,getMetadataCode(codebase,libraryPrefix)));
      }
      return true;
    } 
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Generated a minimized HTML code
   * @param obfuscationLevel the obfuscation code
   * @param viewDesired
   * @param locale
   * @param libraryPrefix
   * @param codebasePath
   * @return String the complete HTML code
   */
  private String createSimulationHTML(Element viewDesired, String locale, java.util.List<String> cssFilenameList, String libraryPrefix, String codebasePath, 
      String metadataCode) {
    if (locale==null) locale = SimulationXML.sDEFAULT_LOCALE;
    String title = mSimulation.getInformation(INFORMATION.TITLE);
    String htmlHead = mSimulation.getInformation(INFORMATION.HTMLHEAD);
    String name = mSimulation.getName();
    String scriptsImport = getScriptsImport(codebasePath,true);
    String modelCode = (mSimulation.isViewOnly()) ? null : getModelStringBuffer(viewDesired, locale,libraryPrefix,true).toString();
    String viewCode = getViewStringBuffer(codebasePath, viewDesired,locale,true).toString();
    return mObfuscator.generate(title, name, htmlHead, scriptsImport, metadataCode, modelCode, viewCode, cssFilenameList, mSimulation.getServerLocalPort(), libraryPrefix, 
        codebasePath);
  }

//  private String createSimulationPlainJS(Element viewDesired, String locale, String libraryPrefix, String codebasePath) {
//    if (locale==null) locale = SimulationXML.sDEFAULT_LOCALE;
//    String modelCode = (mSimulation.isViewOnly()) ? null : getModelStringBuffer(viewDesired, locale,libraryPrefix,true).toString();
//    String viewCode = getViewStringBuffer(codebasePath, viewDesired,locale,true).toString();
//    return mObfuscator.generatePlainJS(mSimulation.getName(), modelCode, viewCode,mSimulation.getServerLocalPort(), libraryPrefix, codebasePath);
//  }

  /**
   * Converts a set of image filenames into a String with the Base64 coding for those images
   * @param ejs
   * @param base64Set
   * @param prefix
   * @return
   */
  static public String getJavascriptForBase64Images (Osejs ejs, Set<String> base64Set, String prefix) { 
    if (base64Set.size()<=0) return prefix;
    String missingText = "";
    StringBuffer buffer = new StringBuffer();
    buffer.append(prefix+"\n");
    buffer.append("var "+sBASE64IMAGES+" = [];\n"); // base64 images used by this view\n");
    File absoluteImagesDir = new File(ejs.getBinDirectory(),"javascript/lib/images");
    for (String token : base64Set) {
      token = token.trim();
      if (token.length()<=0) continue;
      String resourceFilename = org.colos.ejs.osejs.utils.FileUtils.uncorrectUrlString(token.replace('\\','/')); // Change separator char to '/'
      File resFile=null;
//      System.out.println("Processing "+resFile);
      if (resourceFilename.startsWith("/")) resFile = new File (absoluteImagesDir,resourceFilename);
      else {
        Resource res = org.opensourcephysics.tools.ResourceLoader.getResource(resourceFilename);
        if (res!=null) resFile = res.getFile();
      }
      if (resFile==null || !resFile.exists()) {
        missingText += "  "+resourceFilename +"\n";
      }
      else { // Convert to base64
//        String converted = ImageManipulation.convertToBase64(res.getFile());
//        System.out.println("Adding "+"\""+resFile+"\" : \n\""+converted.substring(0,Math.min(100, converted.length()))+"\"");
        String extension = org.colos.ejs.osejs.utils.FileUtils.getPlainNameAndExtension(resFile).getSecondString().toLowerCase();
        String mimeType = MimeInfo.mimeTypeFor(extension);
        buffer.append(sBASE64IMAGES+"[\""+resourceFilename+"\"]=\"data:"+mimeType+";base64,"+ImageManipulation.convertToBase64(resFile)+"\";\n");
      }
    }
    if (missingText.length()>0) JOptionPane.showMessageDialog(ejs.getMainPanel(), 
        res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+missingText, 
        res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    return buffer.toString();
  }

  
  //  /**
  //   * Generated a visible JS and HTML code and saves it to the output directory of EJS
  //   * @param viewDesired
  //   * @param locale
  //   * @param libraryPrefix The prefix to use to find library files in the generated file
  //   * @param codebase
  //   * @return File the generated file
  //   */
  //  File saveSimulationHTMLandJS(File outputFile, Element viewDesired, String locale, String cssPrefix, String libraryPrefix, String codebase) {
  //    // and save it
  //    try {
  //      String title = mSimulation.getInformation(INFORMATION.TITLE);
  ////      String name = mSimulation.getName();
  //      String name = FileUtils.getPlainName(outputFile);
  //      outputFile.getParentFile().mkdirs();
  //      saveToFile(outputFile, null, JSObfuscator.generatePlainHTML(mLicense,title, name, cssPrefix));
  //      File jsFile = new File(outputFile.getParentFile(),name+".js");
  //      saveToFile(jsFile, null, createSimulationPlainJS(viewDesired, locale, libraryPrefix, codebase));
  //      return outputFile;
  //    } 
  //    catch (Exception e) {
  //      e.printStackTrace();
  //      return null;
  //    }
  //  }

  /**
   * Create metadata information
   */  
  private String getMetadataCode (String codebase, String libraryPath) {
//    System.err.println ("Codebase = "+codebase);
//    System.err.println("Library path = "+libraryPath);

    if (codebase==null) codebase = "";
    else {
      codebase = "file://"+codebase;
      libraryPath = "file://"+libraryPath;
    }
    if (!libraryPath.endsWith("/")) libraryPath += "/";
    StringBuffer buffer = new StringBuffer();
    buffer.append("  <div id=\"metadata\" class=\"metadata\">\n");// style=\"margin-top:50px\">\n"); // style=\"float:left\">\n");
    buffer.append("    <br />\n");
    buffer.append("    <div id=\"title_author\">\n");
    buffer.append("      <hr />\n");
    buffer.append("      <b>Title and author:</b>\n");
    buffer.append("      <p>\n");
    String value = mSimulation.getInformation(INFORMATION.TITLE);
    if (value!=null && value.length()>0) buffer.append("      "+value+"<br />\n");
    value = mSimulation.getInformation(INFORMATION.LOGO_IMAGE);
    if (value!=null && value.length()>0) {
      StringTokenizer tkn = new StringTokenizer(value,";");
      while (tkn.hasMoreTokens()) {
        String image = tkn.nextToken();
        if (image.length()<=0) continue;
        if (image.startsWith("./")) image = image.substring(2);
        buffer.append("     <img alt=\"Logo\" src=\""+codebase+image+"\" />\n");
      }
    }
    buffer.append("      </p>\n");
    buffer.append("      <p>\n");
    value = mSimulation.getInformation(INFORMATION.AUTHOR_IMAGE);
    if (value!=null && value.length()>0) {
      StringTokenizer tkn = new StringTokenizer(value,";");
      while (tkn.hasMoreTokens()) {
        String image = tkn.nextToken();
        if (image.length()<=0) continue;
        if (image.startsWith("./")) image = image.substring(2);
        buffer.append("      <img alt=\"author image\" src=\""+codebase+image+"\" />\n");
      }
    }
    String author = mSimulation.getInformation(INFORMATION.AUTHOR);
    if (author==null || author.length()<=0) author = "Author not specified";
    buffer.append(" "+author+"\n");
    buffer.append("      </p>\n");
    buffer.append("    </div>\n");
    buffer.append("    <hr />\n");

    // Add copyright message
    String copyrightOwner = mSimulation.getInformation(INFORMATION.COPYRIGHT);
    //System.err.println("Copy = "+copyrightOwner);
    if (copyrightOwner==null || copyrightOwner.trim().length()<=0) copyrightOwner = author;

    buffer.append("    <p></p>\n");
    buffer.append("    <div id=\"copyright_message\">\n");// style=\"margin-top:50px\">\n");
    buffer.append("      <div class=\"cc_left\"  style=\"float:left\">&#169; "+ Calendar.getInstance().get(Calendar.YEAR)+", "+copyrightOwner+".</div>\n");
    buffer.append("      <div class=\"cc_right\" style=\"float:right\">");
    buffer.append("    Released under a <a rel=\"license\" target=\"_blank\" href=\"http://creativecommons.org/licenses/by-nc-sa/4.0/deed.en_US\">");
//    if (isPackage) 
//      buffer.append(" <img alt=\"Creative Commons Attribution-NonCommercial-ShareAlike\" src=\"_ejs_library/images/cc_icon.png\" />");
//    else 
      buffer.append(" <img alt=\"Creative Commons Attribution-NonCommercial-ShareAlike\" src=\""+libraryPath+"images/cc_icon.png\" />");
    buffer.append(" </a> license.</div>\n");
    buffer.append("    </div>\n");
    buffer.append("  </div>\n");

    return buffer.toString();
  } 

  private String getScriptsImport(String codebasePath, boolean fullScript) {
    String requiredFiles = mSimulation.getInformation(INFORMATION.REQUIRED_FILES);
    String prefixForCodepath;
    StringBuffer buffer = new StringBuffer();
    if (requiredFiles!=null) {
      // Prepare prefix for codebase files
      if (codebasePath==null) prefixForCodepath = "";
      else prefixForCodepath = "file://"+ codebasePath;
//      else {
//        try { prefixForCodepath = (new File(codebasePath)).toURI().toURL().toExternalForm(); } 
//        catch (MalformedURLException e) { 
//          e.printStackTrace();
//          prefixForCodepath = "file://"+ codebasePath;
//        }
//      }
      StringTokenizer tkn = new StringTokenizer(requiredFiles,";");
      while (tkn.hasMoreTokens()) {
        String auxFile = tkn.nextToken();
        String auxLowercase = auxFile.toLowerCase();
        if (auxLowercase.endsWith(".js")) {
          if (auxFile.startsWith("./")) auxFile = auxFile.substring(2);
          if (fullScript) 
            buffer.append("    <script src=\""+prefixForCodepath+auxFile+"\"></script>\n");
//            buffer.append("<script type=\"text/javascript\" src=\""+prefixForCodepath+auxFile+"\"></script>\n");
          else buffer.append(org.colos.ejs.osejs.utils.FileUtils.readTextFile(new File(codebasePath+auxFile),null));
        }
//        else if (auxLowercase.endsWith(".css")) {
//          if (fullScript) { 
//            if (auxFile.startsWith("./")) auxFile = auxFile.substring(2);
//            buffer.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+prefixForCodepath+auxFile+"\" />\n");
//          }
//        }
      }
    }
    // Now insert scripts from model elements
    if (fullScript) {
      HashSet<String> filesSet = new HashSet<String>();
      for (Element page : mSimulation.getModelElements()) {
        String filename = BasicElement.evaluateNode(page,"file");
        filesSet.add(filename);
      } 
      for (String filename : filesSet) {
        File file = new File(mEjs.getBinDirectory(), "javascript/model_elements/"+filename);
        String code = readTextFile(file);
        if (code!=null) {      
          buffer.append("<script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
          buffer.append(code);
          buffer.append("//--><!]]></script>\n");
        }
      }
//      String base64Images = mSimulation.getInformation(INFORMATION.BASE64_IMAGES);
//      if (base64Images!=null && base64Images.length()>0) {
//        buffer.append("<script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
//        buffer.append("var "+sBASE64IMAGES+" = [];\n"); // base64 images used by this view\n");
//        buffer.append(base64Images);
//        buffer.append("//--><!]]></script>\n");
//      }
    }
    else {
      HashSet<String> codeSet = new HashSet<String>();
      for (Element page : mSimulation.getModelElements()) {
        String lintCode = BasicElement.evaluateNode(page,"lintcode");
        codeSet.add(lintCode);
      } 
      for (String code : codeSet) buffer.append(code);
      for (Element page : mSimulation.getModelElements()) {
        String fullCode = BasicElement.evaluateNode(page,"code");
        buffer.append(fullCode);
//        System.out.println("Adding full code "+fullCode);
        buffer.append("\n");
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

  //  private StringBuffer readScriptsImport(String codebasePath) {
  //    StringBuffer buffer = new StringBuffer();
  //    String requiredFiles = mSimulation.getInformation(INFORMATION.REQUIRED_FILES);
  //    if (requiredFiles==null) {
  ////      System.err.println ("Required files empty");
  //      return buffer;
  //    }
  //    StringTokenizer tkn = new StringTokenizer(requiredFiles,";");
  //    while (tkn.hasMoreTokens()) {
  //      String jsScript = tkn.nextToken();
  //      if (jsScript.toLowerCase().endsWith(".js")) {
  //        buffer.append(readTextFile(new File(codebasePath,jsScript)));
  //      }
  //    }
  //    return buffer;
  //  }


  private StringBuffer getModelStringBuffer(Element viewSelected, String locale, String libraryPrefix, boolean fullScript) {
    StringBuffer buffer = new StringBuffer();

//    // First insert scripts from model elements
//    for (Element page : mSimulation.getModelElements()) {
//      String initcode = BasicElement.evaluateNode(page,"code");
//      buffer.append(initcode);
//      buffer.append("\n");
//    }
    
    // Compute the number of ODEs   
    int numODEs = 0;
    for (Element page : mSimulation.getModelEvolution()) {
      String type = BasicElement.evaluateNode(page,"type");
      if ("ode".equals(type)) numODEs++;
    }

    buffer.append("/* _inputParameters: an object with different values for the model parameters */\n");
    buffer.append("function "+ mSimulation.getName()+"(_topFrame,_libraryPath,_codebasePath, _inputParameters) {\n");
    //    buffer.append("function "+ mSimulation.getName()+"(_topFrame) {\n");
    
    buffer.append("  var _model = EJSS_CORE.createAnimationLMS();\n");
    buffer.append("  var _view;\n");
    buffer.append("  var _isPlaying = false;\n");
    buffer.append("  var _isPaused = true;\n");
//    buffer.append("  var _isEPub = (__IS_EPUB__===undefined) ? false : __IS_EPUB__;\n");
    buffer.append("  var _isMobile = (navigator===undefined) ? false : navigator.userAgent.match(/iPhone|iPad|iPod|Android|BlackBerry|Opera Mini|IEMobile/i);\n\n");
    buffer.append("var _stringProperties = {};\n");

    buffer.append("  var _tools = {\n");
    buffer.append("    showInputDialog : EJSS_INTERFACE.BoxPanel.showInputDialog,\n");
    buffer.append("    showOkDialog : EJSS_INTERFACE.BoxPanel.showOkDialog,\n");
    buffer.append("    showOkCancelDialog : EJSS_INTERFACE.BoxPanel.showOkCancelDialog,\n");
    buffer.append("    downloadText: EJSS_TOOLS.File.downloadText,\n");
    buffer.append("    uploadText: function(action) { EJSS_TOOLS.File.uploadText(_model,action); } \n");
    buffer.append("  };\n\n");

    // insert scripts from model elements
    for (Element page : mSimulation.getModelElements()) {
      String initcode = BasicElement.evaluateNode(page,"code");
      buffer.append(initcode);
      buffer.append("\n");
    }

    buffer.append("  function _play()  { _isPaused = false; _isPlaying = true;  _model.play();  }\n");
    buffer.append("  function _pause() { _isPaused = true;  _isPlaying = false; _model.pause(); }\n");
    buffer.append("  function _step()  { _pause();  _model.step(); }\n");
    buffer.append("  function _reset() { _model.reset();  _isPaused = _model.isPaused(); _isPlaying = _model.isPlaying(); }\n");

    buffer.append("  _model._play  = _play;\n");
    buffer.append("  _model._pause = _pause;\n");
    buffer.append("  _model._step  = _step;\n");
    buffer.append("  _model._reset = _reset;\n");

    buffer.append("  function _update() { _model.update(); }\n");
    buffer.append("  function _initialize() { _model.initialize(); }\n");
    buffer.append("  function _setFPS(_fps) { _model.setFPS(_fps); }\n");
    buffer.append("  function _setDelay(_delay) { _model.setDelay(_delay); }\n");
    buffer.append("  function _setStepsPerDisplay(_spd) { _model.setStepsPerDisplay(_spd); }\n");
    buffer.append("  function _setUpdateView(_updateView) { _model.setUpdateView(_updateView); }\n");
    buffer.append("  function _setAutoplay(_auto) { _model.setAutoplay(_auto); }\n");
    buffer.append("  function _println(_message) { console.log(_message); }\n\n");
    buffer.append("  function _breakAfterThisPage() { _model.setShouldBreak(true); }\n\n");
    buffer.append("  function _resetSolvers() { if (_model.resetSolvers) _model.resetSolvers(); }\n\n");
    buffer.append("  function _saveText(name,type,content) { if (_model.saveText) _model.saveText(name,type,content); }\n\n");
    buffer.append("  function _saveState(name) { if (_model.saveState) _model.saveState(name); }\n\n");
    buffer.append("  function _saveImage(name,panelname) { if (_model.saveImage) _model.saveImage(name,panelname); }\n\n");
    buffer.append("  function _readState(url,type) { if (_model.readState) _model.readState(url,type); }\n\n");
    buffer.append("  function _readText(url,type,varname) { if (_model.readText) _model.readText(url,type,varname); }\n\n");
    buffer.append("  function _getStringProperty(propertyName) {\n");
    buffer.append("    var _value = _stringProperties[propertyName];\n");
    buffer.append("    if (_value===undefined) return propertyName;\n");
    buffer.append("    else return _value;\n");
    buffer.append("  }\n");
    LocaleItem localeItem = mEjs.getLocaleItem();
    for (org.colos.ejs.osejs.utils.TwoStrings ts : mEjs.getTranslationEditor().getResourceLocalePairs(localeItem)) {
        buffer.append("_stringProperties." + ts.getFirstString() + " = \"" + ts.getSecondString() + "\";\n");
    }
   /*OneView oneViewPage = ((OneView) htmlViewEditor.getCurrentPage());
    if (oneViewPage!=null) {
        LocaleItem localeItem = oneViewPage.getLocale();
        for (org.colos.ejs.osejs.utils.TwoStrings ts : mEjs.getTranslationEditor().getResourceLocalePairs(localeItem)) {
            buffer.append("_stringProperties." + ts.getFirstString() + " = \"" + ts.getSecondString() + "\";\n");
        }
    }*/
    //    buffer.append("  function _isPlaying() { _model.isPlaying(); }\n\n");
    //    buffer.append("  function _isPaused() { _model.isPaused(); }\n\n");

    buffer.append("  var __pagesEnabled = [];\n");
    buffer.append("  function _setPageEnabled(pageName,enabled) { __pagesEnabled[pageName] = enabled; }\n\n");
//    buffer.append("  function _isPageEnabled(pageName) { return __pagesEnabled[pageName]; }\n\n\n");

    // Variables declaration
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored
      String pageName = BasicElement.evaluateNode(page,"name");
      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String name = BasicElement.evaluateNode(variable, "name"); 
        java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(name,"[] ");
        buffer.append("  var "+tknIndexes.nextToken()+";");
        buffer.append(" // EjsS Model.Variables."+pageName+"."+name+"\n");
      }
      buffer.append("\n");
    }

    // BLOCKLY EXPERIMENTS //
    String odesNames = "[";
    if (numODEs>0) { // ODEs declaration
      buffer.append("  var _privateOdesList;\n");
      for (int i=1; i<=numODEs; i++) {
        buffer.append("  var _ODEi_evolution"+i+";\n");
        if(i==1) odesNames+="_ODEi_evolution"+i;
        else odesNames+=",_ODEi_evolution"+i;
        buffer.append("  var userEvents"+i+"=[];\n");
      }
      buffer.append("\n");
    }
    odesNames += "]";
    
    
    buffer.append("  _model.getOdes = function() { return "+odesNames+"; };\n\n");
    buffer.append("  _model.removeEvents = function(){\n");
    if (numODEs>0) { // ODEs declaration
      for (int i=1; i<=numODEs; i++) {
        buffer.append("    userEvents"+i+"=[];\n");
      }
    }
    buffer.append("  };\n\n");

    
    
    
    // Makes JSLint complain buffer.append("  _model.addFixedRel = function(code){_model.addToFixedRelations(function() { eval(code);});};\n\n  ");    
    ///

    buffer.append("  function _serialize() { return _model.serialize(); }\n\n");
    buffer.append("  _model._userSerialize = function() {\n");
    buffer.append("    return {\n");
    boolean firstOne = true;
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored

      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String name = BasicElement.evaluateNode(variable, "name");
        int index = name.indexOf('[');
        if (index>0) name = name.substring(0, index).trim();
        if (firstOne) firstOne = false;
        else buffer.append(",\n");
        buffer.append("      "+name+" : "+name);
//
//        String dimension = BasicElement.evaluateNode(variable, "dimension");
//        if (dimension!=null && dimension.trim().length()>0) {
//          java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
//          int dim = tkn.countTokens();
//          buffer.append("    "+name+" : EJSS_TOOLS.serializeArray("+name+","+dim+"),");
//        }
//        else { // Simple variable 
//          buffer.append("    "+name+" : "+name+"");
//        }
      }
    }
    buffer.append("\n    };\n");
    buffer.append("  };\n\n");
    
    
   
    

    buffer.append("  _model._readParameters = function(json) {\n");
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored
      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String name = BasicElement.evaluateNode(variable, "name"); 
        int index = name.indexOf('[');
        if (index>0) name = name.substring(0, index).trim();
        buffer.append("    if(typeof json."+name+" != \"undefined\") "+name+" = json."+name+";\n");
      }
    }
    buffer.append("  };\n\n");
    
    buffer.append("  _model._inputAndPublicParameters =");
    String parameters = "";
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored
      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String domain = BasicElement.evaluateNode(variable, "domain");
        if((domain.contains("public"))||(domain.contains("input"))){
          String name = BasicElement.evaluateNode(variable, "name"); 
          int index = name.indexOf('[');
          if (index>0) name = name.substring(0, index).trim();
          if(parameters.equals("")) parameters ="\""+name+"\"";
          else parameters = parameters+",  \""+name+"\"";
        }
      }
    }
    
    buffer.append(" ["+ parameters+"]; \n\n");
    
    buffer.append("  _model._outputAndPublicParameters =");
    parameters = "";
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored
      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String domain = BasicElement.evaluateNode(variable, "domain");
        if((domain.contains("public"))||(domain.contains("output"))){
          String name = BasicElement.evaluateNode(variable, "name"); 
          int index = name.indexOf('[');
          if (index>0) name = name.substring(0, index).trim();
          if(parameters.equals("")) parameters ="\""+name+"\"";
          else parameters = parameters+",  \""+name+"\"";
        }
      }
    }
    
    buffer.append(" ["+ parameters+"];\n\n");
    
    
    buffer.append("  function _unserialize(json) { return _model.unserialize(json); }\n\n");
    
    buffer.append("  _model._userUnserialize = function(json) {\n");
    buffer.append("    _model._readParameters(json);\n");
    buffer.append("   _resetSolvers();\n");
    buffer.append("   _model.update();\n");
    buffer.append("  };\n\n");
    
    if (fullScript) { // Resetting pages enabled
      buffer.append("  _model.addToReset(function() {\n");
      for (Element page : mSimulation.getModelInitialization()) {
        buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(page,"name")+"\"] = "+mSimulation.isEnabled(page)+";\n");
      }
      for (Element evolutionPage : mSimulation.getModelEvolution()) {
        String type = BasicElement.evaluateNode(evolutionPage,"type");
        buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(evolutionPage,"name")+"\"] = "+mSimulation.isEnabled(evolutionPage)+";\n");
        if ("ode".equals(type)) {
          for (Element page : mSimulation.getODEEvents(evolutionPage)) {
            buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(page,"name")+"\"] = "+mSimulation.isEnabled(page)+";\n");
          }
          for (Element page : mSimulation.getODEDiscontinuities(evolutionPage)) {
            buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(page,"name")+"\"] = "+mSimulation.isEnabled(page)+";\n");
          }
          for (Element page : mSimulation.getODEErrorHandlers(evolutionPage)) {
            buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(page,"name")+"\"] = "+mSimulation.isEnabled(page)+";\n");
          }
        }
      }
      for (Element page : mSimulation.getModelFixedRelations()) {
        buffer.append("    __pagesEnabled[\""+BasicElement.evaluateNode(page,"name")+"\"] = "+mSimulation.isEnabled(page)+";\n");
      }
      buffer.append("  });\n\n");
    }

    // Variables reset
    for (Element page : mSimulation.getModelVariables()) {
      if (!mSimulation.isEnabled(page)) continue; // Disabled pages of variables are simply ignored
      String pageName = BasicElement.evaluateNode(page,"name");
      buffer.append("  _model.addToReset(function() {\n");
      for (Element variable : BasicElement.getElementList(page,SimulationXML.sVARIABLE_TAG)) {
        String name = BasicElement.evaluateNode(variable, "name"); 
        String value = BasicElement.evaluateNode(variable, "value");
        String dimension = BasicElement.evaluateNode(variable, "dimension");
        if (dimension!=null && dimension.trim().length()>0) {
          String varName = name;
          java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
          int dim = tkn.countTokens();
          java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(varName,"[] ");
          int dimIndex = tknIndexes.countTokens();
          String lineOfIndexes = null;
          if (dimIndex>1) {
            varName = tknIndexes.nextToken();
            lineOfIndexes = tknIndexes.nextToken();
            while (tknIndexes.hasMoreTokens()) lineOfIndexes += ","+tknIndexes.nextToken();
            if ((dimIndex-1)!=dim) {
              mEjs.getOutputArea().println ("Syntax error: Dimension brackets in variable name "+name+ " ("+BasicElement.evaluateNode(page, "name")+ ") do not match the dimension "+dimension);
            }
          }
          String comment = " // EjsS Model.Variables."+BasicElement.evaluateNode(page,"name")+"."+name;
          buffer.append(SimulationXML.initCodeForAnArray ("    ",comment, lineOfIndexes, varName, dimension, value)+"\n");
        }
        else {
          if (value!=null && value.trim().length()>0) {
            buffer.append("    "+name+" = "+value+";");
            buffer.append(" // EjsS Model.Variables."+pageName+"."+name+"\n");
          }
        }
      }
      buffer.append("  });\n\n");
    }

    buffer.append("  if (_inputParameters) {\n");
    buffer.append("    _inputParameters = _model.parseInputParameters(_inputParameters);\n");
    buffer.append("    if (_inputParameters) _model.addToReset(function() { _model._readParameters(_inputParameters); });\n");
    buffer.append("  }\n\n");

    // Evolution's ODEs
    if (numODEs>0 && !mSimulation.isInsideEJS()) { // ODEs declaration
      buffer.append("  _model.addToReset(function() {\n");
      buffer.append("    _privateOdesList=[];\n");
      for (int i=1; i<=numODEs; i++) {
        buffer.append("    _ODEi_evolution"+i+" = _ODE_evolution"+i+"();\n");
        buffer.append("    _privateOdesList.push(_ODEi_evolution"+i+");\n");
      }
      buffer.append("  });\n\n");
    }
    
    { // Reset Model parameters
      buffer.append("  _model.addToReset(function() {\n");
      boolean useAutoplay = false;
      for (Element page : mSimulation.getModelEvolution()) {
        if (mSimulation.isEnabled(page)) { useAutoplay = true; break; }
      }
      if (useAutoplay) {
        boolean isAutostart = false;
        String autoText = mSimulation.getModelConfiguration(MODEL.AUTOSTART);
        if (autoText!=null) isAutostart = "true".equals(autoText.toLowerCase());
        buffer.append("    _model.setAutoplay("+isAutostart+");\n");
        
        boolean pauseOnExit = true;
        String pauseOnExitText = mSimulation.getModelConfiguration(MODEL.PAUSE_ON_PAGE_EXIT);
        if (pauseOnExitText!=null) pauseOnExit = ! "false".equals(pauseOnExitText.toLowerCase());
        buffer.append("    _model.setPauseOnPageExit("+pauseOnExit+");\n"); 
      }
      else {
        buffer.append("    _model.setAutoplay(false);\n");
      }

      String runningMode = mSimulation.getModelConfiguration(MODEL.RUNNING_MODE);
      if (runningMode!=null) buffer.append("    _model.setRunningMode(EJSS_CORE.Model."+runningMode+");\n");

      String fpsText = mSimulation.getModelConfiguration(MODEL.FRAMES_PER_SECOND);
      if (fpsText==null) fpsText = "10"; 
      String spdText = mSimulation.getModelConfiguration(MODEL.STEPS_PER_DISPLAY);
      if (spdText==null) spdText = "1";
      buffer.append("    _model.setFPS("+fpsText+");\n");
      buffer.append("    _model.setStepsPerDisplay("+spdText+");\n");
      buffer.append("  });\n\n");
    }
    
    // Custom code goes first to avoid "used before defined" messages
    for (Element page : mSimulation.getModelCustomCodePages()) {
      if (!mSimulation.isEnabled(page)) continue;
      String name = BasicElement.evaluateNode(page,"name");
      String code = BasicElement.evaluateNode(page,"code");
      SimulationXML.splitCode(buffer,code,"CustomCode."+name,"  ");
      buffer.append("\n");
    }

    // Initialization
    for (Element page : mSimulation.getModelInitialization()) {
      String name = BasicElement.evaluateNode(page,"name");
      String code = BasicElement.evaluateNode(page,"code");
      buffer.append("  _model.addToInitialization(function() {\n");
      if (fullScript) buffer.append("    if (!__pagesEnabled[\""+name+"\"]) return;\n");
      SimulationXML.splitCode(buffer,code,"Initialization."+name,"    ");
      buffer.append("  });\n\n");
    }

    if (numODEs>0 && !mSimulation.isInsideEJS()) { // ODEs Initialization
      buffer.append("  _model.addToInitialization(function() {\n");
      buffer.append("    _initializeSolvers();\n");
      buffer.append("  });\n\n");
    }

    // Evolution (including ODEs, because order is important)
    {
      int pageCounter = 0;
      for (Element page : mSimulation.getModelEvolution()) {
        String type = BasicElement.evaluateNode(page,"type");
        String name = BasicElement.evaluateNode(page,"name");
        if ("code".equals(type)) {
          String code = BasicElement.evaluateNode(page,"code");
          buffer.append("  _model.addToEvolution(function() {\n");
          if (fullScript) buffer.append("    if (!__pagesEnabled[\""+name+"\"]) return;\n");
          SimulationXML.splitCode(buffer,code,"Evolution."+name,"    ");
          buffer.append("  });\n\n");
        }
        else if ("ode".equals(type)) {
          pageCounter++;
          buffer.append("  _model.addToEvolution(function() {\n");
          if (fullScript) buffer.append("    if (!__pagesEnabled[\""+name+"\"]) return;\n");
          buffer.append("    _ODEi_evolution"+pageCounter+".step();\n");
          buffer.append("  });\n\n");
        }
      }
    }
    // Fixed relations
    buffer.append("  _model.addToFixedRelations(function() { _isPaused = _model.isPaused(); _isPlaying = _model.isPlaying(); });\n\n");
    for (Element page : mSimulation.getModelFixedRelations()) {
      String name = BasicElement.evaluateNode(page,"name");
      String code = BasicElement.evaluateNode(page,"code");
      buffer.append("  _model.addToFixedRelations(function() {\n");
      if (fullScript) buffer.append("    if (!__pagesEnabled[\""+name+"\"]) return;\n");
      SimulationXML.splitCode(buffer,code,"FixedRelations."+name,"    ");
      buffer.append("  });\n\n");
    }
    buffer.append("  _model.addToFixedRelations(function() { _isPaused = _model.isPaused(); _isPlaying = _model.isPlaying(); });\n\n");

    // ODE code 
    if (numODEs>0) XMLTransformerODE.appendCode(buffer, mSimulation);

    // View from the model
    //Paco para cada vista y seleccionar la deseada por defecto

//    buffer.append("    _model._resized = function(_width,_height) {\n");
//    buffer.append("      _view._resized(_width,_height);\n");
//    buffer.append("  }; // end of _resized\n");
    buffer.append("    _model._fontResized = function(iBase,iSize,iDelta) {\n");
    buffer.append("      _view._fontResized(iBase,iSize,iDelta);\n");
    buffer.append("  }; // end of _fontResized\n\n");

    buffer.append("  function _getViews() {\n");
    buffer.append("    var _viewsInfo = [];\n");
    buffer.append("    var _counter = 0;\n");
    for (Element view : mSimulation.getViews()) {
      String viewName = BasicElement.evaluateNode(view, "name");
      String viewWidth = BasicElement.evaluateNode(view, "width");
      String viewHeight = BasicElement.evaluateNode(view, "height");
      buffer.append("    _viewsInfo[_counter++] = { name : \""+viewName+"\", width : "+viewWidth+", height : "+viewHeight+ " };\n");
    }
    buffer.append("    return _viewsInfo;\n");
    buffer.append("  } // end of _getViews\n\n");

    buffer.append("  function _selectView(_viewNumber) {\n");
    buffer.append("    _view = null;\n");
    buffer.append("    _view = new "+mSimulation.getName()+"_View(_topFrame,_viewNumber,_libraryPath,_codebasePath);\n");

    buffer.append("    var _view_super_reset = _view._reset;\n");
    buffer.append("    _view._reset = function() {\n");
    buffer.append("      _view_super_reset();\n");
    buffer.append("      switch(_viewNumber) {\n");
    int viewCounter = 0;
    for (Element view : mSimulation.getViews()) {
      String viewName = BasicElement.evaluateNode(view, "name");
      if (view.equals(viewSelected)) {
        buffer.append("        case -10 : break; // make Lint happy\n");
        buffer.append("        default :\n");
      }
      buffer.append("        case "+viewCounter+ ":\n");
      // Root view properties
      {
        Element rootViewElement = mSimulation.getRootViewElement(view);
        if (rootViewElement!=null) {
          // For each constant property
          for (Element property : BasicElement.getElementList(rootViewElement,SimulationXML.sELEMENT_PROPERTY_TAG)) { // for each property
            String name  = BasicElement.evaluateNode(property, "name");
            String value = mSimulation.getViewElementPropertyValue(property, locale);
            buffer.append("          _view._setRootProperty(_model,\""+name+"\","+value+");");
            buffer.append(" // "+viewName+" setting property '"+name+"' for root element\n");
          }
        } // End root view properties
      }
      for (Element element : mSimulation.getViewElements(view)) { // for each element of the current view
        String elementName = BasicElement.evaluateNode(element, "name");
        for (Element property : BasicElement.getElementList(element,SimulationXML.sELEMENT_PROPERTY_TAG)) { // for each property
          String name  = BasicElement.evaluateNode(property, "name"); 
          String value = mSimulation.getViewElementPropertyValue(property, locale);

          switch (SimulationXML.toPropertyTag(BasicElement.evaluateNode(property, "type"))) {
            case CONSTANT : break; // Do nothing  
            case ACTION : 
              buffer.append("          _view."+elementName+".setAction(\""+name+"\", "+ value+");");
              buffer.append(" // "+viewName+" setting action '"+name+"' for element '"+elementName+"'\n");
              break;
            case EXPRESSION : 
              buffer.append("          _view."+elementName+".linkProperty(\""+name+"\", ");
              if (value.indexOf("return")>=0) {
                buffer.append(" function() {\n");
                SimulationXML.splitCode(buffer,value,elementName+"."+name,"      ");
                if (!value.trim().endsWith(";")) buffer.append("      ;");
                buffer.append("    }");
              }
              else {
                if (value.trim().endsWith(";")) buffer.append(" function() { return "+ value+" } ");
                else buffer.append(" function() { return "+ value+"; } ");
              }
              buffer.append(");");
              buffer.append(" // "+viewName+" linking property '"+name+"' for element '"+elementName+"'\n");
              break;
            case VARIABLE : 
              buffer.append("          _view."+elementName+".linkProperty(\""+name+"\", ");
              buffer.append(" function() { return "+ value+"; },");
              buffer.append(" function(_v) { "+ value+" = _v; } ");
              buffer.append(");");
              buffer.append(" // "+viewName+" linking property '"+name+"' for element '"+elementName+"'\n");
              break;
          } // end switch
        } // end for property
      } // for view element
      buffer.append("          break;\n");
      viewCounter++;
    }
    buffer.append("      } // end of switch\n");
    buffer.append("    }; // end of new reset\n\n");
    buffer.append("    _model.setView(_view);\n");
    buffer.append("    _model.reset();\n");
    buffer.append("    _view._enableEPub();\n");
    buffer.append("  } // end of _selectView\n\n");


    // Model parameters
    { 
      if (mSimulation.getModelEvolution().size()<=0) {
        buffer.append("  _model.setAutoplay(false);\n");
      }
      else {
        boolean isAutostart = false;
        String autoText = mSimulation.getModelConfiguration(MODEL.AUTOSTART);
        if (autoText!=null) isAutostart = "true".equals(autoText.toLowerCase());
        buffer.append("  _model.setAutoplay("+isAutostart+");\n");
      }

      String fpsText = mSimulation.getModelConfiguration(MODEL.FRAMES_PER_SECOND);
      if (fpsText==null) fpsText = "10"; 
      String spdText = mSimulation.getModelConfiguration(MODEL.STEPS_PER_DISPLAY);
      if (spdText==null) spdText = "1";
      buffer.append("  _model.setFPS("+fpsText+");\n");
      buffer.append("  _model.setStepsPerDisplay("+spdText+");\n");
    }

    // And that's it!
    String autoSV = mSimulation.getInformation(INFORMATION.AUTOSELECT_VIEW);
    if ("true".equals(autoSV)) buffer.append("  _selectView(_model._autoSelectView(_getViews())); // this includes _model.reset()\n");
    else buffer.append("  _selectView(-1); // this includes _model.reset()\n");
    buffer.append("  return _model;\n");
    buffer.append("}\n");
    return buffer;
  }

  private boolean hasProperty(Element element, String locale, String propertyName, String propertyValue) {
    for (Element property : BasicElement.getElementList(element,SimulationXML.sELEMENT_PROPERTY_TAG)) { // for each property
      String name  = BasicElement.evaluateNode(property, "name");
      if (propertyName.equals(name) && SimulationXML.toPropertyTag(BasicElement.evaluateNode(property, "type"))==SimulationXML.PROPERTY.CONSTANT) {
        String value = mSimulation.getViewElementPropertyValue(property, locale);
        if (value.startsWith("\"")) value = value.substring(1);
        if (value.endsWith("\""))   value = value.substring(0, value.length()-1);
        if (propertyValue.equalsIgnoreCase(value)) return true;
      }
    }
    return false;
  }
  
  private StringBuffer getViewStringBuffer(String codebasePath, Element viewSelected, String locale, boolean fullScript) {
    String serverURL =  mSimulation.getServerLocalPort();
    StringBuffer buffer = new StringBuffer();
    
    if (!fullScript) { // Make lint happy. If fullScript, the complete function is defined in getScriptImports
//      buffer.append("var __base64Images = null;\n");
      buffer.append("function _getBase64Image(__base64ImageName) { return __base64ImageName; }\n\n");
    }
    
    if (mSimulation.isViewOnly() && serverURL!=null) {
      buffer.append("function " + mSimulation.getName()+"_View (_topFrame,_localPort,_viewNumber,_libraryPath,_codebasePath) {\n");
    }
    else {
      buffer.append("function " + mSimulation.getName()+"_View (_topFrame,_viewNumber,_libraryPath,_codebasePath) {\n");
    }
    buffer.append("  var _view;\n");
    int viewCounter = 0;
    buffer.append("  switch(_viewNumber) {\n");
    for (Element view : mSimulation.getViews()) {
      if (view.equals(viewSelected)) {
        buffer.append("    case -10 : break; // make Lint happy\n");
        buffer.append("    default :\n");
      }
      buffer.append("    case "+viewCounter+ ": _view = ");
      if (mSimulation.isViewOnly() && serverURL!=null) {
        buffer.append(mSimulation.getName()+"_View_"+viewCounter+" (_topFrame,_localPort); break;\n");
      }
      else {
        buffer.append(mSimulation.getName()+"_View_"+viewCounter+" (_topFrame); break;\n");
      }
      viewCounter++;
    }
    buffer.append("  } // end of switch\n\n");
    buffer.append("  if (_codebasePath) _view._setResourcePath(_codebasePath);\n\n");
    buffer.append("  if (_libraryPath) _view._setLibraryPath(_libraryPath);\n\n");

    int pageCounter = 0;
    for (Element descPage : mSimulation.getDescriptionPages()) {
      pageCounter++;
      String prefix = "./"+mSimulation.getName() + Generate.getIntroductionPageKey(pageCounter, null);
      String descPageName = BasicElement.evaluateNode(descPage, "name");
      String filename = null;
      for (Element localizedPage : mSimulation.getDescriptionPageLocalizations(descPage)) {
        String pageLocale  = BasicElement.evaluateNode(localizedPage, "locale");
        if (locale.equals(pageLocale)) {
          filename = getDescriptionPageURL(prefix, pageLocale, localizedPage);
          break;
        }
        else if (SimulationXML.sDEFAULT_LOCALE.equals(pageLocale)) {
          filename = getDescriptionPageURL(prefix, pageLocale, localizedPage);
        }
      }
      if (filename!=null) buffer.append("  _view._addDescriptionPage('"+descPageName+"','"+filename+"');\n");
    }
    buffer.append("\n");

    if (mSimulation.isViewOnly()) buffer.append("  _view._startUp();\n\n");

    buffer.append("  return _view;\n");
    buffer.append("} // end of main function\n\n");

    viewCounter = 0;
    for (Element view : mSimulation.getViews()) {
      String viewName = BasicElement.evaluateNode(view, "name");
      if (viewName==null) viewName = sEJSS_PREFIX+ " HtmlView: ";
      else viewName = sEJSS_PREFIX+ " HtmlView."+viewName+":";

      if (mSimulation.isViewOnly() && serverURL!=null) {
        buffer.append("function " + mSimulation.getName()+"_View_"+viewCounter+" (_topFrame,_localPort) {\n");
        buffer.append("  var _view = EJSS_CORE.createRemoteView(_topFrame,_localPort);\n\n"); // "ws://localhost:8887"
      }
      else {
        buffer.append("function " + mSimulation.getName()+"_View_"+viewCounter+" (_topFrame) {\n");
        buffer.append("  var _view = EJSS_CORE.createView(_topFrame);\n\n");
      }

      buffer.append("  _view._reset = function() {\n");
      buffer.append("    _view._clearAll();\n");

      if (mSimulation.isViewOnly()) {
        buffer.append("\n");
        buffer.append("    // -----------------------------------\n");
        buffer.append("    // Define this view input/output API\n\n");

        for (Element element : mSimulation.getViewVariables(view)) { // for each element of the selected view
          String variableName = BasicElement.evaluateNode(element, "name");
          String variableValue = BasicElement.evaluateNode(element, "value");
          String type = BasicElement.evaluateNode(element, "type");
          if (variableValue!=null) {
            buffer.append("    _view._registerVariable(\""+variableName+"\","+variableValue);
            if (type!=null && type .equals("INPUT_ONLY")) buffer.append(",true");
            buffer.append(");\n");
          }
          else buffer.append("    _view._registerVariable(\""+variableName+"\");\n");
        }

        for (Element element : mSimulation.getViewActions(view)) { // for each element of the selected view
          String actionName = BasicElement.evaluateNode(element, "name");
          buffer.append("    _view._registerAction(\""+actionName+"\");\n");
        }

        buffer.append("\n");
        buffer.append("    // End of this view input/output API\n");
        buffer.append("    // -----------------------------------\n\n");
      }

      for (Element element : mSimulation.getViewElements(view)) { // for each element in this view
        String elementName  = BasicElement.evaluateNode(element, "name");
        String elementClass = BasicElement.evaluateNode(element, "type");
        String elementParent = BasicElement.evaluateNode(element, "parent");

        if (hasProperty(element, locale, "GraphicsMode", "Canvas")) 
          buffer.append("    _view._addElement("+elementClass+",\""+elementName+"\", _view."+elementParent+",\"GRAPHICS2D_CANVAS\")");
        else 
          buffer.append("    _view._addElement("+elementClass+",\""+elementName+"\", _view."+elementParent+")");
        buffer.append(" // "+viewName+" declaration of element '"+elementName+"'\n");
        // For each constant property
        //      boolean firstTime = true;
        for (Element property : BasicElement.getElementList(element,SimulationXML.sELEMENT_PROPERTY_TAG)) { // for each property
          if (SimulationXML.toPropertyTag(BasicElement.evaluateNode(property, "type"))==SimulationXML.PROPERTY.CONSTANT) {
            String name  = BasicElement.evaluateNode(property, "name");
            String value = mSimulation.getViewElementPropertyValue(property, locale);
            if (name.equals("Html")) { // a very special property
              value = convertSRCtags(value,codebasePath);
            }
            buffer.append("      .setProperty(\""+name+"\","+value+")");
            buffer.append(" // "+viewName+" setting property '"+name+"' for element '"+elementName+"'\n");
            //          if (firstTime) {
            //            buffer.append("\n      .setProperties({ "+name+":"+value);
            //            firstTime = false;
            //          }
            //          else buffer.append(", "+name+":"+value);
          }
        }

        if (mSimulation.isViewOnly()) { // Link properties for view only simulations
          for (Element property : BasicElement.getElementList(element,SimulationXML.sELEMENT_PROPERTY_TAG)) { // for each property
            String name  = BasicElement.evaluateNode(property, "name"); 
            String value = mSimulation.getViewElementPropertyValue(property, locale);

            switch (SimulationXML.toPropertyTag(BasicElement.evaluateNode(property, "type"))) {
              case CONSTANT : break; // Do nothing  
              case ACTION : 
                buffer.append("      .setProperty(\""+name+"\","+value+")");
                buffer.append(" // "+viewName+" setting action '"+name+"' for element '"+elementName+"'\n");
                break;
              case EXPRESSION : 
                buffer.append("      .linkProperty(\""+name+"\","+value+",\"InputOnly\")");
                buffer.append(" // "+viewName+" linking input only property '"+name+"' for element '"+elementName+"'\n");
                break;
              case VARIABLE : 
                buffer.append("      .linkProperty(\""+name+"\",\""+value+"\")");
                buffer.append(" // "+viewName+" linking property '"+name+"' for element '"+elementName+"'\n");
                break;
            } // end switch
          } // end for property
        }
        //      if (firstTime) buffer.append(";\n\n");
        //      else           buffer.append(" });\n\n");
        buffer.append("      ;\n\n");
      }
      buffer.append("  };\n\n");

      buffer.append("  return _view;\n");
      buffer.append("}\n\n");
      viewCounter++;
    }
    return buffer;
  }

  static private String getDescriptionPageURL(String prefix, String locale, Element localizedPage) {
    String pageType = BasicElement.evaluateNode(localizedPage, "type");
    if ("htmlFile".equals(pageType)) return BasicElement.evaluateNode(localizedPage, "filename");
    // plain HTML or XHTML file
    String pageCode = BasicElement.evaluateNode(localizedPage, "code");
    String pageFilename = prefix;
    if (!SimulationXML.sDEFAULT_LOCALE.equals(locale)) pageFilename += "_"+locale;
    if (pageCode.startsWith("<?xml ")) pageFilename += ".xhtml"; 
    else pageFilename += ".html";
    return pageFilename;
  }
  
  /**
   * Read the text inside a file.
   * @param _file File
   * @return String null if failed
   */
  static public String readTextFile(File _file) {
    if (!_file.exists()) return null;
    try {
      //      System.out.println("Reading "+ _file.getName()+ " with charset "+_charset);
      FileReader reader = new FileReader(_file);
      LineNumberReader l = new LineNumberReader(reader);
      StringBuffer buffer = new StringBuffer();
      String sl = l.readLine();
      while (sl != null) { buffer.append(sl+"\n"); sl = l.readLine(); }
      reader.close();
      return buffer.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }


  /**
   * Saves a String to file
   * @param _filename String The name of the file to save
   * @param _content String The content to be saved
   * @throws IOException
   * @return File the file created
   */
  static public File saveToFile (File _file,  String _content) throws Exception {
    Charset charset = UTF8_CHARSET;
    if (charset==null) {
      FileWriter fout = new FileWriter(_file);
      fout.write(_content);
      fout.close();
    }
    else {
      Writer writer = new OutputStreamWriter(new FileOutputStream(_file),charset);
      writer.write(_content,0,_content.length());
      writer.flush();
      writer.close();
    }
    return _file;
  }

  /**
   * Changes the SRC reference in IMG tags to absolute URL
   */
  static private String convertSRCtags (String htmlCode, String codebase) {
    if (codebase==null || codebase.trim().length()<=0) return htmlCode;
    try {
      codebase = (new File(codebase)).toURI().toURL().toExternalForm();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      codebase = "file://"+ codebase;
    }
    if (!codebase.endsWith("/")) codebase += "/";
    String textLowercase = htmlCode.toLowerCase();
    StringBuffer textChanged = new StringBuffer();
    // Compute relativePath
    int index = textLowercase.indexOf("<img");
    while (index>=0) {
      int index2 = textLowercase.indexOf('>',index);
      if (index2<0) break; // This is a syntax error , actually
      String tag = textLowercase.substring(index,index2);
      // Process the tag
      int srcBegin = tag.indexOf("src=\\\"");
      if (srcBegin<0) break;
      srcBegin += 6;
      int srcEnd = index+tag.indexOf("\\\"",srcBegin);
      if (srcEnd<0) break;
      srcBegin = index + srcBegin;
      String filename = htmlCode.substring(srcBegin,srcEnd);
      if (filename.startsWith("./")) filename = filename.substring(2);
      textChanged.append(htmlCode.substring(0,srcBegin)+codebase+filename);
      // Search next tag
      textLowercase = textLowercase.substring(srcEnd);
      htmlCode = htmlCode.substring(srcEnd);
      index = textLowercase.indexOf("<img");
    }
    textChanged.append(htmlCode);
    return textChanged.toString();
  }
  
}
