package DataStructures;

import java.util.ArrayList;

public class Heap<E extends Comparable<E>> {

    private ArrayList<E> heap = new ArrayList<>();

    /**
     * Add the given object to the heap.
     * @param object The object to add to the heap.
     */
    public void add(E object) {
        heap.add(object);
        bubbleUp(heap.size() - 1);
    }

    public void remove(E object) {
        for (int i=0; i<heap.size(); i++) {
            if (heap.get(i) == object) {
                swap(i, heap.size() - 1 );
                heap.remove(heap.size() - 1);
                bubbleDown(i);
            }
        }
    }

    public E removeMin() {
        swap(0, heap.size() - 1);
        E item = heap.remove(heap.size() - 1);
        bubbleDown(0);
        return item;
    }

    public E peek() {
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.size() == 0;
    }

    public int size() {
        return heap.size();
    }

    // --------------------------------------------------------------------------------------------------------

    private void markAsRemvoed() {}

    /**
     * Bubble up the element at the given index so that the heap maintains its order property.
     * @param index The index of the element to bubble up.
     */
    private void bubbleUp(int index) {
        int parentIndex = parent(index);
        if (parentIndex >= 0 && heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
            swap(index, parentIndex);
            bubbleUp(parentIndex);
        }
    }

    private void bubbleDown(int index) {
        if (hasLeftChild(index)) {
            int leftChildIndex = leftChild(index);
            int smallestChild = leftChildIndex;

            if (hasRightChild(index)) {
                int rightChildIndex = rightChild(index);
                if (heap.get(leftChildIndex).compareTo(heap.get(rightChildIndex)) > 0) {
                    smallestChild = rightChildIndex;
                }
            }

            if (heap.get(smallestChild).compareTo(heap.get(index)) < 0) {
                swap(index, smallestChild);
                bubbleDown(smallestChild);
            }
        }
    }

    private boolean hasLeftChild(int index) {
        return leftChild(index) < heap.size();
    }

    private boolean hasRightChild(int index) {
        return rightChild(index) < heap.size();
    }

    private int parent(int index) {
        return Math.floorDiv(index - 1, 2);
    }

    /**
     * Return the left child's index of the given node's index.
     * @param index the node to find the left child of.
     * @return The index of the left child of the given node index.
     */
    private int leftChild(int index) {
        return (index*2) + 1;
    }

    /**
     * Return the right child's index of the given node's index.
     * @param index the node to find the right child of.
     * @return The index of the right child of the given node index.
     */
    private int rightChild(int index) {
        return (index*2) + 2;
    }

    /**
     * Swap elementd at two indexes in the array.
     * @param a An index in the array
     * @param b A different index in the array
     */
    private void swap(int a, int b) {
        E x = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, x);
    }
}
