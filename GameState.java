package edu.cwru.sepia.agent.planner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	public HashMap<Integer, WorkerWrapper> workers;
	public HashMap<Integer, ResourceNodeWrapper> resources;
	public PriorityQueue<ResourceNodeWrapper> closestTree;
	public PriorityQueue<ResourceNodeWrapper> closestGoldMine;
	public Stack<StripsAction> actions;
	public int xExtent;
	public int yExtent;
	public int requiredGold;
	public int requiredWood;
	public int obtainedGold;
	public int obtainedWood;
	public int foodAmount;
	public Position townhallLocation;
	public int townhallID;
	
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    	xExtent = state.getXExtent();
    	yExtent = state.getYExtent();
    	
    	workers = new HashMap<Integer,WorkerWrapper>();
    	resources = new HashMap<Integer, ResourceNodeWrapper>();
    	actions = new Stack<StripsAction>();
    	closestGoldMine = new PriorityQueue<ResourceNodeWrapper>();
    	closestTree = new PriorityQueue<ResourceNodeWrapper>();
    	
    	
        for(UnitView unit : state.getUnits(playernum)) {
        	if(unit.getTemplateView().getName().equals("Peasant"))
        		workers.put(unit.getID(), new WorkerWrapper(unit));
        	if(unit.getTemplateView().getName().equals("TownHall")) {
        		townhallLocation = new Position(unit.getXPosition(), unit.getYPosition());
        		townhallID = unit.getID();
        	}
        }
        
        for(ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
        	resources.put(resource.getID(), new ResourceNodeWrapper(resource));
        	
        	if(resource.getType().equals(ResourceNode.Type.GOLD_MINE)) {
        		closestGoldMine.add(new ResourceNodeWrapper(resource));
        	} else if(resource.getType().equals(ResourceNode.Type.TREE)) {
        		closestTree.add(new ResourceNodeWrapper(resource));
        	}
        }
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        
        obtainedGold = state.getResourceAmount(playernum, ResourceType.GOLD);
        obtainedWood = state.getResourceAmount(playernum, ResourceType.WOOD);
        foodAmount = state.getSupplyAmount(townhallID);
    }
    
    /**
     * Makes an immutable copy of this game state
     */
    public GameState(GameState stateToCopy) {
    	xExtent = stateToCopy.xExtent;
    	yExtent = stateToCopy.yExtent;
    	
    	// Must go before resource queues
        townhallLocation = stateToCopy.townhallLocation;
        townhallID = stateToCopy.townhallID;
    	
    	workers = new HashMap<Integer,WorkerWrapper>();
    	resources = new HashMap<Integer, ResourceNodeWrapper>();
    	actions = new Stack<StripsAction>();
    	closestGoldMine = new PriorityQueue<ResourceNodeWrapper>();
    	closestTree = new PriorityQueue<ResourceNodeWrapper>();
    	
    	for(WorkerWrapper worker : stateToCopy.workers.values()) {
        	workers.put(worker.id, new WorkerWrapper(worker));
        }
        for(ResourceNodeWrapper resource : stateToCopy.resources.values()) {
        	ResourceNodeWrapper resourceCopy = new ResourceNodeWrapper(resource);
        	resources.put(resource.id, resourceCopy);
        	if(resourceCopy.type.equals(ResourceNode.Type.GOLD_MINE) && resourceCopy.remainingResources>0) {
        		closestGoldMine.add(resourceCopy);
        	} else if (resourceCopy.type.equals(ResourceNode.Type.TREE) && resourceCopy.remainingResources>0) {
        		closestTree.add(resourceCopy);
        	}
        }
        this.requiredGold = stateToCopy.requiredGold;
        this.requiredWood = stateToCopy.requiredWood;
        
        obtainedGold = stateToCopy.obtainedGold;
        obtainedWood = stateToCopy.obtainedWood;
        foodAmount = stateToCopy.foodAmount;
        
        for(StripsAction action : stateToCopy.actions) {
        	actions.push(action);
        }
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        if(requiredGold <= obtainedGold && requiredWood <= obtainedWood) {
        	return true;
        } else {
        	return false;
        }
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
    	List<GameState> children = new LinkedList<GameState>();
    	
    	for(WorkerWrapper worker : workers.values()) {
    		Move moveToMine = new Move(closestGoldMine.peek().position, worker);
    		if(moveToMine.preconditionsMet(this))
				children.add(moveToMine.apply(this));
    		
    		Move moveToTree = new Move(closestTree.peek().position, worker);
    		if(moveToTree.preconditionsMet(this))
				children.add(moveToTree.apply(this));

    		Move moveToTownHall = new Move(townhallLocation, worker);
    		if(moveToTownHall.preconditionsMet(this))
				children.add(moveToTownHall.apply(this));
    		
    		// Have to deal with multiple workers harvesting from an almost empty resource
    		Harvest harvestGold = new Harvest(closestGoldMine.peek(), worker);
    		if(harvestGold.preconditionsMet(this))
    			children.add(harvestGold.apply(this));
    		
    		Harvest harvestWood = new Harvest(closestTree.peek(), worker);
    		if(harvestWood.preconditionsMet(this))
    			children.add(harvestWood.apply(this));
    		
    		Deposit deposit = new Deposit(worker, townhallID);
    		if(deposit.preconditionsMet(this))
    			children.add(deposit.apply(this));
    	}
    	
        return children;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
    	double closestTreeDistance = Double.MAX_VALUE;
    	double closestMineDistance = Double.MAX_VALUE;
    	
    	for(ResourceNodeWrapper resource : resources.values()) {
    		if(resource.type.equals(ResourceNode.Type.GOLD_MINE)) {
    			closestMineDistance = Math.min(closestMineDistance, resource.position.euclideanDistance(townhallLocation));
    		} else if(resource.type.equals(ResourceNode.Type.TREE)) {
    			closestTreeDistance = Math.min(closestTreeDistance, resource.position.euclideanDistance(townhallLocation));
    		}
    	}
    	
    	int woodNeeded = requiredWood-obtainedWood;
    	int goldNeeded = requiredGold-obtainedGold;
    	double heuristicCost = 2*closestTreeDistance*(woodNeeded/100)+2*closestMineDistance*(goldNeeded/100);
    	
    	// Reduced heuristic cost based on favorable worker positions
    	for(WorkerWrapper worker : workers.values()) {
    		heuristicCost-=calculateWorkerValue(worker, closestTreeDistance, closestMineDistance);
    	}
        
        return heuristicCost;
    }
    
    private double calculateWorkerValue(WorkerWrapper worker, double closestTreeDistance, double closestMineDistance) {
    	double workerValue = 0;
    	int woodNeeded = requiredWood-obtainedWood;
    	int goldNeeded = requiredGold-obtainedGold;
    	
    	if(worker.hasLoad) {
    		if(goldNeeded>0 && worker.loadType.equals(ResourceType.GOLD)) {
        		if(nextToTownhall(worker)) {
        			workerValue+=(closestMineDistance*2);
        		} else {
        			workerValue+=(closestMineDistance+1);
        		}
        	} else if(woodNeeded>0 && worker.loadType.equals(ResourceType.WOOD)) {
           		if(nextToTownhall(worker)) {
        			workerValue+=(closestTreeDistance*2);
        		} else {
        			workerValue+=(closestTreeDistance+1);
        		}
        	}
    	} else {
    		if(goldNeeded>0 && nextToResource(worker, ResourceNode.Type.GOLD_MINE)) {
    			workerValue+=closestMineDistance;
    		} else if(woodNeeded>0 && nextToResource(worker, ResourceNode.Type.TREE)) {
    			workerValue+=closestTreeDistance;
    		}
    	}
    	
    	return workerValue;
    }
    
    // Refactor if there is time, so it isn't a double loop
    private boolean nextToResource(WorkerWrapper worker, ResourceNode.Type resourceType) {
    	for(Position p : worker.position.getAdjacentPositions()) {
    		for(ResourceNodeWrapper resource : resources.values()) {
    			if(resource.type.equals(resourceType) && resource.position.equals(p)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private boolean nextToTownhall(WorkerWrapper worker) {
    	for(Position p : worker.position.getAdjacentPositions()) {
    		if(p.equals(townhallLocation)) 
    			return true;
    	}
    	return false;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return actions.size();
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param that The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState that) {
        if (this.getCost() > that.getCost()) return 1;
		if (this.getCost() < that.getCost()) return -1;
		return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
    	if(!(o instanceof GameState)) {
    		return false;
    	}
    	
        GameState otherState = (GameState) o;
        return otherState.obtainedGold == obtainedGold 
        		&& otherState.obtainedWood == obtainedWood 
        		&& otherState.foodAmount == foodAmount
        		&& otherState.workers.equals(workers)
        		&& otherState.resources.equals(resources);
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        int hash = obtainedGold*31 + obtainedWood;
        hash = hash*31+workers.hashCode();
        hash = hash*31+resources.hashCode();
        hash = hash*31+foodAmount;
        return hash;
    }
    
    @Override
    public String toString() {
    	return actions.toString();
    }
    
    public WorkerWrapper getWorker() {
    	return workers.values().iterator().next();
    }
    
    
    public class WorkerWrapper {
    	public Position position;
    	public boolean hasLoad;
    	public ResourceType loadType;
    	public int id;
    	
    	public WorkerWrapper(UnitView unit) {
    		position = new Position(unit.getXPosition(), unit.getYPosition());
    		hasLoad = unit.getCargoAmount() != 0;
    		loadType = unit.getCargoType();
    		id = unit.getID();
    	}
    	
    	/**
    	 * Acts as a cloning constructor to create an immutable copy of the given worker
    	 * @param workerToClone
    	 */
    	public WorkerWrapper(WorkerWrapper workerToClone) {
    		position = workerToClone.position.clone();
    		hasLoad = workerToClone.hasLoad;
    		loadType = workerToClone.loadType;
    		id = workerToClone.id;
    	}
    	
    	@Override
    	public String toString() {
    		return "worker-" + id;
    	}
    	
        @Override
        public boolean equals(Object o) {
        	if(!(o instanceof WorkerWrapper)) {
        		return false;
        	}
        	
            WorkerWrapper otherWorker = (WorkerWrapper) o;
            if(otherWorker.position.equals(position) && otherWorker.hasLoad == hasLoad) {
            	return true;
            } else {
            	return false;
            }
        }
    	
        @Override
        public int hashCode() {
        	return position.hashCode() + (hasLoad ? 1231 : 1237);
        }
    }
    
    public class ResourceNodeWrapper implements Comparable<ResourceNodeWrapper> {
    	public Position position;
    	public ResourceNode.Type type;
    	public int remainingResources;
    	public int id;
    	
    	public ResourceNodeWrapper(ResourceNode.ResourceView resource) {
    		position = new Position(resource.getXPosition(), resource.getYPosition());
    		type = resource.getType();
    		remainingResources = resource.getAmountRemaining();
    		id = resource.getID();
    	}
    	
    	public ResourceNodeWrapper(ResourceNodeWrapper resourceToCopy) {
    		position = resourceToCopy.position.clone();
    		type = resourceToCopy.type;
    		remainingResources = resourceToCopy.remainingResources;
    		id = resourceToCopy.id;
    	}
    	
    	public void removeResources(int amount) {
    		remainingResources = remainingResources-amount;
    	}
    	
		@Override
		public int compareTo(ResourceNodeWrapper otherNode) {
			return (int) (position.euclideanDistance(townhallLocation)-otherNode.position.euclideanDistance(townhallLocation));
		}
    	
    	@Override
    	public String toString() {
    		return type.toString() + "-" + id;
    	}
    	
        @Override
        public boolean equals(Object o) {
        	if(!(o instanceof ResourceNodeWrapper)) {
        		return false;
        	}
        	
        	ResourceNodeWrapper otherResource = (ResourceNodeWrapper) o;
            if(otherResource.position.equals(position) && otherResource.type == type 
            		&& otherResource.remainingResources == remainingResources) {
            	return true;
            } else {
            	return false;
            }
        }
    	
        @Override
        public int hashCode() {
        	int hash = position.hashCode();
        	hash = hash + (type.equals(ResourceNode.Type.TREE) ? 1231 : 1237);
        	hash = hash*31+remainingResources;
            return hash;
        }
    }
}
