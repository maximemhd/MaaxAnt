package io.github.maximemhd.maaxant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.common.FitFileCommon;
import com.sweetzpot.stravazpot.athlete.api.AthleteAPI;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginButton;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.api.exception.StravaAPIException;
import com.sweetzpot.stravazpot.common.api.exception.StravaUnauthorizedException;
import com.sweetzpot.stravazpot.upload.api.UploadAPI;
import com.sweetzpot.stravazpot.upload.model.DataType;
import com.sweetzpot.stravazpot.upload.model.UploadActivityType;
import com.sweetzpot.stravazpot.upload.model.UploadStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.maximemhd.maaxant.watchdownloader.Activity_WatchScanList;

public class UploadActivity extends AppCompatActivity {
ParamPost parampost_global;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ListView listView = (ListView) findViewById(R.id.list_files);
        Button button_check = (Button) findViewById(R.id.button_check);
        //List<File> listFitFile = new ArrayList<>();
        ArrayList<String> liste = new ArrayList<String>();

        if(isExternalStorageAvailable()){

           File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File[] files = dir.listFiles();
            //final File[] fitfiles;

            for (File file : files) {
                //Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
                if(java.util.Objects.equals(file.getName().substring(file.getName().lastIndexOf(".")+1), "fit")){
                    liste.add(file.getName());
                }

                //listFitFile.add(file);
            }
            // On transforme le tableau en une structure de données de taille variable


            listView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, liste));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // Clicking on items
                    //String filename = parent.getItemAtPosition(position).toString();
                    //toast filename avec string
                   // Toast.makeText(UploadActivity.this, "Activity read from External Storage..."+filename, Toast.LENGTH_LONG).show();

                    //avec tableau de files
                   // Toast.makeText(UploadActivity.this, "Activity read from External Storage..."+files[position].toString(), Toast.LENGTH_LONG).show();


                    SharedPreferences sharedPref = UploadActivity.this.getSharedPreferences(
                            getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
                    String token = sharedPref.getString(getString(R.string.sharedPreferences),"disconnected");
                    StravaConfig config = null;
                    if(token=="disconnected"){
                        Toast.makeText(UploadActivity.this, "Please connect to strava before...", Toast.LENGTH_LONG).show();
                    }
                    else {

                        config = StravaConfig.withToken(token)
                                .debug()
                                .build();
                        MyTaskParams param = new MyTaskParams(config,parent.getItemAtPosition(position).toString());
                        new async_upload().execute(param);
                    }

                }
            });

        }

        button_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new async_status().execute(parampost_global);
            }
        });

    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private class async_upload extends AsyncTask<MyTaskParams, Integer, ParamPost> {
        protected ParamPost doInBackground(MyTaskParams... config) {

            UploadAPI uploadAPI = new UploadAPI(config[0].config);
            UploadStatus uploadStatus  = null;
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File[] files = dir.listFiles();
            File file_up = null;

            for (File file : files) {
                //Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
                if (file.getName().equals(config[0].file)) {
                    file_up = file;
                }
            }


            try {
                uploadStatus = uploadAPI.uploadFile(file_up)
                        .withDataType(DataType.FIT)
                        .withActivityType(UploadActivityType.RUN)
                        .withName("Run activity")
                        .withDescription("")
                        .isPrivate(false)
                        .hasTrainer(false)
                        .isCommute(false)
                        .withExternalID("uploadActivity.fit")
                        .execute();
            }catch(StravaUnauthorizedException e){
                Toast.makeText(UploadActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(UploadActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            }finally{
                ParamPost parampost = new ParamPost(config[0].config, uploadStatus);
                return parampost;
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
            Toast.makeText(UploadActivity.this, "In upload...", Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(ParamPost result) {
            ParamPost param = new ParamPost(result.config, result.uploadstatus);
           // new async_status().execute(param);
            parampost_global = param;
            Toast.makeText(UploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

        }
    }
    private static class MyTaskParams {
        StravaConfig config;
        String file;

        MyTaskParams(StravaConfig config, String file) {
            this.config = config;
            this.file = file;

        }
    }

    private static class ParamPost {
        StravaConfig config;
        UploadStatus uploadstatus;

        ParamPost(StravaConfig config, UploadStatus uploadstatus) {
            this.config = config;
            this.uploadstatus = uploadstatus;

        }
    }



    private class async_status extends AsyncTask<ParamPost, Integer, String> {
        protected String doInBackground(ParamPost... config) {

            UploadAPI uploadAPI = new UploadAPI(config[0].config);
            UploadStatus uploadStatus  = null;

            try {
                 uploadStatus = uploadAPI.checkUploadStatus(config[0].uploadstatus.getId())
                        .execute();
            }catch(StravaUnauthorizedException e){
                Toast.makeText(UploadActivity.this, "erreur", Toast.LENGTH_SHORT).show();

            }catch(StravaAPIException e){
                Toast.makeText(UploadActivity.this, "erreur", Toast.LENGTH_SHORT).show();
            }finally{
                if(uploadStatus==null){
                    return "Erreur";
                }
                else
            return uploadStatus.getStatus();
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            Toast.makeText(UploadActivity.this, result, Toast.LENGTH_SHORT).show();

        }
    }

}
