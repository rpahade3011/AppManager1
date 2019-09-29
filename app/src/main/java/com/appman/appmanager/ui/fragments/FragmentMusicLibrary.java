package com.appman.appmanager.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appman.appmanager.R;
import com.appman.appmanager.commands.GetAllMusicCommand;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.MusicInfo;
import com.appman.appmanager.interfaces.IMusicLibraryListener;
import com.appman.appmanager.ui.adapters.MusicLibraryAdapter;
import com.appman.appmanager.util.AppUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;

public class FragmentMusicLibrary extends BaseFragment {

    private static final String TAG = "FragmentMusicLibrary";

    private ShimmerFrameLayout shimmerFrameLayout;

    private PullToRefreshView pullToRefreshView;
    private RecyclerView recyclerView;

    private MusicLibraryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideMenuItem();
        checkToRestoreData(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        saveDataToCache(outState);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View musicLibView = inflater.inflate(R.layout.layout_fragment_music_library,
                container, false);
        initializeViews(musicLibView);
        setAdapter();
        return musicLibView;
    }

    @Override
    public void onDestroy() {
        unHideMenuItem();
        super.onDestroy();
    }

    @Override
    protected void initializeViews(View viewItem) {
        super.initializeViews(viewItem);
        // Initialize shimmer
        shimmerFrameLayout = viewItem.findViewById(R.id.shimmer_view_container);

        // Initialize error layout
        noResults = viewItem.findViewById(R.id.noResults);

        pullToRefreshView = viewItem.findViewById(R.id.pull_to_refresh);
        recyclerView = viewItem.findViewById(R.id.appList);

        pullToRefreshView.setEnabled(false);
    }

    @Override
    protected void setAdapter() {
        super.setAdapter();
        if (AppUtil.getInstance().getMusicInfoList() != null
                && AppUtil.getInstance().getMusicInfoList().size() > 0) {
            showErrorLayout(false);
            adapter = new MusicLibraryAdapter(activityContext,
                    AppUtil.getInstance().getMusicInfoList());
            recyclerView.setHasFixedSize(true);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            pullToRefreshView.setEnabled(true);

            setPullToRefreshView(pullToRefreshView);
        } else {
            showErrorLayout(true);
        }
    }

    @Override
    protected void showErrorLayout(Boolean result) {
        super.showErrorLayout(result);
        if (!result) {
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPullToRefreshView(PullToRefreshView pullToRefreshView) {
        pullToRefreshView.setOnRefreshListener(() -> {
            adapter.clear();
            recyclerView.setAdapter(null);
            getMusicLibrary();
            pullToRefreshView.postDelayed(() ->
                    pullToRefreshView.setRefreshing(false), 2000);
        });
    }

    private void checkToRestoreData(Bundle savedInstanceState) {
        if (canRestoreDataFromCache(savedInstanceState)) {
            restoreDataFromCache(savedInstanceState);
        }
    }

    private boolean canRestoreDataFromCache(Bundle savedInstanceState) {
        return savedInstanceState
                != null && savedInstanceState.containsKey(AppConstants.OUT_STATE_MUSIC_LIBRARY_KEY);
    }

    private void saveDataToCache(Bundle outState) {
        // Saving music library list
        if (AppUtil.getInstance().getMusicInfoList() != null) {
            outState.putParcelableArrayList(AppConstants.OUT_STATE_MUSIC_LIBRARY_KEY,
                    new ArrayList<Parcelable>(AppUtil.getInstance().getMusicInfoList()));
        }
    }

    private void restoreDataFromCache(Bundle savedInstanceState) {
        // Restoring music library list
        AppUtil.getInstance()
                .setMusicInfoList(savedInstanceState.
                        <MusicInfo>getParcelableArrayList(AppConstants.
                                OUT_STATE_MUSIC_LIBRARY_KEY));
    }

    private void getMusicLibrary() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        new GetAllMusicCommand(activityContext.getApplicationContext(), musicLibraryListener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void hideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", true);
        LocalBroadcastManager.getInstance(activityContext).sendBroadcast(hideMenuIntent);
    }

    private void unHideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", false);
        LocalBroadcastManager.getInstance(activityContext).sendBroadcast(hideMenuIntent);
    }

    private final IMusicLibraryListener musicLibraryListener = musicType -> {
        switch (musicType) {
            case LIBRARY:
                setAdapter();
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                Intent musicCounterIntent = new Intent(AppConstants.ACTION_MUSIC_LIBRARY_COUNT_CALLBACK);
                musicCounterIntent.putExtra(AppConstants.EXTRA_KEY_MUSIC_COUNT,
                        AppUtil.getInstance().getMusicInfoList().size());
                LocalBroadcastManager.getInstance(activityContext).sendBroadcast(musicCounterIntent);
                break;
            case NO_DATA_FOUND:
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                showErrorLayout(true);
                break;
        }
    };
}
