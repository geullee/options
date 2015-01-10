package kr.geul.options.structure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
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
import kr.geul.options.structure.OptionCurve;

public class OptionSurface {

	protected final String[] generatedOptionParameterNames = {"S", "K", "R", "T", "D", "C", "sigma"};

	protected ArrayList<Double> timeToMaturities;
	protected ArrayList<OptionCurve> curves;
	protected double underlyingPrice, riskFreeRate,	dividendRate;

	public OptionSurface (OptionCurve curve) {

		curves = new ArrayList<OptionCurve>();
		timeToMaturities = new ArrayList<Double>();

		double[] array = curve.getVariableArray();

		underlyingPrice = array[0];
		riskFreeRate = array[2];
		dividendRate = array[3];
		curves.add(curve);
		timeToMaturities.add(array[1]);

	}

	public OptionSurface() {

		curves = new ArrayList<OptionCurve>();
		timeToMaturities = new ArrayList<Double>();

	}

	public void add(OptionCurve curve) throws InconsistentOptionException, DuplicateOptionsException {

		if (timeToMaturities.size() == 0 ||
				(areBasicVariablesEqual(curve) == true && isNew(curve) == true)) {
			addCurve(curve);
		}

	}

	private void addCurve(OptionCurve curve) {

		double[] array = curve.getVariableArray();

		if (timeToMaturities.size() == 0) {

			underlyingPrice = array[0];
			riskFreeRate = array[2];
			dividendRate = array[3];
			curves.add(curve);
			timeToMaturities.add(array[1]);

		}

		else {

			double timeToMaturity = array[1];
			boolean isIncluded = false;
			int location = 1;

			while (isIncluded == false) {

				if ((location == timeToMaturities.size() && isIncluded == false) ||
						(timeToMaturities.get(location - 1) < timeToMaturity 
								&& timeToMaturities.get(location) > timeToMaturity)) {

					curves.add(location, curve);
					timeToMaturities.add(location, timeToMaturity);
					isIncluded = true;

				}

				else
					location++;

			}

		}

	}

	private boolean areBasicVariablesEqual(OptionCurve curve) {

		double[] array = curve.getVariableArray();
		if (array[0] == underlyingPrice)
			return true;
		else
			return false;

	}

	public void extrapolate(double precisionMultiplier) throws TooManyEvaluationsException, InvalidArgumentException, 
	InconsistentArgumentLengthException, DuplicateOptionsException, InconsistentOptionException, 
	AtTheMoneyException {

		for (int i = 0; i < curves.size(); i++) {
			curves.get(i).extrapolate(precisionMultiplier);
		}

	}

