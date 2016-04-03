package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Deposit implements StripsAction {
	private List<WorkerWrapper> validWorkers;
	private int townhallID, k;
	private Position townhallLocation;
	
	public Deposit(int k, HashMap<Integer, WorkerWrapper> workers, int townhallID, Position townhallLocation) {
		this.validWorkers = getValidWorkers(workers);
		this.townhallID = townhallID;
		this.townhallLocation = townhallLocation;
		this.k = k;
	}

	@Override
	public boolean preconditionsMet(GameState state) {		
		// if there are enough workers next to the town hall to
		// execute k deposits then return true
		return this.validWorkers.size() >= this.k;
	}

	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		
		// shorten the valid workers to the size of k
		this.validWorkers = new ArrayList<WorkerWrapper>(this.validWorkers.subList(0, k));
		
		for (WorkerWrapper worker : this.validWorkers) {
			WorkerWrapper harvestingWorker = newState.workers.get(worker.id);	
			
			harvestingWorker.hasLoad = false;
			harvestingWorker.loadType = null;
			
			if(harvestingWorker.loadType.equals(ResourceType.GOLD)) {
				newState.obtainedGold+=100;
			} else if(harvestingWorker.loadType.equals(ResourceType.WOOD)) {
				newState.obtainedWood+=100;
			}
		}
		
		newState.actions.push(this);
		return newState;
	}
	
	private List<WorkerWrapper> getValidWorkers(HashMap<Integer, WorkerWrapper> workers) {
		List<WorkerWrapper> validNeighbors = new ArrayList<WorkerWrapper>();
		for (WorkerWrapper worker : workers.values()) {
			// if any of the workers don't have a load then you shouldn't use them
			if (!worker.hasLoad)
				continue;
			// Check to see if all workers are next to the town hall your trying to deposit to
			if (worker.position.equals(this.townhallLocation))
				validNeighbors.add(worker);
		}
		return validNeighbors;
	}
	
	@Override
	public Action getSepiaAction() {
		if (this.validWorkers.size() > this.k) {
			System.out.println("Attempt to get action before strip applied");
			return null;
		}
		
		// find one of the workers next to it
		WorkerWrapper selectedWorker = this.validWorkers.get(0);
		// remove it from being next to it
		// to prevent us from getting the same action on a later request
		this.validWorkers.remove(selectedWorker);
		
		return Action.createCompoundDeposit(selectedWorker.id, townhallID);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Deposit ");
		for (WorkerWrapper worker : this.validWorkers) {
			sb.append(worker.loadType.toString()+" with "+worker.toString()+ " and ");
		}
		String toReturn = sb.toString();
		int andBegin = toReturn.length() - 5;
		return toReturn.substring(0, andBegin);
	}

}
