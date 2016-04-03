package edu.cwru.sepia.agent.planner.actions;

import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Deposit implements StripsAction {
	private List<WorkerWrapper> workers;
	private int townhallID;
	
	public Deposit(List<WorkerWrapper> workers, int townhallID) {
		this.workers = workers;
		this.townhallID = townhallID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		boolean nextTo = true;
		for (WorkerWrapper worker : workers) {
			// if any of the workers don't have a load then you shouldn't use this
			if (!worker.hasLoad)
				return false;
			// Check to see if all workers are next to the town hall your trying to deposit to
			nextTo = nextTo && worker.position.equals(state.townhallLocation);
		}
		
		return nextTo;
	}

	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		for (WorkerWrapper worker : this.workers) {
			WorkerWrapper harvestingWorker = newState.workers.get(worker.id);
			
			harvestingWorker.hasLoad = false;
			harvestingWorker.loadType = null;
			
			if(worker.loadType.equals(ResourceType.GOLD)) {
				newState.obtainedGold+=100;
			} else if(worker.loadType.equals(ResourceType.WOOD)) {
				newState.obtainedWood+=100;
			}
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
