package kr.geul.options.option;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.Option;
import kr.geul.options.pricer.BSPricer;

public class PutOption extends Option {

	public PutOption() {
		super();
	}

	public PutOption(String string) {
		super(string);
	}

	public synchronized void evaluate() throws TooManyEvaluationsException {
		optionPrice = pricer.pricePut(getVariableArray(), paramSet.getValues());
	}

	public double getBSImpVol() throws InvalidArgumentException {
		
		if (type.equals("BS") &&
				get("sigma") > 0.0)
			return get("sigma");
		else
			return BSPricer.getPutImpVol(getVariableArray());
		
	}

	public boolean isCall() {
		return false;
	}
	
}
