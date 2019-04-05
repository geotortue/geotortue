/**
 * 
 */
package type;

import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JDict extends JAbstractObject<JHashTable> implements JIterable, JMutable {
	
	private final Vector<JMutableListener> listeners = new Vector<>();
	private String name;
	

	protected JDict(JHashTable v) {
		super(v);
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.DICT;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
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
		JObjectsVector vec= new JObjectsVector();
		for (JObjectI<?> o : getValue().getKeys()) 
			vec.add(o);
		return vec;
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
		r = getValue().get(o);
		if (v.size() == 1)
			return r;
		
		JIterable subList = JEP2.getIterable(r);
		return subList.elementAt(indices.getQueue());
	}
	
	/**
	 * @param indices
	 * @param value
	 * @return
	 * @throws JEPException
	 */
	public JDict changeElementAt(JList indices, JObjectI<?> value) throws JEPException {
		JHashTable newTable = new JHashTable(getValue());

		JObjectsVector v = indices.getValue(); 
		JObjectI<?> o = v.elementAt(0);

		if (v.size() == 1)
			newTable.put(o, value);
		else {
			JObjectI<?> r = getValue().get(o);
			JIterable subList = JEP2.getIterable(r);
			JObjectI<?> obj = subList.changeElementAt(indices.getQueue(), value);
			newTable.put(o, obj);
		}
		return new JDict(newTable);
	}

	@Override
	public String toString() {
		JHashTable table = getValue();
		JObjectsVector vec = table.getKeys();
		
		int size = table.size();
		if (size == 0)
			return "{}";

		String str = "{ ";
		int idx = 0;
		while (idx<size) {
			JObjectI<?> ob = vec.elementAt(idx);
			try {
				str += ob.toString()+" : "+(table.get(ob))+", ";
			} catch (JEPException ex) { // cannot occur
				ex.printStackTrace();
			}
			idx++;
		}
		str = str.substring(0, str.length()-2);
		str += " } ";
		return str;
	}

	/**
	 * @param key
	 * @return
	 * @throws JEPException 
	 */
	public JObjectI<?> remove(JObjectI<?> key) throws JEPException {
		return getValue().remove(key);
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

