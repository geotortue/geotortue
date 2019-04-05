/**
 * 
 */
package color;

import java.awt.Color;
import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPTroubleI;

import function.JFunction;
import jep2.JEP2;
import jep2.JEP2Exception;
import jep2.JKey;
import type.JInteger;
import type.JObjectI;
import type.JObjectI.JEP2Type;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTColorFunctionFactory {

	
	private static final JKey RGB = new JKey(GTColorFunctionFactory.class, "rgb");
	private static final JKey HSB = new JKey(GTColorFunctionFactory.class, "hsb");
	
	
	public enum ColorTrouble implements JEPTroubleI {COLOR_HSB_ILLEGAL, COLOR_RGB_ILLEGAL} 
	
	public static Vector<JFunction> getFunctions() {
		Vector<JFunction> table = new Vector<>();
		table.add(new RGBFunction());
		table.add(new HSBFunction());
		return table;
	}
	
	private static class RGBFunction extends JFunction {
		public RGBFunction() {
			super(RGB, 3);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> ob = popJEPObjectI(inStack);
			JObjectI<?> og = popJEPObjectI(inStack);
			JObjectI<?> or = popJEPObjectI(inStack);
			if (or.getType() == JEP2Type.LONG 
					&& og.getType() == JEP2Type.LONG 
					&& ob.getType() == JEP2Type.LONG) {
			
				long r = ((JInteger) or).getValue();
				long g = ((JInteger) og).getValue();
				long b = ((JInteger) ob).getValue();
				if (r<0 || r>255 || g<0 || g>255 || b<0 || b>255)
					throw new JEP2Exception(this, ColorTrouble.COLOR_RGB_ILLEGAL, r + "", g + "", b + "");
				return new GTColor(new Color((int) r, (int) g, (int) b)) ;
			} else {
				double r = JEP2.getDouble(or);
				double g = JEP2.getDouble(og);
				double b = JEP2.getDouble(ob);
				if (r<0 || r>1 || g<0 || g>1 || b<0 || b>1)
					throw new JEP2Exception(this, ColorTrouble.COLOR_RGB_ILLEGAL, r + "", g + "", b + "");
				return new GTColor(new Color((float) r, (float) g, (float) b)) ;
			}
		}
	}

	
	private static class HSBFunction extends JFunction {
		public HSBFunction() {
			super(HSB, 3);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> ob = popJEPObjectI(inStack);
			JObjectI<?> os = popJEPObjectI(inStack);
			JObjectI<?> oh = popJEPObjectI(inStack);
			if (oh.getType() == JEP2Type.LONG 
					&& os.getType() == JEP2Type.LONG 
					&& ob.getType() == JEP2Type.LONG) {
			
				long h = ((JInteger) oh).getValue();
				long s = ((JInteger) os).getValue();
				long b = ((JInteger) ob).getValue();
				if (h<0 || h>360 || s<0 || s>100 || b<0 || b>100)
					throw new JEP2Exception(this, ColorTrouble.COLOR_HSB_ILLEGAL, h + "", s + "", b + "");
				return new GTColor(Color.getHSBColor(h/360f, s/100f, b/100f)) ;
			} else {
				double h = JEP2.getDouble(oh);
				double s = JEP2.getDouble(os);
				double b = JEP2.getDouble(ob);
				if (h<0 || h>1 || s<0 || s>1 || b<0 || b>1)
					throw new JEP2Exception(this, ColorTrouble.COLOR_HSB_ILLEGAL, h + "", s + "", b + "");
				return new GTColor(Color.getHSBColor((float) h, (float) s, (float) b)) ;
			}
		}
	}


}
