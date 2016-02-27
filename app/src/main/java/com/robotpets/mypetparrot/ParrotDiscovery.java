package com.robotpets.mypetparrot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

/**
 * Created by Vikash Kothary on 27-Feb-16.
 */
public class ParrotDiscovery implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

    private Context mContext;
    private ARDiscoveryService mDiscoveryService;
    private ServiceConnection mDiscoveryServiceConnection;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mDiscoveryDevicesUpdatedReceiver;
    private String TAG = "com.robotpets.mypetparrot";

    public ParrotDiscovery(Context context) {
        mContext = context;
        ARSDK.loadSDKLibs();
        initDiscoveryService();
    }

    private void initDiscoveryService() {
        // create the service connection
        if (mDiscoveryServiceConnection == null) {
            mDiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mDiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();

                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mDiscoveryService = null;
                }
            };
        }

        if (mDiscoveryService == null) {
            // if the discovery service doesn't exists, bind to it
            Intent i = new Intent(mContext, ARDiscoveryService.class);
            mContext.bindService(i, mDiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mDiscoveryService != null) {
            mDiscoveryService.start();
        }
    }

    private void registerReceivers() {
        mDiscoveryDevicesUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(mContext);
        localBroadcastMgr.registerReceiver(mDiscoveryDevicesUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated() {
        Log.d(TAG, "onServicesDevicesListUpdated ...");

        if (mDiscoveryService != null) {
            List<ARDiscoveryDeviceService> deviceList = mDiscoveryService.getDeviceServicesArray();

            // Do what you want with the device list
        }
    }

    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service) {
        ARDiscoveryDevice device = null;
        if ((service != null) &&
                (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))) {
            try {
                device = new ARDiscoveryDevice();

                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

                device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
            } catch (ARDiscoveryException e) {
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.getError());
            }
        }

        return device;
    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(mContext);

        localBroadcastMgr.unregisterReceiver(mDiscoveryDevicesUpdatedReceiver);
    }

    private void closeServices() {
        Log.d(TAG, "closeServices ...");

        if (mDiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDiscoveryService.stop();

                    mContext.unbindService(mDiscoveryServiceConnection);
                    mDiscoveryService = null;
                }
            }).start();
        }
    }
}
