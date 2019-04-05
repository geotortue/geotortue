package geotortue.geometry;

import fw.geometry.GeometryI;
import fw.geometry.HyperbolicGeometry;
import fw.geometry.HyperboloidGeometry;
import fw.geometry.util.TangentVector;
import fw.renderer.MouseManager;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.obj.GTPolygon;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;

public class GTHyperboloidGeometry extends GTGeometry {
	
	private static final JKey U_FUN = new JKey(GTHyperboloidGeometry.class, "U");
	private static final JKey V_FUN = new JKey(GTHyperboloidGeometry.class, "V");
	// TODO : zzz GTHyperboloidGeometry & HyperboloidGeometry
	

	final private HyperbolicGeometry<GTPoint> delegate = new HyperboloidGeometry<GTPoint>()	{
		public String getXMLTag() {
			return "GTHyperboloidGeometry";
		}
		
	};
	
	
	@Override
	public void fill(GTPolygon fan, GTRendererI renderer) {
	}

	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTPolygon(pen) {
			@Override
			public void add(GTPoint p) {
			}
		};
	}
	
	@Override
	protected GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}

	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		return new GTRotation();
	}

	@Override
	public GTTransport getGTTransport(GTPoint gp, TangentVector v, double d) throws GeometryException {
		return null;
	}
	
	public final void update(MouseManager m) {
		m.setAllAbilitiesAvailable(true);
	}
	
	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW3D;
	}

	/*
	 * JEP
	 */
	

	@Override
	public void addFunctions(GTEnhancedJEP jep) {
		super.addFunctions(jep);
		jep.addGFunction(U_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU1();
			}
		});
		
		jep.addGFunction(V_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU2();
			}
		});
	}

}