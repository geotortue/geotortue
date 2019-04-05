package fw.renderer.core;

import java.awt.Dimension;
import java.util.ArrayList;

import fw.geometry.obj.GPoint;


public abstract class RendererManager<T extends GPoint> {

	private final RendererI<T>[] availableRenderer;
	private RendererI<T> selectedRenderer;

	private ArrayList<RendererListener> listeners = new ArrayList<>();
	private int index = 0;
	private final RendererSettingsI rendererSettings;
	
	public RendererManager(RendererSettingsI s, RendererI<T>[] rs) {
		this.rendererSettings = s;
		this.availableRenderer = rs;
		selectedRenderer = availableRenderer[0];
	}
	
	public RendererSettingsI getRendererSettings() {
		return rendererSettings;
	}
	
	public RendererI<T>[] getAvailableRenderers() {
		return availableRenderer;
	}

	public RendererI<T> getRenderer() {
		return selectedRenderer;
	}
	
	protected void setRenderer(int idx) {
		if (idx == index)
			return;
		Dimension d = selectedRenderer.getSize();
		selectedRenderer = availableRenderer[idx];
		selectedRenderer.setSize(d);
		this.index = idx;
		for (RendererListener l : listeners)
			l.rendererChanged();
	}
	
	public void updateSettings() {
		selectedRenderer.updateSettings();
	}
	
	public void addListener(RendererListener l) {
		listeners.add(l);
	}
}
