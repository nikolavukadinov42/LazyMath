package ftn.sc.lazymath.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ftn.sc.lazymath.ocr.OcrUtil;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {

	private Image image;
	private String imagePath;
	private Point translate;
	private Rectangle ImageFrame;
	private Dimension preferredSize = new Dimension(1800, 480);
	private MainFrame mainFrame;

	public ImagePanel(MainFrame mainFrame) {
		super(new GridLayout(4, 4, 20, 20));
		this.mainFrame = mainFrame;
		imagePath = "./res/exponents2.png";
		setImage(imagePath);
		ImageFrame = new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
		translate = new Point();

		addMouseMotionListener(this);
		addMouseListener(this);

		setPreferredSize(preferredSize);
		setBackground(Color.cyan);
		setVisible(true);
	}

	public void setImage(String imagePath) {
		imagePath = imagePath.trim().isEmpty() ? this.imagePath : imagePath;
		this.imagePath = imagePath;
		try {
			BufferedImage img = ImageIO.read(new File(imagePath));
			image = img;
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImage(BufferedImage img) {
		image = img;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(getWidth()/2 - image.getWidth(null)/2 + translate.x, getHeight()/2 - image.getHeight(null)/2 + translate.y);
		// g2.scale(3,3);
		drawImage(g2);
		drawRegionsFrame(g2);
		// x(x+1)+(y*2)4
		g2.dispose();
	}

	public void drawRegionsFrame(Graphics2D g2) {
		g2.setColor(Color.red);
		if (mainFrame.getOcr() != null && mainFrame.getOcr().getRegions() != null) {
			int regionsSize = mainFrame.getOcr().getRegions().size();
			int stringLen = OcrUtil.rez.length();
			// System.out.println(regionsSize);
			if (mainFrame.getOcr().getRegions().size() > 0) {
				int i = 0;
				for (RasterRegion r : mainFrame.getOcr().getRegions()) {
					g2.drawRect(r.minX - 1, r.minY - 1, r.maxX - r.minX + 2, r.maxY - r.minY + 2);
					if (regionsSize == stringLen) {
						g2.drawString(String.valueOf(OcrUtil.rez.charAt(i)), r.minX - 1, r.minY - 1);
					}
					i++;
				}
			}
		}
	}

	private void drawImage(Graphics2D g2) {
		if (image != null) {
			g2.drawImage(image, 0,0, image.getWidth(null), image.getHeight(null), null);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return preferredSize;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	boolean pressed = false;

	@Override
	public void mousePressed(MouseEvent e) {
		if (translate.x < e.getPoint().x && translate.y < e.getPoint().y && translate.x + image.getWidth(null) > e.getPoint().x
				&& translate.y + image.getHeight(null) > e.getPoint().y) {
//			pressed = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		pressed = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (pressed) {
			translate.setLocation(e.getPoint().x, e.getPoint().y);
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

}
