package edu.aust.cse.collections;

import java.util.ArrayList;

public class Set<T> {
	public ArrayList<T> S = new ArrayList<T>();;

	public void add(T e) {
		if (S.contains(e) == false)
			S.add(e);
	}

	public T get(int i) {
		if (i >= S.size())
			return null;
		return S.get(i);
	}

	public boolean isEmpty() {
		return S.isEmpty();
	}

	public int size() {
		return S.size();
	}

	public void clear() {
		S.clear();
	}
}
