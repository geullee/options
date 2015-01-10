package kr.geul.options.pricer;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class SVJPricer extends Pricer {

	private final double pi = Math.PI;
	private double underlyingPrice, strikePrice, timeToMaturity, 
	riskFreeRate, dividendRate, kappaV, thetaV, v0, sigmaV, muJ, sigmaJ, rho, lambda;

	private double exp(double n) {
		return Math.exp(n);
	} 

	private double SVJP(double s0, double K, double rfr, double T, double v0, 
			double kappaV, double thetaV, double sigmaV, double muJ, double sigmaJ,
			double rho, double lambda, int type) throws TooManyEvaluationsException {

		SVJPIntegrand SVJPIntegrand = new SVJPIntegrand(s0, K, rfr, T, v0,
				kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, type);

		IterativeLegendreGaussIntegrator integrator = 
				new IterativeLegendreGaussIntegrator(10, Math.pow(10, -12), Math.pow(10, -12));

//		double p = 0.5 + (1 / pi) * integrator.integrate(2000000, SVJPIntegrand, 0, 1000);

		double p;
		try {
			p = 0.5 + (1 / pi) * integrator.integrate(1000000, SVJPIntegrand, 0, 1000);
		}
		
		catch (TooManyEvaluationsException e) {
			throw new TooManyEvaluationsException(1);
		}
		
		return p;

	}

	public double priceCall(double[] variableArray, double[] parameterArray) 
			throws TooManyEvaluationsException {

		setVariables(variableArray, parameterArray);

		double callPrice = 0;

//		callPrice = underlyingPrice * exp(-dividendRate * timeToMaturity)
//				* SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
//					v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 1)
//					- strikePrice * exp(-riskFreeRate * timeToMaturity)
//				* SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
//					v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 2);
		try {
		
			callPrice = underlyingPrice * exp(-dividendRate * timeToMaturity)
					* SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 1)
						- strikePrice * exp(-riskFreeRate * timeToMaturity)
					* SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 2);
			
		}
		
		catch (TooManyEvaluationsException t) {
			throw new TooManyEvaluationsException(1);
		}
		
		return Math.abs(callPrice);

	}

	public double pricePut(double[] variableArray, double[] parameterArray) 
			throws TooManyEvaluationsException {

		double call, putPrice;
		
		try {
			
			call = priceCall(variableArray, parameterArray);
			putPrice = call + strikePrice * exp(-riskFreeRate * timeToMaturity) - 
				underlyingPrice * exp(-dividendRate * timeToMaturity);
			
		}
		
		catch (TooManyEvaluationsException t) {
			throw new TooManyEvaluationsException(1);
		}

		return Math.abs(putPrice);

	}

	protected void setVariables(double[] variableArray,
			double[] parameterArray) {

		underlyingPrice = variableArray[0];
		strikePrice = variableArray[1];
		timeToMaturity = variableArray[2];
		riskFreeRate = variableArray[3];
		dividendRate = variableArray[4];
		kappaV = parameterArray[0];
		thetaV = parameterArray[1];
		v0 = parameterArray[2];
		sigmaV = parameterArray[3];
		muJ = parameterArray[4];
		sigmaJ = parameterArray[5];
		rho = parameterArray[6];
		lambda = parameterArray[7];

	}

	private Complex getSVJF(double s0, double rfr, double tau, double v0, 
			double kappaV, double thetaV, double sigmaV, double muJ, double sigmaJ, 
			double rho, double lambda, double u, int type) {

		if (type == 1) {

			Complex i = Complex.I;
			Complex minusI = i.multiply(-1);

			return getSVJf(s0, rfr, tau, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda,
					minusI.add(u)).divide(getSVJf(s0, rfr, tau, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda,
							minusI));

		}

		else {

			Complex i = Complex.I;
			Complex iPhi = i.multiply(u);
			Complex rsiph = iPhi.multiply(rho * sigmaV);

			Double x = Math.log(s0);

			Complex d2_1 = rsiph.subtract(kappaV).pow(2);
			Complex d2_2 = iPhi.add(Math.pow(u, 2)).multiply(Math.pow(sigmaV, 2));
			Complex d2 = d2_1.add(d2_2);
			Complex d = d2.sqrt();

			Complex gamma1 = rsiph.multiply(-1).add(kappaV).subtract(d);
			Complex gamma2 = rsiph.multiply(-1).add(kappaV).add(d);
			Complex g = gamma1.divide(gamma2);

			Complex C = iPhi.multiply(x + rfr * tau);

			double D1 = thetaV * (Math.pow(sigmaV, -2));
			Complex D2 = d.multiply(-1).multiply(tau).exp().multiply(g);
			Complex D3 = rsiph.multiply(-1).add(kappaV).multiply(d.pow(-1)).subtract(1); 
			Complex D4 = D2.divide(g.multiply(-1).add(1)).multiply(-1).add(1).add(D3.multiply(0.5));

			Complex D = gamma1.multiply(tau).subtract(D4.log().multiply(2)).multiply(D1);

			Complex E1 = gamma1.multiply(v0 * Math.pow(sigmaV, -2));
			Complex E2 = d.multiply(-tau).exp().multiply(-1).add(1);
			Complex E3 = d.multiply(-tau).exp().multiply(g).multiply(-1).add(1);

			Complex E = E1.multiply(E2).divide(E3);

			double F1 = lambda * tau;
			Complex F2 = Complex.valueOf(1 + muJ).pow(iPhi);
			Complex F3 = iPhi.divide(2).multiply(iPhi.subtract(1)).multiply(Math.pow(sigmaJ, 2));

			Complex F = F2.multiply(F3.exp()).subtract(1).multiply(F1);

			Complex G = iPhi.multiply(-lambda * muJ * tau);

			return C.add(D).add(E).add(F).add(G).exp();

		}

	}

	private Complex getSVJf(double s0, double rfr, double tau,
			double v0, double kappaV, double thetaV, double sigmaV,
			double muJ, double sigmaJ, double rho, double lambda,
			Complex u) {

		Complex i = Complex.I;
		Complex iPhi = i.multiply(u);
		Complex rsiph = iPhi.multiply(rho * sigmaV);

		Double x = Math.log(s0);
		Complex d2 = 
				rsiph.subtract(kappaV).pow(2).add(iPhi.add(u.pow(2)).multiply(Math.pow(sigmaV, 2)));
		Complex d = d2.sqrt();
		Complex gamma1 = rsiph.multiply(-1).add(kappaV).subtract(d);
		Complex gamma2 = rsiph.multiply(-1).add(kappaV).add(d);
		Complex g = gamma1.divide(gamma2);

		Complex C = iPhi.multiply(x + rfr * tau);

		double D1 = thetaV * (Math.pow(sigmaV, -2));
		Complex D2 = d.multiply(-1).multiply(tau).exp().multiply(g);
		Complex D3 = rsiph.multiply(-1).add(kappaV).multiply(d.pow(-1)).subtract(1); 
		Complex D4 = D2.divide(g.multiply(-1).add(1)).multiply(-1).add(1).add(D3.multiply(0.5));

		Complex D = gamma1.multiply(tau).subtract(D4.log().multiply(2)).multiply(D1);

		Complex E1 = gamma1.multiply(v0 * Math.pow(sigmaV, -2));
		Complex E2 = d.multiply(-tau).exp().multiply(-1).add(1);
		Complex E3 = d.multiply(-tau).exp().multiply(g).multiply(-1).add(1);

		Complex E = E1.multiply(E2).divide(E3);

		double F1 = lambda * tau;
		Complex F2 = Complex.valueOf(1 + muJ).pow(iPhi);
		Complex F3 = iPhi.divide(2).multiply(iPhi.subtract(1)).multiply(Math.pow(sigmaJ, 2));

		Complex F = F2.multiply(F3.exp()).subtract(1).multiply(F1);

		Complex G = iPhi.multiply(-lambda * muJ * tau);

		return C.add(D).add(E).add(F).add(G).exp();

	}

	class SVJPIntegrand implements UnivariateFunction {

		double s0, K, rfr, T, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda;
		int type;

		public SVJPIntegrand(double underlyingPrice, double strikePrice, 
				double riskFreeRate, double timeToMaturity, double hestonV0, double hestonKappa, 
				double hestonTheta, double hestonSigma, double SVJMuJ, double SVJSigmaJ,
				double hestonRho, double hestonLambda, int integrandType) {

			s0 = underlyingPrice;
			K = strikePrice;
			rfr = riskFreeRate;
			T = timeToMaturity;
			v0 = hestonV0;
			kappaV = hestonKappa;
			thetaV = hestonTheta;
			sigmaV = hestonSigma;
			muJ = SVJMuJ;
			sigmaJ = SVJSigmaJ;
			rho = hestonRho;
			lambda = hestonLambda;
			type = integrandType;

		}

		public double value(double u) {

			Complex i = Complex.I;
			Complex minusI = i.multiply(-1);

			Complex numeratorMultiplier = minusI.multiply(u).multiply(Math.log(K)).exp();
			Complex denominator = i.multiply(u);

			Complex hestF = 
					getSVJF(s0, rfr, T, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, u, type);

			return numeratorMultiplier.multiply(hestF).divide(denominator).getReal(); 

		}	

	}

}
