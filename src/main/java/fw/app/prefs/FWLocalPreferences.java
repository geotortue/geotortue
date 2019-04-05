/**
 * 
 */
package fw.app.prefs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import fw.app.FWManager;
import fw.app.FWRestrictedAccessException;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;

public class FWLocalPreferences implements XMLCapabilities {
	
	private File file;
	private final Map<String, WrappedPrefEntry> entries = Collections.synchronizedSortedMap(new TreeMap<String, WrappedPrefEntry>());
	private final Map<String, XMLReader> pool = new HashMap<>();
	private final static FWLocalPreferences SHARED_INSTANCE = new FWLocalPreferences();

	private FWLocalPreferences() {
		try {
			this.file = new File(FWManager.getConfigDirectory(), "prefs.xml");
			updatePool();
		} catch (FWRestrictedAccessException e) {
		}
	}
	
	private void updatePool() {
		if (!FWManager.isRestricted() && file.exists()) {
			try {
				XMLReader e = new XMLFile(file).parse();
				loadXMLProperties(e);
			} catch (XMLException | IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public String getXMLTag() {
		return "FWPreferences";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		return null;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {
			while (child.hasChild(Entry.XML_TAG)) {
				XMLReader r = child.popChild(Entry.XML_TAG);
				String key = r.getAttribute("key");
				pool.put(key, r);
			} 
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
		return child;
	}
	
	public static void loadDefaults() {
		for (WrappedPrefEntry e : SHARED_INSTANCE.entries.values()) 
			e.entry.fetchDefaultValue();
	}

	public static void synchronize() {
		if (!FWManager.isRestricted())
			SHARED_INSTANCE.synchronize_();
	}
	
	private void synchronize_()  {
		XMLWriter writer = new XMLWriter(this);
		HashSet<String> keys = new HashSet<>(pool.keySet());
		synchronized (entries) {
			for (String key : entries.keySet()) {
				writer.put(entries.get(key));
				keys.remove(key);
			}
			
			for (String key : keys) { // save unused entries
				XMLReader r = pool.get(key);
				writer.put(new Entry(r));
			}
		}
		
		XMLFile f = new XMLFile(writer);
		try {
			f.write(file);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		updatePool();
	}
	
	/**
	 * 
	 * @param e
	 * @return true if a value has been found in prefs
	 */
	public static boolean register(FWPreferenceEntryI e) {
		if (!FWManager.isRestricted())
			return SHARED_INSTANCE.register_(e);
		return false;
	}
	
	private boolean register_(FWPreferenceEntryI entry) {
		String key = entry.getXMLTag();
		XMLReader r = pool.get(key);
		if (r!=null)
			try {
				entry.fetchValue(r.getAttribute("value"));
				return true;
			} catch (XMLException ex) {
				ex.printStackTrace();
			}
		
		entry.fetchDefaultValue();
		entries.put(key, new WrappedPrefEntry(entry));
		return false;
	}
	
	private static class Entry implements XMLCapabilities {
		private final static XMLTagged XML_TAG = XMLTagged.Factory.create("entry");
		private String key, value;
		
		private Entry() {
		}
		
		private Entry(XMLReader e) {
			loadXMLProperties(e);
		}
	
		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", key);
			e.setAttribute("value", value);
			return e;
		}
	
		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			key = e.getAttribute("key", "");
			value = e.getAttribute("value", "");
			return e;
		}
	
		@Override
		public String getXMLTag() {
			return XML_TAG.getXMLTag();
		}
	}

	private static class WrappedPrefEntry extends Entry {
		private final FWPreferenceEntryI entry;
		
		private WrappedPrefEntry(FWPreferenceEntryI e) {
			this.entry = e;
		}

		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			String value = entry.getEntryValue();
			if (value != null) {
				e.setAttribute("key", entry.getXMLTag());
				e.setAttribute("value", value);
			}
			return e;
		}
	}
}