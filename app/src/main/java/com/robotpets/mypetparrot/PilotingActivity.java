package com.robotpets.mypetparrot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

import java.util.Vector;

public class PilotingActivity extends Activity implements ARDeviceControllerListener {

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
        for(int i=0;i<10;i++){
            commands.up(null,null);
        }
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
        commands.takeOff();
        Log.i("cmd","point");
        double target_xloc=4*java.lang.Math.tan(y_angle);
        double target_yloc=target_xloc/java.lang.Math.tan(x_angle);
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


    private static String TAG = PilotingActivity.class.getSimpleName();
    public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

    public ARDeviceController deviceController;
    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;

    private Button emergencyBt;
    private Button takeoffBt;
    private Button landingBt;

    private Button gazUpBt;
    private Button gazDownBt;
    private Button yawLeftBt;
    private Button yawRightBt;

    private Button forwardBt;
    private Button backBt;
    private Button rollLeftBt;
    private Button rollRightBt;

    private TextView batteryLabel;

    private AlertDialog alertDialog;

    private RelativeLayout view;

    private Commands commands;


    private static CommandsParcelable commandsParcelable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piloting);

        initIHM();

        Intent intent = getIntent();
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        //create the device
        try {
            device = new ARDiscoveryDevice();
            ARDiscoveryDeviceBLEService bleDeviceService = (ARDiscoveryDeviceBLEService) service.getDevice();
            ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());

            device.initBLE(product, getApplicationContext(), bleDeviceService.getBluetoothDevice());
        } catch (ARDiscoveryException e) {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getError());
        }


        if (device != null) {
            try {
                //create the deviceController
                deviceController = new ARDeviceController(device);
                deviceController.addListener(this);

                // Commands class
                commands = new Commands(deviceController);

            } catch (ARControllerException e) {
                e.printStackTrace();
            }

        }
    }

    private void initIHM() {
        view = (RelativeLayout) findViewById(R.id.piloting_view);

        emergencyBt = (Button) findViewById(R.id.emergencyBt);
        emergencyBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                commands.emergency();
            }
        });

        takeoffBt = (Button) findViewById(R.id.takeoffBt);
        takeoffBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                commands.takeOff();
            }
        });
        landingBt = (Button) findViewById(R.id.landingBt);
        landingBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                commands.landing();
            }
        });

        gazUpBt = (Button) findViewById(R.id.gazUpBt);
        gazUpBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.up(v, event);
            }
        });

        gazDownBt = (Button) findViewById(R.id.gazDownBt);
        gazDownBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.down(v, event);
            }
        });
        yawLeftBt = (Button) findViewById(R.id.yawLeftBt);
        yawLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.left(v, event);
            }
        });
        yawRightBt = (Button) findViewById(R.id.yawRightBt);
        yawRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.right(v, event);
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.forward(v, event);
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.back(v, event);
            }
        });
        rollLeftBt = (Button) findViewById(R.id.rollLeftBt);
        rollLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.rollLeft(v, event);
            }
        });
        rollRightBt = (Button) findViewById(R.id.rollRightBt);
        rollRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return commands.roolRight(v, event);
            }
        });

        batteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    @Override
    public void onStart() {
        super.onStart();

        //start the deviceController
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Connecting ...");
            alertDialogBuilder.setCancelable(false);


            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ARCONTROLLER_ERROR_ENUM error = deviceController.start();

            if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                finish();
            }
        }
    }

    private void stopDeviceController() {
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");

            // show it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ARCONTROLLER_ERROR_ENUM error = deviceController.stop();

                    if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                        finish();
                    }
                }
            });
            //alertDialog.show();

        }
    }

    @Override
    protected void onStop() {
        if (deviceController != null) {
            deviceController.stop();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        stopDeviceController();
    }

    public void onUpdateBattery(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });

    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        Log.i(TAG, "onStateChanged ... newState:" + newState + " error: " + error);

        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                //The deviceController is started
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_RUNNING .....");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                    }
                });
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                //The deviceController is stoped
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_STOPPED .....");

                deviceController.dispose();
                deviceController = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                        finish();
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {
        // Nothing to do here since we don't want to connect to the Bebop through a SkyController
    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary != null) {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {

                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);

                    onUpdateBattery(batValue);
                }
            }
        } else {
            Log.e(TAG, "elementDictionary is null");
        }
    }
}
