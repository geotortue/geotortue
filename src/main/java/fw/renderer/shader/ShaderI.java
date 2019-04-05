package fw.renderer.shader;

import fw.app.Translator.TKey;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.xml.XMLTagged;

public interface ShaderI extends XMLTagged {

	public void prepare(Mesh mesh);
	
	public FaceShader getFaceShader(LightingContext lc, Face f);
	
	public TKey getName();
}