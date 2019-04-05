/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

import type.JObjectI.JEP2Type;

/**
 * @author Salvatore Tummarello
 *
 */
public class JObjectHelper {

	public static int compare(JObjectI<?> a, JObjectI<?> b) {
		JEP2Type aType = a.getType();
		JEP2Type bType = b.getType();
		if (aType != bType && !(a.isANumber() && b.isANumber()))
			return aType.compareTo(bType);
		
		switch (aType) {
		case LONG:
		case DOUBLE:
			JNumber<?> e1 = (JNumber<?>) a;
			JNumber<?> e2 = (JNumber<?>) b;
			return e1.doubleValue().compareTo(e2.doubleValue());
		case BOOLEAN:
			JBoolean b1 = (JBoolean) a;
			JBoolean b2 = (JBoolean) b;
			return b1.getValue().compareTo(b2.getValue());
		case LIST:
			JIterable l1 = (JIterable) a;
			JIterable l2 = (JIterable) b;
			return compare(l1.getItems(), l2.getItems());
		case METHOD:
			JMethod m1 = (JMethod) a;
			JMethod m2 = (JMethod) b;
			return m1.getValue().getName().compareTo(m2.getValue().getName());
		case NULL:
			return -1;
		case SLICER:
			JSlicer s1 = (JSlicer) a;
			JSlicer s2 = (JSlicer) b;
			return (s1.isTheSameAs(s2))? 0 : -1;
		case STRING:
			String str1 = ((JString) a).getValue();
			String str2 = ((JString) b).getValue();
			return str1.compareTo(str2);
		default:
			
			return -1;
		}
	}
	
	/**
	 * @param b
	 * @return
	 * @throws JEPException 
	 */
	private static int compare(JObjectsVector a, JObjectsVector b) {
		int aSize = a.size();
		int bSize = b.size();
		for (int idx = 0; idx < Math.min(aSize, bSize); idx++) {
			JObjectI<?> e1 = a.elementAt(idx);
			JObjectI<?> e2 = b.elementAt(idx);
			int comp = compare(e1, e2);
			if (comp!=0)
				return comp;
		}
		return aSize-bSize;
	}
	
}