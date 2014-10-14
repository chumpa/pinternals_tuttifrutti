package com.pinternals.testmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.sap.aii.util.misc.api.Language;

public class Main {
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a z");
		sdf.setLenient(true);
		Date d = null;
		TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
		System.out.println(timeZone.getOffset(new Date().getTime()));
		String s = "10/14/2014 06:36:34 PM +0400";
		try {
			d = sdf.parse(s);
		} catch (ParseException e) {
			System.err.println(e.getErrorOffset() + "\t" + s.substring(e.getErrorOffset()));
			e.printStackTrace();
		}
		System.out.println(d.getTime());
		
	}
	public static void main2(String[] args) {
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
		for (int i=0; i<1; i++) {
			String s = z.get1(src);
			System.out.println(s);
		}
	}
}
//try {
//PrintWriter pw = new PrintWriter("DOM_" + new Date().getTime() + ".log");
//pw.println("x");
//pw.close();
//} catch (Exception e) {}
