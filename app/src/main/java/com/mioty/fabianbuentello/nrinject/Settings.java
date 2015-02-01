package com.mioty.fabianbuentello.nrinject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.regex.Pattern;


public class Settings extends ActionBarActivity {

    // UserDefaults
    String PREF_IPADDR, PREF_PASS;

    String USERS_IPADDR, USERS_PASS;

    SharedPreferences sharedPref;

    Button verifyBtn,backBtn;
    TextView ipAddressField, portField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        verifyBtn = (Button) findViewById(R.id.Verify_Button);
        backBtn = (Button) findViewById(R.id.backBtn);
        ipAddressField = (TextView) findViewById(R.id.ipAddress);
        portField = (TextView) findViewById(R.id.port);
        passwordField = (TextView) findViewById(R.id.password);


        sharedPref = this.getSharedPreferences(
                getString(R.string.preference_url_settings),
                Context.MODE_PRIVATE);
        // Set user Defaults
        PREF_IPADDR = getResources().getString(R.string.PREF_IPADDRESS);
        PREF_PASS = getResources().getString(R.string.PREF_PASSWORD);

        USERS_IPADDR = sharedPref.getString(PREF_IPADDR,"");
        USERS_PASS = sharedPref.getString(PREF_PASS,"");

        if (USERS_IPADDR != "" && USERS_PASS != "") {
            String[] fullIPAddress = USERS_IPADDR.split(":");

            ipAddressField.setText(ipAddressField.getText().toString()+fullIPAddress[0]);
            portField.setText(fullIPAddress[1]);
            passwordField.setText(USERS_PASS);
        }



        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ipAddressField.getText().toString() != "" && passwordField.getText().toString() != "") {
                    String ipAddStr = String.format("%s:%s",ipAddressField.getText().toString(), portField.getText().toString());
                    String passStr = passwordField.getText().toString();

                    if(Patterns.WEB_URL.matcher(ipAddStr).matches()) {
                        new verifySettingsAsyncTask().execute(ipAddStr, passStr);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Your URL is invalid.", Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.setBackgroundResource(R.drawable.invalid_warning);
                        toast.show();
                    }

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Oops: You need to fill in all fields.", Toast.LENGTH_LONG);
                    View view = toast.getView();
                    view.setBackgroundResource(R.drawable.invalid_warning);
                    toast.show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class verifySettingsAsyncTask extends AsyncTask<String, Void, Boolean> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Settings.this);
            dialog.setMessage("Verifying, please wait...");
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

                /*IT WORKED SAVE USER DEFAULTS*/

                    savePreferences(urls[0],urls[1]);

                    return true;
                }

                //------------------>>
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            dialog.cancel();
            if (result == false) {
                Toast toast = Toast.makeText(getApplicationContext(), "Invaild: Please double check your input.", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.invalid_warning);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Vaild: Successfully saved your data.", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.verify_background);
                toast.show();

            }

        }

        private void savePreferences(String ipAddr, String password) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(PREF_IPADDR, ipAddr);
            editor.putString(PREF_PASS, password);
            editor.commit();
        }
    }
}