package org.colos.ejss.xml;

import org.w3c.dom.Element;

public class SimulationXML extends BasicElement {
  // ------------------------------------
  // Static part
  //-------------------------------------
  static public final String sDEFAULT_LOCALE = "_default_";
  
  static private final String sEJSS_TAG             = "EJsS";
  static private final String sEJSS_VERSION         = "1.1";
  static private final String sINFORMATION_TAG      = "information";
  static private final String sDESCRIPTION_TAG      = "description";
  static private final String sMODEL_TAG            = "model";
  static private final String sVIEW_TAG             = "view";
  
  static         final String sPAGE_TAG             = "page";
  static private final String sLOCALIZATION_TAG     = "localization";
  
  static private final String sMODEL_CONFIG_TAG     = "configuration";
  static private final String sVARIABLES_TAG        = "variables";
  static         final String sVARIABLE_TAG         = "variable";
  static private final String sINITIALIZATION_TAG   = "initialization";
  static private final String sEVOLUTION_TAG        = "evolution";
  static private final String sFIXED_RELATIONS_TAG  = "fixed_relations";
  static private final String sCUSTOM_CODE_TAG      = "custom_code";
  static private final String sELEMENTS_TAG         = "elements";
  static private final String sELEMENT_TAG          = "element";
  static         final String sELEMENT_PROPERTY_TAG = "property";
  static private final String sODE_EQUATION_TAG     = "ode_equation";
  static private final String sODE_EVENT_TAG        = "ode_event";
  static private final String sODE_DISCONTINUITY_TAG= "ode_discontinuity";
  static private final String sODE_ERROR_TAG        = "ode_error";
  static private final String sACTION_TAG           = "action";
  static private final String sROOT_ELEMENT_TAG     = "root_element";

  static public enum INFORMATION { TITLE, AUTHOR, COPYRIGHT, KEYWORDS, EXECUTION_PASSWORD, LEVEL, LANGUAGE, ABSTRACT, REQUIRED_FILES, 
//                                   BASE64_IMAGES, EXTRA_CSS_FILES, 
                                   MODEL_TAB, MODEL_TAB_TITLE, 
                                   AUTHOR_IMAGE, LOGO_IMAGE, LOCALES_SUPPORTED, AUTOSELECT_VIEW, HTMLHEAD } // MENU_LOCATION,  
  static public enum MODEL       { FRAMES_PER_SECOND, STEPS_PER_DISPLAY, REAL_TIME_VARIABLE, AUTOSTART, RUNNING_MODE, PAUSE_ON_PAGE_EXIT} 
  static public enum ODE         { 
    INDEPENDENT_VARIABLE, INCREMENT, PRELIMINARY_CODE, PRELIMINARY_CODE_COMMENT,
    SOLVER_METHOD, ABSOLUTE_TOLERANCE, 
    ACCELERATION_INDEPENDENT_OF_VELOCITY, FORCE_SYNCHRONIZATION, USE_BEST_INTERPOLATION, ESTIMATE_FIRST_STEP, 
    HISTORY_LENGTH, INTERNAL_STEP, MAXIMUM_STEP, MAXIMUM_NUMBER_OF_STEPS,  RELATIVE_TOLERANCE,
    DELAY_LIST, DELAY_MAXIMUM, DELAY_DISCONTINUITIES, DELAY_INITIAL_CONDITION, DELAY_COMMENT,
    MAXIMUM_EVENT_STEP, 
    ZENO_ACTION, ZENO_END_STEP, ZENO_COMMENT
  }
  
  static public enum ODE_EVENT   { 
    TYPE, ITERATIONS, METHOD,  
    ZERO_CONDITION, TOLERANCE,
    ACTION, END_AT_EVENT
  }

  static public enum ODE_DISCONTINUITY   { 
    ZERO_CONDITION, TOLERANCE, ACTION, END_AT_DISCONTINUITY
  }

  static public enum PROPERTY    { CONSTANT, VARIABLE, EXPRESSION, ACTION }

