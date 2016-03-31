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
	ResourceNodeWrapper resourceNode;
	WorkerWrapper worker;
	
	public Harvest(ResourceNodeWrapper resourceNode, WorkerWrapper worker) {
		this.resourceNode = resourceNode;
		this.worker = worker;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(worker.hasLoad)
			return false;
		
		/* 		***Use if we want to force actions only to be valid if adjacent***
		boolean harvestLocationValid = false;
		for(Position p : worker.position.getAdjacentPositions()) {
			if(p.equals(resourceNode.position)) {
				harvestLocationValid = true;
			}
		}
		
		if(!harvestLocationValid) {
			return false;
		}
		*/
		
		return false;
	}

	/**
	 * This is currently horrible abuse of mutability. 
	 */
	@Override
	public GameState apply(GameState state) {
		worker.hasLoad = true;
		if(resourceNode.type.equals(ResourceNode.Type.GOLD_MINE)) {
			worker.loadType = ResourceType.GOLD;
		} else if(resourceNode.type.equals(ResourceNode.Type.TREE)) {
			worker.loadType = ResourceType.WOOD;
		}
		
		return state;
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
