/**
 * 
 */
package function;

import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import fw.geometry.util.MathException;
import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;
import jep2.JEP2Exception;
import jep2.JKey;
import type.JDict;
import type.JList;
import type.JMethod;
import type.JNullObject;
import type.JObjectHelper;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;
import type.JString;

/**
 * @author Salvatore Tummarello
 *
 */
public class JMethodFactory {

	private static final JKey SORT = new JKey(JIterableFunctionFactory.class, "sort");
	private static final JKey REVERSE = new JKey(JIterableFunctionFactory.class, "reverse");
	private static final JKey POP = new JKey(JIterableFunctionFactory.class, "pop");
	private static final JKey INSERT = new JKey(JIterableFunctionFactory.class, "insert");
	private static final JKey COUNT = new JKey(JIterableFunctionFactory.class, "count");
	private static final JKey INDEX = new JKey(JIterableFunctionFactory.class, "index");
	private static final JKey APPEND = new JKey(JIterableFunctionFactory.class, "append");
	private static final JKey REMOVE = new JKey(JIterableFunctionFactory.class, "remove");
	private static final JKey KEYS = new JKey(JIterableFunctionFactory.class, "keys");
	private static final JKey VALUES = new JKey(JIterableFunctionFactory.class, "values");

	public Vector<JFunction> getListMethods() {
		Vector<JFunction> v = new Vector<>();
		v.add(new SortFunction());
		v.add(new ReverseFunction());
		v.add(new PopFunction());
		v.add(new InsertFunction());
		v.add(new CountFunction());
		v.add(new IndexFunction());
		v.add(new AppendFunction());
		v.add(new RemoveFunction());
		v.add(new KeysFunction());
		v.add(new ValuesFunction());
		return v;
	}

	private class ReverseMethod extends JFunction {

		public ReverseMethod() {
			super(REVERSE, -1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.LIST)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", REVERSE.translate());

			JList l = (JList) o;
			JObjectsVector v = l.getItems();
			Collections.reverse(v);
			l.notifyListeners();

			return JNullObject.NULL_OBJECT;
		}
	}

	private class ReverseFunction extends JFunction {

		public ReverseFunction() {
			super(REVERSE, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return new JMethod(new ReverseMethod());
		}
	}

	private class PopMethod extends JFunction {

		private final JObjectI<?> key;

		public PopMethod(JObjectI<?> o) {
			super(POP, 1);
			this.key = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() == JEP2Type.DICT)
				return pop((JDict) o);
			else if (o.getType() == JEP2Type.LIST) {
				try {
					return pop((JList) o);
				} catch (MathException | ArrayIndexOutOfBoundsException ex) {
					throw new JEP2Exception(this, JEP2Trouble.JEP2_LIST_INDEX_OUT, key + "");
				}
			} else
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", POP.translate());
		}

		private JObjectI<?> pop(JList l) throws JEPException, MathException {
			JObjectsVector v = l.getItems();
			int idx = (key == JNullObject.NULL_OBJECT) ? -1 : JEP2.getInteger(key);
			idx = (idx >= 0) ? idx : idx + v.size();
			JObjectI<?> r = v.remove(idx);
			l.notifyListeners();
			return r;
		}

		private JObjectI<?> pop(JDict d) throws JEPException {
			JObjectI<?> r = d.remove(key);
			d.notifyListeners();
			return r;
		}

	}

	private class PopFunction extends JFunction {

		public PopFunction() {
			super(POP, -1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			if (curNumberOfParameters == 1) {
				return new JMethod(new PopMethod(popJEPObjectI(inStack)));
			} else
				return new JMethod(new PopMethod(JNullObject.NULL_OBJECT));
		}
	}

	private class InsertMethod extends JFunction {

		private final int index;
		private final JObjectI<?> obj;

		public InsertMethod(int idx, JObjectI<?> o) {
			super(INSERT, 1);
			this.index = idx;
			this.obj = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.LIST)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", INSERT.translate());

			JList l = (JList) o;
			JObjectsVector v = l.getItems();
			try {
				int idx = (index >= 0) ? index : index + v.size();
				v.insertElementAt(obj, idx);
				l.notifyListeners();
			} catch (ArrayIndexOutOfBoundsException ex) {
				throw new JEP2Exception(this, JEP2Trouble.JEP2_LIST_INDEX_OUT, index + "");
			}
			return JNullObject.NULL_OBJECT;
		}
	}

	// TODO : (done) L.insert(3, "T") -> rafraîchir l'affichage du moniteur

	private class InsertFunction extends JFunction {

