package cn.edu.fjnu.musicdemo;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    private static Context mContext;
    private int playStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.playStatus = playStatus;
    }
}