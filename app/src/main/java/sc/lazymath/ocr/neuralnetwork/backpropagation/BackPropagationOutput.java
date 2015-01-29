package sc.lazymath.ocr.neuralnetwork.backpropagation;

public class BackPropagationOutput {
	private int index;
	private double outputValue;

	public BackPropagationOutput(int index, double outputValue) {
		super();
		this.index = index;
		this.outputValue = outputValue;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getOutputValue() {
		return this.outputValue;
	}

	public void setOutputValue(double outputValue) {
		this.outputValue = outputValue;
	}

}
