//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:54
//

package ftn.sc.lazymath.ocr.neuralnetwork;


import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackPropagation {
    public static int MAX_NUM = 64;
    public static int LAYERS_NUM = 3;
    public static int SAMPLES_NUM = 4;
    public static int MAX_ITERATIONS = 100000;
    // TODO greska nm
    public static double MAX_ERROR = 0.005;
    public static int ATTEMPTS_NUM = 100;

    public NeuralNetworkLayer[] layers = new NeuralNetworkLayer[LAYERS_NUM];

    public double[][][] weights = new double[LAYERS_NUM - 1][MAX_NUM][MAX_NUM];
    public double[][][] weightChanges = new double[LAYERS_NUM - 1][MAX_NUM][MAX_NUM];

    public double[][][] trainingSet;

    public double ni;
    public double beta;

    public BackPropagation(int samplesNum, double[][][] trainingSet) {
        SAMPLES_NUM = samplesNum;
        this.trainingSet = trainingSet;
        initialize();
    }

    protected void initialize() {
        for (int i = 0; i < LAYERS_NUM; i++)
            layers[i] = new NeuralNetworkLayer();
        ni = 0.05;
        beta = 0.5;
        layers[0].n = 64;
        layers[1].n = 35;
        layers[2].n = 18;
    }

    protected void weightsInitialize() {
        Random rnd = new Random();
        for (int s = 0; s < LAYERS_NUM - 1; s++) {
            for (int u = 0; u < layers[s].n; u++)
                for (int v = 0; v < layers[s + 1].n; v++) {
                    //[-0.1, 0.1]
                    weights[s][u][v] = (float) (rnd.nextDouble() - 0.5) / 5;
                    weightChanges[s][u][v] = 0;
                }
        }

        for (int s = 0; s < LAYERS_NUM; s++) {
            for (int u = 0; u < MAX_NUM; u++) {
                layers[s].bias[u] = 0;
                layers[s].biasp[u] = 0;
            }
        }

        for (int s = 1; s < LAYERS_NUM - 1; s++) {
            for (int u = 0; u < layers[s].n; u++) {
                layers[s].bias[u] = (float) rnd.nextDouble();
            }
        }
    }

    //[0,1]
    double sigmoid(double net) {
        return 1 / (1 + Math.exp(-net));
    }

    double[] calculateOutput() {
        double net = 0.0;

        for (int s = 1; s < LAYERS_NUM; s++) {
            for (int v = 0; v < layers[s].n; v++) {
                net = layers[s].bias[v];
                for (int u = 0; u < layers[s - 1].n; u++) {
                    net += layers[s - 1].output[u] * weights[s - 1][u][v];
                }
                layers[s].output[v] = sigmoid(net);
            }
        }

        return layers[LAYERS_NUM - 1].output;
    }

    void setInput(int sample) {
        for (int u = 0; u < layers[0].n; u++) {
            layers[0].output[u] = trainingSet[sample][0][u];
        }
    }

    double calculateErrors(int sample) {
        double erorrs = 0;

        for (int v = 0; v < layers[LAYERS_NUM - 1].n; v++) {
            layers[LAYERS_NUM - 1].delta[v] = trainingSet[sample][1][v] - layers[LAYERS_NUM - 1].output[v];
            erorrs += (layers[LAYERS_NUM - 1].delta[v]) * (layers[LAYERS_NUM - 1].delta[v]);
        }

        for (int s = LAYERS_NUM - 2; s >= 0; s--) {
            for (int u = 0; u < layers[s].n; u++) {
                double sigmaa = 0.0;
                for (int v = 0; v < layers[s + 1].n; v++)
                    sigmaa += layers[s + 1].delta[v] * weights[s][u][v];
                double f = layers[s].output[u];
                layers[s].delta[u] = f * (1 - f) * sigmaa;
            }
        }

        return erorrs;
    }

    void adjustWeights() {
        for (int s = 0; s < LAYERS_NUM - 1; s++) {
            for (int v = 0; v < layers[s + 1].n; v++) {
                for (int u = 0; u < layers[s].n; u++) {
                    weightChanges[s][u][v] = ni * layers[s + 1].delta[v] * layers[s].output[u] + beta * weightChanges[s][u][v];
                    weights[s][u][v] += weightChanges[s][u][v];
                }

                layers[s + 1].biasp[v] = ni * layers[s + 1].delta[v] + beta * layers[s + 1].biasp[v];
                layers[s + 1].bias[v] += layers[s + 1].biasp[v];

                double net = layers[s + 1].bias[v];

                for (int u = 0; u < layers[s].n; u++) {
                    net += layers[s].output[u] * weights[s][u][v];
                }

                layers[s + 1].output[v] = sigmoid(net);
            }
        }
    }

    double training() {
        greske = new ArrayList<>();
        double error = 0;

        for (int it = 0; it < MAX_ITERATIONS; it++) {
            error = 0;
            for (int sample = 0; sample < SAMPLES_NUM; sample++) {
                setInput(sample);
                calculateOutput();
                error += 0.5 * calculateErrors(sample);
                adjustWeights();
            }
        	System.out.println("" + it + " " + error);
            greske.add(new Point2D.Float(it, (float) error));
            if (error < MAX_ERROR)
                break;

        }

        return error;
    }

    public List<Point2D.Float> greske = null;

    public void train() {
        for (int attempt = 0; attempt < ATTEMPTS_NUM; attempt++) {
            weightsInitialize();

            double error = training();

            if (error < MAX_ERROR)
                break;

        }
    }

    public double[] calculate(double[] data) {
        for (int i = 0; i < data.length; i++) {
            layers[0].output[i] = data[i];
        }

        return calculateOutput();
    }
    
    public int izracunajCifru(double[] data)
    {
        double[] d = calculate(data);
        // izracunati koja je cifra prepoznata na osnovu
        // podataka na izlazima neuronske mreze
        double max = 0;
        int index = 0;
        for (int i = 0; i < 13; i++)
        {
            if (d[i] > max)
            {
                max = d[i];
                index = i;
            }
        }

        return index;
    }

    public Point2D.Float calculateIndex(double[] data) {
        double[] d = calculate(data);
        double max = 0;
        int index = 0;

        for (int i = 0; i < 18; i++) {
            if (d[i] > max) {
                max = d[i];
                index = i;
            }

        }

        return new Point2D.Float((float) max, index);
    }

}


