/**
 * 
 */
package fw.geometry.obj;

import java.awt.Color;

public class GObject {
	
	private final Color color;
	private final int thickness;

	public GObject(Color c, int t) {
		this.color = c;
		this.thickness = t;
	}

	public Color getColor() {
		return color;
	}
	
	public int getThickness() {
		return thickness;
	}
}
