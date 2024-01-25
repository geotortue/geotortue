package fw.app;

import java.util.Hashtable;

import javax.swing.Action;


public class FWActionManager {

	private final Hashtable<String, FWAction> table;

	/**
	 * 
	 */
	public FWActionManager() {
		this.table = new Hashtable<>();
	}

	public FWAction get(final String key) throws NoSuchActionFound {
		FWAction command = table.get(key);
		if (command == null) {
			throw new NoSuchActionFound(key);
		}

		return command;
	}
	
	public void addAction(FWAction a){
		if (a == null) {
			return;
		}

		table.put((String) a.getValue(Action.ACTION_COMMAND_KEY), a);
	}
	
	public static class NoSuchActionFound extends Exception {
		private static final long serialVersionUID = -7678687859659312918L;

		private NoSuchActionFound(final String msg){
			super(msg);
		}
	}
}



