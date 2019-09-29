package com.appman.appmanager.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Parcelable {
    private String title;
    private String artist;
    private String musicSize;
    private String location;

    public MusicInfo() {}

    public MusicInfo(String title, String artist, String musicSize, String location) {
        this.title = title;
        this.artist = artist;
        this.musicSize = musicSize;
        this.location = location;
    }

    public MusicInfo(String string) {
        String[] split = string.split("##");
        if (split.length == 4) {
            this.title = split[0];
            this.artist = split[1];
            this.musicSize = split[2];
            this.location = split[3];
        }
    }

    protected MusicInfo(Parcel in) {
        title = in.readString();
        artist = in.readString();
        musicSize = in.readString();
        location = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getMusicSize() { return musicSize; }

    public String getLocation() {
        return location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(musicSize);
        dest.writeString(location);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };
}
