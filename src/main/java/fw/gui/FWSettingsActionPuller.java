/**
 * 
 */
package fw.gui;

import java.util.HashMap;

public class FWSettingsActionPuller {
	
	private final HashMap<FWSettingsActionKey, FWSettingsAction> listeners = new HashMap<>();
	
	public static class FWSettingsActionKey {}
	
	public final static FWSettingsActionKey REPAINT = new FWSettingsActionKey();
	
	public FWSettingsActionPuller() {
	}

	public void fire(FWSettingsActionKey key) {
		FWSettingsAction l = listeners.get(key);
		if (l==null) {
			new Exception("no such setting action : "+key).printStackTrace();
			return;
		}
		listeners.get(key).fire();
	}
	
	public void register(FWSettingsActionKey key, FWSettingsAction l) {
		listeners.put(key, l);
	}
	

	public FWSettingsAction get(FWSettingsActionKey key) {
		return listeners.get(key);
	}
}
