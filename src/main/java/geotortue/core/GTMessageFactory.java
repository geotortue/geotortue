package geotortue.core;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TreeMap;

import org.nfunk.jep.addon.JEPException.JEPTrouble;
import org.nfunk.jep.addon.JEPTroubleI;

import color.GTColorFunctionFactory.ColorTrouble;
import files.GTUserFileManager.GTFileTrouble;
import fw.app.Translator;
import fw.files.FileUtilities.HTTPException;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import jep2.JEP2.JEP2Trouble;
import sound.MusicFunctionFactory.MusicTrouble;

public class GTMessageFactory implements XMLCapabilities {

	private final static Hashtable<JEPTroubleI, GTMessage> TABLE = new Hashtable<JEPTroubleI, GTMessage>();
	
	public enum GTTrouble implements JEPTroubleI {
		GTJEP_ARITY, 
		GTJEP_ASK_FOR,
		GTJEP_CONFLICT_FUNCTION,
		GTJEP_CONFLICT_KEYWORD,
		GTJEP_CONFLICT_LIBRARY,
		GTJEP_CONFLICT_PROCEDURE, 
		GTJEP_CONFLICT_TURTLE,
		GTJEP_DISC_TELEPORTATION,
		GTJEP_DISC_TRANSPORT,
		GTJEP_EXEC,
		GTJEP_FILLING, 
		GTJEP_FILLING_REDONDANT,
		GTJEP_FOR_EACH,
		GTJEP_FUNCTION_DECLARATION,
		GTJEP_FUN_EVAL_ERROR, 
		GTJEP_HP_TELEPORTATION,
		GTJEP_HP_TRANSPORT, 
		GTJEP_IF_THEN_ELSE,
		GTJEP_IS_RUNNING,
		GTJEP_NEW_TURTLE,
		GTJEP_LAST_TURTLE, 
		GTJEP_LIBRARY_EXCEPTION, 
		GTJEP_MANIPULATION, 
		GTJEP_MANIPULATION_UNAVAILABLE, 
		GTJEP_MISMATCHING_BRACKETS, 
		GTJEP_MISMATCHING_PASSWORD, 
		GTJEP_MISSING_CLOSING_BRACKET, 
		GTJEP_MISSING_QUOTATION_MARK, 
		GTJEP_MODEL_EXCEPTION,
		GTJEP_MIDI_CONCERT,
		GTJEP_MIDI_INVALID_EVENT,
		GTJEP_MIDI_INVALID_CHANNEL,
		GTJEP_MIDI_TURTLE_WO_SCORE, 
		GTJEP_MIDI_UNAVAILABLE, 
		GTJEP_NON_FLAT_FILLING, 
		GTJEP_NOT_3D, 
		GTJEP_NOT_4D, 
		GTJEP_NO_SUCH_COMMAND, 
		GTJEP_NULL_RETURNED, 
		GTJEP_PAUSE_NEGATIVE_TIME, 
		GTJEP_PENCIL, 
		GTJEP_PROCEDURE_ARITY, 
		GTJEP_REPEAT, 
		GTJEP_STACK_OVERFLOW, 
		GTJEP_SYMBOL_CONTAINS_DELIMITERS, 
		GTJEP_SYMBOL_DIGITS, 
		GTJEP_TELEPORTATION, 
		GTJEP_TRANSPORT, 
		GTJEP_TURTLE_INDEX, 
		GTJEP_TURTLE_SELECTION,
		GTJEP_WHILE, 
		GTJEP_WRONG_PASSWORD
		//VISE, IMITATE, // TODO : vise, imite, in spherical geometry
	};
	
	public static void build(URL url) {
		try {
			new GTMessageFactory(new XMLFile(url).parse());
		} catch (XMLException | HTTPException | IOException ex) {
			ex.printStackTrace();
		}
	}
//	
	private GTMessageFactory(XMLReader e) throws XMLException {
		loadXMLProperties(e);
		
		for (JEPTroubleI k : GTTrouble.values()) 
			if (TABLE.get(k)==null)
				new Exception("no message for " + k).printStackTrace();
		
		for (JEPTroubleI k : JEPTrouble.values()) 
			if (TABLE.get(k)==null) 
				new Exception("no message for " + k).printStackTrace();
		
		for (JEPTroubleI k : JEP2Trouble.values()) 
			if (TABLE.get(k)==null)
				new Exception("no message for " + k).printStackTrace();

		for (JEPTroubleI k : MusicTrouble.values()) 
			if (TABLE.get(k)==null)
				new Exception("no message for " + k).printStackTrace();
		
		for (JEPTroubleI k : ColorTrouble.values()) 
			if (TABLE.get(k)==null)
				new Exception("no message for " + k).printStackTrace();
		
		// save
//		try {
//			FWFileWriter sWriter = new FWFileWriter(new File("/home/euclide/Bureau/msg.zip"));
//			sWriter.writeXML(this, "filename");
//			sWriter.close();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
	}
	
	public String getXMLTag() {
		return "GTMessageFactory";
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("lang", Translator.getLanguage());
		TreeMap<String, GTMessage> map = new TreeMap<>(); // sort messages by key
		for (JEPTroubleI t: TABLE.keySet()) 
			map.put(t.toString(), TABLE.get(t));

		for (GTMessage m : map.values())
			e.put(m);
		
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child!=null){ 
			try {
				String code = child.getAttribute("lang");
				if (Translator.getLanguage().equals(new Locale(code).getLanguage())) {
					while (child.hasChild(GTMessage.XML_TAG))
						add(new GTMessage(child));
					return child;
				} else
					child = e.popChild(this);
			} catch (XMLException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	private void add(GTMessage m){
		TABLE.put(getTrouble(m.key), m);
	}
	
	public static String get(JEPTroubleI key){
		GTMessage msg = TABLE.get(key);
		if (msg==null) 
			return "Oups ! Aucune aide n'est disponible pour le problème : '"+key.toString()+"'";
		return msg.content;
	}
	
	private static JEPTroubleI getTrouble(String key) {
		try {
			return GTTrouble.valueOf(key);
		} catch (IllegalArgumentException ex) {}
		
		try {
			return JEPTrouble.valueOf(key);
		} catch (IllegalArgumentException ex) {}
		
		try {
			return MusicTrouble.valueOf(key);
		} catch (IllegalArgumentException ex) {}
		
		try {
			return ColorTrouble.valueOf(key);
		} catch (IllegalArgumentException ex) {}
		
		try {
			return GTFileTrouble.valueOf(key);
		} catch (IllegalArgumentException ex) {}
		
		return JEP2Trouble.valueOf(key);
	}

	
	private static class GTMessage implements XMLCapabilities {
		public static final XMLTagged XML_TAG = XMLTagged.Factory.create("GTMessage");
		
		private String content = "";
		private String key ;

		private GTMessage(XMLReader e) {
			loadXMLProperties(e);
		}
		
		public String getXMLTag() {
			return XML_TAG.getXMLTag();
		}

		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", key.toString());
			e.setContent(content);
			return e;
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			try {
				key = getTrouble(child.getAttribute("key")).toString();
				content = child.getContent();
			} catch (XMLException ex) {
				ex.printStackTrace();
				
			}
			return child;
		}
	}
}