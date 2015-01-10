package kr.geul.options.parameterset;

import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InvalidArgumentException;

public abstract class ParameterSet {

	protected String[] parameterNames;
	protected double[] parameterValues;
	protected String type;
	
	protected ParameterSet(){
	}
	
	public double get(String name) {
		
		for (int i = 0; i < parameterNames.length; i++) {
			
			if (parameterNames[i].equals(name)) {
				return parameterValues[i];
			}
			
		}
		
		return 0;
		
	}

	public abstract double[] getInitialValues();
	
	public String[] getNames() { 
		return parameterNames;
	}
	
	public String getType() {
		return type;
	}
	
	public double[] getValues() { 
		return parameterValues;
	}
	
	protected double[] getZeroArray(int length) {

		double[] array = new double[length];

		for (int i = 0; i < length; i++) {
			array[i] = 0.0;
		}

		return array;

	}
	
	public boolean isIn(String name) {

		for (int i = 0; i < parameterNames.length; i++) {

			if (parameterNames[i].equals(name)) 
				return true;

		}

		return false;

	}
	
	public boolean set(String name, double value) {

		for (int i = 0; i < parameterNames.length; i++) {
			
			if (parameterNames[i].equals(name)) {
				parameterValues[i] = value;
				return true;
			}

		}

		return false;

	}

	public boolean set(String[] name, double[] value) throws InvalidArgumentException, 
	InconsistentArgumentLengthException {
		
		if (name.length != value.length)
			throw new InconsistentArgumentLengthException(name.length, value.length);
		
		else {
		
			for (int i = 0; i < name.length; i++) {
				boolean bool = set(name[i], value[i]);
				
				if (bool == false)
					return false;
				
			}
			
			return true;
			
		}
		
	}
	
}
