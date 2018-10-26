package es.uned.dia.softwarelinks;

import java.util.Set;

import org.opensourcephysics.tools.minijar.MiniJar;

public class PackageElements {

  /**
   * Default main for packing the library (invoke main("create_jar"))
   * @param args
   */
  static public void main(String[] args) {
    String commandLine = 
      " -o ../Ejs/distribution/bin/extensions/model_elements/SoftwareLinks/SoftwareLinks.jar " + // The output JAR file, to be located under in bin/extensions/model_elements
//      " -m .;../../ejs.jar;../../ejs_lib.jar;../../osp.jar org.colos.ejs.model_elements.input_output.DataReaderElement "+ // To display something and also for EJS to know the main class
      " -s ../ModelElements/bin " + // the location of the compiled classes
      " -c ../Ejs/distribution/bin/osp.jar " +  // Because this class uses standard OSP classes
      " -c ../Ejs/distribution/bin/ejs.jar " +  // Because this class uses EJS classes
      " -c ../Ejs/distribution/bin/bsh.jar " +  // Because this class uses EJS classes
      " -s ../ModelElements/lib/XML-RPC-Request/xmlrpc-common-3.1.3.jar" +  // Because this class uses EJS classes
      " -s ../ModelElements/lib/XML-RPC-Request/xmlrpc-client.jar" +  // Because this class uses EJS classes
      " -s ../ModelElements/lib/XML-RPC-Request/org-apache-ws-commons-util.jar" +
      " -s ../ModelElements/lib/XML-RPC-Request/org-apache-commons-logging.jar" +
      " -s ../ModelElements/lib/SoftwareLinks/rpcmatlab-common.jar" +
      " -s ../ModelElements/lib/SoftwareLinks/libjsonrpc.jar" +
      " -s ../Ejs/distribution/bin/extensions/_utils/javax.json-api-1.0.jar" +
      " -s ../Ejs/distribution/bin/extensions/_utils/javax.json-1.0.4.jar" +
      " -s ../ModelElements/lib/SoftwareLinks/httpclient-4.3.2.jar" +
      " -s ../ModelElements/lib/SoftwareLinks/httpcore-4.3.1.jar" +
      " -s ../ModelElements/lib/matlabcontrol-4.1.0.jar" +
      " -f org/glassfish/json/++" +
      //" org/apache/++"+
      " -x es/uned/dia/softwarelinks/matlab/test/++" +
      " -x ../Ejs/distribution/bin/osp.jar -x ../Ejs/distribution/bin/ejs.jar -x ../Ejs/distribution/bin/ejs_lib.jar -x ++Thumbs.db"+ // do not include these classes, nor MAC OS X's _Thumbs.db files
      " es/uned/dia/softwarelinks/++ "; // get ALL files under this directory and its dependencies (for class files)
    System.out.println ("Processing "+commandLine);
    MiniJar sj = new MiniJar(commandLine.split(" "));
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+commandLine+"\n");
  }

}