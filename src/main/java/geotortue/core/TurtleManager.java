package geotortue.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWColorBox;
import fw.gui.FWColorBox.FWColorChooserListener;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.gui.FWDialog;
import fw.gui.FWImagePane;
import fw.gui.FWLabel;
import fw.gui.FWModularList;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.TurtleAvatar2D.AVATAR_TYPE;
import geotortue.geometry.GTGeometryI;
import geotortue.gui.GTDialog;
import geotortue.gui.GTImagePane;
import geotortue.renderer.GTRendererI;

public class TurtleManager implements XMLCapabilities, FWSettings {


	private static final TKey NAME = new TKey(TurtleManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey GET_NAME = new TKey(TurtleManager.class, "getName");
	private static final BKey CREATE_TURTLE = new BKey(TurtleManager.class, "createTurtle");
	private static final TKey CREATE_TURTLE_DESC = new TKey(TurtleManager.class, "createTurtle.getTitle");
	private static final ActionKey ADD_TURTLE = new ActionKey(TurtleManager.class, "addTurtle");
	private static final ActionKey REMOVE_TURTLE = new ActionKey(TurtleManager.class, "removeTurtle");
	private static final TKey TURTLE_NAME = new TKey(TurtleManager.class, "turtleName");
	private static final TKey TURTLE_COLOR = new TKey(TurtleManager.class, "turtleColor");
	private static final TKey TURTLE_AVATAR = new TKey(TurtleManager.class, "turtleAvatar");
	
	private final List<Turtle> turtles = Collections.synchronizedList(new ArrayList<Turtle>());
	private List<Turtle> focusedTurtles = Collections.synchronizedList(new ArrayList<Turtle>()); 

	private final KeywordManager keywordManager;
	
	private boolean axisVisible = false;
	
	private GTEnhancedJEP jep = null;
	
	public TurtleManager(KeywordManager km) {
		this.keywordManager = km; 
		keywordManager.updateCompletionKeys();
		try {
			TurtleAvatar2D.init(this);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		//turtles.
	}
	
	private void add(Turtle t){
		synchronized (turtles) {
			turtles.add(t);	
		}
		keywordManager.addTurtle(t.getName());
		keywordManager.updateCompletionKeys();
	}
	
	public void setListener(GTEnhancedJEP jep) {
		this.jep = jep;
		updateJEP();
	}
	

	/**
	 * 
	 */
	private void updateJEP() {
		jep.updateTurtles(turtles);
	}
	
	public void addAll(List<Turtle> candidates) {
		ArrayList<Turtle> newTurtles = new ArrayList<>();
		newTurtles.addAll(candidates);
		synchronized (turtles) {
			for (Turtle c : candidates)
				for (Turtle t : turtles)  
					if (t.getName().equals(c.getName())) 
						newTurtles.remove(c);
		}
		synchronized (turtles) {
			for (Turtle t : newTurtles) {
				turtles.add(t);	
				keywordManager.addTurtle(t.getName());
			}
		}
		keywordManager.updateCompletionKeys();
		updateJEP();
	}

	public List<Turtle> getFocusedTurtles() {
		ArrayList<Turtle> copy = new ArrayList<>();
		synchronized (focusedTurtles) {
			for (Turtle turtle : focusedTurtles)
				copy.add(turtle);
		}
		return copy;
	}

	public void setFocusedTurtle(Turtle... ts) {
		synchronized (focusedTurtles) {
			focusedTurtles.clear();
			for (Turtle t : ts)
				focusedTurtles.add(t);
		}
	}
	
	public void setFocusOnAllTurtles() {
		synchronized (focusedTurtles) {
			focusedTurtles.clear();
			focusedTurtles.addAll(turtles);
		}
	}
	
	public List<Turtle> getTurtles() {
			return turtles;	
	}
	
	public void resetTurtles() {
		synchronized (turtles) {
			if (turtles.isEmpty())
				return;
			for (Turtle t : turtles)
				t.reset();
			setFocusedTurtle(turtles.get(0));
		}
	}
	
	public Turtle getTurtle(int idx) throws NoSuchTurtleException  {
		synchronized (turtles) {
			if (idx >= turtles.size() || idx < 0)
				throw new NoSuchTurtleException();
			return turtles.get(idx);
		}
	}
	
	public class NoSuchTurtleException extends Exception {
		private static final long serialVersionUID = 6889488760044256840L;
	}
	

	public void toggleAxisVisibility() {
		axisVisible = ! axisVisible;
	}
	
	public synchronized void display(GTRendererI renderer, GTGeometryI g) {
		synchronized (turtles) {
			for (Turtle t : turtles) 
				t.drawPath(renderer, g);
			
			for (Turtle t : turtles) {
				if (t.isVisible()) {
					if (axisVisible)
						renderer.drawAxis(g, t);
					
					renderer.draw(t, g);
				}
			}
		}
	}
	// TODO : croix et axes sur image de fond
	/*
	 * XML
	 */
	
	public String getXMLTag() {
		return "TurtleManager";
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (Turtle t : turtles)
			e.put(t);
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		synchronized (turtles) {
			turtles.clear();
		}
		keywordManager.clearTurtles();
		while (child.hasChild(Turtle.XML_TAG))
			add(new Turtle(child));
		updateJEP();

		setFocusedTurtle(turtles.get(0));
		return child;
	}
	
	private Turtle addTurtle(final String name, final Window owner) throws GTException {
		keywordManager.testValidity(SourceLocalization.create(name, owner)); 
		final Turtle t = new Turtle(name);
		try {
			final Random random = SecureRandom.getInstanceStrong();
			final int r = random.nextInt(255);
			final int g = random.nextInt(255);
			final int b = random.nextInt(255);
			t.setColor(new Color(r, g, b));
		} catch(NoSuchAlgorithmException e) {
			throw new GTException(GTTrouble.GTJEP_NEW_TURTLE, owner, t.getName());
		}
		add(t);
		updateJEP();
		keywordManager.addTurtle(name);
		return t;
	}
	
	private void removeTurtle(Turtle t, Window owner) throws GTException {
		synchronized (turtles) {
			if (turtles.size() == 1)
				throw new GTException(GTTrouble.GTJEP_LAST_TURTLE, owner, t.getName());
			turtles.remove(t);
			updateJEP();
		}
		keywordManager.removeTurtle(t.getName());
		keywordManager.updateCompletionKeys();
		setFocusedTurtle(turtles.get(0));
	}
	
	/*
	 * FWS
	 */
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		final FWModularList<Turtle> list = new FWModularList<Turtle>(ADD_TURTLE, REMOVE_TURTLE, TURTLE_NAME, TURTLE_COLOR, TURTLE_AVATAR){
			private static final long serialVersionUID = 1052011818001884704L;

			@Override
			protected Turtle getDefaultItem() {
				final JPanel mainPane = new JPanel(new BorderLayout());
				mainPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 24));
				
				FWImagePane imPane = new GTImagePane();
				
				final JTextField textField = new JTextField(30);
				JPanel textPane = VerticalFlowLayout.createPanel(12, new FWLabel(GET_NAME), textField);
				textPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 12));
				textPane.setBackground(Color.WHITE);

