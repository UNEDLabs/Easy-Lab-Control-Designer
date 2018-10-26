package es.uned.dia.softwarelinks.evaluator;

public interface Evaluator {
/*	public void addInput(String name, double value);
	public void addInput(String name, double[] value);
	public void addOutput(String name, double value);
	public void addOutput(String name, double[] value);*/
	
	public void add(String input, double value);
	public void add(String input, double[] value);
	public void eval(String code) throws EvaluatorException;
//	public void double[] getInputs();
	public void setOutputsSize(int size);
	public void setOutputsName(String name);
	public double[] getOutputs();
	public void close();
}