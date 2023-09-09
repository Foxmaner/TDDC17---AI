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
		return new Pos(this.x-sentPos.x,this.y-sentPos.y);
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
	ArrayList<Pos> route = new ArrayList<Pos>();
	ArrayList<Pos> queued;
	ArrayList<Pos> visited;

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

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 1000;
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
		//East
		neighbors[1]=(new Pos(pos.x+1,pos.y));
		//South
		neighbors[2]=(new Pos(pos.x,pos.y+1));
		//West
		neighbors[3]=(new Pos(pos.x-1,pos.y));

		return neighbors;

	}

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

	public void generateRoute() {
		Pos position = new Pos(state.agent_x_position,state.agent_y_position);

		Pos[] neighbors = getNeighborsAbs(position);

		state.queued = new ArrayList<Pos>();
		state.visited = new ArrayList<Pos>();

		state.visited.add(position);

		for(int i=0; i<=3; i++) {
			neighbors[i].parent = position;

			if (!(state.world[neighbors[i].x][neighbors[i].y] == state.WALL)) {
				state.queued.add(neighbors[i]);
			}
		}

		state.route = BFS(position);

	}


	public ArrayList BFS(Pos start_node) {
		int search_state;
		if (state.finished) {
			search_state = state.HOME;
		}
		else {
			search_state = state.UNKNOWN;
		}

		if (state.queued.isEmpty()) {
			ArrayList<Pos> empty_path = new ArrayList<Pos>();
			return empty_path;
		}

		Pos node = state.queued.get(0);
		state.queued.remove(0);


		if (state.world[node.x][node.y] == search_state) {
			return getRouteToStart(node, start_node);
		}

		else if (state.world[node.x][node.y] == state.WALL) {
			if (!visitedPos(node, state.visited)) {
				state.visited.add(node);
			}
			return BFS(start_node);
		}

		else {
			if(!visitedPos(node, state.visited)) {
				state.visited.add(node);
			}
			Pos[] neighbours = new Pos[4];
			neighbours = getNeighborsAbs(node);
			for (Pos neighbour : neighbours) {
				if (!(state.world[neighbour.x][neighbour.y] == state.WALL) &&
						!visitedPos(neighbour, state.visited) && !inQueue(neighbour, state.queued)) {
					neighbour.parent = node;
					state.queued.add(neighbour);
				}
			}
			return BFS(start_node);
		}
	}

	public ArrayList<Pos> getRouteToStart(Pos pos, Pos startPos){
		ArrayList<Pos> path = new ArrayList<Pos>();
		while(pos != startPos){
			path.add(0, pos);
			pos = pos.parent;
		}
		return path;
	}

	public Boolean inQueue(Pos pos, ArrayList<Pos> queue){
		for (Pos queuePos: queue) {
			if (pos.x == queuePos.x && pos.y == queuePos.y) {
				return true;
			}
		}
		return false;
	}


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
		System.out.println(state.route.toString());

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

		Pos currentPosition = new Pos(state.agent_x_position,state.agent_y_position);


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
		state.updatePosition((DynamicPercept)percept);
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
			if(state.route.isEmpty()) {
				generateRoute();

				if(state.route.isEmpty()) {
					System.out.println("Done!");
					if(state.world[currentPosition.x][currentPosition.y] == state.HOME) {
						return NoOpAction.NO_OP;
					}
					else {
						//If the agent is not in its home position set "go_home" to true
						// and calculate the path back to the home position
						state.finished = true;
						generateRoute();
					}

				}
			}else {
				Pos nextStep = state.route.get(0);

				Pos dir = nextStep.offset(currentPosition);
				System.out.println("Hello"  + dir);
				System.out.println("nextpos" + nextStep);
				System.out.println("Path" + state.route);
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
					System.out.println("I dont know how to help you man.....");

				}


			}
		}
		return goForward();
	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
