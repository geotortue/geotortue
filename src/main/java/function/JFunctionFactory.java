/**
 * 
 */
package function;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.EvaluatorI;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.function.CallbackEvaluationI;

import jep2.JEP2;
import jep2.JEP2Exception;
import jep2.JEP2.JEP2Trouble;
import jep2.JKey;
import type.JObjectI;

/**
 * @author Salvatore Tummarello
 *
 */
public class JFunctionFactory {
	
	private static final JKey SIN = new JKey(JFunctionFactory.class, "sin");
	private static final JKey COS = new JKey(JFunctionFactory.class, "cos");
	private static final JKey TAN = new JKey(JFunctionFactory.class, "tan");
	private static final JKey ASIN = new JKey(JFunctionFactory.class, "asin");
	private static final JKey ACOS = new JKey(JFunctionFactory.class, "acos");
	private static final JKey ATAN = new JKey(JFunctionFactory.class, "atan");
	private static final JKey ATAN2 = new JKey(JFunctionFactory.class, "atan2");
	
	private static final JKey SINH = new JKey(JFunctionFactory.class, "sinh");
	private static final JKey COSH = new JKey(JFunctionFactory.class, "cosh");
	private static final JKey TANH = new JKey(JFunctionFactory.class, "tanh");
	private static final JKey ASINH = new JKey(JFunctionFactory.class, "asinh");
	private static final JKey ACOSH = new JKey(JFunctionFactory.class, "acosh");
	private static final JKey ATANH = new JKey(JFunctionFactory.class, "atanh");

	private static final JKey EXP = new JKey(JFunctionFactory.class, "exp");
	private static final JKey LOG = new JKey(JFunctionFactory.class, "log");
	private static final JKey LN = new JKey(JFunctionFactory.class, "ln");
	private static final JKey SQRT = new JKey(JFunctionFactory.class, "sqrt");
	private static final JKey SQRT2 = new JKey(JFunctionFactory.class, "sqrt2");
	private static final JKey ABS = new JKey(JFunctionFactory.class, "abs");
	
	private static final JKey ROUND = new JKey(JFunctionFactory.class, "round");
	private static final JKey ROUND2 = new JKey(JFunctionFactory.class, "round2");
	
	private static final JKey FLOOR = new JKey(JFunctionFactory.class, "floor");
	private static final JKey FLOOR2 = new JKey(JFunctionFactory.class, "floor2");
	
	private static final JKey MOD = new JKey(JFunctionFactory.class, "mod");
	private static final JKey MOD2 = new JKey(JFunctionFactory.class, "mod2");
	
	private static final JKey RAND = new JKey(JFunctionFactory.class, "rand");
	private static final JKey ALEA = new JKey(JFunctionFactory.class, "alea");
	private static final JKey BINOM = new JKey(JFunctionFactory.class, "binom");

	private static final JKey IF = new JKey(JFunctionFactory.class, "if");
	
	private static final double PI_OVER_180 = Math.PI/180;
	
	public Vector<JFunction> getTrigFunsInDegrees() {
		Vector<JFunction> table = new Vector<>();
		table.add(new CosineD());
		table.add(new SineD());
		table.add(new TangentD());
		table.add(new ArcSineD());
		table.add(new ArcCosineD());
		table.add(new ArcTangentD());
		table.add(new ArcTangent2D());
		return table;
	}

	private abstract class SimpleRealFunction extends JFunction {

		public SimpleRealFunction(JKey key) {
			super(key, 1);
		}

		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double res = getResult(popDouble(inStack));
			return JEP2.createNumber(res);
		}

