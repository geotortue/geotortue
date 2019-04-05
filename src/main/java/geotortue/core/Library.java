package geotortue.core;

import java.util.Collection;
import java.util.Vector;

import fw.app.FWConsole;
import fw.text.FWParsingTools;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.Procedure.ProcedureParsingException;



public class Library extends SimpleLibrary implements XMLCapabilities {

	private String password = null;
	
	public Library(KeywordManager km) {
		super(km);
	}
	
	public void remove(Procedure p){
		synchronized (table) {
			table.remove(p.getKey());
		}
		keywordManager.removeLibrary(p.getKey());
	}
	
	protected int getSize() {
		return table.size();
	}
	
	public boolean isEmpty() {
		return table.isEmpty();
	}

	public Procedure getProcedure(String key){
		return table.get(key);
	}
	
	public Vector<String> getSortedKeys(){
		synchronized (table) {
			Vector<String> v = new Vector<>();
			for (String key : table.keySet()) {
				if (!table.get(key).isHidden())
					v.add(key);
			}
			return v;
		}
	}
	
	public Collection<Procedure> getAllProcedures() {
		synchronized (table) {
			return new Vector<>(table.values());
		}
	}
	
	/*
	 * Password
	 */
	
	public boolean checkPassword(char[] p){
		return password.equals(new String(p));
	}
	
	public boolean isPasswordSet(){
		return password!=null;
	}
	
	public void setPassword(String p){
		this.password = p;
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "Library";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		if (password!=null)
			e.setAttribute("password", password);
		e.setAttribute("version", "4");
		synchronized (table) {
			for (Procedure p : table.values())
				e.put(p);
		}
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		table.clear();
		keywordManager.clearLibrary();
		try {
			password = child.getAttribute("password");
		} catch (XMLException ex) {
			password = null;
		}
		String version = child.getAttribute("version", "");
		if (version.equals("4"))
			while (child.hasChild(Procedure.XML_TAG))
				addProcedureVersion4(child);
		else
			while (child.hasChild(Procedure.XML_TAG))
				addProcedureOldVersion(child);
		return child;
	}
	
	private void addProcedureOldVersion(XMLReader e){
		XMLReader child = e.popChild(Procedure.XML_TAG);
		try {
			String text = KeywordManager.START_KEY.translate()+ " ";
			text += child.getAttribute("key");
			String[] localVars = child.getAttributeAsStringArray("vars", new String[0]);
			for (int idx = 0; idx < localVars.length; idx++)
				text += " "+localVars[idx];
			text += "\n"+child.getContent();
			SourceLocalization loc = SourceLocalization.create(text, null);
			add(new Procedure(keywordManager, loc));
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		} catch (GTException | ProcedureParsingException ex) {
			//ex.printStackTrace();
		}
	}
	
	private void addProcedureVersion4(XMLReader e){
		XMLReader child = e.popChild(Procedure.XML_TAG);
		try {
			String text = child.getContent();
			SourceLocalization loc = SourceLocalization.create(text, null);
			add(new Procedure(keywordManager, loc));
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		} catch (GTException | ProcedureParsingException ex) {
			//ex.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String s = super.toString() +" [";
		synchronized (table) {
			for (Procedure p : table.values())
				s+= p.getKey()+" ";
		}
		s+= "]";
		return s;
	}

	public void replace(String target, String replacement, boolean smart) throws GTException, ProcedureParsingException {
		Vector<Procedure> newTable = new Vector<>(); 
		synchronized (table) {
			for (String key : table.keySet()) {
				keywordManager.removeLibrary(key);
				try {
					newTable.add(replace(table.get(key), target, replacement, smart));
				} catch (GTException ex){
					keywordManager.addLibrary(key);
					throw ex;
				}	
			}
		
			table.clear();
		
			for (Procedure p : newTable)  {
				String key = p.getKey();
					table.put(key, p);
				keywordManager.addLibrary(key);
			}
		}
	}
	
	private Procedure replace(Procedure p, String target, String replacement, boolean smart) throws GTException, ProcedureParsingException {
		String text = p.getRawText();
		if (smart)
			text = FWParsingTools.replaceTokens(text, 
					new String[] { target }, new String[] { replacement });
		else
			text = text.replace(target, replacement);
		SourceLocalization loc = createLibLocalization(p.getKey(), text);
		return new Procedure(keywordManager, loc);
	}
}