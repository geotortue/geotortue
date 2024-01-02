package geotortue.core;


import java.awt.Dialog.ModalExclusionType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JWindow;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPException.JEPTrouble;
import org.nfunk.jep.addon.JEPTroubleI;

import fw.HelpI;
import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.geometry.GTGeometryI.GeometryException;
import geotortue.gui.GTDecoratedPane;
import geotortue.gui.GTDialog;
import geotortue.gui.GTHelp;
import jep2.JEP2.JEP2Trouble;
import jep2.JEP2Exception;


public class GTException extends Exception  {
	/**
	 * 
	 */
	private static final TKey INFO = new TKey(GTException.class, "info");
	private static final TKey WRONG_INSTRUCTION = new TKey(GTException.class, "wrongInstruction");
	private static final long serialVersionUID = -2343323542695588699L;
	private static final ActionKey BUTTON_HELP = new ActionKey(GTException.class, "helpButton");
	
	
	private final SourceLocalization localization;
	private final String[] args;
	private final JEPTroubleI trouble;
	private final HelpI help;
	private static Stack<GTException> keptExceptions = new Stack<>();
	private final int priority;
	
	private static int currentPriority;
	
	public GTException(HelpI h, JEPTroubleI trouble, SourceLocalization loc, String... args) {
		super(trouble.toString());
		this.help = h;
		this.trouble = trouble;
		this.localization = loc;
		this.args = args;
		this.priority = computePriority(trouble);
	}
	
	public GTException(JEPTroubleI trouble, SourceLocalization loc, String... args){
		this(null, trouble, loc, args);
	}

	public GTException(JEPException ex, SourceLocalization loc) {
		this(null, ex.getTrouble(), loc, ex.getInfos());
	}
	
	public GTException(JEP2Exception ex, SourceLocalization loc) {
		this(ex.getHelp(), ex.getTrouble(), loc, ex.getInfos());
	}
	
	public GTException(HelpI h, GeometryException ex, SourceLocalization loc) {
		this(h, ex.getTrouble(), loc, ex.getInfos());
	}
	
	public GTException(GTTrouble trouble, Window owner, String... args) {
		this(null, trouble, SourceLocalization.create("unknown", owner), args);
	}


	/**
	 * @return
	 */
	public JEPTroubleI getTrouble() {
		return trouble;
	}

	public void displayDialog(){
		currentPriority = priority;
		
		if (help == null) 
			GTDialog.show(getTopLevelAncestor(), INFO, getAllExceptionsMessage(), false);
		else {
			FWAction action = new FWAction(BUTTON_HELP, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					GTHelp.displayHelp(getTopLevelAncestor(), help);
				}
			});
			JButton button = new JButton(action);
			button.setIcon(FWToolKit.getIcon("help-browser.png"));

			GTDialog.show(getTopLevelAncestor(), INFO, getAllExceptionsMessage(), false, button);
		}

	}
	
	public void displayTransientWindow(){
		currentPriority = priority;
		
		final JWindow window = new JWindow();
		GTDecoratedPane pane = new GTDecoratedPane(getAllExceptionsMessage());
		pane.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				window.dispose();
			}
		});
		pane.setBorder(BorderFactory.createEtchedBorder());
		
		window.add(pane);
		window.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		window.pack();
		window.validate();
		
		window.setLocationRelativeTo(getTopLevelAncestor());
		window.setVisible(true);
		window.setAlwaysOnTop(true);
		

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				window.dispose();
			}
		}, 2000);
		
	}
	

	public void keep() {
		GTException.keptExceptions.push(this);
	}
	
	public static void forget() {
		GTException.keptExceptions.clear();
	}
	

	private String getAllExceptionsMessage(){
		boolean highlightDone = localization.highlight(true);
		String m = getFormattedMessage();
		
		if (!keptExceptions.isEmpty()) {
		
			HashSet<JEPTroubleI> troubles = new HashSet<>();
			troubles.add(trouble);
			
			GTException ex = this ;
			
			while (!keptExceptions.isEmpty()) {
				ex = keptExceptions.pop();
				if (! troubles.contains(ex.trouble)) {
					if (ex.priority == currentPriority || ex.trouble == GTTrouble.GTJEP_LIBRARY_EXCEPTION) { 
						m = m+ex.getFormattedMessage();
						if (!highlightDone)
							highlightDone = ex.localization.highlight(true);
					} else if (ex.priority > currentPriority) {
						currentPriority = ex.priority;
						m = ex.getFormattedMessage();
						highlightDone = ex.localization.highlight(true);
					}
					troubles.add(ex.trouble);
				}
			}
		}
		
		if (!highlightDone)
			m += "<p>"+WRONG_INSTRUCTION.translate()+localization.getRawText()+"</p>";
		
		return m;
	}
	
	private String getFormattedMessage() {
		String m = GTMessageFactory.get(trouble);
			
		if (args != null) 
			for (int idx = 0; idx < args.length; idx++) 
				m = m.replace("#" + (idx + 1), args[idx]);

		return m;
	}
	
	

	private Window getTopLevelAncestor() {
		return localization.getTopLevelAncestor();
	}
	
	private int computePriority(JEPTroubleI trouble) {
		if (trouble instanceof GTTrouble)
			switch ((GTTrouble) trouble) {
			case GTJEP_FUN_EVAL_ERROR:
				return 50;
			case GTJEP_NO_SUCH_COMMAND:
				return 12;
			case GTJEP_NULL_RETURNED:
				return 15;
			case GTJEP_LIBRARY_EXCEPTION :
				return 1;
			default:
				return 100;
			}
		else if (trouble instanceof JEP2Trouble)
			switch ((JEP2Trouble) trouble) {
			case JEP2_ELEMENT_AT_EMPTY_LIST:
				return 90;
			case JEP2_ILLEGAL_PROD:
				return 60;
			case JEP2_ILLEGAL_SUM:
				return 60;
			case JEP2_INVALID_KEY:
				return 90;
			case JEP2_LIST_INDEX_OUT:
				return 90;
			case JEP2_NOT_AN_INT:
				return 80;
			case JEP2_NOT_A_BOOLEAN:
				return 80;
			case JEP2_NOT_A_LIST:
				return 80;
			case JEP2_NOT_A_METHOD:
				return 70;
			case JEP2_NOT_A_NUMBER:
				return 80;
			case JEP2_NOT_A_STRING:
				return 80;
			case JEP2_NOT_HASHABLE:
				return 80;
			case JEP2_NOT_ITERABLE:
				return 80;
			case JEP2_NO_METHOD:
				return 90;
			case JEP2_UNEXPECTED_ERROR:
				return 25;
			default:
				return 100;
			}
		else if (trouble instanceof JEPTrouble)
			switch ((JEPTrouble) trouble) {
			case JEP_ILLEGAL_NUMBER_OF_ARGUMENTS:
				return 85;
			case JEP_TOKEN_ERROR:
				return 5;
			case JEP_UNRECOGNIZED_FUNCTION:
				return 15;
			case JEP_UNRECOGNIZED_VARIABLE:
				return 12;
			default:
				return 100;
		}
		else return 100;
	}

}