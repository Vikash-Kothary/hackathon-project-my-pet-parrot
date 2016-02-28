package com.robotpets.mypetparrot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math;
import java.util.Vector;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

/**
 * Created by stevenhuang on 2/27/16.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.Vector;

public class MyoActivity extends ActionBarActivity {

    private TextView mLockStateView;
    private TextView mTextView;

    private double init_x_angle=190;
    private double init_y_angle;
    private double init_z_angle;

    private double x_angle;
    private double y_angle;
    private double z_angle;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
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
            myo.unlock(Myo.UnlockType.HOLD);
        }
        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            init_x_angle=0;
            init_y_angle=0;
            init_z_angle=0;
        }
        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText("unlocked");
        }
        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText("locked");
        }
        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            if(init_x_angle>180){
                init_y_angle = Math.toDegrees(Quaternion.pitch(rotation));
                init_x_angle = Math.toDegrees(Quaternion.yaw(rotation));
                init_z_angle = Math.toDegrees(Quaternion.roll(rotation));

                String s = init_x_angle + ", " + init_y_angle + ", " + init_z_angle;
                Log.i("init", s);
            }else {
                y_angle = Math.toDegrees(Quaternion.pitch(rotation)) - init_y_angle;
                x_angle = Math.toDegrees(Quaternion.yaw(rotation)) - init_x_angle;
                z_angle = Math.toDegrees(Quaternion.roll(rotation)) - init_z_angle;

                String s = x_angle + ", " + y_angle + ", " + z_angle;
                Log.d("orientation", s);
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
                    //Log.i("tap","a");
                    //bark();
                    break;
                case FIST:
                    Log.i("fist_data", String.valueOf(z_angle));
                    if (Math.abs(z_angle) > 15) rollover();
                    else pointAndGo();
                    break;
                case WAVE_IN:
                    Log.i("in_data", String.valueOf(z_angle));
                    if (z_angle > 8) sit();
                    else if (z_angle < -8) come();
                    break;
                case WAVE_OUT:
                    Log.i("out_data", String.valueOf(z_angle));
                    if (z_angle > 8) stay();
                    else idle();
                    break;
                case FINGERS_SPREAD:
                    chaseTail();
                    break;
            }
        }
    };
    private Commands commands;

    private Vector idle(){
        Vector ret= new Vector();

        double x=(Math.random()-.5) * 3;
        double y=(Math.random()-.5) * 3;

        //updating with predetermined location
        GlobalValues.getInstance().drone_yloc += y;
        GlobalValues.getInstance().drone_xloc += x;
        Log.i("loc","IDLE vector: ("+x+", "+y+") loc: ("+GlobalValues.getInstance().drone_xloc+", "
                +GlobalValues.getInstance().drone_yloc+")");

        return ret;
    }
    private Vector come(){
        Log.i("cmd","come");
        Vector ret= new Vector();
        double xloc=-GlobalValues.getInstance().drone_xloc;
        double yloc=-GlobalValues.getInstance().drone_yloc;

        //updating with predetermined location
        GlobalValues.getInstance().drone_yloc=0;
        GlobalValues.getInstance().drone_xloc=0;

        Log.i("loc","COME vector: ("+xloc+", "+yloc+") loc: ("+GlobalValues.getInstance().drone_xloc+", "
                +GlobalValues.getInstance().drone_yloc+")");
        return ret;
    }
    private void stay(){
        Log.i("cmd","stay");
    }
    private void sit(){
        Log.i("cmd","sit");
        commands.landing();
    }
    private void bark(){
        Log.i("cmd","bark");
    }
    private void rollover(){
        Log.i("cmd","rollover");
    }
    private void chaseTail(){
        Log.i("cmd","chaseTail");
    }
    private Vector pointAndGo(){
        Log.i("cmd","point");
        double target_xloc=-4*java.lang.Math.tan(Math.toRadians(x_angle));
        double target_yloc=4;
        double xloc=target_xloc - GlobalValues.getInstance().drone_xloc;
        double yloc=target_yloc - GlobalValues.getInstance().drone_yloc;

        //updating with predetermined location
        GlobalValues.getInstance().drone_yloc=target_yloc;
        GlobalValues.getInstance().drone_xloc=target_xloc;
        Vector ret=new Vector();

        Log.i("loc","GO vector: ("+xloc+", "+yloc+") loc: ("+GlobalValues.getInstance().drone_xloc+", "
                +GlobalValues.getInstance().drone_yloc+")");

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commands = ((CommandsParcelable) getIntent().getParcelableExtra("Commands")).getCommands();

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
