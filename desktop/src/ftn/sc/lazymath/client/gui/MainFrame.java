package ftn.sc.lazymath.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ftn.sc.lazymath.ocr.Ocr;
import ftn.sc.lazymath.ocr.OcrTemplate;
import ftn.sc.lazymath.ocr.OcrTraining;
import ftn.sc.lazymath.ocr.OcrUtil;
import ftn.sc.lazymath.ocr.imageprocessing.ImageUtil;
import ftn.sc.lazymath.util.ImageFilter;

public class MainFrame extends JFrame implements ActionListener {

	public static final int WIDTH = 1800;
	public static final int HEIGHT = 600;
	public static final String TITLE = "Lazy math";

	private JPanel panelControls;

	private JButton buttonBlackAndWhite, buttonFindRegions, buttonFindPows, buttonReset;
	private ImagePanel imagePanel;

	private String imageFileName = "razlomak.png";
	private JButton buttonFindFractions;
	private JButton buttonLoadImage;

	private JTextField inputRecognize;
	private JButton buttonTrain;
	private JButton buttonTrainingSet;
	private JButton buttonProcess;
	private JButton buttonPrepareImage;
	private OcrTemplate ocr;
	private JButton buttonTrainServer;

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
		buttonTrainServer = new JButton("Train Server");
		buttonTrainServer.addActionListener(this);

//		inputRecognize = new JTextField("xx+1+(y*2)4", 10);
		inputRecognize = new JTextField("ab+1ab+1abbb+22x+13+(x+y)2+a", 10);

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
		panelUp.add(buttonTrainServer);
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
		
		
		buttonTrainServer.doClick();
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
		} 
		else if (e.getSource() == buttonLoadImage) {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new ImageFilter());
			int returnVal = fc.showOpenDialog(MainFrame.this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            imagePanel.setImage(file.toString());
	        }
		} 
		else if(e.getSource() == buttonFindRegions) {
			OcrUtil.getRegions(getInputRecognize());
			imagePanel.repaint();
		} 
		else if (e.getSource() == buttonTrainingSet) {
			OcrUtil.trainingSet();
		} 
		else if (e.getSource() == buttonTrain) {
			OcrUtil.train();
		} 
		else if (e.getSource() == buttonProcess) {
			OcrUtil.prepoznaj();
			imagePanel.repaint();
		}
		else if (e.getSource() == buttonPrepareImage) {
			BufferedImage image = ocr.processImage(ImageUtil.bitmapToMatrix((BufferedImage) imagePanel.getImage()));
			imagePanel.setImage(image);
			ocr.recognize();
		}
		else if (e.getSource() == buttonTrainServer) {
			OcrTraining temp = new OcrTraining("ab+1ab+1abbb+22x+13+(x+y)2+a");//ab+1ab+1abbb+22x+13+(x+y)2+a
			temp.processImage(ImageUtil.bitmapToMatrix((BufferedImage) imagePanel.getImage()));
			ocr = new Ocr(temp.getAlfabet(), temp.getAlfabetInverse(), temp.getBackPropagation());
		}
	}

	public OcrTemplate getOcr() {
		return ocr;
	}

	public String getInputRecognize() {
		return inputRecognize.getText();
	}

	public void setInputRecognize(JTextField inputRecognize) {
		this.inputRecognize = inputRecognize;
	}
}
