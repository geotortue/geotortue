package fw.geometry.obj;

import java.awt.Color;

public class GPen {
	
	private Color color;
	private int thickness;

	public GPen(Color color, int thickness) {
		this.color = color;
		this.thickness = thickness;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getThickness() {
		return thickness;
	}

	public void setThickness(int thickness) {
		if (thickness>0)
			this.thickness = thickness;
	}
}
