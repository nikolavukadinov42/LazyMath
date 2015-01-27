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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ftn.sc.lazymath.ocr.OcrMath;
import ftn.sc.lazymath.ocr.imageprocessing.RasterRegion;

public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {

	private Image image;
	private String imagePath;
	private Point translate;
	private Rectangle ImageFrame;
	private Dimension preferredSize = new Dimension(1200, 480);
	private MainFrame mainFrame;

	public ImagePanel(MainFrame mainFrame) {
		super(new GridLayout(4, 4, 20, 20));
		this.mainFrame = mainFrame;
		this.imagePath = "./res/" + mainFrame.getImageFileName();
		this.setImage(this.imagePath);
		this.ImageFrame = new Rectangle(0, 0, this.image.getWidth(null), this.image.getHeight(null));
		this.translate = new Point();

		this.addMouseMotionListener(this);
		this.addMouseListener(this);

		this.setPreferredSize(this.preferredSize);
		this.setBackground(Color.cyan);
		this.setVisible(true);
	}

	public void setImage(String imagePath) {
		imagePath = imagePath.trim().isEmpty() ? this.imagePath : imagePath;
		this.imagePath = imagePath;
		try {
			BufferedImage img = ImageIO.read(new File(imagePath));
			System.out.println(img.getHeight() + ", " + img.getWidth());
			this.image = img;
			this.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImage(BufferedImage img) {
		this.image = img;
		this.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(this.getWidth() / 2 - this.image.getWidth(null) / 2 + this.translate.x,
				this.getHeight() / 2 - this.image.getHeight(null) / 2 + this.translate.y);
		// g2.scale(3,3);
		this.drawImage(g2);
		this.drawRegionsFrame(g2);
		// x(x+1)+(y*2)4
		g2.dispose();
	}

	public void drawRegionsFrame(Graphics2D g2) {
		g2.setColor(Color.red);
		if (this.mainFrame.getOcr() != null && this.mainFrame.getOcr().getBackupRegions() != null) {
			int regionsSize = this.mainFrame.getOcr().getBackupRegions().size();
			OcrMath ocr = (OcrMath) this.mainFrame.getOcr();
			int stringLen = ocr.getInputString().length();
			// System.out.println(regionsSize);
			if (this.mainFrame.getOcr().getBackupRegions().size() > 0) {
				int i = 0;
				for (RasterRegion r : this.mainFrame.getOcr().getBackupRegions()) {
					g2.drawRect(r.minX - 1, r.minY - 1, r.maxX - r.minX + 2, r.maxY - r.minY + 2);
					if (regionsSize == stringLen) {
						g2.drawString(String.valueOf(ocr.getInputString().charAt(i)), r.minX - 1,
								r.minY - 1);
					}
					i++;
				}
			}
		}
	}

	private void drawImage(Graphics2D g2) {
		if (this.image != null) {
			g2.drawImage(this.image, 0, 0, this.image.getWidth(null), this.image.getHeight(null),
					null);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return this.preferredSize;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	boolean pressed = false;

	@Override
	public void mousePressed(MouseEvent e) {
		if (this.translate.x < e.getPoint().x && this.translate.y < e.getPoint().y
				&& this.translate.x + this.image.getWidth(null) > e.getPoint().x
				&& this.translate.y + this.image.getHeight(null) > e.getPoint().y) {
			// pressed = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		this.pressed = false;
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
		if (this.pressed) {
			this.translate.setLocation(e.getPoint().x, e.getPoint().y);
			this.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public Image getImage() {
		return this.image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

}
