package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.util.Direction;

public class Deposit implements StripsAction {
	private WorkerWrapper worker;
	private int townhallID;
	
	public Deposit(WorkerWrapper worker, int townhallID) {
		this.worker = worker;
		this.townhallID = townhallID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(!worker.hasLoad)
			return false;
		
		// Check to see if your next to the townhall your trying to deposit to
		return worker.position.equals(state.townhallLocation);
	}

	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		WorkerWrapper harvestingWorker = newState.workers.get(worker.id);
		
		harvestingWorker.hasLoad = false;
		harvestingWorker.loadType = null;
		if(worker.loadType.equals(ResourceType.GOLD)) {
			newState.obtainedGold+=100;
		} else if(worker.loadType.equals(ResourceType.WOOD)) {
			newState.obtainedWood+=100;
		}
		
		newState.actions.push(this);
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		return Action.createCompoundDeposit(worker.id, townhallID);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Deposit " + worker.loadType.toString() + " with " + worker.toString());
		return sb.toString();
	}

}
