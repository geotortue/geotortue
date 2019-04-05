package fw.app;

import java.util.Hashtable;

import javax.swing.Action;


public class FWActionManager {

	private final Hashtable<String, FWAction> table;

	/**
	 * 
	 */
	public FWActionManager() {
		this.table = new Hashtable<String, FWAction>();
	}

	public FWAction get(String key) throws NoSuchActionFound {
		FWAction command = table.get(key);
		if (command != null)
			return command;
		else
			throw new NoSuchActionFound(key);
	}
	
	public void addAction(FWAction a){
		if (a==null)
			return;
		table.put((String) a.getValue(Action.ACTION_COMMAND_KEY), a);
	}
	
	public static class NoSuchActionFound extends Exception {
		private static final long serialVersionUID = -7678687859659312918L;

		private NoSuchActionFound(String msg){
			super(msg);
		}
	}
}



