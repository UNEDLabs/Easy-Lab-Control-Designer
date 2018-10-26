package org.colos.ejss.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.colos.ejs.osejs.utils.FileUtils;
import org.w3c.dom.Element;

public class SimulationXML extends BasicElement {
  // ------------------------------------
  // Static part
  //-------------------------------------
  static private final String sEJSS_TAG             = "EjsS";
  static private final String sEJSS_VERSION         = "1.1";
  static private final String sINFORMATION_TAG      = "information";
  static private final String sDESCRIPTION_TAG      = "description";
  static private final String sMODEL_TAG            = "model";
  static private final String sVIEW_TAG             = "view";
  static private final String sPAGE_TAG             = "page";
  static private final String sMODEL_CONFIG_TAG     = "configuration";
  static private final String sVARIABLES_TAG        = "variables";
  static private final String sVARIABLE_TAG         = "variable";
  static private final String sINITIALIZATION_TAG   = "initialization";
  static private final String sEVOLUTION_TAG        = "evolution";
  static private final String sFIXED_RELATIONS_TAG  = "fixed_relations";
  static private final String sCUSTOM_CODE_TAG      = "custom_code";
  static private final String sELEMENT_TAG          = "element";
  static private final String sELEMENT_PROPERTY_TAG = "property";

  static public enum INFORMATION { TITLE, AUTHOR, KEYWORDS, EXECUTION_PASSWORD, LEVEL, LANGUAGE, ABSTRACT, REQUIRED_FILES } 
  static public enum MODEL       { FRAMES_PER_SECOND, STEPS_PER_DISPLAY, REAL_TIME_VARIABLE, AUTOSTART } 
  static public enum ODE         { INDEPENDENT_VARIABLE, INCREMENT, EQUATION, 
    METHOD, TOLERANCE, INTERNAL_STEP, MEMORY_LENGTH, MAXIMUM_STEP, MAXIMUM_NUMBER_OF_STEPS, ABSOLUTE_TOLERANCE, RELATIVE_TOLERANCE,
    ACCELERATION_INDEPENDENT_OF_VELOCITY, FORCE_SYNCHRONIZATION, USE_BEST_INTERPOLATION, EVENT_MAXIMUM_STEP,
    PRELIMINARY_CODE, ERROR_HANDLING, EVENTS, ZENO_EFFECT,
    DELAY_INITIAL_CONDITION, DELAY_LIST, DELAY_ADD_DISCONTINUITY, DELAY_INCIDENCE_MATRIX }
  static public enum PROPERTY    { CONSTANT, VARIABLE, EXPRESSION, ACTION }

  static private final String[] sSTYLE_SHEETS = { "css/ejss.css", "lib/jquery-ui-1.9.1.custom.min.css" };
  static private final String[] sSCRIPTS      = { "lib/jquery-1.8.3.js", "lib/jquery-ui-1.9.1.custom.min.js", "lib/ejsS.v1.max.js" };

  // ------------------------------------
  // Non-static part
  // ------------------------------------
  
  private String mName;
  private Element mInformation, mDescription, mModel, mView;
  private Element mModelSetup, mModelVariables, mModelInitialization, 
                  mModelEvolution, mModelFixedRelations, mModelCustomCode;

	/**
	 * XML Builder
	 * @param rootName
	 */
	public SimulationXML(String name) {
	  super(sEJSS_TAG);
	  mName = name;
	  addTextElement("version",sEJSS_VERSION);
		mInformation = addElement(sINFORMATION_TAG);
    mDescription = addElement(sDESCRIPTION_TAG);
    mModel = addElement(sMODEL_TAG);
    mModelSetup          = addElement(mModel,sMODEL_CONFIG_TAG);
    mModelVariables      = addElement(mModel,sVARIABLES_TAG);
    mModelInitialization = addElement(mModel,sINITIALIZATION_TAG);
    mModelEvolution      = addElement(mModel,sEVOLUTION_TAG);
    mModelFixedRelations = addElement(mModel,sFIXED_RELATIONS_TAG);
    mModelCustomCode     = addElement(mModel,sCUSTOM_CODE_TAG);
    mView = addElement(sVIEW_TAG);
	}
	
