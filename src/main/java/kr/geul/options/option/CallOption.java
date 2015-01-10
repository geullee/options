package kr.geul.options.option;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.Option;
import kr.geul.options.pricer.BSPricer;

public class CallOption extends Option {

	public CallOption() {
		super();
	}
	
	public CallOption(String string) {
		super(string);
	}
	
	public void evaluate() throws TooManyEvaluationsException {
		optionPrice = pricer.priceCall(getVariableArray(), paramSet.getValues());
	}

	public double getBSImpVol() throws InvalidArgumentException {
		
		if (type.equals("BS") &&
				get("sigma") > 0.0)
			return get("sigma");
		else
			return BSPricer.getCallImpVol(getVariableArray());
		
	}
	
	public boolean isCall() {
		return true;
	}
	
}
