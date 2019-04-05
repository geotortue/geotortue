package geotortue.core;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import org.nfunk.jep.Variable;
import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.function.PostfixMathCommandI;

import color.GTColor;
import color.GTColorFunctionFactory;
import color.GTColors;
import files.GTUserFileManager;
import function.JFunction;
import fw.app.FWConsole;
import fw.app.Translator;
import fw.app.Translator.TKey;
import fw.geometry.util.MathUtils;
import fw.gui.FWLabel;
import fw.gui.FWRadioButtons;
import fw.gui.FWRadioButtons.FWRadioButtonKey;
import fw.gui.FWRadioButtons.FWRadioButtonsListener;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWInteger;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.geometry.GTGeometry;
import geotortue.gui.GTMonitor;
import jep2.JEP2;
import jep2.JKey;
import sound.MusicEvent;
import sound.MusicFunctionFactory;
import type.JAssignment;
import type.JBoolean;
import type.JDict;
import type.JDouble;
import type.JHashTable;
import type.JInteger;
import type.JList;
import type.JMethod;
import type.JObjectI;
import type.JObjectsVector;
import type.JSlicer;
import type.JString;


public class GTEnhancedJEP  extends GTJEP  implements XMLCapabilities, FWSettings {

	private static final TKey NAME = new TKey(GTEnhancedJEP.class, "settings");
	private static final TKey PRECISION = new TKey(GTEnhancedJEP.class, "precision");

	private static final TKey TRIGO_MODE = new TKey(GTEnhancedJEP.class, "trigoMode");
	private static final FWRadioButtonKey DEGREE = new FWRadioButtonKey(GTEnhancedJEP.class, AngleMode.Degree.name());
	private static final FWRadioButtonKey RADIAN = new FWRadioButtonKey(GTEnhancedJEP.class, AngleMode.Radian.name());

	private static final TKey SCOPE_MODE = new TKey(GTEnhancedJEP.class, "scopeMode");

	private static final FWRadioButtonKey GLOBAL_SCOPES = new FWRadioButtonKey(GTEnhancedJEP.class, ScopeMode.Global.name());
	private static final FWRadioButtonKey LOCAL_SCOPES = new FWRadioButtonKey(GTEnhancedJEP.class, ScopeMode.Local.name());

//	// TODO : (done) set global / python mode
	// TODO : (done) dict
	
	private final FWInteger precision = new FWInteger("precision", 4, 2, 10);
	private List<String> turtles = Collections.synchronizedList(new ArrayList<String>());
	
	private GTJEPFunctionFactory funFactory;
	private final Stack<String[]> argumentStack = new Stack<>();
	private final Vector<String> loopVarNames = new Vector<>();
	private final GTMonitor monitor;
	private final KeywordManager keywordManager;
	private final GTCommandFactory commandFactory;
	
	public GTEnhancedJEP(GTGeometry g, TurtleManager tm, KeywordManager km, GTCommandFactory cf, GTUserFileManager fileManager) {
		this.keywordManager = km;
		this.funFactory = new GTJEPFunctionFactory(tm, fileManager);
		this.monitor = new GTMonitor(this, km);
		this.commandFactory = cf;
		init(g, tm);
	}
	
	public void init(GTGeometry g, TurtleManager tm){
		initSymTab();
		initFunTab();
		addStandardFunctions();
		addStandardConstants();
		g.addFunctions(this);
		userFunctions = new Vector<>();
		updateMonitor();
	}
	
	public void addFunction(String functionName, PostfixMathCommandI function) {
		FWConsole.printWarning(this, "addFunction() "+functionName+" is not a GTFunction");
		super.addFunction(functionName, function);
	}
	
	public void addGFunction(JKey key, int numOfParameters, final GFunctionI gFunctionI) {
		addFunction(funFactory.createGFunction(key, numOfParameters, gFunctionI));
	}

	@Override
	public void addStandardFunctions() {
		super.addStandardFunctions();
		for (JFunction fun : funFactory.getGTFunctions()) 
			addFunction(fun);
		for (JFunction fun : GTColorFunctionFactory.getFunctions()) 
			addFunction(fun);
	}

	public void addStandardConstants() {
		super.addStandardConstants();
		addTurtles();
		GTColors.addColors(this);
	}
	
