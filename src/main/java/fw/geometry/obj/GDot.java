package fw.geometry.obj;

import java.awt.Color;

public class GDot<T extends GPoint> extends GObject {

	private final T position;

	public GDot(T p, Color c, int t) {
		super(c, t);
		this.position = p;
	}

	public T getPosition() {
		return position;
	}
}