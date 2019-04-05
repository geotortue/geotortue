/**
 * 
 */
package geotortue.core;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import files.GTUserFileManager;
import function.JFunction;
import function.JFunctionFactory;
import fw.geometry.util.Quaternion;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.TurtleManager.NoSuchTurtleException;
import geotortue.geometry.GTPoint;
import jep2.JEP2;
import jep2.JEP2Exception;
import jep2.JKey;
import type.JNullObject;
import type.JObjectI;
import type.JObjectsVector;
import type.JString;

/**
 * 
 *
 */
public class GTJEPFunctionFactory {
	
	private static final JKey LQUATERNION = new JKey(JFunctionFactory.class, "lquaternion");
	private static final JKey RQUATERNION = new JKey(JFunctionFactory.class, "rquaternion");
	private static final JKey IMPORT = new JKey(JFunctionFactory.class, "import");
	private static final JKey EXPORT = new JKey(JFunctionFactory.class, "export");
	
	private final TurtleManager turtleManager;
	private final GTUserFileManager fileManager;
	
	public GTJEPFunctionFactory(TurtleManager tm, GTUserFileManager fileManager) {
		this.turtleManager = tm;
		this.fileManager = fileManager;
	}

	public Vector<JFunction> getGTFunctions() {
		Vector<JFunction> table = new Vector<>();
		table.add(new QuaternionFunction(LQUATERNION, true));
		table.add(new QuaternionFunction(RQUATERNION, false));
		table.add(new ImportFunction());
		table.add(new ExportFunction());
		return table;
	}
	
	
	/*
	 * Quaternion Functions
	 */

	private class QuaternionFunction extends JFunction {
		private final boolean isLeft;
		
		private QuaternionFunction(JKey key, boolean left){
			super(key, 1);
			this.isLeft = left;
		}
		
		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			int tIdx = (int) popLong(inStack);
			
			try {
				Turtle t = turtleManager.getTurtle(tIdx);
				Quaternion q = (isLeft) ? t.getRotation4D().getQ1() : t.getRotation4D().getQ2();
				JObjectsVector v = new JObjectsVector();
				v.add(JEP2.createNumber(q.getS()));
				v.add(JEP2.createNumber(q.getX()));
				v.add(JEP2.createNumber(q.getY()));
				v.add(JEP2.createNumber(q.getZ()));
				return JEP2.createList(v);
			} catch (NoSuchTurtleException ex) {
				throw new JEP2Exception(this, GTTrouble.GTJEP_TURTLE_INDEX, tIdx+"");
			}
		}
	}
	
	/*
	 * GFunction
	 */
	
	public JFunction createGFunction(JKey key, int numOfParameters, GFunctionI gFunctionI) {
		return new GFunction(key, numOfParameters, gFunctionI);
	}

	
	public interface GFunctionI {
		public double getValue(GTPoint... ps);
	}
	
	private class GFunction extends JFunction {
		private final GFunctionI gFunctionI;
		
		private GFunction(JKey key, int numOfParameters, GFunctionI gFunI){
			super(key, numOfParameters);
			gFunctionI = gFunI;
		}
		
		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			GTPoint[] points = new GTPoint[numberOfParameters];
			for (int idx = 0; idx < numberOfParameters; idx++) {
				int turtleIdx = (int) popDouble(inStack);
				try {
					Turtle t = turtleManager.getTurtle(turtleIdx);
					points[idx] = t.getPosition();
				} catch (NoSuchTurtleException ex) {
					throw new JEP2Exception(this, GTTrouble.GTJEP_TURTLE_INDEX, turtleIdx+"");
				}
			}
			
			double res = gFunctionI.getValue(points);
			return JEP2.createNumber(res);
		}
	}
	
	/*
	 * import / export function
	 */
	
	private class ImportFunction extends JFunction {
		
		private ImportFunction(){
			super(IMPORT, 1);
		}
		
		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JString pathname = popString(inStack);
			String content = fileManager.importFile(pathname.getValue());
			return JEP2.createString(content);
		}
	}
	
	
	private class ExportFunction extends JFunction {
		
		private ExportFunction(){
			super(EXPORT, 2);
		}
		
		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			String pathname = popString(inStack).getValue();
			String content = popString(inStack).getValue();
			
			fileManager.exportFile(pathname, content);
			return JNullObject.NULL_OBJECT;
		}
	}
}