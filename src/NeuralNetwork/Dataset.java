package NeuralNetwork;

public class Dataset {
	private double [] data;
	private double [] desiredOutput;

	public Dataset(double [] data, double [] desiredOutput) {
		this.data = data;
		this.desiredOutput = desiredOutput;
	}
	
	public double [] getData() {
		return data;
	}
	
	public double [] getDesiredOutput() {
		return desiredOutput;
	}
}
