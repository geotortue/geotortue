/**
 * 
 */
package function;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPException.JEPTrouble;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;
import jep2.JEP2Exception;
import jep2.JKey;
import type.JDict;
import type.JHashTable;
import type.JIterable;
import type.JNumber;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class JCastFunctionFactory {
	
	private static final JKey LIST = new JKey(JCastFunctionFactory.class, "list");
	private static final JKey STR = new JKey(JCastFunctionFactory.class, "str");
	private static final JKey BOOL = new JKey(JCastFunctionFactory.class, "bool");
	private static final JKey INT = new JKey(JCastFunctionFactory.class, "int");
	private static final JKey FLOAT = new JKey(JCastFunctionFactory.class, "float");
	private static final JKey TYPE = new JKey(JCastFunctionFactory.class, "type");
	private static final JKey DICT = new JKey(JCastFunctionFactory.class, "dict");

	public Vector<JFunction> getFunctions() {
		Vector<JFunction> v = new Vector<>();
		v.add(new DictCast());
		v.add(new ListCast());
		v.add(new StringCast());
		v.add(new IntCast());
		v.add(new FloatCast());	
		v.add(new BooleanCast());
		v.add(new Type());
		return v;
	}

	private abstract class AbstractCastFunction extends JFunction {

		public AbstractCastFunction(JKey key) {
			super(key, -1);
		}

		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			if (curNumberOfParameters==0)
				return getDefaultObject();
			if (curNumberOfParameters==1)
				return getResult(popJEPObjectI(inStack));
			throw new JEP2Exception(this, JEPTrouble.JEP_ILLEGAL_NUMBER_OF_ARGUMENTS, getName(), "1", curNumberOfParameters+"");
		}

		public abstract JObjectI<?> getDefaultObject();

		public abstract JObjectI<?> getResult(JObjectI<?> x) throws JEPException;
	}

	private class IntCast extends AbstractCastFunction {

		public IntCast() {
			super(INT);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createNumber(0L);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			JNumber<?> n;
			try {
				n = JEP2.createNumber(o);
			} catch(JEPException ex) {
				throw new JEP2Exception(this, JEP2Trouble.JEP2_CAST_ERROR, getName(), o.toString());
			}

			Double v = n.doubleValue();
			if (Math.abs(v) > Long.MAX_VALUE)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_CAST_ERROR, getName(), v.toString());
			return JEP2.createNumber(v.longValue());
			
		}
	}
	
	private class FloatCast extends AbstractCastFunction {

		public FloatCast() {
			super(FLOAT);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createNumber(0.0);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			try {
				return JEP2.createNumber(o);
			} catch(JEPException ex) {
				throw new JEP2Exception(this, JEP2Trouble.JEP2_CAST_ERROR, getName(), o.toString());
			}
		}
	}
	
	private class BooleanCast extends AbstractCastFunction {

		public BooleanCast() {
			super(BOOL);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createBoolean(false);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			if (o.isANumber()) {
				JNumber<?> n = (JNumber<?>) o;
				return (n.doubleValue()==0) ? JEP2.createBoolean(false) : JEP2.createBoolean(true);
			}
			if (o.isIterable()) {
				JIterable it = (JIterable) o;
				return (it.getItems().isEmpty()) ? JEP2.createBoolean(false) : JEP2.createBoolean(true);
			}
			throw new JEP2Exception(this, JEP2Trouble.JEP2_CAST_ERROR, getName(), o.toString());
		}
	}
	
	private class ListCast extends AbstractCastFunction {

		public ListCast() {
			super(LIST);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			if (!o.isIterable())
				throw new JEP2Exception(this, JEP2Trouble.JEP2_CAST_ERROR, getName(), o.toString());
			JObjectsVector v = new JObjectsVector();
			for (JObjectI<?> obj : ((JIterable) o).getItems()) 
				v.add(obj);
			return JEP2.createList(v);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createList(new JObjectsVector());
		}
	}
	
	private class StringCast extends AbstractCastFunction {

		public StringCast() {
			super(STR);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			return JEP2.createString(o.toString());
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createString("");
		}
	}
	
	private class DictCast extends AbstractCastFunction {

		public DictCast() {
			super(DICT);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			if (o.getType() == JEP2Type.DICT) {
				JHashTable t = ((JDict) o).getValue();
				return JEP2.createDict(new JHashTable(t));
			}
			JIterable it = JEP2.getIterable(o);
			JHashTable t = new JHashTable();
			for (JObjectI<?> obj : it.getItems()) {
				JIterable kv = JEP2.getIterable(obj);
				JObjectsVector items = kv.getItems();
				if (items.size()!=2)
					throw new JEP2Exception(this, JEP2Trouble.JEP2_DICT_ERROR, getName(), kv.toString());
				t.put(items.elementAt(0), items.elementAt(1));
			}
			return JEP2.createDict(t);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createDict(new JHashTable());
		}
	}
	
	private class Type extends AbstractCastFunction {

		public Type() {
			super(TYPE);
		}

		@Override
		public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
			String s = "<type '";
			String t = "";
			switch (o.getType()) {
			case LONG:
				t = "int";
				break;
			case BOOLEAN:
				t = "bool";
				break;
			case DOUBLE:
				t = "float";
				break;
			case LIST:
				t = "list";
				break;
			case NULL:
				t = "null";
				break;
			case STRING:
				t = "str";
				break;
			case SLICER:
				t = "slicer";
				break;
			case METHOD:
				t = "method";
				break;
			case ASSIGNMENT:
				t = "assignment";
				break;
			case DICT:
				t = "dict";
				break;
			case MUSIC:
				t = "music event";
				break;
			case COLOR:
				t = "color";
				break;
			}
			s += t+"'>";
			return JEP2.createString(s);
		}

		@Override
		public JObjectI<?> getDefaultObject() {
			return JEP2.createString("");
		}
	}
}