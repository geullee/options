package kr.geul.options.parameterset;

public class SVJParameterSet extends ParameterSet {

	public SVJParameterSet() {
		
		type = "SVJ";
		parameterNames = new String[8];
		parameterNames[0] = "kappaV";
		parameterNames[1] = "thetaV";
		parameterNames[2] = "v0";
		parameterNames[3] = "sigmaV";
		parameterNames[4] = "muJ";
		parameterNames[5] = "sigmaJ";
		parameterNames[6] = "rho";
		parameterNames[7] = "lambda";
		parameterValues = getZeroArray(8);
		
	}

	public double[] getInitialValues() {
		double[] values = {parameterValues[0], parameterValues[1], 
				parameterValues[2], parameterValues[3], parameterValues[4],
				parameterValues[5], parameterValues[6], parameterValues[7]};
		return values;
	}
	
}
