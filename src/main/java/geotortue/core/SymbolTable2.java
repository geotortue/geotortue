/**
 * 
 */
package geotortue.core;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.VariableFactory;

import type.JAssignment;

/**
 * @author Salvatore Tummarello
 *
 */
public class SymbolTable2 extends SymbolTable {

	private static final long serialVersionUID = 3311759321171633934L;
	
	private final HashSet<String> globalVars = new HashSet<>();
	
	private SymbolTableListener stListener = new SymbolTableListener() {
		@Override
		public void update(Observable o, Object arg) {}
		
		@Override
		public void itemRemoved(String o) {}

	};

	public SymbolTable2(VariableFactory varFac) {
		super(varFac);
	}

	/**
	 * Create global var if needed
	 * @param name
	 * @return global var
	 */
	public void createGlobalVar(String name) {
		if (globalVars.add(name));
			makeVarIfNeeded(name);	
	}
	
	public void importGlobalVars(SymbolTable2 refTable) {
		for (String name : refTable.globalVars) {
			makeVarIfNeeded(name);
			Object value = refTable.getValue(name);
			setVarValue(name, value);
		}
	}
	
	public boolean updateGlobalVarValue(JAssignment assignment, boolean globalMode) {
		String name = assignment.getVarName();
		if (globalMode || globalVars.contains(name)) {
			Object val = assignment.getValue();
			createGlobalVar(name);
			setVarValue(name, val);
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized Object remove(Object key) {
		Object o = super.remove(key);
		if (o!=null)
			stListener.itemRemoved((String) key);
		return o;
	}
	
	@Override
	public synchronized void addObserver(Observer l) {
		super.addObserver(l);
		if (l==null)
			return;
		SymbolTableListener stl = (SymbolTableListener) l;
		stListener = stl;
	}
}