/**
 * 
 */
package geotortue.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import fw.app.Translator.TKey;
import fw.gui.FWTitledPane;
import fw.gui.layout.BasicLayoutAdapter;
import fw.gui.tabs.FWTabItemSupplier;
import fw.text.FWEnhancedTextPane;
import fw.text.FWSyntaxDocument;
import geotortue.gui.Divider;
import geotortue.gui.Divider.DIRECTION;

/**
 *
 */
public class GTModelTabs implements FWTabItemSupplier<GTModel> {

	private static final TKey GRAPHICS = new TKey(GTModelTabs.class, "graphics");
	private static final TKey HTML_EDITOR = new TKey(GTModelTabs.class, "htmlEditor");
	private static final TKey TEXT = new TKey(GTModelTabs.class, "text");
	private static final TKey COMMAND = new TKey(GTModelTabs.class, "command");
	
	private final GTModelManager modelManager;
	private final JPanel pane;
	private final JPanel graphicPane;
	private final FWEnhancedTextPane htmlEditor; 
	private final JPanel htmlPane;
	private GTModel currentModel;
	
	public GTModelTabs(GTModelManager m) {
		this.modelManager = m;
		this.pane = new JPanel(new EditorLayout());
		
		JScrollPane graphics = new JScrollPane(modelManager.getGraphicSpace().getPane());
		this.graphicPane = new FWTitledPane(GRAPHICS, graphics);
		
		this.htmlEditor = new FWEnhancedTextPane(new GTModelHTMLDocument());
		this.htmlEditor.setTabulationLength(2);
		this.htmlPane = new FWTitledPane(HTML_EDITOR, new JScrollPane(htmlEditor));
	}
	
	public void refresh() {
		modelManager.refreshGraphics(currentModel);
		currentModel.setText(htmlEditor.getText());
		Document doc = currentModel.getCommandTextPane().getDocument();
		if (doc instanceof FWSyntaxDocument)
			((FWSyntaxDocument) doc).refresh();
	}
	
	@Override
	public JPanel getPane(GTModel model) {
		if (currentModel!=null)
			refresh();
		
		this.currentModel = model;
		pane.removeAll();
		
		pane.add(graphicPane);

		JScrollPane html = new JScrollPane(model.getHTMLPane());
		pane.add(new FWTitledPane(TEXT, html));
	
		JScrollPane command = new JScrollPane(model.getCommandTextPane());
		pane.add(new FWTitledPane(COMMAND, command));
	
		pane.add(divider);
	
		htmlEditor.setText(model.getHTMLCode());
		
		pane.add(htmlPane);
		htmlEditor.setCaretPosition(0);
		
		
		pane.validate();
		
		modelManager.refreshGraphics(model);
		return pane;
	}
	
	@Override
	public String getTitle(GTModel model) {
		return model.getTitle();
	}

	@Override
	public GTModel addNewItem() {
		return modelManager.addNewModel();
	}
	
	@Override
	public Vector<GTModel> getItems() {
		return modelManager.getModels();
	}

	@Override
	public boolean removeItem(GTModel item) {
		return modelManager.remove(item);
	}

	@Override
	public boolean moveItem(GTModel item, int idx) {
		return modelManager.move(item, idx);
	}
	
	@Override
	public GTModel copyItem(GTModel item) {
		return modelManager.copy(item);
	}
	

	/*
	 * Layout
	 */

	private class EditorLayout extends BasicLayoutAdapter {
		protected int gap = 4;
		private int commandHeight = -1;
		
		public void layoutContainer(Container parent) {
			init(parent);
			Component[] components = parent.getComponents();
			
			Dimension d = modelManager.getGraphicSpace().getSize();
			int graphicsW = d.width + 4;
			int w = Math.max(parentW - graphicsW - gap, 200);
			graphicsW = parentW - w - gap;

			int graphicsH = Math.min(d.height + 28, parentH-120);
			
			Component c = components[0]; // graphics
			c.setBounds(currX, currY, graphicsW, graphicsH);
			
			c = components[1];  // htmlPane
			currY += graphicsH + gap;
			c.setBounds(currX, currY, graphicsW, parentH-graphicsH-gap);
			
			c = components[2]; // commandEditor
			currX += graphicsW+gap;
			currY = insets.top;
			if (commandHeight<0)
				commandHeight = 128;
			c.setBounds(currX, currY, parentW-graphicsW-gap, commandHeight);
			
			c = components[3]; // divider
			currY += commandHeight ;
			c.setBounds(currX, currY, parentW-graphicsW-gap, gap);
			
			
			c = components[4]; // textEditor
			currY += gap;
			c.setBounds(currX, currY, parentW-graphicsW-gap, parentH-commandHeight);
			

		}

		@Override
		public void layoutComponent(Component c, int idx) {}

		
		public void setCommandHeight(int h) {
			if (h<36)
				commandHeight = 26;
			else if (h>parentH-40)
				commandHeight = parentH-30;
			else
				commandHeight = h;
			
		}
	}
	
	private Divider divider = new Divider(DIRECTION.S) {
		private static final long serialVersionUID = -6742979139681458126L;

		@Override
		public int captureRef() {
			return ((EditorLayout) pane.getLayout()).commandHeight;
		}

		@Override
		public void resize(int x) {
			((EditorLayout) pane.getLayout()).setCommandHeight(x);
			invalidate();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					pane.validate();
				}
			});
		}
	};
	

	Document getHtmlDoc() {
		return htmlEditor.getDocument();
	}


}
