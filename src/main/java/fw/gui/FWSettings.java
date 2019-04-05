package fw.gui;

import javax.swing.JPanel;

import fw.app.Translator.TKey;


public interface FWSettings {

	public JPanel getSettingsPane(FWSettingsActionPuller actions);
	
	public TKey getTitle();
	
}
