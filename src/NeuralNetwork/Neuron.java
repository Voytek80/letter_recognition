package NeuralNetwork;

import java.util.Random;

class Neuron {
	/**
	 * Determines section of initial weights, for e.g. 1.0 means from -1.0 to 1.0
	 */
	private double randomCoefficient = 1.0;
	protected double output;
	protected double [] inputs;
	protected double [] weights;
	protected double sigma;
	protected int inputSignals;
	protected double [] corrections;
	private ActivationFunction activationFunc;

	public Neuron(ActivationFunction function) {
		activationFunc = function;
		inputSignals = 0;
		weights = null;
	}
	public Neuron(ActivationFunction function, int liczba_wejsc) {
		activationFunc = function;
		this.inputSignals = liczba_wejsc;
		weights = new double[liczba_wejsc+1];
		corrections = new double [liczba_wejsc + 1];
		initializeWeights();
	}

	private void initializeWeights() {
		Random r = new Random();
		for(int i = 0; i <= inputSignals; i++) {
			weights[i] = (r.nextDouble()-0.5) * 2.0 * randomCoefficient;
		}
	}

	protected double getOutput(double [] inputs) {
		double fi = 0.0;
		this.inputs = new double [inputs.length];
		System.arraycopy(inputs, 0, this.inputs, 0, inputs.length);
		
		for(int i = 1; i <= inputSignals; i++) {	
			fi += weights[i] * inputs[i-1];
		}
		
		double result = activationFunc.getOutput(fi);
		output = result;
		return result;
	}

	public void calcCorrections(double eta) {
		double derivate = activationFunc.getDerivativeOutput(output);
		for(int i = 1; i <= inputSignals; i++) {
			corrections[i] += eta * sigma * derivate * inputs[i-1];
		}
	}
	
	public void updateCorrections() {
		for(int i = 1; i <= inputSignals; i++) {
			weights[i] += corrections[i];
			corrections[i] = 0.0;
		}
	}
}
