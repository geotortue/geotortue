package geotortue.core;

import java.awt.Window;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.Procedure.ProcedureParsingException;

public class SimpleLibrary {
	
	protected final SortedMap<String, Procedure> table = Collections.synchronizedSortedMap(new TreeMap<String, Procedure>());
	
	protected final KeywordManager keywordManager;
	
	public SimpleLibrary(KeywordManager km) {
		this.keywordManager = km;
	}
	
	public void add(Procedure p_){
		String text = p_.getRawText();
		String key = p_.getKey();
		SourceLocalization loc = createLibLocalization(key, text) ;
		try {
			keywordManager.removeProcedure(key);
			Procedure p = new Procedure(keywordManager, loc);
			if (p_.isHidden())
				p.hide();
			synchronized (table) {
				table.put(key, p);
			}
			keywordManager.addLibrary(key);
		} catch (GTException ex) {
			ex.displayDialog();
		} catch (ProcedureParsingException ex) {
			ex.printStackTrace();
		}
	}
	
	protected SourceLocalization createLibLocalization(final String key, final String text) {
		SourceProvider provider = new SourceProvider() {
			@Override
			public String getText(int offset, int length) {
				return text.substring(offset, offset + length);
			}

			@Override
			public boolean highlight(int offset, int len, boolean error) {
				if (error)
					new GTException(GTTrouble.GTJEP_LIBRARY_EXCEPTION, (Window) null, key).keep();
				return false;
			}

			@Override
			public Window getTopLevelAncestor() {
				return null;
			}
		};
		return new SourceLocalization(provider, 0, text.length());
		}
}
