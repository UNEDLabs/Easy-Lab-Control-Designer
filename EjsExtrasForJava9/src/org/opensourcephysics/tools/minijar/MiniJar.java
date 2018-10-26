/**
 * The minijar package contains utilities to create the minimal
 * jar file that contains a series of classes and their dependencies.
 * Copyright (c) January 2008 F. Esquembre
 * @author F. Esquembre (http://www.um.es/fem).
 */

package org.opensourcephysics.tools.minijar;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import javax.swing.filechooser.FileSystemView;

//import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.colos.ejs.osejs.OsejsCommon;

public class MiniJar {
  static private final boolean sIsJava9orLater = (OsejsCommon.getJavaVersion()>=9.0);
  static private final Set<File> bootCPlist; // The Java boot class path as a List
  static private Map<String, Map<String,Object>> jarContents = new HashMap<String, Map<String,Object>>(); // Holds a map of the contents of all jar files previously read

  /**
   * Retrieves the Java boot class path
   */
  static {
    if (sIsJava9orLater) bootCPlist = null;
    else {
      String bootClassPath = System.getProperty("sun.boot.class.path"); //$NON-NLS-1$
      bootCPlist = new HashSet<File>();
      StringTokenizer tkn = new StringTokenizer(bootClassPath, File.pathSeparator);
      while (tkn.hasMoreTokens()) {
        String path = tkn.nextToken();
        File file = new File (path);
        if (file.exists()) bootCPlist.add(file); 
      }
//      for (File file : bootCPlist) System.out.println("boot CP: "+file);

    }
  }

  /**
   * Sets the output stream for error messages
   */
  private PrintStream printStream=System.out;
  /**
   * The output file to create
   */
  private File outputFile=null;
  /**
   * The Manifest file, in case of Jar output
   */
  private Manifest manifestFile=null;
  /**
   * List of paths where to look for for desired (source) files
   */
  private Set<File> sourcePathSet=new HashSet<File>(); 
  /**
   * List of paths where to look for classes referenced in the sources
   */
  private Set<File> classpathSet=new HashSet<File>();
  /**
   *  List of patterns for files to include in the output
   */
  private Set<Pattern> desiredPatterns=new HashSet<Pattern>();
  /**
   *  List of patterns for files in given sources to include in the output
   */
  private Set<PatternAndSource> desiredPatternsFromSource=new HashSet<PatternAndSource>();
  /**
   * List of patterns of files to exclude
   */
  private Set<Pattern> excludedPatterns=new HashSet<Pattern>();
  /**
   * List of patterns of files which will be added even if excluded
   */
  private Set<Pattern> forcedPatterns = new HashSet<Pattern>();
  /**
   * List of files actually added
   */
  private Set<PathAndFile> addedSet = new HashSet<PathAndFile>();  
  /**
   * List of classes required (because of dependencies) but not found 
   */
  private Set<String> missingSet = new TreeSet<String>();
  /**
   * Whether to produce detailed information about the process
   */
  private boolean verbose = false;
  /*
   * Class path used by the BCEL routines 
   */
  //private ClassPath bcelPath;

  /**
   * Standard main program. Passes the arguments to an instance of MiniJar and then creates
   * the output. It also prints the list of missing files.
   * @param args
   */
  static public void main (String[] args) {
    MiniJar sj = new MiniJar(args);
    Set<String> missing = sj.compress();
    for (String missingFilename : missing) System.out.println ("Missing file: "+missingFilename);  //$NON-NLS-1$
    System.out.println ("Done!"); //$NON-NLS-1$
    System.exit(0);
  }

  /**
   * Frees memory used to store the contents of directories and compressed files.
   */
  static public void freeMemory () {
    jarContents.clear();  
  }

  /**
   * Empty constructor.
   */
  public MiniJar () {}

  /**
   * Constructor which processes a list of commands.
   */
  public MiniJar (String[] args) {
    processArguments (args);  
  }

  /**
   * Resets the object to its initial state (except for the PrintStream 
   * and the verbose condition).
   */
  public void reset () {
    outputFile=null;
    manifestFile=null;
    sourcePathSet=new HashSet<File>(); 
    classpathSet=new HashSet<File>();
    desiredPatterns=new HashSet<Pattern>();
    desiredPatternsFromSource=new HashSet<PatternAndSource>();
    excludedPatterns=new HashSet<Pattern>();
    forcedPatterns = new HashSet<Pattern>();
    addedSet = new HashSet<PathAndFile>();  
    missingSet = new TreeSet<String>();
  }

