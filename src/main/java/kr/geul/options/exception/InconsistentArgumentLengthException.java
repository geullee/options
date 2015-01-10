package kr.geul.options.exception;

public class InconsistentArgumentLengthException extends Exception {

	private static final long serialVersionUID = -4293613855952455889L;
	double length1, length2;
	
	public InconsistentArgumentLengthException(int length1, int length2) {
		this.length1 = length1;
		this.length2 = length2;
	}	
	
}
