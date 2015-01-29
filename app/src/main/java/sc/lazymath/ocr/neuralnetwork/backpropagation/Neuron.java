//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:54
//

package sc.lazymath.ocr.neuralnetwork.backpropagation;

public class Neuron {
    public double[] weights;
    public int X;
    public int Y;
    private int length;
    private double nf;

    public Neuron(int x, int y, int length) throws Exception {
        X = x;
        Y = y;
        this.length = length;
        nf = 1000 / Math.log(length);
    }

    private double gauss(Neuron win, int it) throws Exception {
        double rr = Math.sqrt(Math.pow(win.X - X, 2) + Math.pow(win.Y - Y, 2));
        return Math.exp(-Math.pow(rr, 2) / (Math.pow(strength(it), 2)));
    }

    private double trainingStep(int it) throws Exception {
        return Math.exp(-it / 1000) * 0.1;
    }

    private double strength(int it) throws Exception {
        return Math.exp(-it / nf) * length;
    }

    private double gaussAproximation(Neuron win, int it) throws Exception {
        return 0;
    }

    // aproksimirati gausovu funckiju
    private double trainingStepAproximation(int it) throws Exception {
        return 0;
    }

    // aproksimirati eksponencijalnu funckiju
    public double weightsChange(double[] pattern, Neuron winner, int it, boolean fast) throws Exception {
        double sum = 0;
        double kk = 0;
        double gg = 0;
        if (!fast) {
            kk = trainingStep(it);
            gg = gauss(winner, it);
        } else {
            kk = trainingStepAproximation(it);
            gg = gaussAproximation(winner, it);
        }
        for (int i = 0; i < weights.length; i++) {
            double delta = kk * gg * (pattern[i] - weights[i]);
            weights[i] += delta;
            sum += delta;
        }
        return sum / weights.length;
    }

}


