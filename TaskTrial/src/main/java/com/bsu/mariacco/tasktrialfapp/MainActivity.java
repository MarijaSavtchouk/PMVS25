package com.bsu.mariacco.tasktrialfapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
    }

    public void onclick(View v) {
        HavyTask mt = new HavyTask();
        mt.execute("url1", "url2", "url3");
    }
    public class HavyTask extends AsyncTask<String, Integer, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Long doInBackground(String... strings) {
            try {
                int cnt= 0;
                for(String url: strings) {
                    cnt++;
                    publishProgress(cnt);
                    TimeUnit.SECONDS.sleep(4);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Toast.makeText(context, "Processed:" + values[0], Toast.LENGTH_SHORT).show(); ;
        }
        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            Toast.makeText(context, "End", Toast.LENGTH_SHORT).show();
        }
    }
}
