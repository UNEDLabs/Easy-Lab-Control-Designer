package org.colos.ejss.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

public class JSObfuscator {
  static public final String LIB_MAX_FILENAME = "ejsS.v1.max.js";		// uglified JS libray 	
  static public final String LIB_MIN_FILENAME = "ejsS.v1.min.js";    // uglified JS library
  
  static public final String    sCOMMON_SCRIPT = "scripts/common_script.js";
  static public final String    sTEXTRESIZEDETECTOR_SCRIPT = "scripts/textresizedetector.js";
  static private final String   DEP_FILENAME = "/org/colos/ejss/xml/dependences.json";	// dependences between JS classes
  static private final String   UGLIFY_FILENAME = "uglify.js";			// uglify JS
  static private final String[] GLOBALVARS = new String[] {			// global vars to obfuscate
    // secret view functions
    "setName",
    "_clearAll", "_setResourcePath", "_getResourcePath", "_setLibraryPath", 
    "_registerVariable", "_registerVariables","_registerAction","_registerActions",
    "_addElement", "_getInitialValue", "_startUp",
    "_setReportInteractionMethod", "_linkVariable", 
    "_setReportNeeded", "_initValues", "_readInteractions", "_interactionsNumber",
    "_reportInteraction", "_addListener", "_addUnkownListener", "_getAction", "_addInteraction",
    // SVG
    "getClass", 
    //"drawElement", 
    "drawGutters",
    // 2D 
    "setGutters", "toPixelPosition", "recomputeScales", "checkMeasure"
  }; // add more!

  static private String sJSlib;     	// JS library
  static private JSUglify sUglifier;	// Uglifier
  static private Tokens sTokens;		// dependence tokens

  static private boolean sGenerateXHTML = true;
  
  // read file with dependence tree
  static {
    sTokens = readTokens(DEP_FILENAME);
  }
  
  static public enum Level { OPEN, HALF, FULL };

  
  private boolean mUseFullLibrary = false; // Set to true 
  private String mEjsSLibraryPath;        // path to EjsS library folder
  private String mLibraryVersion = null;  // library version to use
  private Level mObfuscationLevel = Level.HALF;

  public JSObfuscator(String libraryPath, boolean useFullLibrary, Level obfuscationLevel) {
    mUseFullLibrary = useFullLibrary || obfuscationLevel==Level.OPEN;
    mEjsSLibraryPath = libraryPath;
    File maxFile = new File(mEjsSLibraryPath + LIB_MAX_FILENAME);
    if (maxFile.exists()) {
      mLibraryVersion = LIB_MAX_FILENAME;
      mObfuscationLevel = Level.OPEN;
    }
    else {
      mLibraryVersion = LIB_MIN_FILENAME;
      mObfuscationLevel = obfuscationLevel;  //? Level.FULL : Level.HALF;
    }
    try { // read JS library
      byte[] encoded;
      encoded = Files.readAllBytes(Paths.get(mEjsSLibraryPath+mLibraryVersion));
      sJSlib = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
      Reader reader = new java.io.FileReader(libraryPath + UGLIFY_FILENAME); // read uglify JS
      sUglifier = new JSUglify(reader);
    }
    catch (Exception exc) { 
      exc.printStackTrace();
    }
  }

  // ----------------------------------
  // public API
  //----------------------------------

  static public Boolean isGenerateXHTML() {
    return sGenerateXHTML;
  }

  static public void setGenerateXHTML(Boolean how) {
    sGenerateXHTML = how;
  }

  static public String whichJSLibrary(File javascriptDir) {
    File maxFile = new File(javascriptDir, LIB_MAX_FILENAME);
    if (maxFile.exists()) return LIB_MAX_FILENAME;
    return LIB_MIN_FILENAME;
  }
    
