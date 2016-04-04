package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.ResourceNodeWrapper;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Harvest implements StripsAction {
	private ResourceNodeWrapper resourceNode;
	private int k;
	private List<WorkerWrapper> validWorkers;
	
	public Harvest(int k, HashMap<Integer, WorkerWrapper> workers, ResourceNodeWrapper resourceNode) {
		this.resourceNode = resourceNode;
		this.k = k;
		this.validWorkers = getValidWorkers(workers);
	}
	
	public int getNumActions() {
		return this.k;
	}
	
	public List<Integer> getActorIds() {
		List<Integer> ids = new ArrayList<Integer>();
		for (WorkerWrapper worker : this.validWorkers) {
			ids.add(worker.id);
		}
		ids.add(this.resourceNode.id);
		return ids;
	}
	
	private List<WorkerWrapper> getValidWorkers(HashMap<Integer, WorkerWrapper> workers) {
		List<WorkerWrapper> validNeighbors = new ArrayList<WorkerWrapper>();
		for (WorkerWrapper worker : workers.values()) {
			// if any of the workers have a load then you shouldn't use them
			if (worker.hasLoad)
				continue;
			// Check to see if all workers are next to the resource node you're trying to harvest
			if (worker.position.equals(this.resourceNode.position))
				validNeighbors.add(worker);
		}
		return validNeighbors;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(resourceNode.remainingResources< this.k*100)
			return false;
		
		int requiredAmount = -1;
		int obtainedAmount = 0;
		int potentialAmount = 0;
		if(resourceNode.type.equals(ResourceNode.Type.GOLD_MINE)) {
			requiredAmount = state.requiredGold;
			obtainedAmount = state.obtainedGold;
			potentialAmount = getPotentialAmount(state.workers.values(), ResourceType.GOLD);
		} else if(resourceNode.type.equals(ResourceNode.Type.TREE)) {
			requiredAmount = state.requiredWood;
			obtainedAmount = state.obtainedWood;
			potentialAmount = getPotentialAmount(state.workers.values(), ResourceType.WOOD);
		}
		// if harvesting this exceeds the required amount don't do it
		if (this.k*100 + obtainedAmount + potentialAmount  > requiredAmount) {
			return false;
		}
				
		return this.validWorkers.size() >= this.k;	
	}
	
	private int getPotentialAmount(Collection<WorkerWrapper> workers, ResourceType type) {
		int potentialAmount = 0;
		for (WorkerWrapper worker : workers) {
			if (worker.hasLoad && worker.loadType.equals(type)) {
				potentialAmount += 100;
			}
		}
		return potentialAmount;
	}

	/**
	 * This is currently horrible abuse of mutability. 
	 */
	@Override
	// TODO: harvest the gold
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		// shorten the valid workers to the size of k
		this.validWorkers = new ArrayList<WorkerWrapper>(this.validWorkers.subList(0, k));
		
		for (WorkerWrapper worker : this.validWorkers) {
			WorkerWrapper harvestingWorker = newState.workers.get(worker.id);
			harvestingWorker.hasLoad = true;
			if(resourceNode.type.equals(ResourceNode.Type.GOLD_MINE)) {
				harvestingWorker.loadType = ResourceType.GOLD;
			} else if(resourceNode.type.equals(ResourceNode.Type.TREE)) {
				harvestingWorker.loadType = ResourceType.WOOD;
			}
			// Remove the resources from the new state
			newState.resources.get(resourceNode.id).removeResources(100);
		}
		// If resource is now empty, take it out of the priority queue
		if(newState.closestTree.peek().remainingResources<=0) {
			newState.closestTree.remove();
		}
		if(newState.closestGoldMine.peek().remainingResources<=0) {
			newState.closestGoldMine.remove();
		}
		
		newState.actions.push(this);
		
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		if (this.validWorkers.size() > this.k) {
			System.out.println("Attempt to get action before strip applied");
			return null;
		}
		
		// find one of the workers next to it
		WorkerWrapper selectedWorker = this.validWorkers.get(0);
		// remove it from being valid
		// to prevent us from getting the same action on a later request
		this.validWorkers.remove(selectedWorker);
		
		return Action.createCompoundGather(selectedWorker.id, resourceNode.id);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Harvest from " + resourceNode.toString() + " with ");
		for (WorkerWrapper worker : this.validWorkers) {
			sb.append(worker.toString()+ " and ");
		}
		String toReturn = sb.toString();
		int andBegin = toReturn.length() - 5;
		return toReturn.substring(0, andBegin);
	}
}
