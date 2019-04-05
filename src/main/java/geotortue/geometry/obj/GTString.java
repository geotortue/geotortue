package geotortue.geometry.obj;

import java.awt.Font;

import fw.geometry.obj.GObject;
import fw.text.TextStyle;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTString extends GObject implements GTObject {
	
	private final GTPoint position;
	private final String text;
	private final TextStyle style;

	public GTString(String str, TextStyle s, GTPoint p, TurtlePen pen) {
		super(pen.getColor(), pen.getThickness());
		this.text = str;
		this.position = p;
		this.style = s;
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.draw(this,  r);
	}
	
	public String getText() {
		return text;
	}

	public Font getFont() {
		return style.getFont();
	}
	

	public GTPoint getPosition() {
		return position;
	}

}