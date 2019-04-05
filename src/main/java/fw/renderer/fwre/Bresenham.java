package fw.renderer.fwre;

import java.awt.image.WritableRaster;

import fw.geometry.util.Pixel;


public class Bresenham extends  LineDrawer {
	
	public Bresenham(WritableRaster raster, ZColorI zColor) {
		super(raster, zColor);
	}

	public void drawLine(Pixel p0, Pixel p1) {
		drawLine(p0.i, p0.j, p1.i, p1.j);
	}
	
	private void drawLine(int i0, int j0, int i1, int j1) {
		int dx = i1-i0;
		int dy = j1-j0;
		if (dx==0 && dy==0) {
			setPixel(i0, j0, 0.5, true);
			return;
		}
		if (dy==0) {
			if (dx>0)
				drawHorizontalLine_1stOctant(i0, j0, i1, j1, true);
			else
				drawHorizontalLine_1stOctant(i1, j1, i0, j0, false);
		} else if (dx==0) {
			if (dy>0)
				drawVerticalLine_3rdOctant(i0, j0, i1, j1, true);
			else
				drawVerticalLine_3rdOctant(i1, j1, i0, j0, false);
		} else if (dx == dy) {
			if (dx > 0) 
				drawDiagonal_2ndOctant(i0, j0, i1, j1, true);
			else
				drawDiagonal_2ndOctant(i1, j1, i0, j0, false);
		} else if (dx == -dy) {
			if (dx > 0)
				drawDiagonal_4thOctant(i1, j1, i0, j0, false);
			else
				drawDiagonal_4thOctant(i0, j0, i1, j1, true);
		} else if (dy > 0) {
			if (dx > 0) {
				if (dx > dy)
					drawLine_1stOctant(i0, j0, i1, j1, true);
				else
					drawLine_2ndOctant(i0, j0, i1, j1, true);
			} else {
				if (dy > - dx)
					drawLine_3rdOctant(i0, j0, i1, j1, true);
				else
					drawLine_4thOctant(i0, j0, i1, j1, true);
			}
		} else {
			if (dx < 0) {
				if (dx < dy)
					drawLine_1stOctant(i1, j1, i0, j0, false);
				else
					drawLine_2ndOctant(i1, j1, i0, j0, false);
			} else {
				if (dx < - dy)
					drawLine_3rdOctant(i1, j1, i0, j0, false);
				else
					drawLine_4thOctant(i1, j1, i0, j0, false);
			}
		}
	}
	
	private void setPixel(int x, int y, double alpha, boolean pointsOrdered) {
		if (bounds.contains(x, y)) {
			int[] col = zColor.getRGBColor(x, y, alpha);
			if (col !=null)
					raster.setPixel(x, y, col);
		}
	}
	
	private void drawLine_1stOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int xStart = i0;
		int y = j0;
		
