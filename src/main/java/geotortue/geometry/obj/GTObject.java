package geotortue.geometry.obj;

import geotortue.geometry.GTGeometryI;
import geotortue.renderer.GTRendererI;

public interface GTObject {
	
//	private final Color color;
//	private final int thickness;
//	
//	public GTObject(TurtlePen pen) {
//		this.color = pen.getColor();
//		this.thickness = pen.getThickness();
//	}
	
	public void draw(GTGeometryI g, GTRendererI r);

//	public Color getColor() {
//		return color;
//	}
//
//	public int getThickness() {
//		return thickness;
//	}
	
}