				mainPane.add(imPane, BorderLayout.WEST);
				mainPane.add(textPane, BorderLayout.CENTER);
				final Window owner =  (Window) getTopLevelAncestor();
				FWDialog dial = new FWDialog(owner, ADD_TURTLE, mainPane, true, true){
					private static final long serialVersionUID = -2088403923723999133L;

					@Override
					protected void validationPerformed() {
						String name = textField.getText();
						if (name.length()>0)
							try {
								Turtle t = addTurtle(name, owner);
								addItem(t);
								dispose();
								actions.fire(FWSettingsActionPuller.REPAINT);
							} catch (GTException e) {
								e.displayDialog();
							}
					}
				};
				textField.requestFocusInWindow();
				dial.setVisible(true);
				return null;
			}
			

			@Override
			protected boolean removeItem(Turtle t, Window owner) {
				try {
					TurtleManager.this.removeTurtle(t, owner);
					keywordManager.removeTurtle(t.getName());
				} catch (GTException e) {
					e.displayTransientWindow();
					return false;
				}
				actions.fire(FWSettingsActionPuller.REPAINT);
				return true;
			}

			@Override
			protected JComponent[] getItemComponents(final Turtle t) {
				FWColorBox cc = new FWColorBox(t.getColor(), new FWColorChooserListener() {
					public void colorSelected(Color c) {
						t.setColor(c);
						actions.fire(FWSettingsActionPuller.REPAINT); 
					}
				});
				
				FWComboBox avatarCB = new FWComboBox(AVATAR_TYPE.values(), t.getAvatarType(),  new FWComboBoxListener() {
					@Override
					public void itemSelected(Object o) {
						t.setAvatarType((AVATAR_TYPE) o); 
						actions.fire(FWSettingsActionPuller.REPAINT); 
					}
				});
				
				avatarCB.setRenderer(TurtleAvatar2D.getCellRenderer());
				return new JComponent[]{new JLabel(t.getName(), SwingConstants.CENTER), cc, avatarCB};
			}
		};

		synchronized (turtles) {
			list.addItem(turtles.toArray(new Turtle[turtles.size()]));
		}
		
		FWButton createTurtleButton = new FWButton(CREATE_TURTLE, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				final JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
				JPanel pane = VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(CREATE_TURTLE_DESC, SwingConstants.RIGHT), spinner);
		
				final Window owner =  (Window) source.getTopLevelAncestor();
				GTDialog dial = new GTDialog(owner, ADD_TURTLE, pane, true) {
					private static final long serialVersionUID = -3493603185598202557L;

					@Override
					protected void validationPerformed() {
						int n = (Integer) spinner.getValue();
						for (int idx = 1; idx <= n; idx++) {
							try {
								Turtle t = addTurtle("T" + idx, getOwner());
								list.addItem(t);
							} catch (GTException e) {
								n++;
							}
						}
						dispose();
						actions.fire(FWSettingsActionPuller.REPAINT); 
					}	
				};
				dial.setVisible(true);
			}
		});
		return VerticalFlowLayout.createPanel(5, list, createTurtleButton);
	}
}