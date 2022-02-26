package cn.edu.fjnu.musicdemo;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements MediaSessionManager.OnActiveSessionsChangedListener, OnControlClick {
    private static final int REQUEST_DIALOG_PERMISSION = 1002;
    final String TAG = "MainActivity";
    private RecyclerView mRvMusicBrowser;
    private NotifyReceiver mNotifyReceiver = new NotifyReceiver();
    private MusicActionReceiver musicActionReceiver = new MusicActionReceiver();
    private Handler mHandler = new Handler();
    private MediaSessionManager mediaSessionManager;
    private ComponentName mNotifyReceiveService;
    private long progress = 0L;
    private int playStat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        if(!checkFloatPermission(getApplicationContext())){
            //权限请求方法
            requestSettingCanDrawOverlays();
        }
        onePiexlInit();
        initScheduler();
    }

    /**
     * 注册广播
     */
    private void onePiexlInit() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new OnePixelReceiver(), intentFilter);
    }

    /**
     * 初始化定时任务
     */
    private void initScheduler() {
        final Handler handler = new Handler();
        final MainActivity controlClick = this;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                long interval = 200L;
                try {
                    if (playStat == PlaybackStateCompat.STATE_PLAYING) {
                        progress += (interval + 30);
                        loadMusicControlAdapter(true);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    //also call the same runnable to call it at regular interval
                    handler.postDelayed(this, interval);
                }
            }
        };
        handler.post(runnable);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkNotificationPermission();
        registerListener();
        loadMusicControlAdapter();
        moveTaskToBack(false);
        Toast.makeText(this, "媒体广播转换启动成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
//        progress = 0L;
        loadMusicControlAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterListener();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
//        switch (view.getId()) {
//            case R.id.iv_last_music:
//                lastMusic((MusicInfo) view.getTag());
//                break;
//            case R.id.iv_next_music:
//                nexMusic((MusicInfo) view.getTag());
//                break;
//            case R.id.iv_play_pause:
//                playOrPause((MusicInfo) view.getTag());
//                break;
//        }
    }

    private void nexMusic(MusicInfo musicInfo) {
        if (isUseAudioManagerKey(musicInfo)) {
            Log.i(TAG, "使用audioManger key方式");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
         /*Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         downIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
         sendOrderedBroadcast(downIntent, null);

         Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         upIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
         sendOrderedBroadcast(upIntent, null);*/

         /*new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
            }
        }).start();*/

        } else {
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            if (controllerCompat != null) {
                Log.i(TAG, "nextMusic->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if (!isSucc) {
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if (transportControls != null)
                        transportControls.skipToNext();
                }
            }
        }
    }


    private void lastMusic(MusicInfo musicInfo) {
        if (isUseAudioManagerKey(musicInfo)) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        } else {
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            if (controllerCompat != null) {
                Log.i(TAG, "lastMusic->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if (!isSucc) {
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if (transportControls != null)
                        transportControls.skipToPrevious();
                }
            }
        }

    }


    private void playOrPause(MusicInfo musicInfo) {
        if (isUseAudioManagerKey(musicInfo)) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        } else {
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            boolean isPlay = musicInfo.isMusicState();
            if (controllerCompat != null) {
                Log.i(TAG, "playOrPause->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if (!isSucc) {
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if (transportControls != null) {
                        if (isPlay)
                            transportControls.pause();
                        else
                            transportControls.play();
                    }

                }
            }
        }

    }


    private boolean isUseAudioManagerKey(MusicInfo musicInfo) {
        switch (musicInfo.getPkgName()) {
            case "com.tencent.qqmusic":
                return true;
        }
        return false;
    }


    public MediaControllerCompat findMediaControl(MusicInfo musicInfo) {
        try {
            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
            for (MediaController controller : mediaControllers) {
                MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                if (musicInfo.getPkgName().equals(controllerCompat.getPackageName()))
                    return controllerCompat;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 检测通知权限
     */
    private void checkNotificationPermission() {
        String pkgName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean isOpen = false;
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        isOpen = true;
                    }
                }
            }
        }
        if (!isOpen)
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    private void initView() {
        mRvMusicBrowser = findViewById(R.id.rv_music_browser);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvMusicBrowser.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        if (Build.VERSION.SDK_INT >= 21)
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mNotifyReceiveService = new ComponentName(this, MusicControlService.class);
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

    /**
     * 加载音乐控制页面
     */
    private void loadMusicControlAdapter() {
        loadMusicControlAdapter(false);
    }

    /**
     * 加载音乐控制页面
     */
    private void loadMusicControlAdapter(boolean onlyProgress) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                if (mediaControllers.size() > 0) {
                    List<MusicInfo> musicInfos = new ArrayList<>();
                    for (MediaController controller : mediaControllers) {
                        MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                        MusicInfo itemMusicInfo = new MusicInfo();
                        String pkgName = controllerCompat.getPackageName();
                        if (!"com.apple.android.music".equals(pkgName)) {
                            continue;
                        }
                        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(pkgName, 0);
                        itemMusicInfo.setAppName(applicationInfo.loadLabel(getPackageManager()).toString());
                        itemMusicInfo.setPkgName(pkgName);
                        PlaybackStateCompat playbackStateCompat = controllerCompat.getPlaybackState();
                        itemMusicInfo.setMusicState(playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING);
                        MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
//                        if (itemMusicInfo.isMusicState()) {
//                            progress += 1000L;
//                        }

                        // android.media.metadata.DURATION 毫秒值
                        itemMusicInfo.setDuration(controllerCompat.getMetadata().getLong("android.media.metadata.DURATION"));
//                        itemMusicInfo.setProgress(progress);
                        itemMusicInfo.setProgress(progress);

                        if (mediaMetadataCompat != null) {
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
                        if (onlyProgress) {
                            intent.putExtra("method", "updatepos");
                            intent.putExtra("pos", Long.toString(musicInfo.getProgress()));
                        } else {
                            intent.putExtra("method", "dashboard");
                            intent.putExtra("getTrackName", musicInfo.getTitle());
                            intent.putExtra("getAlbumName", musicInfo.getSinger());
                            intent.putExtra("getArtistName", musicInfo.getAlbumTitle());
                            intent.putExtra("getDuration", musicInfo.getDuration().toString());
                            intent.putExtra("getArtwork", musicInfo.getAlbumUrl());
                        }
                        sendBroadcast(intent);
                    }
//                    Toast.makeText(this, TimeUtil.millisToMines(progress), Toast.LENGTH_SHORT).show();
                    Log.d("broadcast", TimeUtil.millisToMines(progress));
//                    mRvMusicBrowser.setAdapter(new ControlAdapter(this, musicInfos, this));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    /**
     * 注册监听
     */
    private void registerListener() {
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_POSTED);
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_REMOVED);
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

    class NotifyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "NotifyReceiver->收到广播:" + intent.getAction());
            processNotify();
        }
    }

    class MusicActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "MusicActionReceiver->收到广播:" + intent.getAction());
            processNotify();
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
            progress = state.getPosition();
            playStat = state.getState();
            switch (playStat) {
                case PlaybackStateCompat.STATE_BUFFERING:
                case PlaybackStateCompat.STATE_CONNECTING:
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_STOPPED:
                case PlaybackStateCompat.STATE_ERROR: {
                    progress = 0L;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED:
                    break;
                case PlaybackStateCompat.STATE_FAST_FORWARDING:
                    break;
                case PlaybackStateCompat.STATE_REWINDING:
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                    break;
                default:
                    break;
            }
            loadMusicControlAdapter();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            //播放内容发生改变
            if (metadata != null) {
//                progress = 0L;
                loadMusicControlAdapter();
            }
        }
    };

    //判断是否开启悬浮窗权限   context可以用你的Activity.或者tiis
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    //权限打开
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
        } else {//4.4-6.0以下
            //无需处理了
        }
    }

}
