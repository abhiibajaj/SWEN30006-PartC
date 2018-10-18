package mycontroller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;


public class Astar implements IPathFindingStrategy{
	private CarController C;
	private static Astar instance;
	private HashMap<Coordinate,AstarNode> nodes;
	private int maxX; 
	private int maxY;
	//

	private Astar(CarController creator) {
		C=creator;
		nodes = new HashMap<Coordinate,AstarNode>();
		loadMap();
	}
	public static IPathFindingStrategy getInstance(CarController creator) {
		if(instance!=null) {
			return instance;
		}
		instance = new Astar(creator);
		return instance;
	}
	public ArrayList<Coordinate> getPath(Coordinate i,Coordinate g){
		//System.out.println("GETTINGPATH");

		ArrayList<AstarNode> open = new ArrayList<AstarNode>();
		ArrayList<AstarNode> clsd = new ArrayList<AstarNode>();
		AstarNode current=nodes.get(i);
		AstarNode goal=nodes.get(g);
		current.parent=null;
		open.add(current);
		while(!open.isEmpty()) {
			//System.out.println("OpenLoop");
			open.sort(fX);
			current = open.get(0);
			if (current.point.equals(goal.point)) {
				
				return tracePath(goal);
			}
			open.remove(current);
			clsd.add(current);
			
			for(Coordinate potentialPt:getMoves(current.point)) {
				AstarNode potentialNode = nodes.get(potentialPt);
				if(potentialNode.isBlocked) {
					//System.out.println("Blocked Check");
					// this tile is useless to us!
					continue;
				}
				if(!clsd.contains(potentialNode)) {
					if(!open.contains(potentialNode)) {
						potentialNode.parent=current;
						potentialNode.gCalc(current);
						potentialNode.hCalc(goal.point);
						open.add(potentialNode);
					}
					else if(potentialNode.g<current.g) {
							//Move on to this node!
							potentialNode.gCalc(current);
							current=potentialNode;							
						}
					}

				}
			}
		//System.out.println("GETTINGPATH");
		//satisfy compiler
		return null;
	}
	// now that we have set the appropriate parents for each node then we can find A* path by tracing the parents from the destination back to the original start (which will not have a parent)
	private ArrayList<Coordinate> tracePath(AstarNode dest){
		ArrayList<Coordinate> response = new ArrayList<Coordinate>();
		AstarNode node = dest;
		while(node.parent!=null) {
			//System.out.println(node.point);
			response.add(node.point);
			node = node.parent;
		}
		System.out.println("Completed Path");
		return response;
	}
	
	
	// return the tiles around us.
	private ArrayList<Coordinate> getMoves(Coordinate point){
		ArrayList<Coordinate> response = new ArrayList<Coordinate>();
		for (int i=-1;i<2;i++) {
			for (int j=-1;j<2;j++) {
				int newX=point.x+i;
				int newY=point.y+j;
				if((i==0&&j==0)||newX<0||newY<0||newX>maxX||newY>maxY) {
					//invalid point, don't add to list (same, or outside bounds)
					continue;
				}
				else if(Math.abs(i)==Math.abs(j)) {
					//no diagonals
					continue;
				}
				else {
					response.add(new Coordinate(newX,newY));
				}
			}
		}
		
		return response;
	}

	private void loadMap() {
		HashMap<Coordinate, MapTile> map = C.getMap();
		maxX=-1;
		maxY=-1;
		for (Coordinate point:map.keySet()) {
			if(point.x>maxX) {
				maxX=point.x;
			}
			if(point.y>maxY) {
				maxY=point.y;
			}
			boolean isWall = map.get(point).isType(MapTile.Type.WALL)||map.get(point).isType(MapTile.Type.EMPTY);
			AstarNode node = new AstarNode(point,isWall);
			nodes.put(point,node);
		}
	}
	private Comparator<AstarNode> fX = new Comparator<AstarNode>() {
		public int compare(AstarNode a, AstarNode b) {
			return Double.compare(a.fX(),b.fX());
		}
	};
}
