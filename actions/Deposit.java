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
	Direction depositDirection;
	
	public Deposit(WorkerWrapper worker, ResourceType type, Direction depositDirection) {
		this.resourceType = type;
		this.worker = worker;
		this.depositDirection = depositDirection;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(!worker.hasLoad)
			return false;
		
		if(!worker.loadType.equals(resourceType))
			return false;
					
		// Find if you are depositing resources to the townhall
		Position depositPosition = worker.position.move(depositDirection);
		if(state.townhallLocation.equals(depositPosition)) {
			return true;
		}
		
		// If you aren't depositing to the townhall then you can't deposit
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
	public Action getSepiaAction() {
		return Action.createPrimitiveDeposit(worker.id, depositDirection);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Deposit " + resourceType.toString() + " with worker-" + worker.id);
		return sb.toString();
	}

}
