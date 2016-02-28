package com.robotpets.mypetparrot;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


/**
 * Created by Vikash Kothary on 28-Feb-16.
 */
public class CommandsParcelable {

    public static Commands getCommands() {
        return mCommands;
    }

    private static Commands mCommands;

    public CommandsParcelable(Commands commands){
        mCommands = commands;
    }

    protected CommandsParcelable(Parcel in) {
    }

    public static final Parcelable.Creator<CommandsParcelable> CREATOR = new Parcelable.Creator<CommandsParcelable>() {
        @Override
        public CommandsParcelable createFromParcel(Parcel in) {
            return new CommandsParcelable(in);
        }

        @Override
        public CommandsParcelable[] newArray(int size) {
            return new CommandsParcelable[size];
        }
    };


}
