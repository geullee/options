package kr.geul.options.pricer;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.complex.Complex;

public class SVJPricer_Complex {

	static private final Complex i = Complex.I;
	static private double underlyingPrice, strikePrice, timeToMaturity, 
	riskFreeRate, dividendRate, kappaV, thetaV, v0, sigmaV, muJ, sigmaJ, rho, lambda;
	
	private static Complex getComplex(double d) {
		return Complex.valueOf(d);
	}

	private static Complex getSVJF1(double s, double rfr, double tau, double v0, 
			double kappaV, double thetaV, double sigmaV, double muJ, double sigmaJ, 
			double rho, double lambda, double ts, double u) {
		
		double B = Math.exp(rfr * tau * -1.0);
		Complex xi = getXi(kappaV, sigmaV, rho, u);
		Complex iu = i.multiply(u);
		
		Complex part1 = iu.negate().multiply(Math.log(B));
		
		Complex part2_1 = getComplex(ts);
		Complex part2_2_1_1_1 = iu.add(1.0).multiply(rho * sigmaV).
				add(xi).subtract(kappaV);
		Complex part2_2_1_1_2 = xi.multiply(tau).negate().exp().negate().add(1.0);
		Complex part2_2_1_1 = part2_2_1_1_1.multiply(part2_2_1_1_2);
		Complex part2_2_1_2 = xi.multiply(2.0);
		Complex part2_2_1 = part2_2_1_1.divide(part2_2_1_2);
		Complex part2_2 = part2_2_1.negate().add(1.0).log().multiply(2.0);
		Complex part2 = part2_1.multiply(part2_2).negate();
		
		Complex part3_1 = part2_1;
		Complex part3_2 = iu.add(1).multiply(sigmaV * rho).add(xi).
				subtract(kappaV).multiply(tau);
		Complex part3 = part3_1.multiply(part3_2).negate();
		
		Complex part4 = iu.multiply(Math.log(s));
		
		Complex part5_1 = getComplex(lambda * tau * (muJ + 1.0));
		Complex part5_2_1 = getComplex(muJ + 1.0).pow(iu);
		Complex part5_2_2_2 = iu.add(1.0);
		Complex part5_2_2_3 = getComplex(Math.pow(sigmaJ, 2.0));
		Complex part5_2_2_1 = iu.divide(2.0);
		Complex part5_2_2 = part5_2_2_1.multiply(part5_2_2_2).multiply(part5_2_2_3).exp();
		Complex part5_2 = part5_2_1.multiply(part5_2_2).subtract(1.0);
		Complex part5 = part5_1.multiply(part5_2);
		
		Complex part6 = iu.multiply(lambda).multiply(muJ).multiply(tau).negate();
		
		Complex part7_1_1 = iu;
		Complex part7_1_2 = iu.add(1.0);
		Complex part7_1_3 = xi.multiply(tau).negate().exp().negate().add(1.0);
		Complex part7_1 = part7_1_1.multiply(part7_1_2).multiply(part7_1_3);
		Complex part7_2_1 = xi.multiply(2.0);
		Complex part7_2_2_1 = iu.add(1.0).multiply(rho * sigmaV);
		Complex part7_2_2 = xi.subtract(kappaV).add(part7_2_2_1);
		Complex part7_2_3 = xi.multiply(tau).negate().exp().negate().add(1.0);
		Complex part7_2 = part7_2_1.subtract(part7_2_2.multiply(part7_2_3));
		Complex part7 = part7_1.divide(part7_2).multiply(v0);
		
		return part1.add(part2).add(part3).add(part4).add(part5).add(part6).add(part7).exp();
		
	}

	private static Complex getSVJF2(double s, double rfr, double tau, double v0, 
			double kappaV, double thetaV, double sigmaV, double muJ, double sigmaJ, 
			double rho, double lambda, double ts, double u) {
		
		double B = Math.exp(rfr * tau * -1.0);
		Complex xiStar = getXiStar(kappaV, sigmaV, rho, u);
		Complex iu = i.multiply(u);
		
		Complex part1 = iu.negate().multiply(Math.log(B));		
				
		Complex part2_1 = getComplex(ts);
		Complex part2_2_1_1_1 = iu.multiply(rho * sigmaV).add(xiStar).subtract(kappaV);
		Complex part2_2_1_1_2 = xiStar.multiply(tau).negate().exp().negate().add(1.0);
		Complex part2_2_1_1 = part2_2_1_1_1.multiply(part2_2_1_1_2);
		Complex part2_2_1_2 = xiStar.multiply(2.0);
		Complex part2_2_1 = part2_2_1_1.divide(part2_2_1_2);
		Complex part2_2 = part2_2_1.negate().add(1.0).log().multiply(2.0);
		Complex part2 = part2_1.multiply(part2_2).negate();

		Complex part3_1 = part2_1;
		Complex part3_2 = iu.multiply(rho * sigmaV).add(xiStar).subtract(kappaV).multiply(tau);
		Complex part3 = part3_1.multiply(part3_2).negate();
		
		Complex part4 = iu.multiply(Math.log(s));
		
		Complex part5_1 = getComplex(lambda * tau);
		Complex part5_2_1 = getComplex(muJ + 1.0).pow(i.multiply(u));
		Complex part5_2_2_1 = iu.divide(2.0);
		Complex part5_2_2_2 = iu.subtract(1.0);
		Complex part5_2_2_3 = getComplex(Math.pow(sigmaJ, 2.0));
		Complex part5_2_2 = part5_2_2_1.multiply(part5_2_2_2).multiply(part5_2_2_3).exp();
		Complex part5_2 = part5_2_1.multiply(part5_2_2).subtract(1.0);
		Complex part5 = part5_1.multiply(part5_2);
		
		Complex part6 = iu.multiply(lambda).multiply(muJ).multiply(tau).negate();
		
		Complex part7_1_1 = iu;
		Complex part7_1_2 = iu.subtract(1.0);
		Complex part7_1_3 = xiStar.multiply(tau).negate().exp().negate().add(1.0);
		Complex part7_1 = part7_1_1.multiply(part7_1_2).multiply(part7_1_3);
		Complex part7_2_1 = xiStar.multiply(2.0);
		Complex part7_2_2_1 = iu.multiply(rho * sigmaV);
		Complex part7_2_2 = xiStar.subtract(kappaV).add(part7_2_2_1);
		Complex part7_2_3 = xiStar.multiply(tau).negate().exp().negate().add(1.0);
		Complex part7_2 = part7_2_1.subtract(part7_2_2.multiply(part7_2_3));
		Complex part7 = part7_1.divide(part7_2).multiply(v0);
		
		return part1.add(part2).add(part3).add(part4).add(part5).add(part6).add(part7).exp();
		
	}

