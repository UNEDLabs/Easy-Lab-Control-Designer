package org.colos.ejss.html_view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.lang.reflect.Type;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.utils.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JSONDocAccess {
  static final String LIB_DOC_FILENAME = "javascript/lib/ejsS.v1.max.doc.js";

  static private class EjsSClass {
    String description;
    String parent;
    String see;
    Map<String,Map<String,String>> properties;
    Map<String,Map<String,String>> methods;
    Map<String,Map<String,String>> actions;
  }

  static private Map<String,EjsSClass> sClassesMap;

  public JSONDocAccess(Osejs ejs) {
    this(new File(ejs.getBinDirectory(),LIB_DOC_FILENAME));
  }

  private JSONDocAccess(File docFile) {
    if (sClassesMap==null) {
      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(docFile), "UTF-8"));
        Type listFoldersType = new TypeToken<Map<String,EjsSClass>>(){}.getType();
        sClassesMap = new Gson().fromJson(br, listFoldersType);
        br.close();
      } catch (Exception exc) {
        exc.printStackTrace();
        sClassesMap = null;
      }
    }
  }

  /**
   * Gets the set of classes
   * @return Set<String>
   */
  public Set<String> getClasses() {
    return sClassesMap.keySet();
  }

  public String getHTML(String name, String classname, String linkBack, String cssFilename) {
//    System.err.println("name = "+name);
//    System.err.println("classname = "+classname);

    try {
      boolean equalNames = name.equals(classname); 
      EjsSClass ejscls = sClassesMap.get(classname);
      StringBuffer buffer = new StringBuffer();
      buffer.append("<html>\n");
      buffer.append("  <head>\n");
      buffer.append("    <title> EjsS library doc for "+classname+"</title>\n");
      if (cssFilename!=null) 
        buffer.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+cssFilename+"\"></link>\n");
      else {
        buffer.append("    <style>\n");
        buffer.append("      body {\n");
        buffer.append("        font-family: Georgia, \"Times New Roman\", Times, serif;\n");
        buffer.append("        color: purple;\n");
        buffer.append("        background-color: #C8DFD0 \n");
        buffer.append("    }\n");

        buffer.append("    h3 {\n");
        //      buffer.append("      font-family: Helvetica, Geneva, Arial, SunSans-Regular, sans-serif\n"); 
        buffer.append("        color: blue;\n");
        buffer.append("    }\n");
        buffer.append("    </style>\n");
      }
      buffer.append("  </head>\n");
      buffer.append("  <body>\n");
      
      if (equalNames) buffer.append("<h3>"+classname+"</h3>\n");
      else buffer.append("<h3>"+name+ " ("+classname+")</h3>\n");
      buffer.append("<p><b>Description: </b>"+ejscls.description+"</p>\n");
      if (ejscls.see!=null) buffer.append("<p><b>See also: </b><a href=\""+ejscls.see+"\">"+ejscls.see+"</a></p>\n");
      if (ejscls.parent!=null) buffer.append("<p><b>Inherits from: </b><a href=\""+ejscls.parent+"\">"+ejscls.parent+"</a></p>\n");
      if (linkBack!=null && !linkBack.equals(ejscls.parent)) buffer.append("<p><b>Back to: </b><a href=\""+linkBack+"\">"+linkBack+"</a></p>\n");
      

      ArrayList<String> parentList = listOfParents(classname);

      buffer.append("<hr/>\n");
      buffer.append("<h3>Functions</h3>\n");
      Map<String,Map<String,String>> functionsMap = getFunctions(parentList);
      Set<String> functions = new TreeSet<String>(functionsMap.keySet());
      for (String function : functions) {
        Map<String,String> functionMap = functionsMap.get(function);
//        if (function.equals("setX")) for (String entry : functionMap.keySet()) {
//          System.err.println ("<"+entry +"> = "+functionMap.get(entry));
//        }
        String visibility = functionMap.get("visibility");
        if (!"private".equals(visibility)) {
          buffer.append("<p><b>"+function+": </b>"+functionMap.get("description")+"<br/>\n");
          buffer.append("<ul>\n");
          int paramIndex = 1;
          while (paramIndex<100) {
            String param = functionMap.get("param"+paramIndex);
            if (param==null) break;
            buffer.append("<li><i>Parameter: </i>"+param+"</li>\n");
            paramIndex++;
          }
          String returnValue = functionMap.get("return");
          if (returnValue!=null) buffer.append("<li><i>Returns: </i>"+returnValue+"</li>\n");
          buffer.append("</ul></p>\n");
          String see = functionMap.get("see");
          if (see!=null) buffer.append("<li><i>See also: </i><a href=\""+see+"\">"+see+"</a></li>\n");
        }
      }

      buffer.append("<hr/>\n");
      buffer.append("<h3>Actions</h3>\n");
      Map<String,Map<String,String>> actionsMap = getActions(parentList);
      Set<String> actions = new TreeSet<String>(actionsMap.keySet());
      for (String action : actions) {
        Map<String,String> actionMap = actionsMap.get(action);
        buffer.append("<p><b>"+action+": </b>"+actionMap.get("description")+"</p>\n");
      }

      buffer.append("<hr/>\n");
      buffer.append("<h3>Properties</h3>\n");
      Map<String,Map<String,String>> propertiesMap = getProperties(parentList);
      Set<String> properties = new TreeSet<String>(propertiesMap.keySet());
      for (String property : properties) {
        Map<String,String> propertyMap = propertiesMap.get(property);
        buffer.append("<p><b>"+property+": </b>"+propertyMap.get("description")+"<br/>\n");
        buffer.append("<ul>\n");
        String type = propertyMap.get("type");
        if (type!=null) buffer.append("<li><i>Type: </i><tt>"+type+"</tt></li>\n");
        String def = propertyMap.get("default");
        if (def!=null) buffer.append("<li><i>Default: </i><tt>"+def+"</tt></li>\n");
        String values = propertyMap.get("values");
        if (values!=null) buffer.append("<li><i>Values: </i><tt>"+values+"</tt></li>\n");
        buffer.append("</ul></p>\n");
      }

      buffer.append("<hr/>\n");
      buffer.append("  <body>\n");
      buffer.append("</html>\n");
      return buffer.toString();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      StringBuffer buffer = new StringBuffer();
      buffer.append("<h3>"+name+ " ("+classname+")</h3>\n");
      buffer.append("<p>Error when trying to obtain help for class <tt>"+name+"<tt><p>");
      return buffer.toString();
    }
  }

  /**
   * Gets the properties of a class (excluding actions)
   * @param parentList
   * @return Map<String,Map<String,String>> A map of properties
   */
  private Map<String,Map<String,String>> getProperties(ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String property : ejscls.properties.keySet()) {
        if (map.get(property)==null) map.put(property, ejscls.properties.get(property));
        else map.get(property).putAll(ejscls.properties.get(property));
      }
    }   
    return map;
  }
  
  /**
   * Gets the properties of a class (excluding actions and not recursively)
   * @param classname
   * @return Map<String,Map<String,String>> A map of properties
   */
  public Map<String,Map<String,String>> getProperties(String classname, ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    EjsSClass ownCls = sClassesMap.get(classname);
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String property : ejscls.properties.keySet()) {
        if (ownCls.properties.containsKey(property)) {
          if (map.get(property)==null) map.put(property, ejscls.properties.get(property));
          else map.get(property).putAll(ejscls.properties.get(property));
        }
      }
    }   
    return map;
  }

