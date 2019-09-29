package com.appman.appmanager.interfaces;

import com.appman.appmanager.constants.AppConstants;

public interface IMusicLibraryListener {
    void onMusicLibraryFound(AppConstants.MUSIC_TYPE musicType);
}
