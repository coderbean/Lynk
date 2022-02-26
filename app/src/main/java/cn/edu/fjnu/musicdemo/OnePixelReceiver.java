package cn.edu.fjnu.musicdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnePixelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //屏幕关闭启动1像素Activity
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("OnePx", "屏幕关闭启动1像素Activity");
            Intent it = new Intent(context, OnePxActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
            //屏幕打开 结束1像素
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("OnePx", "屏幕打开 结束1像素");
            context.sendBroadcast(new Intent("finish"));
        }
    }
}
