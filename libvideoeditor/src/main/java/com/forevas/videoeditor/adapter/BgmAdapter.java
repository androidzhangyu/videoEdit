package com.forevas.videoeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.forevas.videoeditor.R;
import com.forevas.videoeditor.bean.Song;
import com.forevas.videoeditor.constants.Constants;
import com.forevas.videoeditor.utils.DensityUtils;
import com.forevas.videoeditor.utils.StorageFormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *Created by carden
 */

public class BgmAdapter extends PagerAdapter {
    public static final int PAGE_SIZE=6;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Song> mSongs;
    private OnItemSelectListener mListener;
    private List<View> viewList=new ArrayList<>();
    public BgmAdapter(Context context) {
        this(context,null);
    }
    public BgmAdapter(Context context, List<Song> songs) {
        this.mContext = context;
        this.mSongs = songs;
        mInflater = LayoutInflater.from(context);
    }
    public void setSongData(List<Song> songs){
        this.mSongs=songs;
        notifyDataSetChanged();
    }
    public void setOnItemSelectListener(OnItemSelectListener listener){
        this.mListener=listener;
    }
    @Override
    public int getCount() {
        if(mSongs==null){
            return 0;
        }
        return mSongs.size() % PAGE_SIZE == 0 ? mSongs.size() / PAGE_SIZE : mSongs.size() / PAGE_SIZE + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View pagerItem = mInflater.inflate(R.layout.editor_bgm_pager_item, null);
        RecyclerView rv = (RecyclerView) pagerItem.findViewById(R.id.rv_item);
        RecyclerView.LayoutManager manager = new GridLayoutManager(mContext, 3, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = i + position * PAGE_SIZE;
            if (index < mSongs.size()) {
                songs.add(mSongs.get(index));
            } else {
                break;
            }
        }
        PagerItemAdapter pagerItemAdapter = new PagerItemAdapter(mContext, songs,position);
        rv.setAdapter(pagerItemAdapter);
        container.addView(pagerItem);
        viewList.add(pagerItem);
        return pagerItem;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        viewList.remove(object);
    }
    public void notifyPager(){
        for(int i=0;i<viewList.size();i++){
            View view = viewList.get(i);
            RecyclerView rvItem = (RecyclerView) view.findViewById(R.id.rv_item);
            RecyclerView.Adapter adapter = rvItem.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    class PagerItemAdapter extends RecyclerView.Adapter<PagerItemAdapter.ItemViewHolder> {
        private Context context;
        private LayoutInflater inflator;
        private List<Song> songs;
        private int pageIndex;

        public PagerItemAdapter(Context context, List<Song> songs,int pageIndex) {
            this.context = context;
            this.songs = songs;
            this.pageIndex=pageIndex;
            inflator = LayoutInflater.from(context);
        }

        @Override
        public PagerItemAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View content = inflator.inflate(R.layout.editor_bgm_recycler_item, parent, false);
            return new ItemViewHolder(content);
        }

        @Override
        public void onBindViewHolder(PagerItemAdapter.ItemViewHolder holder, final int position) {
            Song song = songs.get(position);
            holder.rlContainer.setSelected(song.selected);
            holder.tvSong.setText(song.getName());
            holder.tvAuthor.setText(song.getAuthor());
            holder.rlContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener!=null){
                        mListener.onItemSelect(position+pageIndex*PAGE_SIZE);
                    }
                }
            });
            if(song.isSelected()){
                holder.rlContainer.setSelected(true);
                holder.tvSong.setSelected(true);
                holder.tvAuthor.setSelected(true);
            }else{
                holder.rlContainer.setSelected(false);
                holder.tvSong.setSelected(false);
                holder.tvAuthor.setSelected(false);
            }
            if(song.location==0){
                holder.ivStatus.setImageResource(song.isSelected()?R.mipmap.editor_icon_music_selected:R.mipmap.editor_icon_music_normal);
                holder.llProgress.setVisibility(View.INVISIBLE);
            }else if(song.location==1){
                switch (song.status) {
                    case 0:
                        holder.ivStatus.setImageResource(R.mipmap.editor_icon_music_download);
                        holder.llProgress.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        holder.ivStatus.setImageResource(R.mipmap.editor_icon_music_download);
                        holder.llProgress.setVisibility(View.VISIBLE);
                        holder.progressBar.setProgress(song.progress);
                        holder.tvProgress.setText(StorageFormatUtils.formatByte((song.getTotalSize()*song.progress/100))+"/"+StorageFormatUtils.formatByte(song.getTotalSize()));
                        break;
                    case 2:
                        holder.ivStatus.setImageResource(R.mipmap.editor_icon_music_normal);
                        holder.llProgress.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rlContainer;
            TextView tvSong, tvAuthor;
            ImageView ivStatus;
            LinearLayout llProgress;
            ProgressBar progressBar;
            TextView tvProgress;

            public ItemViewHolder(View itemView) {
                super(itemView);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                layoutParams.height = (Constants.screenHeight - Constants.screenWidth - DensityUtils.dp2px(context, 90)) / 2;
                itemView.setLayoutParams(layoutParams);
                rlContainer = (RelativeLayout) itemView.findViewById(R.id.rl_container);
                tvSong = (TextView) itemView.findViewById(R.id.tv_song);
                tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
                ivStatus = (ImageView) itemView.findViewById(R.id.iv_status);
                llProgress= (LinearLayout) itemView.findViewById(R.id.ll_progress);
                progressBar= (ProgressBar) itemView.findViewById(R.id.progress);
                tvProgress= (TextView) itemView.findViewById(R.id.tv_progress);
            }
        }
    }
    public interface OnItemSelectListener{
        void onItemSelect(int position);
    }
}
