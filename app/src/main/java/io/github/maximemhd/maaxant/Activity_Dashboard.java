/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2013
All rights reserved.
*/

package io.github.maximemhd.maaxant;

//import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import io.github.maximemhd.maaxant.watchdownloader.Activity_WatchScanList;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.pluginlib.version.PluginLibVersionInfo;
import com.sweetzpot.stravazpot.athlete.api.AthleteAPI;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope;
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI;
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin;
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials;
import com.sweetzpot.stravazpot.authenticaton.model.LoginResult;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginButton;
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.api.exception.StravaAPIException;
import com.sweetzpot.stravazpot.common.api.exception.StravaUnauthorizedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt.AUTO;

/**
 * Dashboard 'menu' of available sampler activities
 */
public class Activity_Dashboard extends AppCompatActivity
{
    private static final int RQ_LOGIN = 1001;
    protected ListAdapter mAdapter;
    protected ListView mList;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    //Initialize the list
    @SuppressWarnings("serial") //Suppress warnings about hash maps not having custom UIDs
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            Log.i("ANT+ Plugin Sampler", "Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e)
        {
            Log.i("ANT+ Plugin Sampler", "Version: " + e.toString());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        List<Map<String,String>> menuItems = new ArrayList<Map<String,String>>();
        menuItems.add(new HashMap<String,String>(){{put("title","Watch Downloader Utility");put("desc","Download data from watches");}});
        menuItems.add(new HashMap<String,String>(){{put("title","Launch ANT+ Plugin Manager");put("desc","Controls device database and default settings");}});

        //login button
        StravaLoginButton loginButton = (StravaLoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.sharedPreferences),"disconnected");

        if(token=="disconnected"){
            ((TextView)findViewById(R.id.textView_connection)).setText("Disconnected");
        }
        else {
            StravaConfig config = null;
            config = StravaConfig.withToken(token)
                        .debug()
                        .build();
            new async().execute(config);
        }



        SimpleAdapter adapter = new SimpleAdapter(this, menuItems, android.R.layout.simple_list_item_2, new String[]{"title","desc"}, new int[]{android.R.id.text1,android.R.id.text2});
        setListAdapter(adapter);

       /* try
        {
            ((TextView)findViewById(R.id.textView_PluginSamplerVersion)).setText("Sampler Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e)
        {
            ((TextView)findViewById(R.id.textView_PluginSamplerVersion)).setText("Sampler Version: ERR");
        }
        ((TextView)findViewById(R.id.textView_PluginLibVersion)).setText("Built w/ PluginLib: " + PluginLibVersionInfo.PLUGINLIB_VERSION_STRING);
        ((TextView)findViewById(R.id.textView_PluginsPkgVersion)).setText("Installed Plugin Version: " + AntPluginPcc.getInstalledPluginsVersionString(this));
    */
    }

    //Launch the appropriate activity/action when a selection is made
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        int j=0;

        if(position == j++) {
            Intent i = new Intent(this, Activity_WatchScanList.class);
            startActivity(i);
        }
        else if(position == j++)
        {
            /**
             * Launches the ANT+ Plugin Manager. The ANT+ Plugin Manager provides access to view and modify devices
             * saved in the plugin device database and control default plugin settings. It is also available as a
             * stand alone application, but the ability to launch it from your own application is useful in situations
             * where a user wants extra convenience or doesn't already have the stand alone launcher installed. For example,
             * you could place this launch command in your application's own settings menu.
             */
            if(!AntPluginPcc.startPluginManagerActivity(this))
            {
                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(this);
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("This application requires the ANT+ Plugins, would you like to install them?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent startStore = null;
                        startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.dsi.ant.plugins.antplus"));
                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        Activity_Dashboard.this.startActivity(startStore);
                    }
                });
                adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                final AlertDialog waitDialog = adlgBldr.create();
                waitDialog.show();
            }
        }
        else
        {
            Toast.makeText(this, "This menu item is not implemented", Toast.LENGTH_SHORT).show();
        }
    }



    private void login() {
        Intent intent = StravaLogin.withContext(this)
                .withClientID(13966)
                .withRedirectURI("http://maaxrun.pythonanywhere.com")
                .withApprovalPrompt(AUTO)
                .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                .makeIntent();
        startActivityForResult(intent, RQ_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RQ_LOGIN && resultCode == RESULT_OK && data != null) {
            Log.d("Strava code", data.getStringExtra(StravaLoginActivity.RESULT_CODE));
            //Toast.makeText(this, data.getStringExtra(StravaLoginActivity.RESULT_CODE), Toast.LENGTH_LONG).show();
            new codeToToken().execute(data.getStringExtra(StravaLoginActivity.RESULT_CODE));

        }
    }


    /**
     * Sets the list display to the give adapter
     * @param adapter Adapter to set list display to
     */
    public void setListAdapter(ListAdapter adapter)
    {
        synchronized (this)
        {
            if (mList != null)
                return;
            mAdapter = adapter;
            mList = (ListView)findViewById(android.R.id.list);
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?>  parent, View v, int position, long id)
                {
                    onListItemClick((ListView)parent, v, position, id);
                }
            });
            mList.setAdapter(adapter);
        }
    }

    private class codeToToken extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... code) {
            AuthenticationConfig config = AuthenticationConfig.create()
                    .debug()
                    .build();
            AuthenticationAPI api = new AuthenticationAPI(config);
            LoginResult result = api.getTokenForApp(AppCredentials.with(13966, "a67b6efd42d941633fd631b35df2d22ae9b566c1"))
                    .withCode(code[0])
                    .execute();
            return result.getToken().toString();
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            //showDialog("Downloaded " + result + " bytes");
            Toast.makeText(Activity_Dashboard.this, result, Toast.LENGTH_SHORT).show();

            SharedPreferences sharedPref = Activity_Dashboard.this.getSharedPreferences(
                    getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.sharedPreferences), result);
            editor.commit();
            StravaConfig config = StravaConfig.withToken(result)
                        .debug()
                        .build();
             new async().execute(config);
           

        }
    }

    private class async extends AsyncTask<StravaConfig, Integer, Athlete> {
        protected Athlete doInBackground(StravaConfig... config) {

            AthleteAPI athleteAPI = new AthleteAPI(config[0]);
            Athlete athlete = null;
            try {
                athlete = athleteAPI.retrieveCurrentAthlete()
                        .execute();

            }catch(StravaUnauthorizedException e){
                Toast.makeText(Activity_Dashboard.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(Activity_Dashboard.this, "erreur", Toast.LENGTH_SHORT).show();
            }finally{
                if(athlete==null){
                    return null;
                }else{
                    return athlete;
                }
            }



        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Athlete result) {
            //Toast.makeText(Activity_Dashboard.this, "Athlete: "+ result.getFirstName(), Toast.LENGTH_SHORT).show();
            if(result==null){
                ((TextView)findViewById(R.id.textView_athlete)).setText("Need to reconnect");
            }else{
                ((TextView)findViewById(R.id.textView_athlete)).setText("Hello "+result.getFirstName()+" ! You are connected to Strava :-)");
                StravaLoginButton loginButton = (StravaLoginButton) findViewById(R.id.login_button);
                loginButton.setVisibility(View.GONE);
                //U+1F603 (emoji)
            }


        }
    }

}

