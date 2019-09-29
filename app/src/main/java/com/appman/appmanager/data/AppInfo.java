package com.appman.appmanager.data;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {

    private String name;
    private String apk;
    private String version;
    private String source;
    private String data;
    private String apkSize;
    private Drawable icon;
    private Boolean system;

    public AppInfo(String name, String apk, String version,
                   String source, String data, String appSize, Drawable icon, Boolean isSystem) {
        this.name = name;
        this.apk = apk;
        this.version = version;
        this.source = source;
        this.data = data;
        this.apkSize = appSize;
        this.icon = icon;
        this.system = isSystem;
    }

    public AppInfo(String string) {
        String[] split = string.split("##");
        if (split.length == 7) {
            this.name = split[0];
            this.apk = split[1];
            this.version = split[2];
            this.source = split[3];
            this.data = split[4];
            this.apkSize = split[5];
            this.system = Boolean.getBoolean(split[6]);
        }
    }

    protected AppInfo(Parcel in) {
        name = in.readString();
        apk = in.readString();
        version = in.readString();
        source = in.readString();
        data = in.readString();
        apkSize = in.readString();
        byte systemVal = in.readByte();
        system = systemVal == 0x02 ? null : systemVal != 0x00;
    }

    public String getName() {
        return name;
    }

    public String getAPK() {
        return apk;
    }

    public String getVersion() {
        return version;
    }

    public String getSource() {
        return source;
    }

    public String getData() {
        return data;
    }

    public String getApkSize() {
        return apkSize; }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Boolean isSystem() {
        return system;
    }

    public String toString() {
        return getName() + "##" + getAPK() + "##"
                + getVersion() + "##" + getSource() + "##" + getData() + "##" + isSystem();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(apk);
        dest.writeString(version);
        dest.writeString(source);
        dest.writeString(data);
        dest.writeString(apkSize);
        if (system == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (system ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
