package kr.geul.options.option;

import kr.geul.options.pricer.BSPricer;
import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.parameterset.BSParameterSet;
import kr.geul.options.parameterset.ParameterSet;
import kr.geul.options.parameterset.SVParameterSet;
import kr.geul.options.parameterset.SVJParameterSet;
import kr.geul.options.pricer.Pricer;
import kr.geul.options.pricer.SVPricer;
import kr.geul.options.pricer.SVJPricer;

public abstract class Option {

	protected double underlyingPrice, strikePrice, timeToMaturity, riskFreeRate, 
	dividendRate, optionPrice, tradingVolume, delta;
	protected String type;
	protected ParameterSet paramSet;
	protected Pricer pricer;
	
	protected Option() {

		underlyingPrice = 0;
		strikePrice = 0;
		timeToMaturity = 0;
		dividendRate = 0;
		riskFreeRate = 0;
		optionPrice = 0;
		tradingVolume = 0;
		delta = 0;
		type = "Real";

	}

	protected Option(String type) {

		underlyingPrice = 0;
		strikePrice = 0;
		timeToMaturity = 0;
		dividendRate = 0;
		riskFreeRate = 0;
		optionPrice = 0;
		tradingVolume = 0;
		delta = 0;
		this.type = type; 

		switch (type) {
		
		case "BS":
			paramSet = new BSParameterSet();
			pricer = new BSPricer();
			break;
		case "SV":
			paramSet = new SVParameterSet();
			pricer = new SVPricer();
			break;
		case "SVJ":
			paramSet = new SVJParameterSet();
			pricer = new SVJPricer();
			break;
		default:
			
		}

	}

	public abstract void evaluate();
	
	public double get(String name) throws InvalidArgumentException {

		switch (identifyName(name)) {

		case "underlyingPrice": 
			return underlyingPrice;

		case "strikePrice": 			
			return strikePrice;

		case "timeToMaturity":
			return timeToMaturity;

		case "riskFreeRate": 
			return riskFreeRate;

		case "dividendRate":			
			return dividendRate;

		case "optionPrice":			
			return optionPrice;

		case "tradingVolume":			
			return tradingVolume;
		
		case "delta":			
			return delta;
			
		default:			
			boolean isInParamSet = paramSet.isIn(name);
			if (isInParamSet == true)
				return paramSet.get(name);
			else
				throw new InvalidArgumentException(name);

		}

	}

	public abstract double getBSImpVol() throws InvalidArgumentException;
	
	public double getDelta() {
		return delta;
	}
	
	public String getInfo() {
		
		String info = "S: " + underlyingPrice + ", K: " + strikePrice 
				+ ", T: " + timeToMaturity + ", R: " + riskFreeRate 
				+ ", D: " + dividendRate + ", C: " + optionPrice 
				+ ", V: " + tradingVolume + ", delta: " + delta;
				
		if (paramSet != null) {
			
			String[] names = paramSet.getNames();
			double[] values = paramSet.getValues();
			
			for (int i = 0; i < names.length ; i++) {
				
				info += ", " + names[i] + ": " + values[i];
					
			}
			
		}
		
		return info;
	}
	
	public String[] getParameterNames() {
		return paramSet.getNames();
	}
	
	public double getStrikePrice() {
		return strikePrice;
	}

	public double getOptionPrice() {
		return optionPrice;
	}
	
	public double getTradingVolume() {
		return tradingVolume;
	}
	
	public String getType() {
		return type;
	}
	
	public double[] getVariableArray() {
		double[] array = {underlyingPrice, strikePrice, timeToMaturity, 
				riskFreeRate, dividendRate, optionPrice};
		return array;
	}
	
	protected String identifyName(String name) {

		String result;

		switch (name) {

		case "s": case "S": case "underlying": case "underlyingPrice": case "underlyingprice": 
		case "stockPrice": case "stockprice": case "sharePrice": case "shareprice":

			result = "underlyingPrice";
			break;

		case "k": case "K": case "x": case "X": case "strike": 
		case "strikePrice": case "strikeprice":

			result = "strikePrice";
			break;

		case "t": case "T": case "tau": case "ttm": case "TTM":
		case "timeToMaturity": case "timetomaturity": 

			result = "timeToMaturity";
			break;

		case "r": case "R": case "rfr": case "RFR": 
		case "riskFreeRate": case "riskfreerate":  

			result = "riskFreeRate";
			break;

		case "d": case "D": case "div": case "Div": 
		case "dividend": case "dividendRate": case "dividendrate": 

			result = "dividendRate";
			break;

		case "c": case "C": case "p": case "P": case "price":  
		case "optionPrice": case "optionprice":  

			result = "optionPrice";
			break;

		case "v": case "V": case "volume": case "tradingvolume":

			result = "tradingVolume";
			break;
			
		default:
			result = name;

		}

		return result;

	}

	public abstract boolean isCall();
	
	protected void set(String name, double value) throws InvalidArgumentException, 
	AtTheMoneyException {

		switch (identifyName(name)) {

		case "underlyingPrice": 
			underlyingPrice = value;
			break;

		case "strikePrice": 			
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

		case "tradingVolume":			
			tradingVolume = value;
			break;
			
		case "delta":			
			delta = value;
			break;
			
		default:			
			boolean isInParamSet = paramSet.set(name, value);
			if (isInParamSet == false)
				throw new InvalidArgumentException(name);

		}

	}

	public void set(String[] name, double[] value) throws InvalidArgumentException, 
	InconsistentArgumentLengthException, AtTheMoneyException {

		if (name.length != value.length)
			throw new InconsistentArgumentLengthException(name.length, value.length);
		
		else {
		
			for (int i = 0; i < name.length; i++) {
				set(name[i], value[i]);
			}
			
		}
		
	}
	
}