  public void readFromFile(String filename) {
    if (super.readRootElement(filename,sEJSS_TAG)) {
      mInformation = getElement(sINFORMATION_TAG);
      mDescription = getElement(sDESCRIPTION_TAG);
      mModel = getElement(sMODEL_TAG);
      mModelSetup          = getElement(mModel,sMODEL_CONFIG_TAG);
      mModelVariables      = getElement(mModel,sVARIABLES_TAG);
      mModelInitialization = getElement(mModel,sINITIALIZATION_TAG);
      mModelEvolution      = getElement(mModel,sEVOLUTION_TAG);
      mModelFixedRelations = getElement(mModel,sFIXED_RELATIONS_TAG);
      mModelCustomCode     = getElement(mModel,sCUSTOM_CODE_TAG);
      mView = getElement(sVIEW_TAG);
    }
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(mName+"\n");
    buffer.append("  Information.title = "+getInformation(INFORMATION.TITLE)+"\n");
    buffer.append("  Information.abstract = "+getInformation(INFORMATION.ABSTRACT)+"\n");
    for (Element page : getElementList(mDescription,sPAGE_TAG)) {
      String name = evaluateNode(page, "name");
      String type = evaluateNode(page,"type");
      buffer.append("  Description.page: type = "+ type+", name = "+name+"\n");
    }
    buffer.append("  Model.fps = "+getModelConfiguration(MODEL.FRAMES_PER_SECOND)+"\n");
    return buffer.toString();
  }
  
  // ------------------------------------
  // Information
  //-------------------------------------

  private String informationTagname(INFORMATION tag) {
    switch (tag) {
      case TITLE              : return "title";
      case AUTHOR             : return "author";
      case KEYWORDS           : return "keywords";
      case EXECUTION_PASSWORD : return "execution_password";
      case LEVEL              : return "level";
      case LANGUAGE           : return "language";
      case ABSTRACT           : return "abstract";
      case REQUIRED_FILES     : return "required_files";
      default: return null;
    }
  }
  
  public void setInformation(INFORMATION tag, String value) {
    String tagName = informationTagname(tag);
    if (tagName!=null) {
      removeNode(mInformation,tagName);
      switch (tag) {
        case REQUIRED_FILES : 
          addTextElement(mInformation,tagName,value);
          break;
        default : 
          addCDATAElement(mInformation,tagName,value);
          break;
      }
    }
  }
  
  public String getInformation(INFORMATION tag) {
    String tagName = informationTagname(tag);
    if (tagName!=null) return super.evaluateNode(mInformation,tagName);
    return null;
  }
  
  // ------------------------------------
  // Description
  //-------------------------------------
  
  public void addDescriptionPage(String locale, String title, String code) {
    Element page = addElement(mDescription,sPAGE_TAG);
    addTextElement(page,"type","html");
    addTextElement(page,"locale",locale);
    addCDATAElement(page,"title",title);
    addCDATAElement(page,"code",code);
  }

  public void addDescriptionExternalPage(String locale, String title, String filename) {
    Element page = addElement(mDescription,sPAGE_TAG);
    addTextElement(page,"type","htmlFile");
    addTextElement(page,"locale",locale);
    addCDATAElement(page,"title",title);
    addCDATAElement(page,"filename",filename);
  }

  // ------------------------------------
  // Description
  //-------------------------------------

  private String modelTagname(MODEL tag) {
    switch (tag) {
      case FRAMES_PER_SECOND  : return "frames_per_second";
      case STEPS_PER_DISPLAY  : return "steps_per_display";
      case REAL_TIME_VARIABLE : return "real_time_variable";
      case AUTOSTART          : return "autostart";
      default:     return null;
    }
  }
  
