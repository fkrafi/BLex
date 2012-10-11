package edu.aust.cse.collections;

public class ParseTreeNode {
	public String val;
	public int pos, index, left, right;
	public boolean nullable;
	public Set<Integer> firstpos = new Set<Integer>();
	public Set<Integer> lastpos = new Set<Integer>();
}
