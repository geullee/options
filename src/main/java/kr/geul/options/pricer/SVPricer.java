package kr.geul.options.pricer;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
//import org.apache.commons.math3.analysis.integration.gauss.GaussIntegrator;
//import org.apache.commons.math3.analysis.integration.gauss.GaussIntegratorFactory;
import org.apache.commons.math3.complex.Complex;

public class SVPricer extends Pricer {

	static private final double pi = Math.PI;
	static private double underlyingPrice, strikePrice, timeToMaturity, 
	riskFreeRate, dividendRate, kappa, theta, v0, sigma, rho, lambda;

	private static double exp(double n) {
		return Math.exp(n);
	} 

	private static double hestonP1(double s0, double K, double T, double v0, 
			double kappa, double theta, double sigma, double rho, double lambda) {

		HestonPIntegrand1 hestonPIntegrand1 = new HestonPIntegrand1(s0, K, T, v0,
				kappa, theta, sigma, rho, lambda);

		//		GaussIntegratorFactory factory = new GaussIntegratorFactory();
		//		GaussIntegrator integrator = factory.legendre(100, 0, 1000);
		//
		//		double p1 = 0.5 + (1 / pi) * integrator.integrate(hestonPIntegrand1);

		IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(10, pow(10, -12), pow(10, -12));

		double p1 = 0.5 + (1 / pi) * integrator.integrate(50000000, hestonPIntegrand1, 0, 1000);

		return p1;

		//		return 0.5 + (1 / pi) * Integrator.integrate(hestonPIntegrand1, 0, 1000);

	}

	private static double hestonP2(double s0, double K, double T, double v0, 
			double kappa, double theta, double sigma, double rho, double lambda) {


		HestonPIntegrand2 hestonPIntegrand2 = new HestonPIntegrand2(s0, K, T, v0,
				kappa, theta, sigma, rho, lambda);

		//		GaussIntegratorFactory factory = new GaussIntegratorFactory();
		//		GaussIntegrator integrator = factory.legendre(100, 0, 1000);
		//		double p2 = 0.5 + (1 / pi) * integrator.integrate(hestonPIntegrand2);

		IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(10, pow(10, -12), pow(10, -12));

		double p2 = 0.5 + (1 / pi) * integrator.integrate(5000000, hestonPIntegrand2, 0, 1000);

		return p2;
		//		return 0.5 + (1 / pi) * Integrator.integrate(hestonPIntegrand2, 0, 1000);		

	}

	private static double log(double n) {
		return Math.log(n);
	} 

	private static double pow(double m, double n) {
		return Math.pow(m, n);
	} 

	public double priceCall(double[] variableArray, double[] parameterArray) {

		setVariables(variableArray, parameterArray);
		double callPrice = underlyingPrice * exp(-dividendRate * timeToMaturity)
				* hestonP1(underlyingPrice, strikePrice, timeToMaturity,
						v0, kappa, theta, sigma, rho, lambda)
						- strikePrice * exp(-riskFreeRate * timeToMaturity)
						* hestonP2(underlyingPrice, strikePrice, timeToMaturity,
								v0, kappa, theta, sigma, rho, lambda);
		return callPrice;

	}

	public double pricePut(double[] variableArray, double[] parameterArray) {

		double call = priceCall(variableArray, parameterArray);
		double putPrice = call + strikePrice * exp(-riskFreeRate * timeToMaturity) - 
				underlyingPrice * exp(-dividendRate * timeToMaturity);
		return putPrice;
	}

	protected void setVariables(double[] variableArray,
			double[] parameterArray) {

		underlyingPrice = variableArray[0];
		strikePrice = variableArray[1];
		timeToMaturity = variableArray[2];
		riskFreeRate = variableArray[3];
		dividendRate = variableArray[4];
		kappa = parameterArray[0];
		theta = parameterArray[1];
		v0 = parameterArray[2];
		sigma = parameterArray[3];
		rho = parameterArray[4];
		lambda = parameterArray[5];

	}

	private static Complex getHestF(double xt, double vt, double tau, double mu, double a, 
			double uj, double bj, double rho, double sig, double phi) {

		Complex i = Complex.I;
		Complex minusI = i.multiply(-1);

		Complex xj = minusI.multiply(rho * sig * phi).add(bj);
		Complex dj = xj.pow(2).
				subtract(i.multiply(2 * uj * phi).subtract(pow(phi, 2)).multiply(pow(sig, 2))).
				sqrt();
		Complex gj = xj.add(dj).divide(xj.subtract(dj));
		Complex D = xj.add(dj).divide(pow(sig, 2)).
				multiply(dj.multiply(tau).exp().multiply(-1).add(1)).
				divide(gj.multiply(dj.multiply(tau).exp()).multiply(-1).add(1));
		Complex xx = gj.multiply(dj.multiply(tau).exp()).multiply(-1).add(1).
				divide(gj.multiply(-1).add(1));
		Complex C = i.multiply(mu * phi * tau).add(xj.add(dj).multiply(tau).
				subtract(xx.log().multiply(2)).multiply(a / pow(sig, 2)));

		return C.add(D.multiply(vt)).add(i.multiply(phi * xt)).exp(); 

	}

	static class HestonPIntegrand1 implements UnivariateFunction {

		double s0, K, T, v0, kappa, theta, sigma, rho, lambda;

		public HestonPIntegrand1(double underlyingPrice, double strikePrice, 
				double timeToMaturity, double hestonV0,	double hestonKappa, 
				double hestonTheta, double hestonSigma, double hestonRho, double hestonLambda) {

			s0 = underlyingPrice;
			K = strikePrice;
			T = timeToMaturity;
			v0 = hestonV0;
			kappa = hestonKappa;
			theta = hestonTheta;
			sigma = hestonSigma;
			rho = hestonRho;
			lambda = hestonLambda;

		}

		public double value(double u) {

			Complex i = Complex.I;
			Complex minusI = i.multiply(-1);

			Complex numeratorMultiplier = minusI.multiply(u).multiply(log(K)).exp();
			Complex denominator = i.multiply(u);

			Complex hestF = getHestF(log(s0), v0, T, 0, kappa * theta, 0.5, 
					kappa + lambda - rho * sigma, rho, sigma, u);

			return numeratorMultiplier.multiply(hestF).divide(denominator).getReal(); 

		}	

	}

	static class HestonPIntegrand2 implements UnivariateFunction {

		double s0, K, T, v0, kappa, theta, sigma, rho, lambda;

		public HestonPIntegrand2(double underlyingPrice, double strikePrice, 
				double timeToMaturity, double hestonV0,	double hestonKappa, 
				double hestonTheta, double hestonSigma, double hestonRho, double hestonLambda) {

			s0 = underlyingPrice;
			K = strikePrice;
			T = timeToMaturity;
			v0 = hestonV0;
			kappa = hestonKappa;
			theta = hestonTheta;
			sigma = hestonSigma;
			rho = hestonRho;
			lambda = hestonLambda;

		}

		public double value(double u) {

			Complex i = Complex.I;
			Complex minusI = i.multiply(-1);

			Complex numeratorMultiplier = minusI.multiply(u).multiply(log(K)).exp();
			Complex denominator = i.multiply(u);

			Complex hestF = getHestF(log(s0), v0, T, 0, kappa * theta, -0.5, 
					kappa + lambda, rho, sigma, u);

			return numeratorMultiplier.multiply(hestF).divide(denominator).getReal(); 

		}	

	}

}
