package es.uned.dia.softwarelinks.evaluator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import es.uned.dia.softwarelinks.matlab.common.MatlabConnector;

public class MatlabEvaluator implements Evaluator {
	MatlabConnector engine;
	Map<String, Double> inputs = new HashMap<String, Double>();
	double[] outputs = new double[]{0.0};
	String outputsName;

	public MatlabEvaluator(MatlabConnector o) {
		this.engine = o;		
	}

	public void setOutputsName(String name) {
		this.outputsName = name;
	}

	public void setOutputsSize(int size) {
		if(this.outputs.length != size) {
			this.outputs = new double[size];
			engine.set(outputsName, (Object)outputs);
		}
	}

	@Override
	public void add(String variable, double value) {
		inputs.put(variable, new Double(value));
	}

	@Override
	public void add(String variable, double[] value) {
		System.arraycopy(value, 0, outputs, 0, outputs.length);
	}

	@Override
	public void eval(String code) {		
//		try {
			setInputs();
			engine.eval(code);
			getOutputs();
//		} catch (ScriptException e) {
//        	System.err.println("Error when evaluating the code: " + code);
//		}
	}

	private void setInputs() {		
		Iterator<String> iter = (Iterator<String>)inputs.keySet().iterator();
		while(iter.hasNext()) {
			String inputName = (String)iter.next();
			double inputValue = inputs.get(inputName);
			engine.set(inputName, inputValue);
		}
		engine.set(outputsName, outputs);
	}

	@Override
	public double[] getOutputs() {		
		this.outputs = (double[])engine.get(outputsName);
		return outputs;
	}

	@Override
	public void close() {
		engine.disconnect();
	}
}
