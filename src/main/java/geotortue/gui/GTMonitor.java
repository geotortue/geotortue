/**
 * 
 */
package geotortue.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

import org.nfunk.jep.FunctionTable;
import org.nfunk.jep.Variable;

import fw.gui.layout.VerticalFlowLayout;
import fw.text.FWEnhancedDocument;
import geotortue.core.GTCodeDocument;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTFontStyle;
import geotortue.core.GTJEP.UserFunction;
import geotortue.core.KeywordManager;
import geotortue.core.SymbolTable2;
import geotortue.core.SymbolTableListener;
import type.JMutable;
import type.JMutableListener;
import type.JObjectI;

/**
 *
 */
public class GTMonitor extends JScrollPane implements SymbolTableListener, JMutableListener {

	private static final long serialVersionUID = -8200678222360128700L;

	private SymbolTable2 symbolTable; 
	
	private final KeywordManager keywordManager;
	private final MutableAttributeSet argumentStyle;
	private final MutableAttributeSet loopVarStyle;
	private final MonitorList  list;
	private final GTEnhancedJEP jep;


	private final Observer variableObserver = new Observer() {
		@Override
		public void update(Observable o, Object arg) {
			Variable v = (Variable) o;
			add(v.getName(), v.getValue());
		}
	};
	
	private final static Color BG_COLOR = new Color(248,  248, 241);
	
	public GTMonitor(GTEnhancedJEP j, KeywordManager km) {
		this.jep = j;
		this.symbolTable = jep.getSymbolTable();
		this.keywordManager = km;
		this.argumentStyle = km.getLocalVariablesAttributeSet();
		this.loopVarStyle = km.getLoopVariablesAttributeSet();
		this.list = new MonitorList();
		
		setEnabled(false);
		
		setViewportView(list);
		getHorizontalScrollBar().setUnitIncrement(16);
		getVerticalScrollBar().setUnitIncrement(16);
	}
	
	private Stack<Runnable> symbolTableUpdates = new Stack<>(); 
	
	public void setSymbolTable(final SymbolTable2 st) {
		if (!isEnabled())
			return;

		symbolTableUpdates.push(new Runnable() {
			@Override
			public void run() {
				disableObservation();
				GTMonitor.this.symbolTable = st;
				if (isEnabled())
					enableObservation();
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				if (symbolTableUpdates.isEmpty())
					return;
				symbolTableUpdates.peek().run();
				symbolTableUpdates.clear();
			}
		});
		
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!isEnabled() && enabled ) 
			symbolTable = jep.getSymbolTable();
		
