/**
 * 
 */
package fw.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 *
 */
public class FWScopes {

	private final List<FWScope> scopes = Collections.synchronizedList(new ArrayList<FWScope>());

	/**
	 * The scope start to be added shall not be less than the last one end   
	 **/
	public void addScope(FWScope scope) {
		// test if contract is ok
//		if (!scopes.isEmpty())
//			if (scope.getStart() < scopes.get(scopes.size()-1).getEnd()) {
//				new Exception().printStackTrace();
//				System.err.println(scope+"\n"+scopes);
//			}
		scopes.add(scope);
	}
	
	public Vector<FWScope> getScopes(int start, int end) {
		synchronized (scopes) {
			Vector<FWScope> hits = new Vector<>();
			int idx = getScopeIndex(start);
			if (idx<0)
				return hits;
	
			int len = scopes.size();
			while (idx<len) {
				FWScope s = scopes.get(idx);
				if (!s.overlaps(start, end))
					return hits;
				hits.add(s);
				idx++;
			}
			
			
			// old fashion way
	//			for (FWScope scope : scopes) {
	//				if (scope.overlaps(start, end))
	//					return scope;
	//			}
			
			return hits;
		}
	}

	public FWScope getScope(int offset) {
		synchronized (scopes) {
			if (scopes.isEmpty())
				return null;
			
			// old fashion			
//			for (FWScope scope : scopes) {
//				if (scope.contains(offset)) {
//					return scope;
//				}
//			}
//			return null;
			
			// divide to reign
			int len = scopes.size()-1;
			int idx = (int) (len/2);
			int min = 0;
			int max = len;
			while (max-min>1) {
				FWScope s = scopes.get(idx);
				if (offset>=s.getStart()) {
					if (offset<s.getEnd())
						return s;
					else {
						min = idx;
						idx = (int) (idx+max+1)/2;
					}
				} else {
					max = idx;
					idx = (int) (min+idx)/2;
				}
			}
			
			FWScope s = scopes.get(min);
			if (s.contains(offset)) 
				return s;
			
			s = scopes.get(max);
			if (s.contains(offset))
				return s;

			return null;
		}
	}
	
	private int getScopeIndex(int offset) {
		synchronized (scopes) {
			if (scopes.isEmpty())
				return -1;
			
			// divide to reign
			int len = scopes.size()-1;
			int idx = (int) (len/2);
			int min = 0;
			int max = len;
			while (max-min>1) {
				FWScope s = scopes.get(idx);
				if (offset>=s.getStart()) {
					if (offset<s.getEnd())
						return idx;
					else {
						min = idx;
						idx = (int) (idx+max+1)/2;
					}
				} else {
					max = idx;
					idx = (int) (min+idx)/2;
				}
			}
			
			FWScope s = scopes.get(min);
			if (s.contains(offset)) 
				return min;
			
			s = scopes.get(max);
			if (s.contains(offset))
				return max;

			return -1;
		}
	}
	
	public List<FWScope> getScopes() {
		return scopes;
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return scopes.isEmpty();
	}

	@Override
	public String toString() {
		String str = super.toString()+" : ";
		for (FWScope s : scopes)
			str += " " + s;
		return str;
	}

	public static FWScope getScopeAt(int offset, FWScopes... scopesArray) {
		for (FWScopes scopes : scopesArray) {
			FWScope scope = scopes.getScope(offset);
			if (scope!=null)
				return scope;
		}
		return null;
	}

	

}
