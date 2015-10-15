package com.bbk.lling.multitaskdownload.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bbk.lling.multitaskdownload.R;
import com.bbk.lling.multitaskdownload.beans.AppContent;

import java.util.List;

/**
 * @Class: AppContentAdapter
 * @Description: 应用市场app内容Adapter
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/13
 */
public class AppContentAdapter extends BaseAdapter{

    private List<AppContent> mDates = null;
    private Context mContext;

    public AppContentAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mDates.size();
    }

    @Override
    public Object getItem(int position) {
        return mDates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDates(List<AppContent> mDates) {
        this.mDates = mDates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.listitem_download, null);
            holder.statusIcon = (ImageView) convertView.findViewById(R.id.status_icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.downloadPercent = (TextView) convertView.findViewById(R.id.download_percent);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setData(holder, position);
        return convertView;
    }

    /**
     * 设置viewHolder的数据
     * @param holder
     * @param itemIndex
     */
    private void setData(ViewHolder holder, int itemIndex) {
        AppContent appContent = mDates.get(itemIndex);
        holder.name.setText(appContent.getName());
        holder.progressBar.setProgress(appContent.getDownloadPercent());
        setIconByStatus(holder.statusIcon, appContent.getStatus());
        if(appContent.getStatus() == AppContent.Status.PENDING) {
            holder.downloadPercent.setVisibility(View.INVISIBLE);
        } else {
            holder.downloadPercent.setVisibility(View.VISIBLE);
            holder.downloadPercent.setText("下载进度：" + appContent.getDownloadPercent() + "%");
        }
    }


    /**
     * 局部刷新
     * @param view
     * @param itemIndex
     */
    public void updateView(View view, int itemIndex) {
        if(view == null) {
            return;
        }
        //从view中取得holder
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.statusIcon = (ImageView) view.findViewById(R.id.status_icon);
        holder.name = (TextView) view.findViewById(R.id.name);
        holder.downloadPercent = (TextView) view.findViewById(R.id.download_percent);
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        setData(holder, itemIndex);
    }

    /**
     * 根据状态设置图标
     * @param imageView
     * @param status
     */
    private void setIconByStatus(ImageView imageView, AppContent.Status status) {
        imageView.setVisibility(View.VISIBLE);
        if(status == AppContent.Status.PENDING) {
            imageView.setImageResource(R.drawable.ic_no_download);
        }
        if(status == AppContent.Status.DOWNLOADING) {
            imageView.setVisibility(View.INVISIBLE);
        }
        if(status == AppContent.Status.WAITING) {
            imageView.setImageResource(R.drawable.ic_wait);
        }
        if(status == AppContent.Status.PAUSED) {
            imageView.setImageResource(R.drawable.ic_pause);
        }
        if(status == AppContent.Status.FINISHED) {
            imageView.setImageResource(R.drawable.ic_finished);
        }
    }

    private class ViewHolder {
        private ImageView statusIcon;
        private TextView name;
        private TextView downloadPercent;
        private ProgressBar progressBar;
    }
}
