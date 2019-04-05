package fw.renderer.core;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fw.geometry.GeometryI;
import fw.geometry.GeometryManager;
import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveManager;
import fw.gui.FWSettings;

public abstract class GraphicSpace<T extends GPoint> implements FWSettings {

	protected final GeometryManager<T> geometryManager;
	protected final PerspectiveManager perspectiveManager;
	protected final RendererManager<T> rendererManager;
	private Dimension size = new Dimension(640, 480);
	
	public GraphicSpace(GeometryManager<T> gm, RendererManager<T> rm, PerspectiveManager pm) {
		this.geometryManager = gm;
		this.perspectiveManager = pm;
		this.rendererManager = rm;
		
		update();
	}
	
	/**
	 * @param gs
	 */
	public GraphicSpace(GraphicSpace<T> gs) {
		this(gs.geometryManager, gs.rendererManager, gs.perspectiveManager);
	}

	public void update() {
		rendererManager.updateSettings();
		RendererI<T> renderer = rendererManager.getRenderer();
		PerspectiveI perspective = perspectiveManager.getPerspective();
		renderer.setPerspective(perspective);
		renderer.setSize(size);
		
		GeometryI<T> geometry = geometryManager.getGeometry();		
		geometry.init(renderer);
		
		repaint();
	}
	
	public Dimension getSize() {
		return size;
	}
	
	public void setSize(Dimension d){
		if (size.equals(d))
			return;
		size = d;
		rendererManager.getRenderer().setSize(size);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame owner =  (JFrame) getPane().getTopLevelAncestor();
				if (owner != null)
					owner.validate();
			}
		});
			
	}

	public void setWidth(int width) {
		setSize(new Dimension(width, size.height));
	}

	public void setHeight(int height) {
		setSize(new Dimension(size.width, height));
	}
	
	public JPanel getPane() {
		return  rendererManager.getRenderer().getPane();
	}

	public final void repaint() {
		getPane().repaint();
	}
	
}