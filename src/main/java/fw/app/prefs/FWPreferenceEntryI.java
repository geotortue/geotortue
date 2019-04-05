/**
 * 
 */
package fw.app.prefs;

public interface FWPreferenceEntryI {
	
	public void fetchDefaultValue();

	public void fetchValue(String v);
	
	public String getEntryValue();
	
	public String getXMLTag();

}
