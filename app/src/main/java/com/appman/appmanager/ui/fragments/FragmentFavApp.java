package com.appman.appmanager.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.ui.adapters.FavoriteAppsAdapter;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.FileUtil;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavApp extends BaseFragment {
    private static final String TAG = "FragmentFavApp";

    private PullToRefreshView pullToRefreshView;
    private RecyclerView recyclerView;

    private FavoriteAppsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideMenuItem();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View favAppView = inflater.inflate(R.layout.layout_fragment_fav_app, container, false);
        initializeViews(favAppView);
        setAdapter();
        return favAppView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_EXTRACT_APPLICATION_CALLBACK);
        filter.addAction(AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK);
        filter.addAction(AppConstants.ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK);
        registerBroadcastReceiver(filter);
    }

    @Override
    public void onStop() {
        unHideMenuItem();
        super.onStop();
        unRegisterBroadcastReceiver();
    }
    @Override
    protected void initializeViews(View viewItem) {
        super.initializeViews(viewItem);
        // Initialize error layout
        noResults = viewItem.findViewById(R.id.noResults);

        pullToRefreshView = viewItem.findViewById(R.id.pull_to_refresh);
        recyclerView = viewItem.findViewById(R.id.appList);

        pullToRefreshView.setEnabled(false);
    }

    @Override
    protected void setAdapter() {
        super.setAdapter();
        if (AppUtil.getInstance().getFavoriteAppsList() != null
                && AppUtil.getInstance().getFavoriteAppsList().size() > 0) {
            showErrorLayout(false);
            adapter = new FavoriteAppsAdapter(AppUtil.getInstance().getFavoriteAppsList(), getActivity());
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
    public void setPullToRefreshView(final PullToRefreshView pullToRefreshView) {
        pullToRefreshView.setOnRefreshListener(() -> {
            adapter.clear();
            recyclerView.setAdapter(null);
            getFavoriteApps();

            pullToRefreshView.postDelayed(() ->
                    pullToRefreshView.setRefreshing(false), 2000);
        });
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
    protected void onBroadcastReceived(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        super.onBroadcastReceived(context, intent);
        if (isVisible() && getActivity() != null) {
            switch (intent.getAction()) {
                case AppConstants.ACTION_EXTRACT_APPLICATION_CALLBACK:
                    AppInfo info = intent.getParcelableExtra(AppConstants.EXTRA_KEY_APP_INFO);
                    extractAppInBackground(getActivity(), info);
                    break;
                case AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK:
                    getFavoriteApps();
                    break;
                case AppConstants.ACTION_APP_REMOVED_FROM_FAVORITES_CALLBACK:
                    getFavoriteApps();
                    break;
            }
        }
    }

    private void getFavoriteApps() {
        List<AppInfo> favAppList = new ArrayList<>();
        favAppList.clear();
        // Favorites
        for (AppInfo appInfo : AppUtil.getInstance().getInstalledAppsList()) {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), AppManagerController.getInstance()
                    .getAppPreferences().getFavoriteApps())) {
                favAppList.add(appInfo);
            }
        }
        for (AppInfo appInfo : AppUtil.getInstance().getSystemAppsList()) {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), AppManagerController.getInstance()
                    .getAppPreferences().getFavoriteApps())) {
                favAppList.add(appInfo);
            }
        }
        if (favAppList.isEmpty()) {
            adapter.clear();
            showErrorLayout(true);
        } else {
            adapter.notifyDataSetChanged();
            showErrorLayout(false);
        }
    }

    private void hideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", true);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(hideMenuIntent);
    }

    private void unHideMenuItem() {
        Intent hideMenuIntent = new Intent();
        hideMenuIntent.setAction(AppConstants.ACTION_HIDE_MENU_ITEM_CALLBACK);
        hideMenuIntent.putExtra("Menu", false);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(hideMenuIntent);
    }
}
