/**
 * 
 */
package geotortue.core;

import java.util.Collection;
import java.util.regex.PatternSyntaxException;

import geotortue.core.Procedure.ProcedureParsingException;

/**
 * @author Salvatore Tummarello
 *
 */
public class OldFileRefactor {

	private boolean updateRequired = false;
	
	public void init() {
		updateRequired = false;
	}
	
	public void requireUpdate() {
		updateRequired = true;
	}
	
	public void update(GTDocumentFactory docFactory) {
		if (!updateRequired)
			return;
		GTCodeDocument doc = docFactory.getCommandDocument();
		String code = doc.getText();
		code = update(code);
		doc.setText(code);
		
		doc = docFactory.getProcedureDocument();
		code = doc.getText();
		code = update(code);
		doc.setText(code);
	}

	/**
	 * @param reader
	 * @return
	 */
	public void update(Library lib, KeywordManager km) {
		if (!updateRequired)
			return ;
		
		Collection<Procedure> procs = lib.getAllProcedures();
		for (Procedure p : procs) {
			String code = p.getRawText();
			code = update(code);
			SourceLocalization loc = SourceLocalization.create(code, null);
			km.removeLibrary(p.getKey());
			try {
				lib.add(new Procedure(km, loc));
			} catch (GTException ex) {
				ex.displayDialog();
			} catch (ProcedureParsingException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param code
	 * @return
	 */
	private String update(String str) {
		String code = str.replace("boucle", "pour_chaque");
		code = code.replace("palette", "crayon");
		code = code.replace("RVB", "rvb");
		code = code.replace("TSV", "tsv");
		try {
			code = code.replaceAll("tsv\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)", "tsv($1, $2, $3)");
			code = code.replaceAll("rvb \\s*(\\S*)\\s*(\\S*)\\s*(\\S*)", "rvb($1, $2, $3)");
			code = code.replaceAll("écris\\s+([^\n^;]*)", "écris \"$1\"");
			code = code.replaceAll("dis\\s+([^\n^;]*)", "dis \"$1\"");
		} catch (PatternSyntaxException ex) {
		}
		return code;
	}
}
