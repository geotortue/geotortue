package geotortue.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation;
import fw.geometry.util.QRotation4D;
import fw.geometry.util.TangentVector;
import fw.text.TextStyle;
import fw.xml.XMLCapabilities;
import fw.xml.XMLEntry;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.TurtleAvatar2D.AVATAR_TYPE;
import geotortue.geometry.GTEuclidean2DGeometry;
import geotortue.geometry.GTEuclidean4DGeometry;
import geotortue.geometry.GTGeometry;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTGeometryI.GeometryException;
import geotortue.geometry.GTPoint;
import geotortue.geometry.GTRotation;
import geotortue.geometry.GTTransport;
import geotortue.geometry.obj.GTArc;
import geotortue.geometry.obj.GTCircle;
import geotortue.geometry.obj.GTDot;
import geotortue.geometry.obj.GTFilledCircle;
import geotortue.geometry.obj.GTObject;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.obj.GTString;
import geotortue.renderer.GTRendererI;
import sound.MusicEvent;


public class Turtle implements XMLCapabilities {

	private GTPoint position = new GTPoint(0, 0, 0);
	private GTRotation rotation = new GTRotation();
	
	private String name;
	private TurtleAvatar2D avatar2d;
	private AVATAR_TYPE avatarType = AVATAR_TYPE.TURTLE;
	private TurtleAvatar3D avatar3d;
	private Color color;
	private boolean visible = true;
	private TurtlePen pen = new TurtlePen(); 
	private boolean recordingEnabled = false;
	
	private int orientation = 1;
	
	private final List<GTObject> path = Collections.synchronizedList(new ArrayList<GTObject>());
	private GTPolygon tempPolygon = null;
	public static final Color GREEN = new Color(160, 220, 160);

	public static final XMLTagged XML_TAG = XMLTagged.Factory.create("Turtle");

	private List<MusicEvent> score = null;
	
	public Turtle(String name) {
		this.name = name;
		setColor(GREEN);
		avatar2d = new TurtleAvatar2D(color);
	}
	
	public Turtle(XMLReader e) {
		loadXMLProperties(e);
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		this.color = c;
		avatar2d = new TurtleAvatar2D(color, avatarType);
		avatar3d = new TurtleAvatar3D(color);
	}
	
	public AVATAR_TYPE getAvatarType() {
		return avatarType;
	}
	
	public void setAvatarType(AVATAR_TYPE type) {
		if (avatarType == type)
			return;
		avatarType = type;
		avatar2d = new TurtleAvatar2D(color, avatarType);
	}

	public TurtleAvatar2D getAvatar2D() {
		return avatar2d;
	}

	public TurtleAvatar3D getAvatar3D() {
		return avatar3d;
	}

	public GTPoint getPosition() {
		return position;
	}
	
	public void drawPath(GTRendererI r, GTGeometryI g) {
		synchronized (path) {
			for (GTObject s : path) 
				s.draw(g, r);
		}
	}
	
	public void openRecording(GTGeometryI geo) throws RecordingAlreadyOpenedException {
		tempPolygon = geo.createPolygon(position, pen);
		if (recordingEnabled) 
			throw new RecordingAlreadyOpenedException();
		this.recordingEnabled = true;
	}
	
	public void addPolygon() {
		path.add(tempPolygon);
	}
	
	public void giveUpRecording() {
		this.recordingEnabled = false;		
	}
	
	public static class RecordingAlreadyOpenedException extends Exception {
		private static final long serialVersionUID = 7728459398196909359L;
	}

	public void setPosition(GTPoint p) throws NonFlatPolygonException {
		this.position = p;
		if (recordingEnabled)
			tempPolygon.add(position);
	}

	public GTRotation getRotation4D() {
		return rotation;
	}
	
	public QRotation getRotation() {
		return rotation.getRotation3D();
	}


	public void setRotation(GTRotation r) {
		this.rotation = r;
	}

	public int getOrientation() {
		return orientation;
	}

	public void reset() {
		synchronized (path) {
			path.clear();
		}
		
		giveUpRecording();
		position = new GTPoint(0, 0, 0);
		rotation = new GTRotation();
		orientation = 1;
		
		visible = true;
		pen.reset();
		score = null;
	}
	

	public UndoableAction walk(GTGeometry gc, double d) throws GeometryException, NonFlatPolygonException {
		if (d==0)
			return null;
		
		GTPoint oldPosition = position;
		GTRotation oldRotation = rotation;
		
		TangentVector v = new TangentVector(rotation.apply(new Point3D(0, 1, 0)));

		if (gc instanceof GTEuclidean4DGeometry) {
			v = new TangentVector(rotation.apply(new Point4D(0, 1, 0, 0)));
		}

		GTTransport tp = gc.getGTTransport(position, v, d);
		tp.apply(this);

		if (recordingEnabled)
			return new UndoableAction(this, oldPosition, oldRotation);

		Vector<GTObject> segments = tp.getPath(pen);
		return addToPath(segments, oldPosition, oldRotation);
	}
	
