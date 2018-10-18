package mycontroller;

import java.util.ArrayList;

import controller.CarController;
import utilities.Coordinate;

public interface  IPathFindingStrategy {
	public ArrayList<Coordinate> getPath(Coordinate start,Coordinate finish);
	public static IPathFindingStrategy getInstance(CarController creator) {
		return null;
	}
}
