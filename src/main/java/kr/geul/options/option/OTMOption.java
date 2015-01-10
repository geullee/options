package kr.geul.options.option;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.Option;
import kr.geul.options.pricer.BSPricer;

public class OTMOption extends Option {

	public OTMOption() {
		super();
	}
	
	public OTMOption(String string) {
		super(string);
	}

	public void evaluate()  throws TooManyEvaluationsException {
			
		if (strikePrice > underlyingPrice)		
			optionPrice = pricer.priceCall(getVariableArray(), paramSet.getValues());
		else
			optionPrice = pricer.pricePut(getVariableArray(), paramSet.getValues());
		
	}

	public double getBSImpVol() throws InvalidArgumentException {
		
		if (type.equals("BS") &&
				get("sigma") > 0.0)
			return get("sigma");
		else if (strikePrice > underlyingPrice)		
			return BSPricer.getCallImpVol(getVariableArray());
		else
			return BSPricer.getPutImpVol(getVariableArray());
		
	}
	
	public boolean isCall() {
		
		if (strikePrice > underlyingPrice)		
			return true;
		else
			return false;

	}

	@Override
	protected void set(String name, double value) throws InvalidArgumentException, AtTheMoneyException {
 
		switch (identifyName(name)) {

		case "underlyingPrice": 
			
			if (strikePrice == value)
				throw new AtTheMoneyException();
			else
				underlyingPrice = value;
			
			break;

		case "strikePrice": 	
			
			if (underlyingPrice == value)
				throw new AtTheMoneyException();
			else
				strikePrice = value;
			break;

		case "timeToMaturity":
			timeToMaturity = value;
			break;

		case "riskFreeRate": 
			riskFreeRate = value;
			break;

		case "dividendRate":			
			dividendRate = value;
			break;

		case "optionPrice":			
			optionPrice = value;
			break;

		default:		
			boolean isInParamSet = paramSet.set(name, value);
			if (isInParamSet == false)
				throw new InvalidArgumentException(name);

		}

	}
	
}
