package fw.renderer.fwre;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import fw.geometry.util.Pixel;

public abstract class TriangleRasterizer {

	final public int xmin, ymin, xmax, ymax, width, height;
	final public Pixel p0, p1, p2;
	final private int det;
	
	public TriangleRasterizer(Pixel p0, Pixel p1, Pixel p2, int width, int height) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.xmin = Math.max(min(p0.i, p1.i, p2.i), 0);
		this.xmax = Math.min(max(p0.i, p1.i, p2.i), width);
		this.ymin = Math.max(min(p0.j, p1.j, p2.j), 0);
		this.ymax = Math.min(max(p0.j, p1.j, p2.j), height);

		this.det = det(p0, p1, p2);
		
		this.width = xmax - xmin;
		this.height = ymax - ymin;
	}
	
	public BufferedImage getImage() {
		if (width * height == 0 || det == 0)
			return null;
		BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = im.getRaster();
		raster = raster.createWritableTranslatedChild(xmin, ymin);

		int s = (det>0) ? 1 : -1;
		
		Pixel p;
		for (int i = xmin; i < xmax; i++)
			for (int j = ymin; j < ymax; j++) {
				p = new Pixel(i, j);
				int a0 = det(p, p1, p2);
				if (a0*s>=0) {
					int a1 = det(p0, p, p2);
					if (a1*s>=0) {
						int a2 = det(p0, p1, p); //det-a0-a1;
						
						if (a2*s>=0) {
							int[] col = getRGBAColor(i, j, a0, a1, a2);
							if (col !=null)
								raster.setPixel(i, j, col);
						}
					}
				}
			}
		
		raster = raster.createWritableTranslatedChild(0, 0);
		im.setData(raster);
		return im;
	}
	
	public abstract int[] getRGBAColor(int i, int j, int a0, int a1, int a2);

	private int min(int a, int b, int c){
		return Math.min(Math.min(a, b), c);
	}
	
	private int max(int a, int b, int c){
		return Math.max(Math.max(a, b), c);
	}
	
	private int det(Pixel p0, Pixel p1, Pixel p2) {
		return det(p0.i, p0.j, p1.i, p1.j, p2.i, p2.j);
	}
	
	private int det(int x0, int y0, int x1, int y1, int x2, int y2) {
		return x0*y1 + x1*y2 + x2*y0 - x0*y2 - x1*y0 - x2*y1;
	}

	public Rectangle getBounds() {
		return new Rectangle(xmin, ymin, width, height);
	}
}