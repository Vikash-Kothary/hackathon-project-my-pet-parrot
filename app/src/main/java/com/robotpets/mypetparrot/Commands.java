package com.robotpets.mypetparrot;


import android.test.MoreAsserts;
import android.view.MotionEvent;
import android.view.View;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARDeviceController;

/**
 * Created by Robert on 28/02/2016.
 */
public class Commands {

    ARDeviceController deviceController;

    public Commands(ARDeviceController deviceController){
        this.deviceController = deviceController;
    }


    public void emergency(){
        if ((deviceController != null) && (deviceController.getFeatureMiniDrone() != null)) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureMiniDrone().sendPilotingEmergency();
        }
    }

    public void takeOff(){
        if ((deviceController != null) && (deviceController.getFeatureMiniDrone() != null)) {

            //send takeOff
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureMiniDrone().sendPilotingTakeOff();
        }
    }

    public void landing(){
        if ((deviceController != null) && (deviceController.getFeatureMiniDrone() != null)) {
            //send landing
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureMiniDrone().sendPilotingLanding();
        }
    }

    public boolean up(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDGaz((byte) 50);
                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDGaz((byte) 0);

                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean down(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDGaz((byte) -50);

                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDGaz((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean left(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte) -50);

                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean right(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte) 50);

                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDYaw((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean forward(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDPitch((byte) 50);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 1);
                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDPitch((byte) 0);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean back(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDPitch((byte) -50);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 1);
                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDPitch((byte) 0);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean rollLeft(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDRoll((byte) -50);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 1);
                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDRoll((byte) 0);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }

    public boolean roolRight(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDRoll((byte) 50);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 1);
                }
                break;

            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (deviceController != null) {
                    deviceController.getFeatureMiniDrone().setPilotingPCMDRoll((byte) 0);
                    deviceController.getFeatureMiniDrone().setPilotingPCMDFlag((byte) 0);
                }
                break;

            default:

                break;
        }

        return true;
    }


}
