package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;

public class Move implements StripsAction {
	Position destination;
	WorkerWrapper worker;
	
	public Move(Position destination, WorkerWrapper worker) {
		this.destination = destination;
		this.worker = worker;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if(!destination.inBounds(state.xExtent, state.yExtent)) {
			return false;
		}
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		// Creates a immutable copy of the state
		GameState newState = new GameState(state);
		WorkerWrapper movingWorker = newState.workers.get(worker.id);
		movingWorker.position = destination;
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		return Action.createCompoundMove(worker.id, destination.x, destination.y);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Move " + "worker-" + worker.id + " to " + destination.toString());
		return sb.toString();
	}
}
