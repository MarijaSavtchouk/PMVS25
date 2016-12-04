package com.bsu.mariacco.lastfmapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    EditText artistName;
    Context context;
    DBHelper dbHelper;
    private String dbName = "dbName";
    private String tableName ="artist";
    private String artistNameCol = "artist_name";
    private String trackNameCol = "track_name";
    private String listeners = "listeners";
    private String playcount = "playcount";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        dbHelper = new DBHelper(this);
        artistName = (EditText)findViewById(R.id.editText_artist);
    }

    public void onClickLoad(View v) {

        String findName = artistName.getText().toString();
        if(findName.equals(""))
        {
            return;
        }
        if(!hasConnection(context)){
            Toast.makeText(context, "noConnection", Toast.LENGTH_SHORT).show();
        }
        LoadTask mt = new LoadTask();

        String queryString = "http://ws.audioscrobbler.com/2.0/?method="+getString(R.string.method_top)+"&artist="+findName+"&limit=10"+"&api_key="+getString(R.string.api_key);
        mt.execute(queryString);
        try {
            List<Track> list = mt.get();
            if(list.size()==0){
                Toast.makeText(context, "No artist", Toast.LENGTH_SHORT).show();
            }
            else
            {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(tableName, artistNameCol+" = ?", new String[]{findName});
                dbHelper.close();

                db = dbHelper.getWritableDatabase();

                for(Track track : list){
                    track.track = track.track.replaceAll("[\\(\\)\'\"]", "");
                    db.execSQL("insert into "+tableName+" ("+artistNameCol+", "+playcount+", "+listeners+", "+trackNameCol+") VALUES ('"+findName+"', '"+track.playCount+"', '"+track.listeners+"', '"+track.track+"')");
                }
                dbHelper.close();
                Toast.makeText(context, "Data uploaded", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }
    private void showWithQuery(Cursor c)
    {
        ArrayList<String> values = new ArrayList<>();
        if (c.moveToFirst()) {

            int nameColIndex = c.getColumnIndex(artistNameCol);
            int trackColIndex = c.getColumnIndex(trackNameCol);
            int listenersColIndex = c.getColumnIndex(listeners);
            int playColIndex = c.getColumnIndex(playcount);
            do{
                values.add("Имя: " + c.getString(nameColIndex) +
                        ", Песня: " + c.getString(trackColIndex) +
                        ", Прослушало пользователей = " + c.getString(listenersColIndex)
                        + ", Прослушано раз = " + c.getString(playColIndex)
                );
            } while (c.moveToNext());
        }
        c.close();
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("values", values);
        startActivity(intent);
    }

    public void onClickShowAll(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(tableName, null, null, null, null, null, null);
        showWithQuery(c);
        dbHelper.close();
    }

    public void onClickShow(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<String> values = new ArrayList<>();
        Cursor c = db.query(tableName, null, artistNameCol+" = ?", new String[]{artistName.getText().toString()}, null, null, null);
        showWithQuery(c);
        dbHelper.close();
    }
    public class LoadTask extends AsyncTask<String, String, List<Track>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Track> doInBackground(String... strings) {
            List<Track> list = new ArrayList<Track>();
            try {
                    URL url = new URL(strings[0]);
                    URLConnection conn = url.openConnection();

                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(conn.getInputStream());
                    doc.getDocumentElement().normalize();
                    NodeList error = doc.getElementsByTagName("error");
                    if(error.getLength()!=0){
                        return list;
                    }
                    NodeList tracks = doc.getElementsByTagName("track");
                    for (int j = 0; j < tracks.getLength(); ++j) {
                        Element condition = (Element) tracks.item(j);
                        String nameText = condition.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                        String playcount =  condition.getElementsByTagName("playcount").item(0).getFirstChild().getNodeValue();
                        String listeners =  condition.getElementsByTagName("listeners").item(0).getFirstChild().getNodeValue();
                        Track track = new Track();
                        track.track = nameText;
                        track.playCount = playcount;
                        track.listeners = listeners;
                        list.add(track);
                    }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            return list;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(context, "Processed:" + values[0], Toast.LENGTH_SHORT).show(); ;
        }

        @Override
        protected void onPostExecute(List<Track> result) {
            super.onPostExecute(result);
            Toast.makeText(context, "End", Toast.LENGTH_SHORT).show();
        }
    }
    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, dbName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table "+tableName +" (id integer primary key autoincrement, artist_name text, track_name text, listeners integer, playcount integer);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int
                newVersion) {
        }
    }

}
