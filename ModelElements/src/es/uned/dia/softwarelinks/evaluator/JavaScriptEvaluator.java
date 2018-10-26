package es.uned.dia.softwarelinks.evaluator;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JavaScriptEvaluator implements Evaluator {
	ScriptEngine scriptEngine;
	String outputsName;
	int outputsSize;

	public JavaScriptEvaluator(ScriptEngine o){
		this.scriptEngine = o;
	}
	
	public void setOutputsName(String name) {
		this.outputsName = name;
	}

	public void setOutputsSize(int size) {
		this.outputsSize = size; 
	}

	@Override
	public void add(String variable, double value) {
		scriptEngine.put(variable, value);
	}

	@Override
	public void add(String variable, double[] value) {
		scriptEngine.put(variable, value);
	}
	
	@Override
	public void eval(String code) throws EvaluatorException {
		try {
			scriptEngine.eval(code);
		} catch (ScriptException e) {
			throw new EvaluatorException("Error when evaluating the code.");
		}
	}

	@Override
	public double[] getOutputs() {
		double[] outputs = new double[outputsSize];
		try {
			for(int j=0; j<outputsSize; j++) {
				String index = String.valueOf(j);
				String variable = outputsName+"["+index+"]";
				Object result = scriptEngine.eval(variable);
				outputs[j] = new Double(result.toString());
			}
		} catch (Exception e) {
			System.err.println("Error when reading variable: "+outputsName);
		}
		return outputs;
	}

	@Override
	public void close() {	
	}
}
