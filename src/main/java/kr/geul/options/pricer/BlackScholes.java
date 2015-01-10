package kr.geul.options.pricer;

import org.apfloat.ApcomplexMath;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class BlackScholes {

    public static double getCallPrice(double S, double K, double r, double sigma, double tau) {
        double d1 = (Math.log(S / K) + (r + sigma * sigma / 2.0) * tau) / (sigma * Math.sqrt(tau));
        double d2 = d1 - sigma * Math.sqrt(tau);
        return S * Gaussian.Phi(d1) - K * Math.exp(-r * tau) * Gaussian.Phi(d2);
    }
	
    public static double getPutPrice(double S, double K, double r, double sigma, double tau) {
        double d1 = (Math.log(S / K) + (r + sigma * sigma / 2.0) * tau) / (sigma * Math.sqrt(tau));
        double d2 = d1 - sigma * Math.sqrt(tau);
        return K * Math.exp(-r * tau) * Gaussian.Phi(-d2) - S * Gaussian.Phi(-d1) ;
    }
    
    public static double getCallPriceWithDividend(double SDouble, double KDouble, double rDouble, 
			double dividendDouble, double sigmaDouble, double tauDouble) {
		
    	Apfloat S = new Apfloat(SDouble), K = new Apfloat(KDouble),
				r = new Apfloat(rDouble), dividend = new Apfloat(dividendDouble),
				sigma = new Apfloat(sigmaDouble), tau = new Apfloat(tauDouble);
		Apfloat d1 = ApfloatMath.log(S.divide(K)).
				add(r.subtract(dividend).add(ApcomplexMath.pow(sigma, new Apfloat(2.0)).real().
				divide(new Apfloat(2.0))).multiply(tau)).divide(sigma.multiply(ApfloatMath.sqrt(tau)));
        Apfloat d2 = d1.subtract(sigma.multiply(ApfloatMath.sqrt(tau)));
        return S.multiply(ApfloatMath.exp(dividend.negate().multiply(tau))) 
        		.multiply(Gaussian.Phi(d1)).
        		subtract(K.multiply(ApfloatMath.exp(r.negate().multiply(tau))).
        		multiply(Gaussian.Phi(d2))).doubleValue();
        
    }
	
    public static double getPutPriceWithDividend(double SDouble, double KDouble, double rDouble, 
			double dividendDouble, double sigmaDouble, double tauDouble) {
	
    	Apfloat S = new Apfloat(SDouble), K = new Apfloat(KDouble),
				r = new Apfloat(rDouble), dividend = new Apfloat(dividendDouble),
				sigma = new Apfloat(sigmaDouble), tau = new Apfloat(tauDouble);
		Apfloat d1 = ApfloatMath.log(S.divide(K)).
				add(r.subtract(dividend).add(ApfloatMath.pow(sigma, new Apfloat(2.0)).
				divide(new Apfloat(2.0))).multiply(tau)).divide(sigma.multiply(ApfloatMath.sqrt(tau)));
		Apfloat d2 = d1.subtract(sigma.multiply(ApfloatMath.sqrt(tau)));
		
        return K.multiply(ApfloatMath.exp(r.negate().multiply(tau))).
        		multiply(Gaussian.Phi(d2.negate())).
        		subtract(S.multiply(ApfloatMath.exp(dividend.negate().multiply(tau))) 
        		.multiply(Gaussian.Phi(d1.negate()))).doubleValue();
		
    }

	public static Apfloat getCallPriceWithDividend(double SDouble, double KDouble, double rDouble, 
			double dividendDouble, Apfloat sigma, double tauDouble) {
		
		Apfloat S = new Apfloat(SDouble), K = new Apfloat(KDouble),
				r = new Apfloat(rDouble), dividend = new Apfloat(dividendDouble),
				tau = new Apfloat(tauDouble);
		Apfloat d1 = ApfloatMath.log(S.divide(K)).
				add(r.subtract(dividend).add(ApfloatMath.pow(sigma, new Apfloat(2.0)).
				divide(new Apfloat(2.0))).multiply(tau)).divide(sigma.multiply(ApfloatMath.sqrt(tau)));
        Apfloat d2 = d1.subtract(sigma.multiply(ApfloatMath.sqrt(tau)));
        return S.multiply(ApfloatMath.exp(dividend.negate().multiply(tau))) 
        		.multiply(Gaussian.Phi(d1)).
        		subtract(K.multiply(ApfloatMath.exp(r.negate().multiply(tau))).
        		multiply(Gaussian.Phi(d2)));
        
	}

	public static Apfloat getPutPriceWithDividend(double SDouble, double KDouble, double rDouble, 
			double dividendDouble, Apfloat sigma, double tauDouble) {
		
		Apfloat S = new Apfloat(SDouble), K = new Apfloat(KDouble),
				r = new Apfloat(rDouble), dividend = new Apfloat(dividendDouble),
				tau = new Apfloat(tauDouble);
		Apfloat d1 = ApfloatMath.log(S.divide(K)).
				add(r.subtract(dividend).add(ApfloatMath.pow(sigma, new Apfloat(2.0)).
				divide(new Apfloat(2.0))).multiply(tau)).divide(sigma.multiply(ApfloatMath.sqrt(tau)));
		Apfloat d2 = d1.subtract(sigma.multiply(ApfloatMath.sqrt(tau)));
		
//		Apfloat KeMinusRT = K.multiply(ApfloatMath.exp(r.negate().multiply(tau)));
//		Apfloat NMinusD2 = Gaussian.Phi(d2.negate());
//		Apfloat SeMinusDT = S.multiply(ApfloatMath.exp(dividend.negate().multiply(tau)));
//		Apfloat NMinusD1 = Gaussian.Phi(d1.negate());
//		return KeMinusRT.multiply(NMinusD2).subtract(SeMinusDT.multiply(NMinusD1));
        
        return K.multiply(ApfloatMath.exp(r.negate().multiply(tau))).
        		multiply(Gaussian.Phi(d2.negate())).
        		subtract(S.multiply(ApfloatMath.exp(dividend.negate().multiply(tau))) 
        		.multiply(Gaussian.Phi(d1.negate())));
		
	}
    
}
