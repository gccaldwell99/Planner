package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.GameState.WorkerWrapper;
import edu.cwru.sepia.agent.planner.Position;

public class Move implements StripsAction {
	private Position destination;
	private int k;
	private List<WorkerWrapper> kWorkers;
	
	public Move(int k, HashMap<Integer, WorkerWrapper> workers, Position destination) {
		this.k = k;
		this.kWorkers = getKWorkers(workers);
		this.destination = destination;
	}
	
	private List<WorkerWrapper> getKWorkers(HashMap<Integer, WorkerWrapper> workers) {
		List<WorkerWrapper> kWorkers = new ArrayList<WorkerWrapper>();
		int count = 0;
		for (WorkerWrapper worker : workers.values()) {
			if (worker.position != this.destination) {
				count++;
				kWorkers.add(worker);	
			}
			if (count > this.k) 
				break;
		}
		return kWorkers;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		if(!destination.inBounds(state.xExtent, state.yExtent))
			return false;
		if (this.kWorkers.size() < k)
			return false;
		return true;
	}

	@Override
	public GameState apply(GameState state) {
		// Creates a immutable copy of the state
		GameState newState = new GameState(state);
		
		for (WorkerWrapper worker : this.kWorkers) {
			WorkerWrapper movingWorker = newState.workers.get(worker.id);
			movingWorker.position = destination;
		}
		
		newState.actions.push(this);
		return newState;
	}
	
	@Override
	public Action getSepiaAction() {
		if (this.kWorkers.size() > this.k) {
			System.out.println("Attempt to get action before strip applied");
			return null;
		}
		
		// find one of the workers next to it
		WorkerWrapper selectedWorker = this.kWorkers.get(0);
		// remove it from being next to it
		// to prevent us from getting the same action on a later request
		this.kWorkers.remove(selectedWorker);
		
		return Action.createCompoundMove(selectedWorker.id, destination.x, destination.y);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Move ");
		for (WorkerWrapper worker : this.kWorkers) {
			sb.append(worker.id+ " and ");
		}
		sb.append(" to "+destination.toString());
		
		return sb.toString();
	}
}