		public InsertFunction() {
			super(INSERT, 2);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			long idx = popLong(inStack);
			if (idx > Integer.MAX_VALUE)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_LIST_INDEX_OUT, idx + "");
			return new JMethod(new InsertMethod((int) idx, o));
		}
	}

	private class CountMethod extends JFunction {

		private final JObjectI<?> obj;

		public CountMethod(JObjectI<?> o) {
			super(COUNT, 1);
			this.obj = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() == JEP2Type.LIST) {
				JObjectsVector v = ((JList) o).getItems();
				int count = 0;

				for (JObjectI<?> ob : v)
					if (ob.getValue().equals(obj.getValue()))
						count++;

				return JEP2.createNumber(count);
			} else if (o.getType() == JEP2Type.STRING) {
				String pattern = JEP2.getString(obj).getValue();
				String str = ((JString) o).getValue();

				int len = pattern.length();
				int count = 0;
				int idx = str.indexOf(pattern);
				while (idx >= 0) {
					count++;
					idx = str.indexOf(pattern, idx + len);
				}

				return JEP2.createNumber(count);
			} else
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", COUNT.translate());
		}
	}

	private class CountFunction extends JFunction {

		public CountFunction() {
			super(COUNT, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			return new JMethod(new CountMethod(o));
		}
	}

	private class SortMethod extends JFunction {

		public SortMethod() {
			super(SORT, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.LIST)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", SORT.translate());

			JList l = (JList) o;
			JObjectsVector v = l.getItems();
			Collections.sort(v, new Comparator<JObjectI<?>>() {

				@Override
				public int compare(JObjectI<?> o1, JObjectI<?> o2) {
					return JObjectHelper.compare(o1, o2);
				}

			});
			l.notifyListeners();
			return JNullObject.NULL_OBJECT;
		}
	}

	private class SortFunction extends JFunction {
		public SortFunction() {
			super(SORT, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return new JMethod(new SortMethod());
		}
	}

	private class IndexMethod extends JFunction {

		private final JObjectI<?> obj;

		public IndexMethod(JObjectI<?> o) {
			super(INDEX, 1);
			this.obj = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() == JEP2Type.LIST) {
				JObjectsVector v = ((JList) o).getItems();

				int count = 0;
				for (JObjectI<?> ob : v) {
					if (ob.getValue().equals(obj.getValue()))
						return JEP2.createNumber(count);
					count++;
				}
				return JEP2.createNumber(-1);
			} else if (o.getType() == JEP2Type.STRING) {
				String pattern = JEP2.getString(obj).getValue();
				String str = ((JString) o).getValue();

				int idx = str.indexOf(pattern);
				return JEP2.createNumber(idx);
			} else
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", INDEX.translate());
		}
	}

	private class IndexFunction extends JFunction {
		public IndexFunction() {
			super(INDEX, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			return new JMethod(new IndexMethod(o));
		}
	}

	private class AppendMethod extends JFunction {

		private final JObjectI<?> obj;

		public AppendMethod(JObjectI<?> o) {
			super(APPEND, 1);
			this.obj = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.LIST)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", APPEND.translate());

			JList l = (JList) o;
			JObjectsVector v = l.getItems();
			v.add(obj);
			l.notifyListeners();
			return JNullObject.NULL_OBJECT;
		}
	}

	private class AppendFunction extends JFunction {
		public AppendFunction() {
			super(APPEND, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			return new JMethod(new AppendMethod(o));
		}
	}

	private class RemoveMethod extends JFunction {

		private final JObjectI<?> obj;

		public RemoveMethod(JObjectI<?> o) {
			super(REMOVE, 1);
			this.obj = o;
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.LIST)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", REMOVE.translate());

			JList l = (JList) o;
			JObjectsVector v = l.getItems();
			for (JObjectI<?> ob : v)
				if (ob.getValue().equals(obj.getValue())) {
					v.remove(ob);
					l.notifyListeners();
					return JNullObject.NULL_OBJECT;
				}

			
			return JNullObject.NULL_OBJECT;
		}
	}

	private class RemoveFunction extends JFunction {
		public RemoveFunction() {
			super(REMOVE, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			return new JMethod(new RemoveMethod(o));
		}
	}

	private class KeysMethod extends JFunction {

		public KeysMethod() {
			super(KEYS, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.DICT)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", KEYS.translate());

			JObjectsVector v = ((JDict) o).getValue().getKeys();
			return JEP2.createList(v);
		}
	}

	private class KeysFunction extends JFunction {
		public KeysFunction() {
			super(KEYS, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return new JMethod(new KeysMethod());
		}
	}

	private class ValuesMethod extends JFunction {

		public ValuesMethod() {
			super(VALUES, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			if (o.getType() != JEP2Type.DICT)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NO_METHOD, o + "", VALUES.translate());

			JObjectsVector v = ((JDict) o).getValue().getValues();
			return JEP2.createList(v);
		}
	}

	private class ValuesFunction extends JFunction {
		public ValuesFunction() {
			super(VALUES, 0);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			return new JMethod(new ValuesMethod());
		}
	}

}