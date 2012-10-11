package edu.aust.cse.collections;

import java.util.ArrayList;

public class DFANode {
	public int State;
	public Set<Integer> Poses = new Set<Integer>();
	public ArrayList<Pair<String, Integer>> edges = new ArrayList<Pair<String, Integer>>();
	// public ArrayList<Pair<String, Set<Integer>>> edges = new
	// ArrayList<Pair<String, Set<Integer>>>();
}
