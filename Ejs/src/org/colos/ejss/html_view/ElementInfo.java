package org.colos.ejss.html_view;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.colos.ejs.osejs.utils.TwoStrings;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This class is used to store information about classes
 * @author Francisco Esquembre, Felix Garcia
 * @version 1.0 April 2013
 */
public class ElementInfo {
  static private final String sPropertiesFile = "/org/colos/ejss/html_view/ElementsInfo.txt";
  static private Map<String,ElementInfo> sPropertiesMap = new HashMap<String,ElementInfo>(); 
  static private Map<String,List<TwoStrings>> sConstantsMap = new HashMap<String,List<TwoStrings>>();

  static { // Process the properties file only once
    Resource res = ResourceLoader.getResource(sPropertiesFile);
    if (res==null) {
      System.err.println ("Error: critical properties file is missing : "+sPropertiesFile);
    }
    else {
      ElementInfo classInfo=null;
      try {
        BufferedReader in = new BufferedReader(res.openReader());
        String line = in.readLine().trim();
        while (line!=null) { // only lines starting with correct keywords are processed
          if (line.startsWith("class:")) {
//            System.err.println("Reading class "+line);
            if (classInfo!=null) sPropertiesMap.put(classInfo.mClassname, classInfo); // archive current class info
            String classname = line.substring(6).trim();
            classInfo = sPropertiesMap.get(classname); // Avoid repetitions
            if (classInfo==null) classInfo = new ElementInfo(classname); // create new class name
          }
          else if (line.startsWith("parent:"))  classInfo.mParent       = line.substring(7).trim();
          else if (line.startsWith("text:"))    classInfo.mTextProperty = line.substring(5).trim();
          else if (line.startsWith("import:"))  classInfo.mImportStatements.add(line.substring(7).trim());
          else if (line.startsWith("accepts:")) classInfo.mAcceptedChildren.add(line.substring(8).trim());
          else if (line.startsWith("edition:")) classInfo.mEditionBuffer.append(line.substring(8).trim());
          else if (line.startsWith("name:"))    classInfo.mPropertiesBuffer.append("{"+line+"},");
          else if (line.startsWith("defaults:"))    {
            line = line.substring(9).trim();
            for (String entry : line.split(";")) {
              entry = entry.trim();
              int index = entry.indexOf('=');
              String key,value;
              if (index>0) {
                key = entry.substring(0,index).trim();
                value = entry.substring(index+1).trim();
              }
              else key = value = entry;
//              System.out.println("Defaults: adding (key=value) = "+key+"="+value);
              classInfo.mCreationDefaults.put(key, value);
            }
          }
          else if (line.startsWith("type:"))    {
            int end = line.indexOf('=');
            if (end>=0) {
              String type = line.substring(5,end).trim();
              List<TwoStrings> list = sConstantsMap.get(type); 
              if (list==null) list = new ArrayList<TwoStrings>();
              for (String entry : line.substring(end+1).split(",")) {
                entry = entry.trim();
                int index = entry.indexOf('=');
                String key,value;
                if (index>0) {
                  key = entry.substring(0,index).trim();
                  value = entry.substring(index+1).trim();
                }
                else key = value = entry;
//                System.out.println("Type: "+type+ " adding (key=value) = "+key+"="+value);
                list.add(new TwoStrings(key, value));
              }
              sConstantsMap.put(type, list);
            }
          }
          line = in.readLine();
        }
        in.close();
        if (classInfo!=null) sPropertiesMap.put(classInfo.mClassname, classInfo); // archive current class info
      } 
      catch(IOException ex) { ex.printStackTrace(); }
    }
  }
  
  /**
   * Returns the info
   * @param classname
   * @return
   */
  static public ElementInfo getInfo(String classname) {
    return sPropertiesMap.get(classname);
  }
  
  // -------------------------------
  // Fields and constructors
  // -------------------------------

  private String mClassname;
  private String mParent       = null;
  private String mTextProperty = null;
  private Set<String> mImportStatements  = new HashSet<String>();
  private Set<String> mAcceptedChildren  = new HashSet<String>();
  private StringBuffer mPropertiesBuffer = new StringBuffer();
  private StringBuffer mEditionBuffer    = new StringBuffer();
  private Map<String,Property> mProperties = null;
  private Map<String,String> mCreationDefaults = new HashMap<String,String>();

