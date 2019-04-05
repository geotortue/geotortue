/**
 * 
 */
package geotortue.core;

import java.awt.Color;

import fw.geometry.obj.GPen;

/**
 * Pencil used to draw segments 
 *
 */
public class TurtlePen extends GPen {

	private boolean isDown = true;
	
	public TurtlePen(Color color, int thickness) {
		super(color, thickness);
	}

	public TurtlePen() {
		this(Color.BLACK, 1);
	}
	
	public void setDown(boolean isDown) {
		this.isDown = isDown;
	}
	
	public boolean isDown() {
		return isDown;
	}

	public void reset() {
		setColor(Color.BLACK);
		setThickness(1);
		isDown = true;
	}
}
