package geotortue.core;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.text.MutableAttributeSet;

import color.GTColors;
import fw.app.Translator.TKey;
import fw.gui.FWComboBox;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsAction;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.text.FWParsingTools;
import fw.text.FWParsingTools.ParsingException;
import fw.text.FWStyledKeySet;
import fw.text.FWStylesManager;
import fw.text.FWSyntaxDocument.UpdatesCollector;
import fw.text.TextStyle;
import fw.text.TextStyleWColorP;
import fw.xml.XMLTagged;
import geotortue.core.GTCommandFactory.GTCommandKey;
import geotortue.core.GTMessageFactory.GTTrouble;


public class KeywordManager extends FWStylesManager implements FWSettings {
	

	private static final TKey NAME = new TKey(KeywordManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey STRINGS = new TKey(KeywordManager.class, "strings");
	private static final TKey LIBRARY = new TKey(KeywordManager.class, "library");
	private static final TKey TURTLES = new TKey(KeywordManager.class, "turtles");
	private static final TKey LOOP_VARIABLES = new TKey(KeywordManager.class, "loopVariables");
	private static final TKey ARGUMENTS = new TKey(KeywordManager.class, "arguments");
	private static final TKey PROCEDURES = new TKey(KeywordManager.class, "procedures");
	private static final TKey COMMANDS = new TKey(KeywordManager.class, "commandKeys");
	private static final TKey RESERVED = new TKey(KeywordManager.class, "reservedKeys");
	private static final TKey COMMENTS = new TKey(KeywordManager.class, "comments");
	private static final TKey NORMAL_FONT = new TKey(KeywordManager.class, "normalFont");
	private static final TKey FONT_SIZE = new TKey(KeywordManager.class, "fontSize");
	private static final TKey FONT_FAMILY = new TKey(KeywordManager.class, "fontFamily");

	private final static XMLTagged TAG = XMLTagged.Factory.create("KeywordManager");
	
	public final static TKey START_KEY = new TKey(KeywordManager.class, "pour"), 
						END_KEY = new TKey(KeywordManager.class, "fin"),
						THEN_KEY = new TKey(KeywordManager.class, "alors"),
						ELSE_KEY = new TKey(KeywordManager.class, "sinon"),
						ALL_KEY = new TKey(KeywordManager.class, "toutes"),
						FROM_KEY = new TKey(KeywordManager.class, "de"),
						TO_KEY = new TKey(KeywordManager.class, "à"),
						EVAL_KEY = new TKey(KeywordManager.class, "eval"),
						IN_LIST_KEY = new TKey(KeywordManager.class, "in_list"), 
						TAKE_THE_VALUE_KEY = new TKey(KeywordManager.class, "prend_la_valeur"),
						QUESTION_KEY = new TKey(KeywordManager.class, "?");
	
	private final static TKey[] RESERVED_KEYS = new TKey[]{START_KEY, END_KEY, THEN_KEY, ELSE_KEY, ALL_KEY, 
			FROM_KEY, TO_KEY, EVAL_KEY, IN_LIST_KEY, TAKE_THE_VALUE_KEY, QUESTION_KEY};
	
	final static TKey TRANSLATION_KEY = new TKey(KeywordManager.class, "translation"),
						ROTATION_KEY = new TKey(KeywordManager.class, "rotation"),
						ZOOM_KEY = new TKey(KeywordManager.class, "zoom"),
						H_TRANSLATION_KEY = new TKey(KeywordManager.class, "htranslation"),
						H_ROTATION_KEY = new TKey(KeywordManager.class, "hrotation"),
						Q_TRANSLATION_KEY = new TKey(KeywordManager.class, "qtranslation"),
						TURTLE_VISION_KEY = new TKey(KeywordManager.class, "vision"),
						XY_ROTATION_KEY = new TKey(KeywordManager.class, "xy"),
						XZ_ROTATION_KEY = new TKey(KeywordManager.class, "xz"),
						YZ_ROTATION_KEY = new TKey(KeywordManager.class, "yz"),
						DELAY_KEY = new TKey(KeywordManager.class, "delay");
	
	private final static TKey[] MG_KEYS = new TKey[]{TRANSLATION_KEY, ROTATION_KEY, ZOOM_KEY, H_TRANSLATION_KEY, H_ROTATION_KEY, 
			Q_TRANSLATION_KEY, TURTLE_VISION_KEY, XY_ROTATION_KEY, XZ_ROTATION_KEY, YZ_ROTATION_KEY, DELAY_KEY};

	final static TKey RVB_KEY = new TKey(KeywordManager.class, "RVB"),
			rvb_KEY = new TKey(KeywordManager.class, "rvb"),
			TSV_KEY = new TKey(KeywordManager.class, "TSV"),
			tsv_KEY = new TKey(KeywordManager.class, "tsv");


	private final static TKey[] COLOR_KEYS = new TKey[]{RVB_KEY, rvb_KEY, TSV_KEY, tsv_KEY};

	public final static GTFontStyle DEFAULT_STYLE = new GTFontStyle(TAG, "default", Color.BLACK);
	public final static TextStyleWColorP COMMENTS_STYLE = new TextStyleWColorP(TAG, "comments", Color.GRAY); 
	public final static TextStyleWColorP STRINGS_STYLE = new TextStyleWColorP(TAG, "strings", new Color(255, 0, 192)); 
	public final static TextStyle BOLD_STYLE = new TextStyle();
	
	private final static TextStyleWColorP RESERVED_STYLE = new TextStyleWColorP(TAG, "reservedKeys", new Color(0x990033));
	private final static TextStyleWColorP COMMANDS_STYLE = new TextStyleWColorP(TAG, "commands", new Color(0x000099));
	private final static TextStyleWColorP PROCS_STYLE = new TextStyleWColorP(TAG, "procedures", new Color(0x990099));
	private final static TextStyleWColorP ARGUMENTS_STYLE = new TextStyleWColorP(TAG, "localVariables", 	new Color(0x009900));
	private final static TextStyleWColorP LOOP_VARIABLES_STYLE = new TextStyleWColorP(TAG, "loopVariables", new Color(0x666600));
	private final static TextStyleWColorP FUN_STYLE = new TextStyleWColorP(TAG+".functions", new Color(0x000000));
	private final static TextStyleWColorP TURTLES_STYLE = new TextStyleWColorP(TAG, "turtles", new Color(0x506e50));
	private final static TextStyleWColorP LIBRARY_STYLE = new TextStyleWColorP(TAG, "library", new Color(0x993300));
	
	
	private final FWStyledKeySet<TextStyleWColorP> reservedKeys = new FWStyledKeySet<TextStyleWColorP>(RESERVED_STYLE);
	private final FWStyledKeySet<TextStyleWColorP> commands = new FWStyledKeySet<TextStyleWColorP>(COMMANDS_STYLE);
	private final FWStyledKeySet<TextStyleWColorP> procedureKeys = new FWStyledKeySet<TextStyleWColorP>(PROCS_STYLE);
	private final FWStyledKeySet<TextStyleWColorP> functionsKeys = new FWStyledKeySet<TextStyleWColorP>(FUN_STYLE);
	private final FWStyledKeySet<TextStyleWColorP> turtlesKeys= new FWStyledKeySet<TextStyleWColorP>(TURTLES_STYLE);
	private final FWStyledKeySet<TextStyleWColorP> libraryKeys = new FWStyledKeySet<TextStyleWColorP>(LIBRARY_STYLE);
	
	private final FWStyledKeySet<TextStyle> boldKeys= new FWStyledKeySet<TextStyle>(BOLD_STYLE);	
	
	private final FWStyledKeySet<?>[] keySets = new FWStyledKeySet<?>[]{reservedKeys, commands, procedureKeys,
		functionsKeys, turtlesKeys, libraryKeys, boldKeys};

	
	private final Set<String> completionKeys = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * Constructor
	 */
	public KeywordManager(){
		super(DEFAULT_STYLE, COMMENTS_STYLE, STRINGS_STYLE, 
				RESERVED_STYLE, COMMANDS_STYLE, PROCS_STYLE, FUN_STYLE, 
				TURTLES_STYLE, LIBRARY_STYLE, BOLD_STYLE,
				ARGUMENTS_STYLE, LOOP_VARIABLES_STYLE);
		
		for (GTCommandKey key : GTCommandKey.values()) {
			String name = GTCommandDescTable.getName(key);
			if (GTCommandDescTable.isCommand(key))
				commands.add(name);
			else
				reservedKeys.add(name);
		}
		
		for (TextStyleWColorP s : new TextStyleWColorP[]{RESERVED_STYLE, COMMANDS_STYLE, PROCS_STYLE, 
				ARGUMENTS_STYLE, LOOP_VARIABLES_STYLE, TURTLES_STYLE, LIBRARY_STYLE})
			s.setBold(true);
		
		for (TKey key : RESERVED_KEYS) 
			reservedKeys.add(key.translate());	
		
		for (TKey key : MG_KEYS) 
			boldKeys.add(key.translate());
		
		boldKeys.add("•");
		
		for (TKey key : COLOR_KEYS) 
			boldKeys.add(key.translate());
		
		for (String str : GTColors.getColorNames())
			boldKeys.add(str);
		
		
		for (String str : GTEnhancedJEP.getLogicalOps())
			boldKeys.add(str);
		
		BOLD_STYLE.setBold(true);
		FUN_STYLE.setItalic(true);
		
		updateFont();
	}
	
	public GTFontStyle getDefaultStyle() {
		return DEFAULT_STYLE;
	}

	public MutableAttributeSet getLocalVariablesAttributeSet() {
		return ARGUMENTS_STYLE;
	}

	public MutableAttributeSet getLoopVariablesAttributeSet() {
		return LOOP_VARIABLES_STYLE;
	}
	
	/*
	 * 
	 */
	
	static enum Filter {NO_OMITION, OMIT_FUNCTION };
	
	String testValidity(SourceLocalization loc, Filter filter) throws GTException {
		String key = loc.getText();
		if (key.length()<1)
			new Exception("empty symbol").printStackTrace();

		if (FWParsingTools.containsDelimiter(key))
			throw new GTException(GTTrouble.GTJEP_SYMBOL_CONTAINS_DELIMITERS, loc, key);
		
		if (Character.isDigit(key.charAt(0)))
			throw new GTException(GTTrouble.GTJEP_SYMBOL_DIGITS, loc, key);
		
		if (reservedKeys.getKeys().contains(key))
			throw new GTException(GTTrouble.GTJEP_CONFLICT_KEYWORD, loc, key);

		if (commands.getKeys().contains(key)) {
			throw new GTException(GTTrouble.GTJEP_CONFLICT_KEYWORD, loc, key);
		}
		if (procedureKeys.getKeys().contains(key))
			throw new GTException(GTTrouble.GTJEP_CONFLICT_PROCEDURE, loc, key);

		if (libraryKeys.getKeys().contains(key))
			throw new GTException(GTTrouble.GTJEP_CONFLICT_LIBRARY, loc, key);
		
		if (turtlesKeys.getKeys().contains(key))
			throw new GTException(GTTrouble.GTJEP_CONFLICT_TURTLE, loc, key);
		
		if (filter!= Filter.OMIT_FUNCTION && functionsKeys.getKeys().contains(key))
			throw new GTException(GTTrouble.GTJEP_CONFLICT_FUNCTION, loc, key);
		
		return key;
	}
	
	String testValidity(SourceLocalization loc) throws GTException {
		return testValidity(loc, Filter.NO_OMITION);
	}
	
	public void updateCompletionKeys() {
		new Thread(new Runnable() {
			public void run() {
				Vector<String> tmp = new Vector<>();
				addSynchronized(commands, tmp);
				addSynchronized(reservedKeys, tmp);
				addSynchronized(turtlesKeys, tmp);
				addSynchronized(procedureKeys, tmp);
				addSynchronized(libraryKeys, tmp);
				addSynchronized(functionsKeys, tmp);
				
				for (TKey key : MG_KEYS)
					tmp.add(key.translate());
				for (TKey key : RESERVED_KEYS)
					tmp.add(key.translate());
				for (String str : GTColors.getColorNames())
					tmp.add(str);
				
				synchronized (completionKeys) {
					completionKeys.clear();
					completionKeys.addAll(tmp);
				}
			}
		}).start();
	}
	
	private void addSynchronized(FWStyledKeySet<?> ks, Vector<String> v) {
		Set<String> s = ks.getKeys();
		synchronized (s) {
			v.addAll(s);
		}
	}
	
	/**
	 * @return the keySets
	 */
	public FWStyledKeySet<?>[] getKeySets() {
		return keySets;
	}
	
	/**
	 * @return the completionKeys
	 */
	public Set<String> getCompletionKeys() {
		return completionKeys;
	}
	
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		int RIGHT = SwingConstants.RIGHT;

		FWSettingsAction updateFontAction = new FWSettingsAction() {
			@Override
			public void fire() {
				updateFont();
			}
		};
		
		FWComboBox fontFamilyCB = DEFAULT_STYLE.getFontFamilyComboBox(updateFontAction);
		JSpinner fontSizeSpinner = DEFAULT_STYLE.getFontSizeSpinner(updateFontAction);
		JPanel fontSettings = VerticalPairingLayout.createPanel(10, 10,
				new FWLabel(FONT_FAMILY, RIGHT), fontFamilyCB,
				new FWLabel(FONT_SIZE, RIGHT), fontSizeSpinner);
		
		
		JPanel colorSettings =  VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(NORMAL_FONT, RIGHT), DEFAULT_STYLE.getColorBox(updateFontAction),
				new FWLabel(COMMENTS, RIGHT), COMMENTS_STYLE.getColorBox(updateFontAction),
				new FWLabel(RESERVED, RIGHT), reservedKeys.getStyle().getColorBox(updateFontAction),
				new FWLabel(COMMANDS, RIGHT), commands.getStyle().getColorBox(updateFontAction),
				new FWLabel(PROCEDURES, RIGHT), procedureKeys.getStyle().getColorBox(updateFontAction),
				new FWLabel(ARGUMENTS, RIGHT), ARGUMENTS_STYLE.getColorBox(updateFontAction),
				new FWLabel(LOOP_VARIABLES, RIGHT), LOOP_VARIABLES_STYLE.getColorBox(updateFontAction),
				new FWLabel(TURTLES, RIGHT), turtlesKeys.getStyle().getColorBox(updateFontAction), 
				new FWLabel(LIBRARY, RIGHT), libraryKeys.getStyle().getColorBox(updateFontAction),
				new FWLabel(STRINGS, RIGHT), STRINGS_STYLE.getColorBox(updateFontAction));
		
