package com.appman.appmanager.commands;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.format.Formatter;

import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.MusicInfo;
import com.appman.appmanager.interfaces.IMusicLibraryListener;
import com.appman.appmanager.util.AppUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetAllMusicCommand extends AsyncTask<Void, Void, List<MusicInfo>> {
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private IMusicLibraryListener musicLibraryListener;

    public GetAllMusicCommand(Context context, IMusicLibraryListener listener) {
        this.mContext = context;
        this.musicLibraryListener = listener;
    }

    @Override
    protected List<MusicInfo> doInBackground(Void... voids) {
        List<MusicInfo> mMusicLibraryList = new ArrayList<>();

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri songs = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Cursor cursor = contentResolver.query(songs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int songLocation = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do {
                    String currentTitle = cursor.getString(songTitle);
                    String currentArtist = cursor.getString(songArtist);
                    String currentLocation = cursor.getString(songLocation);
                    String musicSize = Formatter.formatFileSize(mContext, new File(currentLocation).length());

                    mMusicLibraryList.add(new MusicInfo(currentTitle,
                            currentArtist, musicSize, currentLocation));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return mMusicLibraryList;
    }

    @Override
    protected void onPostExecute(List<MusicInfo> musicInfo) {
        super.onPostExecute(musicInfo);
        if (musicInfo.size() > 0) {
            AppUtil.getInstance().setMusicInfoList(musicInfo);
            if (musicLibraryListener != null) {
                musicLibraryListener.onMusicLibraryFound(AppConstants.MUSIC_TYPE.LIBRARY);
            }
        } else {
            if (musicLibraryListener != null) {
                musicLibraryListener.onMusicLibraryFound(AppConstants.MUSIC_TYPE.NO_DATA_FOUND);
            }
        }
    }
}
