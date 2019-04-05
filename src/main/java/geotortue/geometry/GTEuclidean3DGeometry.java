package geotortue.geometry;

import fw.geometry.Euclidean3DGeometry;
import fw.geometry.GeometryI;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.geometry.util.Quaternion;
import fw.geometry.util.TangentVector;
import fw.renderer.MouseManager;
import fw.renderer.mesh.FVMesh;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTVerticesPolygon;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;




public class GTEuclidean3DGeometry extends GTGeometry {

	
	private static final JKey X_FUN = new JKey(GTEuclidean3DGeometry.class, "X");
	private static final JKey Y_FUN = new JKey(GTEuclidean3DGeometry.class, "Y");
	private static final JKey Z_FUN = new JKey(GTEuclidean3DGeometry.class, "Z");
	
	private Euclidean3DGeometry<GTPoint> delegate = new Euclidean3DGeometry<GTPoint>(){
		@Override
		public String getXMLTag() {
			return "GTEuclidean3DGeometry";
		}
	};
	
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
		GTPoint q = t1.getPosition();
		GTPoint p= t2.getPosition();
		try {
			Point3D u = new Point3D(
					q.getU1() - p.getU1(), 
					q.getU2() - p.getU2(),
					q.getU3() - p.getU3()).getNormalized();
			
			if (u.y==1)
				return new GTRotation(new QRotation(new Quaternion(0, -1, 0, 0)));
			
			if (u.y==-1)
				return new GTRotation();
			
			Quaternion quat = new Quaternion(0, u.x, u.y-1, u.z);
			QRotation r = new QRotation(quat);
			return new GTRotation(r);
		} catch (ZeroVectorException ex) {
			ex.printStackTrace();
		}
		return new GTRotation();
	}
	
	public void fill(GTPolygon poly, GTRendererI r) {
		GTVerticesPolygon vPoly = (GTVerticesPolygon) poly;
		FVMesh mesh = vPoly.getMesh(this, r);
		r.draw(mesh);
		mesh.reverseOrientation();
		r.draw(mesh);
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTVerticesPolygon(position, pen);
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
		
		jep.addGFunction(Z_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU3();
			}
		});
	}
}