package kr.geul.options.modelerrorcalculator;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentComponentException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.Option;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.PutOption;
import kr.geul.options.parameterset.ParameterSet;
import kr.geul.options.pricer.SVPricer;

public class SVModelErrorCalculator extends ModelErrorCalculator {
	
	public SVModelErrorCalculator() {
		
		pricer = new SVPricer();
		modelType = "SV";
		paramNames = new String[5];
		paramLowerBounds = new double[5];
		paramUpperBounds = new double[5];
		paramScales = new double[5];
		paramSigmas = new double[5];
		paramNames[0] = "kappa";
		paramLowerBounds[0] = 0.0025; 
		paramUpperBounds[0] = 10;
		paramScales[0] = 100;
		paramSigmas[0] = 0.5;
		paramNames[1] = "theta";
		paramLowerBounds[1] = 0.0025; 
		paramUpperBounds[1] = 0.8;
		paramScales[1] = 100;
		paramSigmas[1] = 0.5;
		paramNames[2] = "v0";
		paramLowerBounds[2] = 0.0001; 
		paramUpperBounds[2] = 0.8;
		paramScales[2] = 100;
		paramSigmas[2] = 0.5;
		paramNames[3] = "sigma";
		paramLowerBounds[3] = 0.0 + Math.ulp(0.0);
		paramUpperBounds[3] = 5.0;
		paramScales[3] = 100;
		paramSigmas[3] = 0.5;
		paramNames[4] = "rho";
		paramLowerBounds[4] = -1.0; 
		paramUpperBounds[4] = 1.0;
		paramScales[4] = 100;
		paramSigmas[4] = 0.5;
		
	}

	protected Option getModelOption(Option option, double[] param) 
			throws InvalidArgumentException, InconsistentArgumentLengthException, 
			AtTheMoneyException, TooManyEvaluationsException {
		
		Option modelOption;
		
		if (option.isCall() == true)
			modelOption = new CallOption("SV");
		else
			modelOption = new PutOption("SV");		
		
		String[] parameterNames = {"S", "K", "T", "R", "D", "C"};
		double[] modelParameterValues = {param[0], param[1], param[2], param[3], param[4], 0.0};
		modelOption.set(parameterNames, option.getVariableArray());
		modelOption.set(paramSet.getNames(), modelParameterValues);
		
		modelOption.evaluate();
		
		return modelOption;
		
	}

	public void setParameterSet(ParameterSet paramSet) 
			throws InconsistentComponentException {

		if (paramSet.getType().equals("SV"))
			this.paramSet = paramSet;
		else
			throw new InconsistentComponentException();

	}
	
}
