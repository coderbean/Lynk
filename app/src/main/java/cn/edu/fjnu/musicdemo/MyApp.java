package cn.edu.fjnu.musicdemo;

import android.app.Application;
import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;

public class MyApp extends Application {
    private static Context mContext;
    private int playStatus;
    private boolean serviceRunning = false;

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

    public boolean isServiceRunning() {
        return serviceRunning;
    }

    public void setServiceRunning(boolean serviceRunning) {
        this.serviceRunning = serviceRunning;
    }
}
