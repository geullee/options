package kr.geul.options.pricer;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class BSPricer extends Pricer {

	static final Apfloat initialIV = new Apfloat(0.3);
	static final Apfloat initialStepSize = new Apfloat(0.05);
	static final Apfloat tolerance = new Apfloat(Math.pow(10, -11));

	static private double underlyingPrice, strikePrice, timeToMaturity, 
	riskFreeRate, dividendRate, optionPrice, sigma;

	public static double getCallImpVol(double[] variableArray) {

		setVariables(variableArray);
		
		Apfloat iv = initialIV;
		Apfloat stepSize = initialStepSize;
		Apfloat optionPriceApfloat = new Apfloat(optionPrice);
		Apfloat price_BS = new Apfloat(99999.99);
		
		
		while (ApfloatMath.abs(price_BS.subtract(optionPriceApfloat)).compareTo(tolerance) == 1 && 
				iv.compareTo(Apfloat.ZERO) == 1 && iv.compareTo(new Apfloat(3.0)) <= 0) {

			price_BS = BlackScholes.getCallPriceWithDividend(underlyingPrice, strikePrice, riskFreeRate, 
					dividendRate, iv, timeToMaturity);
			
			if (ApfloatMath.abs(price_BS.subtract(optionPriceApfloat)).compareTo(tolerance) == 1) {
				
				if ((price_BS.compareTo(optionPriceApfloat) == 1 && 
						stepSize.compareTo(Apfloat.ZERO) == 1) || 
						(price_BS.compareTo(optionPriceApfloat) == -1 && 
						stepSize.compareTo(Apfloat.ZERO) == -1)) 
					stepSize = stepSize.multiply(new Apfloat(-0.5));

				iv = iv.add(stepSize);
				
			}

		}

		if (iv.compareTo(Apfloat.ZERO) <= 0 || iv.compareTo(new Apfloat(3.0)) == 1)
			iv = new Apfloat(-99.99);
			
		return iv.doubleValue();		

	}

	public static double getPutImpVol(double[] variableArray) {

		setVariables(variableArray);
		
		Apfloat iv = initialIV;
		Apfloat stepSize = initialStepSize;
		Apfloat optionPriceApfloat = new Apfloat(optionPrice);
		Apfloat price_BS = new Apfloat(99999.99);
		
		while (ApfloatMath.abs(price_BS.subtract(optionPriceApfloat)).compareTo(tolerance) == 1 && 
				iv.compareTo(Apfloat.ZERO) == 1 && iv.compareTo(new Apfloat(3.0)) <= 0) {

			price_BS = BlackScholes.getPutPriceWithDividend(underlyingPrice, strikePrice, riskFreeRate, 
					dividendRate, iv, timeToMaturity);
			
			if (ApfloatMath.abs(price_BS.subtract(optionPriceApfloat)).compareTo(tolerance) == 1) {
				
				if ((price_BS.compareTo(optionPriceApfloat) == 1 && 
						stepSize.compareTo(Apfloat.ZERO) == 1) || 
						(price_BS.compareTo(optionPriceApfloat) == -1 && 
						stepSize.compareTo(Apfloat.ZERO) == -1)) 
					stepSize = stepSize.multiply(new Apfloat(-0.5));

				iv = iv.add(stepSize);
				
			}

		}

		if (iv.compareTo(Apfloat.ZERO) <= 0 || iv.compareTo(new Apfloat(3.0)) == 1)
			iv = new Apfloat(-99.99);
			
		return iv.doubleValue();	

	}
	
	public double priceCall(double[] variableArray, double[] parameterArray) {

		setVariables(variableArray, parameterArray);

		return BlackScholes.getCallPriceWithDividend(underlyingPrice, strikePrice, riskFreeRate, 
				dividendRate, sigma, timeToMaturity);

	}

	public double pricePut(double[] variableArray, double[] parameterArray) {

		setVariables(variableArray, parameterArray);

		return BlackScholes.getPutPriceWithDividend(underlyingPrice, strikePrice, riskFreeRate, 
				dividendRate, sigma, timeToMaturity);

	}

	private static void setVariables(double[] variableArray) {

		underlyingPrice = variableArray[0];
		strikePrice = variableArray[1];
		timeToMaturity = variableArray[2];
		riskFreeRate = variableArray[3];
		dividendRate = variableArray[4];
		optionPrice = variableArray[5];
		
	}
	
	protected void setVariables(double[] variableArray,
			double[] parameterArray) {

		underlyingPrice = variableArray[0];
		strikePrice = variableArray[1];
		timeToMaturity = variableArray[2];
		riskFreeRate = variableArray[3];
		dividendRate = variableArray[4];
		optionPrice = variableArray[5];
		sigma = parameterArray[0];
		
	}

}
