package edu.cwru.sepia.agent.planner.actions;

import java.util.HashSet;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.ResourceNodeWrapper;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.util.Direction;

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
		
		// Check if your next to the resource your trying to harvest
		boolean harvestLocationValid = false;
		for(Position p : worker.position.getAdjacentPositions()) {
			if(p.equals(resourceNode.position)) {
				harvestLocationValid = true;
			}
		}
		
		return harvestLocationValid;
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
	
		// Remove the resources from the new state
		newState.resources.get(resourceNode.id).remainingResources-=100;
		
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
