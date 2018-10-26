package com.example.android.staysaferesq;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SQLQuery sqlQuery;
    ArrayList<UserSafe> al;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getApplicationContext(),"Loading",Toast.LENGTH_SHORT);
        mMap = googleMap;
        ArrayList<LatLng> ll_safe=new ArrayList<>();
        ArrayList<LatLng> ll_unsafe=new ArrayList<>();
        FloatingActionButton fab1=(FloatingActionButton) findViewById(R.id.floatingActionButton_face);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),FaceActivity.class);
//                i.putExtra("phoneno1")
                startActivity(i);
            }
        });
        if (isInternetAvailable()) {
            sqlQuery = new SQLQuery();// this is the Asynctask, which is used to process in background to reduce load on app process
            try {
                String ssss= sqlQuery.execute("").get();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            catch (ExecutionException e){
                e.printStackTrace();
            }

//            while (!sqlQuery.done);
//            String str_result= new RunInBackGround().execute().get();
            al=sqlQuery.getUsers();

        }
        else {
            Log.d("nointernet", "nointernet");
            Toast.makeText(this,"Check your Internet connection",Toast.LENGTH_LONG);
        }
//        try{
//            TimeUnit.SECONDS.sleep(1);
//        }
//        catch (InterruptedException e){
//            e.printStackTrace();
//        }
        Log.v("alsize",Integer.toString(al.size()));
        if(al.size()>0){
            Log.v("startdata","Startdata");
             ll_safe=new ArrayList<>();
             ll_unsafe=new ArrayList<>();
            for(UserSafe x : al){
                Log.v("checkdbval",x.flag);
                if(x.flag.equals("Y")||x.flag.equals("y")) {
                    ll_safe.add(new LatLng(x.lat, x.lon));
                    Log.v("trueif", "ifistrue");
                }
                else
                    ll_unsafe.add(new LatLng(x.lat, x.lon));
            }

        }


        // Add a marker in Sydney and move the camera
        LatLng indiaCentre = new LatLng(23.843171, 79.821223);
        for(LatLng point: ll_safe){
            MarkerOptions mkr = new MarkerOptions().position(point).title("Safe").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//            mkr.icon(BitmapDescriptorFactory.fromResource(R.drawable.set_safe));
            mMap.addMarker(mkr);
            Log.v("addpoint1",point.toString());
            Log.v("check1",point.toString()+"lll");
        }

        for(LatLng point: ll_unsafe){
            mMap.addMarker(new MarkerOptions().position(point).title("Unsafe"));
//            Log.v("plotsafe")
            Log.v("addpoint",point.toString());
            Log.v("check1",point.toString()+"kkkk");
        }

//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(indiaCentre));
    }


    public boolean isInternetAvailable() {

        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        if(!isAvailable)Toast.makeText(this,"No Internet Connection. Sending an SMS!",Toast.LENGTH_LONG).show();
        return isAvailable;

    }
}
