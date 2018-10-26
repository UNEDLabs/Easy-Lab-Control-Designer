package es.uned.dia.softwarelinks.evaluator;

import java.util.ArrayList;

import bsh.EvalError;
import bsh.Interpreter;

public class JavaEvaluator implements Evaluator {
	Interpreter interpreter;
	String outputsName;
	int outputsSize;	
	
	public JavaEvaluator(Interpreter o) {
		this.interpreter = o;
	}
	
	public void setOutputsName(String name) {
		this.outputsName = name;
	}

	public void setOutputsSize(int size) {
		this.outputsSize = size; 
	}
	
	@Override
	public void add(String name, double value) {
		addInput(name, value);
	}

	private void addInput(String name, double value) {
		try {
			interpreter.set(name, value);
		} catch (EvalError e) {
			System.err.println("Error when exporting variable");
		}
	}
	
	@Override
	public void add(String name, double[] value) {
		addOutput(name, value);
	}
	
	private void addOutput(String name, double[] value) {
		try {
			interpreter.set(name, value);
		} catch (EvalError e) {
			System.err.println("Error when exporting variable");
		}
	}	

	@Override
	public void eval(String code) throws EvaluatorException {
		try {
			interpreter.eval(code);
		} catch (EvalError e) {
			throw new EvaluatorException("Error when evaluating code");
		}
	}

	@Override
	public double[] getOutputs() {
		double[] outputs = new double[outputsSize];
		try {
			for(int j=0; j<outputsSize; j++) {
				String index = String.valueOf(j);
				String variable = outputsName+"["+index+"]";
				Object result = interpreter.eval(variable);
				outputs[j] = new Double(result.toString());
			}
		} catch (Exception e) {
			System.err.println("Error when reading variable "+outputsName);
		}
		return outputs;
	}

	@Override
	public void close() {
	}
}