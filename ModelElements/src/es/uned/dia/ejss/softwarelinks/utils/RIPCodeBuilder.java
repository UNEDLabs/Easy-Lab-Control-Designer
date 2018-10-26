package es.uned.dia.ejss.softwarelinks.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import es.uned.dia.softwarelinks.nodejs.RIPExperienceInfo;
import es.uned.dia.softwarelinks.nodejs.RIPInfo;
import es.uned.dia.softwarelinks.nodejs.RIPMethod;


public class RIPCodeBuilder {
  private static final String TYPE = "type";
  private static final String SERVER = "server";
  private static final String CLIENT = "client";
  private String name;
  private RIPConfigurationModel model;

  List<Map<String, String>> varsToGet = new ArrayList<>();
  List<Map<String, String>> varsToSet = new ArrayList<>();
	
  private static final String TEMPLATE_DEF = "var __conf = '{'''host'':''{0}'',''port'':''{1}'''}';\n" +
      "\tvar __transport = new {2}(__conf);\n" +
      "\t_model.{3} = new {4}(__transport);\n" +
      "\t_model._rip = _model.{3};\n" +
      "\tvar {3} = _model.{3};\n" +
      "\t_model.{3}.setDefaultExperience(''{5}'');\n" +
      "\t_model.{3}.__connect__ = _model.{3}.connect;\n" +
      "\t_model.{3}.connect = function(expid, callback) '{'\n" +
      "\tif(callback != undefined) '{'\n" +
      "\t\tthis.__connect__(expid, function(response) '{'\n" +
      "\tthis.updateEjsVariables(response);\n" +
      "\tcallback(response);\n" +
      "\t'}');\n" +
      "\t'}' else '{'\n" +
      "\t\tthis.__connect__(expid, this.updateEjsVariables);\n" +
      "\t'}'\n" +
      "\t'}';\n";

  private static final String TEMPLATE_METHOD = "// Auto-generated method \n" +
      "{0}.{1} = function({2}) '{'\n" +
      "\tthis.jsonrpcClient.invoke(''{1}'', [{3}], callback);\n" +
      "'}'\n";
  
  private static final String TEMPLATE_UPDATE = "// Auto-generated method \n" +
      "{0}.updateEjsVariables = function(result) '{'\n" +
      "\t{1}\n" +
      "\t_model.getView()._update();\n" +
      "'}'\n";

  private static final String TEMPLATE_INIT = "// Auto-generated method Init\n" +  
	    "{0}.init = function(callback) '{'\n" +
	    "\t" + "this.post(''eval'', [''{1}'']);" + "\n" +
	    "\t" + "this.sync(function(response) '{'" + "\n" +
	    "\t\t\tthis._isConnected = (response[0].result !== undefined);\n" +
	    "\t\t" + "if(callback != undefined) '{'" + "\n" +
	    "\t\t\t" + "callback();" + "\n"+
	    "\t\t" + "'}';" + "\n"+
	    "\t" + "'}'.bind(this));" + "\n" +
	    "'}'" + "\n" +
      "{0}._isConnected = false\n" + 
	    "{0}.isConnected = function() '{'\n" +
	    "return this._isConnected;" +
      "'}'" + "\n";

	private static final String TEMPLATE_STEP = "// Auto-generated method step()\n" +  
      "{0}.step = function(callback) '{'" + "\n" +
      "\t" + "this.post(''set'', {1});" + "\n" +
      "\t" + "this.post(''eval'', [''{2}'']);" + "\n" +
      "\t" + "this.post(''step'', [{3}]);" + "\n" +
      "\t" + "this.post(''get'', {4});" + "\n" +
      "\t" + "this.sync(function(response) '{'" + "\n" +
      "\t\t\t" + "var result = response[3].result;" + "\n" +
      "\t\t\t{5}\n" +
      "\t\t" + "if(callback != undefined) '{'" + "\n" +
      "\t\t\t" + "callback(result);" + "\n" +
      "\t\t" + "'}'" + "\n" +
      "\t" + "'}');" + "\n" +
      "}";
 
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setModel(RIPConfigurationModel model) {
		this.model = model;
	}

