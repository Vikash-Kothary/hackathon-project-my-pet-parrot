package com.robotpets.mypetparrot;

/**
 * Created by stevenhuang on 2/28/16.
 */
/**
 * Created by stevenhuang on 2/28/16.
 */
public class GlobalValues {
    public double drone_xloc=0;
    public double drone_yloc=0;


    private static GlobalValues ourInstance = new GlobalValues();

    public static GlobalValues getInstance() {
        return ourInstance;
    }

    private GlobalValues() {
    }
}