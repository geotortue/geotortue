package fw.renderer.light;

import java.awt.Color;
import java.util.ArrayList;

import fw.geometry.util.Point3D;

public abstract class LightingContext {

	public abstract ArrayList<Light> getLights();

	public abstract double getAmbientComponent();

	public int[] computeComponent(Point3D normal, Color c) {
		double coeff = getAmbientComponent();
		for (Light light : getLights())
			coeff += light.computeDiffuseCoefficient(normal);

		int r = multiply(c.getRed(), coeff);
		int g = multiply(c.getGreen(), coeff);
		int b = multiply(c.getBlue(), coeff);
		int a = c.getAlpha();
		
		return new int[]{r, g, b, a};
	}
	
	private int multiply(int c, double coeff) {
		int c1 = (int) (c * coeff);
		if (c1>255)
			return 255;
		if (c1<0)
			return 0;
		return c1;
	}
	
}