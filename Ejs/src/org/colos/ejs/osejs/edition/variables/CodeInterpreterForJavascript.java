package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.ObjectValue;
import org.colos.ejs.library.control.value.StringValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejss.xml.SimulationXML;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

public class CodeInterpreterForJavascript implements CodeInterpreter {
  static private final int[] EMPTY_INT_ARRAY=new int[0];
  static private final double[] EMPTY_DOUBLE_ARRAY=new double[0];

  private Osejs mEjs;
  private ScriptableObject scope;
  private final ContextFactory contextFactory;
  private Context context;
    
  public Object getValue(String name) {
      return scope.get(name, scope);
  }
  
  CodeInterpreterForJavascript (Osejs ejs) {
    mEjs = ejs;
    contextFactory = new ContextFactory();
  }
  
  public void reset() {
//  System.out.println ("resetting mInterpreter");
//    if (scope!=null) scope.sealObject();        
    context = contextFactory.enterContext();
    scope = context.initStandardObjects();                 
//    cx.evaluateString(scope, javascript, "JSEVALUATE", 1, null);        
//    scope.sealObject();        

    StringBuffer buffer = new StringBuffer();
    
    buffer.append(FileUtils.readTextFile(new java.io.File(mEjs.getBinDirectory(),"javascript/lib/dummy.js"), null));
//    buffer.append("var EJSS_CORE = EJSS_CORE || { };\n");
//    buffer.append("var EJSS_DRAWING2D = EJSS_DRAWING2D || { };\n");
//    buffer.append("var EJSS_INTERFACE = EJSS_INTERFACE || { };\n");
//    buffer.append("var EJSS_ODE_SOLVERS = EJSS_ODE_SOLVERS || { };\n");
//    buffer.append("var _model = _model || { }; \n");
//    buffer.append("var _view = _view || { };\n");
//    buffer.append("var console = {};\n");

    buffer.append("var _stringProperties = {};\n");
    
    buffer.append("var _view = {};\n");
    buffer.append("var _model = {};\n");

    buffer.append("var _isPaused = true;\n");
    buffer.append("var _isPlaying = false;\n");
    buffer.append("var _isApplet = true;\n");
    
//    buffer.append("if (!Math.log10) Math.log10 = function (_value) { return Math.log(_value)/Math.LN10; };\n");
    
    buffer.append("  function _format(_value, _digits) {\n" +
        "var _num = new Number(_value);\n"+
        "return _num.toPrecision(_digits);\n"+ 
        "};\n");
    buffer.append("function _getDelay() { return 10; };\n");
    buffer.append("function _getParameter(_name) { return _name; };\n");
    buffer.append("function _getArguments() { return null; };\n");
    buffer.append("function _getStringProperty(propertyName) {\n"+
                  "  var _value = _stringProperties[propertyName];\n"+
                  "  if (_value===undefined) return propertyName;\n"+
                  "  else return _value;\n" +
                  "}\n");
    for (org.colos.ejs.osejs.utils.TwoStrings ts : mEjs.getTranslationEditor().getResourceLocalePairs(mEjs.getTranslationEditor().getLocaleItem())) {
        buffer.append("_stringProperties."+ts.getFirstString()+" = \""+ts.getSecondString()+"\";\n");
    }

    // add extra declarations to the mInterpreter
    // in particular, methods for state variables of ODEs
    mEjs.getModelEditor().getEvolutionEditor().addToInterpreter();
//    System.err.println ("Evaluating:\n"+buffer.toString());
    context.evaluateString(scope, buffer.toString(), "JSEVALUATE", 1, null);
    
    
    if (mEjs.getSimInfoEditor().useInterpreter()) {
      TabbedEditor libEditor = mEjs.getModelEditor().getLibraryEditor();
      for (Editor page : libEditor.getPages()) {
        String customCode = page.generateCode(Editor.GENERATE_CODE,null).toString();
        //        System.out.println ("Code for page "+page.getName()+" = \n"+customCode);
        try { 
          context.evaluateString(scope, customCode, "JSEVALUATE", 1, null);
        }
        catch (Exception parseExc) {
          mEjs.getOutputArea().println(Osejs.getResources().getString("VariablesEditor.ErrorOnCustomPage")+" "+ page.getName()+ ":\n  "+parseExc.toString());
          //          ejs.getOutputArea().println("  ("+parseExc.getErrorLineNumber()+"): "+parseExc.getErrorText());
        }
      }
    }
//      String methods[] = (String[]) .get("this.methods");
//      for (int i=0; i<methods.length; i++) 
//        System.out.println ("Method "+i+" = "+methods[i]);
}