  private StringBuffer createHeader (String title, java.util.List<String> cssFilenameList, String htmlHead, String libraryPath, String codebasePath, 
      String scriptsImport, String script) {
    StringBuffer buffer = new StringBuffer();
    if (isGenerateXHTML()) buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    buffer.append("<!DOCTYPE html>\n");
    if (isGenerateXHTML()) buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    else buffer.append("<html>\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta charset=\"utf-8\" />\n");
    buffer.append("    <title>"+title+"</title>\n");
    buffer.append(htmlHead);
    // Find CSS file to include
    if (codebasePath!=null && !codebasePath.endsWith("/")) codebasePath += "/";
    for (String cssFilename : cssFilenameList) {
      if      (cssFilename.startsWith("_"))   buffer.append("    <link rel=\"stylesheet\"  type=\"text/css\" href=\""+cssFilename+"\" />\n");
      else if (cssFilename.startsWith("./"))  {
        if (codebasePath==null) buffer.append("    <link rel=\"stylesheet\"  type=\"text/css\" href=\""+cssFilename.substring(2)+"\" />\n");
        else                    buffer.append("    <link rel=\"stylesheet\"  type=\"text/css\" href=\"file://"+codebasePath+cssFilename.substring(2)+"\" />\n");
      }
      else buffer.append("    <link rel=\"stylesheet\"  type=\"text/css\" href=\"file://"+cssFilename+"\" />\n");
    }

    String lPath = libraryPath.endsWith("/") ? libraryPath : libraryPath+"/";
    if (libraryPath.startsWith("_")) {
      buffer.append("    <script src=\""+lPath+sCOMMON_SCRIPT+"\"></script>\n");
      buffer.append("    <script src=\""+lPath+sTEXTRESIZEDETECTOR_SCRIPT+"\"></script>\n");
      if (mUseFullLibrary) buffer.append("    <script src=\""+lPath+mLibraryVersion+"\"></script>\n");
//      if (isScorm) buffer.append("    <script src=\""+lPath+sTEXTRESIZEDETECTOR_SCRIPT+"\"></script>\n");
    }
    else {
      buffer.append("    <script src=\"file://"+libraryPath+sCOMMON_SCRIPT+"\"></script>\n");
      buffer.append("    <script src=\"file://"+libraryPath+sTEXTRESIZEDETECTOR_SCRIPT+"\"></script>\n");
      if (mUseFullLibrary) buffer.append("    <script src=\"file://"+mEjsSLibraryPath+mLibraryVersion+"\"></script>\n");
//    if (isScorm) buffer.append("    <script src=\""+lPath+sTEXTRESIZEDETECTOR_SCRIPT+"\"></script>\n");
    }
    if (scriptsImport.length()>0) buffer.append(scriptsImport+"\n");
    if (script!=null) buffer.append(script+"\n");
    buffer.append("  </head>\n");
    return buffer;
  }
  
