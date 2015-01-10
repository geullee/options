package kr.geul.options.parameterset;

public class BSParameterSet extends ParameterSet {

	public BSParameterSet() {
		type = "BS";
		parameterNames = new String[1];
		parameterNames[0] = "sigma";
		parameterValues = getZeroArray(1);
	}

	public double[] getInitialValues() {
		return parameterValues;
	}
	
}
