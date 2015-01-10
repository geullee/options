package kr.geul.options.structure;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.DuplicateOptionsException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentOptionException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.OTMOption;
import kr.geul.options.option.Option;
import kr.geul.options.option.PutOption;

public class OptionCurve {

	protected double extrapolationLeftEnd = -1, extrapolationRightEnd = -1, strikePriceGap = -1;
	protected double underlyingPrice, timeToMaturity, riskFreeRate,	dividendRate;
	
	protected final String[] generatedOptionParameterNames = {"S", "K", "R", "T", "D", "C", "sigma"};
	
	protected ArrayList<Option> options;
	
	public OptionCurve() {
		options = new ArrayList<Option>();
	}

	public OptionCurve (Option option) {

		options = new ArrayList<Option>();
		
		addOption(option);

	}

	public void add(Option option) throws DuplicateOptionsException, 
	InconsistentOptionException {
				
		if (options.size() == 0 || 
				(areBasicVariablesEqual(option) == true && isNew(option) == true))
			addOption(option);
		
	}

	protected void addOption(Option option) {

		double[] array = option.getVariableArray();
		
		if (options.size() == 0) {
			
			underlyingPrice = array[0];
			timeToMaturity = array[2];
			riskFreeRate = array[3];
			dividendRate = array[4];

			options.add(option);
			
		}

		else {
			
			double strikePrice = array[1];
			boolean isIncluded = false;
			int location = 0;

			while (isIncluded == false) {

				if ((location == options.size() && isIncluded == false) ||
						(location == 0 && options.get(location).getStrikePrice() >= strikePrice) ||
						(options.size() > 1 && location >= 1 &&
						options.get(location).getStrikePrice() >= strikePrice && 
						options.get(location - 1).getStrikePrice() < strikePrice)) {

					if (location < options.size() && 
						options.get(location).getStrikePrice() == strikePrice && 
						options.get(location).isCall() == true) 
						location++;
					
					options.add(location, option);
					isIncluded = true;
					
				}

				else
					location++;

			}

		}
		
	}

	protected boolean areBasicVariablesEqual(Option option) {

		double[] array = option.getVariableArray();
		if (array[0] == underlyingPrice && array[2] == timeToMaturity 
				&& array[3] == riskFreeRate && array[4] == dividendRate)
			return true;
		else
			return false;

	}
	
	public void extrapolate(double precisionMultiplier) throws InvalidArgumentException, 
	InconsistentArgumentLengthException, DuplicateOptionsException, InconsistentOptionException, AtTheMoneyException, TooManyEvaluationsException {
		
		if (extrapolationLeftEnd > -1 && extrapolationRightEnd > -1 && strikePriceGap > -1) {
			
			System.out
					.println("Implied volatility curve for T = "
							+ timeToMaturity + " is linearly extrapolated");
			
			Option leftEndOption = options.get(0), 
					rightEndOption = options.get(options.size() - 1);
			
			double leftEndStrikePrice = leftEndOption.getStrikePrice(),
					rightEndStrikePrice = rightEndOption.getStrikePrice(),
					leftEndBSImpVol = leftEndOption.getBSImpVol(), 
					rightEndBSImpVol = rightEndOption.getBSImpVol(),
					extrapolationLeftWidth = leftEndStrikePrice - extrapolationLeftEnd,
					extrapolationRightWidth = extrapolationRightEnd - rightEndStrikePrice;
			
			int numberOfGeneratedPuts = (int) Math.ceil(extrapolationLeftWidth / strikePriceGap),
					numberOfGeneratedCalls = (int) Math.ceil(extrapolationRightWidth / strikePriceGap);
			
			System.out.println("Total of " + numberOfGeneratedPuts + " puts and " + 
					numberOfGeneratedCalls + " calls are generated.");
						
			System.out.print("Generating puts... ");
			
			for (int i = 0; i < numberOfGeneratedPuts; i++) {
				
				double strike;
				PutOption option = new PutOption("BS");

				strike = 
					Math.round((extrapolationLeftEnd + (strikePriceGap * i)) * precisionMultiplier) / 
					precisionMultiplier;
				
				double[] parameterValues = {underlyingPrice, strike, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, leftEndBSImpVol};
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				
				add(option);

			}

			System.out.println("DONE");
			System.out.print("Generating calls... ");
			
			for (int i = 0; i < numberOfGeneratedCalls; i++) {

				double strike;
				CallOption option = new CallOption("BS");

				strike = 
					Math.round((extrapolationRightEnd - (strikePriceGap * i)) * precisionMultiplier) /
					precisionMultiplier;
				
				double[] parameterValues = {underlyingPrice, strike, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, rightEndBSImpVol};
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				
				add(option);

			}
			
			System.out.println("DONE");
			
		}

	}
	