  public void setModelConfiguration(MODEL tag, String value) {
    String tagName = modelTagname(tag);
    if (tagName!=null) {
      removeNode(mModelSetup,tagName);
      addTextElement(mModelSetup,tagName,value);
    }
  }

  public String getModelConfiguration(MODEL tag) {
    String tagName = modelTagname(tag);
    if (tagName!=null) return super.evaluateNode(mModelSetup,tagName);
    return null;
  }

  public Element addModelVariablesPage(String name, String comment) {
    Element page = addElement(mModelVariables,sPAGE_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    return page;
  }
  
  public void addModelVariable(Element page, String name, String type, String dimension, String value, String comment) {
    if (page.getNodeName()!=sPAGE_TAG || page.getParentNode()!=mModelVariables) {
      System.err.println ("SimulationXML error: Trying to add variable <"+name+"> to non variable page: "+page.getNodeName());
      return;
    }
    Element variable = addElement(page,sVARIABLE_TAG);
    addTextElement(variable,"name",name);
    addTextElement(variable,"type",type);
    addTextElement(variable,"dimension",dimension);
    addCDATAElement(variable,"value",value);
    addCDATAElement(variable,"comment",comment);
  }
  
  private Element addModelPage(Element modelPart, String name, String comment, String code) {
    Element page = addElement(modelPart,sPAGE_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    if (code!=null) addCDATAElement(page,"code",code);
    return page;
  }

  public void addModelInitializationPage(String name, String comment, String code) {
    addModelPage(mModelInitialization,name,comment,code);
  }

  public void addModelEvolutionPage(String name, String comment, String code) {
    Element page = addModelPage(mModelEvolution,name,comment,code);
    addTextElement(page,"type","code");
  }

  public Element addModelEvolutionODE(String name, String comment) {
    Element page = addModelPage(mModelEvolution,name,comment,null);
    addTextElement(page,"type","ode");
    return page;
  }

  private boolean isODE(Element element) {
    if (element.getNodeName()!=sPAGE_TAG || element.getParentNode()!=mModelEvolution) return false;
    return "ode".equals(evaluateNode(element,"type"));
  }
  
//  static public enum ODE         { INDEPENDENT_VARIABLE, INCREMENT, EQUATION, 
//    METHOD, TOLERANCE, INTERNAL_STEP, MEMORY_LENGTH, MAXIMUM_STEP, MAXIMUM_NUMBER_OF_STEPS, ABSOLUTE_TOLERANCE, RELATIVE_TOLERANCE,
//    ACCELERATION_INDEPENDENT_OF_VELOCITY, FORCE_SYNCHRONIZATION, USE_BEST_INTERPOLATION, EVENT_MAXIMUM_STEP,
//    PRELIMINARY_CODE, ERROR_HANDLING, EVENTS, ZENO_EFFECT,
//    DELAY_INITIAL_CONDITION, DELAY_LIST, DELAY_ADD_DISCONTINUITY, DELAY_INCIDENCE_MATRIX }

  public void setODEConfiguration(Element ode, ODE tag, String value, String secondValue) {
    if (isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to configure non ODE element: "+ode.getNodeName());
      return;
    }
    switch (tag) {
      case INDEPENDENT_VARIABLE : addTextElement(ode, "independent_variable",value); break;
      case INCREMENT            : addTextElement(ode, "increment",value); break;
      default: break; // do nothing
    }
  }

  public void addModelFixedRelationsPage(String name, String comment, String code) {
    addModelPage(mModelFixedRelations,name,comment,code);
  }

  public void addModelCustomCodePage(String name, String comment, String code) {
    addModelPage(mModelCustomCode,name,comment,code);
  }
  
  // ------------------------------------
  // View
  //-------------------------------------

  private String propertyTagname(PROPERTY tag) {
    switch (tag) {
      case CONSTANT   : return "constant";
      case VARIABLE   : return "variable";
      case EXPRESSION : return "expression";
      case ACTION     : return "action";
      default:     return null;
    }
  }

  private PROPERTY toPropertyTag(String tagname) {
    if ("constant".equals(tagname))   return PROPERTY.CONSTANT;
    if ("variable".equals(tagname))   return PROPERTY.VARIABLE;
    if ("expression".equals(tagname)) return PROPERTY.EXPRESSION;
    if ("action".equals(tagname))     return PROPERTY.ACTION;
    return PROPERTY.CONSTANT;
  }

  public Element addViewElement(String type, String name, String parent) {
    Element element = addElement(mView,sELEMENT_TAG);
    addTextElement(element, "type",type);
    addCDATAElement(element,"name",name);
    addCDATAElement(element,"parent",parent);
    return element;
  }
  
  public void setViewElementProperty(Element element, PROPERTY type, String name, String value) {
    if (element.getNodeName()!=sELEMENT_TAG || element.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to add property <"+name+"> to non view element: "+element.getNodeName());
      return;
    }
    Element property = addElement(element,sELEMENT_PROPERTY_TAG);
    String tagName = propertyTagname(type);
    addTextElement(property,"type",tagName);
    addCDATAElement(property,"name",name);
    addCDATAElement(property,"value",value);
  }

  // ------------------------------------
  // HTML generation
  //-------------------------------------

  public String toSimulationHTML(String librariesPath, String codebase) {
    StringBuffer buffer = new StringBuffer();
    File libDir = new File(librariesPath);
    buffer.append("<html>\n");
    buffer.append("  <head>\n");
//    buffer.append("    <meta charset=\"UTF-8\">\n");
    buffer.append("    <title>"+getInformation(INFORMATION.TITLE)+"</title>\n");
    
    buffer.append("    <style type=\"text/css\">\n");
    for (String styleSheet : sSTYLE_SHEETS) buffer.append(readTextFile(new File(libDir,styleSheet)));
    buffer.append("    </style>\n");
    
    for (String scriptFile : sSCRIPTS) { 
      buffer.append("    <script type=\"text/javascript\">\n");
      buffer.append(readTextFile(new File(libDir,scriptFile)));
      buffer.append("    </script>\n");
    }

//    buffer.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+librariesPath+"/css/ejss.css\"></link>\n");
//    buffer.append("    <link rel=\"stylesheet\" href=\""+librariesPath+"/lib/jquery-ui-1.9.1.custom.min.css\" />\n");
//    buffer.append("    <script src=\""+librariesPath+"/lib/jquery-1.8.3.js\"></script>\n");
//    buffer.append("    <script src=\""+librariesPath+"/lib/jquery-ui-1.9.1.custom.min.js\"></script>\n");
//    buffer.append("    <script type=\"text/javascript\" src=\""+librariesPath+"/lib/ejsS.v1.js\"></script>\n");
    buffer.append("  </head>\n");
    buffer.append("  <body>\n");
//    buffer.append("    <h1>"+mName+"</h1>\n");
    buffer.append("    <div id=\"_topFrame\"> </div>\n");
    buffer.append("    <script type=\"text/javascript\">\n");
    addModel(buffer,codebase);
    buffer.append("    </script>\n");
    buffer.append("    <script type=\"text/javascript\">\n");
    addView(buffer);
    buffer.append("    </script>\n");
    buffer.append("    <script type=\"text/javascript\">\n");
    buffer.append("window.addEventListener('load', function () { new "+mName+"(); }, false);\n");
    buffer.append("    </script>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");

    return buffer.toString();
  }
  

  private void addModel(StringBuffer buffer, String codebase) {
    buffer.append("function "+ mName+"() {\n");
    buffer.append("  var _model = EJSS_CORE.createAnimationLMS();\n");
    buffer.append("  function _play() { _model.play(); }\n");
    buffer.append("  function _pause() { _model.pause(); }\n");
    buffer.append("  function _step() { _model.step(); }\n");
    buffer.append("  function _reset() { _model.reset(); }\n");
    buffer.append("  function _initialize() { _model.initialize(); }\n\n");

    // Variables declaration
    for (Element page : getElementList(mModelVariables,sPAGE_TAG)) {
      for (Element variable : getElementList(page,sVARIABLE_TAG)) {
        String name = evaluateNode(variable, "name"); 
        buffer.append("  var "+name+";\n");
      }
      buffer.append("\n");
    }
    // Variables reset
    for (Element page : getElementList(mModelVariables,sPAGE_TAG)) {
      buffer.append("  _model.addToReset(function() {\n");
      for (Element variable : getElementList(page,sVARIABLE_TAG)) {
        String name = evaluateNode(variable, "name"); 
        String value = evaluateNode(variable, "value");
        String dimension = evaluateNode(variable, "dimension");
        if (dimension!=null && dimension.trim().length()>0) {
          buffer.append("    "+name+" = new Array("+dimension+");\n");
          if (value!=null && value.trim().length()>0) buffer.append("    for (var _i=0;_i<"+dimension+"; _i++) "+name+"[_i] = "+value+";\n");
        }
        else {
          if (value!=null && value.trim().length()>0) buffer.append("    "+name+" = "+value+";\n");
        }
      }
      buffer.append("  })\n\n");
    }
    
    // Initialization
    for (Element page : getElementList(mModelInitialization,sPAGE_TAG)) {
      String name = evaluateNode(page,"name");
      String code = evaluateNode(page,"code");
      buffer.append("  _model.addToInitialization(function() {\n");
      splitCode(buffer,code,"Initialization."+name,"    ");
      buffer.append("  });\n\n");
    }
    
    // Evolution
    for (Element page : getElementList(mModelEvolution,sPAGE_TAG)) {
      String name = evaluateNode(page,"name");
      String code = evaluateNode(page,"code");
      String type = evaluateNode(page,"type");
      if ("code".equals(type)) {
        buffer.append("  _model.addToEvolution(function() {\n");
        splitCode(buffer,code,"Evolution."+name,"    ");
        buffer.append("  });\n\n");
      }
      else if ("ode".equals(type)) { // todo: ODE pages
      }
    }
    // Fixed relations
    for (Element page : getElementList(mModelFixedRelations,sPAGE_TAG)) {
      String name = evaluateNode(page,"name");
      String code = evaluateNode(page,"code");
      buffer.append("  _model.addToFixedRelations(function() {\n");
      splitCode(buffer,code,"FixedRelations."+name,"    ");
      buffer.append("  });\n\n");
    }
    // Custom code
    for (Element page : getElementList(mModelCustomCode,sPAGE_TAG)) {
      String name = evaluateNode(page,"name");
      String code = evaluateNode(page,"code");
      splitCode(buffer,code,"CustomCode."+name,"  ");
    }
    
    // View from the model
    buffer.append("\n");
    buffer.append("  _view = new "+mName+"_View();\n");
    buffer.append("  _view._setResourcePath(\"file://"+ codebase + "\");\n");
    buffer.append("  var _view_super_reset = _view._reset;\n\n");
    
    buffer.append("  _view._reset = function() {\n");
    buffer.append("    _view_super_reset();\n\n");
    
    for (Element element : getElementList(mView,sELEMENT_TAG)) { // for each view element
      String elementName = evaluateNode(element, "name");
      for (Element property : getElementList(element,sELEMENT_PROPERTY_TAG)) { // for each property
        String name  = evaluateNode(property, "name"); 
        String value = evaluateNode(property, "value"); 
        switch (toPropertyTag(evaluateNode(property, "type"))) {
          case CONSTANT : break; // Do nothing  
          case ACTION : 
            buffer.append("    _view."+elementName+".setAction(\""+name+"\", function() { "+ value+"; } );\n");
            break;
          case EXPRESSION : 
            buffer.append("    _view."+elementName+".linkProperty(\""+name+"\", ");
            if (value.indexOf("return")>=0) {
              buffer.append(" function() {\n");
              splitCode(buffer,value,elementName+"."+name,"      ");
              if (!value.trim().endsWith(";")) buffer.append("      ;");
              buffer.append("    }");
            }
            else buffer.append(" function() { return "+ value+"; } ");
            buffer.append(");\n");
            break;
          case VARIABLE : 
            buffer.append("    _view."+elementName+".linkProperty(\""+name+"\", ");
            buffer.append(" function() { return "+ value+"; },");
            buffer.append(" function(_v) { "+ value+" = _v; } ");
            buffer.append(");\n");
            break;
        } // end switch
      } // end for property
    } // for view element

    buffer.append("  };\n\n");
    buffer.append("  _model.setView(_view);\n");

    // Model parameters
    { 
      boolean isAutostart = false;
      String autoText = evaluateNode(mModelSetup,modelTagname(MODEL.AUTOSTART));
      if (autoText!=null) isAutostart = "true".equals(autoText.toLowerCase());
      String fpsText = evaluateNode(mModelSetup,modelTagname(MODEL.FRAMES_PER_SECOND));
      if (fpsText==null) fpsText = "10"; 
      String spdText = evaluateNode(mModelSetup,modelTagname(MODEL.STEPS_PER_DISPLAY));
      if (spdText==null) spdText = "1";
      buffer.append("  _model.setAutoplay("+isAutostart+");\n");
      buffer.append("  _model.setFPS("+fpsText+");\n");
      buffer.append("  _model.setStepsPerDisplay("+spdText+");\n");
    }
    
    // And that's it!
    buffer.append("  _model.reset();\n\n");
    buffer.append("  return _model;\n");
    buffer.append("};\n");
  }
  
  private void addView(StringBuffer buffer) {
    buffer.append(mName+"_View = function() {\n");
    buffer.append("  var _view = EJSS_CORE.createView(\"_topFrame\");\n\n");
    buffer.append("  _view._reset = function() {\n");
    buffer.append("    _view._clearAll();\n");
    
    for (Element element : getElementList(mView,sELEMENT_TAG)) { // for each view element
      String elementName  = evaluateNode(element, "name");
      String elementClass = evaluateNode(element, "type");
      String elementParent = evaluateNode(element, "parent");

      buffer.append("    _view._addElement("+elementClass+",\""+elementName+"\", _view."+elementParent+")");
      // For each constant property
      boolean firstTime = true;
      for (Element property : getElementList(element,sELEMENT_PROPERTY_TAG)) { // for each property
        if (toPropertyTag(evaluateNode(property, "type"))==PROPERTY.CONSTANT) {
          String name  = evaluateNode(property, "name"); 
          String value = evaluateNode(property, "value"); 
          if (firstTime) {
            buffer.append("\n      .setProperties({ "+name+":"+value);
            firstTime = false;
          }
          else buffer.append(", "+name+":"+value);
        }
      }
      if (firstTime) buffer.append(";\n\n");
      else           buffer.append(" });\n\n");
    }
    buffer.append("  };\n\n");
    buffer.append("  return _view;\n");
    buffer.append("};\n");
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
      Reader reader = new FileReader(_file);
      LineNumberReader l = new LineNumberReader(reader);
      StringBuffer buffer = new StringBuffer();
      String sl = l.readLine();
      while (sl != null) { buffer.append(sl+"\n"); sl = l.readLine(); }
      reader.close();
      return buffer.toString();
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Splits the code into nicer-looking lines and appends it to a buffer 
   * @param buffer
   * @param code
   * @param information
   * @param prefix
   */
  static public void splitCode (StringBuffer buffer, String code, String information, String prefix) {
//    int lineNumber = 1;
    StringTokenizer tkn = new StringTokenizer(code, "\n");
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      buffer.append(prefix + line);
//      if (information!=null) buffer.append("  // > " + information + ":" + lineNumber + "\n");
//      else 
        buffer.append("\n");
//      lineNumber++;
    }
  }
  
  
}