		return VerticalFlowLayout.createPanel(fontSettings, colorSettings);
	}
	

	public void updateFont() {
		super.updateFont();
		DEFAULT_STYLE.refresh();
	}

	/*
	 * Procedures
	 */

	public void addProcedure(String key) {
		procedureKeys.add(key);
	}

	public void removeProcedure(String key) {
		procedureKeys.remove(key);
	}

	public void clearProcedures() {
		procedureKeys.clear();
	}

	/*
	 * LibraryKeys
	 */
	
	public void addLibrary(String key) {
		libraryKeys.add(key);
	}

	public void removeLibrary(String key) {
		libraryKeys.remove(key);
	}

	public void clearLibrary() {
		libraryKeys.clear();		
	}

	/*
	 * Functions
	 */
	
	public void addFunction(String key) {
		functionsKeys.add(key);
		
	}

	public void removeFunction(String key) {
		functionsKeys.remove(key);
	}

	/*
	 * Turtles
	 */
	
	public void addTurtle(String key) {
		turtlesKeys.add(key);
	}

	public void removeTurtle(String key) {
		turtlesKeys.remove(key);
	}

	public void clearTurtles() {
		turtlesKeys.clear();
	}
	
	/**
	 * 
	 */
	public String addHTMLTags(String str) throws ParsingException {
		String[] keys = reservedKeys.getKeysAsArray();
		String html = FWParsingTools.surroundTokens(str, keys, "<span class=\"keyword\">", "</span>");

		keys = commands.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"command\">", "</span>");

		keys = procedureKeys.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"procedure\">", "</span>");

		keys = libraryKeys.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"library\">", "</span>");

		keys = functionsKeys.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"function\">", "</span>");

		keys = turtlesKeys.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"turtle\">", "</span>");

		keys = boldKeys.getKeysAsArray();
		html = FWParsingTools.surroundTokens(html, keys, "<span class=\"function\">", "</span>");
		return html;
	}

	public void addProcedure(String key, UpdatesCollector collector) {
		addProcedure(key);
		collector.addToken(key, procedureKeys);	
	}

	public void addCommand(String name, boolean isCommand) {
		if (isCommand)
			commands.add(name);
		else
			reservedKeys.add(name);
	}
	
	public void removeCommand(String name, boolean isCommand) {
		if (isCommand)
			commands.remove(name);
		else
			reservedKeys.remove(name);
	}
}