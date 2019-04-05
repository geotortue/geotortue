/**
 * 
 */
package geotortue.core;

import java.util.Vector;

import geotortue.geometry.GTPoint;
import geotortue.geometry.GTRotation;
import geotortue.geometry.obj.GTObject;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;

/**
 *
 */
public class UndoableAction {
	
	private final GTPoint oldPosition;
	private final GTRotation oldRotation;
	private final Vector<GTObject> objects;
	private final Turtle turtle;
	
	public UndoableAction(Turtle t, GTPoint position, GTRotation rotation, Vector<GTObject> objects) {
		this.turtle = t;
		this.oldPosition = position;
		this.oldRotation = rotation;
		this.objects = objects;
	}
	
	public UndoableAction(Turtle t, GTPoint position, GTRotation rotation) {
		this(t, position, rotation, new Vector<GTObject>());
	}
	
	public void add(GTObject obj) {
		objects.add(obj);
	}
	
	
	public void undo() {
		try {
			turtle.setPosition(oldPosition);
		} catch (NonFlatPolygonException ex) {
			ex.printStackTrace();
		}

		turtle.setRotation(oldRotation);
		
		for (GTObject s : objects) 
			turtle.remove(s);
	}
}
