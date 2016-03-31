package edu.cwru.sepia.agent.planner.actions;

import java.util.HashSet;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.ResourceNodeWrapper;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.util.Direction;

public class Harvest implements StripsAction {
	Direction harvestDirection;
	ResourceType resourceType;
	WorkerWrapper worker;
	
	public Harvest(ResourceType type, Direction harvestDirection, WorkerWrapper worker) {
		type = resourceType;
		this.harvestDirection = harvestDirection;
		this.worker = worker;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		if(worker.hasLoad)
			return false;
		
		Position harvestLocation = worker.position.move(harvestDirection);
		
		// Ensure that the worker is next to the harvest location
		boolean harvestLocationValid = false;
		for(Position p : worker.position.getAdjacentPositions()) {
			if(p.equals(harvestLocation)) {
				harvestLocationValid = true;
			}
		}
		if(!harvestLocationValid) {
			return false;
		}
		
		// Ensure there is a resource node at the harvest location
		if(resourceType==ResourceType.GOLD) {
			for(ResourceNodeWrapper mine : state.mines) {
				if(mine.position.equals(harvestLocation) && mine.remainingResources>0) {
					return true;
				}
			}
		} else if(resourceType==ResourceType.WOOD) {
			for(ResourceNodeWrapper tree : state.trees) {
				if(tree.position.equals(harvestLocation) && tree.remainingResources>0) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * This is currently horrible abuse of mutability. 
	 */
	@Override
	public GameState apply(GameState state) {
		worker.hasLoad = true;
		worker.loadType = resourceType;
		return state;
	}
	
	@Override
	public Action getSepiaAction() {
		return Action.createPrimitiveGather(worker.id, harvestDirection);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Harvest " + resourceType.toString() + " with worker-" + worker.id);
		return sb.toString();
	}
}
