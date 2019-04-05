package fw.renderer.shader;

import java.awt.Color;

import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;


public class PhongShader implements ShaderI {
	
	private static final TKey NAME = new TKey(PhongShader.class, "name");
	
	private Color color;
	private int alpha;

	public void prepare(Mesh mesh) {
		mesh.computeVerticesMeanNormal();
		this.color = mesh.getColor();
		this.alpha = color.getAlpha();
	}
	
	@Override
	public FaceShader getFaceShader(LightingContext lc, Face f) {
		return new PhongFaceShader(lc, f);
	}

	private class PhongFaceShader implements FaceShader {

		private final LightingContext lightingContext;
		private final Point3D n0, n1, n2;
		
		public PhongFaceShader(LightingContext lc, Face face) {
			this.lightingContext = lc;
			this.n0 = face.p0.getNormal();
			this.n1 = face.p1.getNormal();
			this.n2 = face.p2.getNormal();
		}
		
		@Override
		public int[] getRGBA(int a0, int a1, int a2) {
			double nx = a0*n0.x + a1*n1.x + a2*n2.x;
			double ny = a0*n0.y + a1*n1.y + a2*n2.y;
			double nz = a0*n0.z + a1*n1.z + a2*n2.z;
			double s = a0+a1+a2;
			Point3D n = new Point3D(nx/s, ny/s, nz/s);
			
			int[] rgb = lightingContext.computeComponent(n, color);
			int[] argb = new int[4];
			for (int idx = 0; idx < 3; idx++)
				argb[idx] = rgb[idx];
			
			argb[3] = alpha;
			return argb;
		}
	}
	
	@Override
	public String getXMLTag() {
		return "PhongShader";
	}

	@Override
	public TKey getName() {
		return NAME;
	}
}