	public void fixTimeToMaturities(double[] newTimeToMaturities, UnivariateFunction kMinFunction, 
			UnivariateFunction kMaxFunction, double precisionMultiplier) throws InvalidArgumentException, 
			InconsistentArgumentLengthException, AtTheMoneyException, DuplicateOptionsException, 
			InconsistentOptionException, FileNotFoundException {

		double[] strikes = getFullStrikes(precisionMultiplier), oldTimeToMaturities = getOldTimeToMaturities();
		double[][] impVols = new double[strikes.length][oldTimeToMaturities.length];

		for (int ttm = 0; ttm < curves.size(); ttm++) {

			OptionCurve curve = curves.get(ttm);
			System.out.println("Volatility curve for T = " + oldTimeToMaturities[ttm] + " is collected.");

			ArrayList<Option> options = curve.getOptions();

			double[] curveStrikes = curve.getStrikePrices();
			System.out.println("Orginal curve spans the domain from K = "
					+ curveStrikes[0] + " to K = "
					+ curveStrikes[curveStrikes.length - 1]);
			
			double[] adjustedCurveStrikes = getEndPointAdjustedStrikePrices(strikes, curveStrikes);
			System.out.println("Adjusted curve spans the domain from K = "
					+ adjustedCurveStrikes[0] + " to K = "
					+ adjustedCurveStrikes[adjustedCurveStrikes.length - 1]);
			
			double[] adjustedCurveIVs = getEndPointAdjustedImpVols(options, adjustedCurveStrikes, curveStrikes[0],
					curveStrikes[curveStrikes.length - 1]);

			UnivariateInterpolator interpolator = new SplineInterpolator();
			UnivariateFunction curveFunction = interpolator.interpolate
					(adjustedCurveStrikes, adjustedCurveIVs);

			System.out
					.println("Implied volatility surface is being constructed.");
			
			for (int strike = 0; strike < strikes.length; strike++) {
				impVols[strike][ttm] = curveFunction.value(strikes[strike]);			
			}

		}
		
		BivariateGridInterpolator interpolator = new BicubicSplineInterpolator();
		BivariateFunction surfaceFunction = interpolator.interpolate(strikes, oldTimeToMaturities, impVols);

		curves = new ArrayList<OptionCurve>();
		timeToMaturities = new ArrayList<Double>();

		for (int i = 0; i < newTimeToMaturities.length; i++) {

			if (newTimeToMaturities[i] > oldTimeToMaturities[0] &&
					newTimeToMaturities[i] < oldTimeToMaturities[oldTimeToMaturities.length - 1]) {

				double kMin = 
						(double) Math.round(kMinFunction.value(newTimeToMaturities[i]) * precisionMultiplier) /
						precisionMultiplier;
				double kMax = (double) Math.round(kMaxFunction.value(newTimeToMaturities[i]) * precisionMultiplier) / 
						precisionMultiplier;
				
				System.out.println("Curve for T = " + newTimeToMaturities[i] + " is generated.");
				System.out.println("Approximated minimum and maximum strikes are: K_min = " + 
						kMin + ", K_max = " + kMax);

				OptionCurve newCurve = new OptionCurve();

				for (int j = 0; j < strikes.length; j++) {

					if (strikes[j] >= kMin && strikes[j] <= kMax && strikes[j] != underlyingPrice) {

						Option option = new OTMOption("BS");
						double impVol = surfaceFunction.value(strikes[j], newTimeToMaturities[i]);
						
						double[] parameterValues = {underlyingPrice, strikes[j], riskFreeRate, 
								newTimeToMaturities[i],	dividendRate, 0.0, 
								Math.max(0.0, impVol)};
						option.set(generatedOptionParameterNames, parameterValues);

						if (Math.max(0.0, impVol) > 0.0)
							option.evaluate();

						newCurve.add(option);

					}

				}

				System.out
						.println("The new curve contains "
								+ newCurve.size()
								+ " options that spans the domain from K = "
								+ newCurve.getStrikePrices()[0]
								+ " to K = "
								+ newCurve.getStrikePrices()[newCurve.size() - 1] + ".");
				
				add(newCurve);

			}

		}

	}
	
	public ArrayList<OptionCurve> getCurves() {
		return curves;
	}

	private double[] getEndPointAdjustedStrikePrices(double[] strikes, double[] curveStrikes) {
		
		double leftEndCurveStrike = curveStrikes[0], 
				rightEndCurveStrike = curveStrikes[curveStrikes.length - 1];
		double[] result;
		
		ArrayList<Double> resultList = new ArrayList<Double>();
		
		System.out.println("Strikes are extended from K = " + leftEndCurveStrike
				+ " to K = " + strikes[0] + ":");
		
		for (int i = 0; i < strikes.length; i++) {
			if (strikes[i] < leftEndCurveStrike) {
				resultList.add(strikes[i]);
			}
		}
		
		System.out.println("Original strikes are added: ");
		
		for (int i = 0; i < curveStrikes.length; i++) {
			resultList.add(curveStrikes[i]);
		}
		
		System.out.println("");
		System.out.println("Strikes are extended from K = " + rightEndCurveStrike
				+ " to K = " + strikes[strikes.length - 1] + ":");
		
		for (int i = 0; i < strikes.length; i++) {
			if (strikes[i] > rightEndCurveStrike) {
				resultList.add(strikes[i]);
			}
		}
		
		result = new double[resultList.size()];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = resultList.get(i);
		}
		
