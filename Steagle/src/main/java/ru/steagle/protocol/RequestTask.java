package ru.steagle.protocol;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ru.steagle.utils.MySSLSocketFactory;
import ru.steagle.utils.Utils;

public class RequestTask
		extends
		AsyncTask<String /* Params */, String /* Progress */, String /* Result */> {

	private String regServer;
	
	public RequestTask(String regServer) {
		this.regServer = regServer;
	}

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... params) { // postBody
        String postBody = params[0];
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
            Utils.writeLogMessage("Exception: " + e.getMessage()+"\n", false);
        }
        return responseString;
    }
//    @Override
//    protected String doInBackground(String... params) { // postBody
//        String postBody = params[0];
//        String responseString = null;
//        try {
//            HttpClient httpclient = HTTPClientFactory.getClient();
//
//            HttpPost httpPost = new HttpPost(regServer);
//            StringEntity stringEntity = new StringEntity(postBody, HTTP.UTF_8);
//            stringEntity.setContentType("text/xml");
//            httpPost.setHeader("Content-Type","application/xml; charset=utf-8");
//            httpPost.setEntity(stringEntity);
//
//            HttpResponse response = httpclient.execute(httpPost);
//            HttpEntity entity = response.getEntity();
//            responseString = EntityUtils.toString(entity, "UTF-8");
//
//        } catch (ClientProtocolException e) {
//            Utils.writeLogMessage("Exception: " + e.getMessage(), false);
//        } catch (IOException e) {
//            Utils.writeLogMessage("Exception: " + e.getMessage(), false);
//        }
//        return responseString;
//    }
}