	public OptionCurve getCallCurve() throws DuplicateOptionsException, 
	InconsistentOptionException {
		
		OptionCurve callCurve = new OptionCurve();
		
		for (int i = 0; i < options.size(); i++) {
			
			Option option = options.get(i);	
			if (option.isCall() == true)
				callCurve.add(option);
			
		}
		
		return callCurve;
		
	}
	
	public OptionCurve getPutCurve() throws DuplicateOptionsException, 
	InconsistentOptionException {
		
		OptionCurve putCurve = new OptionCurve();
		
		for (int i = 0; i < options.size(); i++) {
			
			Option option = options.get(i);	
			if (option.isCall() == false)
				putCurve.add(option);
			
		}
		
		return putCurve;
		
	}
	
	public void interpolate(double precisionMultiplier, boolean isATMOptionGenerated) throws InvalidArgumentException, InconsistentArgumentLengthException, 
		AtTheMoneyException, DuplicateOptionsException, InconsistentOptionException, FileNotFoundException {
			
		double[] strikes = getStrikePrices();
		double[] impVols = getBSImpVols();
				
		System.out.println("[ORIGINAL IMPLIED VOLATILITIES]");
		
		for (int i = 0; i < strikes.length; i++) {
			System.out.println(strikes[i] + "," + impVols[i]);
		}
		
		System.out.println("Interpolation is being done for T = " + timeToMaturity + 
				" based on implied volatilities in the range from K = " + 
				strikes[0] + " to K = " + strikes[strikes.length - 1]);
				
		UnivariateInterpolator interpolator = new SplineInterpolator();
		UnivariateFunction function = interpolator.interpolate(strikes, impVols);
		
		options = new ArrayList<Option>();
		
		System.out.println("Getting new middle strikes: ");
		
		System.out.println("OTM put options are generated from K = " + 
				strikes[0] + " to K = " + underlyingPrice);
		
		for (double putStrike = strikes[0]; putStrike < underlyingPrice; 
				putStrike += strikePriceGap) {
			
				OTMOption option = new OTMOption("BS");
				double putStrikeRounded = 
						Math.round(putStrike * precisionMultiplier) / precisionMultiplier;
			
			if (putStrikeRounded < underlyingPrice) {
				
				double[] parameterValues = {underlyingPrice, putStrikeRounded, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, function.value(putStrikeRounded)};
				
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				add(option);
				
			}
			
		}
		
		if (isATMOptionGenerated == true) {
		
			CallOption callATMOption = new CallOption("BS");
			PutOption putATMOption = new PutOption("BS");
			
			double[] atmParameterValues = {underlyingPrice, underlyingPrice, riskFreeRate, timeToMaturity,
					dividendRate, 0.0, function.value(underlyingPrice)};
			callATMOption.set(generatedOptionParameterNames, atmParameterValues);
			putATMOption.set(generatedOptionParameterNames, atmParameterValues);
			callATMOption.evaluate();
			putATMOption.evaluate();
			add(callATMOption);
			add(putATMOption);
			
		}
		
		System.out.println("OTM call options are generated from K = " + 
				underlyingPrice + " to K = " + strikes[strikes.length - 1]);
		
		for (double callStrike = strikes[strikes.length - 1]; 
				callStrike > underlyingPrice; 
				callStrike -= strikePriceGap) {
						
			OTMOption option = new OTMOption("BS");
			double callStrikeRounded = 
					Math.round(callStrike * precisionMultiplier) / precisionMultiplier;
			
			if (callStrikeRounded > underlyingPrice) {
			
				double[] parameterValues = {underlyingPrice, callStrikeRounded, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, function.value(callStrikeRounded)};
				
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				add(option);
				
			}
			
		}
		
	}