	public String getCode() {
        createClass();

        RIPExperienceInfo meta = model.getMetadata();
        RIPInfo info = meta.getInfo();

        StringBuilder code = new StringBuilder();
        try {
            String toGet = this.serializeVarsToGet(),
                    toEvalInit = model.getInitCode(),
                    toEvalStep = model.getStepCode(),
                    toSet = this.serializeVarsToSet(),
                    toAssign = this.serializeVarsToAssign(false),
                    toUpdate = this.serializeVarsToAssign(true);
            Object[] argsDef = {model.getServer(), model.getPort(), getTransport(), this.name, getApi(), info.getName()},
                    argsUpdate = {this.name, toUpdate},
                    argsInit = {this.name, toEvalInit},
                    argsStep = {this.name, toSet, toEvalStep, 1.0, toGet, toAssign};
            code.append(MessageFormat.format(TEMPLATE_DEF, argsDef));
            /*if(meta != null) {
                for(RIPMethod method : meta.getMethods()) {
                    Object[] args = getMethodArgs(method);
                    code.append(MessageFormat.format(TEMPLATE_METHOD, args));
                }
            }*/
            code.append(MessageFormat.format(TEMPLATE_UPDATE, argsUpdate));
            code.append(MessageFormat.format(TEMPLATE_INIT, argsInit));
            code.append(MessageFormat.format(TEMPLATE_STEP, argsStep));
        } catch (NullPointerException e){
            System.out.println("[WARNING] RIP element not configured yet.");
        }
        return String.format(code.toString());
  }

  private String getApi() {
    if(model.getApi().equals(model.RIP_SSE)) {
      return "RIPClient";
    }
    return "RIPArduino";
  }
  
  private String getTransport() {
    if(model.getApi().equals(model.RIP_SSE)) {
      return "SSETransport";
    }
    return "WebSocketTransport";
  }

  private String serializeVarsToAssign(boolean objectFormat) {
    StringBuilder assign = new StringBuilder();
    Object[] links = this.varsToGet.toArray();
    for(int i=0; i<links.length; i++) {
      Map<String, String> link = (Map<String, String>)links[i];
      if(objectFormat) {
        assign.append(String.format("%s = result['%s'];\n", link.get(CLIENT), link.get(SERVER)));
      } else {
        assign.append(String.format("%s = result[%s][0];\n", link.get(CLIENT), i));
      }
    }
    return assign.toString();
  }

	private Object[] getMethodArgs(RIPMethod method) {
	  Map<String, String> params = method.getParams();
	  boolean first = true;
	  String paramList = "";
	  for(String param : params.keySet()) {
	    if(first) {
	      first = false;
	      paramList += param;
	    } else {
	      paramList += ", " + param;
	    }
	  }
	  if(params.isEmpty()) {
	    return new Object[]{ this.name, method.getName(), "callback", paramList };
    } else {
      return new Object[]{ this.name, method.getName(), paramList + ", callback", paramList };
    }
	}

	private void createClass() {
		Vector<Vector> data = model.getDataVector();
		for(Vector<Object> row : data) {
			String matlabVariable = (String)row.get(0);
			String clientVariable = (String)row.get(1);
			boolean shouldAddToGet = (Boolean)row.get(2);
			boolean shouldAddToSet = (Boolean)row.get(3);
			if(isValid(matlabVariable, clientVariable)) {
				if(shouldAddToGet) {
					addLinkToGet(matlabVariable, clientVariable, "Double");
				}
				if(shouldAddToSet) {
					addLinkToSet(matlabVariable, clientVariable);
				}
			}
		}
	}

  private void addLinkToSet(String server, String client) {
    Map<String, String> link = new HashMap<>();
    link.put(CLIENT, client);
    link.put(SERVER, server);
    varsToSet.add(link);
  }

  private void addLinkToGet(String server, String client, String type) {
    Map<String, String> link = new HashMap<>();
    link.put(CLIENT, client);
    link.put(SERVER, server);
    link.put(TYPE, type);
    varsToGet.add(link);
  }
  
  private String serializeVarsToGet() {
    StringBuilder variables = new StringBuilder();
    boolean firstTime=true;
    for(Map<String, String> link : this.varsToGet) {
      String server = link.get(SERVER);
      variables.append(firstTime ? String.format("'%s'", server) : String.format(",'%s'", server));
      firstTime = false;
    }
    return String.format("[[%s]]", variables.toString());
  }

  private String serializeVarsToSet() {
    StringBuilder variables = new StringBuilder();
    StringBuilder values = new StringBuilder();
    boolean firstTime=true;
    for(Map<String, String> link : this.varsToSet) {
      String client = link.get(CLIENT), server = link.get(SERVER);
      variables.append(firstTime ? String.format("'%s'", server) : String.format(",'%s'", server));
      values.append(firstTime ? String.format("%s", client) : String.format(",%s", client));
      firstTime = false;
    }
    
    return String.format("[[%s], [%s]]", variables.toString(), values);
  }
  
  
  private boolean isValid(String matlab, String ejs) {
		return matlab != "" && matlab != null && ejs != "" && ejs != null;
	}
}