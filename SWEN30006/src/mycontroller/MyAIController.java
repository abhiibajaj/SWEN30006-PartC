package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.plaf.synth.SynthSeparatorUI;

import controller.CarController;
import tiles.*;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController{
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;


	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	private HashMap<Integer,Coordinate> keyCoords;
	private ArrayList<Coordinate> path;
	private Coordinate location;
	private IPathFindingStrategy pathStrategy;
	private Coordinate target;

	// Car Speed to move at
	private float CAR_SPEED = (float) 2 ;

	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;

	public MyAIController(Car car) {
		super(car);
		keyCoords=new HashMap<Integer, Coordinate>();
		path = new ArrayList<Coordinate>();
		pathStrategy = CompositePathFinder.getInstance(this);

		// Add new strategies here
		IPathFindingStrategy astarStrat;
		astarStrat = Astar.getInstance(this);
		CompositePathFinder.addStrategy(astarStrat);
	}

	Coordinate initialGuess;
	boolean notSouth = true;
	private int i;
	@Override
	public void update(float delta) {
		location = new Coordinate(Math.round(getX()),Math.round(getY()));
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		//System.out.println("UPDATE");
		checkStateChange();
		updateKeyCoordinates(currentView);

		// No Path and available key to path to ? Path and drive!
		if (path==null)path = new ArrayList<Coordinate>();
		if (path.size()>0) {
			if(Math.abs(location.x-path.get(path.size()-1).x)>1||Math.abs(location.y-path.get(path.size()-1).y)>1) {
				//we're too far off-path! new path
				path = pathStrategy.getPath(new Coordinate((int)getX(),(int)getY()), target);
				System.out.println("OFF PATH");

			}
			System.out.println(path);
		}

		if(keyCoords.containsKey(getKey()-1)&&target!=keyCoords.get(getKey()-1)) {

			// We have found the location of the key we need, plug it into A* and get a path.!
			target = keyCoords.get(getKey()-1);
			path = pathStrategy.getPath(new Coordinate((int)getX(),(int)getY()),target);
			System.out.println("KEY AT:"+keyCoords.get(getKey()-1));
			System.out.print("NEW PATH: ");
			System.out.println(path);
			driveToCoordinate(delta, path.get(path.size()-1));	

		} else if(path.size()>0) { // path ? drive path!
			System.out.println("LOCATION: "+ location);
			if(location.equals(path.get(path.size()-1))) { // on path point? remove!
				path.remove(path.size()-1);
			}
			if (path.size()==0) {
				return;
			}
			driveToCoordinate(delta, path.get(path.size()-1));

		}
		else if(getKey()==1){
			target=getFinish();
			path=pathStrategy.getPath(new Coordinate((int)getX(),(int)getY()),target);
			//FinalKey, Find Goal Tile
		}else{
			//drive somewhere!
			target=getRandomCoord();
			path = pathStrategy.getPath(new Coordinate((int)getX(),(int)getY()), target);
		} 

	}

	public void driveToCoordinate(float delta,Coordinate coord) {
		System.out.println("DRIVE TO: "+ coord);
		double angle = getAngleToDest(coord);
		//opposite of our angle
		double angleR = angle <= 180? angle+180:angle-180;
		System.out.println("A:"+angle+" cA:"+getAngle()+" R:"+angleR);

		if (angle>180) {
			if(Math.round(getAngle())==Math.round(angle)) {
				speedUp();
			}
			else if(getAngle()<angle &&getAngle()>angleR) {
				turnLeft(delta);
				slowDown();
			}
			else if(getAngle()>angle) {
				turnRight(delta);
				slowDown();
			}
			else if(getAngle()<angleR) {
				turnRight(delta);
				slowDown();
			}
		}
		else {
			if(Math.round(getAngle())==Math.round(angle)) {
				speedUp();
			}
			else if(getAngle()<angle||getAngle()>=angleR) {
				turnLeft(delta);
				slowDown();
			}
			else if(getAngle()>angle&&getAngle()<=angleR) {
				turnRight(delta);
				slowDown();
			}
		}
	}
	public double getAngleToDest(Coordinate coord) {
		//Coordinate location = new Coordinate(getPosition());
		float adjacent=coord.x-getX();
		float opposite=coord.y-getY();

		double angle = Math.toDegrees(Math.atan2(opposite,adjacent));
		if (angle<0) {
			angle+=360;
		}
		return angle;
	}

	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}


	public void updateKeyCoordinates(HashMap<Coordinate, MapTile> look) {
		for(Coordinate coord : look.keySet()) {
			MapTile tile = look.get(coord);

			if(tile.isType(MapTile.Type.TRAP)) {
				if(((TrapTile) tile).getTrap().equals("lava")) {
					int keyCheck = ((LavaTrap)(TrapTile) tile).getKey();
					if(keyCheck > 0) {
						if(!keyCoords.containsKey(keyCheck)){
							System.out.println(keyCheck);
							System.out.println(coord.toString());
							keyCoords.put(keyCheck, coord);
						}
					}
				}
			}
		}
	}
	private void slowDown() {
		if(getSpeed()>=0.8) {
			applyReverseAcceleration();
		}
		else {
			applyForwardAcceleration();
		}
	}
	private void speedUp() {
		if(getSpeed()<=CAR_SPEED) {
			applyForwardAcceleration();
		}
		else {
			applyReverseAcceleration();
		}
	}
	/*
	 * gets finish tile
	 * @return coordinate finish tile(one of)
	 */
	private Coordinate getRandomCoord() {
		HashMap<Coordinate, MapTile> look=getMap();
		ArrayList<Coordinate> coords=new ArrayList<Coordinate>(look.keySet());
		Random r = new Random();
		Coordinate response = coords.get(r.nextInt(coords.size()));
		if(look.get(response).isType(MapTile.Type.WALL)||look.get(response).isType(MapTile.Type.EMPTY)) {
			return getRandomCoord();
			// we don't like walls or empties. next!
		}
		//return a random coordinate from the map 
		return response;
	}
	private Coordinate getFinish() {
		HashMap<Coordinate, MapTile> look=getMap();
		for(Coordinate coord : look.keySet()) {
			MapTile tile = look.get(coord);

			if (tile.isType(MapTile.Type.FINISH)) {
				return coord;
			}
		}
		return null;
	}
	/*
	 * gets a health tile from a view.
	 */
	private Coordinate getHealth(HashMap<Coordinate, MapTile> look) {
		for(Coordinate coord : look.keySet()) {
			MapTile tile = look.get(coord);

			if(tile.isType(MapTile.Type.TRAP)) {
				if(((TrapTile) tile).getTrap().equals("health")) {
					return coord;
				}
			}
		}
		return null;
	}
}

