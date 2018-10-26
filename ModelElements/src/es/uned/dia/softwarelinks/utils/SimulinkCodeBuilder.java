package es.uned.dia.softwarelinks.utils;

import java.util.Vector;

import es.uned.dia.softwarelinks.utils.RIPConfigurationModel;

public class SimulinkCodeBuilder {
	private static String prefix = "es.uned.dia.softwarelinks.transport.";
	private String name;
	private ClassBuilder builder = new ClassBuilder();
	private RIPConfigurationModel model;

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
		switch(model.getMode()) {
		default:
		case "local":
			return "new es.uned.dia.softwarelinks.matlab.common.SimulinkConnector()" + builder.toString() + ";";
		case "remote":
			return "try {" +
				prefix + "Transport transport = new "+ prefix + getTransport() + "(\"" + model.getURL() + "\");" +
				"es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient __matlab__ = new es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient(transport);" +
				name + " = new es.uned.dia.softwarelinks.matlab.client.RemoteSimulinkConnector(__matlab__)" + builder.toString() + ";" +
			"} catch (Exception e) { e.printStackTrace(); }";
		}
	}

	private String getTransport() {
		switch(model.getProtocol()) {
		default:
		case "tcp":
			return "TcpTransport";
		case "http":
			return "HttpTransport";
		}
	}

	private void createClass() {
		MethodGetBuilder mgb = new MethodGetBuilder();
		MethodSetBuilder msb = new MethodSetBuilder();
		Vector<Vector<Object>> data = model.getDataVector();
		for(Vector<Object> row : data) {
			String matlabVariable = (String)row.get(0);
			String ejsVariable = (String)row.get(1);
			boolean shouldAddToGet = (Boolean)row.get(2);
			boolean shouldAddToSet = (Boolean)row.get(3);
			if(isValid(matlabVariable, ejsVariable)) {
				if(shouldAddToGet) {
					mgb.addLink(matlabVariable, ejsVariable, "Double");
				}
				if(shouldAddToSet) {
					msb.addLink(matlabVariable, ejsVariable);
				}
			}
		}
		builder.addMethod("get", mgb);
		builder.addMethod("set", msb);
	}

	private boolean isValid(String matlab, String ejs) {
		return matlab != "" && matlab != null && ejs != "" && ejs != null;
	}
}