package kr.geul.options.pricer;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

public abstract class Pricer {

	protected Pricer() {
	}
	
	public abstract double priceCall(double[] variableArray, double[] parameterArray)
			throws TooManyEvaluationsException;
	public abstract double pricePut (double[] variableArray, double[] parameterArray)
			throws TooManyEvaluationsException;	
	protected abstract void setVariables(double[] variableArray,
			double[] parameterArray);
	
}
