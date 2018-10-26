package es.uned.dia.softwarelinks.evaluator;

public class EvaluatorException extends java.lang.Exception {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public EvaluatorException(String message) {
		super(message);
	}
	
}
