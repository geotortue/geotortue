package fw.geometry.proj;



public class ZPoint {

public double x, y, z;
	
	public ZPoint(double x, double y, double z){
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public String toString(){
		return "ZPoint : x = "+x+"; y = "+y+"; z= "+z;
	}
}
