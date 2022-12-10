package NeuralNetwork;

public class SigmoidFunction implements ActivationFunction {

	@Override
	public double getOutput(double input) {
		return 1.0 / (1.0 + Math.exp(-input));
	}

	@Override
	public double getDerivativeOutput(double input) {
		return input * (1 - input);
	}
}
