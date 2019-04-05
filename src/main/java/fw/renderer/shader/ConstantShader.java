package fw.renderer.shader;


import java.awt.Color;

import fw.app.Translator.TKey;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;


public class ConstantShader implements ShaderI {
	
	private static final TKey NAME = new TKey(ConstantShader.class, "name");

	private int[] color;
	private final FaceShader fShader;
	
	public ConstantShader() {
		this.fShader = new FaceShader() {
			@Override
			public int[] getRGBA(int a0, int a1, int a2) {
				return color;
			}
		};
	}

	@Override
	public FaceShader getFaceShader(LightingContext lc, Face f) {
		return fShader;
	}

	@Override
	public void prepare(Mesh mesh) {
		Color c = mesh.getColor();
		this.color = new int[]{c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
	}
	
	@Override
	public String getXMLTag() {
		return "ConstantShader";
	}

	@Override
	public TKey getName() {
		return NAME;
	}

}
