package com.appman.appmanager.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appman.appmanager.R;
import com.appman.appmanager.app.AppManagerController;
import com.appman.appmanager.constants.AppConstants;
import com.appman.appmanager.data.AppInfo;
import com.appman.appmanager.ui.activities.info.AppInfoActivity;
import com.appman.appmanager.util.AppPreferences;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.FileUtil;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SystemAppsAdapter extends RecyclerView.Adapter<SystemAppsAdapter.AppViewHolder>
        implements Filterable {
    private Context context;
    private List<AppInfo> appList;
    private List<AppInfo> appListSearch;
    // Load Settings
    private AppPreferences appPreferences;

    public SystemAppsAdapter(List<AppInfo> list, Context ctx) {
        this.appList = list;
        this.context = ctx;
        this.appPreferences = AppManagerController.getInstance().getAppPreferences();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View appAdapterView = LayoutInflater.
                from(parent.getContext()).inflate(R.layout.system_app_list_item_layout, parent, false);
        return new AppViewHolder(appAdapterView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.vName.setText(appInfo.getName());
        holder.vApk.setText(appInfo.getAPK());
        holder.vApkSize.setText(appInfo.getApkSize());
        holder.vIcon.setImageDrawable(appInfo.getIcon());

        // Setting fav app
        Set<String> appsFavorite = appPreferences.getFavoriteApps();
        holder.imageViewFav.setImageResource(FileUtil.getInstance()
                .isAppFavorite(appInfo.getAPK(), appsFavorite) ? R.drawable.ic_star_white
                : R.drawable.ic_star_border_white);
        setButtonEvents(holder, appInfo, appsFavorite);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void clear() {
        appList.clear();
        notifyDataSetChanged();
    }

    private void setButtonEvents(AppViewHolder appViewHolder, final AppInfo appInfo, Set<String> appsFavorite) {
        // SwipeLayout
        final SwipeLayout swipeLayout = appViewHolder.swipeLayout;
        final LinearLayout txtSwipeExtract = appViewHolder.txtSwipeExtract;
        final LinearLayout txtSwipeShare = appViewHolder.txtSwipeShare;
        final LinearLayout txtSwipeFav = appViewHolder.txtSwipeFav;
        final LinearLayout txtSwipeInfo = appViewHolder.txtSwipeInfo;
        final ImageView imageViewFav = appViewHolder.imageViewFav;

        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        txtSwipeExtract.setOnClickListener(view -> {
            Intent extractBroadcastIntent = new Intent();
            extractBroadcastIntent.putExtra(AppConstants.EXTRA_KEY_APP_INFO, appInfo);
            extractBroadcastIntent.setAction(AppConstants.ACTION_EXTRACT_APPLICATION_CALLBACK);
            LocalBroadcastManager.getInstance(context).sendBroadcast(extractBroadcastIntent);
            swipeLayout.close(true);
        });

        txtSwipeShare.setOnClickListener(view -> {
            FileUtil.getInstance().copyFile(appInfo);
            Intent shareIntent = FileUtil.getInstance()
                    .getShareIntent(FileUtil.getInstance().getOutputFilename(appInfo));
            if (AppUtil.getInstance().isActivityResolvable(shareIntent, context)) {
                context.startActivity(Intent.createChooser(shareIntent,
                        String.format(context.getResources().getString(R.string.send_to), appInfo.getName()))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                AppUtil.getInstance().makeToast(context, "Unable to find shareable activity");
            }
            swipeLayout.close(true);
        });

        txtSwipeFav.setOnClickListener(view -> {
            if (FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), appsFavorite)) {
                appsFavorite.remove(appInfo.getAPK());
                appPreferences.setFavoriteApps(appsFavorite);
            } else {
                appsFavorite.add(appInfo.getAPK());
                appPreferences.setFavoriteApps(appsFavorite);
            }
            imageViewFav.setImageResource(FileUtil.getInstance().isAppFavorite(appInfo.getAPK(), appsFavorite)
                    ? R.drawable.ic_star_white : R.drawable.ic_star_border_white);
            swipeLayout.close(true);
            informAboutFavoriteApps();
        });

        txtSwipeInfo.setOnClickListener(v -> {
            swipeLayout.close(true);
            Activity currentActivity = AppManagerController.getInstance().getCurrentActivity();
            if (currentActivity != null) {
                Intent appInfoIntent = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                    Bitmap bitmap = ((BitmapDrawable) appInfo.getIcon()).getBitmap();
                    appInfoIntent = AppUtil.getInstance()
                            .createAppInfoIntent(appInfo.getName(),
                                    appInfo.getAPK(),
                                    appInfo.getVersion(),
                                    appInfo.getSource(),
                                    appInfo.getData(),
                                    appInfo.getApkSize(),
                                    bitmap,
                                    appInfo.isSystem(),
                                    currentActivity,
                                    AppInfoActivity.class);
                } else {
                    appInfoIntent = AppUtil.getInstance()
                            .createAppInfoIntent(appInfo.getName(),
                                    appInfo.getAPK(),
                                    appInfo.getVersion(),
                                    appInfo.getSource(),
                                    appInfo.getData(),
                                    appInfo.getApkSize(),
                                    AppUtil.getInstance().getBitmapFromDrawable(appInfo.getIcon()),
                                    appInfo.isSystem(),
                                    currentActivity,
                                    AppInfoActivity.class);
                }
                String transitionName = currentActivity.getResources().getString(R.string.transition_app_icon);

                ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(currentActivity, appViewHolder.vIcon, transitionName);
                currentActivity.startActivity(appInfoIntent, transitionActivityOptions.toBundle());
            }
        });
    }

    private void informAboutFavoriteApps() {
        Intent i = new Intent();
        i.setAction(AppConstants.ACTION_APP_PUT_TO_FAVORITES_CALLBACK);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults oReturn = new FilterResults();
                final List<AppInfo> results = new ArrayList<>();
                if (appListSearch == null) {
                    appListSearch = appList;
                }
                if (charSequence != null) {
                    if (appListSearch != null && appListSearch.size() > 0) {
                        for (final AppInfo appInfo : appListSearch) {
                            if (appInfo.getName().toLowerCase().contains(charSequence.toString())) {
                                results.add(appInfo);
                            }
                        }
                    }
                    oReturn.values = results;
                    oReturn.count = results.size();
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                Intent i = new Intent();
                i.setAction(AppConstants.ACTION_SEARCH_RESULT_CALLBACK);
                if (filterResults.count > 0) {
                    i.putExtra(AppConstants.EXTRA_KEY_SEARCH_RESULT, false);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                } else {
                    i.putExtra(AppConstants.EXTRA_KEY_SEARCH_RESULT, true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                }
                appList = (List<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView vName;
        private TextView vApk;
        private TextView vApkSize;
        private ImageView vIcon;
        private CardView vCard;
        // SwipeLayout
        private SwipeLayout swipeLayout;
        private LinearLayout txtSwipeExtract;
        private LinearLayout txtSwipeShare;
        private LinearLayout txtSwipeFav;
        private LinearLayout txtSwipeInfo;
        private ImageView imageViewFav;

        private AppViewHolder(View v) {
            super(v);
            vName = v.findViewById(R.id.txtName);
            vApk = v.findViewById(R.id.txtApk);
            vApkSize = v.findViewById(R.id.txtApkSize);
            vIcon = v.findViewById(R.id.imgIcon);
            vCard = v.findViewById(R.id.app_card);
            // SwipeLayout
            swipeLayout = v.findViewById(R.id.swipe_selection_layout);
            txtSwipeExtract = v.findViewById(R.id.txt_swipe_extract);
            txtSwipeShare = v.findViewById(R.id.txt_swipe_share);
            txtSwipeFav = v.findViewById(R.id.txt_swipe_fav);
            txtSwipeInfo = v.findViewById(R.id.txt_swipe_info);
            imageViewFav = v.findViewById(R.id.swipe_fav);
        }
    }
}
