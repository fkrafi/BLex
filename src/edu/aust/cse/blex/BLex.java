package edu.aust.cse.blex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import edu.aust.cse.collections.Pair;

public class BLex {
	private static ArrayList<REtoDFA> dfas;
	private static ArrayList<Pair<String, String>> regexps;
	private static String alpha = "ঁংঃঅআইঈউঊঋঌএঐওঔকখগঘঙচছজঝঞটঠডঢণতথদধনপফবভমযরলশষসহ ়ঽািীুূৃৄেৈোৌ্ৎৗড়ঢ়য়ৠৡৢৣ০১২৩৪৫৬৭৮৯ৰৱ৲৳৴৵৶৷৸৹৺";

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out
					.println("usuage :: java BLex blexfilename codefile");
		}/* else if (args[0].endsWith(".blex") == false) {
			System.out
					.println("Invalid File Type. File extension must have to be \'blex\'");
		} else if (args[1] == "") {
			System.out
					.println("Invalid File Type. File extension must have to be \'blex\'");
		} */else {
			dfas = new ArrayList<REtoDFA>();
			regexps = new ArrayList<Pair<String, String>>();
			readCode(args[0]);
			match(args[1]);
		}
	}

	private static void readCode(String blexfile) {
		try {
			FileInputStream fstream = new FileInputStream(blexfile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String line;
			boolean flag = true;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (flag || line.length() == 0 || line.charAt(0) == '#') {
					flag = false;
					continue;
				}
				Pair<String, String> regexp = new Pair<String, String>();
				String tokens[] = line.split("=>");
				regexp.first = processRegex(tokens[0].trim());
				regexp.second = tokens[1].trim();
				REtoDFA dfa = new REtoDFA(regexp.first);
				regexps.add(regexp);
				dfas.add(dfa);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error : " + e.toString());
		}
	}

	private static String processRegex(String re) {
		String ret = "";
		int len = re.length(), i;
		for (i = 0; i < len; i++) {
			if (re.charAt(i) == '-') {
				i++;
				char j = ret.charAt(ret.length() - 1);
				char k = re.charAt(i);
				int index = search(ret.charAt(ret.length() - 1));
				if (index >= 0) {
					j = alpha.charAt(index + 1);
				}
				index = search(re.charAt(i));
				if (index >= 0) {
					k = alpha.charAt(index);
				}
				for (; j <= k; j++) {
					ret += j;
				}
			} else {
				ret += re.charAt(i);
			}
		}
		re = "";
		len = ret.length();
		for (i = 0; i < len; i++) {
			if (ret.charAt(i) == '[') {
				re += "(";
				i++;
				while (i < len && ret.charAt(i) != ']') {
					if (re.charAt(re.length() - 1) != '(') {
						re += "|";
					}
					re += ret.charAt(i);
					i++;
				}
				re += ")";
			} else {
				re += ret.charAt(i);
			}
		}
		ret = re;
		return ret;
	}

	static int search(char ch) {
		for (int i = 0; i < alpha.length(); i++) {
			if (alpha.charAt(i) == ch) {
				return i;
			}
		}
		return -1;
	}

	private static void match(String codefile) {
		try {
			int i;
			FileInputStream fstream = new FileInputStream(codefile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("lexical.out"), "UTF8"));

			String line;
			boolean flag = true;
			while ((line = br.readLine()) != null) {
				if (flag) {
					flag = false;
					continue;
				}
				for (i = 0; i < dfas.size(); i++) {
					if (dfas.get(i).travel(line) == true) {
						out.write(line + "\t=>\t" + regexps.get(i).second
								+ "\n");
						break;
					}
				}
				if (i == dfas.size()) {
					out.write("Invalid Token :: " + line + "\n");
				}
			}
			in.close();
			out.close();
		} catch (Exception e) {
			System.err.println("Error : " + e.toString());
		}
	}
}