  public Object evaluateExpression(String _expression) {
    try { 
//      System.err.println("Evaluating expression "+_expression);
      return context.evaluateString(scope, _expression, "JSEVALUATE", 1, null);
    }
    catch (Exception exc) {
//      exc.printStackTrace();
      return null; 
    }
  }

  /**
   * Whether a given name corresponds to a variable of the given type.
   */
  public boolean isVariableDefined(String _name, String _type) {
    try {
      //System.err.println("Checking variable defined "+_name+" of type "+_type);
      if (_name.indexOf('[')>=0) return false; // avoid expressions like x[0] to be considered a variable
      context.evaluateString(scope, _name+" = "+_name+";", "JSEVALUATE", 1, null); // This avoids evaluating java.awt.Color.RED as a variable
      // If I am here, the name stands for a variable. We now check the right type
      Object object = getValue(_name);
//      System.err.println("Object is "+object);
//      System.err.println("Object class is "+object.getClass());
      

      //if (object==null) return false; null strings or objects are acceptable
      if (_type.equals("double") || _type.equals("float")) return (object instanceof Double) || (object instanceof Integer);
      if (_type.equals("boolean")) return object instanceof Boolean;
      if (_type.equals("int") || _type.equals("char") || _type.equals("byte") || _type.equals("short") || _type.equals("long")) { 
        if (object instanceof Integer) return true;
        if (object.getClass()==Double.class) { // the interpreter initializes x = 1 to 1.0!
          int intValue = ((Number) object).intValue();
          double doubleValue = ((Number) object).doubleValue();
          return (intValue==doubleValue);
        }
        return false;
      }
      if (_type.equals("String")) return object instanceof String || object==null; // a null string is also valid
      return true;
    }
    catch (Exception exc) { 
      //exc.printStackTrace();
      return false; 
    }
  }

  /**
   * Whether a given name corresponds to double array.
   */
  public boolean isDoubleArray(String _name) {
    try {
//      System.err.println("Checking array of doubles "+_name);
      Object object = getValue(_name);
      return (object instanceof NativeArray);
    }
    catch (Exception exc) { return false; }
  }

  /**
   * Parses an expression of the given type and returns a Value with is value
   * @return a Value with the correct type and value, null if there was any error
   */
  public Value checkExpression(String _expression, String _type) {
    try {
//      System.err.println("Checking expression "+_expression +" of type "+_type);
      String name = "__ejs_tmp";
      context.evaluateString(scope, name+" = "+_expression+";", "JSEVALUATE", 1, null); 
      Object object = getValue(name);
      if (_type.equals("double") || _type.equals("float")) return new DoubleValue(((Number)object).doubleValue());
      if (_type.equals("boolean")) return new BooleanValue(((Boolean)object).booleanValue());
      if (_type.equals("int") || _type.equals("char") || _type.equals("byte") || _type.equals("short") || _type.equals("long")) return new IntegerValue(((Number)object).intValue());
      if (_type.equals("String")) return new StringValue((String)object);
      return new ObjectValue(object);
    } 
    catch (Exception exc) { return null; }
  }

