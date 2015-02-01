package com.mioty.fabianbuentello.nrinject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class mainListView extends ActionBarActivity {

    ArrayList<Nodes> nodesList;
    NodeAdapter adapter;
    SharedPreferences sharedPref;
    String USERS_IPADDR, USERS_PASS;
    Button refreshBtn, settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list_view);

        refreshBtn = (Button) findViewById(R.id.refresh_button);
        settingsBtn = (Button) findViewById(R.id.settings_button);

        sharedPref = this.getSharedPreferences(
                getString(R.string.preference_url_settings), Context.MODE_PRIVATE);
        populateTable();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateTable();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                segueToSettings();
            }
        });

    }

    private void populateTable() {


        System.out.println("populateTable");
        nodesList = new ArrayList<Nodes>();
        String ipAddrStr = getResources().getString(R.string.PREF_IPADDRESS);
        String passStr = getResources().getString(R.string.PREF_PASSWORD);
        USERS_IPADDR = sharedPref.getString(ipAddrStr, ipAddrStr);
        USERS_PASS = sharedPref.getString(passStr, passStr);
        new getNodesAsyncTask().execute(USERS_IPADDR,USERS_PASS);

        ListView listview = (ListView) findViewById(R.id.list);
        adapter = new NodeAdapter(getApplicationContext(), R.layout.row, nodesList);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1, int position,
                                    long id) {

                new TriggerNodeAsyncTask().execute(USERS_IPADDR, USERS_PASS,nodesList.get(position).getnID());



            }
        });
    }

    private void segueToSettings() {
        Intent segueIntent = new Intent(this, Settings.class);
        startActivity(segueIntent);
    }


    class getNodesAsyncTask extends AsyncTask<String, Void, Boolean> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mainListView.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                //------------------>>
                String finalURL = String.format("http://%s/injector/nodes",urls[0]);
                String finalPass = urls[1];

                HttpGet httpget = new HttpGet(finalURL);
                httpget.setHeader("AUTH", finalPass);
                HttpParams httpParameters = new BasicHttpParams();
                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 5000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                HttpResponse response = httpClient.execute(httpget);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);


                    JSONArray JsonArr = new JSONArray(data);

                    for (int i = 0; i < JsonArr.length(); i++) {
                        JSONObject object = JsonArr.getJSONObject(i);

                        Nodes node = new Nodes();

                        node.setnID(object.getString("id"));
                        node.setnName(object.getString("name"));

                        nodesList.add(node);
                    }
                    return true;
                }

                //------------------>>
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            dialog.cancel();
            adapter.notifyDataSetChanged();
            if (result == false) {
                Toast toast = Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.invalid_warning);
                toast.show();
            }
        }
    }


    class TriggerNodeAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", urls[2]));
                //------------------>>
                String finalURL = String.format("http://%s/injector/inject",urls[0]);
                String finalPass = urls[1];
                //------------------>>
                HttpGet httpget = new HttpGet(finalURL + "?" + URLEncodedUtils.format(params, "utf-8"));

                httpget.setHeader("AUTH", finalPass);

                HttpParams httpParameters = new BasicHttpParams();
                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 5000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

                HttpResponse response = httpClient.execute(httpget);

                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {

                    /*
                    *need to alert on the main thread
                    * */
                    return true;
                }

                //------------------>>

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            if (result == false) {
                Toast.makeText(getApplicationContext(), "Unable to Trigger Node", Toast.LENGTH_LONG).show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Successfully Triggered Node", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.toast_background);
                toast.show();
            }


        }
    }
}