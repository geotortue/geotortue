/**
 * 
 */
package geotortue.sandbox;

import java.io.IOException;

import fw.app.Translator;
import fw.app.Translator.TKey;
import fw.app.prefs.FWLocalPreferences;
import fw.app.prefs.FWPreferenceEntryI;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.xml.XMLException;
import fw.xml.XMLFile;

public class GTPresetSandBoxConfigurations implements FWPreferenceEntryI{

	private final static TKey CLASSIC = new TKey(GTPresetSandBoxConfig.class, "classic");
	private final static TKey ICONS = new TKey(GTPresetSandBoxConfig.class, "icons");
	private final GTSandBox sandBox;
	private int selectedIndex;
	private final FWComboBox comboBox;
	
	public GTPresetSandBoxConfigurations(GTSandBox sb) {
		this.sandBox = sb;
		this.comboBox = new FWComboBox(sandBoxes, null, new FWComboBoxListener() {
			
			@Override
			public void itemSelected(Object o) {
				if (o==null)
					return;
				
				GTPresetSandBoxConfig sb = (GTPresetSandBoxConfig) o;
				sb.load();
				for (int idx = 0; idx < sandBoxes.length; idx++) 
					if (sandBoxes[idx] == sb) {
						selectedIndex = idx;
						return;
					}
			}
		});
		FWLocalPreferences.register(this);
	}

	public void load() {
		sandBoxes[selectedIndex].load();
		comboBox.setSelectedIndex(selectedIndex);
	}

	public FWComboBox getComboBox() {
		return comboBox;
	}
	
	private abstract class GTPresetSandBoxConfig {
		private void load() {
			try {
				sandBox.loadPresetConfiguration(getXMLFile(Translator.getLanguage()).parse());
			} catch (XMLException | HTTPException | IOException ex) {
				ex.printStackTrace();
			}
		}
		
		protected abstract XMLFile getXMLFile(String code) throws HTTPException, IOException;
	}
	
	/*
	 * 
	 */
	
	private final GTPresetSandBoxConfig classicSB = new GTPresetSandBoxConfig(){
		@Override
		protected XMLFile getXMLFile(String code) throws HTTPException, IOException {
			return new XMLFile(getClass().getResource("/cfg/lang/"+code+"/sandbox.xml"));
		}

		@Override
		public String toString() {
			return CLASSIC.translate();
		}
	};
	
	private final GTPresetSandBoxConfig iconsSB = new GTPresetSandBoxConfig(){
		@Override
		protected XMLFile getXMLFile(String code) throws HTTPException, IOException {
			return new XMLFile(getClass().getResource("/cfg/lang/"+code+"/sandbox2.xml"));
		}

		@Override
		public String toString() {
			return ICONS.translate();
		}
	};
	
	private final GTPresetSandBoxConfig[] sandBoxes = new GTPresetSandBoxConfig[]{classicSB, iconsSB};
	
	
	/*
	 * FWPreferenceEntryI
	 */
	
	@Override
	public void fetchDefaultValue() {
		selectedIndex = 0;
	}


	@Override
	public void fetchValue(String v) {
		selectedIndex = Integer.valueOf(v);
	}


	@Override
	public String getEntryValue() {
		return String.valueOf(selectedIndex);
	}


	@Override
	public String getXMLTag() {
		return "SelectableSandBoxes";
	}
}