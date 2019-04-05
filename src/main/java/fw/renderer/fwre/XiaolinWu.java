/**
 * 
 */
package fw.renderer.fwre;

import java.awt.image.WritableRaster;

import fw.geometry.util.Pixel;

public class XiaolinWu extends LineDrawer {

	public XiaolinWu(WritableRaster raster, ZColorI zColor) {
		super(raster, zColor);
	}

	public void drawLine(Pixel p0, Pixel p1) {
		drawLine(p0.i, p0.j, p1.i, p1.j);
	}

	private float fpart(float x) {
		if (x < 0)
			return 1 - (x - (int) x);
		return x - (int) x;
	}

	private void drawLine(int x1, int y1, int x2, int y2) {
		int dx = x2 - x1;
		int dy = y2 - y1;
		if (dx==0 && dy==0) {
			plot(x1, y1, 1, .5f, false);
			return;
		}
		
		boolean steeped = false;
		boolean swapped = false;
		
		if (Math.abs(dx) < Math.abs(dy)) {
			int tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
			tmp = x2;
			x2 = y2;
			y2 = tmp;
			tmp = dx;
			dx = dy;
			dy = tmp;
			steeped = true;
		}

		if (x2 < x1) {
			int tmp;
			tmp = x1;
			x1 = x2;
			x2 = tmp;
			tmp = y1;
			y1 = y2;
			y2 = tmp;
			swapped = true;
		}
		
		if (dx==1) {
			if (steeped) 
				plot(y1, x1, 1, .5f, swapped);
			else 
				plot(x1, y1, 1, .5f, swapped);
			return;
		}
		

		float gradient = dy / (float) dx;

		// handle first endpoint
		int xend = Math.round(x1);
		float yend = y1 + gradient * (xend - x1);
		float xgap = 1-fpart(x1 + 0.5f);
		int xpxl1 = xend; // this will be used in the main loop
		int ypxl1 = (int) (yend);
		float c = fpart(yend) ;
		if (steeped) {
			plot(ypxl1, xpxl1, (1-c) * xgap, 1, swapped);
			plot(ypxl1+1, xpxl1, c * xgap, 1, swapped);
		} else {
			plot(xpxl1, ypxl1, (1-c) * xgap, 1, swapped);
			plot(xpxl1, ypxl1 + 1, c * xgap, 1, swapped);
		}
		float intery = yend + gradient; // first y-intersection for the main loop

		// handle second endpoint
		xend = Math.round(x2);
		yend = y2 + gradient * (xend - x2);
		xgap = fpart(x2 + 0.5f);
		int xpxl2 = xend; // this will be used in the main loop
		int ypxl2 = (int) (yend);
		c = fpart(yend) ;
		if (steeped) {
			plot(ypxl2, xpxl2, (1-c) * xgap, 0, swapped);
			plot(ypxl2+1, xpxl2, c * xgap , 0, swapped);
		} else {
			plot(xpxl2, ypxl2, (1-c) * xgap, 0, swapped);
			plot(xpxl2, ypxl2 + 1, c * xgap, 0, swapped);
		}

		// main loop
		float w = xpxl1-xpxl2; 
		for (int x = xpxl1 + 1; x < xpxl2; x++) {
			float t = (x-xpxl2)/w;
			int y = (int) (intery);
			c = fpart(intery);
			if (steeped) {
				plot(y, x, 1-c, t, swapped);
				plot(y + 1, x, c, t, swapped);
			} else {
				plot(x, y, 1-c, t, swapped);
				plot(x, y + 1, c, t, swapped);
			}
			intery += gradient;
		}
	}

	private void plot(int x, int y, float c, float t, boolean swapped) {
		if (bounds.contains(x, y) && !Float.isNaN(c)) {
			int[] col = (swapped) ? zColor.getRGBColor(x, y, 1-t) : zColor.getRGBColor(x, y, t);
			if (col != null) {
				int[] bg = raster.getPixel(x, y, new int[4]);
				int r = (int) (c * col[0] + (1 - c) * bg[0]);
				int g = (int) (c * col[1] + (1 - c) * bg[1]);
				int b = (int) (c * col[2] + (1 - c) * bg[2]);
				raster.setPixel(x, y, new int[] { r, g, b, 255});
			}
		}
	}
}
