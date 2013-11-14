
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class ApacheTest {
	
	static byte[] readStream(InputStream is, boolean close) throws IOException {
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
		if (close) is.close();
		return b;
	}
	
	static String s731 = "http://sapsrv:50600/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
	static String s731ua = "http://sapsrv:50600/useradmin";
	static String s731ja = "http://sapsrv:50600/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/j_security_check";
	static String sUA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36";
	static String uname = "Uuu";
	static String passwd = "Ppp";
	static String saltB = "<input type=\"hidden\" name=\"j_salt\" value=\"", saltE="\"";
	
	static String getContent(HttpEntity entity, boolean close) throws UnsupportedEncodingException, IllegalStateException, IOException {
		String s = new String(readStream(entity.getContent(), close), "UTF-8");
        return s;
	}
	
	  public static void main(String[] args) throws Exception {
	        BasicCookieStore cookieStore = new BasicCookieStore();
	        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	        String salt=null;
	        try {
	            HttpGet httpget = new HttpGet(s731);
	            httpget.setHeader("User-Agent", sUA);


	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            try {
	                HttpEntity entity = response1.getEntity();

	                System.out.println("Login form get: " + response1.getStatusLine());
	                String s = getContent(entity, true);
                    int i = s.indexOf(saltB)+saltB.length(), j = s.indexOf(saltE, i);
                    salt = s.substring(i,j);
//                    System.out.println(s);
                    System.out.println("salt=|"+salt+"|");
	                

	                System.out.println("Initial set of cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
	            } finally {
	                response1.close();
	            }

	            HttpPost httpost = new HttpPost(s731ja);
	            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	            nvps.add(new BasicNameValuePair("j_username", uname));
	            nvps.add(new BasicNameValuePair("j_password", passwd));
	            nvps.add(new BasicNameValuePair("j_salt", salt));

	            httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

	            CloseableHttpResponse response2 = httpclient.execute(httpost);
	            try {
	                HttpEntity entity = response2.getEntity();

	                System.out.println("Login form get: " + response2.getStatusLine());
	                String s = getContent(entity, true);
	                System.out.println("Login result is: " + s);

	                System.out.println("Post logon cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
	            } finally {
	                response2.close();
	            }
	        } finally {
	            httpclient.close();
	        }
	    }
}
