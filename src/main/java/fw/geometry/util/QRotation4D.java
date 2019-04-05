package fw.geometry.util;


public class QRotation4D {

	protected QRotation q1, q2;
	
	public QRotation getQ1() {
		return q1;
	}

	public QRotation getQ2() {
		return q2;
	}

	public QRotation4D() {
		q1 = new QRotation();
		q2 = new QRotation();
	}
	
	public QRotation4D(QRotation r1, QRotation r2) {
		q1 = r1;
		q2 = r2;
	}

	public QRotation4D apply(QRotation4D r) {
		QRotation r1 = QRotation.mul(this.q1, r.q1);
		QRotation r2 = QRotation.mul(r.q2, this.q2);
		return new QRotation4D(r1, r2);
	}
	
	public QRotation4D inv() {
		return new QRotation4D(q1.inv(), q2.inv());
	}
	
	public Point4D apply(Point4D p){
		Quaternion q = new Quaternion(p.t, p.x, p.y, p.z);
		Quaternion p1 =  Quaternion.mul(q1, Quaternion.mul(q, q2));
		return new Point4D(p1.x, p1.y, p1.z, p1.s);
	}
	
	public static QRotation4D getXT(double angle) {
		QRotation r = QRotation.getXRotation(angle);
		return new QRotation4D(r, r.inv());
	}

	public static QRotation4D getYT(double angle) {
		QRotation r = QRotation.getYRotation(angle);
		return new QRotation4D(r, r.inv());
	}

	public static QRotation4D getZT(double angle) {
		QRotation r = QRotation.getZRotation(angle);
		return new QRotation4D(r, r.inv());
	}

	public static QRotation4D getXY(double angle) {
		QRotation r = QRotation.getZRotation(angle);
		return new QRotation4D(r, r);
	}

	public static QRotation4D getXZ(double angle) {
		QRotation r = QRotation.getYRotation(angle);
		return new QRotation4D(r, r);
	}
	
	public static QRotation4D getYZ(double angle) {
		QRotation r = QRotation.getXRotation(angle);
		return new QRotation4D(r, r);
	}
	
	private static final QRotation R1 = QRotation.getYRotation(-Math.PI/2);
	private static final QRotation R2 = QRotation.mul(QRotation.getI(), QRotation.getYRotation(Math.PI/2));

	public static QRotation4D getYZT() {
			return new QRotation4D(R1, R2);
	}
	
	public static QRotation4D getZTX() {
		QRotation r1 = QRotation.getI();
		QRotation r2 = QRotation.getK().inv();
		return new QRotation4D(r1, r2);
	}

	public static QRotation4D getTXY() {
			return new QRotation4D(R2, R1);
	}

	@Override
	public String toString() {
		return "q1 = "+q1.toString()+"\n"+"q2 = "+q2.toString();
	}
}