		super.setEnabled(enabled);
		if (enabled)
			enableObservation();
		else
			disableObservation();
	}
	
	private void disableObservation() {
		clear();
		symbolTable.deleteObserver(this);
	}
	
	private void enableObservation() {
		symbolTable.addObserver(this);
		Object[] objs = symbolTable.keySet().toArray();
		for (Object o : objs) {
			String key = (String) o;
			Variable var = symbolTable.getVar(key);
			if (var != null && ! var.isConstant()) {
				Object v = symbolTable.getValue(o);
				add(key, v);
				var.addObserver(variableObserver);
			}
		}
		
		FunctionTable funTab = jep.getFunctionTable();
		for (String key : jep.getUserFunctions()) {
			UserFunction fun = (UserFunction) funTab.get(key);
			addFunction(key, fun);
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Variable v = (Variable) arg;
		v.addObserver(variableObserver);
		add(v.getName(), v.getValue());
	}

	@Override
	public void itemRemoved(final String name) {
		if (name.startsWith("_"))
			return ;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MonitorItem item = list.get(name);
				if (item==null)
					return;
				keywordManager.getDefaultStyle().unregister(item.variableDoc);
				list.removeElement(name);
			}
		});
	}
	
	@Override
	public void update(String name, JObjectI<?> val) {
		String value = name +" = "+jep.format(val);
		add(name, value);
	}

	
	private void clear() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GTFontStyle style = keywordManager.getDefaultStyle();
				for (MonitorItem item : list.values())
					style.unregister(item.variableDoc);
				list.clear();
			}
		});
	}
	
	private void add(final String name, final String text) {
		if (name.startsWith("_"))
			return ;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MonitorItem item = list.get(name);
				if (item == null) {
					item = new MonitorItem(name);
					list.addElement(name, item, text);
				} else
					item.setValue(text);
			}
		});
	}
	
	private void add(final String name, Object o) {
		if (o==null)
			return;
		
		JObjectI<?> val = (JObjectI<?>) o;
		if (val.isMutable())
			((JMutable) val).addJMutableListener(name, this);
		String value = name +" = "+jep.format(val);
		add(name, value);
	}
	
	public void addFunction(final String name, UserFunction f) {
		add(name, f.format());
	}

	/*
	 * Variable List
	 */
	
	private class MonitorList extends JPanel {
		private static final long serialVersionUID = -4193222490186389043L;
	
		private final TreeMap<String, MonitorItem> elements = new TreeMap<String, MonitorItem>();
	
		private MonitorList() {
			super(new VerticalFlowLayout(0));
			setFocusable(false);
			setBackground(BG_COLOR);
		}
	
		private int indexOf(String name) { 
			int idx = 0;
			for (String str : elements.keySet()) {
				if (str.equals(name))
					return idx;
				idx++;
			}
			return -1;
		}
	
		private void addElement(String name, MonitorItem item, String text) {
			elements.put(name, item);
			int index = indexOf(name);
			add(item.pane, index);
			item.setValue(text);
			update();
		}
	
		private void removeElement(String name) {
			int index = indexOf(name);
			elements.remove(name);
			remove(index);
			update();
		}
	
		private void clear() {
			if (isEmpty())
				return;
			elements.clear();
			removeAll();
			update();
		}
	
		private MonitorItem get(String name) {
			return elements.get(name);
		}
	
		private boolean isEmpty() {
			return elements.isEmpty();
		}
	
		private Collection<MonitorItem> values() {
			return elements.values();
		}
		
		private void update() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					invalidate();
					validate();
					repaint();
				}
			});
		}
	}

	private class MonitorItem {
		private final String name;
		private final ItemDocument variableDoc;
		private final JTextPane pane;
		private int updateCount = 0;

		private MonitorItem(String name) {
			this.name = name;
			this.variableDoc = new ItemDocument(keywordManager);
			this.pane = new MonitorItemPane(variableDoc);
		}
		
		private void setValue(final String text) {
			updateCount++;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateCount--;
					if (updateCount>0) 
						return;
					variableDoc.setText(text); 
					list.update();
				}
			});
		}
		
		private class ItemDocument extends GTCodeDocument {
			private static final long serialVersionUID = 4400188722068286975L;

			private ItemDocument(KeywordManager km) {
				super(km);
			}
			
			public void setText(String t) {
				String old = getText();
				int oldLen = old.length();
				String text = "• "+ t;
				
				int len = text.length();
				int max = Math.min(oldLen, len);
				boolean compare = true;
				int idx = 0;
				while (compare) {
					if (idx<max && text.charAt(idx) == old.charAt(idx))
						idx++;
					else 
						compare = false;
				}
				try {
					remove(idx, oldLen-idx);
					insertString(idx, text.substring(idx), null);
					getTextPane().setCaretPosition(0);
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
			

			@Override
			protected void customHighlight(String content, int _startOffset, int endOffset) {
				super.customHighlight(content, _startOffset, endOffset);
				for (String arg : jep.getProcedureArguments()) {
					if (name.equals(arg)) 
						setCharacterAttributes(2, name.length(), argumentStyle, true);
				}
				for (String arg : jep.getLoopVarNames()) {
					if (name.equals(arg)) 
						setCharacterAttributes(2, name.length(), loopVarStyle, true);
				}
				
			}
		}
	
		private class MonitorItemPane extends JTextPane {
	
			private static final long serialVersionUID = 7521478755871682043L;
	
			public MonitorItemPane(FWEnhancedDocument doc) {
				super(doc);
				setFocusable(false);
				setBackground(BG_COLOR);
				setEditable(false);
				doc.setOwner(this);
			}
	
			
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width += 30;
		        return d;
		        
			}
		}
	}
}