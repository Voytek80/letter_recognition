package NeuralNetwork;

import java.util.Arrays;

import Diagnostics.Debugger;

public class Network {
	static final double etaEps = 0.999;
	protected Layer [] layers;
	private Dataset [] learningDataset;

	public Network(ActivationFunction function, int liczba_wejsc,int liczba_warstw,int [] lnww) {
		layers = new Layer[liczba_warstw];
		for(int i=0;i<liczba_warstw;i++) {
			int inputSignals = (i==0) ? liczba_wejsc : lnww[i-1];
			layers[i] = new Layer(function, inputSignals, lnww[i]);
		}
	}

	public void learn(Dataset [] learningDataset, int epochCount) {
		this.learningDataset = learningDataset;
		double eta = 0.1;
		double sigma = 0.0;
		for(int i = 0; i < epochCount; i++) {
			sigma = uruchomEpoke(eta);
			updateCorrections();
			Debugger.log("Błąd: " + String.format("%.9f", sigma));
		}
		eta *= etaEps;
	}

	public boolean testData(Dataset data, double treshold) {
		boolean ret = true;
		double [] results = getOutput(data.getData());
		for(int i = 0; i < results.length; i++) {
			int comp = (results[i] > treshold) ? 1 : 0;
			if(comp != (int)(data.getDesiredOutput()[i])) {
				ret = false;
				break;
			}
		}
		return ret;
	}
	
	public double [] getOutput(double [] inputs) {
		double [] output = null;
		for(int i = 0; i < layers.length; i++) {
			inputs = output = layers[i].getOutput(inputs);
		}
		return output;
	}

	protected double uruchomEpoke(double eta) {
		double [] sigma;
		double signalError = 0.0;
		for(int i = 0; i < learningDataset.length; i++) {
			double [] y = getOutput(learningDataset[i].getData());
			sigma = new double [y.length];
			for(int j = 0; j < sigma.length; j++) {
				sigma[j] = learningDataset[i].getDesiredOutput()[j] - y[j];
			}

			backpropagation(sigma);
			calcCorrections(eta);
			signalError += Math.pow(Arrays.stream(sigma).sum() / sigma.length, 2.0);
		}
		return signalError / learningDataset.length;
	}

	protected void backpropagation(double [] sigma) {
		int lastLayerId = layers.length - 1;
		for(int i = lastLayerId; i >= 0; i--) {
			for(int j = 0; j < layers[i].neurons.length; j++) {
				if(i == lastLayerId) {
					layers[i].neurons[j].sigma = sigma[j];
				} else {
					double localSigma = 0.0;
					for(int k = 0; k < layers[i+1].neurons.length; k++) {
						localSigma += layers[i+1].neurons[k].weights[j+1] * layers[i+1].neurons[k].sigma;
					}
					layers[i].neurons[j].sigma = localSigma;
				}
			}
		}
	}
	
	protected void calcCorrections(double eta) {
		for(int i = 0; i < layers.length; i++) {
			for(int j = 0; j < layers[i].neurons.length; j++) {
				layers[i].neurons[j].calcCorrections(eta);
			}
		}
		
	}
	
	protected void updateCorrections() {
		for(int i = 0; i < layers.length; i++) {
			for(int j = 0; j < layers[i].neurons.length; j++) {
				layers[i].neurons[j].updateCorrections();
			}
		}
	}
}
