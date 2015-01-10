package kr.geul.options.modelerrorcalculator;
import java.util.ArrayList;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.ISAACRandom;
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentComponentException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.modelerrorcalculator.CMAES;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.Option;
import kr.geul.options.parameterset.ParameterSet;
import kr.geul.options.pricer.Pricer;
import kr.geul.options.structure.OptionCurve;
import kr.geul.options.structure.OptionSurface;

public abstract class ModelErrorCalculator {

	protected String[] paramNames;
	protected double[] paramLowerBounds, paramUpperBounds, paramScales, paramSigmas;
	protected ParameterSet paramSet;
	protected Pricer pricer;
	protected OptionCurve curve;
	protected OptionSurface surface;
	protected String modelType;

	protected ModelErrorCalculator(){}
	
	protected abstract Option getModelOption(Option option, double[] param) 
			throws InvalidArgumentException, InconsistentArgumentLengthException, AtTheMoneyException;

	public double[] optimize() throws InvalidArgumentException, InconsistentArgumentLengthException {
		
		switch (modelType) {
		
		case "BS":
			return optimizePowell();
			
		default:
			return optimizePowell();
		
		}
		
	}
	
	public double[] optimizeBOBYQA() throws InvalidArgumentException, 
	InconsistentArgumentLengthException {	
		
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(paramNames.length + 2, 10, 0.00001);
		NelderMeadSimplex simplex = new NelderMeadSimplex(paramNames.length);
		LeastSquaresFunction func = new LeastSquaresFunction(); 
		SimpleBounds bounds = new SimpleBounds(paramLowerBounds, paramUpperBounds);
		ObjectiveFunction objFunc = new ObjectiveFunction(func);
		double[] initialValues = paramSet.getInitialValues();
		InitialGuess initialGuess = new InitialGuess(initialValues);
		MaxEval maxEval = new MaxEval(10000000);
		MaxIter maxIter = new MaxIter(10000000);
		PointValuePair pair = optimizer.optimize(simplex, objFunc, initialGuess, maxEval, maxIter, 
				GoalType.MINIMIZE, bounds);
			
		double[] results = pair.getPoint();
		double sse = optimizer.computeObjectiveValue(results);
		
		double[] allValues = new double[results.length + 1]; 
		
		for (int i = 0; i < results.length; i++) {
			allValues[i] = results[i];
		}
		
		allValues[allValues.length - 1] = sse;
		
		return allValues;
		
	}

	public double[] optimizeMultiBOBYQA(int amount) throws InvalidArgumentException, 
	InconsistentArgumentLengthException {	
		
		ISAACRandom randomGenerator = new ISAACRandom();
		UniformRandomGenerator uniformRandomGenerator = 
				new UniformRandomGenerator(randomGenerator);
		UncorrelatedRandomVectorGenerator randomVectorGenerator = 
				new UncorrelatedRandomVectorGenerator(amount, uniformRandomGenerator);
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(paramNames.length + 2);
		NelderMeadSimplex simplex = new NelderMeadSimplex(paramNames.length);
		LeastSquaresFunction func = new LeastSquaresFunction(); 
		SimpleBounds bounds = new SimpleBounds(paramLowerBounds, paramUpperBounds);
		ObjectiveFunction objFunc = new ObjectiveFunction(func);
		double[] initialValues = paramSet.getInitialValues();
		InitialGuess initialGuess = new InitialGuess(initialValues);
		MaxEval maxEval = new MaxEval(10000000);
		MaxIter maxIter = new MaxIter(10000000);
		MultiStartMultivariateOptimizer multiOptimizer =
				new MultiStartMultivariateOptimizer(optimizer, amount, randomVectorGenerator);
		PointValuePair pair = multiOptimizer.optimize(simplex, objFunc, initialGuess, 
				maxEval, maxIter, GoalType.MINIMIZE, bounds);
		
		double[] results = pair.getPoint();
		double sse = optimizer.computeObjectiveValue(results);
		
		double[] allValues = new double[results.length + 1]; 
		
		for (int i = 0; i < results.length; i++) {
			allValues[i] = results[i];
		}
		
		allValues[allValues.length - 1] = sse;
		
		return allValues;
		
	}
	
	public double[] optimizeCMAES(int maxIteration, int populationMultiplier, 
			double relativePrecision, double absolutePrecision) throws InvalidArgumentException, 
	InconsistentArgumentLengthException {	
		
		SimpleValueChecker checker = new SimpleValueChecker (relativePrecision, absolutePrecision);
		ISAACRandom randomGenerator = new ISAACRandom();
		CMAES optimizer = 
				new CMAES(maxIteration, 0.0, true, 1, 1, randomGenerator, 
				false, checker, modelType);
		CMAES.PopulationSize populationSize = 
				new CMAES.PopulationSize(populationMultiplier * 
						(int) (4 + Math.floor(Math.log(paramNames.length) * 3)));
		
		CMAES.Sigma sigmas = new CMAES.Sigma(paramSigmas); 
		LeastSquaresFunction func = new LeastSquaresFunction(); 
		SimpleBounds bounds = new SimpleBounds(paramLowerBounds, paramUpperBounds);
		ObjectiveFunction objFunc = new ObjectiveFunction(func);
		double[] initialValues = paramSet.getInitialValues();
		
		InitialGuess initialGuess = new InitialGuess(initialValues);
		MaxEval maxEval = new MaxEval(10000000);
		MaxIter maxIter = new MaxIter(10000000);
		
		PointValuePair pair = optimizer.optimize(objFunc, initialGuess, maxEval, maxIter, 
				GoalType.MINIMIZE, bounds, populationSize, sigmas);
		
		double[] results = pair.getPoint();
		double sse = optimizer.computeObjectiveValue(results);
		
		double[] allValues = new double[results.length + 1]; 
		
		for (int i = 0; i < results.length; i++) {
			allValues[i] = results[i];
		}
		
		allValues[allValues.length - 1] = sse;
		
		return allValues;
		
	}
	