		double dx = i1 - i0;
		double dy = j1 - j0;
		double l1 = (xmin - i0)/dx;
		double l2 = (ymin - j0)/dy;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				xStart = xmin;
				y = (int) (j0 + l1 * dy);
			} else {
				xStart = (int) (i0 + l2 * dx);
				y = ymin;
			}

		int xStop = min(i1, xmax, (int) (i0 + (ymax-j0)*dx/dy));

		int e = i0 - i1;
		int ex = 2 * (j1 - j0);
		int ey = 2 * e;
		
		double alpha = (pointsOrdered) ? (i1-xStart)/dx : (xStart-i0)/dx;
		double dalpha = (pointsOrdered) ? -1/dx : 1/dx;
		for (int x = xStart; x < xStop; x++) {
			setPixel(x, y, alpha, pointsOrdered);	
			
			e += ex;
			if (e >= 0 ) {
				y += 1;
				e += ey;
			}
			alpha += dalpha;
		}
	}

	private void drawLine_2ndOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int x = i0;
		int yStart = j0;
		
		double dx = i1 - i0;
		double dy = j1 - j0;
		double l1 = (xmin - i0)/dx;
		double l2 = (ymin - j0)/dy;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				x = xmin;
				yStart = (int) (j0 + l1 * dy);
			} else {
				x = (int) (i0 + l2 * dx);
				yStart = ymin;
			}

		int yStop = min(j1, ymax, (int) (j0 + (xmax-i0)*dy/dx));
		
		int e = j0 - j1;
		int ex = 2 * e;
		int ey = 2 * (i1 - i0);

		double alpha = (pointsOrdered) ? (j1-yStart)/dy : (yStart-j0)/dy;
		double dalpha = (pointsOrdered) ? -1/dy : 1/dy;
		for (int y = yStart; y < yStop; y++) {
			setPixel(x, y, alpha, pointsOrdered);
			e += ey;
			if (e >= 0 ) {
				x += 1;
				e += ex;
			}
			alpha += dalpha;
		}
	}
	
	private void drawLine_3rdOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int x = i0;
		int yStart = j0;
		
		double dx = i1 - i0;
		double dy = j1 - j0;
		double l1 = (xmax - i0)/dx;
		double l2 = (ymin - j0)/dy;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				x = xmax;
				yStart = (int) (j0 + l1 *dy);
			} else {
				x = (int) (i0 + l2 * dx);
				yStart = ymin;
			}

		int yStop = min(j1, ymax, (int) (j0 + (xmin-i0)*dy/dx));
		
		int e = j0 - j1;
		int ex = 2 * e;
		int ey = 2 * (i0 - i1);
		
		double alpha = (pointsOrdered) ? (j1-yStart)/dy : (yStart-j0)/dy;
		double dalpha = (pointsOrdered) ? -1/dy : 1/dy;
		for (int y = yStart; y < yStop; y++) {
			setPixel(x, y, alpha, pointsOrdered);
			e += ey;
			if (e >= 0 ) {
				x -= 1;
				e += ex;
			}
			alpha += dalpha;
		}
	}
	
	private void drawLine_4thOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int xStart = i0;
		int y = j0;
		
		double dx = i1 - i0;
		double dy = j1 - j0;
		double l1 = (xmax - i0)/dx;
		double l2 = (ymin - j0)/dy;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				xStart = xmax;
				y = (int) (j0 + l1 * dy);
			} else {
				xStart = (int) (i0 + l2 * dx);
				y = ymin;
			}

		int xStop = max(i1, xmin, (int) (i0 + (ymax-j0)*dx/dy));
		
		int e = i1 - i0;
		int ex = 2 * (j1 - j0);
		int ey = 2 * e;
		
		double alpha = (pointsOrdered) ? (i1-xStart)/dx : (xStart-i0)/dx;
		double dalpha = (pointsOrdered) ? 1/dx : -1/dx;
		for (int x = xStart; x > xStop; x--) {
			setPixel(x, y, alpha, pointsOrdered);
			e += ex;
			if (e >= 0 ) {
				y += 1;
				e += ey;
			}
			alpha += dalpha;
		}
	}
	
	private void drawHorizontalLine_1stOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int xStart = Math.max(xmin, i0);
		int xStop = Math.min(xmax, i1);
		double dx = i1 -i0;
		
		double alpha = (pointsOrdered) ? (i1-xStart)/dx : (xStart-i0)/dx;
		double dalpha = (pointsOrdered) ? - 1/dx : 1/dx;

		for (int x = xStart; x < xStop; x++) {
			setPixel(x, j0, alpha, pointsOrdered);
			alpha += dalpha;
		}
	}
	
	private void drawVerticalLine_3rdOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		
		int yStart = Math.max(ymin, j0);
		int yStop = Math.min(ymax, j1);
		double dy = j1-j0;

		double alpha = (pointsOrdered) ? (j1-yStart)/dy : (yStart-j0)/dy;
		double dalpha = (pointsOrdered) ? -1/dy : 1/dy;

		for (int y = yStart; y < yStop; y++) {
			setPixel(i0, y, alpha, pointsOrdered);
			alpha += dalpha;
		}
	}

	private void drawDiagonal_2ndOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int x = i0;
		int yStart = j0;
		
		int l1 = xmin - i0;
		int l2 = ymin - j0;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				x = xmin;
				yStart = j0 + l1;
			} else {
				x = i0 + l2;
				yStart = ymin;
			}
		
		int yStop = min(j1, ymax, j0+xmax-i0);
		double dy = j1-j0;

		double alpha = (pointsOrdered) ? (j1-yStart)/dy : (yStart-j0)/dy;
		double dalpha = (pointsOrdered) ? -1/dy : 1/dy;

		for (int y = yStart; y < yStop; y++) {
			setPixel(x, y, alpha, pointsOrdered);
			alpha += dalpha;
			x++;
		}
	}

	private void drawDiagonal_4thOctant(int i0, int j0, int i1, int j1, boolean pointsOrdered) {
		int x = i0;
		int yStart = j0;
		
		int l1 = i0 - xmax;
		int l2 = ymin - j0;
		if (l1 > 0 || l2 > 0)
			if (l1 > l2) {
				x = xmax;
				yStart = j0 + l1;
			} else {
				x = i0 - l2;
				yStart = ymin;
			}
		
		int yStop = min(j1, ymax, j0+i0-xmin);
		double dy = j1-j0;

		double alpha = (pointsOrdered) ? (j1-yStart)/dy : (yStart-j0)/dy;
		double dalpha = (pointsOrdered) ? -1/dy : 1/dy;
		
		for (int y = yStart; y < yStop; y++) {
			setPixel(x, y, alpha, pointsOrdered);
			alpha += dalpha;
			x--;
		}
	}
	
	private static int min(int a, int b, int c){
		return Math.min(Math.min(a, b), c);
	}
	
	private static int max(int a, int b, int c){
		return Math.max(Math.max(a, b), c);
	}
	
}