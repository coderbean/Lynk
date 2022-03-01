package cn.edu.fjnu.musicdemo;

import android.annotation.SuppressLint;
import android.content.Intent;

import java.util.Objects;

public class MyUtils {
    @SuppressLint("DefaultLocale")
    public static String millisToMines(Long lg) {
        if (lg == null) {
            return "";
        }
        int seconds = (int) (lg / 1000L);
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static String printBroadCast(Intent intent) {
         return intent.getStringExtra("method") + "|"
         + intent.getStringExtra("pos") + "|"
         + intent.getStringExtra("method") + "|"
         + intent.getStringExtra("getTrackName") + "|"
         + intent.getStringExtra("getAlbumName") + "|"
         + intent.getStringExtra("getArtistName") + "|"
         + intent.getStringExtra("getDuration") + "|"
         + intent.getStringExtra("getArtwork") + "|"
         + intent.getStringExtra("") + "|"
         + intent.getStringExtra("");
    }
}
