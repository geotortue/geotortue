package geotortue.geometry;


import fw.geometry.GeometryI;
import fw.geometry.util.QRotation;
import fw.geometry.util.TangentVector;
import fw.renderer.MouseManager;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.obj.GTArc;
import geotortue.geometry.obj.GTCircle;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.obj.GTString;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;

public interface GTGeometryI extends GeometryI<GTPoint> {
	
	public GTTransport getGTTransport(GTPoint p, TangentVector v, double d) throws GeometryException;
	
	public void teleport(Turtle t, GTPoint p) throws GeometryException, NonFlatPolygonException;
	
	public QRotation getOrientationAt(GTPoint gp);
	
	public void centerWorldOn(GTPoint p, GTRendererI r);
	
	public void resetToNorthOrientation(Turtle t);
	
	public GTRotation getOrientation(Turtle t1, Turtle t2);
	
	public void setParallelOrientation(Turtle ref, Turtle t) throws GeometryException;
	
	public void update(MouseManager m);
	
	public RENDERER_TYPE getRendererType();
	
	public class GeometryException extends Exception {
		private static final long serialVersionUID = -4703133857368740098L;
		private GTTrouble trouble;
		private String[] args;
		
		public GeometryException( GTTrouble trouble, String... args) {
			this.trouble = trouble;
			this.args = args;
		}

		public GTTrouble getTrouble() {
			return trouble;
		}

		public String[] getInfos() {
			return args;
		}
	}
	
	public void draw(GTCircle circle, GTRendererI r);
	
	public void fill(GTCircle circle, GTRendererI r);
	
	public void draw(GTArc arc, GTRendererI r);

	public void draw(GTString str, GTRendererI r);
	
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen);
	
	public void fill(GTPolygon fan, GTRendererI r);
	
	public FrameSupport getFrameSupport();
}