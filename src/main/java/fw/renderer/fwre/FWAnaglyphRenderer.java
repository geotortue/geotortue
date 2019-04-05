package fw.renderer.fwre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.light.LightingContext;


public abstract class FWAnaglyphRenderer<T extends GPoint> extends FWRenderer3D<T> {

	private final FWRenderer3D<T> auxiliaryRenderer;

	public FWAnaglyphRenderer(RendererSettingsI s, RenderJob<T> job, LightingContext lc) {
		super(s, job, lc);
		auxiliaryRenderer = getAuxiliaryRenderer(s, job, lc);
	}
	
	protected abstract FWRenderer3D<T> getAuxiliaryRenderer(RendererSettingsI s, RenderJob<T> r, LightingContext lc);

	protected void paintOffscreenImage(Graphics g) {
		auxiliaryRenderer.doJob();
		BufferedImage image2 = auxiliaryRenderer.offscreenImage;
		compose(offscreenImage, image2);
		super.paintOffscreenImage(g);
	}

	private void compose(BufferedImage src, BufferedImage dstIn) {
		DataBuffer srcBuf = src.getRaster().getDataBuffer();
		DataBuffer inBuf = dstIn.getRaster().getDataBuffer();
		for (int idx = 0; idx < srcBuf.getSize(); idx++) {
			int c1 = srcBuf.getElem(idx);
			int c2 = inBuf.getElem(idx);
			int a = (((c1 >> 24) + (c2 >> 24)) & 0xFF);
			int r = ((c1 >> 16) & 0xFF);
			int g = ((c2 >> 8) & 0xFF);
			int b = ((c2) & 0xFF);

			int value = (a << 24) | (r << 16) | (g << 8) | (b);

			srcBuf.setElem(idx, value);
		}
	}

	public void reset() {
		super.reset();
		auxiliaryRenderer.reset();
		setOrigin(getOrigin());
	}

	public void setBackground(Color c) {
		super.setBackground(c);
		auxiliaryRenderer.setBackground(c);
	}

	public void setOrigin(Point3D p) {
		super.setOrigin(p);
		auxiliaryRenderer.setOrigin(p);
	}

	public void setPerspective(PerspectiveI p) {
		super.setPerspective(p);
		auxiliaryRenderer.setPerspective(p);
	}

	public void setSize(Dimension d) {
		super.setSize(d);
		if (auxiliaryRenderer != null)
			auxiliaryRenderer.setSize(d);
	}

	public void setSpaceTransform(QRotation r) {
		super.setSpaceTransform(r);
		auxiliaryRenderer.setSpaceTransform(r);
	}

	public void setUnit(double u) {
		super.setUnit(u);
		auxiliaryRenderer.setUnit(u);
		setOrigin(getOrigin());
	}
}