package com.pinternals.testmap;

import java.io.BufferedReader;
import java.io.FileReader;
import com.sap.aii.util.misc.api.Language;

public class Main {
	public static void main(String[] args) {
		String repurl = args[0], uname = args[1], pwd = args[2];
		
		MapTest z = null;
		BufferedReader br = null; 
		char[] cbuf = new char[16384];
		String src = null;
		
		try {
			br = new BufferedReader(new FileReader("sample1.xml"));
			int i = br.read(cbuf);
			src = new String(cbuf, 0, i);
			z = new MapTest(repurl, "7.31", Language.EN);
			z.connect(uname, pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i=0; i<100; i++) {
			String s = z.get1(src);
			System.out.println("get1 done [" + i + "]: " + s);
		}
	}
}
//try {
//PrintWriter pw = new PrintWriter("DOM_" + new Date().getTime() + ".log");
//pw.println("x");
//pw.close();
//} catch (Exception e) {}
