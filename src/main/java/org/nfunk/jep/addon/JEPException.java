/**
 * 
 */
package org.nfunk.jep.addon;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.Token;

/**
 * @author Salvatore Tummarello
 *
 */
public class JEPException extends ParseException {

	private static final long serialVersionUID = -7075533771679901401L;

	public enum JEPTrouble implements JEPTroubleI {
		JEP_ILLEGAL_NUMBER_OF_ARGUMENTS, 
		JEP_TOKEN_ERROR, 
		JEP_UNRECOGNIZED_FUNCTION, 
		JEP_UNRECOGNIZED_VARIABLE
	}

	private final JEPTroubleI trouble;
	private final String[] infos;

	public JEPException(final JEPTroubleI t, final String... infos) {
		this.trouble = t;
		this.infos = infos;
	}

	public JEPException(final JEPException ex) {
		this.trouble = ex.getTrouble();
		this.infos = ex.infos;
	}

	public JEPException(final Token token, final int[][] exptokseq, final String[] tokenimage) {
		super(token, exptokseq, tokenimage);
		this.trouble = JEPTrouble.JEP_TOKEN_ERROR;
		final String image = token.image != null ? token.image : token.next.image;
		this.infos = new String[]{ image };
	}

	public JEPTroubleI getTrouble() {
		return trouble;
	}

	public String[] getInfos() {
		return infos;
	}
}