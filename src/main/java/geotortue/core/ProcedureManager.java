package geotortue.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProcedureManager {

	private final Map<String, Procedure> procs = Collections.synchronizedMap(new HashMap<String, Procedure>());
	private final Map<String, Integer> starts = Collections.synchronizedMap(new HashMap<String, Integer>());
	private final Map<String, Integer> ends = Collections.synchronizedMap(new HashMap<String, Integer>());
	
	public Procedure getProcedure(String key){
		synchronized (procs) {
			return procs.get(key);
		}
	}
	
	public Procedure getProcedure(int offset){
		String key = getKey(offset);
		if (key!=null)
			return getProcedure(key);
		return null;
	}
	
	public int getStart(int offset){
		String key = getKey(offset);
		if (key!=null)
			return starts.get(key);
		return offset;
	}
	
	public int getStart(String key){
		return starts.get(key);
	}

	public int getEnd(String key){
		return ends.get(key);
	}

	private String getKey(int offset){
		synchronized (starts) {
			for (String key : starts.keySet())
				if (key != null && starts.get(key) <= offset && offset <= ends.get(key) )
					return key;
			return null;
		}
	}

	public void add(Procedure p, int startIdx, int endIdx) {
		String key = p.getKey();
		synchronized (procs) { 
			synchronized (starts) {
				synchronized (ends) {
					procs.put(key, p);
					starts.put(key, startIdx);
					ends.put(key, endIdx);
				}
			}
		}
	}
	
	public Set<String> getKeys(){
		return procs.keySet();
	}
	
	public ArrayList<String> getKeys(int s, int e){
		ArrayList<String> keys = new ArrayList<>();
		synchronized (procs) {
			for (String key : procs.keySet())  {
				int ps = getStart(key);
				int pe = getEnd(key);
				if (! ((ps<s && pe<s) || (ps>e && pe>e)))
					keys.add(key);
			}
		}
		return keys;
	}
	
	
	public Vector<String> getSortedKeys(){
		synchronized (procs) {
			Vector<String > v = new Vector<String>(procs.keySet());
			Collections.sort(v);
			return v;
		}
	}
	
	public Collection<Procedure> getAllProcedures() {
		synchronized (procs) {
			return procs.values();
		}
	}
	
	public int getSize() {
		return procs.size();
	}
	
	public void clear() {
		synchronized (procs) { 
			synchronized (starts) {
				synchronized (ends) {
					procs.clear();
					starts.clear();
					ends.clear();
				}
			}
		}
	}
	
	public boolean isEmpty() {
		return procs.isEmpty();
	}

	public void remove(String key) {
		synchronized (procs) { 
			synchronized (starts) {
				synchronized (ends) {
					procs.remove(key);
					starts.remove(key);
					ends.remove(key);
				}
			}
		}
	}
}