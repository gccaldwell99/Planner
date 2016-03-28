package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class Harvest implements StripsAction {

	@Override
	public boolean preconditionsMet(GameState state) {
		// Create resource type enum
		// Next to resource location corresponding to that type
		// Not carrying anything
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
