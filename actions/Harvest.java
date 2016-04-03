package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.ResourceNodeWrapper;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Harvest implements StripsAction {
	private ResourceNodeWrapper resourceNode;
	private WorkerWrapper worker;
	
	public Harvest(ResourceNodeWrapper resourceNode, WorkerWrapper worker) {
		this.resourceNode = resourceNode;
		this.worker = worker;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(worker.hasLoad)
			return false;
		
		if(resourceNode.remainingResources<=0)
			return false;
		
		// Check if your next to the resource your trying to harvest
		return worker.position.equals(resourceNode.position);
		
	}

	/**
	 * This is currently horrible abuse of mutability. 
	 */
	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state);
		WorkerWrapper harvestingWorker = newState.workers.get(worker.id);
		harvestingWorker.hasLoad = true;
		if(resourceNode.type.equals(ResourceNode.Type.GOLD_MINE)) {
			harvestingWorker.loadType = ResourceType.GOLD;
		} else if(resourceNode.type.equals(ResourceNode.Type.TREE)) {
			harvestingWorker.loadType = ResourceType.WOOD;
		}
	
		// Remove the resources from the new state, then take it out of the priority queue if it is empty
		newState.resources.get(resourceNode.id).removeResources(100);
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
		return Action.createCompoundGather(worker.id, resourceNode.id);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Harvest from " + resourceNode.toString() + " with " + worker.toString());
		return sb.toString();
	}
}
