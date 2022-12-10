package NeuralNetwork;

class Layer {
	protected Neuron [] neurons;
	
	public Layer() {
		neurons = null;
	}

	public Layer(ActivationFunction function, int inputSignals, int neuronsCount) {
		neurons = new Neuron[neuronsCount];
		for(int i = 0;i < neuronsCount; i++) {
			neurons[i] = new Neuron(function, inputSignals);
		}
	}

	double [] getOutput(double [] inputs){
		double [] output = new double[neurons.length];
		for(int i=0;i<neurons.length;i++)
			output[i]=neurons[i].getOutput(inputs);
		return output;
	}
}