	public void enableMusicItems(boolean b) {
		if (b)
			for (JFunction fun : MusicFunctionFactory.getFunctions())
				addFunction(fun);
		else
			for (JFunction fun : MusicFunctionFactory.getFunctions()) {
				String name = fun.getName();
				removeFunction(name);
				keywordManager.removeFunction(name);
			}
	}

	
	public void removeItem(SourceLocalization bundle) throws GTException {
		String name = bundle.getText();
		Object o = removeVariable(name);
		if (o!=null) {
			updateMonitor();
			return;
		} 
		
		o = removeFunction(name);
		if (o!=null) {
			keywordManager.removeFunction(name);
			updateMonitor();
		}
	}
	
	/*
	 * Turtles 
	 */
	
	/**
	 * @param turtles
	 */
	public void updateTurtles(List<Turtle> newTurtles) {
		for (String turtle : turtles) 
			removeVariable(turtle);
		
		turtles.clear();
		for (int idx = 0; idx < newTurtles.size(); idx++) {
			Turtle t = newTurtles.get(idx);
			String name = t.getName();
			turtles.add(name);
			addConstant(name, JEP2.createNumber(idx));
		} 
	}
	
	private void addTurtles() {
		int index = 0;
		for (String name : turtles) {
			Variable v = getVar(name);
			if (v==null) {
				addConstant(name, JEP2.createNumber(index));
				index++;
			}
		}
	}
	
	@Deprecated
	public boolean isTurtleName(String key) {
		return turtles.contains(key);
	}
	
	/*
	 * 
	 */
	
	public double convertToRadians(double x) {
		if (getMode() == AngleMode.Degree)
			return x * Math.PI /180;
		return x;
	}

	public double convertToCurrentAngleMode(double x) {
		if (getMode() == AngleMode.Degree) 
			return x * 180 / Math.PI;
		return x;
	}
	
	public void addFunction(JFunction function) {
		super.addFunction(function);
		String key = function.getName();
		keywordManager.addFunction(key);
	}

	/**
	 * @param varName
	 * @param str
	 * @throws GTException 
	 * @throws GTInterruptionException 
	 * @throws JEPException 
	 */
	public void assignVariable(GTProcessingContext context, String varName, SourceLocalization loc) throws GTInterruptionException, GTException, JEPException {
		JObjectI<?> value = getJObject(context, loc);
		Variable var = getSymbolTable().makeVarIfNeeded(varName);
		if (var.isConstant())
			throw new JEPException(JEP2Trouble.JEP2_CANNOT_CHANGE_A_CONSTANT, var.getName());
		var.setValue(value);
	}

	/*
	 * User function
	 */
	
	private Vector<String> userFunctions = new Vector<>();

	public UserFunction addUserFunction(GTProcessingContext context, String key, String def, String[] vars) {
		UserFunction fun = super.addUserFunction(context, key, def, vars);
		userFunctions.add(key);
		monitor.addFunction(key, fun);
		keywordManager.addFunction(key);
		return fun;
	}
	
	public Vector<String> getUserFunctions() {
		return userFunctions;
	}


	/**
	 * 
	 */
	public String[] getProcedureArguments() {
		if (argumentStack.isEmpty())
			return new String[0];
		return argumentStack.peek();
	}
	
	/*
	 * Monitor
	 */
	
	@Override
	public void openLocalParser(String[] varNames, JObjectI<?>[] values) throws JEPException {
		super.openLocalParser(varNames, values);
		argumentStack.push(varNames);
		updateMonitor();
	}

	@Override
	public void closeLocalParser() {
		super.closeLocalParser();
		argumentStack.pop();
		updateMonitor();
	}
	
	public void addTempVariable(String name, Object o) {
		addVariable(name, o);
		loopVarNames.add(name);
	}
	
	public void removeLoopVariable(String name) {
		loopVarNames.remove(name);
	}

	public String[] getLoopVarNames() {
		String[] names = new String[loopVarNames.size()];
		return loopVarNames.toArray(names);
	}

	public JComponent getMonitorPane() {
		return monitor;
	}

	private void updateMonitor() {
		monitor.setSymbolTable(getSymbolTable());
	}

	protected void handleAssignment(JAssignment assignment) throws JEPException {
		super.handleAssignment(assignment);
		String name = assignment.getVarName();
		try {
			keywordManager.testValidity(SourceLocalization.create(name, null));
		} catch (GTException ex) {
			if (funTab.get(name) != null)  // workaround to avoid function assignment
				updateMonitor();
			throw new JEPException(ex.getTrouble(), name);
		}
	}

