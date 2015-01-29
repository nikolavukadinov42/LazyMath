package sc.lazymath.ocr.neuralnetwork;

public class NeuralNetworkResult {
	private String character;
	private double outputValue;

	public NeuralNetworkResult(String character, double outputValue) {
		super();
		this.character = character;
		this.outputValue = outputValue;
	}

	public String getCharacter() {
		return this.character;
	}

	public void setCharacter(String character) {
		this.character = character;
	}

	public double getOutputValue() {
		return this.outputValue;
	}

	public void setOutputValue(double outputValue) {
		this.outputValue = outputValue;
	}

}