	public double[] optimizePowell() throws InvalidArgumentException, 
	InconsistentArgumentLengthException {	
		
		SimpleValueChecker checker = new SimpleValueChecker (0.000001, 0.000001);
		PowellOptimizer optimizer = new PowellOptimizer(0.000001, 0.000001, checker);
		NelderMeadSimplex simplex = new NelderMeadSimplex(paramNames.length);
		LeastSquaresFunction func = new LeastSquaresFunction(); 
		MultivariateFunctionPenaltyAdapter boundedFunc = 
				new MultivariateFunctionPenaltyAdapter(func, paramLowerBounds,
				paramUpperBounds, 100000, paramScales);
		ObjectiveFunction objFunc = new ObjectiveFunction(boundedFunc);
		double[] initialValues = paramSet.getInitialValues();
		InitialGuess initialGuess = new InitialGuess(initialValues);
		MaxEval maxEval = new MaxEval(10000000);
		MaxIter maxIter = new MaxIter(10000000);
		PointValuePair pair = optimizer.optimize(simplex, objFunc, initialGuess, maxEval, maxIter, 
				GoalType.MINIMIZE);
			
		return pair.getPoint();
		
	}
	
	public double[] optimizeSimplex() throws InvalidArgumentException, 
	InconsistentArgumentLengthException {	
		
		SimpleValueChecker checker = new SimpleValueChecker (0.000001, 0.000001);
		SimplexOptimizer optimizer = new SimplexOptimizer(checker);
		NelderMeadSimplex simplex = new NelderMeadSimplex(paramNames.length);
		LeastSquaresFunction func = new LeastSquaresFunction(); 
		MultivariateFunctionPenaltyAdapter boundedFunc = 
				new MultivariateFunctionPenaltyAdapter(func, paramLowerBounds,
				paramUpperBounds, 100000, paramScales);
		ObjectiveFunction objFunc = new ObjectiveFunction(boundedFunc);
		double[] initialValues = paramSet.getInitialValues();
		InitialGuess initialGuess = new InitialGuess(initialValues);
		MaxEval maxEval = new MaxEval(10000000);
		MaxIter maxIter = new MaxIter(10000000);
		PointValuePair pair = optimizer.optimize(simplex, objFunc, initialGuess, maxEval, maxIter, 
				GoalType.MINIMIZE);
			
		return pair.getPoint();
		
	}
	
	public void setOptimizerSigma(double[] sigma) {
		paramSigmas = sigma;
	}
	
	public abstract void setParameterSet(ParameterSet paramSet) 
			throws InconsistentComponentException; 

	public void setSurface(OptionSurface surface) {
		this.surface = surface;
	}

	class LeastSquaresFunction implements MultivariateFunction {

		ArrayList<OptionCurve> curves;
		ArrayList<Option> allOptions;
		int optionAmount;
		double[] cArray;
		
		public LeastSquaresFunction() throws InvalidArgumentException, 
		InconsistentArgumentLengthException {
			
			curves = surface.getCurves();
			allOptions = new ArrayList<Option>();
			
			for (int i = 0; i < curves.size(); i++) {

				OptionCurve curve = curves.get(i);
				ArrayList<Option> options = curve.getOptions();

				for (int j = 0; j < options.size(); j++) {
					allOptions.add(options.get(j));
				}

			}
		
			optionAmount = allOptions.size();
			cArray = new double[optionAmount];
			
		}
				
		public double value(double[] arg0) {
			
			double totalSquaredError = 0.0;
			double actualPrice, modelPrice;
			
			for (int i = 0; i < allOptions.size(); i++) {
				
				Option option = allOptions.get(i);
				Option modelOption = new CallOption();
				
				try {
					modelOption = getModelOption(option, arg0);
				} 
				catch (InvalidArgumentException | AtTheMoneyException
						| InconsistentArgumentLengthException e) {
					e.printStackTrace();
				} 

				actualPrice = option.getVariableArray()[5];
				modelPrice = modelOption.getVariableArray()[5];
				
				totalSquaredError += Math.pow(actualPrice - modelPrice, 2);
				
			}
			
			String params = "";
			
			for (int i = 0; i < arg0.length; i++) {
				params += arg0[i];
					if (i < arg0.length - 1)
						params += ",";
			}
						
			System.out.println(params + "," + totalSquaredError);
			
			return totalSquaredError;
			
		}
		
	}
	
}
