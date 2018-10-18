package mycontroller;

import utilities.Coordinate;

public class AstarNode{
	public final Coordinate point;
	public double g;
	public double h;
	public boolean isBlocked;
	public AstarNode parent;
	
	public AstarNode(Coordinate point, boolean isWall) {
		this.point = point;
		this.isBlocked=isWall;
		g=0.0;
	}
	// Our g(x) is one more than previous node's g(x)
	public void gCalc(AstarNode prev) {
		this.g=prev.g+(Math.sqrt(Math.pow(Math.abs(this.point.x-prev.point.x),2)+Math.pow(Math.abs(this.point.y-prev.point.y),2)));
	}
	// h(x) Straight line dist to goal.
	public void hCalc(Coordinate pt) {
		this.h=(Math.sqrt(Math.pow(Math.abs(point.x-pt.x),2)+Math.pow(Math.abs(point.y-pt.y),2)));
	}
	public double fX() {
		return g+h;
	}
	
}
