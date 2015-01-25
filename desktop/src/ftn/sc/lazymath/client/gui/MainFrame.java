package ftn.sc.lazymath.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ftn.sc.lazymath.ocr.OcrMath;
import ftn.sc.lazymath.ocr.OcrTemplate;
import ftn.sc.lazymath.ocr.OcrUtil;
import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;
import ftn.sc.lazymath.ocr.neuralnetwork.NeuralNetwork;
import ftn.sc.lazymath.util.ImageFilter;

public class MainFrame extends JFrame implements ActionListener {

	public static final int WIDTH = 1200;
	public static final int HEIGHT = 650;
	public static final String TITLE = "Lazy math";

	private JPanel panelControls;

	private JButton buttonBlackAndWhite, buttonFindRegions, buttonFindPows, buttonReset;
	private ImagePanel imagePanel;

	
	private JButton buttonFindFractions;
	private JButton buttonLoadImage;

	private JTextField inputRecognize;
	private JButton buttonTrain;
	private JButton buttonProcess;
	private JButton buttonPrepareImage;
	private JButton buttonTrainWithOcrMath;

	private OcrTemplate ocrRecognize;
	private AbstractButton buttonFraction;
	private AbstractButton buttonSqrt;
	private AbstractButton buttonComb;

	private OcrMath ocrMath = null;

//	private String imageFileName = "comb.png";
//	public static final String DEFAULT_INPUT = "2*kab-/kx/kxaab*by";
	
//	private String imageFileName = "sqrt.png";
//	public static final String DEFAULT_INPUT = "k3x+kxx+2";
//	
//	private String imageFileName = "fraction.png";
//	public static final String DEFAULT_INPUT = "//x+/a/ab5b+3";

//	private String imageFileName = "exponents1.png";
//	public static final String DEFAULT_INPUT = "xx+1+(y*2)4";
	
//	private String imageFileName = "exponents.png";
//	public static final String DEFAULT_INPUT = "ab+1ab+1ab+22+13";
	
//	private String imageFileName = "exponents2.png";
//	public static final String DEFAULT_INPUT = "ab+1ab+1abbb+22x+13+(x+y)2+a";
	
//	private String imageFileName = "testt.png";
//	public static final String DEFAULT_INPUT = "ab+1ab+1abbb+22x+13+(x+y)2+a";
	
	private String imageFileName = "complex2.png";
	public static final String DEFAULT_INPUT = "x+y*/a+k/xaa+2*/xb2b+cv+b*/kak/xyx++*y2y";
	
