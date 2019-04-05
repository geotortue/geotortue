/**
 * 
 */
package jep2;

import java.util.Vector;

import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.TokenMgrError;
import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPException.JEPTrouble;
import org.nfunk.jep.addon.JEPTroubleI;

import function.JCastFunctionFactory;
import function.JFunction;
import function.JFunctionFactory;
import function.JIterableFunctionFactory;
import function.JMethodFactory;
import function.JSpecialFunctionFactory;
import function.OperatorSet2;
import fw.geometry.util.MathException;
import type.JBoolean;
import type.JHashTable;
import type.JInteger;
import type.JIterable;
import type.JList;
import type.JNullObject;
import type.JNumber;
import type.JNumberFactory;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;
import type.JString;

/**
 * @author Salvatore Tummarello
 *
 */
public class JEP2 extends JEP {
	
	public enum JEP2Trouble implements JEPTroubleI {
		JEP2_0_POW_0, 
		JEP2_ACOS, 
		JEP2_ACOSH, 
		JEP2_ASIN, 
		JEP2_ATAN2, 
		JEP2_ATANH, 
		JEP2_BINOM, 
		JEP2_CANNOT_CHANGE_A_CONSTANT, 
		JEP2_CAST_ERROR, 
		JEP2_DICT_ERROR,
		JEP2_DIV_BY_0, 
		JEP2_ELEMENT_AT_EMPTY_LIST, 
		JEP2_ILLEGAL_PROD, 
		JEP2_ILLEGAL_SUM, 
		JEP2_INF,
		JEP2_INVALID_KEY,
		JEP2_LIST_INDEX_OUT,
		JEP2_LIST_OVERFLOW,
		JEP2_LOG, 
		JEP2_NO_METHOD,
		JEP2_NOT_AN_INT, 
		JEP2_NOT_A_BOOLEAN,
		JEP2_NOT_HASHABLE,
		JEP2_NOT_A_LIST, 
		JEP2_NOT_A_METHOD, 
		JEP2_NOT_A_NUMBER, 
		JEP2_NOT_A_STRING, 
		JEP2_NOT_ITERABLE, 
		JEP2_RANGE, 
		JEP2_SQRT, 
		JEP2_STRING_ASSIGNMENT,
		JEP2_UNEXPECTED_ERROR, 
		JEP2_ZERO_STEP,   
	}
	
	private static JFunctionFactory jFunctionFactory =  new JFunctionFactory();
	public enum AngleMode {Degree, Radian};
	private AngleMode mode = AngleMode.Degree;
	
	private final static Vector<JFunction> STANDARD_FUNCTIONS = new Vector<>(); 
	
	private final static OperatorSet2 OP_SET = new OperatorSet2();
	private final static JNumberFactory NUMBER_FACTORY = new JNumberFactory();
	
	public JEP2() {
		opSet = OP_SET;
		numberFactory = NUMBER_FACTORY;
		//numberFactory = new JAPFFactory();
		setAllowUndeclared(true);
		setAllowAssignment(true);
		setImplicitMul(true);
		
		for (JFunction fun : new JCastFunctionFactory().getFunctions()) 
			STANDARD_FUNCTIONS.add(fun);

		for (JFunction m : new JMethodFactory().getListMethods()) 
			STANDARD_FUNCTIONS.add(m);
		
		for (JFunction fun : new JIterableFunctionFactory().getFunctions()) 
			STANDARD_FUNCTIONS.add(fun);
		
		for (JFunction fun : new JSpecialFunctionFactory().getFunctions()) 
			STANDARD_FUNCTIONS.add(fun);
		
		for (JFunction fun : new JFunctionFactory().getStandardFuns()) 
			STANDARD_FUNCTIONS.add(fun);
	}
	
	public JEP2(JEP2 j) {
		this();
		SymbolTable jSymTab = j.getSymbolTable();
		for (Object name : jSymTab.keySet()) 
			symTab.addVariable((String) name, jSymTab.getValue(name));
		
		funTab = j.getFunctionTable();
	}

	public AngleMode getMode() {
		return mode;
	}
	
	public void setMode(AngleMode mode) {
		this.mode = mode;
		Vector<JFunction> funs = (mode == AngleMode.Degree) ? 
				jFunctionFactory.getTrigFunsInDegrees() : jFunctionFactory.getTrigFunsInRadians();
		for (JFunction fun : funs) 
			addFunction(fun);
	}
	