	protected boolean isNew(Option option) {

		boolean isNew = true;

		double[] array = option.getVariableArray();

		for (int i = 0; i < options.size(); i++) {

			if (options.get(i).getStrikePrice() == array[1] &&
					((option.isCall() == true && options.get(i).isCall() == true)
							|| (option.isCall() == false && options.get(i).isCall() == false))) 				
				isNew = false;
		}

		return isNew;

	}

	public Option get(int index) {
		return options.get(index);
	}

	private double[] getBSImpVols() throws InvalidArgumentException {
		
		double[] impVols = new double[options.size()];
		
		for (int i = 0; i < impVols.length; i++) {
			
			Option option = options.get(i);
			if (option.getType().equals("BS") &&
					option.get("sigma") > 0.0)
				impVols[i] = option.get("sigma");
			else
				impVols[i] = option.getBSImpVol();
		}
		
		return impVols;
		
	}
	
	public String[] getInfo() {

		String[] info = new String[options.size() + 1];

		info[0] = "S: " + underlyingPrice + ", T: " + timeToMaturity + ", R: " + 
				riskFreeRate + ", D: " + dividendRate;

		for (int i = 0; i < options.size(); i++) {

			info[i + 1] = "(" + (options.get(i).isCall() ? "C" : "P") + ") K: " + 
					options.get(i).getStrikePrice() +
					", Price: " + options.get(i).getOptionPrice() + 
					", Volume: " + options.get(i).getTradingVolume() +
					", Delta: " + options.get(i).getDelta();

		}

		return info;

	}

	public ArrayList<Option> getOptions() throws InvalidArgumentException, 
	InconsistentArgumentLengthException {
		return options;
	}
		
	public double[] getStrikePrices() {
		
		double[] strikePrices = new double[options.size()];
		
		for (int i = 0; i < strikePrices.length; i++) {
			strikePrices[i] = options.get(i).getStrikePrice();
		}
		
		return strikePrices;
		
	}
	
	public double[] getVariableArray() {
		double[] array = {underlyingPrice, timeToMaturity, riskFreeRate, dividendRate};
		return array;
	}

	public void setExtrapolationRange(double leftEnd, double rightEnd) {

		if (leftEnd > 0.0 && leftEnd < underlyingPrice && 
				rightEnd > 0.0 && rightEnd >  underlyingPrice)
		extrapolationLeftEnd = leftEnd;
		extrapolationRightEnd = rightEnd;

	}

	public void setStrikePriceGap(double gap) {
		if (gap > 0)
			strikePriceGap = gap;
	}

	public int size() {
		return options.size();
	}
	
	public void trim(double[] trimmingLocations) {
		
		double kMin = trimmingLocations[0], kMax = trimmingLocations[1];
		System.out
				.println("Trimming option curve for T = " + timeToMaturity + ".");
		System.out.println("K_min: " + kMin + ", K_max: " + kMax); 
		
		for (int i = options.size() - 1; i > -1; i--) {
			
			double strike = options.get(i).getStrikePrice(); 
			if (strike < kMin || strike > kMax) 
				options.remove(i);
			
		}
		
	}
	
	private void writeLog(String content) throws FileNotFoundException {

		Calendar currentTime = Calendar.getInstance();
		currentTime.getTime();
		SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		String log = "[" + timeFormat.format(currentTime.getTime()) + 
				"] (options/OptionCurve) " + content;

	}
	
}
