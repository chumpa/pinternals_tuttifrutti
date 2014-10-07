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
import com.sap.aii.util.hmi.api.HmiClientFactory;
import com.sap.aii.util.hmi.api.HmiCoreException;
import com.sap.aii.util.hmi.api.HmiHttpsURLConnectionFactory;
import com.sap.aii.util.hmi.api.HmiMethodInput;
import com.sap.aii.util.hmi.api.HmiMethodOutput;
import com.sap.aii.util.hmi.api.UriElement;
import com.sap.aii.util.misc.api.BaseException;
import com.sap.aii.util.misc.api.Language;
import com.sap.aii.util.prop.api.PropertySource;

public class MapTest {
	URL appUrl = null;
	UriElement service = null;
	UriElement method = null;
	Language en = null;
	ApplCompLevel appLevel = null;
	HmiClient hmiClient = null;
	PropertySource s = null;

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
			HmiClientFactory hmiFactory = HmiClientFactory.createInstance(urlFactory, (String) null, (String) null, appUrl.toExternalForm());
			hmiClient = hmiFactory.createClient("any", false, service, user, password, en, appLevel);
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
		HmiMethodInput parser = null;
		HmiMethodOutput output = null;
		ResponseObject responseObject = null;
		try {
			parser = new HmiMethodInput(params);
			output = hmiClient.invokeMethod(method, parser);
			responseObject = new ResponseObject(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseObject.getResponseBody();
	}
	//	
}
