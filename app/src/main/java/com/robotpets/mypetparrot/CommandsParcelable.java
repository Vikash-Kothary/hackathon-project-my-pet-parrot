package com.robotpets.mypetparrot;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Vikash Kothary on 28-Feb-16.
 */
public class CommandsParcelable implements Parcelable {

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

    public static final Creator<CommandsParcelable> CREATOR = new Creator<CommandsParcelable>() {
        @Override
        public CommandsParcelable createFromParcel(Parcel in) {
            return new CommandsParcelable(in);
        }

        @Override
        public CommandsParcelable[] newArray(int size) {
            return new CommandsParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
