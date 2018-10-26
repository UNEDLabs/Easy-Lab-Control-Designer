package org.colos.ejss.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

/**
 * A utility class to compress JavaScript source code.
 * 
 * The uglify script is resulted from merging of the following two scripts: parse-js.js, process.js.
 * <p/>
 * {@link https://github.com/mishoo/UglifyJS}.
 * <p/>
 *  
 */
public class JSUglify {
	    private final ScriptableObject scope;
	    private final ContextFactory contextFactory;
	    
	    // Results
	    private long mDuration = 0;
	    private String mOutput;
		
	    // Uncomment to enable the rhino debugger.
	    // static {
	    // org.mozilla.javascript.tools.debugger.Main.mainEmbedded(null);
	    // }

	    /**
	     * Create a new JSUglify object.
	     * @param reader a Reader over JSUglify.js
	     * @throws IOException 
	     */
	    JSUglify(Reader reader) throws IOException {
	        final String scriptInit = "var exports = {}; function require() {return exports;}; var process={version:0.1};";	        

	        contextFactory = new ContextFactory();
	        Context cx = contextFactory.enterContext();
	        scope = cx.initStandardObjects();
	        cx.evaluateString(scope, scriptInit, "INIT", 1, null);	        
	        cx.evaluateReader(scope, reader, "JSUGLIFY", 1, null);

		    // we should no longer be updating this.
	        this.scope.sealObject();
	    }
	    
	    /**
	     * Check for problems in a Reader which contains JavaScript source.
	     *
	     * @param systemId a identifier
	     * @param reader a Reader over JavaScript source code.
	     */
	    public void uglify(String systemId, Reader reader) throws IOException {
	        uglify(systemId, JSUglify.readerToString(reader));
	    }

	    /**
	     * Check for problems in JavaScript source.
	     *
	     * @param systemId a identifier
	     * @param javaScript a String of JavaScript source code.
	     */
	    public void uglify(String systemId, String javaScript) {
	        long before = System.nanoTime();
	        doUglify(javaScript);
	        long after = System.nanoTime();
	        mDuration = TimeUnit.NANOSECONDS.toMillis(after - before);
	    }

	    /**
	     * Get JSUglify output.
	     * 
	     * @return a javascript code.
	     */
	    public String getOutput() {
	      return mOutput;
	    }
	    
	    /**
	     * Get uglify report duration.
	     * 
	     * @return long
	     */
	    public long getDuration() {
	    	return mDuration;
	    }
	    
	    /**
	     * Do uglify.
	     * 
	     * @param javaScript
	     */
	    private void doUglify(final String javaScript) {
	        contextFactory.call(new ContextAction() {
	            public Object run(Context cx) {
	                String src = javaScript == null ? "" : javaScript;
	      	      	
	                final String originalCode = toJSMultiLineString(src);
					final StringBuffer sb = new StringBuffer("(function() {");
					sb.append("var orig_code = " + originalCode + ";");
					sb.append("var ast = jsp.parse(orig_code);");
					sb.append("ast = exports.ast_mangle(ast);");
					sb.append("ast = exports.ast_squeeze(ast);");
					sb.append("return exports.gen_code(ast, {beautify: false });");
					sb.append("})();");
					
					mOutput = cx.evaluateString(scope, sb.toString(), "uglifyIt", 1, null).toString();
					
	                return null;
	            }
	        });
	    }
	    
	    /**
	     * Read all of a Reader into memory as a String.
	     *
	     * @param reader
	     * @return
	     * @throws IOException
	     */
	    static String readerToString(Reader reader) throws IOException {
	        StringBuffer sb = new StringBuffer();
	        int c;
	        while ((c = reader.read()) != -1) {
	            sb.append((char) c);
	        }
	        return sb.toString();
	    }

	    /*
	     * Transforms a java multi-line string into javascript multi-line string.
	     */
	    static String toJSMultiLineString(final String data) {
	      final String[] lines = data.split("\n");
	      final StringBuffer result = new StringBuffer("[");
	      if (lines.length == 0) {
	        result.append("\"\"");
	      }
	      for (int i = 0; i < lines.length; i++) {
	        final String line = lines[i];
	        result.append("\"");
	        result.append(line.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\r|\\n", ""));
	        //this is used to force a single line to have at least one new line (otherwise cssLint fails).
	        if (lines.length == 1) {
	          result.append("\\n");
	        }
	        result.append("\"");
	        if (i < lines.length - 1) {
	          result.append(",");
	        }
	      }
	      result.append("].join(\"\\n\")");
	      return result.toString();
	    }	    
}
