package NeuralNetwork;

public interface ActivationFunction {
	public double getOutput(double input);
	
	public double getDerivativeOutput(double input);
}
