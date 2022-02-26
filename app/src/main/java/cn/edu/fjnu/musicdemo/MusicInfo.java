package cn.edu.fjnu.musicdemo;

import android.graphics.Bitmap;
import android.net.Uri;

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
}
