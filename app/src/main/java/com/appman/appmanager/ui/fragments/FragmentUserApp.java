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
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.ui.adapters.InstalledAppsAdapter;
import com.appman.appmanager.util.AppUtil;
import com.yalantis.phoenix.PullToRefreshView;

public class FragmentUserApp extends BaseFragment {
    private static final String TAG = "FragmentUserApp";

    private PullToRefreshView pullToRefreshView;
    private RecyclerView recyclerView;

    private InstalledAppsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View userAppView = inflater.inflate(R.layout.layout_fragment_user_app, container, false);
        initializeViews(userAppView);
        showErrorLayout(false);
        return userAppView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants
                .ACTION_EXTRACT_APPLICATION_CALLBACK);
        filter.addAction(AppConstants.ACTION_PERFORM_SEARCH_CALLBACK);
        filter.addAction(AppConstants.ACTION_SEARCH_RESULT_CALLBACK);
        registerBroadcastReceiver(filter);
        setAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                case AppConstants.ACTION_SEARCH_RESULT_CALLBACK:
                    boolean shouldShowError = intent.getBooleanExtra(AppConstants.EXTRA_KEY_SEARCH_RESULT, false);
                    if (shouldShowError) {
                        showErrorLayout(true);
                    } else {
                        showErrorLayout(false);
                    }
                    break;
                case AppConstants.ACTION_PERFORM_SEARCH_CALLBACK:
                    String query = intent.getStringExtra("Query");
                    if (query.isEmpty()) {
                        ((InstalledAppsAdapter) recyclerView.getAdapter()).getFilter().filter("");
                        noResults.setVisibility(View.GONE);
                    } else {
                        ((InstalledAppsAdapter) recyclerView.getAdapter()).getFilter().filter(query.toLowerCase());
                    }
                    break;
            }

        }
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
        if (AppUtil.getInstance().getInstalledAppsList() != null
                && !AppUtil.getInstance().getInstalledAppsList().isEmpty()) {
            showErrorLayout(false);
            adapter = new InstalledAppsAdapter(AppUtil.getInstance().getInstalledAppsList(), activityContext);
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
            getAllAppsFromDevice();

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
    protected void getAllAppsFromDevice() {
        super.getAllAppsFromDevice();
        LocalBroadcastManager.getInstance(getActivity())
                .sendBroadcast(new Intent(AppConstants.ACTION_PULL_TO_REFESH_CALLBACK));
    }
}
