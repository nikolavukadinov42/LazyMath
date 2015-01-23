package ftn.sc.lazymath.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

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
import ftn.sc.lazymath.ocr.OcrTraining;
import ftn.sc.lazymath.ocr.OcrUtil;
import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.util.ImageFilter;

public class MainFrame extends JFrame implements ActionListener {

	public static final int WIDTH = 1200;
	public static final int HEIGHT = 650;
	public static final String TITLE = "Lazy math";

	private JPanel panelControls;

	private JButton buttonBlackAndWhite, buttonFindRegions, buttonFindPows, buttonReset;
	private ImagePanel imagePanel;

	private String imageFileName = "comb.png";
	private JButton buttonFindFractions;
	private JButton buttonLoadImage;

	private JTextField inputRecognize;
	private JButton buttonTrain;
	private JButton buttonTrainingSet;
	private JButton buttonProcess;
	private JButton buttonPrepareImage;
	private JButton buttonTrainWithOcrMath;

	private OcrTemplate ocrRecognize;
	private OcrTraining ocrTraining;
	private AbstractButton buttonFraction;
	private AbstractButton buttonSqrt;
	private AbstractButton buttonComb;

	public static final String DEFAULT_INPUT = "2*kab-/kx/kxaab*by";
	// sqrt - k3x+kxx+2
	// fraction - //x+/a/ab5b+3
	// combined - 2*kab-/kx/kxaab*by

	public MainFrame() {

		super(TITLE);

		// Create Graphical Interface
		buttonBlackAndWhite = new JButton("Black&White");
		buttonBlackAndWhite.addActionListener(this);
		buttonFindRegions = new JButton("FindRegions");
		buttonFindRegions.addActionListener(this);
		buttonFindPows = new JButton("FindPows");
		buttonFindPows.addActionListener(this);
		buttonFindFractions = new JButton("FindFractions");
		buttonFindFractions.addActionListener(this);
		buttonReset = new JButton("Reset");
		buttonReset.addActionListener(this);
		buttonLoadImage = new JButton("Load Image");
		buttonLoadImage.addActionListener(this);
		buttonTrainingSet = new JButton("Training Set");
		buttonTrainingSet.addActionListener(this);
		buttonTrain = new JButton("Train");
		buttonTrain.addActionListener(this);
		buttonProcess = new JButton("Process");
		buttonProcess.addActionListener(this);
		buttonPrepareImage = new JButton("Prepare Image");
		buttonPrepareImage.addActionListener(this);
		buttonTrainWithOcrMath = new JButton("Train Server");
		buttonTrainWithOcrMath.addActionListener(this);
		
		buttonFraction = new JButton("Fraction");
		buttonFraction.addActionListener(this);
		
		buttonSqrt = new JButton("Sqrt");
		buttonSqrt.addActionListener(this);
		
		buttonComb = new JButton("Comb");
		buttonComb.addActionListener(this);

		inputRecognize = new JTextField(DEFAULT_INPUT, 20);

		panelControls = new JPanel(new GridLayout(2, 2, 20, 20));
		JPanel panelUp = new JPanel();
		JPanel panelBottom = new JPanel();

		panelBottom.add(buttonBlackAndWhite);
		panelBottom.add(buttonFindRegions);
		panelBottom.add(buttonTrainingSet);
		panelBottom.add(buttonTrain);
		panelBottom.add(buttonProcess);
		panelBottom.add(buttonFindPows);
		panelBottom.add(buttonFindFractions);
		panelBottom.add(buttonReset);
		panelBottom.add(buttonLoadImage);

		panelUp.add(inputRecognize);
		panelUp.add(buttonPrepareImage);
		panelUp.add(buttonTrainWithOcrMath);
		
		panelUp.add(buttonFraction);
		panelUp.add(buttonSqrt);
		panelUp.add(buttonComb);
		panelControls.add(panelUp);
		panelControls.add(panelBottom);

		imagePanel = new ImagePanel(this);
		JScrollPane scrollPane = new JScrollPane(imagePanel);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panelControls, BorderLayout.SOUTH);
		contentPane.add(scrollPane, BorderLayout.NORTH);

		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setVisible(true);

		initialize();

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// buttonTrainWithOcrMath.doClick();
	}

	private void initialize() {
		ocrTraining = new OcrTraining(inputRecognize.getText());
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponents(g);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonBlackAndWhite) {
			OcrUtil.getBinaryMatrix((BufferedImage) imagePanel.getImage());
			BufferedImage processed = ImageUtil.matrixToBitmap(OcrUtil.image);
			imagePanel.setImage(processed);
		} else if (e.getSource() == buttonLoadImage) {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new ImageFilter());
			int returnVal = fc.showOpenDialog(MainFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (getOcr() != null) {
					getOcr().clearRegions();
				}
				imagePanel.setImage(file.toString());
			}
		} else if (e.getSource() == buttonFindRegions) {
			OcrUtil.getRegions(getInputRecognize());
			imagePanel.repaint();
		} else if (e.getSource() == buttonTrainingSet) {
			OcrUtil.trainingSet();
		} else if (e.getSource() == buttonTrain) {
			OcrUtil.train();
		} else if (e.getSource() == buttonProcess) {
			OcrUtil.prepoznaj();
			imagePanel.repaint();
		} else if (e.getSource() == buttonPrepareImage) {
			ocrRecognize = new OcrMath(inputRecognize.getText().trim());
			BufferedImage image = ocrRecognize.processImage(ImageUtil.bitmapToMatrix((BufferedImage) imagePanel.getImage()));
			imagePanel.setImage(image);
			ocrRecognize.recognize();
		} else if (e.getSource() == buttonTrainWithOcrMath) {
			String input = inputRecognize.getText().trim();
			if (input.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please provide an input string");
				return;
			}
			ocrTraining = new OcrTraining(input);
			ocrTraining.processImage(ImageUtil.bitmapToMatrix((BufferedImage) imagePanel.getImage()));
		} else if (e.getSource() == buttonFraction) {
			inputRecognize.setText("//x+/a/ab5b+3");
			imagePanel.setImage("./res/example.png");
		} else if (e.getSource() == buttonSqrt) {
			inputRecognize.setText("k3x+kxx+2");
			imagePanel.setImage("./res/example.gif");
		} else if (e.getSource() == buttonComb) {
			inputRecognize.setText("2*kab-/kx/kxaab*by");
			imagePanel.setImage("./res/comb.png");
		}
		// sqrt - k3x+kxx+2
		// fraction - //x+/a/ab5b+3
		// combined - 2*kab-/kx/kxaab*by
	}

	public OcrTemplate getOcr() {
		return ocrRecognize;
	}

	public String getInputRecognize() {
		return inputRecognize.getText();
	}

	public void setInputRecognize(JTextField inputRecognize) {
		this.inputRecognize = inputRecognize;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
}
