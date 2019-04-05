package fw.geometry.util;

import fw.geometry.obj.GPoint;

public class GeodesicTransport {

	private TangentVector endVector;
	private GPoint endPoint;


	public GeodesicTransport(GPoint p, TangentVector v) {
		this.endPoint = p;
		this.endVector = v;
	}
	
	public GPoint getEndPoint() {
		return endPoint;
	}

	public TangentVector getEndVector() {
		return endVector;
	}
}