  /**
   * Processes a series of arguments. Each argument (or sequence s
   * of arguments) must be of the form:
   * <ul>
   *   <li> -v. Causes the process to print debug information
   *   <li> -o output.jar OR -o output.zip. The output file must be set before compressing. 
   *   <li> -m classpath mainclass (no .class extension!). Manifest information for the generated JAR file.
   *   <li> -s directory OR -s file.jar OR -s file.zip. Adds a directory or compressed file where to search for files which match the given patterns.
   *        It implies the -c switch for that directory.
   *   <li> -c directory OR -c file.jar OR -c file.zip. Adds a directory or compressed file for searching additional references made in the selected Java classes.
   *   <li> -x pattern. Excludes all files which match the pattern in any of the search (either -s or -c) directories.
   *   <li> -x file.jar OR -X file.zip. Excludes all files in the given compressed file.
   *   <li> -f pattern. Forces the search to include all files which match the pattern in any of the search (-s) directories, 
   *   even if they are excluded by any -x switch.
   *   <li> -r pattern source. Include files which math the pattern but only if under the given source. Source is either a directory or a compressed file. 
   *   <li> pattern. Include all files which match the pattern in any of the search (-s) directories.
   *   Including a Java class files will make all its dependent classes to be included, too.
   * </ul>
   *  Switches are case-insensitive, arguments are not.
   *   Examples of patterns are: 
   * <ul>
   *   <li> <tt>dir1/dir2/filename.ext</tt> : The file in the dir1/dir2 directory with the given name and extension.
   *   <li> <tt>dir1/dir2/+.ext</tt> : Any file in the dir1/dir2 directory with the given extension.
   *   <li> <tt>dir1/dir2/whatever+.ext</tt> : Any file in the dir1/dir2 directory, with a name 
   *            that starts with 'whatever', and with the given extension.  
   *   <li> <tt>dir1/dir2/++.ext</tt> : Any file at any level under the dir1/dir2 directory with the given extension.
   *   <li> <tt>dir1/dir2/++</tt> : Any file at any level under the dir1/dir2 directory.
   * </ul>
   * (Note: We don't use the standard wild char '*' because the operating system converts it before
   * passing it to the java program.)
   * @param args
   */
  public void processArguments (String[] args) {
    for (int i=0; i<args.length; i++) {
      // outputStream.println ("arg["+i+"] = "+args[i]);
      String keyword = args[i].toLowerCase();
      if (keyword.equals("-v")) setVerbose(true); //$NON-NLS-1$
      else if (keyword.equals("-o")) {  //$NON-NLS-1$
        if (i+1<args.length) setOutputFile(args[++i]);
      }
      else if (keyword.equals("-m")) {  // -m classpath mainclass //$NON-NLS-1$
        if (i+2<args.length) setManifestFile(createManifest(args[++i],args[++i]));
      }
      else if (keyword.equals("-s")) {  // -s sourcepath //$NON-NLS-1$
        if (i+1<args.length) addSourcePath(args[++i]);
      }
      else if (keyword.equals("-c")) {  // -c classpath //$NON-NLS-1$
        if (i+1<args.length) addClasspath(args[++i]);
      }
      else if (keyword.equals("-x")) { // -x pattern  //$NON-NLS-1$
        if (i+1<args.length) addExclude(args[++i]);
      }
      else if (keyword.equals("-f")) { // -f pattern   //$NON-NLS-1$
        if (i+1<args.length) addForced(args[++i]);
      }
      else if (keyword.equals("-r")) { // -r pattern sourcepath   //$NON-NLS-1$
        if (i+1<args.length) addDesiredFrom(args[++i],args[++i]);
      }
      else addDesired (args[i]);
    }
  }

  /**
   * Sets the output stream for messages. Default is outputStream.
   * @param output
   */
  public void setPrintStream (PrintStream output) { this.printStream = output; }

