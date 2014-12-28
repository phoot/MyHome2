package fr.oxilea.myhome;


import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class HTTPClient {


    public static String connectGet(String url, String loginValue, String passwordValue)
    {
        String retStatus="";

        HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object
        HttpGet httpget = new HttpGet(url);

        // add login/password basic authentication
        String credentials = loginValue + ":" + passwordValue;
        String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        httpget.addHeader("Authorization", "Basic " + base64EncodedCredentials);

        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            // Examine the response status
            Log.i("Praeda", response.getStatusLine().toString());

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                retStatus= convertStreamToString(instream);
                // now you have the string representation of the HTML request
                instream.close();
            }

        } catch (Exception e) {}

        return retStatus;
    }

    public void connectPost(String url, String paramName, String paramValue, String loginValue, String passwordValue) {

        final int NumberOfParam=1;
        HttpResponse response;

        // Create a new HttpClient
        HttpClient httpclient = new DefaultHttpClient();

        // Create http POST
        HttpPost httpPost = new HttpPost(url);

        // add login/password basic authentication
        String credentials = loginValue + ":" + passwordValue;
        String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        httpPost.addHeader("Authorization", "Basic " + base64EncodedCredentials);

        try {
            // Add your data 1 parameters
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(NumberOfParam);

            // add param one line for each param
            nameValuePairs.add(new BasicNameValuePair(paramName, paramValue));

            // Url Encoding the POST parameters
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs, "utf-8");
                httpPost.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            // Execute HTTP Post Request
            response = httpclient.execute(httpPost);
            Log.i("reponse", "Post");

        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();

        } catch (IOException e) {
            /// writing exception to log
            e.printStackTrace();

        }
    }

    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
