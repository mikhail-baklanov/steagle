package ru.steagle.service;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyStore;

import ru.steagle.protocol.HTTPClientFactory;
import ru.steagle.utils.MySSLSocketFactory;
import ru.steagle.utils.Utils;

public class ServiceTask {

	private String regServer;

	public ServiceTask(String regServer) {
		this.regServer = regServer;
	}

	protected String execute(String postBody) {
		String responseString = null;
		try {

			HttpPost httpPost = new HttpPost(regServer);
            StringEntity stringEntity = new StringEntity(postBody, HTTP.UTF_8);
            stringEntity.setContentType("text/xml");
            httpPost.setHeader("Content-Type","application/xml; charset=utf-8");
            httpPost.setEntity(stringEntity);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", sf, 443));

            ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(httpPost.getParams(), registry);

            DefaultHttpClient httpClient = new DefaultHttpClient(ccm, httpPost.getParams());

			HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");

		} catch (Throwable e) {
            Utils.writeLogMessage("Exception: " + e.getMessage() + "\n", false);
		}
        if (responseString == null)
            responseString = "";
		return responseString;
	}

}
