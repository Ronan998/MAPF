package DataStructures;

import java.util.HashMap;
import java.util.Map;

public class PriorityQueue<E> {

    Heap<Entry> heap = new Heap<Entry>();
    Map<E, Entry> entries = new HashMap<>();

    public void put(E object, double priority) {
        Entry newEntry = new Entry(object, priority);
        entries.put(object, newEntry);
        heap.add(newEntry);
    }

    public void update(E object, double newPriority) {
        heap.remove(entries.get(object));
        entries.remove(object);
        put(object, newPriority);
    };

    public E get() {
        E object = heap.removeMin().getElement();
        entries.remove(object);
        return object;
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public boolean contains(E object) {
        return entries.containsKey(object);
    }

    private class Entry implements Comparable<Entry>{
        private E element;
        private double priority;

        private Entry(E element, double priority) {
            this.element = element;
            this.priority = priority;
        }

        public E getElement() {
            return this.element;
        }
        public double getPriority() {
            return this.priority;
        }

        public int compareTo(Entry o) {
            return (int)(this.priority - o.getPriority());
        }

    }

}
