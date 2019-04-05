package geotortue.core;

import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import color.GTColor;
import color.GTColors;
import fw.HelpI;
import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.MouseManager;
import fw.text.TextStyle;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.GTProcessingContext.NoMidiException;
import geotortue.core.Turtle.NoScoreException;
import geotortue.core.Turtle.RecordingAlreadyOpenedException;
import geotortue.geometry.GTEuclidean2DGeometry;
import geotortue.geometry.GTEuclidean4DGeometry;
import geotortue.geometry.GTFlatQuotientGeometry;
import geotortue.geometry.GTGeometry;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTGeometryI.GeometryException;
import geotortue.geometry.GTPoincareDiscGeometry;
import geotortue.geometry.GTPoincareHPGeometry;
import geotortue.geometry.GTPoint;
import geotortue.geometry.GTRotation;
import geotortue.geometry.GTSphericalGeometry;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.proj.GTConicPerspective;
import geotortue.geometry.proj.GTPerspective;
import geotortue.gui.GTDialog;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JEP2;
import sound.GTMidi.MidiChannelException;
import sound.MusicEvent;
import type.JIterable;
import type.JList;
import type.JNullObject;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;

public class GTCommandFactory {
	
	private static final TKey DIS = new TKey(GTCommandFactory.class, "info");

	public static enum GTCommandKey {
		AV, RE, TD, TG, VG, CT, MT, UNDO, 
		LC, BC, THICKNESS, 
		CRAYON, REMPLIS,  
		PVH, PVB, PVG, PVD,  
		VISE, TLP, BOUSSOLE, IMITE, MIROIR,
		PVXY, PVXZ, PVYZ,
		REP, TANT_QUE, SI, 
		ASK_FOR,
		FUN, EFF, INIT, AFF, 
		SELECT, ECRIS, DIS, MG, 
		FOR_EACH, 
		PHOTO, PAUSE, 
		RETOURNE, STOP, 
		WAIT, 
		CERCLE, ARC, POINT,
		RZ, GLOBAL, EXECUTE, PLAY, SCORE, CONCERT
	};
	
	private final TreeMap<String, GTPrimitiveCommand> commandTable = new TreeMap<>();
	
	private final KeywordManager keywordManager;


	public GTCommandFactory(KeywordManager km) {
		this.keywordManager = km;
		addCommand(COMMAND_AV);
		addCommand(COMMAND_RE);
		addCommand(COMMAND_TD);
		addCommand(COMMAND_TG);
		addCommand(COMMAND_VG);
		
		addCommand(COMMAND_CT);
		addCommand(COMMAND_MT);
		
		addCommand(COMMAND_CRAYON);
//		addCommand(COMMAND_PALETTE);
		addCommand(COMMAND_REMPLIS);
		
		addCommand(COMMAND_LC);
		addCommand(COMMAND_BC);
		addCommand(COMMAND_THICKNESS);
		
		addCommand(COMMAND_VISE);
		addCommand(COMMAND_TLP);
		addCommand(COMMAND_BOUSSOLE);
		addCommand(COMMAND_IMITE);
		addCommand(COMMAND_MIROIR);
		
		addCommand(COMMAND_REP);
		addCommand(COMMAND_TANT_QUE);
		addCommand(COMMAND_SI);
		addCommand(COMMAND_FOR_EACH);
		
		// TODO : web deprecate def store boucle  
		addCommand(COMMAND_FUN);
		addCommand(COMMAND_EFF);
		addCommand(COMMAND_INIT);
		addCommand(COMMAND_AFF);
		
		addCommand(COMMAND_SELECT);
		addCommand(COMMAND_ECRIS);
		addCommand(COMMAND_DIS);
		addCommand(COMMAND_MG);
	
		addCommand(COMMAND_PHOTO);
		addCommand(COMMAND_PAUSE);
	
		addCommand(COMMAND_RETURN); 
		addCommand(COMMAND_STOP);
		
		addCommand(COMMAND_WAIT);
		addCommand(COMMAND_UNDO);

		addCommand(COMMAND_POINT);
		
		addCommand(cloneCommand(COMMAND_VG, GTCommandKey.RZ));
		
		addCommand(COMMAND_ASK_FOR);
		
		addCommand(COMMAND_EXECUTE);
	}
	
	public GTPrimitiveCommand getCommand(String key) {
		return commandTable.get(key);
	}

	private void addCommand(GTPrimitiveCommand c){
		GTCommandKey key = c.key;
		String name = GTCommandDescTable.getName(key);
		commandTable.put(name, c);
		keywordManager.addCommand(name, GTCommandDescTable.isCommand(key));
	}
	
	private void removeCommand(GTPrimitiveCommand c) {
		GTCommandKey key = c.key;
		String name = GTCommandDescTable.getName(key);
		commandTable.remove(name);
		keywordManager.removeCommand(name, GTCommandDescTable.isCommand(key));
	}
	
	public void setCircleEnabled(boolean b) {
		if (b) {
			addCommand(COMMAND_CERCLE);
			addCommand(COMMAND_ARC);
		} else {
			removeCommand(COMMAND_CERCLE);
			removeCommand(COMMAND_ARC);
		}
		keywordManager.updateCompletionKeys();
	}
	
