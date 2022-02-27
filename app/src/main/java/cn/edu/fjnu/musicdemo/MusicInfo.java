package cn.edu.fjnu.musicdemo;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MusicInfo {
    private String appName;
    private String pkgName;
    /**
     * 歌曲名称
     */
    private String title = "";
    private boolean musicState;
    /**
     * 封面
     */
    private Bitmap album;

    /**
     * 封面url
     */
    private String albumUrl;

    /**
     * 歌手
     */
    private String singer = "";

    /**
     * 专辑名称
     */
    private String albumTitle = "";

    /**
     * 歌曲时长
     */
    private Long duration = 180000L;

    /**
     * 歌曲进度
     */
    private Long progress = 0L;

    public String hashSong() {
        try {
            // 创建一个MessageDigest实例:
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 反复调用update输入数据:
            md.update(this.title.getBytes("UTF-8"));
            md.update(this.singer.getBytes("UTF-8"));
            md.update(this.albumTitle.getBytes("UTF-8"));
            byte[] result = md.digest(); // 16 bytes: 68e109f0f40ca72a15e05cc22786f8e6
            return (new BigInteger(1, result).toString(16));
        } catch (Throwable e) {
            e.printStackTrace();
            // 降级返回当前时间，保证刷新
            return Long.toString(System.currentTimeMillis());
        }
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMusicState() {
        return musicState;
    }

    public void setMusicState(boolean musicState) {
        this.musicState = musicState;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Bitmap getAlbum() {
        return album;
    }

    public void setAlbum(Bitmap album) {
        this.album = album;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getProgress() {
        return progress;
    }

    public void setProgress(Long progress) {
        this.progress = progress;
    }



    @Override
    public String toString() {
        return "MusicInfo{" +
                "appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", title='" + title + '\'' +
                ", musicState=" + musicState +
                ", album=" + album +
                ", albumUrl='" + albumUrl + '\'' +
                ", singer='" + singer + '\'' +
                ", albumTitle='" + albumTitle + '\'' +
                ", duration=" + duration +
                ", progress=" + progress +
                '}';
    }
}
