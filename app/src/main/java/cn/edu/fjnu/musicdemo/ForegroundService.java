package cn.edu.fjnu.musicdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service implements MediaSessionManager.OnActiveSessionsChangedListener {
    public static final String APPLE_MUSIC_PKG_NAME = "com.apple.android.music";

    private NotificationManager notificationManager;
    private static final String NOTIFICATION_ID = "广播转换通知后台服务";
    private static final String NOTIFICATION_NAME = "channedId";

    private static final String TAG = "ForegroundService";

    private String currSongHash = Long.toString(System.currentTimeMillis());

    private MediaSessionManager mediaSessionManager;

    private ComponentName mNotifyReceiveService;

    private NotifyReceiver mNotifyReceiver = new NotifyReceiver();

    private Handler mHandler = new Handler();

    private MusicActionReceiver musicActionReceiver = new MusicActionReceiver();


    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        registerListener();
        registerListener();
        loadMusicControlAdapter();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (((MyApp)getApplication()).getPlayStatus() == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d("ForegroundService", "send local msg");
                    loadMusicControlAdapter();
                }
            }
        }, 0L, 800L);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1,getNotification());
        ((MyApp)getApplication()).setServiceRunning(true);
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("媒体广播转换服务")
                .setContentText("正在运行...");
        //设置Notification的ChannelID,否则不能正常显示
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_ID);
        }
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        ((MyApp)getApplication()).setServiceRunning(false);
        mHandler.removeCallbacksAndMessages(null);
        stopForeground(true);
    }
    private void initData() {
        if (Build.VERSION.SDK_INT >= 21)
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mNotifyReceiveService = new ComponentName(this, MusicControlService.class);
    }
    /**
     * 加载音乐控制页面
     */
    private void loadMusicControlAdapter() {
        Log.d(TAG, "loadMusicControlAdapter()-foregroundService");
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                if (mediaControllers.size() > 0) {
                    List<MusicInfo> musicInfos = new ArrayList<>();
                    for (MediaController controller : mediaControllers) {
                        MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                        MusicInfo itemMusicInfo = new MusicInfo();
                        String pkgName = controllerCompat.getPackageName();
                        if (!APPLE_MUSIC_PKG_NAME.equals(pkgName)) {
                            continue;
                        }
                        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(pkgName, 0);
                        itemMusicInfo.setAppName(applicationInfo.loadLabel(getPackageManager()).toString());
                        itemMusicInfo.setPkgName(pkgName);
                        PlaybackStateCompat playbackStateCompat = controllerCompat.getPlaybackState();
                        itemMusicInfo.setMusicState(playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING);
                        MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
                        // android.media.metadata.DURATION 毫秒值
                        itemMusicInfo.setProgress(controller.getPlaybackState().getPosition());
                        ((MyApp)getApplication()).setPlayStatus(controller.getPlaybackState().getState());


                        if (mediaMetadataCompat != null) {
                            itemMusicInfo.setDuration(mediaMetadataCompat.getLong("android.media.metadata.DURATION"));
                            MediaDescriptionCompat descriptionCompat = mediaMetadataCompat.getDescription();
                            if (descriptionCompat != null) {
                                CharSequence musicTitle = descriptionCompat.getTitle();
                                // 封面
                                itemMusicInfo.setAlbum(descriptionCompat.getIconBitmap());
                                itemMusicInfo.setAlbumUrl(descriptionCompat.getIconUri().toString());
                                // 专辑名称
                                if (!TextUtils.isEmpty(descriptionCompat.getDescription()))
                                    itemMusicInfo.setAlbumTitle(descriptionCompat.getDescription().toString());
                                // 歌曲名称
                                if (!TextUtils.isEmpty(musicTitle))
                                    itemMusicInfo.setTitle(musicTitle.toString());

                                // 歌手
                                if (!TextUtils.isEmpty(descriptionCompat.getSubtitle()))
                                    itemMusicInfo.setSinger(descriptionCompat.getSubtitle().toString());
                            }
                        }
                        musicInfos.add(itemMusicInfo);
                    }

                    if (musicInfos.size() > 0) {
                        MusicInfo musicInfo = musicInfos.get(0);
                        Intent intent = new Intent();
                        intent.setAction("com.hyphp.playkeytool.service");
                        // 表示是同一个歌曲
                        String newSongHash = musicInfo.hashSong();
                        if (Objects.equals(currSongHash, newSongHash)) {
                            intent.putExtra("method", "updatepos");
                            intent.putExtra("pos", Long.toString(musicInfo.getProgress()));
                        } else {
                            currSongHash = newSongHash;
                            intent.putExtra("method", "dashboard");
                            intent.putExtra("getTrackName", musicInfo.getTitle());
                            intent.putExtra("getAlbumName", musicInfo.getAlbumTitle());
                            intent.putExtra("getArtistName", musicInfo.getSinger());
                            intent.putExtra("getDuration", musicInfo.getDuration().toString());
                            intent.putExtra("getArtwork", musicInfo.getAlbumUrl());
                        }
                        sendBroadcast(intent);
                        Log.d("broadcast", MyUtils.printBroadCast(intent));
                    }
//                    mRvMusicBrowser.setAdapter(new ControlAdapter(this, musicInfos, this));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        loadMusicControlAdapter();
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_POSTED);
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_REMOVED);
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_REFRESH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotifyReceiver, notifyFilter);
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                mediaSessionManager.addOnActiveSessionsChangedListener(this, mNotifyReceiveService);
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                for (MediaController controller : controllers) {
                    MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                    controllerCompat.registerCallback(mediaCompactCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        IntentFilter musicActionFilter = new IntentFilter();
        musicActionFilter.addAction("com.android.music.metachanged");
        musicActionFilter.addAction("com.android.music.playstatechanged");
        musicActionFilter.addAction("com.android.mediacenter.metachanged");
        musicActionFilter.addAction("com.android.mediacenter.playstatechanged");
        musicActionFilter.addAction("com.oppo.music.service.meta_changed");
        musicActionFilter.addAction("com.oppo.music.service.playstate_changed");
        musicActionFilter.addAction("com.miui.player.metachanged");
        musicActionFilter.addAction("com.miui.player.queuechanged");
        registerReceiver(musicActionReceiver, musicActionFilter);
    }

    /**
     * 取消注册监听
     */
    private void unRegisterListener() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(musicActionReceiver);
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(this);
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                for (MediaController controller : controllers) {
                    MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                    controllerCompat.unregisterCallback(mediaCompactCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private MediaControllerCompat.Callback mediaCompactCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            Log.i(TAG, "onSessionReady");
            processNotify();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            //播放状态发生改变
            // 状态列表 https://www.apiref.com/android-zh/android/support/v4/media/session/PlaybackStateCompat.html#STATE_NONE
            ((MyApp)getApplication()).setPlayStatus(state.getState());
            loadMusicControlAdapter();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            //播放内容发生改变
            if (metadata != null) {
                loadMusicControlAdapter();
            }
        }
    };


    class NotifyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "NotifyReceiver->收到广播:" + intent.getAction());
            processNotify();
        }
    }
    /**
     * 处理通知
     */
    private void processNotify() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMusicControlAdapter();
            }
        }, 500);
    }

    class MusicActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "MusicActionReceiver->收到广播:" + intent.getAction());
            processNotify();
        }
    }

}
