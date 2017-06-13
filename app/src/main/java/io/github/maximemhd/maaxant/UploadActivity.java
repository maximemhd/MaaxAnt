package io.github.maximemhd.maaxant;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.common.FitFileCommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.maximemhd.maaxant.watchdownloader.Activity_WatchScanList;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ListView listView = (ListView) findViewById(R.id.list_files);

        //List<File> listFitFile = new ArrayList<>();
        ArrayList<String> liste = new ArrayList<String>();

        if(isExternalStorageAvailable()){

           File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File[] files = dir.listFiles();

            for (File file : files) {
                Toast.makeText(this, file.getName(), Toast.LENGTH_SHORT).show();
                liste.add(file.getName());
                //listFitFile.add(file);
            }
            // On transforme le tableau en une structure de données de taille variable


            listView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, liste));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // Clicking on items
                    String filename = parent.getItemAtPosition(position).toString();
                    //toast filename avec string
                    Toast.makeText(UploadActivity.this, "Activity read from External Storage..."+filename, Toast.LENGTH_LONG).show();

                    //avec tableau de files
                    Toast.makeText(UploadActivity.this, "Activity read from External Storage..."+files[position].toString(), Toast.LENGTH_LONG).show();

                }
            });


        }


    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


}