  /**
   * Makes the processes to print debug information if the argument is true.
   * By default, the processes only prints error messages, if any.
   * @param verbose
   */
  public void setVerbose (boolean verbose) { this.verbose = verbose; }

  /**
   * Sets the output file to the given path. 
   * @param path The path for the desired compressed file, which must end with either ".zip" or ".jar".
   */
  public void setOutputFile (String path) {
    String pathLower = path.toLowerCase();
    if (! (pathLower.endsWith(".jar") || pathLower.endsWith(".zip")) ) { //$NON-NLS-1$ //$NON-NLS-2$
      printStream.println ("ERROR: incorrect extension for output file: "+path); //$NON-NLS-1$
      return;
    }
    outputFile = new File(path);
  }

  /**
   * Sets the output file 
   * @param file The desired output file, its name should end with either ".zip" or ".jar".
   */
  public void setOutputFile (File target) {
    String name = target.getName().toLowerCase();
    if (! (name.endsWith(".jar") || name.endsWith(".zip")) ) { //$NON-NLS-1$ //$NON-NLS-2$
      printStream.println ("ERROR: incorrect extension for output file: "+target.getAbsolutePath()); //$NON-NLS-1$
      return;
    }
    outputFile = target;
  }

  /**
   * Sets the manifest file to use for JAR files.
   * @param manifest
   * @see createManifest(String, String)
   */
  public void setManifestFile (Manifest manifest) { manifestFile = manifest; }

  /**
   * Adds a source path, i.e. a directory or compressed file where to search for files which match the given patterns.
   * It implies addClasspath() for that directory.
   * @param path
   */
  public void addSourcePath (String path) {
    //printStream.println ("Add source "+path);
    File pathFile = new File(path);
    if (pathFile.exists() && 
        (pathFile.isDirectory() || path.toLowerCase().endsWith(".jar") || path.toLowerCase().endsWith(".zip")) ) { //$NON-NLS-1$ //$NON-NLS-2$
      sourcePathSet.add(pathFile);
      classpathSet.add(pathFile);
    }
    else printStream.println ("ERROR: Source path is not a valid directory or compressed file: "+path); //$NON-NLS-1$
  }

  /**
   * Adds a directory or compressed file for searching additional references made in the selected Java classes.
   * @param path
   */
  public void addClasspath (String path) {
    File pathFile = new File(path);
    if (pathFile.exists() && 
        (pathFile.isDirectory() || path.toLowerCase().endsWith(".jar") || path.toLowerCase().endsWith(".zip")) ) { //$NON-NLS-1$ //$NON-NLS-2$
      classpathSet.add(pathFile);
    }
    else printStream.println ("ERROR: Search path is not a valid directory or compressed file: "+path); //$NON-NLS-1$
  }

  /**
   * Excludes from the output all files which match the pattern in any of the search (either -s or -c) directories.
   * If the pattern is the path for a ZIP or JAR file, all files in the compressed file will be excluded.
   */
  public void addExclude (String pattern) {
    //printStream.println ("Add excluded "+pattern);
    if (pattern.toLowerCase().endsWith(".jar") || pattern.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$ //$NON-NLS-2$
      File file = new File (pattern);
      if (file.exists()) excludedPatterns.add(new Pattern(file));
      else printStream.println ("ERROR: Excluded compressed file does not exist: "+pattern); //$NON-NLS-1$
    }
    else excludedPatterns.add(new Pattern(pattern));
  }

  /**
   * Excludes from the output all files in a given ZIP or JAR file.
   */
  public void addExclude (File compressedFile) {
    //printStream.println ("Add excluded "+pattern);
    String filenameLC = compressedFile.getName().toLowerCase();
    if (filenameLC.endsWith(".jar") || filenameLC.endsWith(".zip")) { //$NON-NLS-1$ //$NON-NLS-2$
      if (compressedFile.exists()) excludedPatterns.add(new Pattern(compressedFile));
      else printStream.println ("ERROR: Excluded compressed file does not exist: "+compressedFile.getAbsolutePath()); //$NON-NLS-1$
    }
    else printStream.println ("ERROR: Excluded file is not a compressed file: "+compressedFile.getAbsolutePath()); //$NON-NLS-1$
  }

