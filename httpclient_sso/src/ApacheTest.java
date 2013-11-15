import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;



public class ApacheTest {

	static byte[] readStream(InputStream is, boolean close) throws IOException {
		ArrayList<Byte> a = new ArrayList<Byte>(100);
		int i = is.read();
		while (i != -1) {
			a.add((byte) i);
			i = is.read();
		}
		byte b[] = new byte[a.size()];
		i = 0;
		for (Byte y : a) {
			b[i++] = y;
		}
		if (close)
			is.close();
		return b;
	}

	static String sUA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36";
	static String s731uaa = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
	static String s731ujsc = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/j_security_check";
	static String saltB = "<input type=\"hidden\" name=\"j_salt\" value=\"",
			saltE = "\"";
	static String authAnswerB = "<!--	Federation Error Message				-->",
			authAnswerE = "</div>";
	static String s731repsup = "/rep/support/SimpleQuery";
	static String s731dirsup = "/dir/support/SimpleQuery";

	static String getContent(HttpEntity entity, boolean close)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException {
		String s = new String(readStream(entity.getContent(), close), "UTF-8");
		return s;
	}

	public static void main(String[] args) throws Exception {
		String hp = args[0], uname = args[1], passwd = args[2];
		boolean showCookies = false, getRep = true, getDir = true, useMTread = true;
		
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient;
		PoolingHttpClientConnectionManager cm;
		if (!useMTread) {
			httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		} else {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(30);
			cm.setDefaultMaxPerRoute(30);
			httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setConnectionManager(cm)
				.build();
		}
		
		String salt = null;
		try {
			HttpGet httpget = new HttpGet(hp + s731uaa);
			httpget.setHeader("User-Agent", sUA);

			CloseableHttpResponse response1 = httpclient.execute(httpget);
			try {
				HttpEntity entity = response1.getEntity();

				System.out.println("Login form get: "
						+ response1.getStatusLine());
				String s = getContent(entity, true);
				int i = s.indexOf(saltB) + saltB.length(), j = s.indexOf(saltE,
						i);
				salt = s.substring(i, j);
				// System.out.println(s);
				System.out.println("salt:" + salt + "");

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
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("j_username", uname));
			nvps.add(new BasicNameValuePair("j_password", passwd));
			nvps.add(new BasicNameValuePair("j_salt", salt));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			CloseableHttpResponse response2 = httpclient.execute(httpost);
			boolean ok = false;
			String errorAuth = "(reason of error is undetected)";
			try {
				HttpEntity entity = response2.getEntity();
				System.out.println("Post-login form get: "
						+ response2.getStatusLine().getStatusCode() + "_"
						+ response2.getStatusLine());
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
					if (i != -1)
						i += authAnswerB.length();
					int j = s.indexOf(authAnswerE, i);
					errorAuth = s.substring(i, j + authAnswerE.length()).trim();
				}
			} finally {
				response2.close();
			}

			if (ok) {
				System.out.println("Authorization for " + uname + " is OK!");

				httpget = new HttpGet(hp + s731repsup);
				// httpget.setHeader("User-Agent", sUA);
				response1 = httpclient.execute(httpget);
				try {
					HttpEntity entity = response1.getEntity();
					String s = getContent(entity, true);
					new PrintStream(new File("repository_entities.html"))
							.print(s);
					System.out.println("See repository_entities.html");
					// Attempt to get all entities for REP and DIR
					if (getRep) {
						new File("rep").mkdir();
						String s1 = "<select name=\"types\" size=10 multiple>";
						int i = s.indexOf(s1)+s1.length();
						int j = s.indexOf("</select>", i);
						String[] st = s.substring(i,j).split("<option value=\"");
						List<String> repent = new ArrayList<String>(st.length);
						for (String q: st) if (q.length()>5) repent.add(q.split("\"")[0].trim());
						for (String q:repent) System.out.println("="+q+"=");
						EntityPostThread[] threads = new EntityPostThread[repent.size()];

						List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
						nvps2.add(new BasicNameValuePair("qc", "All software components"));
						nvps2.add(new BasicNameValuePair("syncTabL", "true"));
						nvps2.add(new BasicNameValuePair("deletedL", "B"));
						nvps2.add(new BasicNameValuePair("xmlReleaseL", "7.1"));
						nvps2.add(new BasicNameValuePair("queryRequestXMLL",""));
						nvps2.add(new BasicNameValuePair("action", "Refresh depended values"));

						i=0;
						for (String q:repent) {
							HttpPost p = new HttpPost(hp + s731repsup);
							nvps = nvps2;
							nvps.add(new BasicNameValuePair("types", q));
							p.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
							threads[i++] = new EntityPostThread(q,new File("rep/"+q+".html"),httpclient, p);
						}
						for (j = 0; j < threads.length; j++) {
						    threads[j].start();
						}
						for (j = 0; j < threads.length; j++) {
						    threads[j].join();
						}
					}
				} finally {
					response1.close();
				}

				httpget = new HttpGet(hp + s731dirsup);
				// httpget.setHeader("User-Agent", sUA);
				response1 = httpclient.execute(httpget);
				try {
					HttpEntity entity = response1.getEntity();
					String s = getContent(entity, true);
					new PrintStream(new File("directory_entities.html"))
							.print(s);
					System.out.println("See directory_entities.html");
					if (getDir) {
						new File("dir").mkdir();
						String s1 = "<select name=\"types\" size=10 multiple>";
						int i = s.indexOf(s1)+s1.length();
						int j = s.indexOf("</select>", i);
						String[] st = s.substring(i,j).split("<option value=\"");
						List<String> dirent = new ArrayList<String>(st.length);
						for (String q: st) if (q.length()>5) dirent.add(q.split("\"")[0].trim());
						for (String q:dirent) System.out.println("="+q+"=");
						EntityPostThread[] threads = new EntityPostThread[dirent.size()];

						List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
						nvps2.add(new BasicNameValuePair("qc", "Default (for directory objects)"));
						nvps2.add(new BasicNameValuePair("syncTabL", "true"));
						nvps2.add(new BasicNameValuePair("deletedL", "B"));
						nvps2.add(new BasicNameValuePair("queryRequestXMLL",""));
						nvps2.add(new BasicNameValuePair("xmlReleaseL", "7.1"));
						nvps2.add(new BasicNameValuePair("action", "Refresh depended values"));

						i=0;
						for (String q:dirent) {
							HttpPost p = new HttpPost(hp + s731dirsup);
							nvps = nvps2;
							nvps.add(new BasicNameValuePair("types", q));
							p.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
							threads[i++] = new EntityPostThread(q,new File("dir/"+q+".html"),httpclient, p);
						}
						for (j = 0; j < threads.length; j++) {
						    threads[j].start();
						}
						for (j = 0; j < threads.length; j++) {
						    threads[j].join();
						}
					}
				} finally {
					response1.close();
				}

			} else {
				System.out
						.println("Authorization for " + uname + " is FAILED!");
				System.out.println("ERROR of authorization: " + errorAuth);
				new File("repository_entities.html").delete();
				new File("directory_entities.html").delete();
			}
		} finally {
			httpclient.close();
		}
	}
}