  /**
   * Generate html applying obfuscation and simplifier
   * 
   * @param title
   * @param name
   * @param modelCode
   * @param viewCode
   * @param cssPath
   * @param localPort 
   *  
   */
  public String generate(String title, String name, String htmlHead, String scriptsImport, String metadataCode, String modelCode, String viewCode, 
      java.util.List<String> cssFilenameList, String localPort, String libraryPath, String codebasePath) {
    // JS library and Uglifier must have been defined
  if (!mUseFullLibrary) {
	  if ((sJSlib == null) || (sUglifier == null)) return "";
  }

	// not supported values null
	boolean hasModel = true;	// model code is not null
	if (modelCode == null) { 
		modelCode = ""; 
		hasModel = false; 
	}
	if (viewCode == null) viewCode = "";
	
	String script="";
  if (mUseFullLibrary) {
    switch(mObfuscationLevel) {
      case OPEN: // not obfuscation, not ugly, simplified
      case HALF: // obfuscated, not ugly, simplified
        script = betweenScript(modelCode + "\n" + viewCode);
        break;
      case FULL: // obfuscated, ugly, simplified
        script = betweenScript(uglify(sUglifier, modelCode) + "\n" +uglify(sUglifier, viewCode));
        break;        
    }
  }
  else {
	// get dependence classes in code
    Set<String> dependence = getClasses(modelCode, viewCode);
    
    // get JS script
    //script = sJSlib; // no restrictions
    // apply obfuscation
    switch(mObfuscationLevel) {
    	case OPEN: // not obfuscation, not ugly, simplified
    		script = betweenScript(simplifier(sJSlib,dependence) + "\n" + modelCode + "\n" + viewCode);
    		break;
    	case HALF: // obfuscated, not ugly, simplified
    		script = obfuscater(betweenScript(simplifier(sJSlib,dependence) + "\n" + modelCode + "\n" +viewCode));
    		break;
    	case FULL: // obfuscated, ugly, simplified
    		script = obfuscater(betweenScript(simplifier(sJSlib,dependence) + "\n" + 
    					uglify(sUglifier, modelCode) + "\n" +
    					uglify(sUglifier, viewCode)));
    		break;    		
    }
  }

    // create final HTML
    StringBuffer buffer = createHeader (title, cssFilenameList, htmlHead, libraryPath, codebasePath, scriptsImport, script);

//    if (isScorm) buffer.append("  <body onLoad=\"_scorm_loadPage()\" onbeforeunload=\"_scorm_unloadPage()\">\n");
//    else 
      buffer.append("  <body>\n");
    buffer.append("    <div role=\"button\" id=\"_topFrame\" style=\"text-align:center\"></div>\n");
    if (metadataCode!=null) buffer.append(metadataCode);
    buffer.append("    <script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n");  
    buffer.append(startUpScript(name, libraryPath, codebasePath, hasModel, localPort));

//    //    buffer.append("      window.addEventListener('load', function () { " + startUpScript(name, libraryPath, codebasePath, hasModel, localPort) + " }, false);\n"); 
//    buffer.append("      var _model;\n");
//    buffer.append("      window.addEventListener('load',\n");
//    buffer.append("        function () { \n");
//    buffer.append("          _model = " + startUpScript(name, libraryPath, codebasePath, hasModel, localPort));
//    buffer.append("          TextResizeDetector.TARGET_ELEMENT_ID = '_topFrame';\n");
//    buffer.append("          TextResizeDetector.USER_INIT_FUNC = function () {\n");
//    buffer.append("            var iBase = TextResizeDetector.addEventListener(function(e,args) {\n");
//    buffer.append("              _model._fontResized(args[0].iBase,args[0].iSize,args[0].iDelta);\n");
//    buffer.append("              },null);\n");
//    buffer.append("            _model._fontResized(iBase);\n");
//    buffer.append("          };\n");
//    buffer.append("        }, false);\n"); 
//    buffer.append("      window.addEventListener('resize', function () { _model._resized(window.innerWidth,window.innerHeight); }, false);\n");
    buffer.append("    //--><!]]></script>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    
    return buffer.toString();    
  }
  
  /**
   * Generate only XHTML with license icon
   * 
   * @param license
   * @param title
   * @param name
   * @param cssPath
   * @return
   */
  protected String generatePlainHTML(String title, String name, String htmlHead, String scriptsImport, String metadataCode, String libraryPath, String codePath, 
      java.util.List<String> cssFilenameList) {
    StringBuffer buffer = createHeader (title, cssFilenameList, htmlHead, libraryPath, codePath, scriptsImport, null);

//    if (isScorm) buffer.append("  <body onLoad=\"_scorm_loadPage()\" onbeforeunload=\"_scorm_unloadPage()\">\n");
//    else 
      buffer.append("  <body>\n");
    buffer.append("    <div role=\"button\" id=\"_topFrame\" style=\"text-align:center\"></div>\n");
    if (metadataCode!=null) buffer.append(metadataCode);
    buffer.append("      <script src=\""+name+".js\"></script>\n");
//    buffer.append("    </section>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    return buffer.toString();
  }

  /**
   * Generate only JS applying obfuscation and simplifier
   * 
   * @param limitedVersion
   * @param title
   * @param name
   * @param modelCode
   * @param viewCode
   * @param localPort 
   * 
   */
  public String generatePlainJS(String name, String modelCode, String viewCode, String localPort, String libraryPath, String codebasePath) {
    // JS library and Uglifier must have been defined
    if (!mUseFullLibrary) {
      if ((sJSlib == null) || (sUglifier == null)) return "";
    }
    // not supported values null
    boolean hasModel = true;  // model code is not null
    if (modelCode == null) { 
      modelCode = ""; 
      hasModel = false; 
    }
    if (viewCode == null) viewCode = "";

    String script="";
    if (mUseFullLibrary) {
      switch(mObfuscationLevel) {
        case OPEN: // not obfuscation, not ugly, simplified
        case HALF: // obfuscated, not ugly, simplified
          script = modelCode + viewCode +
                "\n\n" + startUpScript(name, libraryPath, codebasePath, hasModel, localPort);
          break;
        case FULL: // obfuscated, ugly, simplified
          script = uglify(sUglifier, modelCode) +uglify(sUglifier, viewCode) +
                "\n\n" + startUpScript(name, libraryPath, codebasePath, hasModel, localPort);              
          break;        
      }
    }
    else {
    // get dependence classes in code
      Set<String> dependence = getClasses(modelCode, viewCode);
      switch(mObfuscationLevel) {
        case OPEN: // not obfuscation, not ugly, simplified
          script = simplifier(sJSlib,dependence) + "\n" + modelCode + viewCode +
                "\n\n" + startUpScript(name, libraryPath, codebasePath, hasModel, localPort);
          break;
        case HALF: // obfuscated, not ugly, simplified
          script = obfuscater(simplifier(sJSlib,dependence) + "\n" + modelCode + viewCode) +
                      "\n\n" +  
                      startUpScript(name, libraryPath, codebasePath, hasModel, localPort);              
          break;
        case FULL: // obfuscated, ugly, simplified
          script = obfuscater(simplifier(sJSlib,dependence) + "\n" + 
                  uglify(sUglifier, modelCode) + 
                uglify(sUglifier, viewCode)) +
                      "\n\n" +  
                      startUpScript(name, libraryPath, codebasePath, hasModel, localPort);              
          break;        
      }
    }
    return script;
  }

  // ----------------------------------
  // private static methods
  //----------------------------------
  
  /**
   * Create startUp code
   */
  static private String startUpScript(String name, String libraryPath, String codebasePath, boolean hasModel, String localPort) {
      String targetName = hasModel ? "_model" : "_view";
	    StringBuffer buffer = new StringBuffer();
//    buffer.append("      window.addEventListener('load', function () { " + startUpScript(name, libraryPath, codebasePath, hasModel, localPort) + " }, false);\n"); 
	    buffer.append("      var "+targetName+";\n");
      buffer.append("      var _scorm;\n");
	    buffer.append("      window.addEventListener('load',\n");
	    buffer.append("        function () { \n");
	    buffer.append("          "+targetName+" = "); // + startUpScript(name, libraryPath, codebasePath, hasModel, localPort));
	    if (hasModel) buffer.append (" new " + name+ "(\"_topFrame\",");
	    else {
	      buffer.append (" new " + name+ "_View(\"_topFrame\",");
	      if (localPort!=null) buffer.append("\""+localPort+"\",-1,");
	      else                 buffer.append("-1,");
	    }
	    if (codebasePath==null) buffer.append("\""+libraryPath+"/\",null);\n");
	    else try {
	      buffer.append("\""+ (new File(libraryPath)).toURI().toURL().toExternalForm() + "\",\""+ (new File(codebasePath)).toURI().toURL().toExternalForm()   + "\");\n");
	    } catch (MalformedURLException e) {
	      e.printStackTrace();
	      buffer.append("\"file://"+ libraryPath + "\",\"file://"+ codebasePath  + "\");\n");
	    }
	    buffer.append("          if (typeof _isApp !== \"undefined\" && _isApp) "+targetName+".setRunAlways(true);\n");
        buffer.append("          TextResizeDetector.TARGET_ELEMENT_ID = '_topFrame';\n");
	    buffer.append("          TextResizeDetector.USER_INIT_FUNC = function () {\n");
	    buffer.append("            var iBase = TextResizeDetector.addEventListener(function(e,args) {\n");
	    buffer.append("              "+targetName+"._fontResized(args[0].iBase,args[0].iSize,args[0].iDelta);\n");
	    buffer.append("              },null);\n");
	    buffer.append("            "+targetName+"._fontResized(iBase);\n");
	    buffer.append("          };\n");
      buffer.append("          _model.onload();\n");
	    buffer.append("        }, false);\n");
      
	    return buffer.toString();
  }
    
  /**
   * Replace global vars 
   */    
  private String obfuscater(String JSLib) {
    if (mObfuscationLevel==Level.OPEN) return JSLib;
    for (int i=0; i<GLOBALVARS.length; i++) {
      String entry = GLOBALVARS[i];
      String replacement = "_" + i;
      JSLib = JSLib.replaceAll(Pattern.quote(".") + entry, "." + replacement);
    }    	    	
    return JSLib;
  }

  /**
   * Remove classes not used
   */
  private String simplifier(String JSLib, Set<String> dependence) {
    if (mObfuscationLevel==Level.OPEN) return JSLib;
    // remove blanks with 'equals'
    JSLib = JSLib.replaceAll("\\s+" + Pattern.quote("=") + "\\s*", "=");    

    ArrayList<String> excluded = getExclusion(dependence);
    for (String entry : excluded) {
      // small letter
      JSLib = removeEntry(JSLib,entry + "=");
      // capital letter
      int ix = entry.indexOf(".");
      char[] chars = entry.toCharArray();
      chars[ix+1] = Character.toUpperCase(chars[ix+1]); 
      JSLib = removeEntry(JSLib, String.valueOf(chars) + "=");
    }    	
    return JSLib;
  }

  /**
   * Find and remove a class
   */
  static private String removeEntry(String lib, String entry) {
    int i = lib.indexOf(entry);
    if (i > 0) { 
      int ex = i; 	// end index
      int count = 0;
      do {
        int end = lib.indexOf("}", ex);
        int init = lib.indexOf("{", ex);
        if ((end < init) || (init == -1)) { 
          count--; 
          ex = end + 1; 
        } else { 
          count++; 
          ex = init + 1; 
        }
      } while (count > 0);

      int ix = lib.indexOf("{", i - 1); // first "{"
      return lib.substring(0, ix + 1) + lib.substring(ex - 1); 
    }
    return lib;
  }

  /**
   * Get list of classes to remove
   */
  static private ArrayList<String> getExclusion(Set<String> dependence) {

    Map<String,Map<String,String[]>> listOfDependencies = new java.util.HashMap<String,Map<String,String[]>>();
    listOfDependencies.put("EJSS_GRAPHICS.",sTokens.DEPENDENCIES.EJSS_GRAPHICS);
    listOfDependencies.put("EJSS_SVGGRAPHICS.",sTokens.DEPENDENCIES.EJSS_SVGGRAPHICS);
    listOfDependencies.put("EJSS_DRAWING2D.",sTokens.DEPENDENCIES.EJSS_DRAWING2D);
    listOfDependencies.put("EJSS_WEBGLGRAPHICS.",sTokens.DEPENDENCIES.EJSS_WEBGLGRAPHICS);
    listOfDependencies.put("EJSS_DRAWING3D.",sTokens.DEPENDENCIES.EJSS_DRAWING3D);
    listOfDependencies.put("EJSS_INTERFACE.",sTokens.DEPENDENCIES.EJSS_INTERFACE);

    ArrayList<String> excluded = new ArrayList<String>();	

    // Build dependencies
    int counter = 0;
    int rounds = 0;
    do {
      counter = dependence.size();
      rounds++;
      for (Map.Entry<String,Map<String,String[]>> dep : listOfDependencies.entrySet()) {
        for (Map.Entry<String, String[]> entry : dep.getValue().entrySet()) {
          if (dependence.contains(dep.getKey() + entry.getKey())) dependence.addAll(Arrays.asList(entry.getValue()));
        }     
      }
    } while (counter!=dependence.size() && rounds<10);
    if (rounds>=10) System.err.println ("Warning! Dependencies check took too many rounds!");
//    else System.err.println ("Info: Dependencies check took "+rounds+ " rounds.");

    // Remove unused
    for (Map.Entry<String,Map<String,String[]>> dep : listOfDependencies.entrySet()) {
      for (Map.Entry<String, String[]> entry : dep.getValue().entrySet()) {
        String classname = dep.getKey() + entry.getKey();
        if(!dependence.contains(classname)) {
//          System.err.println("Removing class from library: "+classname);
          excluded.add(classname);
        }
//        else System.err.println("Leaving class: "+classname);
      }
    }

    
//    // get excluded from 2D
//    for (Map.Entry<String, String[]> entry : dep2D.entrySet()) {
//      if(!dependence.contains("EJSS_DRAWING2D." + entry.getKey())) excluded.add("EJSS_DRAWING2D." + entry.getKey());
//    }    	
//
//    // get excluded from 3D
//    for (Map.Entry<String, String[]> entry : dep3D.entrySet()) {
//      if(!dependence.contains("EJSS_DRAWING3D." + entry.getKey())) excluded.add("EJSS_DRAWING3D." + entry.getKey());
//      else System.err.println("Adding class: "+"EJSS_DRAWING3D." + entry.getKey());
//    }    	

//    // get excluded from EJSS_GRAPHICS
//    for (String entry : sTokens.ELEMENTS.EJSS_GRAPHICS) {
//      if(!dependence.contains("EJSS_GRAPHICS." + entry)) excluded.add("EJSS_GRAPHICS." + entry);
//    }    	
//
//    // get excluded from EJSS_SVGGRAPHICS
//    for (String entry : sTokens.ELEMENTS.EJSS_SVGGRAPHICS) {
//      if(!dependence.contains("EJSS_SVGGRAPHICS." + entry)) excluded.add("EJSS_SVGGRAPHICS." + entry);
//    }    	

//    // get excluded from EJSS_WEBGLGRAPHICS
//    for (String entry : sTokens.ELEMENTS.EJSS_WEBGLGRAPHICS) {
//      if(!dependence.contains("EJSS_WEBGLGRAPHICS." + entry)) excluded.add("EJSS_WEBGLGRAPHICS." + entry);
//      else System.err.println("Adding class: "+"EJSS_WEBGLGRAPHICS." + entry);
//    }    	
//
//    // get excluded from EJSS_INTERFACE 	
//    for (String entry : sTokens.ELEMENTS.EJSS_INTERFACE) {
//      if(!dependence.contains("EJSS_INTERFACE." + entry)) excluded.add("EJSS_INTERFACE." + entry);
//    }    	

    return excluded;
  }


  /*
   * Get clases used in view and model
   */
  static private Set<String> getClasses(String JSmodel, String JSview) {
    Set<String> list = new HashSet<String>();
    Matcher matcher;

    // get list for EJSS_DRAWING2D
    matcher = Pattern.compile("EJSS_DRAWING2D.(\\w+)").matcher(JSmodel);
    while (matcher.find()) list.add(matcher.group());
    matcher = Pattern.compile("EJSS_DRAWING2D.(\\w+)").matcher(JSview);
    while (matcher.find()) list.add(matcher.group());

    // get list for EJSS_DRAWING3D
    matcher = Pattern.compile("EJSS_DRAWING3D.(\\w+)").matcher(JSmodel);
    while (matcher.find()) list.add(matcher.group());
    matcher = Pattern.compile("EJSS_DRAWING3D.(\\w+)").matcher(JSview);
    while (matcher.find()) list.add(matcher.group());

    // get list for EJSS_INTERFACE    
    matcher = Pattern.compile("EJSS_INTERFACE.(\\w+)").matcher(JSmodel);
    while (matcher.find()) list.add(matcher.group());
    matcher = Pattern.compile("EJSS_INTERFACE.(\\w+)").matcher(JSview);
    while (matcher.find()) list.add(matcher.group());

    return list;   	
  }
  
  /**
   * Get code into tag <script> 
   */
  static private String betweenScript(String code) {
	return "<script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n" + code + "\n//--><!]]></script>";
  }  
  
  /**
   * Uglify all code
   */
  private static String uglify(JSUglify uglifier, String code) {
    uglifier.uglify("EJsS", code);
    return uglifier.getOutput();
  }
  
  /** 
   * Uglify one function in code
   */
  @SuppressWarnings("unused")
  private static String uglifyFunction(JSUglify uglifier, String code, String funcname) {
    int i = 0, j = -1, f = -1;
    while (i>j) {
      f = code.indexOf(funcname,f + 1);		  
      i = code.indexOf("{",f); // init '{' // declare function
      j = code.indexOf(";",f); // init ';' // call function
    }

    // find last '}'
    int ex = i; 	// end index
    if (i > 0) { 
      int count = 0;
      do {
        int end = code.indexOf("}", ex);
        int init = code.indexOf("{", ex);
        if ((end < init) || (init == -1)) { 
          count--; 
          ex = end + 1; 
        } else { 
          count++; 
          ex = init + 1; 
        }
      } while (count > 0);
    }

    String uglified = code.substring(i + 1,ex -1); 
    uglifier.uglify("EJsS", uglified);
    uglified = uglifier.getOutput();        

    String output = code.substring(0, i + 1) + uglified + code.substring(ex - 1);

    return output;
  }

  /*
   * Read file with dependence tree
   */
  static private Tokens readTokens(String tokensPath) {    	
    Resource res = ResourceLoader.getResource(tokensPath);
    if (res != null) { 
	    BufferedReader bufferedReader = res.openReader(); 
	    try {
	      // Find the file and open a reader
	      // get tokens from Json file
	      final Type tipoListFolders = new TypeToken<Tokens>(){}.getType();
	      Tokens tokens = new Gson().fromJson(bufferedReader, tipoListFolders);		        
	      bufferedReader.close();
	      return tokens;
	    } catch (IOException e1) { 
	      e1.printStackTrace();
	      try { bufferedReader.close(); } catch (IOException e) { }
	    }
    }
    return null;
  }    

  static private class Tokens {
    Dependencies DEPENDENCIES;    	
  }

  static private class Dependencies {
    Map<String,String[]> EJSS_GRAPHICS;
    Map<String,String[]> EJSS_SVGGRAPHICS;
    Map<String,String[]> EJSS_DRAWING2D;
    Map<String,String[]> EJSS_WEBGLGRAPHICS;
    Map<String,String[]> EJSS_DRAWING3D;
    Map<String,String[]> EJSS_INTERFACE;
  }

}
