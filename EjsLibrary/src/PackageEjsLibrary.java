import org.opensourcephysics.tools.minijar.MiniJar;
import java.util.*;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class PackageEjsLibrary {
  static private final String VERBOSE = "";
//  static private final String VERBOSE = "-v";
  
  static final String EJS_LIBRARY = VERBOSE + 
  "-o Ejs/distribution/bin/ejs_lib.jar  -m . org.colos.ejs.library._EjsConstants"+
  " -s EjsLibrary/bin -s Ejs/libraries/HotEqn.jar -c Ejs/distribution/bin/osp.jar"+
  " -s EjsLibrary/libraries/java3d/j3dcore.jar -s EjsLibrary/libraries/java3d/j3dutils.jar -s EjsLibrary/libraries/java3d/vecmath.jar"+
  " -x Ejs/distribution/bin/osp.jar"+
  " -x EjsLibrary/libraries/java3d/j3dcore.jar -s EjsLibrary/libraries/java3d/j3dutils.jar -s EjsLibrary/libraries/java3d/vecmath.jar"+
  " -x Ejs/distribution/bin/bcel.jar"+
  " -x ++Thumbs.db"+
  " org/opensourcephysics/swing/++.class org/opensourcephysics/swing/images/++"+
  " org/colos/freefem/++.class"+
  " org/opensourcephysics/numerics/++.class"+
  " org/opensourcephysics/resources/controls/images/++"+
  " org.glassfish.json.JsonProviderImpl.class"+
  " org/opensourcephysics/tools/ToolForDataFull.class "+
  " org/colos/ejs/library/++ com/charliemouse/++.gif"+
  " org/java_websocket/++"+
  " org/opensourcephysics/tools/++" +
  " logback.xml "+
  " Ejs/libraries/HotEqn.jar";
    
  /**
   * @param args
   */
  public static void main(String[] args) {
    processCommand (EJS_LIBRARY);
    System.out.println ("Packaging of EjsLibrary completed!");
    System.exit(0);
  }

  static void processCommand (String command) {
    System.out.println ("Processing "+command);
    String[] args = command.split(" ");
    // Remove ';' that should be blanks (needed for class paths in manifests) 
    for (int i=0; i<args.length; i++) args[i] = args[i].replace(';', ' ');
    MiniJar sj = new MiniJar(args);
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+command+"\n");
  }

}
