package fw.renderer.light;

import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;

public class DirectionalLight extends Light {

	private Point3D direction;
	
	public DirectionalLight() {
		this.direction = new Point3D(0, 0, 1);
	}
	
	public DirectionalLight(Point3D d) throws ZeroVectorException {
		this.direction = MathUtils.getNormalized(d);
	}
	
	@Override
	public double computeDiffuseCoefficient(Point3D normal) {
		double cosTheta = MathUtils.dotProduct(direction, normal);
		if (cosTheta <= 0) {
			return 0;
		}
		return cosTheta;
	}
	
	public Point3D getDirection() {
		return direction;
	}

	public void setDirection(Point3D d) throws ZeroVectorException {
		this.direction = MathUtils.getNormalized(d);
	}
}
