package org.colos.ejss.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejss.xml.SimulationXML;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Element;

public class LintTool {

  static private final String sBLANK_LINE = "                                                                                                                    ";
  static private final int sBLANK_LINE_LENGTH = sBLANK_LINE.length();
  
  static public void lintSimulation(Osejs ejs, SimulationXML sim, String viewDesired, String locale, String libPath, String htmlPath) {
    XMLTransformerJava transformer = new XMLTransformerJava(ejs, libPath,sim,JSObfuscator.Level.OPEN,ejs.getOptions().useFullLibrary());

    Element viewSelected = sim.getViewSelected(viewDesired);
    if (!sim.isViewOnly()) {
      String jsCode = transformer.getJavascriptForEmulator(viewSelected,locale,libPath, htmlPath);
//      if (DEBUG) {
//        try {  // save it for debugging purposes
//          String outputFilepath = sim.getName()+"_debugLint.html";
//          File outputFile = new File (outputFilepath);
//          XMLTransformerJava.saveToFile(outputFile, jsCode);
//          ejs.getOutputArea().println("Debug output file generated "+outputFile.getAbsolutePath());
//        } 
//        catch (Exception e) {
//          e.printStackTrace();
//        }
//      }      
      JSHint hint = null;
      try {
        Reader reader = new BufferedReader(new FileReader(new File(libPath,"jshint.js")));
        hint = new JSHint(reader);
        hint.hint("ejsS", jsCode);
        // error report
        //        System.err.println(lint.getErrorReport());
        // report (functions, vars, globals)
        //        System.err.println(lint.getReport());
        // errors
        List<Scriptable> errors = hint.getErrors();
        for (int i=0; i<errors.size(); i++) {
          Scriptable sc = errors.get(i);
          //  format { line: NUMBER, character: NUMBER, reason: STRING, evidence: STRING }
          // if (evidence.startsWith(prefix) ...
          String evidence = sc.get("evidence", sc).toString();
          ErrorOutput output = ejs.getOutputArea();
          output.println("Lint error: " + sc.get("reason", sc)); //+ " in line "+sc.get("line", sc));
          output.println(evidence);
          if (evidence.indexOf(XMLTransformerJava.sEJSS_PREFIX)>=0) {
            Number position = (Number) sc.get("character",sc);
            output.println(getMarker(position.intValue()));
          }
          // store output (only debug)
          //        java.io.FileOutputStream mf = new java.io.FileOutputStream(new File("salida.js"));
          //        mf.write(jsCode.getBytes());
          //        mf.close();
        }
      } catch (Exception exc) {     
        exc.printStackTrace();
        try {  // save it for debugging purposes
          String outputFilepath = sim.getName()+"_lint_debug.js";
          File outputFile = new File (outputFilepath);
          XMLTransformerJava.saveToFile(outputFile, jsCode);
          ejs.getOutputArea().println("Debug output file generated "+outputFilepath);
        } 
        catch (Exception exc2) {
          exc2.printStackTrace();
        }
      } 
    }
  }
  
  static private String getMarker(int position) {
    if (position<=0) return "^";
    if (position<sBLANK_LINE_LENGTH) return sBLANK_LINE.substring(0,position-1) + "^";
    return sBLANK_LINE + getMarker(position-sBLANK_LINE_LENGTH);
  }
  
  
}
