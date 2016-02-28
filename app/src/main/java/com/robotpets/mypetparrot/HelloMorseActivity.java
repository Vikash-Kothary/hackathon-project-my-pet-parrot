package com.robotpets.mypetparrot;/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.Timer;
import java.util.Vector;

public class HelloMorseActivity extends AppCompatActivity {

    private TextView mLockStateView;
    private TextView mTextView;

    private int[] morse_code=new int[4];
    private int morse_ctr=0;
    private boolean typing=false;
    private long time_start,time_curr;
    private double init_z_angle=190;
    private double z_angle;
    private String msg;
    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            myo.unlock(Myo.UnlockType.HOLD);

        }
        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.

        }
        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {

        }
        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
        }
        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
//            mLockStateView.setText(R.string.unlocked);
        }
        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
//            mLockStateView.setText(R.string.locked);
        }
        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            time_curr=timestamp;
            if(typing && time_curr-time_start> 5000){
                Log.e("Times Up","");
                pushChar();
            }
            if(init_z_angle>180) {
                init_z_angle = Math.toDegrees(Quaternion.roll(rotation));
            }else {
                z_angle = Math.toDegrees(Quaternion.roll(rotation)) - init_z_angle;
            }
            Log.d("time", String.valueOf(timestamp));
            if(myo.isUnlocked()){
                Log.e("something", "content");
            }
        }
        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    break;
                case REST:
                case DOUBLE_TAP:

                    break;
                case FIST:
                    typing=true;
                    myo.vibrate(Myo.VibrationType.SHORT);
                    morse_code[morse_ctr] = Math.abs(z_angle)>5 ? 2 : 1;
                    time_start=timestamp;
                    Log.i("FIST", String.valueOf(morse_code[morse_ctr]));
                    if(morse_ctr==3){
                        pushChar();
                    }else morse_ctr++;

                    break;
                case WAVE_IN:

                    break;
                case WAVE_OUT:
                    break;
                case FINGERS_SPREAD:
                    break;
            }
        }
    };

    private char pushChar(){

        typing=false;
        morse_ctr=0;
        char[] table= {'|','E','T','I','A','N','M','S','U','R','W','D','K','G','O','H','V','F',' ','L',' ','P','J','B','X','C','Y','Z','Q',' ','|'};
        int ind=1;
        for(int i=0;i<4;i++){
            if(morse_code[i]==1) ind*=2;
            else if(morse_code[i]==2) ind=ind*2+1;
        }
        ind--;


        morse_code=new int[4];
        Log.e("letter", String.valueOf(table[ind]));

        //msg += String.valueOf(table[ind]);
        TextView output = (TextView) findViewById(R.id.textView4);

        output.setText(output.getText() + String.valueOf(table[ind]));


        return table[ind];

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_morse);

//        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        hub.setLockingPolicy(Hub.LockingPolicy.NONE);

        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

}
