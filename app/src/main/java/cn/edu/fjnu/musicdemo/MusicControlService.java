package cn.edu.fjnu.musicdemo;

import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

public class MusicControlService implements MediaSessionManager.OnActiveSessionsChangedListener {

    final String TAG = "MusicControlService";

//    @Override
//    public void onCreate() {
//        Log.i(TAG, "音乐推送服务已启动");
//    }
//
//
//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn) {
//        Log.i(TAG, "收到通知");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstData.BroadCastMsg.NOTIFY_POSTED));
//    }
//
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i(TAG, "移除通知");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstData.BroadCastMsg.NOTIFY_REMOVED));
//    }

    @Override
    public void onActiveSessionsChanged(@Nullable List<MediaController> list) {

    }
}
