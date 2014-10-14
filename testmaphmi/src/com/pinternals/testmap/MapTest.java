package com.pinternals.testmap;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import com.sap.aii.repclient.api.query.RequestObject;
import com.sap.aii.repclient.api.query.ResponseObject;
import com.sap.aii.util.applcomp.ApplCompLevel;
import com.sap.aii.util.hmi.api.HmiClient;
import com.sap.aii.util.hmi.api.HmiClientAdapter;
import com.sap.aii.util.hmi.api.HmiClientFactory;
import com.sap.aii.util.hmi.api.HmiCoreException;
import com.sap.aii.util.hmi.api.HmiHttpsURLConnectionFactory;
import com.sap.aii.util.hmi.api.HmiMethodInput;
import com.sap.aii.util.hmi.api.HmiMethodOutput;
import com.sap.aii.util.hmi.api.IHmiHttpURLConnectionFactory;
import com.sap.aii.util.hmi.api.UriElement;
import com.sap.aii.util.hmi.core.msg.HmiRequest;
import com.sap.aii.util.hmi.core.msg.HmiResponse;
import com.sap.aii.util.misc.api.BaseException;
import com.sap.aii.util.misc.api.GUIDFactory;
import com.sap.aii.util.misc.api.Language;
import com.sap.aii.util.prop.api.PropertySource;
import com.sap.guid.IGUID;

public class MapTest {
	URL appUrl = null;
	UriElement service = null;
	UriElement method = null;
	Language en = null;
	ApplCompLevel appLevel = null;
	HmiClient hmiClient = null;
	PropertySource s = null;
	HmiClientFactory hmiFactory = null;
	String user, passwd;

	MapTest(String rep, String version, Language language)
			throws MalformedURLException, BaseException {
		service = UriElement.getInstanceFromUriString("mappingtestservice");
		method = UriElement.getInstanceFromUriString("executemappingmethod");
		appLevel = new ApplCompLevel(version, "*");
		en = language;
		appUrl = new URL(rep);
	}

	HmiClient connect(String user, String password) throws RuntimeException {
		try {
			HmiHttpsURLConnectionFactory urlFactory = HmiHttpsURLConnectionFactory.getInstance((KeyStore) null);
			hmiFactory = HmiClientFactory.createInstance(urlFactory, "ZZ_BNN", "2systemType", appUrl.toExternalForm());
			hmiClient = hmiFactory.createClient("ejb", false, service, user, password, en, appLevel);
			this.user = user;
			this.passwd = password;
		} catch (HmiCoreException e) {
			throw new RuntimeException(e.getCause());
		}
		return hmiClient;
	}

	String get1(String str) {
		RequestObject request = new RequestObject();
		request.setServiceId(service);
		request.setMethodId(method);
		request.setRequestBody(str);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("body", request.getRequestBody());
		// params.put("SAP-PASSPORT", "v1,AAAAAAAAAAAAAA");
		// params.put("X-CorrelationID", "AAAAAAAAAAAAAAAAAAAAAAAAAA");
		HmiMethodOutput output = null;
		ResponseObject resp = null;
		HmiMethodInput parser = null;
		HmiResponse lResponse = null;
		IGUID guid = null;
		try {
			HmiClientAdapter a = (HmiClientAdapter) hmiClient;
			parser = new HmiMethodInput(params);
			guid = GUIDFactory.getInstance().createGUID();
			System.out.println("Generated guid: " + guid);
			HmiRequest lRequest = HmiRequest.createRequest(true, service, method, parser, "ZZ_BNN", "2systemType", guid, appLevel, user, passwd, en, 0);
			lResponse = a.sendRequestAndReceiveResponse(lRequest);
			if (lResponse.getCoreException() != null) {
				throw lResponse.getCoreException();
			} else if (lResponse.getMethodFault() != null) {
				throw lResponse.getMethodFault();
			} else if (lResponse.getMethodOutput() != null) {
				output = lResponse.getMethodOutput();
			} else {
				throw new RuntimeException("Unexpected control flow - neither method output nor method fault nor core exception in response!");
			}
			output = hmiClient.invokeMethod(method, parser);
			resp = new ResponseObject(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return lResponse.getClientId().toString();// resp.getResponseBody();
		return resp.getResponseBody();
	}
	// f4a95df94e4111e48fb7000007a372ba
	// f4a95df94e4111e48fb7000007a372ba transId
	// F4A95DF94E4111E48FB7000007A372BA
}
