/**
 * 
 */
package type;

import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import fw.geometry.util.MathException;
import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JList extends JAbstractObject<JObjectsVector> implements JIterable, JMutable {

	private final Vector<JMutableListener> listeners = new Vector<>();
	private String name;
	
	protected JList(JObjectsVector v) {
		super(v);
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.LIST;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		if (o.getType() != JEP2Type.LIST)
			throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
		JList b = (JList) o;
		JIterable.Checker.checkLength(len()+b.len());
		
		JObjectsVector v = new JObjectsVector();
		v.addAll(getValue());
		
		v.addAll(b.getValue());
		return JEP2.createList(v);
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		if (o.getType() == JEP2Type.LONG) {
			int n = ((JInteger) o).intValue();
			
			JIterable.Checker.checkLength(n*len());

			JObjectsVector v = new JObjectsVector();
			for (int idx = 0; idx < n; idx++)
				v.addAll(getValue());

			return JEP2.createList(v);
		} else if (o.getType() == JEP2Type.LIST) 
				return elementAt((JList) o);

		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, this.toString(), o.toString());
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public boolean isANumber() {
		return false;
	}

	@Override
	public JObjectsVector getItems() {
		return getValue();
	}
	
	@Override
	public int len() {
		return getValue().size();
	}

	@Override
	public JObjectI<?> elementAt(JList indices) throws JEPException {
		JObjectsVector v = indices.getItems();
		if (v.isEmpty())
			throw new JEPException(JEP2Trouble.JEP2_ELEMENT_AT_EMPTY_LIST, this.toString());
		
		JObjectI<?> o = v.elementAt(0);

		JObjectI<?> r;
		try {
			int idx = parseIndex(o);
			r = getValue().elementAt(idx);
		} catch (JEPException ex) {
			if (o.getType() == JEP2Type.SLICER)
				r = slice((JSlicer) o);
			else
				throw ex;
		}
		
		if (v.size() == 1)
			return r;
		
		JIterable subList = JEP2.getIterable(r);
		return subList.elementAt(indices.getQueue());
	}
	
	private int parseIndex(JObjectI<?> o) throws JEPException {
		try {
			int index = JEP2.getInteger(o);
			JObjectsVector v = getValue();
			int vSize = v.size();
			if (index < 0)
				index += vSize;

			if (index < 0 || index >= vSize)
				throw new JEPException(JEP2Trouble.JEP2_LIST_INDEX_OUT, index + "");

			return index;
		} catch (MathException e) {
			throw new JEPException(JEP2Trouble.JEP2_LIST_INDEX_OUT, o.toString());
		}
	}

	JList getQueue() {
		JObjectsVector v = new JObjectsVector();
		v.addAll(getValue());
		v.remove(0);
		return new JList(v);
	}

	private JObjectI<?> slice(JSlicer slice) throws JEPException {
		JObjectsVector res = new JObjectsVector();
		JObjectsVector v = getValue();
		for (int idx : slice.getInterval(v.size())) {
			JObjectI<?> r = getValue().elementAt(idx);
			res.add(r);
		}
		return JEP2.createList(res);
	}

	/**
	 * @param indices
	 * @param value
	 * @return
	 * @throws JEPException
	 */
	public JList changeElementAt(JList indices, JObjectI<?> value) throws JEPException {
		JObjectsVector newVector = new JObjectsVector();
		newVector.addAll(getValue());

		JObjectsVector v = indices.getValue(); 
		
		JObjectI<?> o = v.elementAt(0);

		int idx = parseIndex(o);
		if (v.size() == 1)
			newVector.set(idx, value);
		else {
			JObjectI<?> r = getValue().elementAt(idx);
			JIterable subList = JEP2.getIterable(r);
			JObjectI<?> obj = subList.changeElementAt(indices.getQueue(), value);
			newVector.setElementAt(obj, idx);
		}
		return new JList(newVector);
	}
	
	public void append(JObjectI<?> o) {
		getValue().add(o);
	}

	@Override
	public void addJMutableListener(String name, JMutableListener l) {
		this.name = name;
		listeners.add(l);
	}

	@Override
	public void notifyListeners() {
		for (JMutableListener l : listeners) 
			l.update(name, this);
	}

	@Override
	public boolean isMutable() {
		return true;
	}
	
	
}