  /**
   * Forces the output to include files that match the given pattern, even if a exclude pattern 
   * suggests to exclude them.
   * @param pattern
   */
  public void addForced (String pattern) {
    //printStream.println ("Add forced "+pattern);
    if (pattern.toLowerCase().endsWith(".jar") || pattern.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$ //$NON-NLS-2$
      File file = new File (pattern);
      if (file.exists()) forcedPatterns.add(new Pattern(file));
      else printStream.println ("ERROR: Forced compressed file does not exist: "+pattern); //$NON-NLS-1$
    }
    else forcedPatterns.add(new Pattern(pattern));
  }

  /**
   * Add files matching the pattern to the output.
   * @param pattern
   */
  public void addDesired (String pattern) {
    if (pattern.toLowerCase().endsWith(".jar") || pattern.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$ //$NON-NLS-2$
      File file = new File (pattern);
      if (file.exists()) desiredPatterns.add(new Pattern(file));
      else printStream.println ("ERROR: Desired compressed file does not exist: "+pattern); //$NON-NLS-1$
    }
    else desiredPatterns.add(new Pattern(pattern));
  }

  /**
   * Add files matching the pattern to the output but only from the given source
   * @param pattern
   */
  public void addDesiredFrom (String pattern, String path) {
    //printStream.println ("Add desired "+pattern+ " from "+path);
    File pathFile = new File(path);
    if (! (pathFile.exists() && 
          (pathFile.isDirectory() || path.toLowerCase().endsWith(".jar") || path.toLowerCase().endsWith(".zip")) ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      printStream.println ("ERROR: Search path is not a valid directory or compressed file: "+path); //$NON-NLS-1$
      return;
    }
    if (pattern.toLowerCase().endsWith(".jar") || pattern.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$ //$NON-NLS-2$
      File file = new File (pattern);
      if (file.exists()) desiredPatternsFromSource.add(new PatternAndSource(new Pattern(file),pathFile));
      else printStream.println ("ERROR: Desired compressed file does not exist: "+pattern); //$NON-NLS-1$
    }
    else desiredPatternsFromSource.add(new PatternAndSource(new Pattern(pattern),pathFile));
  }
 
  // Methods to start the process 

  /**
   * Starts the process and creates the output file.
   * @return a list of missing files, i.e. classes that are referenced by the classes included. 
   * Standard Java classes are not listed.
   */
  public Set<String> compress () { 
    compress(getMatches(),outputFile,manifestFile);
    return this.missingSet;
  }

  /**
   * Compresses a given list of matches. This allows the user to customize the list of matches.
   * @return a list of missing files, i.e. classes that are referenced by the classes included. 
   * Standard Java classes are not listed.
   */
  public Set<String> compress (Set<PathAndFile> matches) { 
    compress(matches,outputFile,manifestFile);
    return this.missingSet;
  }

  /**
   * Creates the list of files that match the instruction set.
   * @return
   */
  public Set<PathAndFile> getMatches () {
    missingSet = new TreeSet<String>();
    addedSet = new HashSet<PathAndFile>();
    if (outputFile==null || outputFile.getName().toLowerCase().endsWith(".jar")) addExclude ("META-INF/"+Pattern.DOUBLE_MAGIC); // Do not copy any file in META-INF //$NON-NLS-1$ //$NON-NLS-2$
    for (File source : sourcePathSet) { // search in the source paths
      Map<String,Object> contents = getContentsMap (source);
      if (contents==null) {
        printStream.println ("ERROR: No contents for source "+source.getAbsolutePath()); //$NON-NLS-1$
        continue;
      }
      if (verbose) printStream.println ("DEBUG: Processing "+source.getAbsolutePath()); //$NON-NLS-1$
      for (String path : contents.keySet()) {
        if (verbose) printStream.println ("DEBUG: Considering file "+path); //$NON-NLS-1$
        if (isForced(path)) addToList(source,path,contents.get(path),true);
        else if (isDesired(path)) addToList(source,path,contents.get(path),false);
        else if (isDesiredFromSource(path,source)) addToList(source,path,contents.get(path),false);
        //else if (verbose) printStream.println ("DEBUG: NOT Desired "+canonicalPath);
      }
    }
    return addedSet;
  }

  /**
   * Returns the list of missing files after a match search.
   * @return
   */
  public Set<String> getMissingFilesList () { return this.missingSet; }

  // -----------------
  // Private methods 
  // -----------------

  /**
   * Adds the entry to the list (and removes it from missing) if it is not excluded or already there
   * @param source the source where the file is (a directory or compressed file)
   * @param path the path of the file
   * @param file the object representing the file to add (File or ZipEntry)
   * @param force whether it is a forced addition
   */
  private void addToList (File source, String path, Object file, boolean forced) {
    if (verbose) {
      if (forced) printStream.println ("DEBUG: Forced "+path); //$NON-NLS-1$
      else printStream.println ("DEBUG: Desired "+path); //$NON-NLS-1$
    }
    if (!forced && isExcluded(path)) { if (verbose) printStream.println ("DEBUG: Excluded "+path); return; } //$NON-NLS-1$
    if (isAlreadyListed(path)) { if (verbose) printStream.println ("DEBUG: already in list "+path); return; } //$NON-NLS-1$
    if (verbose) printStream.println ("DEBUG: added "+path); //$NON-NLS-1$
    PathAndFile paf;
    if (source.isDirectory()) paf = new PathAndFile(path,(File) file); 
    else paf = new PathAndFile(path, source, (ZipEntry) file);
    addedSet.add(paf);
    missingSet.remove(path);
    if (path.endsWith(".class")) { //$NON-NLS-1$
      try { handleReferences(paf.getInputStream(), path.substring(0, path.length() - 6)); }
      catch (Exception exc) { printStream.println ("WARNING: Can't handle references for class "+path); } //$NON-NLS-1$
    }
  }

  void processClass(String path) {
    if (verbose) printStream.println ("DEBUG: Referenced "+path); //$NON-NLS-1$
    if (isSystemClass(path)) { if (verbose) printStream.println ("DEBUG: is system class "+path); return; } //$NON-NLS-1$
    for (File source : classpathSet) {
      Object file = sourceFind(source,path);
      if (file!=null) { addToList(source,path,file,false); return; } 
    }
    if (verbose) printStream.println ("DEBUG: Not found "+path); //$NON-NLS-1$
    missingSet.add(path);
  }

  private boolean isAlreadyListed(String filename) {
    for (PathAndFile paf : addedSet) {
      if (paf.getPath().equals(filename)) return true;
    }
    return false;
  }

  private boolean isDesired (String filename) {
    for (Pattern pattern : desiredPatterns) {
      if (pattern.matches(filename)) return true;
    }
    return false;
  }

  private boolean isDesiredFromSource (String filename, File source) {
    for (PatternAndSource pas : desiredPatternsFromSource) {
      if (pas.source.equals(source) && pas.pattern.matches(filename)) return true;
    }
    return false;
  }

  private boolean isForced (String filename) {
    for (Pattern pattern : forcedPatterns) {
      if (pattern.matches(filename)) return true;
    }
    return false;
  }

  private boolean isExcluded (String filename) {
    for (Pattern pattern : excludedPatterns) {
      if (pattern.matches(filename)) return true;
    }
    return false;
  }

  public boolean isSystemClass (String path) {
    if (sIsJava9orLater) {
      try {
        path = path.replace('/', '.');
        if (path.endsWith(".class")) path = path.substring(0,path.length()-6);
        Class<?> classToCheck = Class.forName(path);
        String moduleName = classToCheck.getModule().getName();
        if (moduleName==null) return false;
        return moduleName.startsWith("java.");
      } catch (Exception exc) {
          return false;
      } 
    }
    for (File file : bootCPlist) {
      if (sourceFind(file,path)!=null) return true;
    }
    return false;
  }

  //----------------------------------------------------------------------

  /** Scan class file for class references 
   *  
   *  @param in       Stream reading from class file
   *  @param bcelName BCEL class name in the form pack1/pack2/name
   */

  private void handleReferences(InputStream in, String bcelName) throws IOException {
    if (verbose) printStream.println ("Handling references of class "+bcelName); //$NON-NLS-1$
    JavaClass       klass = new ClassParser(in, bcelName).parse();
    Avisitor        visitor = new Avisitor(this, klass);

    new DescendingVisitor(klass, visitor).visit();
    in.close();
  }
  /*
        if (!dynamic) return;          // no -d option

        ConstantPoolGen pool = new ConstantPoolGen(klass.getConstantPool());
        // look for dynamically loaded classes
        int indexForName = visitor.getIndexForName();
        if (indexForName < 0) return;   // no Class.forName()

        // Scan all methods for invocations of Class.forName("string").
        Method[] methods = klass.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            Method  method = methods[i];
            Code    code = method.getCode();
            if (code == null 
                    || method.getName().endsWith("$")           // javac
                    || method.getName().equals("class")         // jikes
                )  continue;

            // Wrap code into InstructionList
            InstructionList instructions = null;
            try  { instructions = new InstructionList(code.getCode()); }
            catch (ClassGenException ex)  {
              printStream.println(code.toString(), ex.getStackTrace());
              System.exit(1);
            }

            // Iterate through instructions
            for (Iterator it = instructions.iterator(); it.hasNext(); ) {
                InstructionHandle   handle = (InstructionHandle)it.next();
                Instruction         instruction = handle.getInstruction();

                if (instruction instanceof InvokeInstruction)  {
                    // get Instruction from Pool
                    ConstantCP constant  = (ConstantCP)pool.getConstant(((InvokeInstruction)instruction).getIndex());        
                    if (constant.getNameAndTypeIndex() == indexForName)  {
                        // found Invocation...
                        Instruction pre = handle.getPrev().getInstruction();
                        if (pre.getOpcode() == Constants.LDC) {      // pre-instruction loads constant
                            // ... with constant (String) operand
                            LDC ldc = (LDC)pre;
                            String  operand = (String)ldc.getValue(pool);   // operand is constant string
                            boolean found = false; 
                            String  bcelop = operand.replace('.', '/') + ".class";
                            found = dynamic == DYN_AUTO && lookupClassInternal(bcelop);

                            String message = "* Dynamic loading: class " + klass.getClassName() 
                                    + ", method " + method.getName() 
                                    + ", name=\"" + operand + "\"";

                            if (found)
                                message += " (RESOLVED)";

                            printStream.println (message);
                        }
                        else {
                            // ... with computed operand

                            forname.add("class " + klass.getClassName() + ", method " + method.getName());
                        }
                    }
                }
            }
        }
    }
    }
   */

  // -----------------
  // Utility static methods
  // The following methods should be in org.opensourcephysics.tools.JarTool
  // -----------------


  /**
   * Compresses the list of files into a JAR or ZIP file.
   * If the target file exists it will be overwritten.
   * @param list List The list of <File>s to compress
   * @param target File The output file
   * @param manifest Manifest The manifest (in case of a JAR file)
   * @return boolean
   */
  static public boolean compress (Set<PathAndFile> list, File target, Manifest manifest) {
    try {
      //System.out.println ("Compressing items "+list.size());
      if (list==null || list.size()<=0) return false;
      if (target.exists()) target.delete(); // Remove the previous JAR file
      ZipOutputStream output = null;
      boolean isJar = target.getName().toLowerCase().endsWith(".jar"); //$NON-NLS-1$
      if (isJar) {
        if (manifest!=null) output = new JarOutputStream(new FileOutputStream(target),manifest);
        else output = new JarOutputStream(new FileOutputStream(target));
      }
      else output = new ZipOutputStream(new FileOutputStream(target));
      // Create the compressed file
      byte[] buffer = new byte[2048]; // Allocate a buffer for reading entry data.
      int    bytesRead;
      for (PathAndFile paf : list) {
        // System.out.println ("Compressing "+paf.getPath());
        // TODO: Check if this creates empty directories if (fan.getFile().isDirectory()) continue; // skip directories
        InputStream input = paf.getInputStream();
        // Write the entry to the new compressed file.
        if (isJar) output.putNextEntry(new JarEntry(paf.getPath()));
        else output.putNextEntry(new ZipEntry(paf.getPath()));
        while ((bytesRead = input.read(buffer)) != -1) output.write(buffer, 0, bytesRead);
        input.close();
        output.closeEntry();
      }
      output.close();
    } catch (Exception exc) { exc.printStackTrace(); return false; }
    return true;
  }

  /**
   * Whether the given directory or compressed (ZIP or JAR) file contains a file
   * with that name under it (at any level)
   * @param source File The directory or compressed file 
   * @param filename String The path of the file to extract
   * @return boolean true if there is no error and the file is there, false otherwise
   */
  static public Object sourceFind(File source, String filename) {
    if (source.exists()==false || filename==null || filename.trim().length()<1) return false;
    Map<String,Object> contents = getContentsMap(source);
    if (contents==null) return null;
    return contents.get(filename);
  }

  /**
   * Retrieves or initializes the Map entry in jarContents for this directory or compressed file
   * @return Map The map created
   */
  static private Map<String,Object> getContentsMap(File source) {
    try {
      // get contents Map of filename to ZipEntry for source jar
      Map<String,Object> contents = jarContents.get(source.getPath());
      if (contents == null) {
        // create new Map and fill it
//        contents = new java.util.concurrent.ConcurrentSkipListMap<String,Object>(); // This Map class orders the entries
        contents = new HashMap<String,Object>(); // This Map does not order the entries
        jarContents.put(source.getPath(), contents);
        if (source.isDirectory()) recursiveFillDirectoryMap (contents, source,"",FileSystemView.getFileSystemView()); //$NON-NLS-1$
        else { // Read content of the compressed (ZIP or JAR) file
//          System.err.println ("Getting contents map of "+source.getPath());
          ZipInputStream input = new ZipInputStream(new FileInputStream(source));
          ZipEntry zipEntry = null;
          while ( (zipEntry =input.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) continue; // don't include directories
            //System.out.println("Adding "+zipEntry.getName());
            contents.put(zipEntry.getName(),zipEntry);
          }
          input.close();
        }
      }
      return contents;
    } catch (Exception ex) { ex.printStackTrace();}
    return null;
  }

  /**
   * Used by getContentMap
   * @param map Map the content map to fill
   * @param directory File the directory to process
   * @param parentPath String the path of the parent directory
   * @param fsView FileSystemView
   */
  static private void recursiveFillDirectoryMap (Map<String,Object> map, File directory, String parentPath, FileSystemView fsView) {
    File files[] = fsView.getFiles(directory, false);
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) recursiveFillDirectoryMap (map,files[i],parentPath+files[i].getName()+"/",fsView); //$NON-NLS-1$
      else map.put (parentPath+files[i].getName(),files[i]);
    }
  }

  /**
   * Creates a Manifest for a JAR file with the given parameters
   * @param classpath String
   * @param mainclass String
   * @return Manifest
   */
  static public Manifest createManifest (String classpath, String mainclass) {
    return createManifest(classpath,mainclass,null);
  }

  /**
   * Creates a Manifest for a JAR file with the given parameters
   * @param classpath String
   * @param mainclass String
   * @return Manifest
   */
  static public Manifest createManifest (String classpath, String mainclass, Set<String> extraInfo) {
    if (classpath==null || mainclass==null) return null;
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy"); //$NON-NLS-1$
    Calendar cal = Calendar.getInstance();
    String date = sdf.format(cal.getTime());
    try {
      classpath = classpath.replace(';', ' ');
      classpath = classpath.replace(',', ' ');
      classpath = classpath.replace(':', ' ');
      StringBuffer manifestStr = new StringBuffer();
      manifestStr.append("Manifest-Version: 1.0\n"); //$NON-NLS-1$
      manifestStr.append("Built-By: Open Source Physics MiniJar Tool\n"); //$NON-NLS-1$
      manifestStr.append("Build-Date: "+date+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      manifestStr.append("Class-Path: "+classpath+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      manifestStr.append("Main-Class: "+mainclass+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      if (extraInfo!=null) for (String line : extraInfo) manifestStr.append(line+"\n"); //$NON-NLS-1$ 
      manifestStr.append("\n"); //$NON-NLS-1$
      InputStream mis = new ByteArrayInputStream(manifestStr.toString().getBytes("UTF-8")); //$NON-NLS-1$
      return (new Manifest(mis));
    } catch (Exception exc) { exc.printStackTrace(); return null; }
  }

  static private class PatternAndSource {
    Pattern pattern;
    File source;
    PatternAndSource (Pattern _pattern, File _source) {
      pattern = _pattern;
      source = _source;
    }
  }
  
}