	@Override
	public void addStandardFunctions() {
		initFunTab();
		for (JFunction fun : STANDARD_FUNCTIONS) 
			addFunction(fun);

		setMode(mode); // add trigonometric functions
	}
	
	
	
	@Override
	public void addStandardConstants() {
		symTab.addConstant(JNullObject.NULL_OBJECT+"", JNullObject.NULL_OBJECT);
	}
	
	public void addFunction(JFunction function) {
		String functionName = function.getName();
		super.addFunction(functionName, function);
	}

	
	@Override
	public JObjectI<?> getValueAsObject() throws JEPException {
		Object o;
		try {
			o = super.getValueAsObject();
		} catch (JEPException ex) {
			removeInvalidVariables();
			throw ex;
		}
		if (o==null) 
			return JNullObject.NULL_OBJECT;
		
		if (o instanceof JObjectI<?>)
			return (JObjectI<?>) o;
		if (o instanceof String)
			return JEP2.createString((String) o);
		new Exception("JEP2.getValueAsObject() : "+o+" of "+o.getClass()+" is not a JObject !").printStackTrace();
		return JNullObject.NULL_OBJECT;
	}

	private void removeInvalidVariables() {
		Vector<String> invalids = new Vector<>();
		for (Object obj : symTab.keySet()) {
			String key = (String) obj;
			if (!(symTab.getVar(key)).hasValidValue()) 
				invalids.add(key);
		}

		for (String key : invalids) 
			symTab.remove(key);
		
	}

	public JObjectI<?> getValueOf(String text) throws JEPException {
		try {
			parseExpression(text);
		} catch (TokenMgrError er){
			String msg = er.getMessage();
			int idx = msg.indexOf("Encountered: ");
			if (idx<0)
				er.printStackTrace();
			else { 
				msg = msg.substring(idx);
				int start = msg.indexOf("\"");
				int end = msg.indexOf("\"", start+1);
				msg = msg.substring(start, end);
			}
			throw new JEPException(JEPTrouble.JEP_TOKEN_ERROR, msg);
		}

		JObjectI<?> o = getValueAsObject();
		
		if (!hasError()) {
			if (o.isANumber()) {
				double v = ((JNumber<?>) o).doubleValue();
				if (Double.isInfinite(v))
					throw new JEPException(JEP2Trouble.JEP2_INF, text);
			}
			
			return o;
		}
		
		if (exception != null) 
			throw new JEPException(exception);
		else {
			new Exception().printStackTrace();
			throw new JEPException(JEP2Trouble.JEP2_UNEXPECTED_ERROR);
		}
	}
	
	@Override
	public final Node parseExpression(String expressionIn) {
		String str = replaceSpecialSymbolsOutOfQuotes(expressionIn);
		return super.parseExpression(str); 
	}
	
	private String replaceSpecialSymbolsOutOfQuotes(String expressionIn) {
		int quoteStart = expressionIn.indexOf("\"");
		if (quoteStart<0)
			return replaceSpecialSymbols(expressionIn);

		String str = "";
		int len = expressionIn.length();
		int quoteEnd = 0;
		
		while (quoteStart>=0) {
			if (quoteStart>quoteEnd)
				str += replaceSpecialSymbols(expressionIn.substring(quoteEnd, quoteStart));
			
			quoteEnd = getQuoteIndex(expressionIn, quoteStart+1);
			if (quoteEnd<0) 
				quoteEnd = len;
			else
				quoteEnd++;
			
			str += expressionIn.substring(quoteStart, quoteEnd);
			quoteStart = getQuoteIndex(expressionIn, quoteEnd);
			
		}
		
		if (quoteEnd<len)
			str += expressionIn.substring(quoteEnd, len);
		
		return str;
	}
	
	private int getQuoteIndex(String str, int offset) {
		int idx = str.indexOf('"', offset);
		if (idx<=0)
			return idx;
		if (str.charAt(idx-1)!='\\')
				return idx;
		return getQuoteIndex(str, idx+1);
	}
	
