package cn.edu.fjnu.musicdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (((MyApp)getApplication()).getPlayStatus() == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d("ForegroundService", "send local msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ConstData.BroadCastMsg.NOTIFY_REFRESH));
                }
            }
        }, 0L, 800L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
