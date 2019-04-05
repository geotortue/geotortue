package fw.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;




public class FWStyledKeySet<T extends TextStyle> implements XMLCapabilities {
	
	final private T style;
	
	private Set<String> keys = Collections.synchronizedSet(new HashSet<String>());
	
	public FWStyledKeySet(T style){
		this.style = style;
	}

	public T getStyle(){
		return style;
	}
	
	public Set<String> getKeys() {
			return keys;
	}
	
	public String[] getKeysAsArray() {
		synchronized (keys) {
			String[] keysA = new String[keys.size()];
			keysA = keys.toArray(keysA);
			return keysA;
		}
	}
	
	public void add(String key){
		synchronized (keys) {
			keys.add(key);
		}
	}

	public void remove(String key){
		synchronized (keys) {
			keys.remove(key);
		}
	}
	
	public void setKeys(String... ks) {
		boolean shouldUpdateKeys = !(keys.size()==ks.length);
		synchronized (keys) {
			for (String key: ks) {
				if (!keys.contains(key)){
					shouldUpdateKeys=true;
					break;
				}
			}
			if (!shouldUpdateKeys)
				return ;
	
			keys.clear();
			for (String key: ks) {
				this.keys.add(key);			
			}
		}
	}

	public void clear(){
		synchronized (keys) {
			keys.clear();
		}
	}

	/*
	 * XML
	 */
	

	public String getXMLTag() {
		return "StyledKeySet";
	}
	
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.put(style);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		style.loadXMLProperties(child);
		return child;
	}

}