	public MainFrame() {

		super(TITLE);

		// Create Graphical Interface
		this.buttonBlackAndWhite = new JButton("Black&White");
		this.buttonBlackAndWhite.addActionListener(this);
		this.buttonFindRegions = new JButton("FindRegions");
		this.buttonFindRegions.addActionListener(this);
		this.buttonFindPows = new JButton("FindPows");
		this.buttonFindPows.addActionListener(this);
		this.buttonFindFractions = new JButton("FindFractions");
		this.buttonFindFractions.addActionListener(this);
		this.buttonReset = new JButton("Reset");
		this.buttonReset.addActionListener(this);
		this.buttonLoadImage = new JButton("Load Image");
		this.buttonLoadImage.addActionListener(this);
		this.buttonTrain = new JButton("Train");
		this.buttonTrain.addActionListener(this);
		this.buttonProcess = new JButton("Process");
		this.buttonProcess.addActionListener(this);
		this.buttonPrepareImage = new JButton("Prepare Image");
		this.buttonPrepareImage.addActionListener(this);
		this.buttonTrainWithOcrMath = new JButton("Train Server");
		this.buttonTrainWithOcrMath.addActionListener(this);

		this.buttonFraction = new JButton("Fraction");
		this.buttonFraction.addActionListener(this);

		this.buttonSqrt = new JButton("Sqrt");
		this.buttonSqrt.addActionListener(this);

		this.buttonComb = new JButton("Comb");
		this.buttonComb.addActionListener(this);

		this.inputRecognize = new JTextField(DEFAULT_INPUT, 20);

		this.panelControls = new JPanel(new GridLayout(2, 2, 20, 20));
		JPanel panelUp = new JPanel();
		JPanel panelBottom = new JPanel();

		panelBottom.add(this.buttonBlackAndWhite);
		panelBottom.add(this.buttonFindRegions);
		panelBottom.add(this.buttonTrain);
		panelBottom.add(this.buttonProcess);
		panelBottom.add(this.buttonFindPows);
		panelBottom.add(this.buttonFindFractions);
		panelBottom.add(this.buttonReset);
		panelBottom.add(this.buttonLoadImage);

		panelUp.add(this.inputRecognize);
		panelUp.add(this.buttonPrepareImage);
		panelUp.add(this.buttonTrainWithOcrMath);

		panelUp.add(this.buttonFraction);
		panelUp.add(this.buttonSqrt);
		panelUp.add(this.buttonComb);
		this.panelControls.add(panelUp);
		this.panelControls.add(panelBottom);

		this.imagePanel = new ImagePanel(this);
		JScrollPane scrollPane = new JScrollPane(this.imagePanel);

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this.panelControls, BorderLayout.SOUTH);
		contentPane.add(scrollPane, BorderLayout.NORTH);

		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// buttonTrainWithOcrMath.doClick();
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponents(g);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.buttonBlackAndWhite) {
			int[][] image = OcrUtil
					.convertImageToMatrix((BufferedImage) this.imagePanel.getImage());
			BufferedImage processed = ImageUtil.matrixToBitmap(image);
			this.imagePanel.setImage(processed);
		} else if (e.getSource() == this.buttonLoadImage) {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new ImageFilter());
			int returnVal = fc.showOpenDialog(MainFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (this.getOcr() != null) {
					this.getOcr().clearRegions();
				}
				this.imagePanel.setImage(file.toString());
			}
		} else if (e.getSource() == this.buttonFindRegions) {
		} else if (e.getSource() == this.buttonTrain) {
			this.inputRecognize.setText("0123456789abcdxyz+-/*±");
			this.imagePanel.setImage("./res/ts2.png");
			List<RasterRegion> regions = OcrUtil.getRegions((BufferedImage) this.imagePanel
					.getImage());

			int[][] image = ImageUtil.bitmapToMatrix((BufferedImage) this.imagePanel.getImage());
			image = ImageUtil.matrixToBinary(image, 200);
			image = ImageUtil.dilation(image);

			this.imagePanel.setImage(ImageUtil.matrixToBitmap(image));

			NeuralNetwork nn = new NeuralNetwork(regions, this.getInputRecognize());
			this.ocrMath = new OcrMath(nn);
		} else if (e.getSource() == this.buttonProcess) {
			if (this.ocrMath != null) {
				int[][] image = OcrUtil.convertImageToMatrix((BufferedImage) this.imagePanel
						.getImage());

				BufferedImage bitmap = ImageUtil.matrixToBitmap(image);

				this.imagePanel.setImage(bitmap);

				this.ocrMath.processImage(image);

				System.out.println(this.ocrMath.recognize());
			}
		} else if (e.getSource() == this.buttonPrepareImage) {
			this.ocrRecognize = new OcrMath(this.inputRecognize.getText().trim());
			int[][] norf = ImageUtil.bitmapToMatrix((BufferedImage) this.imagePanel.getImage());
			BufferedImage image = this.ocrRecognize.processImage(norf);
			this.imagePanel.setImage(image);
		} else if (e.getSource() == this.buttonTrainWithOcrMath) {
			String input = this.inputRecognize.getText().trim();
			if (input.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please provide an input string");
				return;
			}
			// ocrTraining = new OcrTraining(input);
			// ocrTraining.processImage(ImageUtil.bitmapToMatrix((BufferedImage)
			// imagePanel.getImage()));
		} else if (e.getSource() == this.buttonFraction) {
			this.inputRecognize.setText("//x+/a/ab5b+3");
			this.imagePanel.setImage("./res/example.png");
		} else if (e.getSource() == this.buttonSqrt) {
			this.inputRecognize.setText("k3x+kxx+2");
			this.imagePanel.setImage("./res/example.gif");
		} else if (e.getSource() == this.buttonComb) {
			this.inputRecognize.setText("2*kab-/kx/kxaab*by");
			this.imagePanel.setImage("./res/comb.png");
		}
		// sqrt - k3x+kxx+2
		// fraction - //x+/a/ab5b+3
		// combined - 2*kab-/kx/kxaab*by
	}

	public OcrTemplate getOcr() {
		return this.ocrRecognize;
	}

	public String getInputRecognize() {
		return this.inputRecognize.getText();
	}

	public void setInputRecognize(JTextField inputRecognize) {
		this.inputRecognize = inputRecognize;
	}

	public String getImageFileName() {
		return this.imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
}
