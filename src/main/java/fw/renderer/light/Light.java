package fw.renderer.light;

import fw.geometry.util.Point3D;

public abstract class Light {

	public abstract double computeDiffuseCoefficient(Point3D normal);
}
