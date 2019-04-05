/**
 * 
 */
package geotortue.core;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.Variable;
import org.nfunk.jep.VariableFactory;
import org.nfunk.jep.addon.JEPException;

import function.PostfixMathCommand2;
import fw.app.Translator.TKey;
import fw.text.FWParsingTools;
import fw.text.FWParsingTools.ParsingException;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTMessageFactory.GTTrouble;
import jep2.JEP2;
import jep2.JEP2Exception;
import type.JAssignment;
import type.JNullObject;
import type.JNumber;
import type.JObjectI;
import type.JObjectI.JEP2Type;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTJEP extends JEP2 {

	protected static final TKey TRUE = new TKey(GTJEP.class, "true");
	protected static final TKey FALSE = new TKey(GTJEP.class, "false");
	private static final TKey LOGICAL_AND = new TKey(GTJEP.class, "AND");
	private static final TKey LOGICAL_OR = new TKey(GTJEP.class, "OR");
	private static final TKey LOGICAL_NOT = new TKey(GTJEP.class, "NOT");
	
	private static final String AND_KEY = LOGICAL_AND.translate();
	private static final String OR_KEY = LOGICAL_OR.translate();
	private static final String NOT_KEY = LOGICAL_NOT.translate();
	
	public static String[] getLogicalOps() {
		return new String[]{AND_KEY, OR_KEY, NOT_KEY};
	}

	
	protected enum ScopeMode {Global, Local};
	protected ScopeMode scopeMode = ScopeMode.Global;
	
	private final Stack<SymbolTable2> varTables = new Stack<>();
	
	public GTJEP() {
	}
	
	public GTJEP(GTJEP jep) {
		super(jep);
	}
	
	@Override
	public void addStandardConstants() {
		super.addStandardConstants();
		addConstant(TRUE.translate(), JEP2.createBoolean(true));
		addConstant(FALSE.translate(), JEP2.createBoolean(false));
		addConstant(EVAL_KEY, numberFactory.getOne());
		JNumber<?> pi = createNumber(Math.PI);
		addConstant("pi", pi);
		addConstant("π", pi);
	}

	@Override
	public SymbolTable2 getSymbolTable() {
		return (SymbolTable2) symTab;
	}
	
	public JObjectI<?> getValueOf(String text) throws JEPException {
		JObjectI<?> o = super.getValueOf(text);
		if (o.getType() == JEP2Type.ASSIGNMENT) {
			handleAssignment((JAssignment) o);
			return JNullObject.NULL_OBJECT;
		} else
			return o;
	}
	
	protected void handleAssignment(JAssignment assignment) throws JEPException {
		boolean isGlobalMode = scopeMode == ScopeMode.Global;
		boolean update = getSymbolTable().updateGlobalVarValue(assignment, isGlobalMode);
		if (update)
			for (SymbolTable2 table : varTables)
				table.updateGlobalVarValue(assignment, isGlobalMode);
	}
	
	public JObjectI<?> getJObject(GTProcessingContext context, SourceLocalization bundle) 
			throws GTInterruptionException, GTException {
		String text = bundle.getText();
		

		GTException ex;
		try {
			return getValueOf(text);
		} catch (JEP2Exception e) {
			ex = new GTException(e, bundle);
		} catch (JEPException e) {
			ex = new GTException(e, bundle);
		}
		
		// getValueOf failed : try to evaluate procedures
		
		Vector<ProcedureEvaluation> evals = new Vector<>();
		try {
			evals = getProcedureEvaluations(bundle, context);
		} catch (GTException e) {
			e.keep();
			throw ex;
		}
		if (evals.isEmpty()) 
				throw ex;
		
		for (ProcedureEvaluation pe : evals)
			text = pe.evaluate(text);
		
		try {
			JObjectI<?> o = getValueOf(text);
			GTException.forget();
			return o;
		} catch (JEP2Exception e) {
			throw new GTException(e, bundle);
		} catch (JEPException e) {
			throw new GTException(e, bundle);
		} finally {
			for (ProcedureEvaluation pe : evals) 
				pe.clear();
		}
	}
	
	
	/*
	 * Procedures evaluation
	 */
	
	private static final String EVAL_KEY = KeywordManager.EVAL_KEY.translate();

	private Vector<ProcedureEvaluation> getProcedureEvaluations(SourceLocalization loc,
			GTProcessingContext context) throws GTException, GTInterruptionException {
		
		Vector<ProcedureEvaluation> evals = new Vector<>();

		String text = loc.getRawText();
		int bracketOffset = text.indexOf("(");
		int tagOffset = FWParsingTools.indexOfToken(text, EVAL_KEY, 0);
		boolean tag = (tagOffset > -1) && (bracketOffset < 0 || tagOffset < bracketOffset);
		int offset = bracketOffset;

		while (offset >= 0) {
			try {
				int end = FWParsingTools.getClosingBracketIdx(text, offset)+1;
				int len = end - offset;

				if (len>2) {
					SourceLocalization loc2 = new SourceLocalization(loc.getProvider(), loc.getOffset() + offset, len);
					GTCommandBundles commands = GTCommandBundle.parse(loc2);
					commands.requireValue();
	
					JObjectI<?> o = context.process(commands);
					if (o == null) {
						SourceLocalization loc2t = loc2.getSubLocalization(1, len-2); // trim
						throw new GTException(GTTrouble.GTJEP_NULL_RETURNED, loc2t, loc2t.getRawText());
					}
					
		
					if (tag) 
						offset = tagOffset;
						
					evals.add(new ProcedureEvaluation(text.substring(offset, end), o));
				}
				
				offset = end;
				
			} catch (ParsingException ex) {
				throw new GTException(GTTrouble.GTJEP_MISMATCHING_BRACKETS, loc);
			}

			bracketOffset = text.indexOf("(", offset);
			tagOffset = FWParsingTools.indexOfToken(text, EVAL_KEY, offset);
			tag = (tagOffset > -1) && (tagOffset < bracketOffset);
			offset = bracketOffset;
		}
		
		return evals;
	}
	
	
	private class ProcedureEvaluation {
		private final String target;
		private String replacement;
		private final JObjectI<?> obj;
		
		private ProcedureEvaluation(String target, JObjectI<?> obj) {
			this.target = target;
			this.replacement = target.replaceAll("[\\W]", "_");
			this.obj = obj;
		}

		/**
		 * Replace procedures by variables
		 * @param text
		 * @return
		 */
		private String evaluate(String text) {
			try {
				getSymbolTable().addVariable(replacement, obj);
			} catch (IllegalStateException ex ){ 
				// variable already exists
				// change randomly a letter
				int idx = (int) (Math.random()*replacement.length());
				if (replacement.charAt(idx) != '_') {
					String alpha = "abcdefghijklmnopqrstuvwxyz";
					alpha += alpha.toUpperCase();
					int idx2 = (int) (Math.random()*alpha.length());
					char c = alpha.charAt(idx2);
					replacement = replacement.substring(0, idx)+c+replacement.substring(idx+1);
				}
				return evaluate(text);
			}
			
			int cut = text.indexOf(target);
			if (cut<0)
				return text;
			String start = text.substring(0, cut);
			String end = text.substring(cut+target.length());
			return  start+"("+replacement+")"+end;
		}

		private void clear() {
			removeVariable(replacement);
		}
	}
	
	/*
	 * Gestion des variables locales / globales
	 */
	
	/**
	 * @param name
	 */
	public void declareGlobal(String name) {
		SymbolTable2 symTab = getSymbolTable();
		if (!varTables.isEmpty()) { // local parser opened : fetch global value
			SymbolTable2 refTable = varTables.firstElement();
			refTable.createGlobalVar(name);
			symTab.createGlobalVar(name);
			Variable var = refTable.getVar(name);
			Object val = var.getValue();
			symTab.setVarValue(name, val);
		} else { // no local parser : create global var if needed
			symTab.createGlobalVar(name);
		}
	}
	
	@Override
	public void initSymTab() {
		symTab = new SymbolTable2(new VariableFactory());
	}
	
	public void openLocalParser(String[] varNames, JObjectI<?>[] values) throws JEPException {
		varTables.push(getSymbolTable());

		initSymTab();
		addStandardConstants();

		if (scopeMode == ScopeMode.Global) {
			SymbolTable2 table = varTables.firstElement();
			getSymbolTable().importGlobalVars(table);
		} 
		
		if (varNames != null && varNames.length != 0)
			for (int idx = 0; idx < varNames.length; idx++) {
				String name = varNames[idx];
				JObjectI<?> newValue = values[idx];
				try {
					addVariable(name, newValue);
				} catch (IllegalStateException ex) {
					throw new JEPException(JEP2Trouble.JEP2_CANNOT_CHANGE_A_CONSTANT, name);
				}
			}
	}

	public void closeLocalParser() {
		symTab = varTables.pop();
	}
//	
//	
	/*
	 * User function
	 */
	
	public UserFunction addUserFunction(GTProcessingContext context, String key, String def, String[] vars) {
		UserFunction fun = new UserFunction(context, key, def, vars);
		super.addFunction(key, fun);
		return fun;
	}
	
	public class UserFunction extends PostfixMathCommand2 {

		private final String name;
		private final String def;
		private final String body;
		private final String[] varNames;
		private final GTProcessingContext context;
		
		private UserFunction(GTProcessingContext context, String name, String def, String... var) {
			super(var.length);
			this.context = context;
			this.name = name;
			this.def = def;
			this.varNames = var; 
			this.body = replaceSpecialSymbols(def);
		}
		
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?>[] values = new JObjectI<?>[numberOfParameters];
			for (int idx = numberOfParameters - 1; idx >= 0; idx--)
				values[idx] = popJEPObjectI(inStack);

			JEPException ex = new JEPException(GTTrouble.GTJEP_FUN_EVAL_ERROR, name);
			
			// try with a minimal jep
			try {
				GTJEP tempJep = new GTJEP(GTJEP.this);
				if (varNames != null && varNames.length != 0)
					for (int idx = 0; idx < varNames.length; idx++) {
						String name = varNames[idx];
						JObjectI<?> newValue = values[idx];
						tempJep.addVariable(name, newValue);
					}
				
				return tempJep.getValueOf(body);
			} catch (JEPException e) {
			}
			
			// minimal jep failed : open local parser
			openLocalParser(varNames, values);
			
			try {
				SourceLocalization loc = SourceLocalization.create(body, null);
				return getJObject(context, loc);
			} catch (GTInterruptionException | GTException e) {
				throw ex;
			} finally {
				closeLocalParser();
			}
		}
		
		public String format() {
			int len = varNames.length;
			String str = name + "(";
			if (len==0)
				return str + ") = " + def;
			for (int idx = 0; idx < len-1; idx++)
				str += varNames[idx]+"; ";
			str += varNames[len-1]+")";
			return str+" = " + def; 
		}
	}
	
	@Override
	protected final String replaceSpecialSymbols(String expressionIn) {
		String str = expressionIn.replace(":=", " =");
		str = str.replace("√", "sqrt");
		str = str.replace("²", "^2");
		str = str.replace("³", "^3");
		str = str.replace("≤", "<=");
		str = str.replace("≥", ">=");
		str = str.replace("≠", "!=");
		str = str.replaceAll("(\\W)"+AND_KEY+"(\\W)", "$1 && $2");
		str = str.replaceAll("(\\W)"+OR_KEY+"(\\W)", "$1 || $2");
		str = str.replaceAll("(\\W||^)"+NOT_KEY+"(\\W)", "$1 ! $2");
		str = str.replaceAll("\\s"+KeywordManager.TAKE_THE_VALUE_KEY.translate()+"\\s", "=");
		return super.replaceSpecialSymbols(str);
	}
}
