/**
 * 
 */
package type;

import java.util.Hashtable;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JHashTable  {

	private final Hashtable<Object, JObjectI<?>> values = new Hashtable<>();
	private final Hashtable<Object, JObjectI<?>> keys = new Hashtable<>();
	
	public JHashTable() {
	}

	/**
	 * @param t
	 */
	public JHashTable(JHashTable t) {
		for (Object key : t.keys.keySet()) {
			values.put(key, t.values.get(key));
			keys.put(key, t.keys.get(key));
		}
	}

	/**
	 * @param key
	 * @param value
	 * @return 
	 * @throws JEPException 
	 */
	public void put(JObjectI<?> key, JObjectI<?> value) throws JEPException {
		if (!key.isHashable())
			throw new JEPException(JEP2Trouble.JEP2_NOT_HASHABLE, key.toString());	
			
		Object hk = ((JHashable<?>) key).getHashValue();
		keys.put(hk, key);
		values.put(hk, value);
	}
	
	public int size() {
		return values.size();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	public JObjectI<?> get(JObjectI<?> key) throws JEPException {
		if (!key.isHashable())
			throw new JEPException(JEP2Trouble.JEP2_NOT_HASHABLE, key.toString());
		JObjectI<?> o = values.get(((JHashable<?>) key).getHashValue());
		if (o == null)
			throw new JEPException(JEP2Trouble.JEP2_INVALID_KEY, key.toString());
		return o;
	}

	public JObjectsVector getKeys() {
		JObjectsVector vec = new JObjectsVector();
		vec.addAll(keys.values());
		return vec;
	}
	
	/**
	 * @return
	 */
	public JObjectsVector getValues() {
		JObjectsVector vec = new JObjectsVector();
		vec.addAll(values.values());
		return vec;
	}

	/**
	 * @param key
	 * @return
	 * @throws JEPException 
	 */
	public JObjectI<?> remove(JObjectI<?> key) throws JEPException {
		get(key);
		Object hKey = ((JHashable<?>) key).getHashValue();
		
		keys.remove(hKey);
		return values.remove(hKey);
	}
	
	public static interface JHashable<T extends Object> extends JObjectI<T>{
		public Object getHashValue();
	}


}
