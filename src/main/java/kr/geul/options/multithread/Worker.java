package kr.geul.options.multithread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.option.Option;

public class Worker implements Callable<Double> {

	Object workClass;
	Method workMethod;
	double[] parameters;
	int workerID;
	Option option;
	String modelType;
	ThreadTerminal terminal;
	
	public synchronized Double call() throws Exception {
		
		Double result;
		
		try {
			result = (Double) workMethod.invoke(workClass, option, parameters, modelType);
		}
		
		catch (InvocationTargetException e) {
			throw new TooManyEvaluationsException(1);
		}
		
		terminal.notifyFinish(workerID);
		return result;
		
	}

	public void setInput(Option option) {
		this.option = option;
	}
	
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	public void setWork(Object workClass, Method workMethod) {
		this.workClass = workClass;
		this.workMethod = workMethod;		
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public void setTerminal(ThreadTerminal terminal) {
		this.terminal = terminal; 
	}
	
	public void setWorkerID(int workerID) {
		this.workerID = workerID;
	}

}
