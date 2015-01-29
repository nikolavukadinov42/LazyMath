//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:54
//

package sc.lazymath.ocr.neuralnetwork.backpropagation;

import java.io.Serializable;
import java.util.Random;

public class BackPropagation  implements Serializable {
	public static int MAX_NUM = 64;
	private int layersNum = 3;
	private int samplesNum = 4;
	private int maxIterations = 100000;
	private double maxError = 0.0005;
	private int attemptsNum = 100;

	public NeuralNetworkLayer[] layers = new NeuralNetworkLayer[this.layersNum];

	private double[][][] weights = new double[this.layersNum - 1][MAX_NUM][MAX_NUM];
	private double[][][] weightChanges = new double[this.layersNum - 1][MAX_NUM][MAX_NUM];

	private double[][][] trainingSet;

	private double ni;
	private double beta;

	private int outputNum = 17;

	public BackPropagation(int samplesNum, int outputNum, double[][][] trainingSet) {
		this.outputNum = outputNum;
		this.samplesNum = samplesNum;
		this.trainingSet = trainingSet;

		this.initialize();
	}

	private void initialize() {
		for (int i = 0; i < this.layersNum; i++) {
			this.layers[i] = new NeuralNetworkLayer();
		}
		this.ni = 0.05;
		this.beta = 0.5;
		this.layers[0].n = 64;
		this.layers[1].n = 35;
		this.layers[2].n = this.outputNum;
	}

	private void weightsInitialize() {
		Random rnd = new Random();
		for (int s = 0; s < this.layersNum - 1; s++) {
			for (int u = 0; u < this.layers[s].n; u++) {
				for (int v = 0; v < this.layers[s + 1].n; v++) {
					// [-0.1, 0.1]
					this.weights[s][u][v] = (float) (rnd.nextDouble() - 0.5) / 5;
					this.weightChanges[s][u][v] = 0;
				}
			}
		}

		for (int s = 0; s < this.layersNum; s++) {
			for (int u = 0; u < MAX_NUM; u++) {
				this.layers[s].bias[u] = 0;
				this.layers[s].biasp[u] = 0;
			}
		}

		for (int s = 1; s < this.layersNum - 1; s++) {
			for (int u = 0; u < this.layers[s].n; u++) {
				this.layers[s].bias[u] = (float) rnd.nextDouble();
			}
		}
	}

	// [0,1]
	private double sigmoid(double net) {
		return 1 / (1 + Math.exp(-net));
	}

	private double[] calculateOutput() {
		double net = 0.0;

		for (int s = 1; s < this.layersNum; s++) {
			for (int v = 0; v < this.layers[s].n; v++) {
				net = this.layers[s].bias[v];
				for (int u = 0; u < this.layers[s - 1].n; u++) {
					net += this.layers[s - 1].output[u] * this.weights[s - 1][u][v];
				}
				this.layers[s].output[v] = this.sigmoid(net);
			}
		}

		return this.layers[this.layersNum - 1].output;
	}

	private void setInput(int sample) {
		for (int u = 0; u < this.layers[0].n; u++) {
			this.layers[0].output[u] = this.trainingSet[sample][0][u];
		}
	}

	private double calculateErrors(int sample) {
		double erorrs = 0;

		for (int v = 0; v < this.layers[this.layersNum - 1].n; v++) {
			this.layers[this.layersNum - 1].delta[v] = this.trainingSet[sample][1][v]
					- this.layers[this.layersNum - 1].output[v];
			erorrs += (this.layers[this.layersNum - 1].delta[v])
					* (this.layers[this.layersNum - 1].delta[v]);
		}

		for (int s = this.layersNum - 2; s >= 0; s--) {
			for (int u = 0; u < this.layers[s].n; u++) {
				double sigmaa = 0.0;
				for (int v = 0; v < this.layers[s + 1].n; v++) {
					sigmaa += this.layers[s + 1].delta[v] * this.weights[s][u][v];
				}
				double f = this.layers[s].output[u];
				this.layers[s].delta[u] = f * (1 - f) * sigmaa;
			}
		}

		return erorrs;
	}

	private void adjustWeights() {
		for (int s = 0; s < this.layersNum - 1; s++) {
			for (int v = 0; v < this.layers[s + 1].n; v++) {
				for (int u = 0; u < this.layers[s].n; u++) {
					this.weightChanges[s][u][v] = this.ni * this.layers[s + 1].delta[v]
							* this.layers[s].output[u] + this.beta * this.weightChanges[s][u][v];
					this.weights[s][u][v] += this.weightChanges[s][u][v];
				}

				this.layers[s + 1].biasp[v] = this.ni * this.layers[s + 1].delta[v] + this.beta
						* this.layers[s + 1].biasp[v];
				this.layers[s + 1].bias[v] += this.layers[s + 1].biasp[v];

				double net = this.layers[s + 1].bias[v];

				for (int u = 0; u < this.layers[s].n; u++) {
					net += this.layers[s].output[u] * this.weights[s][u][v];
				}

				this.layers[s + 1].output[v] = this.sigmoid(net);
			}
		}
	}

	private double training() {
		double error = 0;

		for (int it = 0; it < this.maxIterations; it++) {
			error = 0;

			for (int sample = 0; sample < this.samplesNum; sample++) {
				this.setInput(sample);
				this.calculateOutput();
				error += 0.5 * this.calculateErrors(sample);
				this.adjustWeights();
			}

			if (error < this.maxError) {
				break;
			}

		}

		return error;
	}

	public void train() {
		for (int attempt = 0; attempt < this.attemptsNum; attempt++) {
			this.weightsInitialize();

			double error = this.training();

			if (error < this.maxError) {
				break;
			}

		}
	}

	public double[] calculate(double[] data) {
		for (int i = 0; i < data.length; i++) {
			this.layers[0].output[i] = data[i];
		}

		return this.calculateOutput();
	}

	public BackPropagationOutput calculateOutput(double[] data) {
		double[] d = this.calculate(data);
		double max = 0;
		int index = 0;

		for (int i = 0; i < this.outputNum; i++) {
			if (d[i] > max) {
				max = d[i];
				index = i;
			}

		}

		return new BackPropagationOutput(index, max);
	}

}
