/**
 * 
 */
package fw.renderer.fwre;

import java.awt.Rectangle;
import java.awt.image.WritableRaster;

import fw.geometry.util.Pixel;

public abstract class LineDrawer {
	
	protected final WritableRaster raster;
	protected final Rectangle bounds;
	protected final int xmin, ymin, xmax, ymax;
	protected final ZColorI zColor;
	
	public LineDrawer(WritableRaster raster, ZColorI zColor) {
		this.raster = raster;
		this.zColor = zColor;
		this.bounds = raster.getBounds();
		this.xmin = bounds.x;
		this.xmax = xmin + bounds.width;
		this.ymin = bounds.y;
		this.ymax = ymin + bounds.height;
	}
	
	public abstract void drawLine(Pixel p0, Pixel p1);
	
	public interface ZColorI {
		public abstract int[] getRGBColor(int i, int j, double t);
	}

	public interface LineDrawerFactory {
		public LineDrawer getLineDrawer(WritableRaster raster, ZColorI zColor);
	}
	
	public static LineDrawerFactory createBresenhamLineDrawerFactory() {
		return new LineDrawerFactory() {
			@Override
			public LineDrawer getLineDrawer(WritableRaster raster, ZColorI zColor) {
				return new Bresenham(raster, zColor) ;
			}
		};
	}
	
	public static LineDrawerFactory createXiaolinWuLineDrawerFactory() {
		return new LineDrawerFactory() {
			@Override
			public LineDrawer getLineDrawer(WritableRaster raster, ZColorI zColor) {
				return new XiaolinWu(raster, zColor) ;
			}
		};
	}
}
