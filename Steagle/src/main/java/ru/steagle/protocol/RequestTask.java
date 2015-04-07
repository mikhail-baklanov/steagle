package ru.steagle.protocol;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

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
			HttpClient httpclient = HTTPClientFactory.getClient();

			HttpPost httpPost = new HttpPost(regServer);
			StringEntity stringEntity = new StringEntity(postBody, HTTP.UTF_8);
		    stringEntity.setContentType("text/xml");
		    httpPost.setHeader("Content-Type","application/xml; charset=utf-8");
		    httpPost.setEntity(stringEntity);
		    
			HttpResponse response = httpclient.execute(httpPost);
//			BasicResponseHandler responseHandler = new BasicResponseHandler();
//            responseString = responseHandler.handleResponse(response);
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");

		} catch (ClientProtocolException e) {
			// responseString = null
		} catch (IOException e) {
			// responseString = null
		}
		return responseString;
	}

}