	public void setMusicEnabled(boolean b) {
		if (b) {
			addCommand(COMMAND_PLAY);
			addCommand(COMMAND_SCORE);
			addCommand(COMMAND_CONCERT);
		} else {
			removeCommand(COMMAND_PLAY);
			removeCommand(COMMAND_SCORE);
			removeCommand(COMMAND_CONCERT);
		}
		keywordManager.updateCompletionKeys();
	}
	
	public void setDimensionCommands(int d) {
		if (d==2) {
			removeCommand(COMMAND_PVXY);
			removeCommand(COMMAND_PVYZ);
			removeCommand(COMMAND_PVXZ);
			removeCommand(COMMAND_PVG);
			removeCommand(COMMAND_PVD);
			removeCommand(COMMAND_PVB);
			removeCommand(COMMAND_PVH);
		} else if (d==3) {
			removeCommand(COMMAND_PVXY);
			removeCommand(COMMAND_PVYZ);
			removeCommand(COMMAND_PVXZ);
			addCommand(COMMAND_PVG);
			addCommand(COMMAND_PVD);
			addCommand(COMMAND_PVB);
			addCommand(COMMAND_PVH);
		} else if (d==4) {
			addCommand(COMMAND_PVXY);
			addCommand(COMMAND_PVYZ);
			addCommand(COMMAND_PVXZ);
			addCommand(COMMAND_PVG);
			addCommand(COMMAND_PVD);
			addCommand(COMMAND_PVB);
			addCommand(COMMAND_PVH);
		}
		keywordManager.updateCompletionKeys();
	}

	public void setGlobalEnabled(boolean b) {
		if (b) 
			addCommand(COMMAND_GLOBAL);
		else
			removeCommand(COMMAND_GLOBAL);
		
		keywordManager.updateCompletionKeys();
	}
	