	public UndoableAction addCircle(GTEuclidean2DGeometry g2d, double radius) {
		if (radius==0)
			return null;

		double r = orientation*radius;
		Point3D p = rotation.apply(new Point3D(-r, 0, 0));
		GTPoint center = position.getTranslated(p);
		GTCircle c;
		if (recordingEnabled)
			c = new GTFilledCircle(center, Math.abs(radius), pen);
		else 
			c = new GTCircle(center, Math.abs(radius), pen);
		return addToPath(c, position, rotation);
	}
	
	public UndoableAction addArc(GTEuclidean2DGeometry g2d, double radius, double a) {
		if (radius==0)
			return null;
		
		GTPoint oldPosition = position;
		GTRotation oldRotation = rotation;
		
		double r = orientation*radius;
		Point3D p = rotation.apply(new Point3D(-r, 0, 0));
		GTPoint center = position.getTranslated(p);
		double angle = (r<0) ? -a : a;
		GTArc arc = new GTArc(center, position, angle, pen);
		if (recordingEnabled)
			tempPolygon.add(arc);
		
		position = arc.getEndPoint(g2d);
		rotation = GTRotation.getZRotation(angle).apply(rotation);
		
		return addToPath(arc, oldPosition, oldRotation);
	}
	

	public UndoableAction addPoint(GTGeometryI g) {
		GTDot d = new GTDot(position, pen);
		return addToPath(d, position, rotation);
	}
	
	
	public UndoableAction addString(String msg, TextStyle style) {
		GTString s = new GTString(msg, style, position, pen);
		return addToPath(s, position, rotation);
	}
	
	
	private UndoableAction addToPath(Vector<GTObject> objects, GTPoint oldPosition, GTRotation oldRotation) {
		if (pen.isDown()) {
			synchronized (path) {
				for (GTObject s : objects)
					path.add(s);
			}
		}
		return new UndoableAction(this, oldPosition, oldRotation, objects);
	}
	
	private UndoableAction addToPath(GTObject obj, GTPoint oldPosition, GTRotation oldRotation) {
		if (pen.isDown()) {
			synchronized (path) {
					path.add(obj);
			}
			UndoableAction action = new UndoableAction(this, oldPosition, oldRotation);
			action.add(obj);
			return action;
		}
		return new UndoableAction(this, oldPosition, oldRotation);
	}

	public void rotateX(double x) {
		rotation = rotation.apply(QRotation4D.getXT(x));
	}

	public void rotateY(double x) {
		rotation = rotation.apply(QRotation4D.getYT(x));
	}

	public void rotateZ(double x) {
		if (orientation == 1)
			rotation = rotation.apply(QRotation4D.getZT(x));
		else 
			rotation = rotation.apply(QRotation4D.getZT(-x));
	}
	
	public void rotateXY(double x) {
		rotation = rotation.apply(QRotation4D.getXY(x));
	}

	public void rotateXZ(double x) {
		rotation = rotation.apply(QRotation4D.getXZ(x));
	}

	public void rotateYZ(double x) {
		rotation = rotation.apply(QRotation4D.getYZ(x));
	}
	
	public void invertOrientation() {
		orientation = - orientation;
	}
	
	public TurtlePen getPencil() {
		return pen;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void remove(GTObject s) {
		synchronized (path) {
			path.remove(s);
		}
	}
	
	/*
	 * XML
	 */
	
	public String getXMLTag() {
		return XML_TAG.getXMLTag();
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("name", name);
		e.setAttribute("color", color);
		e.setAttribute("avatar", avatarType.toString());
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {
			name = child.getAttribute("name");
		} catch (XMLException ex) {
			ex.printStackTrace();
			name = "null"+Math.random();
		}
		try {
			setColor(child.getAttributeAsColor("color"));
		} catch (XMLException ex) { // Backward compatibility
				XMLReader colorChild = child.popChild(XMLTagged.Factory.create("Color"));
				if (colorChild != XMLEntry.NULL_ENTRY) {
					int r = colorChild.getAttributeAsInteger("red", 160);
					int g = colorChild.getAttributeAsInteger("green", 220);
					int b = colorChild.getAttributeAsInteger("blue", 160);
					setColor(new Color(r, g, b));
				} else
					setColor(GREEN);
		}
		String type = child.getAttribute("avatar", AVATAR_TYPE.TURTLE.toString());
		setAvatarType(AVATAR_TYPE.valueOf(type));
		return child;
	}

	@Override
	public String toString() {
		return "Turtle "+name;
	}

	/**
	 * @param vec
	 */
	public void setScore(List<MusicEvent> vec) {
		this.score = vec;
	}
	
	public List<MusicEvent> getScore() throws NoScoreException {
		if (score == null)
			throw new NoScoreException();
		return score;
	}
	
	public class NoScoreException extends Exception {
		private static final long serialVersionUID = 7387039241571198992L;

		public String getName() {
			return Turtle.this.getName();
		}
	}
}