package kr.geul.options.structure;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.DuplicateOptionsException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentOptionException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.OTMOption;

public class OTMOptionCurve extends OptionCurve {

	protected double extrapolationLeftEnd = -1, extrapolationRightEnd = -1, strikePriceGap = -1;
	
	public void extrapolate() throws InvalidArgumentException, 
	InconsistentArgumentLengthException, DuplicateOptionsException, InconsistentOptionException, AtTheMoneyException, TooManyEvaluationsException {
		
		if (extrapolationLeftEnd > -1 && extrapolationRightEnd > -1 && strikePriceGap > -1) {
			
			OTMOption leftEndOption = (OTMOption) options.get(0), 
					rightEndOption = (OTMOption) options.get(options.size() - 1);
			
			double leftEndStrikePrice = leftEndOption.getStrikePrice(),
					rightEndStrikePrice = rightEndOption.getStrikePrice(),
					leftEndBSImpVol = leftEndOption.getBSImpVol(), 
					rightEndBSImpVol = rightEndOption.getBSImpVol(),
					extrapolationLeftWidth = leftEndStrikePrice - extrapolationLeftEnd,
					extrapolationRightWidth = extrapolationRightEnd - rightEndStrikePrice;
			int numberOfGeneratedPuts = (int) Math.ceil(extrapolationLeftWidth / strikePriceGap),
					numberOfGeneratedCalls = (int) Math.ceil(extrapolationRightWidth / strikePriceGap);
			
			for (int i = 0; i < numberOfGeneratedPuts; i++) {

				double strike;
				OTMOption option = new OTMOption("BS");

				if (i < numberOfGeneratedPuts - 1)
					strike = leftEndStrikePrice - (strikePriceGap * i + 1);
				else 
					strike = extrapolationLeftEnd;

				double[] parameterValues = {underlyingPrice, strike, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, leftEndBSImpVol};
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				
				add(option);

			}

			for (int i = 0; i < numberOfGeneratedCalls; i++) {

				double strike;
				OTMOption option = new OTMOption("BS");

				if (i < numberOfGeneratedCalls - 1)
					strike = rightEndStrikePrice + (strikePriceGap * i + 1);
				else 
					strike = extrapolationRightEnd;

				double[] parameterValues = {underlyingPrice, strike, riskFreeRate, timeToMaturity,
						dividendRate, 0.0, rightEndBSImpVol};
				option.set(generatedOptionParameterNames, parameterValues);
				option.evaluate();
				
				add(option);

			}
			
		}

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

}
