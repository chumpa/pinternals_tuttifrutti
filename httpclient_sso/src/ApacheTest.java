
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
	
	static String sUA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36";
	static String s731uaa = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
	static String s731ujsc = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/j_security_check";
	static String saltB = "<input type=\"hidden\" name=\"j_salt\" value=\"", saltE="\"";
	static String authAnswerB = "<!--	Federation Error Message				-->", authAnswerE = "</div>";
	static String s731repsup = "/rep/support/SimpleQuery";
	static String s731dirsup = "/dir/support/SimpleQuery";
	
	static String getContent(HttpEntity entity, boolean close) throws UnsupportedEncodingException, IllegalStateException, IOException {
		String s = new String(readStream(entity.getContent(), close), "UTF-8");
        return s;
	}
	
	public static void main(String[] args) throws Exception {
		String hp = args[0], uname = args[1], passwd = args[2];
		
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		String salt=null;
		boolean showCookies = false;
	        try {
	            HttpGet httpget = new HttpGet(hp + s731uaa);
	            httpget.setHeader("User-Agent", sUA);

	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            try {
	                HttpEntity entity = response1.getEntity();

	                System.out.println("Login form get: " + response1.getStatusLine());
	                String s = getContent(entity, true);
                    int i = s.indexOf(saltB)+saltB.length(), j = s.indexOf(saltE, i);
                    salt = s.substring(i,j);
//                    System.out.println(s);
                    System.out.println("salt:"+salt+"");

                    if (showCookies) {
		                System.out.println("Initial set of cookies:");
		                List<Cookie> cookies = cookieStore.getCookies();
		                if (cookies.isEmpty()) {
		                    System.out.println("None");
		                } else {
		                    for (i = 0; i < cookies.size(); i++) {
		                        System.out.println("- " + cookies.get(i).toString());
		                    }
		                }
                    }
	            } finally {
	                response1.close();
	            }

	            HttpPost httpost = new HttpPost(hp + s731ujsc);
	            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	            nvps.add(new BasicNameValuePair("j_username", uname));
	            nvps.add(new BasicNameValuePair("j_password", passwd));
	            nvps.add(new BasicNameValuePair("j_salt", salt));

	            httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

	            CloseableHttpResponse response2 = httpclient.execute(httpost);
	            boolean ok = false;
	            String errorAuth = "(reason of error is undetected)";
	            try {
	                HttpEntity entity = response2.getEntity();
	                System.out.println("Post-login form get: " + response2.getStatusLine().getStatusCode() + "_" + response2.getStatusLine());
	                ok = response2.getStatusLine().getStatusCode() == 302;
	                String s = getContent(entity, true);
	                new PrintStream(new File("login_rezult.html")).print(s);

                    if (showCookies) {
		                System.out.println("Post logon cookies:");
		                List<Cookie> cookies = cookieStore.getCookies();
		                if (cookies.isEmpty()) {
		                    System.out.println("None");
		                } else {
		                    for (int i = 0; i < cookies.size(); i++) {
		                        System.out.println("- " + cookies.get(i).toString());
		                    }
		                }
                    }
	                if (!ok) {
	                	int i = s.indexOf(authAnswerB);
	                	if (i!=-1) i += authAnswerB.length();
	                	int j = s.indexOf(authAnswerE, i);
	                	errorAuth = s.substring(i, j + authAnswerE.length()).trim();
	                }
	            } finally {
	                response2.close();
	            }
	            
	            if (ok) {
	            	System.out.println("Authorization for " + uname + " is OK!");
	            	
		            httpget = new HttpGet(hp + s731repsup);
	//	            httpget.setHeader("User-Agent", sUA);
		            response1 = httpclient.execute(httpget);
		            try {
		                HttpEntity entity = response1.getEntity();
		                String s = getContent(entity, true);
	                    new PrintStream(new File("repository_entities.html")).print(s);
	                    System.out.println("See repository_entities.html");
		            } finally {
		                response1.close();
		            }
		            
		            httpget = new HttpGet(hp + s731dirsup);
	//	            httpget.setHeader("User-Agent", sUA);
		            response1 = httpclient.execute(httpget);
		            try {
		                HttpEntity entity = response1.getEntity();
		                String s = getContent(entity, true);
	                    new PrintStream(new File("directory_entities.html")).print(s);
	                    System.out.println("See directory_entities.html");
		            } finally {
		                response1.close();
		            }
	            } else {
	            	System.out.println("Authorization for " + uname + " is FAILED!");
	            	System.out.println("ERROR of authorization: " + errorAuth);
	            	new File("repository_entities.html").delete();
	            	new File("directory_entities.html").delete();
	            }
	        } finally {
	            httpclient.close();
	        }
	    }
}
