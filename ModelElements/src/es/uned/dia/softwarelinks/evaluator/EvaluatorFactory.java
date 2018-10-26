package es.uned.dia.softwarelinks.evaluator;

import javax.script.ScriptEngine;

import bsh.Interpreter;
import es.uned.dia.softwarelinks.matlab.common.MatlabConnector;

public class EvaluatorFactory {
	public static Evaluator getEvaluator(Object o) {
		if (o instanceof Interpreter) {
			Interpreter ip = (Interpreter)o;
			return new JavaEvaluator(ip);
		} else if (o instanceof ScriptEngine) {
			ScriptEngine se = (ScriptEngine)o;
			return new JavaScriptEvaluator(se);
		} else if (o instanceof MatlabConnector) {
			MatlabConnector se = (MatlabConnector)o;
			return new MatlabEvaluator(se);
		} else {
			throw new IllegalArgumentException("Engine object not valid");
		}
	}
}