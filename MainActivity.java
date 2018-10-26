package com.example.android.staysaferesq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goToMapsActivity();



    }

    public void goToMapsActivity(){
        Intent i = new Intent(this,MapsActivity.class);

        startActivity(i);
    }
}
