package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.util.Direction;

public class Deposit implements StripsAction {
	WorkerWrapper worker;
	ResourceType resourceType;
	int townhallID;
	
	public Deposit(WorkerWrapper worker, ResourceType type, int townhallID) {
		this.resourceType = type;
		this.worker = worker;
		this.townhallID = townhallID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(!worker.hasLoad)
			return false;
		
		if(!worker.loadType.equals(resourceType))
			return false;
		
		// If you aren't depositing to the townhall then you can't deposit
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		WorkerWrapper harvestingWorker = newState.workers.get(worker.id);
		
		harvestingWorker.hasLoad = false;
		harvestingWorker.loadType = null;
		if(resourceType.equals(ResourceType.GOLD)) {
			newState.obtainedGold+=100;
		} else if(resourceType.equals(ResourceType.WOOD)) {
			newState.obtainedWood+=100;
		}
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		return Action.createCompoundDeposit(worker.id, townhallID);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Deposit " + resourceType.toString() + " with " + worker.toString());
		return sb.toString();
	}

}
