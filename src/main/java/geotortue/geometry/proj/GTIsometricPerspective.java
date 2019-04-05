package geotortue.geometry.proj;

import fw.app.Translator.TKey;
import fw.geometry.proj.PerspectiveMatrix;

public class GTIsometricPerspective extends GTLinearPerspective {
	
	private static final TKey NAME = new TKey(GTIsometricPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	
	public GTIsometricPerspective() {
		updateMatrix();
	}

	@Override
	protected PerspectiveMatrix getMatrix() {
		double cosOmega = Math.sqrt(1/2.);
		double sinOmega = Math.sqrt(1/2.);
		double cosAlpha = Math.sqrt(2/3.);
		double sinAlpha = Math.sqrt(1/3.);
		return new PerspectiveMatrix(
				cosOmega, 			  0, 		-sinOmega,
				-sinOmega * sinAlpha, cosAlpha, -cosOmega * sinAlpha,
				 sinOmega * cosAlpha, sinAlpha,  cosOmega * cosAlpha);
	}

	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "GTIsometricPerspective";
	}
}