  /**
   * Parsers the given expression and assigns it to a variable of the given name, type, and dimension.
   * Used only by TableOfVariablesEditor
   * @return A Value with the correct type and value, null if there was any error
   */
  public Value checkVariableValue(String _name, String _expression, String _type, String _dimension) {
//    System.out.println ("Checking variable "+_name+ " with value "+_expression);
    //if (control.getVariable(_name)!=null) return null; 
    try {
//      System.err.println("Check variable value "+_name+" = "+ _expression +" of type "+_type);
      Object object = getValue(_name);
//      System.err.println("Object is "+object);
//      System.err.println("Object class is "+object.getClass());
      if (object instanceof org.mozilla.javascript.NativeObject) return new ObjectValue(object);
      if (object!=ScriptableObject.NOT_FOUND) return null; // The variable has been processed already
      int dim = 0;
      if (_dimension.length()>0) {
        java.util.StringTokenizer tkn = new java.util.StringTokenizer(_dimension,"[] ");
        dim = tkn.countTokens();
      }
      boolean hasExpression = _expression.length()>0;
      // Special hack to avoid bsh to issue an exception when a boolean a = 1!
      if (hasExpression && _type.equals("boolean")) {
        // If the value is a constant numeric value return null
        try { 
          Double.parseDouble(_expression);
          return null; 
        }
        catch (NumberFormatException nfe) {}
      }
      if (dim<=0) {  // Simple variable
        String sentence = "var  " + _name;
        if (hasExpression) sentence += " = " + _expression;
        context.evaluateString(scope, sentence+";", "JSEVALUATE", 1, null);
        object = getValue(_name);
//        System.err.println("Object is "+object);
//        System.err.println("Object class is "+object.getClass());
        
        if (object instanceof org.mozilla.javascript.Undefined) {
          if (_type.equals("double"))  return new DoubleValue(0.0);
          if (_type.equals("float"))   return new DoubleValue(0.0);
          if (_type.equals("boolean")) return new BooleanValue(false);
          if (_type.equals("int"))     return new IntegerValue(0);
          if (_type.equals("char"))    return new IntegerValue(0);
          if (_type.equals("short"))   return new IntegerValue(0);
          if (_type.equals("byte"))    return new IntegerValue(0);
          if (_type.equals("long"))    return new IntegerValue(0);
          if (_type.equals("String"))  return new StringValue("");
          return new ObjectValue("");
        }
          
        // Check for integers initialized to doubles. 
        if (hasExpression && object.getClass()==Double.class) {
          if (_type.equals("int") || _type.equals("short") || _type.equals("byte") || _type.equals("long")) {
            int intValue = ((Number) object).intValue();
            double doubleValue = ((Number) object).doubleValue();
            if (intValue!=doubleValue) return null;
          }
        }
        if (_type.equals("double"))  return new DoubleValue(((Number) object).doubleValue());
        if (_type.equals("float"))   return new DoubleValue(((Number) object).floatValue());
        if (_type.equals("boolean")) return new BooleanValue(((Boolean) object).booleanValue());
        if (_type.equals("int"))     return new IntegerValue(((Number) object).intValue());
        if (_type.equals("char"))    return new IntegerValue(((Character) object).charValue());
        if (_type.equals("short"))   return new IntegerValue(((Number) object).intValue());
        if (_type.equals("byte"))    return new IntegerValue(((Number) object).intValue());
        if (_type.equals("long"))    return new IntegerValue(((Number) object).intValue());
        if (_type.equals("String"))  return new StringValue(object.toString());
        return new ObjectValue(object);
      }
      // It is an array

      java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(_name,"[] ");
      int dimIndex = tknIndexes.countTokens();
      String lineOfIndexes = null;
      if (dimIndex>1) {
        _name = tknIndexes.nextToken();
        lineOfIndexes = tknIndexes.nextToken();
        while (tknIndexes.hasMoreTokens()) lineOfIndexes += ","+tknIndexes.nextToken();
        if ((dimIndex-1)!=dim) mEjs.getOutputArea().println ("Syntax error in variable name "+_name.toString());
      }
      String sentence = "var  " + _name+"=[];";

      sentence += SimulationXML.initCodeForAnArray ("", "",lineOfIndexes, _name, _dimension, _expression);
//      System.err.println ("Init Sentence is "+sentence);
      context.evaluateString(scope,sentence, "JSEVALUATE", 1, null); 

      // Check for integer arrays initialized with doubles. BeanShell accepts int[] x = new int[]{0.3,0.4}; !!!
      if (hasExpression && object.getClass()==EMPTY_INT_ARRAY.getClass()) {
        if (_expression.startsWith("[")) {     
          Object objectExpression = Value.parseConstantOrArray(_expression, true).getObject();
          if (objectExpression.getClass()==EMPTY_DOUBLE_ARRAY.getClass()) return null;
        }
      }

      return new ObjectValue(object);
    }
    catch (Exception exc) { return null; }
  }
  
}
