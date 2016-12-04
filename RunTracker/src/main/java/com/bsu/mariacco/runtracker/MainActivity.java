package com.bsu.mariacco.runtracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static int DEFAULT_RADIUS = 150;

    public static int MAX_DISTANCE = DEFAULT_RADIUS / 2;
    public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private LocationManager locationManager;
    private PendingIntent locationListenerPendingIntent;
    private TextView textViewStatus;
    private TextView textViewLatitude;
    private TextView textViewLongitude;
    private TextView textViewAltitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Intent activeIntent = new Intent(this, LocationChangedReceiver.class);
        locationListenerPendingIntent = PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        textViewStatus = (TextView)findViewById(R.id.textView_status);
        textViewLatitude = (TextView)findViewById(R.id.textView_latitude);
        textViewLongitude = (TextView)findViewById(R.id.textView_longitude);
        textViewAltitude = (TextView)findViewById(R.id.textView_altitude);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        textViewLatitude.setText(" " + location.getLatitude());
        textViewLongitude.setText(" " + location.getLongitude());
        textViewAltitude.setText(""+location.getAltitude());
    }

    public void onClickStart(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListenerPendingIntent);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListenerPendingIntent);
        textViewStatus.setText(getString(R.string.started));
    }

    public void onClickStop(View view){
        locationManager.removeUpdates(locationListenerPendingIntent);
        textViewStatus.setText(getString(R.string.not_started));
    }

    public class LocationChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            Location loc = (Location)b.get(android.location.LocationManager.KEY_LOCATION_CHANGED);
            textViewLatitude.setText(""+loc.getLatitude());
            textViewLongitude.setText(""+loc.getLongitude());
            textViewAltitude.setText(""+loc.getAltitude());
            Toast.makeText(context, loc.toString(), Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Пора покормить кота!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}