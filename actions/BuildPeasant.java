package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;

public class BuildPeasant implements StripsAction {
	// TODO: Maybe we shouldn't hard-code the id for a peasant.
	int TEMPLATE_ID = 26;
	int townhallID;
	
	public BuildPeasant(int townhallID) {
		this.townhallID = townhallID;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if(state.foodAmount <= 0 || state.obtainedGold < 400) {
			return false;
		}
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		// Creates a immutable copy of the state
		GameState newState = new GameState(state);
		
		newState.obtainedGold-=400;
		newState.foodAmount-=1;
		newState.highestID+=1;	
		
		// make peasant representation in state
		Position townhallLocation = newState.townhallLocation;
		int newID = newState.highestID;
		WorkerWrapper newWorker = newState.new WorkerWrapper(townhallLocation, newID);
		
		newState.workers.put(newWorker.id, newWorker);
		
		newState.actions.push(this);
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		return Action.createPrimitiveProduction(this.townhallID, this.TEMPLATE_ID);
	}
	
	@Override
	public double cost() {
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create new worker");
		return sb.toString();
	}
}