	private static GTPrimitiveCommand cloneCommand(final GTPrimitiveCommand c, GTCommandKey key){
		GTPrimitiveCommand clone = new GTPrimitiveCommand(c, key) {
			
			@Override
			protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
				return c.execute_(bundle, context);
			}
		};
		return clone;
	}

	private static String getName(GTCommandKey key) {
		return GTCommandDescTable.getName(key);
	}

	final static private GTPrimitiveCommand COMMAND_AV = new GTPrimitiveCommand(GTCommandKey.AV, 1){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			double x = getDoubleAt(context, bundle, 1);
			for (Turtle turtle : context.getFocusedTurtles())
				try {
					UndoableAction action = turtle.walk(context.getGeometry(), x);
					context.addUndoableAction(action);
				} catch (GeometryException ex) {
					throw new GTException(this, ex, bundle);
				} catch (NonFlatPolygonException e) {
					throw new GTException(this, GTTrouble.GTJEP_NON_FLAT_FILLING, bundle);
				} 
			context.repaintIfNeeded();
			context.sleep();
			return null;
			}
		};
		
	final static private GTPrimitiveCommand COMMAND_RE = new GTPrimitiveCommand(GTCommandKey.RE, 1){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			double x = getDoubleAt(context, bundle, 1);
			for (Turtle turtle : context.getFocusedTurtles())
				try {
					UndoableAction action = turtle.walk(context.getGeometry(), -x);
					context.addUndoableAction(action);
				} catch (GeometryException ex) {
					throw new GTException(this, ex, bundle);
				} catch (NonFlatPolygonException e) {
					throw new GTException(this, GTTrouble.GTJEP_NON_FLAT_FILLING, bundle);
				} 
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_TD = new GTPrimitiveCommand(GTCommandKey.TD, 1){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateZ(-x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_TG = new GTPrimitiveCommand(GTCommandKey.TG, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateZ(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_VG = new GTPrimitiveCommand(GTCommandKey.VG, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			context.vg();
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_CT = new GTPrimitiveCommand(GTCommandKey.CT, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.setVisible(false);
			context.repaintIfNeeded();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_MT = new GTPrimitiveCommand(GTCommandKey.MT, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.setVisible(true);
			context.repaintIfNeeded();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_LC = new GTPrimitiveCommand(GTCommandKey.LC, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.getPencil().setDown(false);
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_BC = new GTPrimitiveCommand(GTCommandKey.BC, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.getPencil().setDown(true);
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_THICKNESS = new GTPrimitiveCommand(GTCommandKey.THICKNESS, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int x = (int) getDoubleAt(context, bundle, 1);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.getPencil().setThickness(x);
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_TLP = new GTPrimitiveCommand(GTCommandKey.TLP, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int len = bundle.getArgumentsCount() ;
			GTGeometry geo = context.getGeometry(); 
			int dim = geo.getDimensionCount();
			
			if (len!=dim)
				throw new GTException(this, GTTrouble.GTJEP_TELEPORTATION, bundle, geo.toString(), dim+"");
			double x = getDoubleAt(context, bundle, 1);
			double y = getDoubleAt(context, bundle, 2);
			double z = (len >= 3)? getDoubleAt(context, bundle, 3) : 0;
			double t = (len >= 4)? getDoubleAt(context, bundle, 4) : 0;
			GTPoint p;
			if (geo instanceof GTSphericalGeometry) {
				x = context.convertToRadians(x);
				y = context.convertToRadians(y);
				p = new GTPoint(x, y, z, t);
			}
			else 
				p = new GTPoint(x, y, z, t);
			for (Turtle turtle : context.getFocusedTurtles())
				try {
					geo.teleport(turtle, p);
				} catch (GeometryException ex) {
					throw new GTException(this, ex, bundle);
				} catch (NonFlatPolygonException ex) {
					throw new GTException(this, GTTrouble.GTJEP_NON_FLAT_FILLING, bundle);
				}
			
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_VISE = new GTPrimitiveCommand(GTCommandKey.VISE, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			Turtle t2 = context.getTurtleAt(bundle, 1);
			GTPoint q = t2.getPosition();
			for (Turtle turtle : context.getFocusedTurtles()) {
				GTPoint p = turtle.getPosition();
				if (!p.quasiEquals(q)) {
					GTRotation r = context.getGeometry().getOrientation(turtle, t2);
					turtle.setRotation(r);
				}
			}
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_BOUSSOLE = new GTPrimitiveCommand(GTCommandKey.BOUSSOLE, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
					context.getGeometry().resetToNorthOrientation(turtle);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_IMITE = new GTPrimitiveCommand(GTCommandKey.IMITE, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			Turtle ref = context.getTurtleAt(bundle, 1);
			for (Turtle turtle : context.getFocusedTurtles())
				try {
					context.getGeometry().setParallelOrientation(ref, turtle);
				} catch (GeometryException ex) {
					throw new GTException(this, ex, bundle);
				}
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
 
	final static private GTPrimitiveCommand COMMAND_CRAYON = new GTPrimitiveCommand(GTCommandKey.CRAYON, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			
			SourceLocalization loc = bundle.getLocalizationAt(1);
			String str = loc.getText();
			
			Color color = null;
			JObjectI<?> o = context.getJObject(loc);
			
			if (o.getType() == JEP2Type.COLOR) 
				color = ((GTColor) o).getValue();
			else if (o.getType() == JEP2Type.STRING)
				color = GTColors.getHexColor(JEP2.getString(o).getValue());
			else if (o.getType() == JEP2Type.LONG)
					color = context.getTurtleAt(bundle, 1).getColor(); // turtle color
			else if (str.startsWith("0x"))
				color = Color.decode(str);
			
			if (color == null) 
				throw new GTException(this, GTTrouble.GTJEP_PENCIL, loc, str);
			
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.getPencil().setColor(color);
				
			return null;
		}
	};


//	final static private GTPrimitiveCommand COMMAND_PALETTE = new GTPrimitiveCommand(GTCommandKey.PALETTE, -1) {
//		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
//			int len = bundle.getArgumentsCount() ;
//			if (len!=4)
//				throw new GTException(this, GTTrouble.GTJEP_PALETTE, bundle);
//			Color color = null;
//			
//			if (len >= 1) {
//				SourceLocalization loc = bundle.getLocalizationAt(1);
//				String type = loc.getText();
//				double x = getDoubleAt(context, bundle, 2);
//				double y = getDoubleAt(context, bundle, 3);
//				double z = getDoubleAt(context, bundle, 4);
//				try {
//					if (type.equals("rvb"))
//						color = GTColors.getRGBColor(x, y, z, false);
//					else if (type.equals("RVB"))
//						color = GTColors.getRGBColor(x, y, z, true);
//					else if (type.equals("tsv"))
//						color = GTColors.getHSBColor(x, y, z, false);
//					else if (type.equals("TSV"))
//						color = GTColors.getHSBColor(x, y, z, true);
//					else
//						throw new GTException(this, GTTrouble.GTJEP_PALETTE, loc);
//				} catch (GTColorsException ex) {
//					throw new GTException(this, ex, bundle);
//				}
//			}
//			
//			if (color != null) {
//				for (Turtle turtle : context.getFocusedTurtles())
//					turtle.getPencil().setColor(color);
//			} else
//				throw new GTException(this, GTTrouble.GTJEP_PALETTE, bundle);
//			
//			return null;
//		}
//	};

	final static private GTPrimitiveCommand COMMAND_MIROIR = new GTPrimitiveCommand(GTCommandKey.MIROIR, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.invertOrientation();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_REP = new GTPrimitiveCommand(GTCommandKey.REP) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (bundle.getArgumentsCount()<2)
				throw new GTException(this, GTTrouble.GTJEP_REPEAT, bundle);
			
			int n = (int) getDoubleAt(context, bundle, 1);
			GTCommandBundles commands = bundle.parseBundleSince(2);
			
			for (int idx = 0; idx < n; idx++) {
				JObjectI<?> o = context.process(commands);
				if (o!=null)  
					return o;
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_TANT_QUE = new GTPrimitiveCommand(GTCommandKey.TANT_QUE) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (bundle.getArgumentsCount()!=2)
				throw new GTException(this, GTTrouble.GTJEP_WHILE, bundle);
			
			boolean condition = getBooleanAt(context, bundle, 1);
			
			GTCommandBundles commands = bundle.parseBundleAt(2);
			while (condition) {
				JObjectI<?> o = context.process(commands);
				if (o!=null)  
					return o;
				condition = getBooleanAt(context, bundle, 1);
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_SI = new GTPrimitiveCommand(GTCommandKey.SI, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int len = bundle.getArgumentsCount();
			if (len!=3 && len!=5) 
				throw new GTException(this, GTTrouble.GTJEP_IF_THEN_ELSE, bundle);
			
			SourceLocalization loc = bundle.getLocalizationAt(2);
			if (! loc.getText().equals(KeywordManager.THEN_KEY.translate()))
				throw new GTException(this, GTTrouble.GTJEP_IF_THEN_ELSE, loc);

			boolean condition = getBooleanAt(context, bundle, 1);
			
			if (condition) {
				GTCommandBundles commands = bundle.parseBundleAt(3);
				return context.process(commands);
			} else if (len == 5) {
				loc = bundle.getLocalizationAt(4);
				if (!loc.getText().equals(KeywordManager.ELSE_KEY.translate()))
					throw new GTException(this, GTTrouble.GTJEP_IF_THEN_ELSE, loc);
				GTCommandBundles commands = bundle.parseBundleAt(5);
				return context.process(commands);
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_REMPLIS = new GTPrimitiveCommand(GTCommandKey.REMPLIS) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (bundle.getArgumentsCount()<1)
				throw new GTException(this, GTTrouble.GTJEP_FILLING, bundle);
			
			GTGeometryI geo = context.getGeometry();

			for (Turtle t: context.getFocusedTurtles()) {
				GTCommandBundles commands = bundle.parseBundleSince(1);
				if (commands.isEmpty())
					return null;
				
				if (commands.firstElement().getKey().equals(KeywordManager.QUESTION_KEY.translate()))
					throw new GTException(this, GTTrouble.GTJEP_FILLING, bundle);
				
				try {
					t.openRecording(geo);
				} catch (RecordingAlreadyOpenedException ex) {
					throw new GTException(this, GTTrouble.GTJEP_FILLING_REDONDANT, bundle);
				}
				
				try {
					context.process(commands);
					t.addPolygon();
				} catch (GTException ex) {
					throw ex;
				} finally {
					t.giveUpRecording();
				}
			}
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_SELECT = new GTPrimitiveCommand(GTCommandKey.SELECT, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int len = bundle.getArgumentsCount();
			if (len<1)
				throw new GTException(this, GTTrouble.GTJEP_TURTLE_SELECTION, bundle);
			
			// TODO : (done) select turtle in a list
			SourceLocalization loc = bundle.getLocalizationAt(1);
			if (loc.getText().equals(KeywordManager.ALL_KEY.translate())) {
				context.setFocusOnAllTurtles();
				return null;
			}
			
			Turtle[] turtles;
			
			JObjectI<?> o = context.getJObject(loc);
			if (o.getType() == JEP2Type.LIST) {
				turtles = context.getTurtles(loc, (JList) o);
			} else {
				turtles = new Turtle[len];
				for (int idx = 0; idx < len; idx++) 
					turtles[idx] = context.getTurtleAt(bundle, idx+1);
			}
			context.setFocusedTurtle(turtles);
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_FUN = new GTPrimitiveCommand(GTCommandKey.FUN, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (bundle.getArgumentsCount()<1)
				throw new GTException(this, GTTrouble.GTJEP_FUNCTION_DECLARATION, bundle);
			
			SourceLocalization loc = bundle.getLocalizationSince(1);
			String line = loc.getRawText();

			int cut = line.indexOf("=");
			int start = line.indexOf("(");
			int end = line.indexOf(')');
			
			if (cut<0 || start<0 || start > cut || end < start || end>cut)
				throw new GTException(this, GTTrouble.GTJEP_FUNCTION_DECLARATION, bundle);

			SourceLocalization key = loc.getSubLocalization(0, start);
			
			
			String lhs = line.substring(0, end);			
			Vector<SourceLocalization> args = new Vector<>();
			int idx = start;
			int jdx = lhs.indexOf(",", idx+1);
			if (jdx<0 || jdx>end)
				jdx = end;
			while (jdx>0) {
				SourceLocalization arg = loc.getSubLocalization(idx+1, jdx-idx-1).trim();
				args.add(arg);
				idx = jdx; 
				jdx = lhs.indexOf(",", idx+1);
				if (jdx<0 && idx<end)
					jdx = end;
			}
			
			int len = loc.getLength();
			SourceLocalization rhs = loc.getSubLocalization(cut+1, len-cut-1);
			String def = rhs.getText().trim();
			if (def.length()==0)
				throw new GTException(this, GTTrouble.GTJEP_FUNCTION_DECLARATION, bundle);
			
			context.addUserFunction(key, args, def);
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_EFF = new GTPrimitiveCommand(GTCommandKey.EFF, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int argsCount = bundle.getArgumentsCount();
			
			for (int idx = 0; idx < argsCount; idx++){
				SourceLocalization arg = bundle.getLocalizationAt(idx+1); 
				context.removeItem(arg);
			}
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_INIT = new GTPrimitiveCommand(GTCommandKey.INIT, 0) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			context.init();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_AFF = new GTPrimitiveCommand(GTCommandKey.AFF, -1) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			context.addToBoard(bundle);
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_ECRIS = new GTPrimitiveCommand(GTCommandKey.ECRIS, -1) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			// TODO : (done) ecris
			if (bundle.getArgumentsCount()<1)
				return null;
			
			SourceLocalization loc = bundle.getLocalizationSince(1);
			JObjectI<?> o = context.getJObject(loc);
			String msg = (o.getType() == JEP2Type.STRING)?  JEP2.getString(o).getValue() : context.format(o);
			
			TextStyle style = context.getGTStringStyle();
			for (Turtle t : context.getFocusedTurtles()) {
				UndoableAction action = t.addString(msg, style);
				context.addUndoableAction(action);
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_DIS = new GTPrimitiveCommand(GTCommandKey.DIS, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			// TODO : (done) dis
			if (bundle.getArgumentsCount()<1)
				return null;
			
			String msg = getStringSince(context, bundle, 1);
			Window owner = bundle.getTopLevelAncestor();
			GTDialog.show(owner, DIS, msg, false);
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PAUSE = new GTPrimitiveCommand(GTCommandKey.PAUSE, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			double x = getDoubleAt(context, bundle, 1);
			if (x<0)
				throw new GTException(this, GTTrouble.GTJEP_PAUSE_NEGATIVE_TIME, bundle, x+"");
			context.sleep((int) (1000*x));
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PVH = new GTPrimitiveCommand(GTCommandKey.PVH, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()<3)
				throw new GTException(this, GTTrouble.GTJEP_NOT_3D, bundle, getName(GTCommandKey.PVH));
			
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateX(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PVB = new GTPrimitiveCommand(GTCommandKey.PVB, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()<3)
				throw new GTException(this, GTTrouble.GTJEP_NOT_3D, bundle, getName(GTCommandKey.PVB));
			
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateX(-x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PVD = new GTPrimitiveCommand(GTCommandKey.PVD, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()<3)
				throw new GTException(this, GTTrouble.GTJEP_NOT_3D, bundle, getName(GTCommandKey.PVD));

			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateY(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PVG = new GTPrimitiveCommand(GTCommandKey.PVG, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()<3)
				throw new GTException(this, GTTrouble.GTJEP_NOT_3D, bundle, getName(GTCommandKey.PVG));

			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateY(-x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};
	
	
	final static private GTPrimitiveCommand COMMAND_PVXY = new GTPrimitiveCommand(GTCommandKey.PVXY, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()!=4)
				throw new GTException(this, GTTrouble.GTJEP_NOT_4D, bundle, getName(GTCommandKey.PVXY));

			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateXY(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_PVXZ = new GTPrimitiveCommand(GTCommandKey.PVXZ, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()!=4)
				throw new GTException(this, GTTrouble.GTJEP_NOT_4D, bundle, getName(GTCommandKey.PVXZ));
			
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateXZ(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_PVYZ = new GTPrimitiveCommand(GTCommandKey.PVYZ, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (context.getGeometry().getDimensionCount()!=4)
				throw new GTException(this, GTTrouble.GTJEP_NOT_4D, bundle, getName(GTCommandKey.PVYZ));
			
			double x = getDoubleAt(context, bundle, 1);
			x = context.convertToRadians(x);
			for (Turtle turtle : context.getFocusedTurtles())
				turtle.rotateYZ(x);
			context.repaintIfNeeded();
			context.sleep();
			return null;
		}
	};

	final static private GTPrimitiveCommand COMMAND_PHOTO = new GTPrimitiveCommand(GTCommandKey.PHOTO, 1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			String name = getStringSince(context, bundle, 1);
			context.writePicture(context.getImage(), name);
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_MG = new GTPrimitiveCommand(GTCommandKey.MG, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int argsCount = bundle.getArgumentsCount();
			if (argsCount == 0) {
				context.resetGeometry();
				return null;
			}
			
			GTRendererI renderer = context.getRenderer();
			MouseManager mouseManager = context.getMouseManager();
			GTGeometryI geo = context.getGeometry();

			SourceLocalization loc = bundle.getLocalizationAt(1);
			String key = loc.getText();

			if (key.equals(KeywordManager.TRANSLATION_KEY.translate())) {
				if (!mouseManager.translationAbility.isAvailable())
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION_UNAVAILABLE, bundle, key, geo.toString());
				
				if (argsCount<3 || argsCount>4)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				
				double x = getDoubleAt(context, bundle, 2);
				double y = getDoubleAt(context, bundle, 3);
				double z = (argsCount==3)? 0 : getDoubleAt(context, bundle, 4);;
				Point3D origin = renderer.getOrigin();
				origin = origin.getTranslated(new Point3D(x, y, z));
				renderer.setOrigin(origin);
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.ROTATION_KEY.translate())) {
				if (argsCount!=3)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
			
				double angle = getDoubleAt(context, bundle, 3);
				angle = context.convertToRadians(angle);
				QRotation rotation = renderer.getSpaceTransform();
				
				String axe =  bundle.getLocalizationAt(2).getText();
				if (axe.equals("x")) {
					if (!mouseManager.xRotationAbility.isAvailable())
						throw new GTException(this, GTTrouble.GTJEP_MANIPULATION_UNAVAILABLE, bundle, key+" "+axe, geo.toString());
					rotation = QRotation.getXRotation(angle).apply(rotation);
				} else if (axe.equals("y")) {
					if (!mouseManager.yRotationAbility.isAvailable())
						throw new GTException(this, GTTrouble.GTJEP_MANIPULATION_UNAVAILABLE, bundle, key+" "+axe, geo.toString());
					rotation = QRotation.getYRotation(angle).apply(rotation);
				} else if (axe.equals("z")) {
					if (!mouseManager.zRotationAbility.isAvailable())
						throw new GTException(this, GTTrouble.GTJEP_MANIPULATION_UNAVAILABLE, bundle, key+" "+axe, geo.toString());
					rotation = QRotation.getZRotation(angle).apply(rotation);
				} else if (geo instanceof GTEuclidean4DGeometry) {
						((GTEuclidean4DGeometry) geo).do4DRotation(axe, angle);
						context.repaintIfNeeded();
						return null;
				} else 
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				renderer.setSpaceTransform(rotation);
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.ZOOM_KEY.translate())) {
				if (!mouseManager.zoomAbility.isAvailable())
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION_UNAVAILABLE, bundle, key, geo.toString());
				if (argsCount!=2)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				
				double u = getDoubleAt(context, bundle, 2);
				int w = renderer.getWidth();
				int h = renderer.getHeight();
				renderer.zoom(u, new Point(w/2, h/2));
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.H_ROTATION_KEY.translate())) {
				if (argsCount!=2)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				double angle = getDoubleAt(context, bundle, 2);
				angle = context.convertToRadians(angle);
				if (geo instanceof GTPoincareHPGeometry)
					((GTPoincareHPGeometry) geo).doHRotation(angle);
				else if (geo instanceof GTPoincareDiscGeometry)
					((GTPoincareDiscGeometry) geo).doHRotation(angle);
				else
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.H_TRANSLATION_KEY.translate())) {
				if (argsCount!=3)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				double x = getDoubleAt(context, bundle, 2);
				double y = getDoubleAt(context, bundle, 3);
				if (geo instanceof GTPoincareHPGeometry) {
					Point3D origin = renderer.getOrigin();
					origin = origin.getTranslated(new Point3D(x, y, 0));
					renderer.setOrigin(origin);
				} else if (geo instanceof GTPoincareDiscGeometry)
					((GTPoincareDiscGeometry) geo).doHTranslation(x, y); 
				else
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.Q_TRANSLATION_KEY.translate())) {
				if (argsCount!=3)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				double x = getDoubleAt(context, bundle, 2);
				double y = getDoubleAt(context, bundle, 3);
				if (geo instanceof GTFlatQuotientGeometry)
					((GTFlatQuotientGeometry) geo).doQTranslation(x, y);
				else
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				context.repaintIfNeeded();
				return null;
			}

			if (key.equals(KeywordManager.TURTLE_VISION_KEY.translate())) {
				if (argsCount!=2)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				context.resetGeometry();
				Turtle t = context.getTurtleAt(bundle, 2);
				if (geo.getRendererType() == RENDERER_TYPE.FW3D ) { 
					QRotation r = t.getRotation().inv();
					r = QRotation.getXRotation(-Math.PI/2).apply(r);
					Point3D p = geo.get3DCoordinates(t.getPosition());
//					if (geo instanceof GTSphericalGeometry) { // TODO : mg vision in spherical geometry
//						r = QRotation.getXRotation(Math.PI/2).apply(r);
//						//r = QRotation.getXRotation(Math.PI/8).apply(r);
//						p = r.apply(p).opp().getTranslated(0, -renderer.getUnit(), 0);
//					}
					p = r.apply(p).opp();
					renderer.setOrigin(p);
					renderer.setSpaceTransform(r);
					
					GTPerspective perspective = context.getPerspective();
					if (perspective instanceof GTConicPerspective) {
						double d = ((GTConicPerspective) perspective).getFocalDistance();
						renderer.setOrigin(renderer.getOrigin().getTranslated(0, -2.4, d+10));
					}
				} else {
					QRotation r = t.getRotation().inv();
					renderer.setSpaceTransform(r);
					Point3D p = geo.get3DCoordinates(t.getPosition());
					p = r.apply(p).opp();
					renderer.setOrigin(p);
				}
				
				context.repaintIfNeeded();
				return null;
			}
			
			if (key.equals(KeywordManager.DELAY_KEY.translate())) { // TODO : (done) mg délai
				if (argsCount!=2)
					throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
				int d = (int) getDoubleAt(context, bundle, 2);
				context.setWaitingTime(d);
				return null;
			}
			
			throw new GTException(this, GTTrouble.GTJEP_MANIPULATION, bundle);
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_ASK_FOR = new GTPrimitiveCommand(GTCommandKey.ASK_FOR) {
		
		protected JObjectI<?> execute_(final GTCommandBundle bundle, final GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int len = bundle.getArgumentsCount();
			if (len==0)
				throw new GTException(this, GTTrouble.GTJEP_ASK_FOR, bundle);
			SourceLocalization key = bundle.getLocalizationAt(1);
			String msg = (len>1) ? getStringSince(context, bundle, 2) : "" ;
			context.askFor(bundle, key, msg);
			return null;
		}
	};
	

	final static private GTPrimitiveCommand COMMAND_FOR_EACH = new GTPrimitiveCommand(GTCommandKey.FOR_EACH, -1) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int count = bundle.getArgumentsCount();
			if (count<1)
				throw new GTException(this, GTTrouble.GTJEP_FOR_EACH, bundle);
			
			String varName = bundle.getLocalizationAt(1).getText();
			
			if (count==4) {
				if (!bundle.getLocalizationAt(2).getText().equals(KeywordManager.IN_LIST_KEY.translate()))
					throw new GTException(this, GTTrouble.GTJEP_FOR_EACH, bundle);
				
				JObjectI<?> obj = context.getJObjectAt(bundle, 3);
				JIterable list = JEP2.getIterable(obj);
	
				GTCommandBundles commands = bundle.parseBundleAt(4);
				for (JObjectI<?> item : list.getItems()) {
						context.addLoopVariable(varName, item);
						try {
							JObjectI<?> o = context.process(commands);
							if (o!=null)  
								return o;
						} finally {
							context.removeLoopVariable(varName);
						}
					}
			} else if (count==6) {
				if  (!bundle.getLocalizationAt(2).getText().equals(KeywordManager.FROM_KEY.translate()) ||
						!bundle.getLocalizationAt(4).getText().equals(KeywordManager.TO_KEY.translate()))
					throw new GTException(this, GTTrouble.GTJEP_FOR_EACH, bundle);
				int idx0 = (int) getDoubleAt(context, bundle, 3);
				int idx1 = (int) getDoubleAt(context, bundle, 5);
				
				GTCommandBundles commands = bundle.parseBundleAt(6);
				for (int idx = idx0; idx <= idx1; idx++) {
						context.addLoopVariable(varName, JEP2.createNumber(idx));
						try {
							JObjectI<?> o = context.process(commands);
							if (o!=null)  
								return o;
						} finally {
							context.removeLoopVariable(varName);
						}
					}	
				
			} else 
				throw new GTException(this, GTTrouble.GTJEP_FOR_EACH, bundle);
			return null;
		}
			
	};

	
	final static private GTPrimitiveCommand COMMAND_WAIT = new GTPrimitiveCommand(GTCommandKey.WAIT, 0) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
				context.suspend();
				return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_RETURN = new GTPrimitiveCommand(GTCommandKey.RETOURNE, 1) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
				return context.getJObjectAt(bundle, 1);
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_STOP = new GTPrimitiveCommand(GTCommandKey.STOP, 0) { 
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			return JNullObject.NULL_OBJECT;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_UNDO = new GTPrimitiveCommand(GTCommandKey.UNDO, 0){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			context.undo();
			return null;
		}
	};
	
	
	final static private GTPrimitiveCommand COMMAND_CERCLE = new GTPrimitiveCommand(GTCommandKey.CERCLE, 1){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			GTGeometryI g = context.getGeometry();
			if (g instanceof GTEuclidean2DGeometry) {
				GTEuclidean2DGeometry g2d = (GTEuclidean2DGeometry) g;
				double x = getDoubleAt(context, bundle, 1);
				for (Turtle turtle : context.getFocusedTurtles()) {
					UndoableAction action = turtle.addCircle(g2d, x);
					context.addUndoableAction(action);
				}
				context.repaintIfNeeded();
				context.sleep();
			}
			return null;
		}
	};
	
	
	
	final static private GTPrimitiveCommand COMMAND_ARC = new GTPrimitiveCommand(GTCommandKey.ARC, 2){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			GTGeometryI g = context.getGeometry();
			if (g instanceof GTEuclidean2DGeometry) {
				GTEuclidean2DGeometry g2d = (GTEuclidean2DGeometry) g;
				double x = getDoubleAt(context, bundle, 1);
				double y = getDoubleAt(context, bundle, 2);
				y = context.convertToRadians(y);
				for (Turtle turtle : context.getFocusedTurtles()) {
					UndoableAction action = turtle.addArc(g2d, x, y);
					context.addUndoableAction(action);
				}
				context.repaintIfNeeded();
				context.sleep();
			}
			return null;
		}
	};
	
	
	
	final static private GTPrimitiveCommand COMMAND_POINT = new GTPrimitiveCommand(GTCommandKey.POINT, 0){
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			GTGeometryI g = context.getGeometry();
				for (Turtle turtle : context.getFocusedTurtles()) {
					UndoableAction action = turtle.addPoint(g);
					context.addUndoableAction(action);
				}
				context.repaintIfNeeded();
				context.sleep();
				return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_GLOBAL = new GTPrimitiveCommand(GTCommandKey.GLOBAL) {
		
		protected JObjectI<?> execute_(final GTCommandBundle bundle, final GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int len = bundle.getArgumentsCount();
			for (int idx = 1; idx <= len; idx++) 
				context.declareGlobal(bundle.getLocalizationAt(idx));
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_EXECUTE = new GTPrimitiveCommand(GTCommandKey.EXECUTE, -1) {
		
		protected JObjectI<?> execute_(final GTCommandBundle bundle, final GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			if (bundle.getArgumentsCount()<1)
				throw new GTException(this, GTTrouble.GTJEP_EXEC, bundle, "");
			String str = getStringSince(context, bundle, 1);
			
			GTCommandBundles bundles = GTCommandBundle.parse(SourceLocalization.create(str, bundle.getTopLevelAncestor()));
			try {
				context.process(bundles);
			} catch (GTException ex) {
				throw new GTException(this, GTTrouble.GTJEP_EXEC, bundle, str);
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_PLAY = new GTPrimitiveCommand(GTCommandKey.PLAY, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			// TODO : (done) chante do mi sol + chante liste
			
			int count = bundle.getArgumentsCount();
			ArrayList<MusicEvent> events = new ArrayList<>(); 
			try { // try to get a list
				SourceLocalization loc0 = bundle.getLocalizationSince(1);
				JObjectI<?> l = context.getJObject(loc0);
				events = toList((JEP2.getList(l)).getItems(), this, loc0);
			} catch (GTException | JEPException ex) {
				for (int idx = 0; idx < count; idx++) { // get individual music events
					SourceLocalization loc = bundle.getLocalizationAt(idx + 1);

					JObjectI<?> o = context.getJObject(loc);
					if (o.getType() == JEP2Type.MUSIC)
						events.add((MusicEvent) o);
					else
						throw new GTException(this, GTTrouble.GTJEP_MIDI_INVALID_EVENT, loc, o.toString());
				}
			}
			try {
				context.setMidiScore(events);
				context.play(bundle);
			} catch (NoMidiException e) {
				throw new GTException(this, GTTrouble.GTJEP_MIDI_UNAVAILABLE, bundle);
			}
			return null;
		}
	};
	
	final static private GTPrimitiveCommand COMMAND_SCORE = new GTPrimitiveCommand(GTCommandKey.SCORE, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			ArrayList<MusicEvent> events = new ArrayList<>();

			SourceLocalization loc = bundle.getLocalizationSince(1);
			JObjectI<?> o = context.getJObject(loc);
			JList l = JEP2.getList(o);
			JObjectsVector vec = l.getItems();
				
			events = toList(vec, this, loc);
			
			for (Turtle turtle : context.getFocusedTurtles()) 
				turtle.setScore(events);
			return null;
		}
	};
	
	private static ArrayList<MusicEvent> toList(JObjectsVector vec, HelpI h, SourceLocalization loc) throws GTException {
		ArrayList<MusicEvent> events = new ArrayList<>();
		for (JObjectI<?> o : vec) 
			if (o.getType() == JEP2Type.MUSIC)
				events.add((MusicEvent) o);
			else
				throw new GTException(h, GTTrouble.GTJEP_MIDI_INVALID_EVENT, loc, o.toString());
		
		return events;
	}
	
	final static private GTPrimitiveCommand COMMAND_CONCERT = new GTPrimitiveCommand(GTCommandKey.CONCERT, -1) {
		protected JObjectI<?> execute_(GTCommandBundle bundle, GTProcessingContext context) throws GTException, GTInterruptionException, JEPException {
			int count = bundle.getArgumentsCount();
			try {
				context.setMidiScores(context.getFocusedTurtles());
				if (count==0)
					context.play(bundle); 
				else {
					String str = getStringSince(context, bundle, 1);					
					context.writeMidi(str);
				}
			} catch (NoMidiException ex) {
				throw new GTException(GTTrouble.GTJEP_MIDI_UNAVAILABLE, bundle);
			} catch (NoScoreException ex) {
				throw new GTException(GTTrouble.GTJEP_MIDI_TURTLE_WO_SCORE, bundle, ex.getName());
			} catch (MidiChannelException ex) {
				throw new GTException(GTTrouble.GTJEP_MIDI_INVALID_CHANNEL, bundle);
			} catch (JEPException ex) {
				throw new GTException(this, GTTrouble.GTJEP_MIDI_CONCERT, bundle);				
			}
			return null;
		}
	};

	// TODO : (done) fonctions tempo instrument silence C# 
	// TODO : (done) partition
	// TODO : (done) concert + concert fichier.mid
}