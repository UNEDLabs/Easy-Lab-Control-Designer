package org.colos.ejs.model_elements.roboticsLabs;


import java.util.Set;

import org.opensourcephysics.tools.minijar.MiniJar;

public class PackageRoboticsLabsElements {

  /**
   * Default main for packing the library (invoke main("create_jar"))
   * @param args
   */
  static public void main(String[] args) {
    String commandLine = 
      " -o ../Ejs/distribution/bin/extensions/model_elements/RoboticsLabs/roboticsLabs.jar " + // The output JAR file, to be located under in bin/extensions/model_elements
//        " -o ../Ejs/other/Hardware/hardware.jar " + // The output JAR file, to be located under in bin/extensions/model_elements
//      " -m .;../../ejs.jar;../../ejs_lib.jar;../../osp.jar org.colos.ejs.model_elements.input_output.DataReaderElement "+ // To display something and also for EJS to know the main class
      " -s bin " + // the location of the compiled classes
      " -c ../Ejs/distribution/bin/osp.jar " +  // Because this class uses standard OSP classes
      " -c ../Ejs/distribution/bin/ejs.jar " +  // Because this class uses EJS classes
      " -c ../Ejs/distribution/bin/ejs_lib.jar " +  // Because this class uses EJS classes
      " -c ../Ejs/distribution/bin/extensions/model_elements/Hardware/_utils/jme-2.0-S1.jar "+
      " -x ../Ejs/distribution/bin/osp.jar -x ../Ejs/distribution/bin/ejs.jar -x ../Ejs/distribution/bin/ejs_lib.jar -x ++Thumbs.db"+ // do not include these classes, nor MAC OS X's _Thumbs.db files
      " -x ../Ejs/distribution/bin/extensions/model_elements/Hardware/_utils/jme-2.0-S1.jar " +  
      " -x org/colos/ejs/library/utils/ModelElementsUtilities.class"+ // do not include these classes
      " -x org/colos/ejs/model_elements/roboticsLab/PackageRoboticsLabsElements.class"+ // do not include these classes
      " org/colos/ejs/model_elements/roboticsLabs/++  org/colos/roboticsLabs/++"; //org/colos/ejs/model_elements/robots/++  // get ALL files under these directories and its dependencies (for class files)
    System.out.println ("Processing "+commandLine);
    MiniJar sj = new MiniJar(commandLine.split(" "));
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+commandLine+"\n");
    System.out.println ("  Package of RoboticsLabs Elements ");
  }

}
