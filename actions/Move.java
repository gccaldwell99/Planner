package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class Move implements StripsAction {
	Direction moveDirection;
	
	public Move(Direction moveDirection) {
		this.moveDirection = moveDirection;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		Position currentPosition = state.getWorker().position;
		Position destinationPosition = currentPosition.move(moveDirection); 
		if(!destinationPosition.inBounds(state.xExtent, state.yExtent)) {
			return false;
		}
		if(state.isOccupied(destinationPosition)) {
			return false;
		}
		return true;
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
