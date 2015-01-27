package ftn.sc.lazymath.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

	public static final int WIDTH = 1800;
	public static final int HEIGHT = 650;
	public static final String TITLE = "Lazy math";

	private JPanel panelControls;

	private JButton buttonBlackAndWhite, buttonFindRegions;
	private ImagePanel imagePanel;

	private String imageFileName = "comb.png";
	private JButton buttonLoadImage;
	private JButton buttonTrain;
	private JButton buttonProcess;

	private OcrTemplate ocrRecognize;
	private AbstractButton buttonFraction;
	private AbstractButton buttonSqrt;
	private AbstractButton buttonComb;

	private OcrMath ocrMath = null;

	public static final String DEFAULT_INPUT = "2*kab-/kx/kxaab*by";
	private JTextField tfHeight;
	private JTextField tfWidth;
	private JButton btnReset;

	// sqrt - k3x+kxx+2
	// fraction - //x+/a/ab5b+3
	// combined - 2*kab-/kx/kxaab*by

	public MainFrame() {

		super(TITLE);

		// Create Graphical Interface
		this.buttonBlackAndWhite = new JButton("Black&White");
		this.buttonBlackAndWhite.addActionListener(this);
		this.buttonFindRegions = new JButton("Process screenshot");
		this.buttonFindRegions.addActionListener(this);
		this.buttonLoadImage = new JButton("Load Image");
		this.buttonLoadImage.addActionListener(this);
		this.buttonProcess = new JButton("Process image");
		this.buttonProcess.addActionListener(this);

		this.buttonFraction = new JButton("Fraction");
		this.buttonFraction.addActionListener(this);

		this.buttonSqrt = new JButton("Sqrt");
		this.buttonSqrt.addActionListener(this);

		this.buttonComb = new JButton("Comb");
		this.buttonComb.addActionListener(this);

		this.panelControls = new JPanel(new GridLayout(2, 2, 20, 20));
		JPanel panelUp = new JPanel();
		JPanel panelBottom = new JPanel();

		panelBottom.add(this.buttonBlackAndWhite);
		this.buttonTrain = new JButton("Train");
		this.buttonTrain.addActionListener(this);
		panelBottom.add(this.buttonTrain);
		panelBottom.add(this.buttonFindRegions);
		panelBottom.add(this.buttonProcess);
		panelBottom.add(this.buttonLoadImage);

		panelUp.add(this.buttonFraction);
		panelUp.add(this.buttonSqrt);
		panelUp.add(this.buttonComb);
		this.panelControls.add(panelUp);

		this.tfHeight = new JTextField();
		panelUp.add(this.tfHeight);
		this.tfHeight.setColumns(5);

		this.tfWidth = new JTextField();
		panelUp.add(this.tfWidth);
		this.tfWidth.setColumns(5);
		this.panelControls.add(panelBottom);

		this.btnReset = new JButton("Reset");
		panelBottom.add(this.btnReset);
		this.btnReset.addActionListener(this);

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
			int[][] image = OcrUtil.convertImageToMatrix(
					(BufferedImage) this.imagePanel.getImage(),
					Integer.parseInt(this.tfHeight.getText()),
					Integer.parseInt(this.tfWidth.getText()));
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
				this.imageFileName = file.toString();
			}
		} else if (e.getSource() == this.buttonFindRegions) {
			if (this.ocrMath != null) {
				int[][] image = ImageUtil.bitmapToMatrix(((BufferedImage) this.imagePanel
						.getImage()));

				image = ImageUtil.matrixToBinary(image, 200);

				BufferedImage bitmap = ImageUtil.matrixToBitmap(image);

				this.imagePanel.setImage(bitmap);

				this.ocrMath.processImage(image);

				System.out.println(this.ocrMath.recognize());
			}
		} else if (e.getSource() == this.buttonTrain) {
			this.ocrMath = new OcrMath(this.trainNeuralNetworks());

			this.imagePanel.setImage("./res/download.jpg");
		} else if (e.getSource() == this.buttonProcess) {
			if (this.ocrMath != null) {
				int[][] image = OcrUtil.convertImageToMatrix(
						(BufferedImage) this.imagePanel.getImage(),
						Integer.parseInt(this.tfHeight.getText()),
						Integer.parseInt(this.tfWidth.getText()));

				BufferedImage bitmap = ImageUtil.matrixToBitmap(image);

				this.imagePanel.setImage(bitmap);

				this.ocrMath.processImage(image);

				System.out.println(this.ocrMath.recognize());
			}
		} else if (e.getSource() == this.buttonFraction) {
			this.imagePanel.setImage("./res/example.png");
		} else if (e.getSource() == this.buttonSqrt) {
			this.imagePanel.setImage("./res/example.gif");
		} else if (e.getSource() == this.buttonComb) {
			this.imagePanel.setImage("./res/comb.png");
		} else if (e.getSource() == this.btnReset) {
			this.imagePanel.setImage(this.imageFileName);
		}
	}

	private List<NeuralNetwork> trainNeuralNetworks() {
		List<NeuralNetwork> neuralNetworks = new ArrayList<NeuralNetwork>();
		String[] paths = new String[] { "./res/ts.png", "./res/tsH.png", "./res/tsVerdana.png" };
		String chars = "0123456789abcdxyz+-/*±()";

		try {
			for (String path : paths) {
				BufferedImage img = ImageIO.read(new File(path));
				List<RasterRegion> regions = OcrUtil.getRegions(img);

				NeuralNetwork nn = new NeuralNetwork(regions, chars);

				neuralNetworks.add(nn);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return neuralNetworks;
	}

	public OcrTemplate getOcr() {
		return this.ocrRecognize;
	}

	public String getImageFileName() {
		return this.imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
}
