package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Deposit implements StripsAction {
	WorkerWrapper worker;
	ResourceType resourceType;
	
	public Deposit(WorkerWrapper worker, ResourceType type) {
		this.resourceType = type;
		this.worker = worker;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(!worker.hasLoad)
			return false;
		
		if(!worker.loadType.equals(resourceType))
			return false;
					
		// Find if the townhall is next to you
		for(Position p : worker.position.getAdjacentPositions()) {
			if(state.townhallLocation.equals(p)) {
				return true;
			}
		}
		
		// If you aren't next to the townhall you can't deposit
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		worker.hasLoad = false;
		worker.loadType = null;
		if(resourceType.equals(ResourceType.GOLD)) {
			state.obtainedGold+=100;
		} else if(resourceType.equals(ResourceType.WOOD)) {
			state.obtainedWood+=100;
		}
		return state;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Deposit " + resourceType.toString() + " with worker-" + worker.id);
		return sb.toString();
	}

}
