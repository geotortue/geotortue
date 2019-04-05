package fw.renderer.shader;

import java.awt.Color;

import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;


public class GouraudShader implements ShaderI {

	private static final TKey NAME = new TKey(GouraudShader.class, "name");
	
	private Color color;
	private int alpha;
	
	public void prepare(Mesh mesh) {
		mesh.computeVerticesMeanNormal();
		this.color = mesh.getColor();
		this.alpha = color.getAlpha();
	}
	
	@Override
	public FaceShader getFaceShader(LightingContext lc, Face f) {
		return new GouraudFaceShader(lc, f);
	}

	private class GouraudFaceShader implements FaceShader {
	
		private final int[] rgb0, rgb1, rgb2;
		
		public GouraudFaceShader(LightingContext lc, Face face) {
			Point3D n0 = face.p0.getNormal();
			Point3D n1 = face.p1.getNormal();
			Point3D n2 = face.p2.getNormal();
			
			this.rgb0 = lc.computeComponent(n0, color);
			this.rgb1 = lc.computeComponent(n1, color);
			this.rgb2 = lc.computeComponent(n2, color);
		}
		
		@Override
		public int[] getRGBA(int a0, int a1, int a2) {
			int[] argb = new int[4];
			double s = a0+a1+a2;
			for (int idx = 0; idx < 3; idx++) 
				argb[idx] = (int) ((a0*rgb0[idx] + a1*rgb1[idx] + a2*rgb2[idx])/s);
			
			argb[3] = alpha;
			return argb;
		}
	}
	
	@Override
	public String getXMLTag() {
		return "GouraudShader";
	}

	@Override
	public TKey getName() {
		return NAME;
	}

}