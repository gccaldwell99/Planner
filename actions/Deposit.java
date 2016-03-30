package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;

public class Deposit implements StripsAction {
	
	public Deposit() {
		
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		WorkerWrapper worker = state.getWorker();
		if(!worker.hasLoad)
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
