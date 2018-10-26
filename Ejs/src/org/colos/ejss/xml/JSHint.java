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
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A utility class to check JavaScript source code for potential problems.
 *
 */
public class JSHint {

    private final ScriptableObject scope;
    private final ContextFactory contextFactory;
    
    // Results
    private long mDuration = 0;
    private Scriptable mErrors;
    private String mErrorReport;
	
    // Uncomment to enable the rhino debugger.
    // static {
    // org.mozilla.javascript.tools.debugger.Main.mainEmbedded(null);
    // }

    /**
     * Create a new JSHint object.
     * @param reader a Reader over JSHint.js
     * @throws IOException 
     */
    JSHint(Reader reader) throws IOException {
        contextFactory = new ContextFactory();
        Context cx = contextFactory.enterContext();
        scope = cx.initStandardObjects();                	
        cx.evaluateReader(scope, reader, "JSHint", 1, null);

		    // we should no longer be updating this.
        this.scope.sealObject();
    }
    
    /**
     * Check for problems in a Reader which contains JavaScript source.
     *
     * @param systemId a identifier
     * @param reader a Reader over JavaScript source code.
     */
    public void hint(String systemId, Reader reader) throws IOException {
        hint(systemId, JSHint.readerToString(reader));
    }

    /**
     * Check for problems in JavaScript source.
     *
     * @param systemId a identifier
     * @param javaScript a String of JavaScript source code.
     */
    public void hint(String systemId, String javaScript) {
        long before = System.nanoTime();
        dohint(javaScript);
        long after = System.nanoTime();
        buildResults(systemId, before, after);
    }
    
    /**
     * Get JSHint error report.
     * 
     * @return an HTML report.
     */
    public String getErrorReport() {
    	return mErrorReport;
    }
    
    /**
     * Get JSHint error report.
     * 
     * @return list of Scriptable
     */
    public List<Scriptable> getErrors() {
        ArrayList<Scriptable> issues = new ArrayList<Scriptable>();
        int count = JSHint.intValue("length", mErrors);
        for (int i = 0; i < count; i++) {
            Scriptable err = (Scriptable) mErrors.get(i, mErrors);
            if (err != null) issues.add(err);
        }
        return issues;
    }
    
    /**
     * Get hint report duration.
     * 
     * @return long
     */
    public long getDuration() {
    	return mDuration;
    }
    
    /**
     * Do hint.
     * 
     * @param javaScript
     */
    private void dohint(final String javaScript) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                String src = javaScript == null ? "" : javaScript;
                Scriptable opts = cx.newObject(scope);

//                asi         : true, // if automatic semicolon insertion should be tolerated
//                bitwise     : true, // if bitwise operators should not be allowed
//                boss        : true, // if advanced usage of assignments should be allowed
//                camelcase   : true, // if identifiers should be required in camel case
//                curly       : true, // if curly braces around all blocks should be required
//                debug       : true, // if debugger statements should be allowed
//                devel       : true, // if logging globals should be predefined (console,
//                                    // alert, etc.)
//                eqeqeq      : true, // if === should be required
//                eqnull      : true, // if == null comparisons should be tolerated
//                esnext      : true, // if es.next specific syntax should be allowed
//                evil        : true, // if eval should be allowed
//                expr        : true, // if ExpressionStatement should be allowed as Programs
//                forin       : true, // if for in statements must filter
//                funcscope   : true, // if only function scope should be used for scope tests
//                immed       : true, // if immediate invocations must be wrapped in parens
//                lastsemic   : true, // if semicolons may be ommitted for the trailing
//                                    // statements inside of a one-line blocks.
//                latedef     : true, // if the use before definition should not be tolerated
//                laxbreak    : true, // if line breaks should not be checked
//                laxcomma    : true, // if line breaks should not be checked around commas
//                loopfunc    : true, // if functions should be allowed to be defined within
//                                    // loops
//                multistr    : true, // allow multiline strings
//                newcap      : true, // if constructor names must be capitalized
//                noempty     : true, // if empty blocks should be disallowed
//                nonew       : true, // if using `new` for side-effects should be disallowed
//                nomen       : true, // if names should be checked
//                onevar      : true, // if only one var statement per function should be
//                                    // allowed
//                onecase     : true, // if one case switch statements should be allowed
//                passfail    : true, // if the scan should stop on first error
//                plusplus    : true, // if increment/decrement should not be allowed
//                regexdash   : true, // if unescaped first/last dash (-) inside brackets
//                                    // should be tolerated
//                regexp      : true, // if the . should not be allowed in regexp literals
//                undef       : true, // if variables should be declared before used
                opts.put("undef", opts, true);
//                unused      : true, // if variables should be always used
//                scripturl   : true, // if script-targeted URLs should be tolerated
//                shadow      : true, // if variable shadowing should be tolerated
//                sub         : true, // if all forms of subscript notation are tolerated
//                supernew    : true, // if `new function () { ... };` and `new Object;`
//                                    // should be tolerated
//                trailing    : true, // if trailing whitespace rules apply
//                validthis   : true, // if 'this' inside a non-constructor function is valid.
//                                    // This is a function scoped option only.
//                withstmt    : true, // if with statements should be allowed
//                white       : true, // if strict whitespace rules apply
//                worker      : true, // if Web Worker script symbols should be allowed
                                
                Object[] args = new Object[] { src, opts };
                Function hintFunc = (Function) scope.get("JSHINT", scope);
                // JSHint actually returns a boolean, but we ignore it as we always go
                // and look at the errors in more detail.
                hintFunc.call(cx, scope, scope, args);
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
                Scriptable JSHint = (Scriptable) scope.get("JSHINT", scope);
                mErrors = (Scriptable) JSHint.get("errors", JSHint);
                             
                // create report
                NativeArray na = (NativeArray) mErrors;
            	StringBuilder sb_error = new StringBuilder();
                for(int i=0; i<na.size(); i++) {
                	Scriptable ele = (Scriptable) na.get(i);
                	String reason;
                	String evidence;
                	if (ele==null) {
                	  reason = "ele is null";
                	  evidence = "ele is null";
                	}
//                	String reason = (reasonObj==null) ? "unknown reason" : reasonObj.toString();
                	else {
                	  reason = ele.get("reason", ele).toString();
                	  evidence = ele.get("evidence", ele).toString();
                	}
                	// Double line = (Double) ele.get("line", ele);
                	// Double character = (Double) ele.get("character", ele);
                	// sb_error.append("Error in line " + line.shortValue() + ", col " + character.shortValue() + ", " + reason + "\n");
                	if (!reason.equalsIgnoreCase("Unnecessary semicolon.")) { // ignored warning
                	   	sb_error.append("Error (" + i + "): " + reason + "\n");
                	   	sb_error.append("	> " + evidence + "\n");
                	}
                }
                mErrorReport = sb_error.toString();
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
