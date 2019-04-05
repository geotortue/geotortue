package fw.renderer.shader;

import java.awt.Color;

import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;


public class FlatShader implements ShaderI {

	private static final TKey NAME = new TKey(FlatShader.class, "name");
	
	private Color color;
	private int alpha;
	
	public void prepare(Mesh mesh) {
		this.color = mesh.getColor();
		this.alpha = color.getAlpha();
	}
	
	@Override
	public FaceShader getFaceShader(LightingContext lc, Face f) {
		return new FlatFaceShader(lc, f);
	}

	private class FlatFaceShader implements FaceShader {
	
		private final int[] argb;
		
		public FlatFaceShader(LightingContext lc, Face face) {
			Point3D n = face.getNormal();
			int[] rgb = lc.computeComponent(n, color);
			this.argb = new int[]{rgb[0], rgb[1], rgb[2], alpha}; 
		}
		
		@Override
		public int[] getRGBA(int a0, int a1, int a2) {
			return argb;
		}
	}
	
	@Override
	public String getXMLTag() {
		return "FlatShader";
	}

	@Override
	public TKey getName() {
		return NAME;
	}

}
