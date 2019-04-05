package geotortue.geometry.proj;

import fw.app.Translator.TKey;
import fw.geometry.proj.LinearPerspective;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveMatrix;




public class GTOrthogonalPerspective extends GTLinearPerspective {
	
	private static final TKey NAME = new TKey(GTOrthogonalPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	
	private final LinearPerspective delegate = new LinearPerspective();

	public GTOrthogonalPerspective() {
		updateMatrix();
	}

	@Override
	protected PerspectiveMatrix getMatrix() {
		return new PerspectiveMatrix(
				1, 0, 0,
				0, 1, 0,
				0, 0, 1);
	}
	
	@Override
	protected PerspectiveI getDelegateProjection() {
		return delegate;
	}

	@Override
	public String getXMLTag() {
		return "GTOrthogonalPerspective";
	}
};