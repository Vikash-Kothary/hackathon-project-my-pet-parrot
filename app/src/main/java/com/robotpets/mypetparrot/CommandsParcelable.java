package com.robotpets.mypetparrot;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


/**
 * Created by Vikash Kothary on 28-Feb-16.
 */
public class CommandsParcelable implements  Serializable{

    public Commands getCommands() {
        return mCommands;
    }

    public void setCommands(Commands mCommands) {
        this.mCommands = mCommands;
    }

    private Commands mCommands;

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
