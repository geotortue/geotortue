package fw.renderer.core;

import fw.geometry.obj.GPoint;

public interface RenderJob<T extends GPoint> {

	public void display(RendererI<T> r);
	
}
