//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:54
//

package sc.lazymath.ocr.neuralnetwork.backpropagation;


import java.io.Serializable;

import sc.lazymath.ocr.neuralnetwork.backpropagation.BackPropagation;

public class NeuralNetworkLayer implements Serializable {
    public int n;
    public double[] output = new double[BackPropagation.MAX_NUM];
    public double[] delta = new double[BackPropagation.MAX_NUM];
    public double[] bias = new double[BackPropagation.MAX_NUM];
    public double[] biasp = new double[BackPropagation.MAX_NUM];
}