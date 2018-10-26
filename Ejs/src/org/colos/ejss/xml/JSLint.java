package org.colos.ejss.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

/**
 * A utility class to check JavaScript source code for potential problems.
 *
 */
public class JSLint {

    private final ScriptableObject scope;
    private final ContextFactory contextFactory;
    
    // Results
    private long mDuration = 0;
    private Scriptable mErrors;
    private String mErrorReport;
    private String mReport;
	
    // Uncomment to enable the rhino debugger.
    // static {
    // org.mozilla.javascript.tools.debugger.Main.mainEmbedded(null);
    // }

    /**
     * Create a new JSLint object.
     * @param reader a Reader over jslint.js
     * @throws IOException 
     */
    JSLint(Reader reader) throws IOException {
        contextFactory = new ContextFactory();
        Context cx = contextFactory.enterContext();
        scope = cx.initStandardObjects();                	
        cx.evaluateReader(scope, reader, "JSLINT", 1, null);

		    // we should no longer be updating this.
        this.scope.sealObject();
    }

    /**
     * Return the version of jslint in use.
     */
    public String getEdition() {
        Scriptable JSLINT = (Scriptable) scope.get("JSLINT", scope);
        return (String) JSLINT.get("edition", JSLINT);
    }    
    
    /**
     * Check for problems in a Reader which contains JavaScript source.
     *
     * @param systemId a identifier
     * @param reader a Reader over JavaScript source code.
     */
    public void lint(String systemId, Reader reader) throws IOException {
        lint(systemId, JSLint.readerToString(reader));
    }

    /**
     * Check for problems in JavaScript source.
     *
     * @param systemId a identifier
     * @param javaScript a String of JavaScript source code.
     */
    public void lint(String systemId, String javaScript) {
        long before = System.nanoTime();
        doLint(javaScript);
        long after = System.nanoTime();
        buildResults(systemId, before, after);
    }
    
    /**
     * Get jslint error report.
     * 
     * @return an HTML report.
     */
    public String getErrorReport() {
    	return mErrorReport;
    }

    /**
     * Get jslint report (shows all of the functions and the parameters and vars that they use).
     * 
     * @return an HTML report.
     */
    public String getReport() {
      return mReport;
    }
    
    /**
     * Get jslint error report.
     * 
     * @return list of Scriptable
     */
    public List<Scriptable> getErrors() {
        ArrayList<Scriptable> issues = new ArrayList<Scriptable>();
        int count = JSLint.intValue("length", mErrors);
        for (int i = 0; i < count; i++) {
            Scriptable err = (Scriptable) mErrors.get(i, mErrors);
            if (err != null) issues.add(err);
        }
        return issues;
    }
    
    /**
     * Get lint report duration.
     * 
     * @return long
     */
    public long getDuration() {
    	return mDuration;
    }
    
    /**
     * Do lint.
     * 
     * @param javaScript
     */
    private void doLint(final String javaScript) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                String src = javaScript == null ? "" : javaScript;
                Scriptable opts = cx.newObject(scope);

// not error for var in for (for(var i=...), jslint.js modified:
// 	In function "labeled_stmt('for'": avoid stopping
//   Removed: stop('move_var');
//   Added: warn('move_var'); advance();                    
//  In function "itself.error_report": not include warning messages
//   Added:	if (warning.raw === bundle['move_var']) { i++; continue; } 
                
//              bitwise    true, if bitwise operators should be allowed
                opts.put("bitwise", opts, true);
//              'continue' true, if the continuation statement should be tolerated
                opts.put("'continue'", opts, true);  			                  
//              devel      true, if logging should be allowed (console, alert, etc.)
//              eqeq       true, if == should be allowed
                opts.put("eqeq", opts, true);  			                  
//              maxerr     the maximum number of errors to allow
                opts.put("maxerr", opts, 1000);           
//              maxlen     the maximum length of a source line
//              nomen      true, if names may have dangling _
                opts.put("nomen", opts, true);                            
//              plusplus   true, if increment/decrement should be allowed
                opts.put("plusplus", opts, true);  			  
//              regexp     true, if the . should be allowed in regexp literals
                opts.put("regexp", opts, true);  			                  
//              unparam    true, if unused parameters should be tolerated
                opts.put("unparam", opts, true);
//              sloppy     true, if the 'use strict'; pragma is optional
                opts.put("sloppy", opts, true);            
//              stupid     true, if really stupid practices are tolerated
                opts.put("stupid", opts, true);                      
//              todo       true, if TODO comments are tolerated
                opts.put("todo", opts, true);  			                  
//              vars       true, if multiple var statements per function should be allowed
                opts.put("vars", opts, true);
//              white      true, if sloppy whitespace is tolerated
                opts.put("white", opts, true);                            

                Object[] args = new Object[] { src, opts };
                Function lintFunc = (Function) scope.get("JSLINT", scope);
                // JSLINT actually returns a boolean, but we ignore it as we always go
                // and look at the errors in more detail.
                lintFunc.call(cx, scope, scope, args);
                return null;
            }
        });
    }

    /**
     * Assemble the results.
     * 
     * @param systemId
     * @param startNanos
     * @param endNanos
     */
    private void buildResults(final String systemId, final long startNanos, final long endNanos) {
        contextFactory.call(new ContextAction() {
        	
            public Object run(Context cx) {
            	  mDuration = TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos);
            	                
            	  // get errors
                Scriptable JSLINT = (Scriptable) scope.get("JSLINT", scope);
                mErrors = (Scriptable) JSLINT.get("errors", JSLINT);
                             
                // get reports
                contextFactory.call(new ContextAction() {
                  public Object run(Context cx) {
                      Function fn = null;
                      Object value = null;
                      StringBuilder sb_error = new StringBuilder();
                      StringBuilder sb_report = new StringBuilder();
                      Scriptable JSLINT = (Scriptable) scope.get("JSLINT", scope);

                      // Look up JSLINT.data.
                      value = JSLINT.get("data", JSLINT);
                      if (value == UniqueTag.NOT_FOUND) {
                          return "";
                      }
                      fn = (Function) value;
                      // Call JSLINT.data().  This returns a JS data structure that we need below.
                      Object data = fn.call(cx, scope, scope, new Object[] {});

                      // Look up JSLINT.error_report.
                      value = JSLINT.get("error_report", JSLINT);
                      // Shouldn't happen ordinarily, but some of my tests don't have it.
                      if (value != UniqueTag.NOT_FOUND) {
                          fn = (Function) value;
                          // Call JSLint.report().
                          sb_error.append(fn.call(cx, scope, scope, new Object[] { data }));
                      }
                      
                      // Look up JSLINT.report.
                      value = JSLINT.get("report", JSLINT);
                      // Shouldn't happen ordinarily, but some of my tests don't have it.
                      if (value != UniqueTag.NOT_FOUND) {
                          fn = (Function) value;
                          // Call JSLint.report().
                          sb_report.append(fn.call(cx, scope, scope, new Object[] { data }));
                      }

                      mReport = sb_report.toString();
                      mErrorReport = sb_error.toString();
                      return "";
                  }                  
                });                
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
    
    /**
     * Return the integer value of a JavaScript variable.
     *
     * @param name the JavaScript variable
     * @param scope the JavaScript scope to read from
     * @return the value of name as an integer, or zero.
     */
    static int intValue(String name, Scriptable scope) {
        if (scope == null) {
            return 0;
        }
        Object o = scope.get(name, scope);
        return o == Scriptable.NOT_FOUND ? 0 : (int) Context.toNumber(o);
    }    
}
