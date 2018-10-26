package org.colos.ejss.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class OtherXMLTransformerJava {
  static private final String sSTYLE_SHEETS = "css/ejss.css,lib/jquery-ui-1.9.1.custom.min.css" ;
  static private final String sSCRIPTS      = "lib/jquery-1.8.3.js,lib/jquery-ui-1.9.1.custom.min.js,lib/ejsS.v1.min.js" ;
  
  // ------------------------------------
  // Non-static part
  // ------------------------------------
  
  @SuppressWarnings("unused")
  private SimulationXML mSimulation;
  @SuppressWarnings("unused")
  private ErrorOutput mErrorOutput;
  private File mSimulationFile;

	/**
	 * XML to HTML transformer
   * @param simulation
   * @param output
   */
	public OtherXMLTransformerJava(SimulationXML simulation, ErrorOutput output) {
	  mSimulation = simulation;
	  mErrorOutput = output;
	}

	public OtherXMLTransformerJava(File simulation) {
		mSimulationFile = simulation;
	}
	
  // ------------------------------------
  // HTML generation
  //-------------------------------------

  String getJavascript(String viewDesired, String locale, String librariesPath, String codebase) throws TransformerException {
	XMLTransform xml = new XMLTransform(mSimulationFile, myXMLTrans);
	xml.setParameter("name", "EjsSimulation");
	xml.setParameter("librariesPath", librariesPath);
	xml.setParameter("jsfiles", sSCRIPTS);
	xml.setParameter("cssfiles", sSTYLE_SHEETS);
	xml.setParameter("codebase", codebase);	
	xml.setParameter("viewSelected", viewDesired);			
	xml.setParameter("locale", locale);	
	xml.setParameter("onlyjs", true);
	return xml.transform();
  }

  String toSimulationHTML(String viewDesired,  String locale, String librariesPath, String codebase) throws TransformerException {
	XMLTransform xml = new XMLTransform(mSimulationFile, myXMLTrans);
	xml.setParameter("name", "EjsSimulation");
	xml.setParameter("librariesPath", librariesPath);
	xml.setParameter("jsfiles", sSCRIPTS);
	xml.setParameter("cssfiles", sSTYLE_SHEETS);
	xml.setParameter("codebase", codebase);
	xml.setParameter("viewSelected", viewDesired);			
	xml.setParameter("locale", locale);
	xml.setParameter("onlyjs", false);
	return xml.transform();
  }
   
  static public String splitCode (String code, String information, String name) {
	StringBuffer buffer = new StringBuffer();
	SimulationXML.splitCode(buffer, code, name + ":" + information, "");
	return buffer.toString();
  }
		    
  static public String processArrayVariable(String name, String value, String dimension) {
    String varName = name;
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
    int dim = tkn.countTokens();
    java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(varName,"[] ");
    int dimIndex = tknIndexes.countTokens();
    String lineOfIndexes = null;
    if (dimIndex>1) {
      varName = tknIndexes.nextToken();
      lineOfIndexes = tknIndexes.nextToken();
      while (tknIndexes.hasMoreTokens()) lineOfIndexes += ","+tknIndexes.nextToken();
      if ((dimIndex-1)!=dim) {
        // if (mErrorOutput!=null) mErrorOutput.println ("Syntax error: Dimension brackets in variable name "+name+ " ("+evaluateNode(page, "name")+ ") do not match the dimension "+dimension);
      }
    }
    // String comment = " // EJsS Model.Variables."+evaluateNode(page,"name")+"."+name;
    return SimulationXML.initCodeForAnArray ("", "", lineOfIndexes, varName, dimension, value);
  }

  // XMLTransform 
  public class XMLTransform {
	private Source xsltSource;
	private Source xmlSource;
	private Transformer trans;
	
	public XMLTransform(File source, String xslt) throws TransformerConfigurationException {		
		// xslt
		xsltSource = new StreamSource(new StringReader(xslt));
        TransformerFactory transFact = TransformerFactory.newInstance();
        trans = transFact.newTransformer(xsltSource);		
        // source
        xmlSource = new StreamSource(fileInputStream(source));
	}
	
	public XMLTransform(String source, String xslt) throws TransformerConfigurationException {
		// xslt
		xsltSource = new StreamSource(new StringReader(xslt));
        TransformerFactory transFact = TransformerFactory.newInstance();
        trans = transFact.newTransformer(xsltSource);		
        // source
        xmlSource = new StreamSource(new StringReader(source));
	}
	
	public void transform(File out) throws TransformerException {    	
        StreamResult result = new StreamResult(out);
        trans.transform(xmlSource, result);
    }		

	public String transform() throws TransformerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(out);
        trans.transform(xmlSource, result);
        
        return out.toString();
    }		
	
	private FileInputStream fileInputStream(File filename) {
		try {
			return new FileInputStream(filename);
		}
		catch(IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void setParameter(String name, Object value) {
		trans.setParameter(name, value);		
	}
  }  

  private String myXMLTrans = "<?xml version=\"1.0\"?>\n" + 
  		"\n" + 
  		"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
  		"	xmlns:MyTransform=\"http://www.um.es/\">\n" + 
  		"<xsl:output method=\"html\"/>\n" + 
  		"<xsl:param name=\"librariesPath\"/>\n" + 
  		"<xsl:param name=\"jsfiles\"/>\n" + 
  		"<xsl:param name=\"cssfiles\"/>\n" + 
  		"<xsl:param name=\"codebase\"/>\n" + 
  		"<xsl:param name=\"name\"/>\n" + 
  		"<xsl:param name=\"viewSelected\"/>\n" + 
  		"<xsl:param name=\"locale\"/>\n" + 
  		"<xsl:param name=\"onlyjs\"/>\n" + 
  		"\n" + 
  		"<xsl:template name=\"javascript\">\n" + 
  		"\n" + 
  		"	<!-- ADD MODEL (Begin) -->\n" + 
  		"\n" + 
  		"	function <xsl:value-of select=\"$name\"/>() {\n" + 
  		"	    var _model = EJSS_CORE.createAnimationLMS();\n" + 
  		"	    var _isPlaying = false;\n" + 
  		"	    var _isPaused = true;\n" + 
  		"	    \n" + 
  		"	    function _play() { _model.play(); }\n" + 
  		"	    function _pause() { _model.pause(); }\n" + 
  		"	    function _step() { _model.step(); }\n" + 
  		"	    function _reset() { _model.reset(); }\n" + 
  		"	    function _update() { _model.update(); }\n" + 
  		"	    function _initialize() { _model.initialize(); }\n" + 
  		"	\n" + 
  		"	    function _setFPS(_fps) { _model.setFPS(_fps); }\n" + 
  		"	    function _setDelay(_delay) { _model.setDelay(_delay); }\n" + 
  		"	    function _setStepsPerDisplay(_spd) { _model.setStepsPerDisplay(_spd); }\n" + 
  		"	    function _setUpdateView(_updateView) { _model.setUpdateView(_updateView); }\n" + 
  		"	    function _setAutoplay(_auto) { _model.setAutoplay(_auto); }\n" + 
  		"	    function _println(_message) { console.log(_message); }\n" + 
  		"	\n" + 
  		"	    // Variables declaration\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/variables/page/variable\">\n" + 
  		"	   		var <xsl:value-of select=\"normalize-space(substring-before(concat(name, '[]'), '[]'))\"/>;\n" + 
  		"	    </xsl:for-each>	\n" + 
  		"	\n" + 
  		"	    // Variables reset\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/variables/page\">\n" + 
  		"			_model.addToReset(function() {\n" + 
  		"			<xsl:for-each select=\"variable\">\n" + 
  		"				<xsl:choose>\n" + 
  		"					<xsl:when test=\"string-length(normalize-space(dimension)) > 0\">\n" + 
  		"						<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.processArrayVariable( name, value, dimension)\"/>;			  				\n" + 
  		"				    </xsl:when>			  \n" + 
  		"				    <xsl:otherwise>\n" + 
  		"				    	<xsl:if test=\"string-length(normalize-space(value)) > 0\">\n" + 
  		"				    		<xsl:value-of select=\"name\"/>=<xsl:value-of select=\"value\"/>;\n" + 
  		"				    	</xsl:if>\n" + 
  		"				    </xsl:otherwise>		  					  		\n" + 
  		"		  		  </xsl:choose>			\n" + 
  		"			</xsl:for-each>\n" + 
  		"			});\n" + 
  		"	    </xsl:for-each>	\n" + 
  		"	        \n" + 
  		"	    // Initialization\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/initialization/page\">\n" + 
  		"	    	_model.addToInitialization(function() {\n" + 
  		"	    		<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode(code, 'Initialization', name)\"/>;\n" + 
  		"	    	});\n" + 
  		"	    </xsl:for-each>\n" + 
  		"	        \n" + 
  		"	    // Evolution\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/evolution/page\">\n" + 
  		"			<xsl:choose>\n" + 
  		"				<xsl:when test=\"type = 'code'\">\n" + 
  		"					_model.addToEvolution(function() {\n" + 
  		"						<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode(code, 'Evolution', name)\"/>;	\n" + 
  		"					});					  				\n" + 
  		"			    </xsl:when>			  \n" + 
  		"			    <xsl:otherwise>\n" + 
  		"			    	// TODO: ODE pages\n" + 
  		"			    </xsl:otherwise>		  					  		\n" + 
  		"		    </xsl:choose>\n" + 
  		"	    </xsl:for-each>\n" + 
  		"	\n" + 
  		"	    // Fixed relations\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/fixed_relations/page\">\n" + 
  		"	    	_model.addToFixedRelations(function() {\n" + 
  		"	    		<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode(code, 'FixedRelations', name)\"/>;\n" + 
  		"	    	});\n" + 
  		"	    </xsl:for-each>   \n" + 
  		"	    \n" + 
  		"	    _model.addToFixedRelations(function() { \n" + 
  		"	    		_isPaused = _model.isPaused(); \n" + 
  		"	    		_isPlaying = _model.isPlaying(); \n" + 
  		"	    });\n" + 
  		"	\n" + 
  		"	    // Custom code\n" + 
  		"	    <xsl:for-each select=\"EJsS/model/custom_code/page\">\n" + 
  		"	   		<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode(code, 'CustomCode', name)\"/>;\n" + 
  		"	    </xsl:for-each>   \n" + 
  		"	        \n" + 
  		"	    // View from the model\n" + 
  		"	    _view = new <xsl:value-of select=\"$name\"/>_View();    \n" + 
  		"	    _view._setResourcePath(\"<xsl:value-of select=\"$codebase\"/>\");\n" + 
  		"	    _view._setLibraryPath(\"<xsl:value-of select=\"$librariesPath\"/>/lib\");\n" + 
  		"	    var _view_super_reset = _view._reset;\n" + 
  		"	    \n" + 
  		"	    _view._reset = function() {\n" + 
  		"	    	_view_super_reset();\n" + 
  		"	    \n" + 
  		"		    <xsl:for-each select=\"EJsS/view/page/name[text()=$viewSelected]/../element/property\">	    	\n" + 
  		"				<xsl:variable name=\"localeValue\" select=\"localization/locale[text()=$locale]/../value\"/>\n" + 
  		"				    \n" + 
  		"				<xsl:choose>\n" + 
  		"					<xsl:when test=\"type = 'constant'\">\n" + 
  		"						// Do nothing\n" + 
  		"				    </xsl:when>			  \n" + 
  		"					<xsl:when test=\"type = 'action'\">\n" + 
  		"						_view.<xsl:value-of select=\"../name\"/>.setAction(\"<xsl:value-of select=\"name\"/>\", function() { <xsl:value-of select=\"$localeValue\"/>; } );\n" + 
  		"				    </xsl:when>			  \n" + 
  		"					<xsl:when test=\"type = 'expression'\">\n" + 
  		"						_view.<xsl:value-of select=\"../name\"/>.linkProperty(\"<xsl:value-of select=\"name\"/>\",\n" + 
  		"						<xsl:choose>\n" + 
  		"							<xsl:when test=\"contains(localeValue,'return')\">\n" + 
  		"					            function() {\n" + 
  		"					            	<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode($localeValue, ../name, name)\"/>;\n" + 
  		"					            }\n" + 
  		"						    </xsl:when>		\n" + 
  		"						    <xsl:otherwise>\n" + 
  		"						    	function() { return <xsl:value-of select=\"$localeValue\"/>; } \n" + 
  		"						    </xsl:otherwise>		  					  							    	  \n" + 
  		"					    </xsl:choose>\n" + 
  		"					    );\n" + 
  		"				    </xsl:when>			  \n" + 
  		"					<xsl:when test=\"type = 'variable'\">\n" + 
  		"			            _view.<xsl:value-of select=\"../name\"/>.linkProperty(\"<xsl:value-of select=\"name\"/>\", \n" + 
  		"			            function() { return <xsl:value-of select=\"$localeValue\"/>; },\n" + 
  		"			            function(_v) { <xsl:value-of select=\"$localeValue\"/> = _v; } );\n" + 
  		"				    </xsl:when>			  \n" + 
  		"			    </xsl:choose>\n" + 
  		"		   		\n" + 
  		"		   		<xsl:value-of select=\"MyTransform:org.colos.ejss.xml.OtherXMLTransformerJava.splitCode(code, 'CustomCode', name)\"/>;\n" + 
  		"		   		\n" + 
  		"		    </xsl:for-each>   \n" + 
  		"	    	\n" + 
  		"		};\n" + 
  		"		    \n" + 
  		"		_model.setView(_view);\n" + 
  		"		\n" + 
  		"		// Model parameters\n" + 
  		"		<xsl:choose>\n" + 
  		"			<xsl:when test=\"EJsS/model/configuration/autostart = 'true'\">\n" + 
  		"				_model.setAutoplay(true);\n" + 
  		"		    </xsl:when>		\n" + 
  		"		    <xsl:otherwise>\n" + 
  		"		    	_model.setAutoplay(false);\n" + 
  		"		    </xsl:otherwise>		  					  							    	  \n" + 
  		"	    </xsl:choose>\n" + 
  		"	\n" + 
  		"		<xsl:choose>\n" + 
  		"			<xsl:when test=\"EJsS/model/configuration/frames_per_second\">\n" + 
  		"				_model.setFPS(<xsl:value-of select=\"EJsS/model/configuration/frames_per_second\"/>);\n" + 
  		"		    </xsl:when>		\n" + 
  		"		    <xsl:otherwise>\n" + 
  		"		    	_model.setFPS(10);\n" + 
  		"		    </xsl:otherwise>		  					  							    	  \n" + 
  		"	    </xsl:choose>\n" + 
  		"	\n" + 
  		"		<xsl:choose>\n" + 
  		"			<xsl:when test=\"EJsS/model/configuration/steps_per_display\">\n" + 
  		"				_model.setStepsPerDisplay(<xsl:value-of select=\"EJsS/model/configuration/steps_per_display\"/>);\n" + 
  		"		    </xsl:when>		\n" + 
  		"		    <xsl:otherwise>\n" + 
  		"		    	_model.setStepsPerDisplay(1);\n" + 
  		"		    </xsl:otherwise>		  					  							    	  \n" + 
  		"	    </xsl:choose>\n" + 
  		"	   \n" + 
  		"	    // And that's it!\n" + 
  		"	    _model.reset();\n" + 
  		"	    return _model;\n" + 
  		"	}\n" + 
  		"	 \n" + 
  		"<!-- ADD MODEL (End) -->\n" + 
  		"\n" + 
  		"<!-- ADD VIEW (Begin) -->\n" + 
  		"\n" + 
  		"	var <xsl:value-of select=\"$name\"/>_View = function() {\n" + 
  		"		var _view = EJSS_CORE.createView(\"_topFrame\");\n" + 
  		"	    _view._reset = function() {\n" + 
  		"	    	_view._clearAll();\n" + 
  		"	    \n" + 
  		"	      	<xsl:for-each select=\"EJsS/view/page/name[text()=$viewSelected]/../element\">            	\n" + 
  		"	    	    _view._addElement(<xsl:value-of select=\"type\"/>,\"<xsl:value-of select=\"name\"/>\",_view.<xsl:value-of select=\"parent\"/>)\n" + 
  		"			  	<xsl:if test=\"not(property/type='constant')\">;</xsl:if>\n" + 
  		"			  	<xsl:for-each select=\"property[type='constant']\">\n" + 
  		"			  	  <xsl:variable name=\"localeValue\" select=\"localization/locale[text()=$locale]/../value\"/>\n" + 
  		"				  <xsl:choose>\n" + 
  		"					<xsl:when test=\"position() = 1 and position() = last()\">\n" + 
  		"		  				.setProperties({<xsl:value-of select=\"name\"/>:<xsl:value-of select=\"$localeValue\"/>});\n" + 
  		"				    </xsl:when>			  \n" + 
  		"				    <xsl:when test=\"position() = 1\">\n" + 
  		"		  				.setProperties({<xsl:value-of select=\"name\"/>:<xsl:value-of select=\"$localeValue\"/>\n" + 
  		"				    </xsl:when>\n" + 
  		"				    <xsl:when test=\"position() = last()\">\n" + 
  		"		  				, <xsl:value-of select=\"name\"/>:<xsl:value-of select=\"$localeValue\"/>});\n" + 
  		"				    </xsl:when>\n" + 
  		"				    <xsl:otherwise>\n" + 
  		"				    	, <xsl:value-of select=\"name\"/>:<xsl:value-of select=\"$localeValue\"/>\n" + 
  		"				    </xsl:otherwise>		  					  		\n" + 
  		"		  		  </xsl:choose>\n" + 
  		"			  	</xsl:for-each>\n" + 
  		"		    </xsl:for-each>	\n" + 
  		"		}\n" + 
  		"		return _view;\n" + 
  		"	}                \n" + 
  		"	\n" + 
  		"	<!-- ADD VIEW (End) -->\n" + 
  		"\n" + 
  		"</xsl:template>\n" + 
  		"\n" + 
  		"<xsl:template name=\"css\" >\n" + 
  		"  <xsl:param name=\"cssfiles\"/>\n" + 
  		"    <xsl:if test=\"normalize-space($cssfiles) != ''\">\n" + 
  		"        <xsl:choose>\n" + 
  		"            <xsl:when test=\"contains($cssfiles, ',')\">\n" + 
  		"            	<link rel=\"stylesheet\" href=\"{$librariesPath}{substring-before($cssfiles,',')}\"/>\n" + 
  		"                   <xsl:call-template name=\"css\">\n" + 
  		"                    <xsl:with-param name=\"cssfiles\" select=\"substring-after($cssfiles,',')\"/>\n" + 
  		"                </xsl:call-template>        \n" + 
  		"            </xsl:when>\n" + 
  		"            <xsl:otherwise>\n" + 
  		"            	<link rel=\"stylesheet\" href=\"{$librariesPath}{$cssfiles}\"/>\n" + 
  		"            </xsl:otherwise>\n" + 
  		"        </xsl:choose>\n" + 
  		"    </xsl:if>\n" + 
  		"</xsl:template>\n" + 
  		"\n" + 
  		"<xsl:template name=\"jscript\" >\n" + 
  		"  <xsl:param name=\"jsfiles\"/>\n" + 
  		"    <xsl:if test=\"normalize-space($jsfiles) != ''\">\n" + 
  		"        <xsl:choose>\n" + 
  		"            <xsl:when test=\"contains($jsfiles, ',')\">\n" + 
  		"            	<script src=\"{$librariesPath}{substring-before($jsfiles,',')}\"/>\n" + 
  		"                   <xsl:call-template name=\"jscript\">\n" + 
  		"                    <xsl:with-param name=\"jsfiles\" select=\"substring-after($jsfiles,',')\"/>\n" + 
  		"                </xsl:call-template>        \n" + 
  		"            </xsl:when>\n" + 
  		"            <xsl:otherwise>\n" + 
  		"            	<script src=\"{$librariesPath}{$jsfiles}\"/>\n" + 
  		"            </xsl:otherwise>\n" + 
  		"        </xsl:choose>\n" + 
  		"    </xsl:if>\n" + 
  		"</xsl:template>\n" + 
  		"\n" + 
  		"<xsl:template match=\"/\">\n" + 
  		"\n" + 
  		"  <xsl:choose>\n" + 
  		"	<xsl:when test=\"$onlyjs = 0\">\n" + 
  		"	\n" + 
  		"		<html>\n" + 
  		"		<head>\n" + 
  		"		<meta charset=\"UTF-8\"/>\n" + 
  		"		<title><xsl:value-of select=\"EJsS/information/title\"/></title>\n" + 
  		"		<xsl:text>&#xa;</xsl:text>\n" + 
  		"	\n" + 
  		"		<xsl:call-template name=\"css\">\n" + 
  		"			<xsl:with-param name=\"cssfiles\" select=\"$cssfiles\"/>\n" + 
  		"		</xsl:call-template>\n" + 
  		"\n" + 
  		"		<xsl:call-template name=\"jscript\">\n" + 
  		"			<xsl:with-param name=\"jsfiles\" select=\"$jsfiles\"/>\n" + 
  		"		</xsl:call-template>\n" + 
  		"		\n" + 
  		"		</head>\n" + 
  		"		\n" + 
  		"		<body>\n" + 
  		"			<div id=\"_topFrame\"> </div>\n" + 
  		"			<xsl:text>&#xa;</xsl:text>\n" + 
  		"				\n" + 
  		"			<script type=\"text/javascript\">	\n" + 
  		"				<xsl:call-template name=\"javascript\"/>\n" + 
  		"			</script>\n" + 
  		"			<xsl:text>&#xa;</xsl:text>\n" + 
  		"			\n" + 
  		"			<script type=\"text/javascript\">\n" + 
  		"			    window.addEventListener('load', function () { new <xsl:value-of select=\"$name\"/>(); }, false);\n" + 
  		"			</script>\n" + 
  		"			 \n" + 
  		"		</body>\n" + 
  		"		</html>\n" + 
  		"\n" + 
  		"    </xsl:when>			  \n" + 
  		"    <xsl:otherwise>\n" + 
  		"    \n" + 
  		"    	<xsl:call-template name=\"javascript\"/>\n" + 
  		"    \n" + 
  		"    </xsl:otherwise>		  					  		\n" + 
  		"  </xsl:choose>\n" + 
  		"\n" + 
  		"</xsl:template>\n" + 
  		"\n" + 
  		"</xsl:stylesheet> ";
}


