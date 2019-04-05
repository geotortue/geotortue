package geotortue.geometry;

import fw.app.Translator.TKey;
import fw.geometry.FlatQuotientGeometry;
import fw.geometry.GeometryI;
import fw.geometry.obj.GPoint;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
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

public class GTFlatQuotientGeometry extends GTGeometry {
	
	private static final JKey X_FUN = new JKey(GTFlatQuotientGeometry.class, "X");
	private static final JKey Y_FUN = new JKey(GTFlatQuotientGeometry.class, "Y");
	private static final JKey PX_FUN = new JKey(GTFlatQuotientGeometry.class, "pX");
	private static final JKey PY_FUN = new JKey(GTFlatQuotientGeometry.class, "pY");
	private static final TKey NO_FILLING = new TKey(GTFlatQuotientGeometry.class, "fillingUnavailable");
	
	final private FlatQuotientGeometry<GTPoint> delegate = new FlatQuotientGeometry<GTPoint>(600, 400){
		@Override
		public String getXMLTag() {
			return "GTFlatQuotientGeometry";
		}
	};
	
	@Override
	public void fill(GTPolygon fan, GTRendererI renderer) {
		renderer.setErrorMessage(NO_FILLING.translate());
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTPolygon(pen) {
			@Override
			public void add(GTPoint position) {
			}
		};
	}
	
	@Override
	protected GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}

	@Override
	public GTTransport getGTTransport(GTPoint p, TangentVector v, double d) throws GeometryException {
		return new GTTransport(p, p.getTranslated(v.getPoint3D().getScaled(d)));
	}
	
	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		Point3D p = get3DCoordinates(t1.getPosition());
		Point3D q = get3DCoordinates(t2.getPosition());
		double angle = Math.atan2(q.y - p.y, q.x - p.x);
		return GTRotation.getZRotation(angle - Math.PI/2);
	}
	
	@Override
	public void update(MouseManager m) {
		m.setAllAbilitiesAvailable(false);
		m.translationAbility.setAvailable(true);
		m.zoomAbility.setAvailable(true);
	}

	@Override
	public QRotation getOrientationAt(GTPoint gp) {
		int gx = delegate.getxGluingMode() ;
		int gy = delegate.getyGluingMode() ;
		
		if (gx != -1 && gy != -1)
			return super.getOrientationAt(gp);
		
		int xIdx = delegate.getWPeriod(gp) % 2;
		int yIdx = delegate.getHPeriod(gp) % 2;
		
		if (gx == -1 && gy !=-1) {
			if (xIdx==0)
				return super.getOrientationAt(gp);
			return QRotation.getI();
		}
		
		if (gx != -1 && gy ==-1) {
			if (yIdx==0)
				return super.getOrientationAt(gp);
			return QRotation.getJ();
		}
		
		if (xIdx==0 && yIdx==0)
			return super.getOrientationAt(gp);
		if (yIdx==0)
			return QRotation.getI();
		if (xIdx==0)
			return QRotation.getJ();
		return QRotation.getK();
	}

	public void doQTranslation(double x, double y) {
		GPoint p = delegate.getShiftOffset().getTranslated(x, y);
		delegate.setShiftOffset(p);
	}
	
	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW2D;
	}

	/*
	 * JEP
	 */
	

	@Override
	public void addFunctions(GTEnhancedJEP jep) {
		super.addFunctions(jep);
		jep.addGFunction(X_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU1();
			}
		});
		
		jep.addGFunction(Y_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU2();
			}
		});
		
		jep.addGFunction(PX_FUN, 1, new GFunctionI() { 
			@Override
			public double getValue(GTPoint... ps) {
				return get3DCoordinates(ps[0]).x;
			}
		});
		
		jep.addGFunction(PY_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return get3DCoordinates(ps[0]).y;
			}
		});
	}
}