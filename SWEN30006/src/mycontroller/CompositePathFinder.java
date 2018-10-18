package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import controller.CarController;
import utilities.Coordinate;

public class CompositePathFinder implements IPathFindingStrategy {
	
	private CarController C;
	private static CompositePathFinder instance;
	private HashMap<Coordinate,AstarNode> nodes;
	private static ArrayList<IPathFindingStrategy> strategies;
	private int maxX; 
	private int maxY;
	

	private CompositePathFinder(CarController creator) {
		C=creator;
		nodes = new HashMap<Coordinate,AstarNode>();
		strategies = new ArrayList<>();
		
	}
	@Override
	public ArrayList<Coordinate> getPath(Coordinate start, Coordinate finish) {
		if(strategies.isEmpty()) return null;
		
		ArrayList<ArrayList<Coordinate>> coordinateArrays = new ArrayList<>();
		for(IPathFindingStrategy strategy : strategies) {
			ArrayList<Coordinate> path = strategy.getPath(start, finish);
			coordinateArrays.add(path);
		}
				
		int shortest = Integer.MAX_VALUE;
		ArrayList<Coordinate> finalPath = new ArrayList<>();
		
		for (ArrayList<Coordinate> path : coordinateArrays) {
			if(path.size()<shortest) {
				shortest = path.size();
				finalPath = path;
			}
		}
		return finalPath;
	}
	
	public static IPathFindingStrategy getInstance(CarController creator) {
		if(instance!=null) {
			return instance;
		}
		instance = new CompositePathFinder(creator);
		return instance;
	}
	public static void addStrategy(IPathFindingStrategy strategy) {
		strategies.add(strategy);
		
	}
	
}