  /**
   * Constructor
   * @param classname the name of the class
   */
  private ElementInfo(String classname) { mClassname = classname; }

  // -------------------------------
  // public methods
  // -------------------------------

  /**
   * Returns the fully qualified constructor name. Used to declare the variable. Ex. EJSS_INTERFACE.button
   */
  public String getConstructorName() { return mClassname; }

  /**
   * Returns a semicolon ';' separated list of imports to be added to the source code. Ex. EJSS_INTERFACE
   * May be empty "" if the element requires no imports
   */
  public String getImportStatements() {
    StringBuffer buffer = new StringBuffer();
    for (String oneStatement : this.getImportStatementsSet()) buffer.append(oneStatement+";");
    String importStatements = buffer.toString();
    if (importStatements.endsWith(",")) importStatements = importStatements.substring(0,importStatements.length()-1); 
    return importStatements;
  }
  
  /**
   * Whether an element of this class accepts a given child
   * @param child
   * @return
   */
  public boolean acceptsChild(ElementInfo child) {
    Set<String> acceptedChildren = getAcceptedChildren();
    for (String classname : child.getHierarchy()) {
      if (acceptedChildren.contains(classname)) return true;
    }
    return false;
  }
  
  /**
   * Whether an element inherits from a given class
   * @param classname
   * @return
   */
  public boolean instanceOf(String classname) {
    return getHierarchy().contains(classname);
  }

  // -------------------------------
  // Properties and edition
  // -------------------------------

  
  /**
   * Return the creation defaults for this element, if any
   * @return
   */
  public Map<String,String> getCreationDefaults() {
    Map<String,String> map = new HashMap<String,String>();
    map.putAll(mCreationDefaults);
    ElementInfo parent = getParent();
    if (parent!=null) {
      Map<String,String> parentMap = parent.getCreationDefaults();
      for (String key : parentMap.keySet()) {
        if (map.get(key)==null) map.put(key, parentMap.get(key));
      }
    }
    return map;
  }
  
  /**
   * Gets the text property (if any)
   * @return the property or null, if not applicable
   */
  public String getTextProperty() {
    if (mTextProperty!=null) return mTextProperty;
    ElementInfo parent = getParent();
    if (parent!=null) return parent.getTextProperty();
    return null;
  }
  
  /**
   * Gets the information about how to display the properties
   * @return
   */
  public String getEditionInfo() { 
    return mEditionBuffer.toString(); 
  }

  /**
   * Whether the element has a property with that name
   * @param name
   * @return
   */
  final public boolean hasProperty(String name){
    return getProperty(name)!=null;
  }
  
  /**
   * Gets the collection of properties for this class
   * @return
   */
  public Set<String> getProperties() {
    return buildProperties().keySet();
  }
  
  /**
   * Gets the information about a given property
   * @param property
   * @return
   */
  public String getPropertyInfo(String property) {
    Property prop = getProperty(property);
    return (prop!=null) ? prop.getInfo() : null;
  }

  /**
   * Gets the default value for a given property
   * @param property
   * @return
   */
  public String getPropertyDefault(String property) {
    Property prop = getProperty(property);
    return (prop!=null) ? prop.getDef() : null;
  }

  /**
   * Whether a given property can be of the given type
   * @param property
   * @param type
   * @return
   */
  public boolean propertyIsOfType(String property, String type) {
    String info = getPropertyInfo(property);
    return (info!=null) ? info.indexOf(type)>=0 : false;
  }
  
  /**
   * Returns the list of accepted 'special' constants for a property
   * @param  property String The property 
   * @return java.util.Map<String,String>
   */
  final public List<TwoStrings> getPropertyAcceptedConstants (Property property) {
    Collection<String> types = getPropertyTypes(property);
    for (String type : types) {
      List<TwoStrings> list = sConstantsMap.get(type);
      if (list!=null) return list;
    }
    return new ArrayList<TwoStrings>(); // accepts no constant
  }

  /**
   * Returns the list of accepted 'special' constants for a property
   * @param  property String The name of the property 
   * @return java.util.Map<String,String>
   */
  final public List<TwoStrings> getPropertyAcceptedConstants (String property) {
    return getPropertyAcceptedConstants(getProperty(property));
  }
  
