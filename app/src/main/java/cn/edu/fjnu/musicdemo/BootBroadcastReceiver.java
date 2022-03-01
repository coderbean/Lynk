package cn.edu.fjnu.musicdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "BootBroadcastReceiver";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "收到开机广播");
            Toast.makeText(context, "广播转换：开机启动", Toast.LENGTH_SHORT).show();
            // 启动服务
            context.startForegroundService(new Intent(context, ForegroundService.class));
            // 启动activity
//            Intent intent1 = new Intent(context,MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent1);
        }
    }
}