	protected String replaceSpecialSymbols(String expressionIn) {
		String str = expressionIn.replaceAll("\\[\\s*\\]", "["+JNullObject.NULL_OBJECT+"]"); // constructor [] -> [null]
		str = str.replaceAll("\\[\\s*\\:", "["+JNullObject.NULL_OBJECT+":");	// L[:a] -> L[null:a]
		str = str.replaceAll("\\:\\s*\\]", ":"+JNullObject.NULL_OBJECT+"]");	// L[a:] -> L[a:null]
		str = str.replaceAll("\\:\\s*\\:", ":"+JNullObject.NULL_OBJECT+":");	// L[a::b] -> L[a:null:b]
		str = str.replaceAll("(\\.\\D)", " $1"); // workaround for dot operator : L.sort() -> L .sort()
		return str;
	}
	
	public static JNumber<?> createNumber(JObjectI<?> o) throws JEPException {
		if (o.isANumber()) 
			return (JNumber<?>) o;
		if (o.getType() == JEP2Type.STRING) {
			JString s = (JString) o;
			return NUMBER_FACTORY.createNumber(s.getValue());
		}
		throw new JEPException(JEP2Trouble.JEP2_NOT_A_NUMBER, o.toString());
	}

	public static JNumber<?> createNumber(double value) {
		return NUMBER_FACTORY.createNumber(value);
	}

//	public static JNumber<?> createNumber(Number value) {
//		return NUMBER_FACTORY.createNumber(value);
//	}

	public static JNumber<?> createNumber(boolean value) {
		return NUMBER_FACTORY.createNumber(value);
	}

	public static JNumber<?> createNumber(float value) {
		return NUMBER_FACTORY.createNumber(value);
	}

	public static JNumber<?> createNumber(long value) {
		return NUMBER_FACTORY.createNumber(value);
	}
	
	public static JNumber<?> createNumber(short value) {
		return NUMBER_FACTORY.createNumber(value);
	}

	public static JObjectI<?> createList(JObjectsVector v) {
		return NUMBER_FACTORY.createList(v);
	}

	public static JObjectI<?> createString(String s) {
		return NUMBER_FACTORY.createString(s);
	}

	public static JObjectI<?> createBoolean(boolean b) {
		return NUMBER_FACTORY.createBoolean(b);
	}
	
	public static JObjectI<?> createDict(JHashTable t) {
		return NUMBER_FACTORY.createDict(t);
	}

	@Override
	public String toString() {
		String s = "";
		s += "\n"+this.allowUndeclared;
		s += "\n"+this.allowAssignment;
		s += "\n"+this.implicitMul;
		s += "\n"+this.ev;
		s += "\n"+this.funTab;
		s += "\n"+this.opSet ;
		s += "\n"+this.numberFactory ;
		s += "\n"+this.parser ;
		s += "\n"+this.symTab ;
		s += "\n"+this.errorList ;
		return super.toString()+" "+s;
	}

	public static long getLong(JObjectI<?> o) throws JEPException {
		if (o.getType() != JEP2Type.LONG)
			throw new JEPException(JEP2Trouble.JEP2_NOT_AN_INT, o.toString());
		return ((JInteger) o).getValue();
	}

	public static int getInteger(JObjectI<?> o) throws JEPException, MathException {
		long l = getLong(o);
		int i = (int) l;
		if (i!=l)
			throw new MathException("long to int conversion error");
		return i;
	}
	
	public static double getDouble(JObjectI<?> o) throws JEPException {
		if (!o.isANumber())
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_NUMBER, o.toString());
		return ((JNumber<?>) o).doubleValue();
	}

	public static JNumber<?> getNumber(JObjectI<?> o) throws JEPException {
		if (!o.isANumber())
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_NUMBER, o.toString());
		return (JNumber<?>) o;
	}
	
	public static boolean getBoolean(JObjectI<?> o) throws JEPException {
		if (o.getType() == JEP2Type.BOOLEAN)
			return ((JBoolean) o).getValue();
		if (!o.isANumber())
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_BOOLEAN, o.toString());
		return ( (JNumber<?>) o).doubleValue() != 0;
	}

	public static JList getList(JObjectI<?> o) throws JEPException {
		if (o.getType() != JEP2Type.LIST)
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_LIST, o.toString());
		return (JList) o;
	}
	
	public static JString getString(JObjectI<?> o) throws JEPException {
		if (o.getType() != JEP2Type.STRING)
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_STRING, o.toString());
		return (JString) o;
	}
	
	public static JIterable getIterable(JObjectI<?> o) throws JEPException {
		if (!o.isIterable())
			throw new JEPException(JEP2Trouble.JEP2_NOT_ITERABLE, o.toString());
		return (JIterable) o;
	}
}