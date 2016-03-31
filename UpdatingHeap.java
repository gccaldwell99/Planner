package edu.cwru.sepia.agent.planner;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

// A data structure with similar properties to a Heap
// However if you add something that already exists, it keeps the one with the
// lower given value
public class UpdatingHeap<T extends Comparable<T>> {
  private TreeSet<OVPair> tree;
  // HashMap for quicker lookup
  private Map<T, Float> valueMap;
  private Map<T, Integer> orderMap;
  private Integer count;

  // Initializes a new empty UpdatingMapHeap
  public UpdatingHeap() {
    valueMap = new HashMap<T, Float>();
    tree = new TreeSet<OVPair>();
    orderMap = new HashMap<T, Integer>();
    count = 0;
  }

  /**
   * O(1) if there is no need to update
   * log(n) otherwise
   */
  public boolean updateIfLess(T toAdd, float value) {
    if(valueMap.containsKey(toAdd) && getCurrentValue(toAdd) <= value) {
      return false;
    }
    return add(toAdd, value);
  }

  /**
   * log(n) runtime
   * Adds the element, replaces the old value when applicable.
   */
  public boolean add(T toAdd, float value) {
    treeAdd(toAdd, value, count);
    // IMPORTANT adding to maps must come after adding to tree
    //           as adding to tree uses maps to get old values
    valueMap.put(toAdd, value);
    orderMap.put(toAdd, count);
    count++;
    return true;
  }

  /**
   * If the TreeSet previously contained the element, the old value is replaced.
   * O(log(n)) runtime. log(n) to remove and to add
   */
  private boolean treeAdd(T toAdd, float value, Integer order) {
    OVPair newPair = new OVPair(toAdd, value, order);
    if(valueMap.containsKey(toAdd)) {
      float oldValue = valueMap.get(toAdd);
      Integer oldOrder = orderMap.get(toAdd);
      OVPair oldPair = new OVPair(toAdd, oldValue, oldOrder);
      tree.remove(oldPair);
    }
    return tree.add(newPair);
  }

  // use of this method increases readability
  private float getCurrentValue(T key) {
    return valueMap.get(key);
  }

  // O(log(n))
  // removes and returns the minimum
  public T poll() {
    T toReturn = tree.pollFirst().object;
    valueMap.remove(toReturn);
    orderMap.remove(toReturn);
    return toReturn;
  }

  // O(1)
  // returns the minimum
  public T peek() {
    return tree.first().object;
  }

  // O(1)
  public boolean contains(T object) {
    return valueMap.containsKey(object);
  }

  public boolean isEmpty() {
    return valueMap.isEmpty() || tree.isEmpty();
  }

  public String toString() {
    return tree.toString()+" "+valueMap.toString();
  }

  // an object value pair used to allow us to search and
  // compare equality using the object but sort using the value
  // in the case of a value tie, uses order to break the tie
  private class OVPair implements Comparable<OVPair> {
    public T object;
    public float value;
    public Integer order;

    public OVPair(T object, float value, Integer order) {
      this.object = object;
      this.value = value;
      this.order = order;
    }

    @Override
    public boolean equals(Object o) {
      if(!(o instanceof UpdatingHeap.OVPair)) {
        return false;
      }

      OVPair other = (OVPair)o;
      return this.object.equals(other.object);
    }

    @Override
    public int hashCode() {
      return object.hashCode();
    }

    // The one that was added first is always -1
    // used for tie-breaking
    @Override
    public int compareTo(OVPair that) {
      if (this.value > that.value) return 1; // TODO: but what billy and I said
      if (this.value < that.value) return -1;
      // if they have the same value but they arent equal compare on order added
      if (!this.object.equals(that.object)) return this.order.compareTo(that.order);
      return 0;
    }

    @Override
    public String toString() {
      return object + "=" + value;
    }
  }
}
