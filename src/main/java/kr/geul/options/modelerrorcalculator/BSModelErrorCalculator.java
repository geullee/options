package kr.geul.options.modelerrorcalculator;

import kr.geul.options.option.Option;
import kr.geul.options.parameterset.ParameterSet;
import kr.geul.options.pricer.BSPricer;
import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentComponentException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.PutOption;

public class BSModelErrorCalculator extends ModelErrorCalculator {

	public BSModelErrorCalculator() {
		
		pricer = new BSPricer();
		modelType = "BS";
		paramNames = new String[1];
		paramLowerBounds = new double[1];
		paramUpperBounds = new double[1];
		paramScales = new double[1];
		paramSigmas = new double[1];
		paramNames[0] = "sigma";
		paramLowerBounds[0] = 0.03; 
		paramUpperBounds[0] = 3.0;
		paramScales[0] = 10000;
		paramSigmas[0] = 1;
		
	}

	protected Option getModelOption(Option option, double[] param) 
			throws InvalidArgumentException, InconsistentArgumentLengthException, AtTheMoneyException {
		
		Option modelOption;
		
		if (option.isCall() == true)
			modelOption = new CallOption("BS");
		else
			modelOption = new PutOption("BS");
			
		String[] parameterNames = {"S", "K", "T", "R", "D", "C"};
		double[] parameterValues = option.getVariableArray();
		
		modelOption.set(parameterNames, parameterValues);
		modelOption.set(paramSet.getNames(), param);
		
		modelOption.evaluate();
		
		return modelOption;
		
	}

	public void setParameterSet(ParameterSet paramSet) 
			throws InconsistentComponentException {
		
		if (paramSet.getType().equals("BS"))
			this.paramSet = paramSet;
		else
			throw new InconsistentComponentException();
		
	}
	
}
