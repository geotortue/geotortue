package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;


public class FixGridLayout extends BasicLayoutAdapter {
	
	private int nx, ny, hgap, vgap, cellW, cellH;
	
	public FixGridLayout(int nx, int ny, int hgap, int vgap, int cellW, int cellH){
		this.nx=nx;
		this.ny=ny;
		this.hgap=hgap;
		this.vgap=vgap;
		this.cellW=cellW;
		this.cellH=cellH;
	}

	@Override
	public void layoutComponent(Component c, int idx) {
		int x0 = 1+hgap/2;
		int y0 = insets.top;
	
		currX = hgap+x0+(idx % nx)*(cellW+hgap);
		currY = vgap+y0+(idx / nx)*(cellH+vgap);
		c.setBounds(currX, currY, cellW, cellH);
	}
	
	public Dimension minimumLayoutSize(Container arg0) {
		int x=2*hgap+nx*(cellW+hgap)+1;
		int y=2*vgap+ny*(cellH+vgap);
		return new Dimension(x, y);
	}
}
