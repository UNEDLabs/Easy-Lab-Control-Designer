package org.colos.ejs.osejs.edition.variables;

import java.util.StringTokenizer;

import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.ObjectValue;
import org.colos.ejs.library.control.value.StringValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;

import bsh.Interpreter;

public class CodeInterpreterForJava implements CodeInterpreter {
  static private final int[] EMPTY_INT_ARRAY=new int[0];
  static private final double[] EMPTY_DOUBLE_ARRAY=new double[0];

  private Interpreter mInterpreter = new Interpreter();
  private Osejs mEjs;
  
  CodeInterpreterForJava (Osejs ejs) {
    mEjs = ejs;
  }
  
  public void reset() {
//  System.out.println ("resetting mInterpreter");
  try { mInterpreter.eval("clear()"); } 
  catch (Exception exc) { mInterpreter = new Interpreter(); }
  mInterpreter.setStrictJava(true);
  try { 
    for (String anImport : mEjs.getSimInfoEditor().getImportsList()) mInterpreter.eval("import "+anImport);
    mInterpreter.eval("java.util.Map _stringProperties = new java.util.HashMap();");

    mInterpreter.eval("org.colos.ejs.library.control.EjsControl _view = new org.colos.ejs.library.control.EjsControl();");
    mInterpreter.eval(" org.opensourcephysics.numerics.Function _ZERO_FUNCTION = new org.opensourcephysics.numerics.Function() {public double evaluate(double x){return 0;}};");
    mInterpreter.eval("boolean _isPaused = true;");
    mInterpreter.eval("boolean _isPlaying = false;");
    mInterpreter.eval("boolean _isApplet = false;");
    mInterpreter.eval("boolean _isPaused() { return true; }");
    mInterpreter.eval("boolean _isPlaying() { return false; }");
    mInterpreter.eval("boolean _isApplet() { return false; }");
    String formatStr = "String _format(double _value, String _pattern) {\n" +
      "java.text.DecimalFormat _tmp_format = new java.text.DecimalFormat (_pattern);\n"+
      "return _tmp_format.format(_value);\n"+ 
      "}";
    mInterpreter.eval(formatStr);
    mInterpreter.eval("int _getDelay() { return 10; }");
    mInterpreter.eval("String _getParameter(String _name) { return _name; }");
    mInterpreter.eval("String[] _getArguments() { return null; }");
    mInterpreter.eval("String _getStringProperty(String _property) { Object _value = _stringProperties.get(_property); if (_value!=null) return _value.toString(); return _property; }");
    for (org.colos.ejs.osejs.utils.TwoStrings ts : mEjs.getTranslationEditor().getResourceDefaultPairs()) {
      mInterpreter.eval("_stringProperties.put(\""+ts.getFirstString()+"\",\""+ts.getSecondString()+"\");");
    }

    // add extra declarations to the mInterpreter
    // in particular, methods for state variables of ODEs
    mEjs.getModelEditor().getEvolutionEditor().addToInterpreter();
    
      if (mEjs.getSimInfoEditor().useInterpreter()) {
      TabbedEditor libEditor = mEjs.getModelEditor().getLibraryEditor();
      for (Editor page : libEditor.getPages()) {
        String customCode = page.generateCode(Editor.GENERATE_CODE,null).toString();
        //        System.out.println ("Code for page "+page.getName()+" = \n"+customCode);
        try { mInterpreter.eval(customCode); }
        catch (bsh.EvalError parseExc) {
          mEjs.getOutputArea().println(Osejs.getResources().getString("VariablesEditor.ErrorOnCustomPage")+" "+ page.getName()+ ":\n  "+parseExc.toString());
          //          ejs.getOutputArea().println("  ("+parseExc.getErrorLineNumber()+"): "+parseExc.getErrorText());
        }
      }
    }
//      String methods[] = (String[]) .get("this.methods");
//      for (int i=0; i<methods.length; i++) 
//        System.out.println ("Method "+i+" = "+methods[i]);
  } 
  catch (Exception exc) { exc.printStackTrace(); }
}

  public Object evaluateExpression(String _expression) {
    try { return mInterpreter.eval(_expression); } 
    catch (Exception exc) { return null; }
  }

  /**
   * Whether a given name corresponds to a variable of the given type.
   */
  public boolean isVariableDefined(String _name, String _type) {
    try {
      if (_name.indexOf('[')>0) return false; // avoid expressions like x[0] to be considered a variable
      mInterpreter.eval(_name+" = "+_name+";"); // This avoids evaluating java.awt.Color.RED as a variable
      // If I am here, the name stands for a variable. We now check the right type
      Object object = mInterpreter.get(_name);
      //if (object==null) return false; null strings or objects are acceptable
      if (_type.equals("double") || _type.equals("float")) return object instanceof Double;
      if (_type.equals("boolean")) return object instanceof Boolean;
      if (_type.equals("int") || _type.equals("char") || _type.equals("byte") || _type.equals("short") || _type.equals("long")) 
        return object instanceof Integer;
      if (_type.equals("String")) return object instanceof String || object==null; // a null string is also valid
      return true;
    }
    catch (Exception exc) { return false; }
  }

  /**
   * Whether a given name corresponds to double array.
   */
  public boolean isDoubleArray(String _name) {
    try {
      String name = "__ejs_tmp";
      mInterpreter.unset(name);
      mInterpreter.eval("double[] "+name+" = "+_name+";"); 
      // If I am here, the name stands for a variable. We now check the right type
      return true;
    }
    catch (Exception exc) { return false; }
  }