//  /**
//   * Gets the properties of a class (excluding actions)
//   * @param classname
//   * @return Set<String> A set of names with all the properties
//   */
//  private Map<String,Map<String,String>> getProperties(String classname) {
//    return getProperties(listOfParents(classname));
//  }

//  /**
//   * Gets the properties of a class (excluding actions)
//   * @param classname
//   * @return Set<String> A set of names with all the properties
//   */
//  public Set<String> listProperties(String classname) {
//    HashSet<String> set = new HashSet<String>();
//    ArrayList<String> classes = listOfParents(classname);
//    for(String c : classes) {
//      EjsSClass ejscls = sClassesMap.get(c);
//      Set<String> propset = ejscls.properties.keySet();
//      set.addAll(propset);
//    }   
//    return set;
//  }

  /**
   * Gets the action properties of a class
   * @param parentList
   * @return Map<String,Map<String,String>> A map of actions
   */
  private Map<String,Map<String,String>> getActions(ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String property : ejscls.actions.keySet()) {
        if (map.get(property)==null) map.put(property, ejscls.actions.get(property));
        else map.get(property).putAll(ejscls.actions.get(property));
      }
    }   
    return map;
  }
    
  /**
   * Gets the action properties of a class (not recursively)
   * @param classname
   * @return Map<String,Map<String,String>> A map of actions
   */
  public Map<String,Map<String,String>> getActions(String classname, ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    EjsSClass ownCls = sClassesMap.get(classname); 
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String property : ejscls.actions.keySet()) {
        if (ownCls.actions.containsKey(property)) {
          if (map.get(property)==null) map.put(property, ejscls.actions.get(property));
          else map.get(property).putAll(ejscls.actions.get(property));
        }
      }
    }   
    return map;
  }


  /**
   * Gets the functions of a class
   * @param parentList
   * @return Map<String,Map<String,String>> A map of functions
   */
  private Map<String,Map<String,String>> getFunctions(ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String method : ejscls.methods.keySet()) {
        if (map.get(method)==null) map.put(method, ejscls.methods.get(method));
        else map.get(method).putAll(ejscls.methods.get(method));
      }
    }   
    return map;
  }
  
  /**
   * Gets the functions of a class (but not recursively)
   * @param classname
   * @return Map<String,Map<String,String>> A map of functions
   */
  public Map<String,Map<String,String>> getFunctions(String classname, ArrayList<String> parentList) {
    Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
    EjsSClass ownCls = sClassesMap.get(classname);
    for (String parentClassname : parentList) {
      EjsSClass ejscls = sClassesMap.get(parentClassname);
      for (String method : ejscls.methods.keySet()) {
        if (ownCls.methods.containsKey(method)) {
          if (map.get(method)==null) map.put(method, ejscls.methods.get(method));
          else map.get(method).putAll(ejscls.methods.get(method));
        }
      }
    }   
    return map;
  }
  
