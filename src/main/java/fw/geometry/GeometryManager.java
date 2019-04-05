package fw.geometry;

import fw.geometry.obj.GPoint;

public class GeometryManager<T extends GPoint> {
	
	private final GeometryI<T>[] availableGeometries;
	private GeometryI<T> selectedGeometry;

	private int index = 0;
	
	public GeometryManager(GeometryI<T>[] geometries) {
		this.availableGeometries = geometries;
		selectedGeometry = availableGeometries[0];
	}	

	public GeometryI<T>[] getAvailableGeometries() {
		return availableGeometries;
	}

	public GeometryI<T> getGeometry() {
		return selectedGeometry;
	}
	
	protected void setGeometry(int idx) {
		if (idx == index)
			return;
		this.index = idx;
		selectedGeometry = availableGeometries[idx];
	}
	
	protected int getSelectedIndex() {
		return index;
	}
}