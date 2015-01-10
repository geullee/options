package kr.geul.options.multithread;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

import kr.geul.options.option.Option;

public class ThreadTerminal {

	boolean isWorkDone;
	boolean[] workStatus, workerStatus;
	Option[] options;
	double[] output;
	
	int nextWorkID;
	
	double[] parameters;
	String modelType;	
	
	Object workClass;
	ExecutorService executorService;
	Worker[] workers;
	Method workMethod;
	
	public ThreadTerminal(int workerPoolSize, int optionArraySize) {
		executorService = Executors.newFixedThreadPool(workerPoolSize);
		workerStatus = new boolean[workerPoolSize];
		workers = new Worker[workerPoolSize];
		options = new Option[optionArraySize];
		output = new double[optionArraySize];
		workStatus = new boolean[optionArraySize];
		isWorkDone = false;
	}
	
	public void setOption(Option option, int index) {
		options[index] = option;
	}
	
	public void setWork(Object object, Method workMethod) {
		this.workClass = object;
		this.workMethod = workMethod;
	}
	
	public double[] getOutput() throws InterruptedException, ExecutionException,
	TooManyEvaluationsException {
		
		work();
		return output;
		
	}
	
	private void work() throws InterruptedException, ExecutionException,
	TooManyEvaluationsException {
		
		nextWorkID = 0;
		
		for (int i = 0; i < workStatus.length; i++) {
			workStatus[i] = false;
		}
		
		for (int i = 0; i < workers.length; i++) {
			Worker worker = new Worker();
			worker.setModelType(modelType);
			worker.setParameters(parameters);
			worker.setWork(workClass, workMethod);
			worker.setWorkerID(i);
			worker.setTerminal(this);
			workers[i] = worker;

		}
		
		do {
			
			for (int workerID = 0; workerID < workerStatus.length; workerID++) {
				
				if (workerStatus[workerID] == false && nextWorkID != 99999999) {
					
					workerStatus[workerID] = true;
					workStatus[nextWorkID] = true;				
					workers[workerID].setInput(options[nextWorkID]);					
					nextWorkID = findNextWorkID(workStatus);
					
					if (nextWorkID == 99999999)
						isWorkDone = true;
					
					try {
						if (nextWorkID != 99999999)
							output[nextWorkID] = executorService.submit(workers[workerID]).get(); 
					}
					
					catch (ExecutionException e) {
						throw new TooManyEvaluationsException(1);
					}
					
				}
				
			}
			
		} while (isWorkDone == false);
		
		executorService.shutdown();
		
	}

	private int findNextWorkID(boolean[] workStatus) {
		
		for (int i = 0; i < workStatus.length; i++) {
			if (workStatus[i] == false) {
				return i;
			}
				
		}
		
		return 99999999;
		
	}


	public void notifyFinish(int workerID) {
		workerStatus[workerID] = false;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;		
	}
	
	public void setParameter(double[] parameters) {
		this.parameters = parameters;		
	}

}
