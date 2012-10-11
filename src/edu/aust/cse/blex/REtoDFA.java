package edu.aust.cse.blex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import edu.aust.cse.collections.DFANode;
import edu.aust.cse.collections.Pair;
import edu.aust.cse.collections.ParseTreeNode;
import edu.aust.cse.collections.Set;

public class REtoDFA {

	final int InitialState = 1;
	final int FinalState = 2;

	String post = "";

	ArrayList<ParseTreeNode> ParseTree;
	ArrayList<Set<Integer>> followpos;
	ArrayList<DFANode> DTrans;
	Set<Integer> StartStates;
	ArrayList<Set<Integer>> EndStates;

	REtoDFA() {
		Init();
	}

	REtoDFA(String re) {
		Init();
		getDFA(re);
	}

	private void Init() {
		ParseTree = new ArrayList<ParseTreeNode>();
		followpos = new ArrayList<Set<Integer>>();
		DTrans = new ArrayList<DFANode>();
		StartStates = new Set<Integer>();
		EndStates = new ArrayList<Set<Integer>>();
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	boolean travel(String s) {
		int i, j, len = s.length();
		int State = 0;
		boolean flag;
		for (i = 0; i < len; i++) {
			flag = false;
			for (j = 0; j < DTrans.get(State).edges.size(); j++) {
				if (DTrans.get(State).edges.get(j).first.equals(""
						+ s.charAt(i))) {
					State = DTrans.get(State).edges.get(j).second;
					flag = true;
					break;
				}
			}
			if (flag == false)
				return false;
		}
		if (DTrans.get(State).State == FinalState)
			return true;
		return false;
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	public ArrayList<DFANode> getDFA(String re) {
		re = normalize(re);
		post = infix_to_postfix(re);
		ParseTree = GenerateParseTree(post);
		ParseTree = initDFA(ParseTree);
		followpos = FollowPos(ParseTree);
		DTrans = DFA(ParseTree, followpos);
		StartStates = DFAInitialState(DTrans);
		EndStates = GetDFAEndState(ParseTree, DTrans);
		return DTrans;
	}

	ArrayList<DFANode> DFA(ArrayList<ParseTreeNode> ParseTree,
			ArrayList<Set<Integer>> followpos) {
		ArrayList<DFANode> ret = new ArrayList<DFANode>();
		int UI, VI, i, j;
		ArrayList<String> Value = GetUniqueOperandList(ParseTree);
		ArrayList<Pair<String, Set<Integer>>> ValuePoses = GetUniqueOperandPoses(
				ParseTree, Value);
		Queue<Integer> Q = new LinkedList<Integer>();
		Set<Integer> T = new Set<Integer>();
		DFANode Start = new DFANode();
		Start.Poses = ParseTree.get(ParseTree.size() - 1).firstpos;
		Start.State = 1;
		Q.add(ret.size());
		ret.add(Start);
		while (!Q.isEmpty()) {
			UI = Q.poll();
			DFANode U = new DFANode();
			U = ret.get(UI);
			for (i = 0; i < ValuePoses.size(); i++) {
				Pair<String, Integer> temp = new Pair<String, Integer>();
				temp.first = ValuePoses.get(i).first;
				Set<Integer> SET = new Set<Integer>();
				T = Intersects(U.Poses, ValuePoses.get(i).second);
				for (j = 0; j < T.size(); j++) {
					SET = Union(SET, followpos.get(T.get(j)));
				}
				if (SET.size() == 0)
					continue;
				DFANode V = new DFANode();
				V.Poses = SET;
				VI = SearchDFANode(ret, V);
				if (VI == -1) {
					Q.add(ret.size());
					VI = ret.size();
					ret.add(V);
				}
				temp.second = VI;
				U.edges.add(temp);
			}
		}
		return ret;
	}

	public Set<Integer> DFAInitialState(ArrayList<DFANode> DTrans) {
		DFANode temp = DTrans.get(0);
		temp.State = InitialState;
		return DTrans.get(0).Poses;
	}

	ArrayList<Set<Integer>> GetDFAEndState(ArrayList<ParseTreeNode> ParseTree,
			ArrayList<DFANode> DTrans) {
		int i, j, pos = 0;
		ArrayList<Set<Integer>> ret = new ArrayList<Set<Integer>>();
		for (i = 0; i < ParseTree.size(); i++) {
			if (ParseTree.get(i).val.equals("#")) {
				pos = ParseTree.get(i).pos;
				break;
			}
		}
		for (i = 0; i < DTrans.size(); i++) {
			for (j = 0; j < DTrans.get(i).Poses.size(); j++) {
				if (DTrans.get(i).Poses.get(j) == pos) {
					DFANode temp = new DFANode();
					temp = DTrans.get(i);
					temp.State = FinalState;
					ret.add(DTrans.get(i).Poses);
					break;
				}
			}
		}
		return ret;
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	boolean NullAble(ArrayList<ParseTreeNode> ParseTree, int n) {
		if (ParseTree.get(n).pos != -1)
			return false;
		if (ParseTree.get(n).val.equals("*")
				|| ParseTree.get(n).val.equals("?"))
			return true;
		int L = ParseTree.get(n).left;
		int R = ParseTree.get(n).right;
		if (ParseTree.get(n).val.equals("|"))
			return (ParseTree.get(L).nullable || ParseTree.get(R).nullable);
		if (ParseTree.get(n).val.equals("."))
			return (ParseTree.get(L).nullable && ParseTree.get(R).nullable);
		if (ParseTree.get(n).val.equals("+"))
			return ParseTree.get(L).nullable;
		return true;
	}

	Set<Integer> FirstPos(ArrayList<ParseTreeNode> ParseTree, int n) {
		Set<Integer> firstpos = new Set<Integer>();
		if (ParseTree.get(n).pos != -1) {
			firstpos.add(ParseTree.get(n).pos);
		} else {
			int L = ParseTree.get(n).left;
			int R = ParseTree.get(n).right;
			if (ParseTree.get(n).val.equals("|")) {
				firstpos = Union(ParseTree.get(L).firstpos,
						ParseTree.get(R).firstpos);
			} else if (ParseTree.get(n).val.equals(".")) {
				if (ParseTree.get(L).nullable) {
					firstpos = Union(ParseTree.get(L).firstpos,
							ParseTree.get(R).firstpos);
				} else {
					firstpos = ParseTree.get(L).firstpos;
				}
			} else if (ParseTree.get(n).val.equals("*")
					|| ParseTree.get(n).val.equals("+")
					|| ParseTree.get(n).val.equals("?")) {
				firstpos = ParseTree.get(L).firstpos;
			}
		}
		return firstpos;
	}

	Set<Integer> LastPos(ArrayList<ParseTreeNode> ParseTree, int n) {
		Set<Integer> lastpos = new Set<Integer>();
		if (ParseTree.get(n).pos != -1) {
			lastpos.add(ParseTree.get(n).pos);
		} else {
			int L = ParseTree.get(n).left;
			int R = ParseTree.get(n).right;
			if (ParseTree.get(n).val.equals("|")) {
				lastpos = Union(ParseTree.get(L).lastpos,
						ParseTree.get(R).lastpos);
			} else if (ParseTree.get(n).val.equals(".")) {
				if (ParseTree.get(R).nullable) {
					lastpos = Union(ParseTree.get(L).lastpos,
							ParseTree.get(R).lastpos);
				} else {
					lastpos = ParseTree.get(R).lastpos;
				}
			} else if (ParseTree.get(n).val.equals("*")
					|| ParseTree.get(n).val.equals("+")
					|| ParseTree.get(n).val.equals("?")) {
				lastpos = ParseTree.get(L).lastpos;
			}
		}
		return lastpos;
	}

	ArrayList<ParseTreeNode> initDFA(ArrayList<ParseTreeNode> ParseTree) {
		for (int i = 0, pos = 1; i < ParseTree.size(); i++) {
			if (!isOperator(ParseTree.get(i).val.charAt(0))) {
				ParseTree.get(i).pos = pos++;
			} else {
				ParseTree.get(i).pos = -1;
			}
			ParseTree.get(i).nullable = NullAble(ParseTree, i);
			ParseTree.get(i).firstpos = FirstPos(ParseTree, i);
			ParseTree.get(i).lastpos = LastPos(ParseTree, i);
		}
		return ParseTree;
	}

	int GetMaxPos(ArrayList<ParseTreeNode> ParseTree) {
		int i, ret = -1;
		for (i = 0; i < ParseTree.size(); i++) {
			if (ret < ParseTree.get(i).pos)
				ret = ParseTree.get(i).pos;
		}
		return ret;
	}

	ArrayList<Set<Integer>> FollowPos(ArrayList<ParseTreeNode> ParseTree) {
		int i, j, L, R;
		ArrayList<Set<Integer>> ret = new ArrayList<Set<Integer>>();
		for (i = 0; i <= GetMaxPos(ParseTree); i++) {
			Set<Integer> t = new Set<Integer>();
			ret.add(t);
		}
		for (i = 0; i < ParseTree.size(); i++) {
			if (ParseTree.get(i).val.equals(".")) {
				L = ParseTree.get(i).left;
				R = ParseTree.get(i).right;
				for (j = 0; j < ParseTree.get(L).lastpos.size(); j++) {
					ret.set(ParseTree.get(L).lastpos.get(j),
							Union(ret.get(ParseTree.get(L).lastpos.get(j)),
									ParseTree.get(R).firstpos));
				}
			} else if (ParseTree.get(i).val.equals("*")) {
				L = ParseTree.get(i).left;
				for (j = 0; j < ParseTree.get(L).lastpos.size(); j++) {
					ret.set(ParseTree.get(L).lastpos.get(j),
							Union(ret.get(ParseTree.get(L).lastpos.get(j)),
									ParseTree.get(L).firstpos));
				}
			}
		}
		return ret;
	}

	ArrayList<String> GetUniqueOperandList(ArrayList<ParseTreeNode> ParseTree) {
		int i;
		ArrayList<String> ret = new ArrayList<String>();
		Set<String> temp = new Set<String>();
		for (i = 0; i < ParseTree.size(); i++) {
			if (!isOperator(ParseTree.get(i).val.charAt(0))
					&& ParseTree.get(i).val.equals("#") == false) {
				temp.add(ParseTree.get(i).val);
			}
		}
		for (i = 0; i < temp.size(); i++) {
			ret.add(temp.get(i));
		}
		return ret;
	}

	int SearchDFANode(ArrayList<DFANode> V, DFANode N) {
		for (int i = 0; i < V.size(); i++) {
			Set<Integer> temp = new Set<Integer>();
			temp = Intersects(V.get(i).Poses, N.Poses);
			if (temp.size() == V.get(i).Poses.size()
					&& temp.size() == N.Poses.size())
				return i;
		}
		return -1;
	}

	ArrayList<Pair<String, Set<Integer>>> GetUniqueOperandPoses(
			ArrayList<ParseTreeNode> ParseTree, ArrayList<String> Value) {
		int i, j;
		ArrayList<Pair<String, Set<Integer>>> ret = new ArrayList<Pair<String, Set<Integer>>>();
		for (i = 0; i < Value.size(); i++) {
			Set<Integer> Poses = new Set<Integer>();
			for (j = 0; j < ParseTree.size(); j++) {
				if (Value.get(i).equals(ParseTree.get(j).val)) {
					Poses.add(ParseTree.get(j).pos);
				}
			}
			Pair<String, Set<Integer>> temp = new Pair<String, Set<Integer>>();
			temp.first = Value.get(i);
			temp.second = Poses;
			ret.add(temp);
		}
		return ret;
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	ArrayList<ParseTreeNode> GenerateParseTree(String post) {
		ArrayList<ParseTreeNode> ParseTree = new ArrayList<ParseTreeNode>();
		post += "#";
		int i, len = post.length();
		Stack<ParseTreeNode> Stk = new Stack<ParseTreeNode>();
		for (i = 0; i < len; i++) {
			ParseTreeNode c = new ParseTreeNode();
			c.val = "" + post.charAt(i);
			c.index = ParseTree.size();
			if (isOperator(post.charAt(i))) {
				if (post.charAt(i) == '|' || post.charAt(i) == '.') {
					ParseTreeNode a = new ParseTreeNode();
					ParseTreeNode b = new ParseTreeNode();
					b = Stk.pop();
					a = Stk.pop();
					c.left = a.index;
					c.right = b.index;
				} else {
					ParseTreeNode a = new ParseTreeNode();
					a = Stk.pop();
					c.left = a.index;
					c.right = -1;
				}
			} else {
				c.left = c.right = -1;
			}
			Stk.push(c);
			ParseTree.add(c);
		}
		if (!Stk.empty() && Stk.peek().val.equals("#")) {
			ParseTreeNode b = new ParseTreeNode();
			ParseTreeNode c = new ParseTreeNode();
			b = Stk.pop();
			c.val = ".";
			c.left = b.index - 1;
			c.right = b.index;
			c.index = ParseTree.size();
			ParseTree.add(c);
		}
		return ParseTree;
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	boolean isDotPlaceAble(int i, String re) {
		if (i == 0)
			return false;
		if (re.charAt(i - 1) == ')' && re.charAt(i) == '(')
			return true;
		if (re.charAt(i - 1) == '*' && re.charAt(i) == '(')
			return true;
		if (!isOperator(re.charAt(i - 1)) && !isOperator(re.charAt(i)))
			return true;
		if (re.charAt(i - 1) == ')' && !isOperator(re.charAt(i)))
			return true;
		if (re.charAt(i - 1) == '*' && !isOperator(re.charAt(i)))
			return true;
		if (!isOperator(re.charAt(i - 1)) && re.charAt(i) == '(')
			return true;
		if (!isOperator(re.charAt(i - 1)) && re.charAt(i) == '*')
			return true;
		return false;
	}

	String normalize(String re) {
		String temp = "";
		int i, len = re.length();
		for (i = 0; i < len; i++) {
			if (isDotPlaceAble(i, re)) {
				temp += '.';
			}
			temp += re.charAt(i);
		}
		return temp;
	}

	String infix_to_postfix(String re) {
		Stack<String> Stk = new Stack<String>();
		String x;
		int i = -1;
		String post = "";
		re += "$";
		Stk.push("#");
		while (Stk.peek().equals("$") == false) {
			i++;
			if (!isOperator(re.charAt(i))) {
				post += re.charAt(i);
			} else if (re.charAt(i) == '(') {
				Stk.push("" + re.charAt(i));
			} else if (re.charAt(i) == ')') {
				x = Stk.pop();
				while (x.equals("(") == false) {
					post += x;
					x = Stk.pop();
				}
			} else {
				while (precedence("" + re.charAt(i)) <= precedence(Stk.peek())) {
					post += Stk.pop();
				}
				Stk.push("" + re.charAt(i));
			}
		}
		return post;
	}

	/****************************************************************
	 * * * * *
	 ****************************************************************/

	boolean isOperand(char ch) {
		return !(ch == '.' || ch == '|' || ch == '(' || ch == ')' || ch == '*'
				|| ch == '+' || ch == '$' || ch == '?');
	}

	int precedence(String ch) {
		if (ch.equals("$"))
			return 1;
		if (ch.equals("("))
			return 2;
		if (ch.equals("|"))
			return 3;
		if (ch.equals("."))
			return 4;
		if (ch.equals("*") || ch.equals("+") || ch.equals("?"))
			return 5;
		return 0;
	}

	boolean isOperator(char ch) {
		return (ch == '.' || ch == '|' || ch == '(' || ch == ')' || ch == '*'
				|| ch == '+' || ch == '$' || ch == '?');
	}

	Set<Integer> Union(Set<Integer> S1, Set<Integer> S2) {
		int i;
		Set<Integer> ret = new Set<Integer>();
		for (i = 0; i < S1.size(); i++) {
			ret.add(S1.get(i));
		}
		for (i = 0; i < S2.size(); i++) {
			ret.add(S2.get(i));
		}
		return ret;
	}

	Set<Integer> Intersects(Set<Integer> S1, Set<Integer> S2) {
		int i, j;
		Set<Integer> ret = new Set<Integer>();
		for (i = 0; i < S1.size(); i++) {
			for (j = 0; j < S2.size(); j++) {
				if (S1.get(i) == S2.get(j)) {
					ret.add(S1.get(i));
				}
			}
		}
		return ret;
	}

	void PrintSet(Set<Integer> S) {
		int i;
		System.out.print("{");
		for (i = 0; i < S.size(); i++) {
			if (i > 0)
				System.out.print(", ");
			System.out.print(S.get(i));
		}
		System.out.println("}");
	}
}