  // ------------------------------------
  // Non-static part
  // ------------------------------------
  
  private String mName;
  private String mServerLocalPort=null;
  private boolean mIsViewOnly=false;
  private boolean mInsideEJS=false;
  private Element mInformation, mDescription, mModel, mView;
  private Element mModelSetup, mModelVariables, mModelInitialization, 
                  mModelEvolution, mModelFixedRelations, mModelCustomCode,
                  mModelElements;

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
    mModelElements       = addElement(mModel,sELEMENTS_TAG);
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
      mModelElements       = getElement(mModel,sELEMENTS_TAG);
      mView = getElement(sVIEW_TAG);
    }
  }
  
  public String getName() { return mName; }
    
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

  public String toJSON() {
//    Simulation sim = new Simulation(mName);
//    return sim.toJSON(); 
    
    String xmlString = super.toXMLString();
    org.json.JSONObject xmlJSONObj = org.json.XML.toJSONObject(xmlString);
    return xmlJSONObj.toString(2);
  }

  public void setInsideEJS() { 
    mInsideEJS = true;
  }

  public boolean isInsideEJS() { 
    return mInsideEJS;
  }

  public void setViewOnly(String localPort) { 
    mIsViewOnly = true;
    mServerLocalPort = localPort;
  }

  public boolean isViewOnly() { return mIsViewOnly; }
  
  public String getServerLocalPort() { return mServerLocalPort; }
  
  // ------------------------------------
  // Information
  //-------------------------------------

  private String informationTagname(INFORMATION tag) {
    switch (tag) {
      case TITLE              : return "title";
      case AUTHOR             : return "author";
      case COPYRIGHT          : return "copyright";
      case KEYWORDS           : return "keywords";
      case EXECUTION_PASSWORD : return "execution_password";
      case LEVEL              : return "level";
      case LANGUAGE           : return "language";
      case ABSTRACT           : return "abstract";
      case REQUIRED_FILES     : return "required_files";
//      case EXTRA_CSS_FILES    : return "extra_css_files";
//      case BASE64_IMAGES      : return "base64_images";
      case MODEL_TAB          : return "model_tab";       // An integer. A value < 0 (or absent) means you want to place the model tab at the end.
      case MODEL_TAB_TITLE    : return "model_tab_title"; // String
      case AUTHOR_IMAGE       : return "author_image";    // A file in the _data directory
      case LOGO_IMAGE         : return "logo_image";      // A file in the _data directory
      case LOCALES_SUPPORTED  : return "locales";         // A semicolon separated list, such as "_default_;es;fr"
//      case MENU_LOCATION      : return "menu_location";   // One of: TOP, BOTTOM, LEFT, RIGHT, ...
      case AUTOSELECT_VIEW    : return "autoselect_view"; // true or false. Whether the model should select the view that best matches the screen resolution
      case HTMLHEAD           : return "html_head";    
      default: return null;
    }
  }
  
  public void setInformation(INFORMATION tag, String value) {
    String tagName = informationTagname(tag);
    if (tagName!=null) {
      removeNode(mInformation,tagName);
      switch (tag) {
        case MODEL_TAB : 
        case REQUIRED_FILES : 
//        case EXTRA_CSS_FILES : 
        case AUTHOR_IMAGE : 
        case LOGO_IMAGE : 
//        case MENU_LOCATION : 
        case AUTOSELECT_VIEW :
          addTextElement(mInformation,tagName,value);
          break;
//        case BASE64_IMAGES :
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

  public Element addDescriptionPage(String name) {
    Element page = addElement(mDescription,sPAGE_TAG);
    if (name!=null) addCDATAElement(page,"name",name);
    return page;
  }

  public void addDescriptionLocalizedPage(Element page, String locale, String title, String code) {
    if (page.getNodeName()!=sPAGE_TAG || page.getParentNode()!=mDescription) {
      System.err.println ("SimulationXML error: Trying to add description <"+title+"> to non-description page: "+page.getNodeName());
      return;
    }
    Element localeEntry = addElement(page,sLOCALIZATION_TAG);
    addTextElement(localeEntry,"locale",locale);
    addTextElement(localeEntry,"type","html");
    addCDATAElement(localeEntry,"title",title);
    addCDATAElement(localeEntry,"code",code);
  }

  public void addDescriptionLocalizedExternalPage(Element page, String locale, String title, String filename) {
    if (page.getNodeName()!=sPAGE_TAG || page.getParentNode()!=mDescription) {
      System.err.println ("SimulationXML error: Trying to add description <"+title+"> to non-description page: "+page.getNodeName());
      return;
    }
    Element localeEntry = addElement(page,sLOCALIZATION_TAG);
    addTextElement(localeEntry,"locale",locale);
    addTextElement(localeEntry,"type","htmlFile");
    addCDATAElement(localeEntry,"title",title);
    addCDATAElement(localeEntry,"filename",filename);
  }

  public java.util.List<Element> getDescriptionPages() { return getElementList(mDescription,sPAGE_TAG); }

  public java.util.List<Element> getDescriptionPageLocalizations(Element page) { 
    if (page.getNodeName()!=sPAGE_TAG || page.getParentNode()!=mDescription) {
      return new java.util.ArrayList<Element>();
    }
    return getElementList(page,sLOCALIZATION_TAG); 
  }

  // ------------------------------------
  // Model
  //-------------------------------------

  private String modelTagname(MODEL tag) {
    switch (tag) {
      case FRAMES_PER_SECOND  : return "frames_per_second";
      case STEPS_PER_DISPLAY  : return "steps_per_display";
      case REAL_TIME_VARIABLE : return "real_time_variable";
      case AUTOSTART          : return "autostart";
      case PAUSE_ON_PAGE_EXIT : return "pause_on_page_exit";
      case RUNNING_MODE       : return "running_mode";
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

  public java.util.List<Element> getModelVariables() { return getElementList(mModelVariables,sPAGE_TAG); }

  public java.util.List<Element> getModelInitialization() { return getElementList(mModelInitialization,sPAGE_TAG); }

  public java.util.List<Element> getModelEvolution() { return getElementList(mModelEvolution,sPAGE_TAG); }

  public java.util.List<Element> getModelFixedRelations() { return getElementList(mModelFixedRelations,sPAGE_TAG); }

  public java.util.List<Element> getModelCustomCodePages() { return getElementList(mModelCustomCode,sPAGE_TAG); }

  public java.util.List<Element> getModelElements() { return getElementList(mModelElements,sELEMENT_TAG); }

  public Element addModelVariablesPage(String name, String comment) {
    Element page = addElement(mModelVariables,sPAGE_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    return page;
  }
  
  public void setEnabled(Element page, boolean enabledByDefault) {
    addTextElement(page,"enabled",enabledByDefault ? "true" : "false");
  }

  public boolean isEnabled(Element page) {
    String booleanStr = super.evaluateNode(page,"enabled");
    if (booleanStr!=null && booleanStr.toLowerCase().equals("false")) return false;
    return true;
  }

  public void addModelVariable(Element page, String name, String type, String dimension, String value, String comment, String domain) {
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
    addCDATAElement(variable,"domain",domain);
  }
  
  private Element addModelPage(Element modelPart, String name, String comment, String code) {
    Element page = addElement(modelPart,sPAGE_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    if (code!=null) addCDATAElement(page,"code",code);
    return page;
  }

  public void addModelInitializationPage(String name, String comment, String code, boolean enabled) {
    Element page = addModelPage(mModelInitialization,name,comment,code);
    setEnabled(page, enabled);
  }

  public void addModelEvolutionPage(String name, String comment, String code, boolean enabled) {
    Element page = addModelPage(mModelEvolution,name,comment,code);
    addTextElement(page,"type","code");
    setEnabled(page,enabled);
  }

  public void addModelFixedRelationsPage(String name, String comment, String code, boolean enabled) {
    Element page = addModelPage(mModelFixedRelations,name,comment,code);
    setEnabled(page,enabled);
  }

  public void addModelCustomCodePage(String name, String comment, String code, boolean enabled) {
    Element page = addModelPage(mModelCustomCode,name,comment,code);
    setEnabled(page,enabled);
  }

  public void addModelElement(String jsFile, String lintCode, String fullCode) {
    Element page = addElement(mModelElements,sELEMENT_TAG);
    addTextElement(page,"file",jsFile);
    addCDATAElement(page,"lintcode",lintCode);
    addCDATAElement(page,"code",fullCode);
  }

  // ------------------------------------
  // ODEs
  //-------------------------------------
  
  public Element addModelEvolutionODE(String name, String comment) {
    Element page = addModelPage(mModelEvolution,name,comment,null);
    addTextElement(page,"type","ode");
    return page;
  }
  
  private boolean isODE(Element element) {
    if (element.getNodeName()!=sPAGE_TAG || element.getParentNode()!=mModelEvolution) return false;
    return "ode".equals(evaluateNode(element,"type"));
  }
  
  private String odeTagname(ODE tag) {
    switch (tag) {
      case INDEPENDENT_VARIABLE    : return "independent_variable";
      case INCREMENT               : return "increment";
      case PRELIMINARY_CODE        : return "preliminary_code";
      case PRELIMINARY_CODE_COMMENT: return "preliminary_code_comment";
      
      case SOLVER_METHOD           : return "solver";
      case ABSOLUTE_TOLERANCE      : return "absolute_tolerance";
      
      case ACCELERATION_INDEPENDENT_OF_VELOCITY : return "acceleracion_independent_of_velocity";
      case FORCE_SYNCHRONIZATION   : return "force_synchronization";
      case USE_BEST_INTERPOLATION  : return "use_best_interpolation";
      case ESTIMATE_FIRST_STEP     : return "estimate_first_step";
      
      case HISTORY_LENGTH          : return "history_length";
      case INTERNAL_STEP           : return "internal_step";
      case MAXIMUM_STEP            : return "maximum_step";
      case MAXIMUM_NUMBER_OF_STEPS : return "maximum_number_of_steps";
      case RELATIVE_TOLERANCE      : return "relative_tolerance";
      
      case DELAY_LIST              : return "delay_list";
      case DELAY_MAXIMUM           : return "delay_maximum";
      case DELAY_DISCONTINUITIES   : return "delay_discontinuities";
      case DELAY_INITIAL_CONDITION : return "delay_initial_condition";
      case DELAY_COMMENT           : return "delay_comment";
      
      case MAXIMUM_EVENT_STEP      : return "maximum_event_step";
      
      case ZENO_ACTION             : return "zeno_action";
      case ZENO_END_STEP           : return "zeno_end_step";
      case ZENO_COMMENT            : return "zeno_comment";
      default: return null;
    }
  }
  
  public void setODEConfiguration(Element ode, ODE tag, String value) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to configure non ODE element: "+ode.getNodeName());
      return;
    }
    String tagName = odeTagname(tag);
    if (tagName!=null) {
      removeNode(ode,tagName);
      switch (tag) {
        case ACCELERATION_INDEPENDENT_OF_VELOCITY : 
        case FORCE_SYNCHRONIZATION : 
        case USE_BEST_INTERPOLATION : 
        case ESTIMATE_FIRST_STEP : 
        case SOLVER_METHOD :
        case ZENO_END_STEP :
          addTextElement(ode,tagName,value);
          break;
        default : 
          addCDATAElement(ode,tagName,value);
          break;
      }
    }
  }
  
  public String getODEConfiguration(Element ode, ODE tag) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to get configuration from non ODE element: "+ode.getNodeName());
      return null;
    }
    String tagName = odeTagname(tag);
    if (tagName!=null) return super.evaluateNode(ode,tagName);
    return null;
  }

  public void addODEEquation(Element ode, String state, String index, String rate) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add equation to non ODE element: "+ode.getNodeName());
      return;
    }
    Element odeLine = addElement(ode,sODE_EQUATION_TAG);
    addCDATAElement(odeLine,"state",state);
    if (index!=null) addCDATAElement(odeLine,"index",index);
    addCDATAElement(odeLine,"rate",rate);
  }
  
  public java.util.List<Element> getODEEquations(Element ode) { 
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add equation to non ODE element: "+ode.getNodeName());
      return new java.util.ArrayList<Element>();
    }
    return getElementList(ode,sODE_EQUATION_TAG); 
  }

  public void addODEErrorHandler(Element ode, String name, String type, String code, String comment, boolean enabled) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add error handler to non ODE element: "+ode.getNodeName());
      return;
    }
    Element page = addElement(ode,sODE_ERROR_TAG);
    addTextElement(page, "name",name); 
    addTextElement(page, "type",type); 
    addCDATAElement(page, "code", code); 
    addCDATAElement(page, "comment", comment); 
    setEnabled(page,enabled);
  }

  public java.util.List<Element> getODEErrorHandlers(Element ode) { 
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add equation to non ODE element: "+ode.getNodeName());
      return new java.util.ArrayList<Element>();
    }
    return getElementList(ode,sODE_ERROR_TAG); 
  }
  
  // ------------------------------------
  // Events
  //-------------------------------------
  
  public Element addODEEvent(Element ode, String name, String comment) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add event to non ODE element: "+ode.getNodeName());
      return null;
    }
    Element page = addElement(ode,sODE_EVENT_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    return page;
  }
  
  public java.util.List<Element> getODEEvents(Element ode) { 
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to get events from a non ODE element: "+ode.getNodeName());
      return new java.util.ArrayList<Element>();
    }
    return getElementList(ode,sODE_EVENT_TAG); 
  }


  private String eventTagname(ODE_EVENT tag) {
    switch (tag) {
      case TYPE           : return "type";
      case ITERATIONS     : return "iterations";
      case METHOD         : return "method";
      case ZERO_CONDITION : return "zero_condition";
      case TOLERANCE      : return "tolerance";
      case ACTION         : return "action";
      case END_AT_EVENT   : return "end_at_event";
      default: return null;
    }
  }
  
  public void setEventConfiguration(Element event, ODE_EVENT tag, String value) {
    if (event.getNodeName()!=sODE_EVENT_TAG) {
      System.err.println ("SimulationXML error: Trying to configure non event element: "+event.getNodeName());
      return;
    }
    String tagName = eventTagname(tag);
    if (tagName!=null) {
      removeNode(event,tagName);
      switch (tag) {
        case TYPE : 
        case METHOD : 
        case END_AT_EVENT : 
          addTextElement(event,tagName,value);
          break;
        default : 
          addCDATAElement(event,tagName,value);
          break;
      }
    }
  }
  
  public String getEventConfiguration(Element event, ODE_EVENT tag) {
    if (event.getNodeName()!=sODE_EVENT_TAG) {
      System.err.println ("SimulationXML error: Trying to get configuration from non ODE element: "+event.getNodeName());
      return null;
    }
    String tagName = eventTagname(tag);
    if (tagName!=null) return super.evaluateNode(event,tagName);
    return null;
  }
  
  // ------------------------------------
  // Discontinuities
  //-------------------------------------
  
  public Element addODEDiscontinuity(Element ode, String name, String comment) {
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to add discontinuity to non ODE element: "+ode.getNodeName());
      return null;
    }
    Element page = addElement(ode,sODE_DISCONTINUITY_TAG);
    addTextElement(page,"name",name);
    addCDATAElement(page,"comment",comment);
    return page;
  }
  
  public java.util.List<Element> getODEDiscontinuities(Element ode) { 
    if (!isODE(ode)) {
      System.err.println ("SimulationXML error: Trying to get discontinuities from non event element: "+ode.getNodeName());
      return new java.util.ArrayList<Element>();
    }
    return getElementList(ode,sODE_DISCONTINUITY_TAG); 
  }


  private String discontinuityTagname(ODE_DISCONTINUITY tag) {
    switch (tag) {
      case ZERO_CONDITION : return "zero_condition";
      case TOLERANCE      : return "tolerance";
      case ACTION         : return "action";
      case END_AT_DISCONTINUITY   : return "end_at_event";
      default: return null;
    }
  }
  
  public void setDiscontinuityConfiguration(Element discontinuity, ODE_DISCONTINUITY tag, String value) {
    if (discontinuity.getNodeName()!=sODE_DISCONTINUITY_TAG) {
      System.err.println ("SimulationXML error: Trying to configure non discontinuity element: "+discontinuity.getNodeName());
      return;
    }
    String tagName = discontinuityTagname(tag);
    if (tagName!=null) {
      removeNode(discontinuity,tagName);
      switch (tag) {
        case END_AT_DISCONTINUITY : 
          addTextElement(discontinuity,tagName,value);
          break;
        default : 
          addCDATAElement(discontinuity,tagName,value);
          break;
      }
    }
  }
  
  public String getDiscontinuityConfiguration(Element discontinuity, ODE_DISCONTINUITY tag) {
    if (discontinuity.getNodeName()!=sODE_DISCONTINUITY_TAG) {
      System.err.println ("SimulationXML error: Trying to get configuration from non discontinuity element: "+discontinuity.getNodeName());
      return null;
    }
    String tagName = discontinuityTagname(tag);
    if (tagName!=null) return super.evaluateNode(discontinuity,tagName);
    return null;
  }

  // ------------------------------------
  // View
  //-------------------------------------

  static private String propertyTagname(PROPERTY tag) {
    switch (tag) {
      case CONSTANT   : return "constant";
      case VARIABLE   : return "variable";
      case EXPRESSION : return "expression";
      case ACTION     : return "action";
      default:     return null;
    }
  }

  static PROPERTY toPropertyTag(String tagname) {
    if ("constant".equals(tagname))   return PROPERTY.CONSTANT;
    if ("variable".equals(tagname))   return PROPERTY.VARIABLE;
    if ("expression".equals(tagname)) return PROPERTY.EXPRESSION;
    if ("action".equals(tagname))     return PROPERTY.ACTION;
    return PROPERTY.CONSTANT;
  }

  public Element addView(String name, String width, String height) {
    Element view = addElement(mView,sPAGE_TAG);
    addCDATAElement(view,"name",name);
    addTextElement(view,"width",width);
    addTextElement(view,"height",height);
    return view;
  }

  public java.util.List<Element> getViews() {
    return getElementList(mView,sPAGE_TAG);
  }

  java.util.List<Element> getViewVariables(Element view) {
    return getElementList(view,sVARIABLE_TAG);
  }

  java.util.List<Element> getViewActions(Element view) {
    return getElementList(view,sACTION_TAG);
  }

  java.util.List<Element> getViewElements(Element view) {
    return getElementList(view,sELEMENT_TAG);
  }
  
  public String getViewName(Element view) { return evaluateNode(view, "name"); }
  public String getViewWidth(Element view) { return evaluateNode(view, "width"); }
  public String getViewHeight(Element view) { return evaluateNode(view, "height"); }

  public Element registerViewVariable(Element view, String name, String value, boolean inputOnly) {
    if (view.getNodeName()!=sPAGE_TAG || view.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to register variable <"+name+"> to non view: "+view.getNodeName());
      return null;
    }
    Element exists = getElement(view, sVARIABLE_TAG, "name", name);
    if (exists!=null) {
      //System.err.println ("SimulationXML : Trying to register variable <"+name+"> that already exists in view "+view.getNodeName()+". Ignored.");
      return null;
    }
//    System.err.println ("SimulationXML: Register variable <"+name+"> to view: "+view.getNodeName()+ " with value "+value);
    Element element = addElement(view,sVARIABLE_TAG);
    addCDATAElement(element,"name",name);
    if (inputOnly) addTextElement(element,"type","INPUT_ONLY");
    if (value!=null) addCDATAElement(element,"value",value);
    return element;
  }

  public Element registerViewAction(Element view, String name) {
    if (view.getNodeName()!=sPAGE_TAG || view.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to register action <"+name+"> to non view: "+view.getNodeName());
      return null;
    }
    Element exists = getElement(view, sACTION_TAG, "name", name);
    if (exists!=null) {
      //System.err.println ("SimulationXML : Trying to register action <"+name+"> that already exists in view "+view.getNodeName()+". Ignored.");
      return null;
    }
    Element element = addElement(view,sACTION_TAG);
    addCDATAElement(element,"name",name);
    return element;
  }

  public Element addRootViewElement(Element view) {
    if (view.getNodeName()!=sPAGE_TAG || view.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to add root element to non view: "+view.getNodeName());
      return null;
    }
    Element element = getRootViewElement(view); 
    if (element==null) element = addElement(view,sROOT_ELEMENT_TAG);
    return element;
  }

  public Element getRootViewElement(Element view) {
    if (view.getNodeName()!=sPAGE_TAG || view.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to return root element from non view: "+view.getNodeName());
      return null;
    }
    for (Element element : getElementList(view,sROOT_ELEMENT_TAG)) { // find the first one, if any
      return element;
    }
    return null;
  }

  public Element addViewElement(Element view, String type, String name, String parent) {
    if (view.getNodeName()!=sPAGE_TAG || view.getParentNode()!=mView) {
      System.err.println ("SimulationXML error: Trying to add element <"+name+"> to non view: "+view.getNodeName());
      return null;
    }
    Element element = addElement(view,sELEMENT_TAG);
    addTextElement(element, "type",type);
    addCDATAElement(element,"name",name);
    addCDATAElement(element,"parent",parent);
    return element;
  }

  public Element addViewElementProperty(Element element, PROPERTY type, String name) {
    if (element.getNodeName()!=sELEMENT_TAG && element.getNodeName()!=sROOT_ELEMENT_TAG) {
      System.err.println ("SimulationXML error: Trying to add property <"+name+"> to non view element: "+element.getNodeName());
      return null;
    }
    Element property = addElement(element,sELEMENT_PROPERTY_TAG);
    String tagName = propertyTagname(type);
    addTextElement(property,"type",tagName);
    addCDATAElement(property,"name",name);
    return property;
  }
  
  public Element getViewElementProperty(Element element, String property) {
    if (element.getNodeName()!=sELEMENT_TAG && element.getNodeName()!=sROOT_ELEMENT_TAG) {
      System.err.println ("SimulationXML error: Trying to get property <"+property+"> from non view element: "+element.getNodeName());
      return null;
    }
    return getElement(element, sELEMENT_PROPERTY_TAG, "name", property);
  }

  public void addViewElementLocalizedProperty(Element property, String locale, String value) {
    if (property==null || property.getNodeName()!=sELEMENT_PROPERTY_TAG) {
      System.err.println ("SimulationXML error: Trying to localize (to '"+value+"') a non-property element: "+ ((property==null) ? null : property.getNodeName()));
      return;
    }
    Element localeEntry = addElement(property,sLOCALIZATION_TAG);
    addCDATAElement(localeEntry,"locale",locale);
    addCDATAElement(localeEntry,"value",value);
  }

  public Element addViewElementProperty(Element element, PROPERTY type, String name, String value) {
    Element property = addViewElementProperty(element, type, name);
    addViewElementLocalizedProperty(property,sDEFAULT_LOCALE,value);
    return property;
  }

  String getViewElementPropertyValue(Element property, String locale) {
    String defaultValue = null;
    for (Element localization : getElementList(property,sLOCALIZATION_TAG)) { // for each locale
      String oneLocale = evaluateNode(localization,"locale");
      if (locale.equals(oneLocale)) return evaluateNode(localization,"value");
      if (sDEFAULT_LOCALE.equals(oneLocale)) defaultValue = evaluateNode(localization,"value"); // but keep on seacrhing, just in case
    }
    return defaultValue;
  }

  // ------------------------------------
  // HTML generation
  //-------------------------------------

  Element getViewSelected(String viewDesired) {
    Element viewSelected = null;
    for (Element view : getElementList(mView,sPAGE_TAG)) { // find the desired view (or take the last one)
      viewSelected = view;
      String viewName  = evaluateNode(view, "name");
      if (viewName!=null && viewName.equals(viewDesired)) {
//        System.err.println("View found "+viewName);
        break;
      }
    }
    return viewSelected;
  }
  
  /**
   * Splits the code into nicer-looking lines and appends it to a buffer 
   * @param buffer
   * @param code
   * @param information
   * @param prefix
   */
  static public void splitCode (StringBuffer buffer, String code, String information, String prefix) {
    int lineNumber = 1;
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(code, "\n");
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      buffer.append(prefix + line);
      if (information!=null) buffer.append("  // > " + information + ":" + lineNumber + "\n");
      else buffer.append("\n");
      lineNumber++;
    }
  }
    
  static public String splitCode (String code, String information, String prefix) {
    StringBuffer buffer = new StringBuffer();
    splitCode (buffer, code, information, prefix);
    return buffer.toString();
  }
  
  static public String initCodeForAnArray (String linePrefix, String comment, String lineOfIndexes, String name, String dimension, String value) {
//    buffer.append("    "+name+" = new Array("+dimension+");\n");
//    if (value!=null && value.trim().length()>0) buffer.append("    for (var _i=0;_i<"+dimension+"; _i++) "+name+"[_i] = "+value+";\n");

    if (value==null || value.trim().length()<=0) {
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
      return linePrefix + name+ " = new Array("+tkn.nextToken()+");" + comment;
    }
    if (value.startsWith("new ") || value.startsWith("[")) return linePrefix + name + " = " + value+";" + comment;
    
//    if (!_ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, _type) && // It is NOT a single variable 
//         _ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, "Object")) // But is an object, i.e. an array 
//          return " = " + _value+"; // " + _comment;
      
    StringBuffer line = new StringBuffer(); //linePrefix + name + " = [];\n");
    {
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
      line.append(linePrefix + name+ " = new Array("+tkn.nextToken()+");" + comment + "\n");
    }
    line.append(linePrefix + "(function () {\n");
    
    String prefix = linePrefix+"  ", accumIndexStr = "";
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] "), tknIndexes=null;
    int dim = tkn.countTokens();
    
    if (lineOfIndexes!=null) {
      tknIndexes = new java.util.StringTokenizer(lineOfIndexes,",");
      line.append(prefix+"var "+tknIndexes.nextToken());
      for (int k=1; k<dim; k++) line.append(","+tknIndexes.nextToken());
      tknIndexes = new java.util.StringTokenizer(lineOfIndexes,","); // reset
    }
    else {
      line.append(prefix+"var _i0");
      for (int k=1; k<dim; k++) line.append(",_i"+k);
    }
    line.append(";\n");
    for (int k=0; k<dim; k++) {
      String kDim = tkn.nextToken();
      String indexStr = lineOfIndexes==null ? "_i"+k : tknIndexes.nextToken();
      line.append(prefix + "for ("+indexStr+"=0; "+indexStr+"<"+kDim+"; "+indexStr+"+=1) { "+comment + "\n");
      prefix += "  ";
      accumIndexStr += "["+indexStr+"]";
      if (k<dim-1) line.append(prefix + name+ accumIndexStr+" = [];\n"); // new Array("+kDim+");\n");
      else line.append(prefix + name+ accumIndexStr+" = "+value+"; "+comment+"\n");
    }
    for (int k=0; k<dim; k++) {
      prefix = prefix.substring(2);
      line.append(prefix+"}\n");
    }
    line.append(linePrefix + "}());");
    return line.toString();
  }

}
