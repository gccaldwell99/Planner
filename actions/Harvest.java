package edu.cwru.sepia.agent.planner.actions;

import java.util.HashSet;
import java.util.Set;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.ResourceNodeWrapper;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Harvest implements StripsAction {
	Position harvestLocation;
	ResourceType resourceType;
	
	public Harvest(ResourceType type, Position p) {
		type = resourceType;
		harvestLocation = p;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		WorkerWrapper worker = state.getWorker();
		if(worker.hasLoad)
			return false;
		
		if(resourceType==ResourceType.GOLD) {
			for(ResourceNodeWrapper mine : state.mines) {
				if(mine.position.equals(harvestLocation) && mine.remainingResources>0) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}