		public abstract double getResult(double x) throws JEPException;
	}
	
	private abstract class SimpleIntegerFunction extends JFunction {

		public SimpleIntegerFunction(JKey key) {
			super(key, 1);
		}

		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			int res = getResult(popDouble(inStack));
			return JEP2.createNumber(res);
		}

		public abstract int getResult(double x) throws JEPException;
	}


	private class CosineD extends SimpleRealFunction {
		public CosineD() {
			super(COS);
		}
		
		public double getResult(double x) {
			return Math.cos(x*PI_OVER_180);
		}
	}
	
	private class SineD extends SimpleRealFunction {
		public SineD() { 
			super(SIN);
		}
		
		public double getResult(double x) {
			return Math.sin(x*PI_OVER_180);
		}
	}
	
	private class TangentD extends SimpleRealFunction {
		public TangentD() {
			super(TAN);
		}
		
		public double getResult(double x) {
			return Math.tan(x*PI_OVER_180);
		}
	}
	
	private class ArcSineD extends SimpleRealFunction {
		public ArcSineD() {
			super(ASIN);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<-1 || x>1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ASIN, x+"");
			return Math.asin(x)/PI_OVER_180;
		}
	}
	
	
	private class ArcCosineD extends SimpleRealFunction {
		public ArcCosineD() {
			super(ACOS);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<-1 || x>1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ACOS, x+"");
			return Math.acos(x)/PI_OVER_180;
		}
	}
	
	
	private class ArcTangentD extends SimpleRealFunction {
		public ArcTangentD() {
			super(ATAN);
		}
		
		public double getResult(double x) {
			return Math.atan(x)/PI_OVER_180;
		}
	}
	
	private class ArcTangent2D extends JFunction {
		public ArcTangent2D() {
			super(ATAN2, 2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double x = popDouble(inStack);
			double y = popDouble(inStack);
			if (x==0 && y==0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ATAN2);
			double res = Math.atan2(y, x)/PI_OVER_180;
			return JEP2.createNumber(res);
		}
	}

	public Vector<JFunction> getTrigFunsInRadians() {
		Vector<JFunction> table = new Vector<>();
		table.add(new Cosine());
		table.add(new Sine());
		table.add(new Tangent());
		table.add(new ArcSine());
		table.add(new ArcCosine());
		table.add(new ArcTangent());
		table.add(new ArcTangent2());
		return table;
	}

	private class Cosine extends SimpleRealFunction {
		public Cosine() {
			super(COS);
		}
		
		public double getResult(double x) {
			return Math.cos(x);
		}
	}
	
	private class Sine extends SimpleRealFunction {
		public Sine() { 
			super(SIN);
		}
		
		public double getResult(double x) {
			return Math.sin(x);
		}
	}
	
	private class Tangent extends SimpleRealFunction {
		public Tangent() {
			super(TAN);
		}
		
		public double getResult(double x) {
			return Math.tan(x);
		}
	}
	
	private class ArcSine extends SimpleRealFunction {
		public ArcSine() {
			super(ASIN);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<-1 || x>1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ASIN, x+"");
			return Math.asin(x);
		}
	}
	
	
	private class ArcCosine extends SimpleRealFunction {
		public ArcCosine() {
			super(ACOS);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<-1 || x>1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ACOS, x+"");
			return Math.acos(x);
		}
	}
	
	private class ArcTangent extends SimpleRealFunction {
		public ArcTangent() {
			super(ATAN);
		}
		
		public double getResult(double x) {
			return Math.atan(x);
		}
	}
	
	private class ArcTangent2 extends JFunction {
		public ArcTangent2() {
			super(ATAN2, 2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double x = popDouble(inStack);
			double y = popDouble(inStack);
			if (x==0 && y==0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ATAN2);
			double res = Math.atan2(y, x);
			return JEP2.createNumber(res);
		}
	}
	
	public Vector<JFunction> getStandardFuns() {
		Vector<JFunction> table = new Vector<>();
		table.add(new Cosh());
		table.add(new Sinh());
		table.add(new Tanh());
		table.add(new ACosh());
		table.add(new ASinh());
		table.add(new ATanh());
		table.add(new Exp());
		table.add(new Abs());
		table.add(new Sqrt(SQRT));
		table.add(new Sqrt(SQRT2));
		table.add(new Ln());
		table.add(new Log10());
		table.add(new Floor(FLOOR));
		table.add(new Floor(FLOOR2));
		table.add(new Round(ROUND));
		table.add(new Round(ROUND2));
		table.add(new Mod(MOD));
		table.add(new Mod(MOD2));
		
		table.add(new Rand());
		table.add(new Alea());
		
		table.add(new Binomial(BINOM));
		
		table.add(new IfFunction(IF));
		return table;
	}
	
	private class Sinh extends SimpleRealFunction {
		public Sinh() {
			super(SINH);
		}
		
		public double getResult(double x) {
			return Math.sinh(x);
		}
	}
	
	private class Cosh extends SimpleRealFunction {
		public Cosh() {
			super(COSH);
		}
		
		public double getResult(double x) {
			return Math.cosh(x);
		}
	}
	
	private class Tanh extends SimpleRealFunction {
		public Tanh() {
			super(TANH);
		}
		
		public double getResult(double x) {
			return Math.tanh(x);
		}
	}
	
	private class ASinh extends SimpleRealFunction {
		public ASinh() {
			super(ASINH);
		}
		
		public double getResult(double x) {
			return Math.log(x+Math.sqrt(x*x+1));
		}
	}
	
	private class ACosh extends SimpleRealFunction {
		public ACosh() {
			super(ACOSH);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ACOSH, x+"");
			return Math.log(x+Math.sqrt(x*x-1));
		}
	}
	
	private class ATanh extends SimpleRealFunction {
		public ATanh() {
			super(ATANH);
		}
		
		public double getResult(double x) throws JEPException {
			if (Math.abs(x)>=1)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_ATANH, x+"");
			return Math.log((1+x)/(1-x))/2;
		}
	}
	
	private class Exp extends SimpleRealFunction {
		public Exp() {
			super(EXP);
		}
		
		public double getResult(double x) {
			return Math.exp(x);
		}
	}
	
	private class Abs extends SimpleRealFunction {
		public Abs() {
			super(ABS);
		}
		
		public double getResult(double x) {
			return Math.abs(x);
		}
	}

	private class Sqrt extends SimpleRealFunction {
		public Sqrt(JKey key){
			super(key);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_SQRT, x+"");
			return Math.sqrt(x);
		}
	}
	
	private class Ln extends SimpleRealFunction {
		public Ln(){
			super(LN);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<=0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_LOG, x+"");
			return Math.log(x);
		}
	}
	
	private class Log10 extends SimpleRealFunction {
		public Log10(){
			super(LOG);
		}
		
		public double getResult(double x) throws JEPException {
			if (x<=0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_LOG, x+"");
			return Math.log10(x);
		}
	}
	
	private class Floor extends SimpleIntegerFunction {
		public Floor(JKey key) {
			super(key);
		}
		
		public int getResult(double x) {
			return (int) Math.floor(x);
		}
	}
	
	
	private class Round extends JFunction {
		public Round(JKey key) {
			super(key, 2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double y = popDouble(inStack);
			double x = popDouble(inStack);
			double mul = Math.pow(10,y);
			double res = Math.rint(x*mul)/mul;
			return JEP2.createNumber(res);
		}
	}
	
	private class Mod extends JFunction {
		public Mod(JKey key) {
			super(key, 2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			int x = (int) popDouble(inStack);
			if (x==0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_DIV_BY_0);
			int y = (int) popDouble(inStack);
			int res = y%x;
			return JEP2.createNumber(res);
		}
	}
	
	private class Rand extends JFunction {
		public Rand() {
			super(RAND, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double res = Math.random();
			return JEP2.createNumber(res);
		}
	}
	
	private class Alea extends SimpleIntegerFunction {
		public Alea() {
			super(ALEA);
		}
		
		public int getResult(double x) {
			return 1+(int) (Math.random()*((int) x));
		}
	}
	
	private class IfFunction extends JFunction implements CallbackEvaluationI {
		public IfFunction(JKey key){
			super(key, 3);
		}
		
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return null;
		}

		@Override
		public Object evaluate(Node node, EvaluatorI pv) throws ParseException {
			JObjectI<?> condVal = (JObjectI<?>) pv.eval(node.jjtGetChild(0));
			if (JEP2.getBoolean(condVal)) 
				return (JObjectI<?>) pv.eval(node.jjtGetChild(1));
			else
				return  (JObjectI<?>) pv.eval(node.jjtGetChild(2));
		}
	}
}