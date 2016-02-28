package com.robotpets.mypetparrot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static String TAG = MainActivity.class.getSimpleName();

    static {
        try {
            System.loadLibrary("arsal");
            System.loadLibrary("arsal_android");
            System.loadLibrary("arnetworkal");
            System.loadLibrary("arnetworkal_android");
            System.loadLibrary("ardiscovery");
            System.loadLibrary("ardiscovery_android");
            System.loadLibrary("arcontroller");
            System.loadLibrary("arcontroller_android");

            ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_INFO);
        } catch (Exception e) {
            Log.e(TAG, "Oops (LoadLibrary)", e);
        }
    }

    public IBinder discoveryServiceBinder;
    private ListView listView;
    private List<ARDiscoveryDeviceService> deviceList;
    private String[] deviceNameList;
    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    private ARDiscoveryService ardiscoveryService;
    private boolean ardiscoveryServiceBound = false;
    private ServiceConnection ardiscoveryServiceConnection;
    private BroadcastReceiver ardiscoveryServicesDevicesListUpdatedReceiver;
    private Button btnMorse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        mVisible = true;
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        initBroadcastReceiver();
        initServiceConnection();

        listView = (ListView) findViewById(R.id.list);

        deviceList = new ArrayList<ARDiscoveryDeviceService>();
        deviceNameList = new String[]{};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);


        btnMorse = (Button) findViewById(R.id.btnMorse);

        btnMorse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMorseMyo();
            }
        });

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ARDiscoveryDeviceService service = deviceList.get(position);

                Intent intent = new Intent(MainActivity.this, PilotingActivity.class);
                intent.putExtra(PilotingActivity.EXTRA_DEVICE_SERVICE, service);


                startActivity(intent);
            }

        });
    }

    private void initServices() {
        if (discoveryServiceBinder == null) {
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            ardiscoveryService = ((ARDiscoveryService.LocalBinder) discoveryServiceBinder).getService();
            ardiscoveryServiceBound = true;

            ardiscoveryService.start();
        }
    }

    private void closeServices() {
        Log.d(TAG, "closeServices ...");

        if (ardiscoveryServiceBound) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ardiscoveryService.stop();

                    getApplicationContext().unbindService(ardiscoveryServiceConnection);
                    ardiscoveryServiceBound = false;
                    discoveryServiceBinder = null;
                    ardiscoveryService = null;
                }
            }).start();
        }
    }

    private void initBroadcastReceiver() {
        ardiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
    }

    private void initServiceConnection() {
        ardiscoveryServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                discoveryServiceBinder = service;
                ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
                ardiscoveryServiceBound = true;

                ardiscoveryService.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                ardiscoveryService = null;
                ardiscoveryServiceBound = false;
            }
        };
    }

    private void registerReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(ardiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));

    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.unregisterReceiver(ardiscoveryServicesDevicesListUpdatedReceiver);
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume ...");

        onServicesDevicesListUpdated();

        registerReceivers();

        initServices();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause ...");

        unregisterReceivers();
        closeServices();

        super.onPause();
    }

    @Override
    public void onServicesDevicesListUpdated() {
        Log.d(TAG, "onServicesDevicesListUpdated ...");

        List<ARDiscoveryDeviceService> list;

        if (ardiscoveryService != null) {
            list = ardiscoveryService.getDeviceServicesArray();

            deviceList = new ArrayList<ARDiscoveryDeviceService>();
            List<String> deviceNames = new ArrayList<String>();

            if (list != null) {
                for (ARDiscoveryDeviceService service : list) {
                    Log.e(TAG, "service :  " + service + " name = " + service.getName());
                    ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
                    Log.e(TAG, "product :  " + product);
                    // only display Rolling Spider drones
                    if ((ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_MINIDRONE.equals(product)) ||
                            (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK.equals(product)) ||
                            (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL.equals(product)) ||
                            (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT.equals(product)))

                    {
                        deviceList.add(service);
                        deviceNames.add(service.getName());
                    }
                }
            }

            deviceNameList = deviceNames.toArray(new String[deviceNames.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);

            // Assign adapter to ListView
            listView.setAdapter(adapter);
        }

    }

    public void launchMorseMyo(){
        Intent i = new Intent(this, HelloMorseActivity.class);
        startActivity(i);
    }
}

