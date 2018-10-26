package es.uned.dia.softwarelinks.evaluator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTextArea;

import org.colos.ejs.library.Model;

import bsh.Interpreter;
import es.uned.dia.softwarelinks.matlab.common.MatlabConnector;


public class CodeControllerEvaluator {
	private Model model;
	private Evaluator evaluator;
	private int dim_input=20;
	private int dim_output=20;
	private String[] inputs = new String[dim_input];
	private String output;
	private String lang_code;
	private String text_help;
	private double[] state = new double[dim_input+dim_output];
	private String code; 
	
	private List<JTextArea> views = new ArrayList<JTextArea>();
	
	public CodeControllerEvaluator (int dim1, String dim2, String[] str1, String str2, Model model, String Lang, String Help) {
		this.dim_input = dim1;
		System.arraycopy(str1, 0, this.inputs, 0, dim1);
		this.output = str2;
		this.model = model;
		this.lang_code = Lang;
		this.text_help = Help;
		// The parameter is a string to include dimensions defined as a parameter
		try {
			this.dim_output=Integer.valueOf(dim2);
		} catch (NumberFormatException ex) {
			try {
				Field fieldDimO = model.getClass().getField(dim2);
				try {
					this.dim_output=fieldDimO.getInt(model);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} 
		}
		
		state = new double[dim_input+dim_output]; 
	}

	public Object getEvaluator () {
		Object inter;
		if (this.lang_code.equals("Java")) {
			inter = (Interpreter) new Interpreter ();
		} else if(this.lang_code.equals("JavaScript")) {
			ScriptEngineManager manager = new ScriptEngineManager();
	        inter = (ScriptEngine) manager.getEngineByName("js");
		} else if(this.lang_code.equals("MATLAB")) {
			inter = new MatlabConnector();
			((MatlabConnector)inter).connect();
		} else {
			inter = (Interpreter) new Interpreter ();
		}
		evaluator = EvaluatorFactory.getEvaluator(inter);
		evaluator.setOutputsName(output);
		evaluator.setOutputsSize(dim_output);
		return inter;
	}
	
	
	public double [] constructState(double... state) {
		int num=state.length;
		double[] output= new double[num];
		  
		for (int j=0;j<num;j++) {
			output[j] = state[j];
		}
		
		return output;
	}

	public int getInputsDim () {
		return dim_input;
	}
	
	public int getOutputsDim () {
		return dim_output;
	}

	public String[] getInputs () {
		return this.inputs;
	}
	
	public String getOutput () {
		return this.output;
	}
	
	public boolean exportState() {
		try {			
			getInputsValuesFromEJS();
			exportInputs();
			getOutputsValuesFromEJS();
			exportOutputs();			
		} catch(Exception e) {
			model._pause();
			model._alert("code", "ERROR", "Error when exporting variable: ");// + this.inputs[j]);
			return false;
		}
		return true;
	}
	
	private void getInputsValuesFromEJS() {
		try {
			for (int j=0; j<this.dim_input; j++) {
				Class modelClass = model.getClass();
				Field field = modelClass.getField(this.inputs[j]);
				double value = field.getDouble(this.model);
				state[j] = value;
			}
		} catch (IllegalAccessException | NoSuchFieldException ex) {
            System.err.println("The variable either doesn't exist or is " +
                "not public: " + ex.toString());
        }
	}

	private void exportInputs() {
		for (int j=0; j<dim_input; j++) {
			evaluator.add(inputs[j], state[j]);
		}
	}

	private void getOutputsValuesFromEJS() {
		try {
			Class modelClass = model.getClass();		
			Field field = modelClass.getField(this.output);
			if(field.getType().isArray()) {
				Object array = field.get(model);
				int length = Array.getLength(array);
				for(int i = 0; i<length; i++) {
					Object value = Array.get(array, i);
					state[this.dim_input+i] = Double.parseDouble(value.toString());
				}
			} else {
				state[this.dim_input] = field.getDouble(this.model);
			}
		} catch (IllegalAccessException ex) {
			System.err.println("No encuentro el valor " +
					"excepción: " + ex.toString());
		} catch (NoSuchFieldException ex) {
			System.err.println("The variable either doesn't exist or is " +
					"not public: " + ex.toString());
		}
	}
	
	private void exportOutputs() {
		double[] outputToExport = new double[dim_output];
		for (int j=0; j<this.dim_output; j++) {
			outputToExport[j] = state[j+this.dim_input];
		}
		evaluator.add(this.output, outputToExport);		
	}

	public double[] evaluateCode() {
		return evaluateCode(this.code);
	}

	public double[] evaluateCode(String code) {
		double[] outputs = new double[dim_output];
		try {
			exportState();
			evaluator.eval(code);
			outputs = evaluator.getOutputs();
		} catch(Exception e) {
			model._pause();
			model._alert("code", "ERROR", "Error when evaluating the code: " + code);			
		}
		return outputs;
	}

	public void showContextHelp () {
		String text = this.text_help.replace("$", "\n");
		this.model._alert("help", "HELP", text);
	}
	
	public void close() {
		evaluator.close();
	}

	public void setController(String code) {
		this.code = code;
		for (JTextArea view : views) {
			view.setText(code);
		}
	}
	
	public boolean addView(JTextArea view) {
		return views.add(view);
	}
	
	public boolean removeView(JTextArea view) {
		return views.remove(view);
	}
	

	// TO DO: remove these methods 	
	private void printArray(double[] array) {		
		String debug = "";
		for(int i=0; i<dim_output; i++) {
			debug += state[i+dim_input] + ", ";
		}
		System.out.println(debug);
	}
	
	@Deprecated 
	public double[] evaluateCode(String code, Object ip) {
		return evaluateCode(code);
	}
	
	@Deprecated 
	public boolean exportState(Object ip) {
		return exportState();
	}
}