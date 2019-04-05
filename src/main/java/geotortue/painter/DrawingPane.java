package geotortue.painter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Stack;

import javax.swing.JPanel;


class DrawingPane extends JPanel {

	private static final long serialVersionUID = 4812290172733024708L;

	private BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	private Image scaledImage;
	private int zoom = 1;
	
	DrawingPane(){
		setBackground(new Color(200, 200, 200));
	}

	boolean zoom(boolean in) {
		if (in)
			if (zoom == 8)
				return false;
			else
				zoom *= 2;
		else 
			if (zoom == 1)
				return false;
			else
				zoom /= 2;
		return true;
	}
	
	Point getImageHit() {
		Point p = getMousePosition();
		final int x = (p.x - getXOffset())/zoom;
		final int y = (p.y - getYOffset())/(zoom);
		
		if (x >=0 && y >= 0 && x < img.getWidth() && y < img.getHeight())
			return new Point(x, y);
		else 
			return null;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(img.getWidth()*zoom+4, img.getHeight()*zoom+4);
	}
	
	private int getXOffset() {
		return (getWidth() - img.getWidth()*zoom)/2;
	}

	private int getYOffset() {
		return (getHeight() - img.getHeight()*zoom)/2;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if (img==null)
			return;
		
		int imgWidth = img.getWidth()*zoom;
		int imgHeight = img.getHeight()*zoom;
		
		int xOffset = getXOffset();
		int yOffset = getYOffset();
		
		g.drawRect(xOffset - 1, yOffset - 1, imgWidth + 1, imgHeight + 1);
		g.drawRect(xOffset - 3, yOffset - 3, imgWidth + 5, imgHeight + 5);
		
		g.drawImage(scaledImage, xOffset, yOffset, this);
	}
	
	private int getRGB(int x, int y) {
		return img.getRGB(x, y);
	}

	private void setRGB(int x, int y, int rgb) {
		img.setRGB(x, y, rgb);
	}
	
	public BufferedImage getImage() {
		return img;
	}

	public void setImage(BufferedImage im) {
		if (im==null)
			return;
		this.img=im;
		updateScaledImage();
	}
	