  final public boolean acceptsConstant(String property, String value) {
    for (TwoStrings entry : getPropertyAcceptedConstants(property)) {
      if (entry.getSecondString().equals(value)) return true;
    }
    return false;
  }

  // -------------------------------
  // private methods
  // -------------------------------
 
  /**
   * Returns the information of the parent class (if any)
   * @return
   */
  private ElementInfo getParent() {
    if (mParent==null) return null;
    return ElementInfo.getInfo(mParent);
  }
  
  /**
   * Returns the set of all parent classes
   * @return
   */
  private Set<String> getHierarchy() {
    Set<String> set = new HashSet<String>();
    set.add(mClassname);
    ElementInfo parent = getParent();
    if (parent!=null) set.addAll(parent.getHierarchy());
    return set;
  }
  
  /**
   * Returns the Property object for the given property
   * @param name the name of the property
   * @return
   */
  final private Property getProperty(String name) {
    return buildProperties().get(name);
  }

  /**
   * Gets the collection of properties for this class
   * @return
   */
  private Map<String,Property> buildProperties() {
    if (mProperties==null) { // Build the properties once
      StringBuffer propertiesBuffer = new StringBuffer();
      for (StringBuffer buffer : getPropertiesBuffer()) propertiesBuffer.append(buffer);
      String properties = propertiesBuffer.toString();
//      System.err.println("Properties for "+mClassname +"=\n");
//      System.err.println("["+properties+"]\n");
      if (properties.endsWith(",")) properties = properties.substring(0,properties.length()-1); 
      Collection<Property> props = (new Gson()).fromJson("["+properties+"]", new TypeToken<Collection<Property>>(){}.getType());
      mProperties = new HashMap<String,Property>();
      for (Property oneProp : props) mProperties.put(oneProp.getName(), oneProp);
    }
    return mProperties;
  }

//  private Map<String,Property> buildProperties() {
//    if (mProperties==null) { // Build the properties once
//      mProperties = new HashMap<String,Property>();
//      for (StringBuffer buffer : getPropertiesBuffer()) {
//        String properties = buffer.toString();
//        if (properties.endsWith(",")) properties = properties.substring(0,properties.length()-1); 
//        Collection<Property> props = (new Gson()).fromJson("["+properties+"]", new TypeToken<Collection<Property>>(){}.getType());
//        for (Property oneProp : props) {
//          String key = oneProp.getName();
//          if (!mProperties.containsKey(key)) mProperties.put(key, oneProp); // In case a child overrides its parent definition 
//        }
//      }
//    }
//    return mProperties;
//  }

  /**
   * Returns a set of all proper and inherited properties
   * @return
   */
  private List<StringBuffer> getPropertiesBuffer() {
    List<StringBuffer> set = new ArrayList<StringBuffer>();
    ElementInfo parent = getParent();
    if (parent!=null) set.addAll(parent.getPropertiesBuffer());
    set.add(mPropertiesBuffer);
    return set;
  }
  
  /**
   * Returns a set of classes that this class accepts as children
   * @return
   */
  private Set<String> getAcceptedChildren() {
    Set<String> set = new HashSet<String>();
    set.addAll(mAcceptedChildren);
    ElementInfo parent = getParent();
    if (parent!=null) set.addAll(parent.getAcceptedChildren());
    return set;
  }

  /**
   * Gets a set of import statements required by this class
   * @return
   */
  private Set<String> getImportStatementsSet() {
    Set<String> set = new HashSet<String>(mImportStatements);
    ElementInfo parent = getParent();
    if (parent!=null) set.addAll(parent.getImportStatementsSet());
    return set;
  }
  
  /**
   * Gets the collection of types allowed for the given property
   * @param property
   * @return
   */
  final private Collection<String> getPropertyTypes(Property property) {
    if (property==null) return new java.util.HashSet<String>();
    String info = property.getInfo();
    java.util.Set<String> set = new java.util.HashSet<String>();
    StringTokenizer tkn = new StringTokenizer(info," |");
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (!token.toUpperCase().equals(token)) set.add(token);
    }
    return set;
  }

}
