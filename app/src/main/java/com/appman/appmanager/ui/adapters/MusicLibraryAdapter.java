package com.appman.appmanager.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.appman.appmanager.R;
import com.appman.appmanager.data.MusicInfo;
import com.appman.appmanager.util.AppUtil;
import com.appman.appmanager.util.FileUtil;
import com.daimajia.swipe.SwipeLayout;

import java.util.List;

public class MusicLibraryAdapter extends RecyclerView.Adapter<MusicLibraryAdapter.MusicViewHolder> {

    private Context context;
    private List<MusicInfo> musicInfoList;

    public MusicLibraryAdapter(Context ctx, List<MusicInfo> list) {
        this.context = ctx;
        this.musicInfoList = list;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View appAdapterView = LayoutInflater.from(context).inflate(R.layout.music_lib_list_item_layout,
                parent, false);
        return new MusicViewHolder(appAdapterView);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicInfo musicInfo = musicInfoList.get(position);
        holder.vName.setText(musicInfo.getTitle());
        holder.vApk.setText(musicInfo.getArtist());
        holder.vtxtApkSize.setText(musicInfo.getMusicSize());
        holder.vIcon.setImageResource(R.drawable.ic_music_note_black);

        setButtonEvents(holder, musicInfo);
    }

    private void setButtonEvents(MusicViewHolder holder, MusicInfo musicInfo) {
        // SwipeLayout
        final SwipeLayout swipeLayout = holder.swipeLayout;
        final LinearLayout txtSwipeExtract = holder.txtSwipeExtract;
        final LinearLayout txtSwipeShare = holder.txtSwipeShare;
        final LinearLayout txtSwipeFav = holder.txtSwipeFav;
        final LinearLayout txtSwipeInfo = holder.txtSwipeInfo;
        final ImageView imageViewFav = holder.imageViewFav;
        final TextView txtSwipeShareName = holder.txtSwipeShareName;

        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        txtSwipeFav.setVisibility(View.GONE);
        txtSwipeInfo.setVisibility(View.GONE);
        txtSwipeShareName.setText("Share");

        txtSwipeExtract.setOnClickListener(view -> {

        });

        txtSwipeShare.setOnClickListener(view -> {
            FileUtil.getInstance().copyMusicFile(musicInfo);
            Intent shareIntent = FileUtil.getInstance()
                    .getMusicShareIntent(FileUtil.getInstance().getMusicOutputFilename(musicInfo));
            if (AppUtil.getInstance().isActivityResolvable(shareIntent, context)) {
                context.startActivity(Intent.createChooser(shareIntent,
                        String.format(context.getResources().getString(R.string.send_to),
                                musicInfo.getTitle()))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                AppUtil.getInstance().makeToast(context, "Unable to find shareable activity");
            }
            swipeLayout.close(true);
        });
    }

    @Override
    public int getItemCount() {
        return musicInfoList.size();
    }

    public void clear() {
        musicInfoList.clear();
        notifyDataSetChanged();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView vName;
        private TextView vApk;
        private TextView vtxtApkSize;
        private ImageView vIcon;
        private CardView vCard;
        // SwipeLayout
        private SwipeLayout swipeLayout;
        private LinearLayout txtSwipeExtract;
        private LinearLayout txtSwipeShare;
        private LinearLayout txtSwipeFav;
        private LinearLayout txtSwipeInfo;
        private ImageView imageViewFav;
        private TextView txtSwipeShareName;

        private MusicViewHolder(View v) {
            super(v);
            vName = v.findViewById(R.id.txtName);
            vApk = v.findViewById(R.id.txtApk);
            vtxtApkSize = v.findViewById(R.id.txtApkSize);
            vIcon = v.findViewById(R.id.imgIcon);
            vCard = v.findViewById(R.id.app_card);

            // SwipeLayout
            swipeLayout = v.findViewById(R.id.swipe_selection_layout);
            txtSwipeExtract = v.findViewById(R.id.txt_swipe_extract);
            txtSwipeShare = v.findViewById(R.id.txt_swipe_share);
            txtSwipeFav = v.findViewById(R.id.txt_swipe_fav);
            txtSwipeInfo = v.findViewById(R.id.txt_swipe_info);
            imageViewFav = v.findViewById(R.id.swipe_fav);
            txtSwipeShareName = v.findViewById(R.id.swipe_txt_share);
        }
    }
}