	void updateScaledImage() {
		this.scaledImage = (zoom!=1)? img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_FAST) : img;
		repaint();
	}
	
	public Raster getData() {
		return img.getData();
	}
	
	public void setData(Raster r) {
		img.setData(r);
		updateScaledImage();
	}
	
	/*
	 * 
	 */
	

	private int distance(int c1, int c2) {
		int r1 = ((c1 & 0x00FF0000) >> 16);
		int g1 = ((c1 & 0x0000FF00) >> 8);
		int b1 = (c1 & 0x000000FF);
		int r2 = ((c2 & 0x00FF0000) >> 16);
		int g2 = ((c2 & 0x0000FF00) >> 8);
		int b2 = (c2 & 0x000000FF);

		int dr = Math.abs(r1 - r2);
		int dg = Math.abs(g1 - g2);
		int db = Math.abs(b1 - b2);

		return Math.max(dr, Math.max(dg, db));
	}
	
	void replaceColor(int i, int j, int newRgb, int tolerance){
		int oldRgb = getRGB(i, j);
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int c = getRGB(x, y);
				if (distance(c, oldRgb)<tolerance)
					setRGB(x, y, newRgb);
			}
		}
		updateScaledImage();
	}
	
	void floodFill(int i, int j, int newRgb, int tolerance) {
		int oldRgb = getRGB(i, j);
		int d0=distance(newRgb, oldRgb);
		if (d0==0)
			return;
		
		if (d0 < tolerance)
			tolerance = distance(newRgb, oldRgb)-1; 
		
		Stack<Integer> xStack = new Stack<Integer>();
		Stack<Integer> yStack = new Stack<Integer>();

		int x0 = i;
		int y0 = j;

		xStack.push(x0);
		yStack.push(y0);

		while (!xStack.isEmpty()) {
			x0 = xStack.pop();
			y0 = yStack.pop();

			// Inspection des pixels à gauche et à droite
			int west = x0;
			while (west >= 0 && distance(getRGB(west, y0), oldRgb) < tolerance)
				west--;
			west++;
			
			int east = x0;
			while (east < img.getWidth() && distance(getRGB(east, y0), oldRgb) < tolerance)
				east++;
			east--;
			// Inspection de la ligne précédente
			if (y0 > 0) {
				boolean shouldPushNextValidPixel = true;
				for (int x = west; x <= east; x++) {
					int d = distance(getRGB(x, y0 - 1), oldRgb);
					if (d < tolerance) {
						if (shouldPushNextValidPixel) {
							xStack.push(x);
							yStack.push(y0 - 1);
							shouldPushNextValidPixel = false;
						}
					} else {
						shouldPushNextValidPixel = true;
					}
				}
			}

			// Inspection de la ligne suivante
			if (y0 + 1 < img.getHeight()) {
				boolean shouldPushNextValidPixel = true;
				for (int x = west; x <= east; x++) {
					int  d = distance(getRGB(x, y0 + 1), oldRgb);
					if (d < tolerance) {
						if (shouldPushNextValidPixel) {
							xStack.push(x);
							yStack.push(y0 + 1);
							shouldPushNextValidPixel = false;
						}
					} else {
						shouldPushNextValidPixel = true;
					}
				}
			}

			// Remplissage
			for (int x = west; x <= east; x++)
				setRGB(x, y0, newRgb);
		}
		updateScaledImage();
	}

	void crop() {
		int y0 = getUpperBound();
		if (y0 < 0)
			return;
		int y1 = getLowerBound();
		int x0 = getLeftBound();
		int x1 = getRightBound();
		if (y0 > y1 || x0 > x1)
			return;
		
		int imgWidth = img.getWidth();
		int imgHeight= img.getHeight();
		
		int w = x1-x0+1;
		int h = y1-y0+1;

		int offX = 20 - (w % 10);
		int offY = 20 - (h % 10);
		
		int x = x0-offX/2;
		int y = y0-offY/2;
		int width = w+offX;
		int height = h+offY;
		
		if (width > imgWidth) {
			width = imgWidth;
			x =0;
		} else {
			x = (x < 0)? 0 : x;
			x = (x + width > img.getWidth())?  img.getWidth() - width : x;
		}
		
		if (height > imgHeight) {
			height = imgHeight;
			y = 0;
		} else {
			y = (y < 0)? 0 : y;
			y = (y + height > img.getHeight())?  img.getHeight() - height : y;
		}
		
		BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) im.getGraphics();
	 	g2.drawImage(img.getSubimage(x, y, width, height), 0, 0, null);
		setImage(im);
	}

	private int getUpperBound() {
		int rgb = getRGB(0, 0);
		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++)
				if (getRGB(x, y) != rgb)
					return y;
		return -1;
	}
	
	private int getLowerBound() {
		int rgb = getRGB(0, img.getHeight()-1);
		for (int y = img.getHeight()-1; y >=0 ; y--)
			for (int x = 0; x < img.getWidth(); x++)
				if (getRGB(x, y) != rgb)
					return y;
		return -1;
	}
	
	private int getLeftBound() {
		int rgb = getRGB(0, 0);
		for (int x = 0; x < img.getWidth(); x++)
			for (int y = 0; y < img.getHeight(); y++)
				if (getRGB(x, y) != rgb)
					return x;
		return -1;
	}
	
	private int getRightBound() {
		int rgb = getRGB(img.getWidth()-1, 0);
		for (int x = img.getWidth()-2; x >=0 ; x--)
			for (int y = 0; y < img.getHeight(); y++)
				if (getRGB(x, y) != rgb)
					return x;
		return -1;
	}
}