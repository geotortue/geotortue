/**
 * 
 */
package jep2;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPTroubleI;

import fw.HelpI;

/**
 * @author Salvatore Tummarello
 *
 */
public class JEP2Exception extends JEPException {
	private static final long serialVersionUID = 5671013088068309518L;
	
	private final HelpI help;
	
	public JEP2Exception(final HelpI h, final JEPTroubleI t, final String... infos) {
		super(t, infos);
		this.help = h;
	}

	/**
	 * @param h
	 * @param e
	 */
	public JEP2Exception(final HelpI h, final JEPException e) {
		this(h, e.getTrouble(), e.getInfos());
	}

	/**
	 * @return
	 */
	public HelpI getHelp() {
		return help;
	}

}
