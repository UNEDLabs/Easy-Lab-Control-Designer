package org.colos.ejs;

import org.opensourcephysics.tools.minijar.MiniJar;
import java.util.*;

/**
 * This class packages EjsS
 * @author Francisco Esquembre
 *
 */
public class PackageEjs {
  static private final String VERBOSE = "";
//  static private final String VERBOSE = "-v";
  
  static private final String LOCALES = VERBOSE + 
          " -o ../Ejs/distribution/bin/locales.jar -m .;ejs_lib.jar org.colos.ejs._EjsSConstants"+
          " -s ../Ejs/resources/ca_CA -s ../Ejs/resources/da_DK -s ../Ejs/resources/de_DE -s ../Ejs/resources/es_ES -s ../Ejs/resources/fr_FR "+
          " -s ../Ejs/resources/gr_GR -s ../Ejs/resources/id_ID -s ../Ejs/resources/nl_NL -s ../Ejs/resources/pl_PL -s ../Ejs/resources/ru_RU "+
          " -s ../Ejs/resources/si_SI -s ../Ejs/resources/zh_CN -s ../Ejs/resources/zh_TW  -s ../Ejs/resources/ar_DZ "+
          " org/colos/ejs/osejs/++";

//  static final String LOOK_AND_FEEL = VERBOSE + 
//  " -o distribution/bin/lookAndFeel.jar  -m .;ejs_lib.jar org.colos.ejs._EjsSConstants"+
//  " -s bin"+
//  " -x ++Thumbs.db"+
//  " com/digitprop/tonic/++";

  static final String EJS_CONSOLE = VERBOSE +  
          " -o ../Ejs/distribution/ELCDConsole.jar "+
          " -m .;bin/locales.jar;bin/ejs_lib.jar;bin/osp.jar;bin/ejs_extras_java8.jar;bin/ejs_extras_java9.jar"+
          " org.colos.ejs.osejs.EjsConsole"+
          " -s ../Ejs/bin -s ../Ejs/distribution/bin/osp.jar -s ../Ejs/distribution/bin/bcel.jar -c ../Ejs/libraries/unpackaged/classes"+
          " -x ../Ejs/distribution/bin/bcel.jar "+
          " -x ++Thumbs.db"+
  " org/colos/ejs/osejs/EjsConsole++.class org/colos/ejs/osejs/MacOSXHandler.class org/colos/ejs/osejs/resources/++"+
  " org/opensourcephysics/resources/++ data/icons/EjsLogo.gif data/icons/EjsSLogo.png data/icons/ConsoleIcon.gif data/icons/edit.gif";

  static final String EJS = VERBOSE +
          " -o ../Ejs/distribution/bin/ejs.jar  -m .;ejs_lib.jar;osp.jar;bsh.jar org.colos.ejs._EjsSConstants"+
          " -s ../Ejs/bin -c ../Ejs/libraries/unpackaged/classes"+
          " -c ../Ejs/libraries/HotEqn.jar -c ../Ejs/distribution/bin/osp.jar"+
          " -x ../Ejs/distribution/bin/osp.jar -x ../Ejs/distribution/bin/ejs_lib.jar -x org/colos/ejs/PackageEjs.class -x ++Thumbs.db"+
          " -x logback.xml "+
  " org/colos/ejs/++ org/colos/ejss/++ data/++ com/++.properties com/hexidec/ekit/icons/++"; // jtidy.properties org/w3c/tidy/++.properties";
  
  static final String COM_SUN = VERBOSE +
  " -o ../Ejs/distribution/bin/comSun9.jar -v -m . org.colos.ejs._EjsSConstants"+
  " -c ../Ejs/distribution/bin/osp.jar  "+
  " -s ../Ejs/libraries/tools.jar "+
  " -x ../Ejs/distribution/bin/osp.jar "+
  " com/sun/tools/javac/Main.class com/sun/tools/javac/resources/++";
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    processCommand (LOCALES);
//    processCommand (LOOK_AND_FEEL);
    processCommand (EJS_CONSOLE);
    processCommand (EJS); // This one always after EJS_LIBRARY
    System.out.println ("Packaging of Ejs completed!");
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
