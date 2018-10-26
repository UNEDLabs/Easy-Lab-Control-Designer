package es.uhu.augmented_reality;


import java.util.Set;

import org.opensourcephysics.tools.minijar.MiniJar;

public class PackageElements {

  /**
   * Default main for packing the library (invoke main("create_jar"))
   * @param args
   */
  static public void main(String[] args) {
    String commandLine = 
        " -o ../Ejs/distribution/bin/extensions/_utils/ARElement.jar " +// The output JAR file, to be located under in bin/extensions/model_elements
//            " -o ../../EJS/EJS_EXP_AR/EJS_4.3.7/bin/extensions/model_elements/Hardware/ARSystem.jar " +
            //        " -o ../Ejs/other/Hardware/hardware.jar " + // The output JAR file, to be located under in bin/extensions/model_elements
            //      " -m .;../../ejs.jar;../../ejs_lib.jar;../../osp.jar org.colos.ejs.model_elements.input_output.DataReaderElement "+ // To display something and also for EJS to know the main class
            " -s bin " + // the location of the compiled classes
            " -c ../Ejs/distribution/bin/osp.jar " +  // Because this class uses standard OSP classes
            " -c ../Ejs/distribution/bin/ejs.jar " +  // Because this class uses EJS classes
            " -c ../Ejs/distribution/bin/ejs_lib.jar " +  // Because this class uses EJS classes
            " -c ../ModelElements/lib/nyar4psg-1.2.0/library/NyAR4psg.jar " +  
            " -c ../ModelElements/lib/nyar4psg-1.2.0/library/NyARToolkit.jar " +  
            " -c ../ModelElements/lib/processing-1.5.1/lib/core.jar " + 
            " -c ../ModelElements/lib/IPCapture/library/IPCapture.jar " + 
            " -c ../Ejs/distribution/bin/extensions/_utils/gstreamer-java.jar " + 
            " -c ../Ejs/distribution/bin/extensions/_utils/jna.jar " + 
//            " -c ../Ejs/distribution/bin/extensions/model_elements/Hardware/_utils/GSVideo.jar " + 
            " -x ../Ejs/distribution/bin/osp.jar "+
            " -x ../Ejs/distribution/bin/ejs.jar "+ 
            " -x ../Ejs/distribution/bin/ejs_lib.jar "+
            " -x ++Thumbs.db" + // do not include these classes, nor MAC OS X's _Thumbs.db files
            " -x ../Ejs/distribution/bin/extensions/_utils/gstreamer-java.jar " + 
            " -x ../Ejs/distribution/bin/extensions/_utils/jna.jar " + 
//            " -x ../Ejs/distribution/bin/extensions/model_elements/Hardware/_utils/GSVideo.jar " +
            " -x org/colos/ejs/library/utils/ModelElementsUtilities.class"+ // do not include these classes
            " es/uhu/augmented_reality/ElementLocalAR.class es/uhu/augmented_reality/ElementRemoteAR.class " +
            " es/uhu/augmented_reality/data/cameras/++ es/uhu/augmented_reality/data/patterns/++ "; // get ALL files under these directories and its dependencies (for class files)
    System.out.println ("Processing "+commandLine);
    MiniJar sj = new MiniJar(commandLine.split(" "));
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+commandLine+"\n");
  }

}
