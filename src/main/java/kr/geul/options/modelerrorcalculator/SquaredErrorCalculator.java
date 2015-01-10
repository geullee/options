package kr.geul.options.modelerrorcalculator;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.Option;
import kr.geul.options.option.PutOption;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class SquaredErrorCalculator {

	public synchronized Double calculate (Option option, double[] parameters, String modelType) 
			throws InvalidArgumentException, InconsistentArgumentLengthException, AtTheMoneyException, 
			TooManyEvaluationsException {
		
		double actualPrice, modelPrice;
		Option modelOption = null;
				
		modelOption = getModelOption(option, parameters, modelType);
		actualPrice = option.getVariableArray()[5];
		modelPrice = modelOption.getVariableArray()[5];
		
		return Math.pow(actualPrice - modelPrice, 2);
		
	}
	
	protected synchronized Option getModelOption(Option option, double[] param, String modelType) 
			throws InvalidArgumentException, InconsistentArgumentLengthException, AtTheMoneyException,
			TooManyEvaluationsException {
		
		Option modelOption;
		
		switch (modelType) {
		
		case "BS":
			if (option.isCall() == true)
				modelOption = new CallOption("BS");
			else
				modelOption = new PutOption("BS");
			break;
			
		default:
			if (option.isCall() == true)
				modelOption = new CallOption("SVJ");
			else
				modelOption = new PutOption("SVJ");
			break;
		
		}
		
		String[] parameterNames = modelOption.getParameterNames();
		String[] variableNames = {"S", "K", "T", "R", "D", "C"};
		double[] variableValues = option.getVariableArray();
		
		modelOption.set(variableNames, variableValues);
		modelOption.set(parameterNames, param);
		
		try {
		modelOption.evaluate();
		}
		
		catch (Exception e) {
			throw new TooManyEvaluationsException(1);
		}
		
		return modelOption;
		
	}
	
}
