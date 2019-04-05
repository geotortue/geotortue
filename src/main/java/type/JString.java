/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JString extends JAbstractHashable<String> implements JIterable {

	JString(String value) {
		super(value);
	}

	@Override
	public type.JObjectI.JEP2Type getType() {
		return JEP2Type.STRING;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		if (o.getType() != JEP2Type.STRING)
			throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
		JString b = (JString) o;
		JIterable.Checker.checkLength(len()+b.len());

		return JEP2.createString(getValue()+b.getValue());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		if (o.getType() == JEP2Type.LONG) {
			int n = ((JInteger) o).intValue();
			
			JIterable.Checker.checkLength(n*len());
			
			String res = "";
			for (int idx = 0; idx < n; idx++) 
				res += getValue();

			return JEP2.createString(res);
		} else if (o.getType() == JEP2Type.LIST) 
			return elementAt((JList) o);
		
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, this.toString(), o.toString());
	}
	
	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public JObjectsVector getItems() {
		JObjectsVector v = new JObjectsVector();
		for (char c : getValue().toCharArray()) 
			v.add(new JString(c+""));
		return v;
	}

	@Override
	public int len() {
		return getValue().length();
	}

	@Override
	public boolean isANumber() {
		return false;
	}

	@Override
	public JObjectI<?> elementAt(JList indices) throws JEPException {
		JObjectsVector v = indices.getItems();
		if (v.isEmpty())
			throw new JEPException(JEP2Trouble.JEP2_ELEMENT_AT_EMPTY_LIST, this.toString());
		
		JObjectI<?> o = v.elementAt(0);

		if (o.isANumber()) {
			int idx = parseIndex(JEP2.getLong(o));
			JString res = new JString(getValue().charAt(idx)+"");
			if (v.size() != 1)
				throw new JEPException(JEP2Trouble.JEP2_NOT_ITERABLE, res.toString());
			return res;
		} else if (o.getType() == JEP2Type.SLICER) 
			return slice((JSlicer) o);
		throw new JEPException(JEP2Trouble.JEP2_LIST_INDEX_OUT, o + "");
	}
	
	private int parseIndex(Long l) throws JEPException {
		int index = l.intValue();
		if (l!=index)
			throw new JEPException(JEP2Trouble.JEP2_LIST_INDEX_OUT, index + "");
		
		String v = getValue();
		int vSize = v.length();
		if (index < 0)
			index += vSize;

		if (index < 0 || index >= vSize)
			throw new JEPException(JEP2Trouble.JEP2_LIST_INDEX_OUT, index + "");

		return index;
	}

	private JObjectI<?> slice(JSlicer slice) throws JEPException {
		String res = "";
		String v = getValue();
		for (int idx : slice.getInterval(v.length())) 
			res += v.charAt(idx);
		return JEP2.createString(res);
	}

	@Override
	public String toString() {
		return "\""+super.toString()+"\"";
	}

	@Override
	public JObjectI<?> changeElementAt(JList idx, JObjectI<?> v) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_STRING_ASSIGNMENT, this.toString());
	}
}
