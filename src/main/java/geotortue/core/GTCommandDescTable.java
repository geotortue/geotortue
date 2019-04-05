/**
 * 
 */
package geotortue.core;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.TreeMap;

import fw.app.Translator;
import fw.files.FileUtilities.HTTPException;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GTCommandFactory.GTCommandKey;

public class GTCommandDescTable implements XMLCapabilities {
	private static final TreeMap<GTCommandKey, GTCommandDescriptor> TABLE = new TreeMap<>();
	private static final XMLTagged XML_TAG = XMLTagged.Factory.create("GTCommandDescription");
	
	private GTCommandDescTable(XMLReader e) {
		loadXMLProperties(e);
		
		for (GTCommandKey k : GTCommandKey.values())
			if (TABLE.get(k) == null)
				new Exception("no help for " + k).printStackTrace();
	}
	
	public static void build(URL url){
		try {
			new GTCommandDescTable(new XMLFile(url).parse());
		} catch (IOException | XMLException | HTTPException ex){
			ex.printStackTrace();
		}
	}
	
	
	public static String getName(GTCommandKey key) {
		return TABLE.get(key).name;
	}
	
	public static boolean isCommand(GTCommandKey key) {
		return TABLE.get(key).isCommand;
	}
	

	public String getXMLTag() {
		return "GTCommandDescTable";
	}

	public XMLWriter getXMLProperties() {
		return null;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child != null) {
			try {
				String code = child.getAttribute("lang");
				if (Translator.getLanguage().equals(new Locale(code).getLanguage())) {
					while (child.hasChild(XML_TAG)) {
						GTCommandDescriptor m = new GTCommandDescriptor(child);
						TABLE.put(m.key, m);
					}
					return child;
				} else
					child = e.popChild(this);
			} catch (XMLException ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public static String getDescription(GTPrimitiveCommand command) {
		GTCommandKey key = command.key;
		GTCommandDescriptor descriptor = TABLE.get(key);
		String type = descriptor.isCommand ? "command" : "keyword";
		String s = "<html><h3><span class=\""+type+"\">"+descriptor.name+"</span> : "+descriptor.desc+"</h3>";
		s += descriptor.content;
		return s+"</html>";
	}
	
	public static String getGeneralHelp() {
		String s = "<html>";
		GTCommandKey[] keys = GTCommandKey.values();
		for (int idx = 0; idx < keys.length; idx++) {
			GTCommandKey key = keys[idx];
			GTCommandDescriptor descriptor = TABLE.get(key);
			String type = descriptor.isCommand ? "command" : "keyword";
			s += "<h3><span class=\""+type+"\">"+descriptor.name+"</span> : "+descriptor.desc+"</h3>";
			s += descriptor.content;
		}
		return s+"</html>";
	}
	
	private class GTCommandDescriptor implements XMLCapabilities {
		String content = "";
		String name = "";
		String desc = "";
		
		GTCommandKey key;
		boolean isCommand;

		GTCommandDescriptor(XMLReader e) {
			loadXMLProperties(e);
		}

		public String getXMLTag() {
			return XML_TAG.getXMLTag();
		}

		public XMLWriter getXMLProperties() {
			return null;
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			try {
				key = GTCommandKey.valueOf(child.getAttribute("key"));
				name = child.getAttribute("name");
				desc = child.getAttribute("desc");
				isCommand = child.getAttribute("class").equals("command");
				content = child.getContent();
			} catch (XMLException ex) {
				ex.printStackTrace();
			}
			return child;
		}
	}
}