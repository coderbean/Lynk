package cn.edu.fjnu.musicdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdateWidgetService extends Service {


    public UpdateWidgetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}