//  /**
//   * Gets the methods of a class
//   * @param classname
//   * @return Set<String> A set of names with all the methods
//   */
//  public Set<String> getMethods(String classname) {
//    ArrayList<String> classes = listOfParents(classname);
//    HashSet<String> set = new HashSet<String>();
//    for(String c : classes) {
//      EjsSClass ejscls = sClassesMap.get(c);
//      Set<String> mthset = ejscls.methods.keySet();
//      set.addAll(mthset);
//    }		
//    return set;
//  }

  // get inputs of a classes
//  public Map<String,String> getClass(String classname) {
//    Map<String,String> ret = new HashMap<String,String>();
//    EjsSClass ejscls = sClassesMap.get(classname);
//    ret.put("description", ejscls.description);
//    ret.put("parent", ejscls.parent);
//    return ret;
//  }

//  /**
//   * Gets the information for a property
//   * @param cls
//   * @param prop
//   * @return
//   */
//  public Map<String,String> getProperty(String cls, String prop) { // Paco: Yo creo que esto está mal. Si la propiedad es heredada, no sale.
//    return getProperties(cls).get(prop);
//    //    EjsSClass ejscls = sClassesMap.get(cls);
//    //    return ejscls.properties.get(prop);
//  }

//  // get inputs of a action
//  public Map<String,String> getAction(String cls, String act) {
//    EjsSClass ejscls = sClassesMap.get(cls);
//    return ejscls.actions.get(act);
//  }

//  // get inputs of a method
//  public Map<String,String> getMethod(String cls, String mth) {
//    EjsSClass ejscls = sClassesMap.get(cls);
//    return ejscls.methods.get(mth);
//  }

  // ---
  // Private functions
  // ---

  // get parents of a class
  /**
   * Lists the parents from oldest to more recent
   * @param cls
   * @return
   */
  static private ArrayList<String> listOfParents(String cls) {
    ArrayList<String> parents = new ArrayList<String>();
    String parent = cls;
    while (parent != null && parent.length() > 0) {
      parents.add(parent);
      parent = sClassesMap.get(parent).parent;
    }
    Collections.reverse(parents);
    return parents;
  }

  // ---
  // Main Function
  // ---

  public static void main (String [ ] args) throws IOException {
    File binDir = new File("distribution/bin");
    JSONDocAccess doc = new JSONDocAccess(new File(binDir, LIB_DOC_FILENAME));

    // print list of classes
    //    System.out.println("****** CLASSES");
    //    for(String classname : doc.getClasses())
    //      System.out.println("Class: " + classname);        

    File htmlFile = File.createTempFile("ejss_doc", ".html");
    htmlFile.deleteOnExit();
    FileUtils.saveToFile(htmlFile, null, doc.getHTML("Trace","EJSS_DRAWING2D.Trace",null,null));
    org.opensourcephysics.desktop.OSPDesktop.displayURL("file:///"+FileUtils.correctUrlString(FileUtils.getPath(htmlFile)));

    /*

    // print content of a class
    System.out.println("****** CONTENT Shape");
    Map<String,String> cls = doc.getClass("EJSS_DRAWING2D.Shape");
    System.out.println("parent: " + cls.get("parent"));
    System.out.println("description: " + cls.get("description"));

    // print properties of a class
    System.out.println("****** PROPERTIES Shape");
    for(String property : doc.listProperties("EJSS_DRAWING2D.Shape")) 
      System.out.println("Property: " + property);
    // print actions of a class
    System.out.println("****** ACTIONS Shape");
    for(String action : doc.getActions("EJSS_DRAWING2D.Shape")) 
      System.out.println("Action: " + action);

    // print methods of a class
    System.out.println("****** PROPERTIES Shape");
    for(String method : doc.getMethods("EJSS_DRAWING2D.Shape")) 
      System.out.println("Methods: " + method);

    // print content of a property
    System.out.println("****** CONTENT PROPERTY ShapeType");
    Map<String,String> prop = doc.getProperty("EJSS_DRAWING2D.Shape","Size");
    System.out.println("description: " + prop.get("description"));
    System.out.println("type: " + prop.get("type"));
    System.out.println("default: " + prop.get("default"));
     */

  }   

}
