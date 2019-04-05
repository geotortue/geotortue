package fw.geometry.obj;

import java.awt.Color;

public class GSegment<T extends GPoint> extends GObject {

	private final T start, end;

	public GSegment(T p0, T p1, Color c, int t) {
		super(c, t);
		this.start = p0;
		this.end = p1;
	}

	public T getStart() {
		return start;
	}

	public T getEnd() {
		return end;
	}
}