package fw.renderer.shader;

import java.awt.Color;

import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;


public class MeshShader implements ShaderI {

	private static final TKey NAME = new TKey(MeshShader.class, "name");
	
	private Color color;
	private int alpha;
	private double e = .1;
	
	public void prepare(Mesh mesh) {
		this.color = mesh.getColor();
		this.alpha = color.getAlpha();
	}
	
	@Override
	public FaceShader getFaceShader(LightingContext lc, Face f) {
		return new MeshFaceShader(lc, f);
	}

	private class MeshFaceShader implements FaceShader {
	
		private final int[] argb;
		
		public MeshFaceShader(LightingContext lc, Face face) {
			Point3D n = face.getNormal();
			int[] rgb = lc.computeComponent(n, color);
			this.argb = new int[]{rgb[0], rgb[1], rgb[2], alpha};
		}
		

		
		@Override
		public int[] getRGBA(int a0, int a1, int a2) {
			double e1 = Math.abs((a0+a1+a2)*e);
			if (Math.abs(a0)<=e1 || Math.abs(a1)<=e1 || Math.abs(a2)<=e1)
				return argb;
			return null;
		}
	}
	
	@Override
	public String getXMLTag() {
		return "MeshShader";
	}

	@Override
	public TKey getName() {
		return NAME;
	}
}