	public String format(JObjectI<?> o) {
		switch (o.getType()) {
		case DOUBLE:
			Double d = ((JDouble) o).getValue();
			return MathUtils.format(d, precision.getValue());
		case LIST :
			return format((JList) o);
		case STRING :
			return format((JString) o);
		case DICT:
			return format((JDict) o);
		case BOOLEAN:
			boolean b = ((JBoolean) o).getValue();
			return b ? TRUE.translate() : FALSE.translate(); 
		case ASSIGNMENT:
		case NULL:
			return "null";
		case LONG:
			return ((JInteger) o).getValue()+"";
		case METHOD:
			return "method : "+((JMethod) o).getValue().getName();
		case SLICER:
			return "slicer "+((JSlicer) o).toString();
		case MUSIC:
			return ((MusicEvent) o).format();
		case COLOR:
			return ((GTColor) o).format();
		}
	return "unknown type : "+o.toString();
	}
	
	private String format(JList list) {
		JObjectsVector v = list.getValue();
		if (v.isEmpty()) 
			return "[]";

		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Translator.getLocale());
		char decimalSep = format.getDecimalFormatSymbols().getDecimalSeparator();
		char sep = decimalSep == ',' ? ';' : ',';

		int idx = 0;
		int size = v.size();

		boolean flag = true;
		String str = "[ ";
		while (idx<size) {
			JObjectI<?> ob = v.elementAt(idx);
			str += format(ob)+sep+" ";
			idx++;
			if (idx>50 && flag) {
				str += sep+" [...] ";
				idx = size-5;
				flag = false;
			}
		}
		str = str.substring(0, str.length()-2);
		str += " ] ";
		return str;
	}
	
	private String format(JString o){
		String text = o.getValue();
		int len = text.length(); 
		if (len>=500)
			text = text.substring(0, 400)+" [...] " + text.substring(len-20);
		return "\""+text+"\"";
	}
	
	private String format(JDict o){
		JHashTable table = o.getValue();
		if (table.isEmpty()) 
			return "{}";
		
		JObjectsVector vec = table.getKeys();
		
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Translator.getLocale());
		char decimalSep = format.getDecimalFormatSymbols().getDecimalSeparator();
		char sep = decimalSep == ',' ? ';' : ',';
		
		int size = table.size();
		int idx = 0;
		
		String str = "{ ";
		boolean flag = true;
		while (idx<size) {
			JObjectI<?> ob = vec.elementAt(idx);
			try {
				str += format(ob)+" : "+format(table.get(ob))+sep+" ";
			} catch (JEPException ex) { // cannot occur
				ex.printStackTrace();
			}
			idx++;
			if (idx>50 && flag) {
				str += sep+" [...] ";
				idx = size-5;
				flag = false;
			}
		}
		str = str.substring(0, str.length()-2);
		str += " } ";
		return str;
	}
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "GTJEP";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("mode", getMode().name());
		e.setAttribute("scopeMode", scopeMode.name());
		precision.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		String m = child.getAttribute("mode", AngleMode.Degree.name());
		try {
			setMode(AngleMode.valueOf(m));
		} catch (IllegalArgumentException ex) {
			setMode(AngleMode.Degree);
		}
		m = child.getAttribute("scopeMode", ScopeMode.Global.name());
		try {
			scopeMode = ScopeMode.valueOf(m);
		} catch (IllegalArgumentException ex) {
			scopeMode = ScopeMode.Global;
		}
		precision.fetchValue(child, 4);
		return child;
	}
	
	/*
	 * FWS
	 */
	
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		JSpinner precSpinner = precision.getComponent();
		
		FWRadioButtons trigoModeRB = new FWRadioButtons(new FWRadioButtonsListener() {
			public void selectionChanged(String key) {
				setMode(AngleMode.valueOf(key));
			}
		}, DEGREE, RADIAN);

		trigoModeRB.setSelected(getMode().name());

		FWRadioButtons scopeModeRB = new FWRadioButtons(new FWRadioButtonsListener() {
			public void selectionChanged(String key) {
				scopeMode = ScopeMode.valueOf(key);
				commandFactory.setGlobalEnabled(scopeMode == ScopeMode.Local);
			}
		}, GLOBAL_SCOPES, LOCAL_SCOPES);

		scopeModeRB.setSelected(scopeMode.name());

		return VerticalPairingLayout.createPanel(10, 10,
				new FWLabel(PRECISION, SwingConstants.RIGHT), precSpinner,
				new FWLabel(TRIGO_MODE, SwingConstants.RIGHT), trigoModeRB, 
				new FWLabel(SCOPE_MODE, SwingConstants.RIGHT), scopeModeRB);
	}
	
	
	@Override
	public TKey getTitle() {
		return NAME;
	}
}
