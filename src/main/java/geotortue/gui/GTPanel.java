package geotortue.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fw.app.FWAction;
import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWTitledButtonedPane;
import fw.gui.FWTitledPane;
import fw.gui.layout.BasicLayoutAdapter;
import fw.text.FWEnhancedTextPane;
import geotortue.core.GTDocumentFactory;
import geotortue.core.GTTextPaneWithButton;
import geotortue.gui.Divider.DIRECTION;
import geotortue.model.GTModelManager;
import geotortue.painter.GTPainter;
import geotortue.renderer.GTGraphicSpace;
import geotortue.sandbox.GTSandBox;


public class GTPanel extends JPanel implements GTPanelAssistant {
	private static final TKey COMMAND_PANE = new TKey(GTPanel.class, "CommandPane");
	private static final TKey PROC_PANE = new TKey(GTPanel.class, "ProcedurePane");
	private static final TKey MONITOR_PANE = new TKey(GTPanel.class, "MonitorPane");
	private static final TKey BOARD_PANE = new TKey(GTPanel.class, "BoardPane");
	private static final TKey FLUSH = new TKey(GTPanel.class, "flushCommands.tooltip");
	private static final TKey TOGGLE_MONITOR = new TKey(GTPanel.class, "hideMonitor.tooltip");
	private static final TKey TOGGLE_BOARD= new TKey(GTPanel.class, "hideBoard.tooltip");
	
	private static final long serialVersionUID = 3388790455042216913L;

	private final GTGraphicSpace graphicSpace;
	
	private final FWTitledButtonedPane commandPane;
	private final FWTitledPane procedurePane;
	private final FWTitledPane monitorPane, boardPane;
	private final JComponent focusedComponent;
	
	private final GTPainter painter;
	private final GTModelManager modelManager;
	private final GTSandBox sandBox;
	
	public enum LAYOUT_TYPE {STANDARD, PAINTER, SANDBOX, MODEL};
	private final Map<LAYOUT_TYPE, LayoutManager> layouts = new Hashtable<GTPanel.LAYOUT_TYPE, LayoutManager>();
	
	private boolean showMonitor = false, showBoard = false;

	public GTPanel(GTGraphicSpace gs, GTModelManager modelManager,
			final GTDocumentFactory docFactory, GTPainter p, GTSandBox sb, final JComponent monitor,
			final JComponent board, FWAction makeProcedureAction) {

		JButton flushButton = FWButton.createIconButton(FLUSH, "edit-clear.png", new FWButtonListener() {
			public void actionPerformed(ActionEvent e, JButton source) {
				docFactory.flush();
			}
		});
		
		JButton makeProcedureButton = makeProcedureAction.getButton();

		JButton showMonitorButton = FWButton.createIconButton(TOGGLE_MONITOR, "utilities-system-monitor.png", new FWButtonListener() {
			
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				showMonitor = ! showMonitor;
				monitor.setEnabled(showMonitor);
				revalidate();
			}
		});