	private static Complex getXi(double kappaV, double sigmaV,
			double rho, double u) {
		
		Complex part1_1 = Complex.valueOf(kappaV);
		Complex part1_2 = Complex.valueOf(rho).multiply(sigmaV).multiply(i.multiply(u).add(1));
		Complex part1 = part1_1.subtract(part1_2).pow(2);
		
		Complex part2 = i.multiply(u).multiply(i.multiply(u).add(1)).
				multiply(Math.pow(sigmaV, 2));
		
		Complex xi = part1.subtract(part2).sqrt();
		
		return xi;
		
	}

	private static Complex getXiStar(double kappaV, double sigmaV,
			double rho, double u) {
		
		Complex part1_1 = Complex.valueOf(kappaV);
		Complex part1_2 = i.multiply(u).multiply(rho).multiply(sigmaV);
		Complex part1 = part1_1.subtract(part1_2).pow(2);
		
		Complex part2 = i.multiply(u).multiply(i.multiply(u).subtract(1)).
				multiply(Math.pow(sigmaV, 2));
		
		Complex xiStar = part1.subtract(part2).sqrt();
		
		return xiStar;
		
	}
	
	public double priceCall(double[] variableArray, double[] parameterArray) {

		setVariables(variableArray, parameterArray);
		double sPart = underlyingPrice * Math.exp(dividendRate * timeToMaturity * -1.0) *
				SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 1);
		double kPart = strikePrice * Math.exp(riskFreeRate * timeToMaturity * -1.0) *
				SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 2);
		double callPrice = sPart - kPart;
		
		return callPrice;
		
	}

	public double pricePut(double[] variableArray, double[] parameterArray) {

		setVariables(variableArray, parameterArray);
		setVariables(variableArray, parameterArray);
		double sPart = underlyingPrice * Math.exp(dividendRate * timeToMaturity * -1.0) *
				SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 1);
		double kPart = strikePrice * Math.exp(riskFreeRate * timeToMaturity * -1.0) *
				SVJP(underlyingPrice, strikePrice, riskFreeRate, timeToMaturity,
						v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, 2);
		double callPrice = sPart - kPart;
		double putPrice = callPrice + strikePrice * Math.exp(riskFreeRate * timeToMaturity * -1.0)
				- underlyingPrice;
		
		return putPrice;
		
	}
	
	private static double pow(double r, double n) {
		return Math.pow(r, n);
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
	
	private static double SVJP(double S, double K, double rfr, double tau, double v0, 
			double kappaV, double thetaV, double sigmaV, double muJ, double sigmaJ,
			double rho, double lambda, int type) {

		SVJPIntegrand SVJPIntegrand = new SVJPIntegrand(S, K, rfr, tau, v0,
				kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, type);

		IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(10, pow(10, -15), pow(10, -15));

		double integral = integrator.integrate(2000000, SVJPIntegrand, 0, 10000);
		double p = 0.5 + (1.0 / Math.PI * integral);  
		
		return p;

	}
	
	static class SVJPIntegrand implements UnivariateFunction {

		double B, S, K, rfr, tau, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda,
			ts;
		int type;

		public SVJPIntegrand(double S, double K, 
				double rfr, double tau, double v0, double kappaV, 
				double thetaV, double sigmaV, double muJ, double sigmaJ,
				double rho, double lambda, int type) {

			this.S = S;
			this.K = K;
			this.rfr = rfr;
			this.tau = tau;
			this.v0 = v0;
			this.kappaV = kappaV;
			this.thetaV = thetaV;
			this.sigmaV = sigmaV;
			this.muJ = muJ;
			this.sigmaJ = sigmaJ;
			this.rho = rho;
			this.lambda = lambda;
			this.type = type;
			B = Math.exp(rfr * tau * -1.0);
			ts = thetaV / Math.pow(sigmaV, 2);
			
		}

		public double value(double u) {
			
			Complex numeratorMultiplier = 
					i.multiply(u).multiply(Math.log(K)).negate().exp();
			Complex denominator = i.multiply(u);
			Complex SVJF;
			
			if (type == 1)
				SVJF = 
					getSVJF1(S, rfr, tau, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, ts, u);
			else
				SVJF = 
					getSVJF2(S, rfr, tau, v0, kappaV, thetaV, sigmaV, muJ, sigmaJ, rho, lambda, ts, u);

			return numeratorMultiplier.multiply(SVJF).divide(denominator).getReal(); 

		}	

	}
	
}
