package com.bsu.mariacco.coordsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    TextView tvOut;
    TextView tvLon;
    TextView tvLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvOut = (TextView) findViewById(R.id.textViewOut);
        tvLon = (TextView) findViewById(R.id.textViewLon);
        tvLat = (TextView) findViewById(R.id.textViewLat);
        final LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Context context = getApplicationContext();
        LocationListener mlocListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), "Ch", Toast.LENGTH_SHORT).show();

                tvLat.setText(" " + location.getLatitude());
                tvLon.setText(" " + location.getLongitude());
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                Toast.makeText(getApplicationContext(), provider, Toast.LENGTH_SHORT).show();
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), provider, Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = mlocManager.getLastKnownLocation(provider);
                tvLat.setText(" " + location.getLatitude());
                tvLon.setText(" " + location.getLongitude());
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(getApplicationContext(), provider, Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, mlocListener);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, mlocListener);
        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvOut.setText("GPS is turned on...");
        } else {
            tvOut.setText("GPS is not turned on...");
        }
    }
}