		JButton showBoardButton = FWButton.createIconButton(TOGGLE_BOARD, "board.png", new FWButtonListener() {

			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				showBoard = ! showBoard;
				revalidate();
			}
		});
		
		this.graphicSpace = gs;
		FWEnhancedTextPane commandTP = docFactory.getCommandPane();
		this.commandPane = new FWTitledButtonedPane(COMMAND_PANE, 
				new GTTextPaneWithButton(commandTP, flushButton), makeProcedureButton);
		this.procedurePane = new FWTitledPane(PROC_PANE, 
				new GTTextPaneWithButton(docFactory.getProcedurePane(), showMonitorButton, showBoardButton));
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.painter = p;
		this.focusedComponent = commandTP;
		this.sandBox = sb;
		this.monitorPane = new FWTitledPane(MONITOR_PANE, monitor);
		this.boardPane = new FWTitledPane(BOARD_PANE, board);
		this.modelManager = modelManager;

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		layouts.put(LAYOUT_TYPE.STANDARD, new StandardLayout());
		layouts.put(LAYOUT_TYPE.PAINTER, new BorderLayout());
		layouts.put(LAYOUT_TYPE.SANDBOX, new SandBoxLayout());
		layouts.put(LAYOUT_TYPE.MODEL, new BorderLayout());
	}
	
	@Override
	public void showBoard() {
		if (showBoard)
			return;
		showBoard = true;
		revalidate();
	}
	
	@Override
	public void showMonitor() {
		if (showMonitor)
			return;
		showMonitor = true;
		revalidate();
	}
	
	public void setLayout(final LAYOUT_TYPE layout) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_setLayout(layout);
			}
		});
	}

	private void _setLayout(LAYOUT_TYPE layout){
		setCursor(Cursor.getDefaultCursor());
		removeAll();
		setLayout(layouts.get(layout));
		switch (layout) {
		case STANDARD:
			add(graphicSpace.getPane());
			add(southGraphicDivider);
			add(commandPane);
			add(rightGraphicDivider);
			add(procedurePane);
			add(boardDivider);
			add(boardPane);
			add(monitorDivider);
			add(monitorPane);
			break;
		case PAINTER:
			add(painter, BorderLayout.CENTER);
			break;
		case SANDBOX:
			Component[] comps = sandBox.getExtraComponents();
			add(comps[0]); // compass
			add(comps[1]); // left
			add(leftGraphicDivider);
			add(graphicSpace.getPane());
			add(southGraphicDivider);
			add(commandPane);
			add(rightGraphicDivider);
			add(comps[2]); // right
			break;
		case MODEL:
			add(modelManager.getModelPane(), BorderLayout.CENTER);
			break;
		default:
			break;
		}

		validate();
		repaint();
		focusedComponent.requestFocusInWindow();
	}
	
	private class StandardLayout extends BasicLayoutAdapter {

		protected int graphicsW, graphicsH, procW;
		protected int gap = 3;
		private int boardH = -1;
		private int monitorH = -1;
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			graphicSpace.setMaximumSize(parentW-1-gap, parentH-25-gap);

			Dimension graphicsSize = graphicSpace.getSize();
			graphicsW = graphicsSize.width;
			procW = parentW - graphicsW - gap;
			
			graphicsH = graphicsSize.height;
		}
		
		private int getBoardHeight() {
			return boardH;
		}
		
		private void setBoardHeight(int h) {
			if (h<36)
				boardH = 26;
			else if (h < parentH-monitorH-40)
				boardH = h;
			else {
				if (showMonitor)
					boardH = parentH-monitorH-26-2*gap;
				else
					boardH = parentH-monitorH-26-2;
			}
		}
		
		private int getMonitorHeight() {
			return monitorH;
		}
		
		private void setMonitorHeight(int h) {
			if (h<36)
				monitorH = 26;
			else if (h < parentH - boardH -40)
				monitorH = h;
			else {
				if (showBoard)
					monitorH = parentH-boardH-26-2*gap;
				else 
					monitorH = parentH-boardH-26-gap;
			}
		}
		
		public void layoutContainer(Container parent) {
			init(parent);
			Component[] components = parent.getComponents();
			
			for (int idx = 5; idx <=8; idx++)  // hide extra comps
				components[idx].setBounds(-2, -2, 1, 1);
			
			Component c = components[0]; // graphics
			c.setBounds(currX, currY, graphicsW, graphicsH);
			currY += graphicsH;
			
			c = components[1]; // southGraphicsDivider
			c.setBounds(currX, currY, graphicsW, gap);
			currY += gap;
			
			c = components[2]; // commands
			c.setBounds(currX, currY, graphicsW, parentH - graphicsH - gap);
			currX += graphicsW;
			currY = insets.top;
			
			
			c = components[3]; // rightGraphicsDivider
			c.setBounds(currX, currY, gap, parentH);
			currX += gap;
			
			
			if (!showMonitor && ! showBoard) {
				c = components[4]; // procedurePane
				c.setBounds(currX, currY, procW, parentH);
				return;
			}
			

			if (showMonitor && !showBoard) {
				if (monitorH<0)
					monitorH = parentH/4;
				int h = monitorH;
				boardH = -1;

				c = components[4]; // procedurePane
				int procH = parentH -  h - gap;
				c.setBounds(currX, currY, procW, procH); 
				currY += procH;
				
				c = components[7]; // monitorDivider 
				c.setBounds(currX, currY, procW, gap);
				currY += gap;
				
				c = components[8]; // monitor
				c.setBounds(currX, currY, procW, h); 
				return;
			}
			
			if (showBoard && !showMonitor) {
				if (boardH<0)
					boardH = parentH/4;
				int h = boardH;
				monitorH = -1;
			

				c = components[4]; // procedurePane
				int procH = parentH -  h - gap;
				c.setBounds(currX, currY, procW, procH); 
				currY += procH;
				
				c = components[5]; // boardDivider
				c.setBounds(currX, currY, procW, gap);
				currY += gap;
				
				c = components[6]; // board
				c.setBounds(currX, currY, procW, h); 
				return;
			}
			
			if (monitorH<0) {
				monitorH = parentH/4;
				setBoardHeight(boardH);
			} if (boardH<0) {
				boardH = parentH/4;
				setMonitorHeight(monitorH);
			}
			
			c = components[4]; // procedurePane
			int procH = parentH - boardH - monitorH - 2*gap;
			c.setBounds(currX, currY, procW, procH);
			currY += procH;
			
			c = components[5]; // boardDivider
			c.setBounds(currX, currY, procW, gap);
			currY += gap;
			
			c = components[6] ; // board
			c.setBounds(currX, currY, procW, boardH);
			currY += boardH;
			
			c = components[7]; // monitorDivider
			c.setBounds(currX, currY, procW, gap);
			currY += gap;
			
			c = components[8]; // monitor
			c.setBounds(currX, currY, procW, monitorH);
		}
		
		@Override
		public void layoutComponent(Component c, int idx) {}
	}
	
	private class SandBoxLayout extends StandardLayout {

		private final int compassPaneH = 220;
		private int leftPaneW = -1;
		private int lPminW = 155;
		private int rightPaneW = -1;
		private int rPminW = 190;
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			if (leftPaneW<0) {
					int w = (parentW - 2*gap - graphicsW)/2;
					leftPaneW = Math.max(w, lPminW);
					rightPaneW = w;
			} 
			rightPaneW = parentW - graphicsW - 2*gap -  leftPaneW;
			
			if (rightPaneW<rPminW) {
				rightPaneW = rPminW;
				
				leftPaneW = Math.min(leftPaneW, parentW - 200 - 2*gap -  rightPaneW);
				leftPaneW = Math.max(leftPaneW, lPminW);
				graphicsW = parentW - leftPaneW - 2*gap -  rightPaneW;
				graphicSpace.setWidth(graphicsW);
			}
			
			leftPaneW = Math.max(leftPaneW, lPminW);
		}
		
		public void layoutContainer(Container parent) {
			init(parent);
			Component[] components = parent.getComponents();
			
			Component c = components[0]; // compass
			c.setBounds(currX, currY, leftPaneW, compassPaneH);
			
			c = components[1]; // leftButtonPane
			currY += compassPaneH + gap;
			c.setBounds(currX, currY, leftPaneW, parentH - gap- compassPaneH);
			
			c = components[2]; // leftGraphicsDivider
			currX += leftPaneW;
			currY = insets.top;
			c.setBounds(currX, currY, gap, parentH);
			
			c = components[3]; // graphics
			currX += gap;
			c.setBounds(currX, currY, graphicsW, graphicsH);
			
			c = components[4]; // southGraphicsDivider
			currY += graphicsH;
			c.setBounds(currX, currY, graphicsW, gap);
			
			c = components[5]; // commands
			currY += gap;
			c.setBounds(currX, currY, graphicsW, parentH - graphicsH - gap);
			
			c = components[6]; // rightGraphicsDivider
			currX += graphicsW;
			currY = insets.top;
			c.setBounds(currX, currY, gap, parentH);

			c = components[7]; // rightButtonPane
			currX += gap;
			c.setBounds(currX, currY, rightPaneW, parentH-gap);			
		}
		
		public int getLeftPaneWidth() {
			return leftPaneW;
		}

		public void setLeftPaneWidth(int lW) {
			if (lW>lPminW)
				leftPaneW = lW;
		}
	}
	
	
	/*
	 * Dividers
	 */

	private Divider southGraphicDivider = new Divider(DIRECTION.S){
		private static final long serialVersionUID = -7937129499711075506L;

		@Override
		public int captureRef() {
			return graphicSpace.getSize().height;
		}

		@Override
		public void resize(int x) {
			graphicSpace.setHeight(x);
		}
	};

		
	private Divider boardDivider = new Divider(DIRECTION.N) {
		private static final long serialVersionUID = 659455817174824986L;

		@Override
		public int captureRef() {
			return ((StandardLayout) GTPanel.this.getLayout()).getBoardHeight();
		}

		@Override
		public void resize(int x) {
			((StandardLayout) GTPanel.this.getLayout()).setBoardHeight(x);
			update(GTPanel.this);
		}

	};
	
	private Divider monitorDivider = new Divider(DIRECTION.N) {
		private static final long serialVersionUID = -8410173485776233626L;

		@Override
		public int captureRef() {
			return ((StandardLayout) GTPanel.this.getLayout()).getMonitorHeight();
		}

		@Override
		public void resize(int x) {
			((StandardLayout) GTPanel.this.getLayout()).setMonitorHeight(x);
			update(GTPanel.this);
		}
	};
	
	private Divider leftGraphicDivider = new Divider(DIRECTION.E) {
		private static final long serialVersionUID = 9116951569369345445L;

		@Override
		public int captureRef() {
			return ((SandBoxLayout) GTPanel.this.getLayout()).getLeftPaneWidth();
		}

		@Override
		public void resize(int x) {
			((SandBoxLayout) GTPanel.this.getLayout()).setLeftPaneWidth(x);
			update(GTPanel.this);
		}
	};
	
	private Divider rightGraphicDivider = new Divider(DIRECTION.E){
		private static final long serialVersionUID = 7991231423116386636L;

		@Override
		public int captureRef() {
			return graphicSpace.getSize().width;
		}

		@Override
		public void resize(int x) {
			graphicSpace.setWidth(x);
		}
	};
}