class GetThread extends Thread {
    private final CloseableHttpClient httpClient;
    private final HttpContext context;
    private final HttpGet httpget;
    public GetThread(CloseableHttpClient httpClient, HttpGet httpget) {
        this.httpClient = httpClient;
        this.context = HttpClientContext.create();
        this.httpget = httpget;
    }
    @SuppressWarnings("deprecation")
	@Override
    public void run() {
        try {
            CloseableHttpResponse response = httpClient.execute(
                    httpget, context);
            try {
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            } finally {
                response.close();
            }
        } catch (ClientProtocolException ex) {
            // Handle protocol errors
        } catch (IOException ex) {
            // Handle I/O errors
        }
    }
}

class EntityPostThread extends Thread {

    private final CloseableHttpClient httpClient;
    private final HttpContext context;
    private final HttpPost httppost;
//    private final String typen;
    private final File typef;

    public EntityPostThread(String typen, File typef, CloseableHttpClient httpClient, HttpPost httppost) {
        this.httpClient = httpClient;
        this.context = HttpClientContext.create();
        this.httppost = httppost;
        this.typef = typef;
//        this.typen = typen;
    }

    @Override
    public void run() {
        try {
            CloseableHttpResponse response = httpClient.execute(httppost, context);
            try {
                HttpEntity entity = response.getEntity();
            	typef.createNewFile();
                if (entity!=null) {
                	FileOutputStream o = new FileOutputStream(typef);
                	BufferedInputStream x = new BufferedInputStream(entity.getContent(), 16384);
                	byte[] f = new byte[8192];
                	int q = x.read(f);
                	while (q!=-1) {
                		o.write(f,0,q);
                		q = x.read(f);
                	}
                	o.close();
                	entity.getContent().close();
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException ex) {
            // Handle protocol errors
        } catch (IOException ex) {
            // Handle I/O errors
        }
    }

}