		return result;
		
	}
	
	private double[] getEndPointAdjustedImpVols(ArrayList<Option> options, 
			double[] adjustedCurveStrikes, double leftEnd, double rightEnd) throws InvalidArgumentException {
		
		double[] result = new double[adjustedCurveStrikes.length];
		double leftEndIV = options.get(0).getBSImpVol(),
			   rightEndIV = options.get(options.size() - 1).getBSImpVol();
		
		System.out.println("Left-end IV is collected at K = "
				+ options.get(0).getStrikePrice() + ", and its value is "
				+ leftEndIV);
		System.out.println("Right-end IV is collected at K = "
				+ options.get(options.size() - 1).getStrikePrice() + ", and its value is "
				+ rightEndIV);
		
		int leftEndIndex = 0, rightEndIndex = 0;
		
		int endIndexFinderPointer = 0;
		boolean isLeftIndexFound = false, isRightIndexFound = false;
		
		do {
			
			if (adjustedCurveStrikes[endIndexFinderPointer] == leftEnd)
				leftEndIndex = endIndexFinderPointer;
			else if (adjustedCurveStrikes[endIndexFinderPointer] == rightEnd)
				rightEndIndex = endIndexFinderPointer;
			
		} while (++endIndexFinderPointer < adjustedCurveStrikes.length &&
				(isLeftIndexFound == false || isRightIndexFound == false));
		
		System.out.println("Volatility curve is extrapolated from K = "
				+ adjustedCurveStrikes[leftEndIndex] + " to K = "
				+ adjustedCurveStrikes[0] + " using the left-end IV.");
		
		for (int i = 0; i <= leftEndIndex; i++) {
			result[i] = leftEndIV;
		}
		
		System.out.println("Original volatility curve is transferred from K = "
				+ adjustedCurveStrikes[leftEndIndex] + " to K = "
				+ adjustedCurveStrikes[rightEndIndex] + ".");
		
		for (int i = leftEndIndex + 1; i < rightEndIndex; i++) {
			result[i] = options.get(i - leftEndIndex).getBSImpVol();
		}
		
		System.out.println("Volatility curve is extrapolated from K = "
				+ adjustedCurveStrikes[rightEndIndex] + " to K = "
				+ adjustedCurveStrikes[adjustedCurveStrikes.length - 1] + " using the right-end IV.");
		
		for (int i = rightEndIndex; i < result.length; i++) {
			result[i] = rightEndIV;
		}
		
		return result;
		
	}
	
	private double[] getFullStrikes(double precisionMultiplier) throws InvalidArgumentException, 
	InconsistentArgumentLengthException {

		ArrayList<Double> strikesArray = new ArrayList<Double>(); 

		for (int i = 0; i < curves.size(); i++) {

			OptionCurve curve = curves.get(i);
			double[] strikes = curve.getStrikePrices();

			for (int j = 0; j < strikes.length; j++) {

				boolean isFound = false;
				double strike = Math.round(strikes[j] * precisionMultiplier) / precisionMultiplier;
				
				for (int k = 0; k < strikesArray.size(); k++) {
					if (strike == strikesArray.get(k)) 
						isFound = true;
				}

				if (isFound == false) {

					int strikesArrayPointer = 0;

					do {
		
						if (strikesArray.size() == 0 || (strikesArray.size() >= 1 && strikesArrayPointer == strikesArray.size() - 1 &&
								strike > strikesArray.get(strikesArrayPointer))) {
							strikesArray.add(strike);
							isFound = true;
						}

						else if ((strikesArray.size() >= 1 && strikesArrayPointer == 0 && 
								strike < strikesArray.get(0)) ||
								(strikesArray.size() >= 1 && strikesArrayPointer == strikesArray.size() - 1 &&
								strike < strikesArray.get(strikesArrayPointer))							) {
							strikesArray.add(strikesArrayPointer, strike);
							isFound = true;
						}

						else if	(strikesArray.size() > 1 && strikesArrayPointer < strikesArray.size() - 1 &&
								strike > strikesArray.get(strikesArrayPointer) &&
								strike < strikesArray.get(strikesArrayPointer + 1)) {
							strikesArray.add(strikesArrayPointer + 1, strike);
							isFound = true;
						}

					} while (++strikesArrayPointer < strikesArray.size() && isFound == false); 

				}

			}

		}

		double[] strikes = new double[strikesArray.size()];
		for (int i = 0; i < strikes.length; i++) {
			strikes[i] = strikesArray.get(i);
		}		

		return strikes;

	}

	public String[] getInfo() {

		int lines = 0;

		for (int i = 0; i < curves.size(); i++) {
			OptionCurve curve = curves.get(i);
			lines += curve.getStrikePrices().length;
		}

		lines += curves.size();

		String[] info = new String[lines];

		int lineIndex = 0;

		for (int i = 0; i < curves.size(); i++) {

			OptionCurve curve = curves.get(i);
			String[] curveInfo = curve.getInfo();

			for (int j = 0; j < curveInfo.length; j++) {
				info[lineIndex] = curveInfo[j];
				lineIndex++;
			}

		}

		return info;

	}

	private double[] getOldTimeToMaturities() {

		ArrayList<Double> ttmArray = new ArrayList<Double>();

		for (int i = 0; i < curves.size(); i++) {
			OptionCurve curve = curves.get(i);
			ttmArray.add(curve.getVariableArray()[1]);
		}

		double[] ttm = new double[ttmArray.size()];

		for (int i = 0; i < ttm.length; i++) {
			ttm[i] = ttmArray.get(i);
		}

		return ttm;

	}

	public void interpolate(double precisionMultiplier, boolean isATMOptionGenerated) throws InvalidArgumentException, InconsistentArgumentLengthException, 
	AtTheMoneyException, DuplicateOptionsException, InconsistentOptionException, FileNotFoundException {

		for (int i = 0; i < curves.size(); i++) {
			curves.get(i).interpolate(precisionMultiplier, isATMOptionGenerated);
		}

	}

	private boolean isNew(OptionCurve curve) {

		boolean isNew = true;

		double[] array = curve.getVariableArray();

		for (int i = 0; i < timeToMaturities.size(); i++) {

			if (timeToMaturities.get(i) == array[1]) 				
				isNew = false;
		}

		return isNew;

	}
	
	public void printInfo(String fileName) throws FileNotFoundException, InvalidArgumentException, InconsistentArgumentLengthException {
		
		File file = new File(fileName);
		PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
		
		for (int i = 0; i < curves.size(); i++) {
			
			OptionCurve curve = curves.get(i);
			ArrayList<Option> options = curve.getOptions();
			
			for (int j = 0; j < options.size(); j++) {
				
				Option option = options.get(j);
				writer.println(option.getStrikePrice() + ","
						+ option.get("tau") + "," + option.getOptionPrice()
						+ "," + option.getBSImpVol());
				System.out.println(option.getStrikePrice() + ","
						+ option.get("tau") + "," + option.getOptionPrice()
						+ "," + option.getBSImpVol());
				
			}
			
		}
		
		writer.close();
		
	}

	private void printInfo(String fileName, double[] strikes, double[] maturities,
			double[][] impVols) throws FileNotFoundException {
		
		File file = new File(fileName);
		PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
		
		for (int i = 0; i < maturities.length; i++) {
			
			for (int j = 0; j < strikes.length; j++) {
				
				writer.println(strikes[j] + "," + maturities[i] + ","
						+ impVols[j][i]); 
				
			}
			
		}
		
		writer.close();
		
	}
	
	public void setExtrapolationRange(double leftEnd, double rightEnd) {

		for (int i = 0; i < curves.size(); i++) {
			curves.get(i).setExtrapolationRange(leftEnd, rightEnd);
		}

	}

	public void setStrikePriceGap(double gap) {

		for (int i = 0; i < curves.size(); i++) {
			curves.get(i).setStrikePriceGap(gap);
		}

	}

	private void writeLog(String content) throws FileNotFoundException {

		Calendar currentTime = Calendar.getInstance();
		currentTime.getTime();
		SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		String log = "[" + timeFormat.format(currentTime.getTime()) + 
				"] (options/OptionSurface) " + content;

	}

	public void trim(double[][] trimmingLocations) {
		
		for (int i = 0; i < curves.size(); i++) {
			curves.get(i).trim(trimmingLocations[i]);
		}
		
	}

}
