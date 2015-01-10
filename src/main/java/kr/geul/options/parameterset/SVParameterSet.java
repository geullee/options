package kr.geul.options.parameterset;

public class SVParameterSet extends ParameterSet {

	public SVParameterSet() {
		
		type = "SV";
		parameterNames = new String[6];
		parameterNames[0] = "kappa";
		parameterNames[1] = "theta";
		parameterNames[2] = "v0";
		parameterNames[3] = "sigma";
		parameterNames[4] = "rho";
		parameterNames[5] = "lambda";
		parameterValues = getZeroArray(6);
		
	}

	public double[] getInitialValues() {
		double[] values = {parameterValues[0], parameterValues[1], 
				parameterValues[2], parameterValues[3], parameterValues[4]};
		return values;
	}
	
}