  /**
   * Parses an expression of the given type and returns a Value with is value
   * @return a Value with the correct type and value, null if there was any error
   */
  public Value checkExpression(String _expression, String _type) {
    try {
      String name = "__ejs_tmp";
      mInterpreter.unset(name);
      mInterpreter.eval(_type +" "+ name+";"); 
      mInterpreter.eval(name+" = "+_expression+ ";"); 
      Object object = mInterpreter.get(name);
      if (_type.equals("double") || _type.equals("float")) return new DoubleValue(((Double)object).doubleValue());
      if (_type.equals("boolean")) return new BooleanValue(((Boolean)object).booleanValue());
      if (_type.equals("int") || _type.equals("char") || _type.equals("byte") || _type.equals("short") || _type.equals("long")) return new IntegerValue(((Integer)object).intValue());
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
      if (mInterpreter.get(_name)!=null) return null; // The variable has been processed already
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
        String sentence = _type +" " + _name;
        if (hasExpression) sentence += " = " + _expression;
        mInterpreter.eval(sentence);
        Object object = mInterpreter.get(_name);
        // Check for integers initialized to doubles. BeanShell accepts int x = 0.3; !!!
        if (hasExpression && object!=null && object.getClass()==Integer.class) {
          Object valueExpression = mInterpreter.eval(_expression);
          if (valueExpression.getClass()==Double.class) {
            //System.out.println("Incorrect value for <"+_name+"> = "+_expression);
            return null;
          }
        }
        Value value=null;
        if      (_type.equals("double"))  value = new DoubleValue(((Double) object).doubleValue());
        else if (_type.equals("float"))   value = new DoubleValue(((Float) object).floatValue());
        else if (_type.equals("boolean")) value = new BooleanValue(((Boolean) object).booleanValue());
        else if (_type.equals("int"))     value = new IntegerValue(((Integer) object).intValue());
        else if (_type.equals("char"))    value = new IntegerValue(((Character) object).charValue());
        else if (_type.equals("short"))   value = new IntegerValue(((Short) object).intValue());
        else if (_type.equals("byte"))    value = new IntegerValue(((Byte) object).intValue());
        else if (_type.equals("long"))    value = new IntegerValue(((Long) object).intValue());
        else if (_type.equals("String"))  value = new StringValue((String) object);
        else value =  new ObjectValue(object);
        return value;
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
      String sentence = _type;
      for (int k = 0; k < dim; k++) sentence += "[]";
      sentence += " " + _name;

      sentence += initCodeForAnArray (null, lineOfIndexes, _name, _type, _dimension, _expression, mEjs);
      //System.out.println ("Init Sentence is "+sentence);
      mInterpreter.eval(sentence);
      Object object = mInterpreter.get(_name);

      // Check for integer arrays initialized with doubles. BeanShell accepts int[] x = new int[]{0.3,0.4}; !!!
      if (hasExpression && object.getClass()==EMPTY_INT_ARRAY.getClass()) {
        if (_expression.startsWith("{")) {     
          Object objectExpression = Value.parseConstantOrArray(_expression, true).getObject();
          if (objectExpression.getClass()==EMPTY_DOUBLE_ARRAY.getClass()) return null;
        }
      }

      return new ObjectValue(object);
    }
    catch (Exception exc) { return null; }
  }
  
  static public String initCodeForAnArray (String _comment, String _lineOfIndexes,
      String _name, String _type, String _dimension, String _value, Osejs _ejs) {
    
    StringTokenizer tkn = new java.util.StringTokenizer(_dimension,"[] ");
    int dim = tkn.countTokens();

    if (_value.startsWith("new ")) return " = " + _value+"; // " + _comment;
    
    if (!_ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, _type) && // It is NOT a single variable 
         _ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, "Object")) // But is an object, i.e. an array 
          return " = " + _value+"; // " + _comment;
      
    StringBuffer line = new StringBuffer(" = new "+_type+" ");
    if (_value.startsWith("{")) {
      while (tkn.hasMoreTokens()) { line.append("[]"); tkn.nextToken(); }
      line.append(_value);
      if (_comment==null) line.append(";");
      else line.append("; // " + _comment);
      return line.toString();
    }
    
    while (tkn.hasMoreTokens()) line.append("[" + tkn.nextToken() + "]");
    if (_comment==null) line.append(";");
    else line.append("; // " + _comment);
    if (_value.length()<=0) return line.toString();

    tkn = new java.util.StringTokenizer(_dimension,"[] ");
    if (_lineOfIndexes!=null) {
      StringTokenizer tknIndexes = new java.util.StringTokenizer(_lineOfIndexes,",");
      for (int k=0; k<dim; k++) {
        String indexStr = tknIndexes.nextToken();
        line.append("\n    for (int "+indexStr+"=0; "+indexStr+"<"+tkn.nextToken()+"; "+indexStr+"++) ");
      }
    }
    else {
      for (int k=0; k<dim; k++) {
        String indexStr = "_i"+k;
        line.append("\n    for (int "+indexStr+"=0; "+indexStr+"<"+tkn.nextToken()+"; "+indexStr+"++) ");
      }
    }
    line.append(_name);
    if (_lineOfIndexes!=null) {
      StringTokenizer tknIndexes = new java.util.StringTokenizer(_lineOfIndexes,",");
      for (int k=0; k<dim; k++) line.append("["+tknIndexes.nextToken()+"]");
    }
    else {
      for (int k=0; k<dim; k++) line.append("[_i"+k+"]");
    }
    line.append(" = "+_value+";");
    if (_comment!=null) line.append(" // " + _comment);
    return line.toString();
  }

}
