import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

class Util123 {
	static String begin = "<begin>", end = "<end>", f1 = "velel tashit", f2 = "1234567890";
	static String test1 = "Ivan rodil a girl, " + begin + f1 + end + " a sheet";
	static String test2 = begin + f2 + end;
	static String test3 = "<begin ><begin>.<end ><en d><end>";
	static String test4 = begin + f2;
	static String test5 = test4 + test4 + test4 + test4 + test4 + test4 + test4 + test4 + test4 + test4;
	static String test6 = test5 + test5 + test5 + test5 + test5 + test5 + test5 + test5 + test5 + test5; 
	
	static boolean test() throws IOException {
		boolean b = true;
		String r1 = findBetweenToken(new StringReader(test1), begin, end);
		System.out.println("r1=|" + r1+"|");
		String r2 = findBetweenToken(new StringReader(test2), begin, end);
		System.out.println("r2=|" + r2+"|");
		String r3 = findBetweenToken(new StringReader(test3), begin, end);
		System.out.println("r3=|" + r3+"|");
		String r4 = findBetweenToken(new StringReader(test4), begin, end);
		System.out.println("r4=|" + r4+"|");
		return b;
	}
	
	
	// look for given "begin" and "end" at Reader
	// if "begin" found and no "eng" found, returned  
	public static String findBetweenToken(Reader r, String begin, String end) throws IOException {
		assert begin!=null && end!=null && begin.length()>0 && end.length()>0;
		String f = null;
		StringBuilder b = new StringBuilder(), v = new StringBuilder();
		int i, j = 0, c = 0, d = 0;
		boolean f1 = false, f2 = false;
		char[] x = new char[1024];
		while ((i = r.read(x)) > 0 && !(f1&&f2)) {
//			System.out.print("\n" + i + "\t" + f1 + "\t" + f2 + "\t" + new String(x,0,i)+"\t"+b+"\t"+v);
			if (!f1) for (j = 0; j<i && !f1; j++) {
				if (x[j] == begin.charAt(c)) 
					c++;
				else
					c = 0;
				f1 = f1 || begin.length()==c; 
			}
			for (; j<i && f1 && !f2; j++) {
				if (x[j]==end.charAt(d)) {
					d++;
					v.append(x[j]);
				} else {
					b.append(v);
					v.setLength(0);
					b.append(x[j]);
					d = 0;
				}
				f2 = f2 || end.length()==d;
			}
			j = 0;
		}
		f = b.toString();
		return f;
	}
}


public class Test {
	public static void main(String[] args) throws Exception {
		System.out.println("Test: " + Util123.test());
	}

}
