package kr.geul.options.exception;

public class InvalidArgumentException extends Exception {

	private static final long serialVersionUID = -2469135643708935198L;
	String option;
	
	public InvalidArgumentException(String option) {
		this.option = option; 
	}

}
