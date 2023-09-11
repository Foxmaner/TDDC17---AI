package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;


class Pos{
	public int x;
	public int y;
	public Pos parent;
	Pos(int x, int y){
		this.x = x;
		this.y = y;
	}

	Pos offset(Pos sentPos){
		return new Pos(sentPos.x-this.x,sentPos.y-this.y);
	}

	@Override
	public String toString() {
		return "Pos [x=" + x + ", y=" + y + ", parent=" + parent + "]";
	}
	
	


}

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	// Variabels for bfs
	boolean finished = false;
	ArrayList<ArrayList<Pos>> queued;
	ArrayList<Pos> visited;
	ArrayList<Pos> route = new ArrayList<Pos>();
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
		{
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 5;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 400;
	public MyAgentState state = new MyAgentState();

	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}

	//Returns all the neighbors to the position, not accounting for orientation
	private Pos[] getNeighborsAbs(Pos pos) {
		Pos[] neighbors = new Pos[4];

		//North
		neighbors[0]=(new Pos(pos.x,pos.y-1));
		//South
		neighbors[1]=(new Pos(pos.x,pos.y+1));
		
		//East
		neighbors[3]=(new Pos(pos.x+1,pos.y));
		
		//West
		neighbors[2]=(new Pos(pos.x-1,pos.y));

		return neighbors;

	}
	//Functions to turn and move vacuum forward
	private Action turnRight() {
		state.agent_direction = ((state.agent_direction+1) % 4);
		state.agent_last_action = state.ACTION_TURN_RIGHT;
		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;

	}

	private Action turnLeft() {
		state.agent_direction = ((state.agent_direction+3) % 4);
		state.agent_last_action = state.ACTION_TURN_LEFT;
		return LIUVacuumEnvironment.ACTION_TURN_LEFT;
	}
	
	private Action goForward() {
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	} 

	//Search for new paths with BFS
	public ArrayList BFS(Pos startPos) {
		System.out.println("start_node:" + startPos);
		
		//Creates a arraylist of arraylists of pos
		//Then add a arraylist with only the start position
		ArrayList<ArrayList<Pos>> queued = new ArrayList<ArrayList<Pos>>();
		ArrayList temp = new ArrayList<Pos>();
		temp.add(new Pos(startPos.x,startPos.y));
		queued.add(temp);
		
		//A map of visited nodes
		boolean[][] visited = new boolean[state.world.length][state.world[0].length];
		
		//Cont as long as there is more to be explored
		while(!queued.isEmpty()){
			//Take first element
			ArrayList<Pos> path = queued.remove(0);
			Pos pos = path.get(path.size()-1);
			System.out.println("SEEEE MEEEE!!!!!" + pos);
			//If we find a pos which is unknown when cleaning
			//Or home when done cleaning. Return the path as we wanna explore that place
			if (state.world[pos.x][pos.y] == state.UNKNOWN || (state.finished == true && state.world[pos.x][pos.y] == state.HOME)){
				path.remove(0);
				System.out.println("path in bfs" + path);
				return path;
			}
			//Go through all neighbors and add all neighbors that is not visited/wall/invalid position, to a new queue.
			Pos[] neighbors = getNeighborsAbs(pos);
			for(Pos neighbor: neighbors) {
				if (visited[neighbor.x][neighbor.y] == false && state.world[neighbor.x][neighbor.y] != state.WALL && isValidPosition(neighbor)) {
	                ArrayList<Pos> new_path = new ArrayList<>(path);
	                new_path.add(neighbor);
	                queued.add(new_path);
	                visited[neighbor.x][neighbor.y] = true;
	            }		
						
			}
		}
		//Return empty list
		return new ArrayList<>();
	}
	
	//See if the pos is within the map
	private boolean isValidPosition(Pos pos) {
	    return pos.x >= 0 && pos.x < state.world.length - 1 && pos.y >= 0 && pos.y < state.world[0].length - 1;
	}
	
	//Returns a route to start
	public ArrayList<Pos> getRouteToStart(Pos pos, Pos startPos){
		ArrayList<Pos> path = new ArrayList<Pos>();
		while(pos != startPos){
			path.add(0, pos);
			pos = pos.parent;
		}
		return path;
	}
	
	//return true if pos is in queue
	public Boolean inQueue(Pos pos, ArrayList<Pos> queue){
		for (Pos queuePos: queue) {
			if (pos.x == queuePos.x && pos.y == queuePos.y) {
				return true;
			}
		}
		return false;
	}

	//Returns true if pos is in visited
	public Boolean visitedPos(Pos pos, ArrayList<Pos> visited){
		for (Pos posVisited : visited){
			if (pos.x == posVisited.x && pos.y == posVisited.y)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Action execute(Percept percept) {
		

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions>0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions==0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only moving forward.
		// START HERE - code below should be modified!

		


		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);
		


		iterationCounter--;

		if (iterationCounter==0)
			return NoOpAction.NO_OP;

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean)p.getAttribute("bump");
		Boolean dirt = (Boolean)p.getAttribute("dirt");
		Boolean home = (Boolean)p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition(p);
		Pos currentPosition = new Pos(state.agent_x_position,state.agent_y_position);
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
		else if (home)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.HOME);
		else
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

		state.printWorldDebug();

		
		// Next action selection based on the percept value
		if (dirt)
		{
			System.out.println("DIRT -> choosing SUCK action!");
			state.agent_last_action=state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		} 
		else
		{
			//Checks if route is empty
			if(state.route.isEmpty()) {
				//If empty, try to find a new route
				state.route = BFS(new Pos(state.agent_x_position,state.agent_y_position));
				
				//If still empty, there are more new unexplored. So return home
				if(state.route.isEmpty()) {
					System.out.println("Done!");
					if(state.world[currentPosition.x][currentPosition.y] == state.HOME) {
						System.out.println("Im kinda done here!!!");
						return NoOpAction.NO_OP;
					}
					else {
						//If the agent is not in its home position set "go_home" to true
						// and calculate the path back to the home position
						System.out.println("Going hooome!!!");
						state.finished = true;
						state.route = BFS(new Pos(state.agent_x_position,state.agent_y_position));;
					}

				}
			}
		
			//If there is a route
			//Get the next tile
			Pos nextStep = state.route.get(0);
			
			//Find the offset from current tile to next tile
			Pos dir = currentPosition.offset(nextStep);
			//Pos dir = currentPosition.offset(nextStep);
			System.out.println("CurrentPosition:"  + currentPosition);
			System.out.println("Direction:"  + dir);
			System.out.println("NextPos:" + nextStep);
			System.out.println("Path:" + state.route);
			
			//Depending on the offset, we go up/down/right/left
			//Rotate right until we are pointed towards the next tile. Then go forward
			if(dir.y==-1) {
				//North
				if(state.agent_direction==state.NORTH) {
					state.route.remove(0);
					return goForward();
				}else {
					return turnRight();
				}
			}else if(dir.x==1) {
				//East
				if(state.agent_direction==state.EAST) {
					state.route.remove(0);
					return goForward();
				}else {
					return turnRight();
				}
			}else if(dir.y==1) {
				//South
				if(state.agent_direction==state.SOUTH) {
					state.route.remove(0);
					return goForward();
				}else {
					return turnRight();
				}
			}else if(dir.x==-1) {
				//West
				if(state.agent_direction==state.WEST) {
					state.route.remove(0);
					return goForward();
				}else {
					return turnRight();
				}
			}else {
				//We should never end up here
				System.out.println("I dont know how to help you man.....");

			}


		}
		//We should never end up here
		return goForward();
	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}