import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



class Client {
	URL u;
	String uname, passwd, htCookie, htHost, htUA;
	Client (String hostPort, String uname, String passwd) throws IOException {
		u = new URL(hostPort);
		this.uname = uname;
		this.passwd = passwd;
		HttpURLConnection huc = (HttpURLConnection)u.openConnection();
		huc.setInstanceFollowRedirects(true);
		huc.setRequestProperty("User-Agent", "Mozilla 5.0");
		
		
		
		
		String s = readHttpURLConnection(huc);
		System.out.println(u.toString() + " " + huc.getResponseCode() + " " + huc.getResponseMessage());

//		htCookie = huc.getHeaderField(name)
//		System.out.println(htCookie);

		htCookie = huc.getHeaderField("Cookie");
		System.out.println(htCookie);
		htHost = huc.getHeaderField("Host");
		System.out.println(htHost);
		htUA = huc.getHeaderField("User-Agent");
		System.out.println(htUA);
		int i = s.indexOf("<title>"), j = s.indexOf("</title>");
		s = s.substring(i,j);
		System.out.println(s);
		
		
	}
	static String readHttpURLConnection(HttpURLConnection huc) throws UnsupportedEncodingException, IOException {
		InputStream is;
		is = (huc.getResponseCode()<400) ? huc.getInputStream() : huc.getErrorStream(); 
		return new String(readStream(is), "UTF-8");
	}
		
	static byte[] readStream(InputStream is) throws IOException {
		ArrayList<Byte> a = new ArrayList<Byte>(100);
		int i = is.read();
		while (i!=-1) {
			a.add((byte)i);
			i=is.read();
		}
		byte b[] = new byte[a.size()];
		i=0;
		for (Byte y: a) {
			b[i++] = y;
		}
		return b;
	}

	void login(String resource) throws IOException {
		URL t = new URL(resource.charAt(0)=='/' ? u.toExternalForm() + resource : u.toExternalForm() + resource);
		HttpURLConnection huc = (HttpURLConnection)t.openConnection();
		huc.setRequestProperty("Connection", "keep-alive");
		huc.setRequestProperty("User-Agenr", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36");
		huc.setReadTimeout(20000);
        huc.setInstanceFollowRedirects(false);
		String s = readHttpURLConnection(huc);
		System.out.println("Response code: " + huc.getResponseCode() + " " + huc.getResponseMessage());
		Map<String,List<String>> hd = huc.getHeaderFields();
		for (String hn: hd.keySet()) {
			System.out.println(hn + "\t" + hd.get(hn).toString());
		}
		System.out.println(s); 
	}

	String getRelativeURL(String relative) {
		return "";
	}
}

public class HSTest {
	
	
	
	public static void main(String[] args) {
		assert 1!=0;
		Client cl;
		String s731="/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
		try {
			
			cl = new Client(args[0], args[1], args[2]);
//			cl.login("/useradmin/userAdminServlet");
//			cl.login(s731);
			
		} catch (Exception u) {
			u.printStackTrace();
		}
	}

}
