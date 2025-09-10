package com.example.aslib.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class BoundedList<T> {
    private final int maxSize;
    private final Deque<T> deque;

    public BoundedList(int maxSize) {
        this.maxSize = maxSize;
        this.deque = new LinkedList<>();
    }

    public void add(T element) {
        if (deque.size() >= maxSize) {
            // 移除最先存储的元素
            deque.removeFirst();
        }
        deque.addLast(element);
    }

    public T get(int index) {
        if (index < 0 || index >= deque.size()) {
            throw new IndexOutOfBoundsException("索引超出范围");
        }
        return (T) deque.toArray()[index];
    }

    public int size() {
        return deque.size();
    }

    public void clear() {
        deque.clear();
    }
    public List<T> toList(){
        T[] array = (T[]) deque.toArray();
        return Arrays.asList(array);
    }

    @Override
    public String toString() {
        return deque.toString();
    }

}