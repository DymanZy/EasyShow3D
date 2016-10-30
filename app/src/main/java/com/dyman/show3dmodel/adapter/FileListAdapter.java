package com.dyman.show3dmodel.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.bean.FileBean;
import com.dyman.show3dmodel.utils.FileUtils;


import java.util.List;

/**
 * Created by dyman on 16/8/20.
 */
public class FileListAdapter extends BaseSwipListAdapter {

    private static final String TAG = "FileListAdapter";
    private List<FileBean> mData;
    private Context context;
    private LayoutInflater inflater;

    public FileListAdapter(Context context, List<FileBean> list){
        this.context = context;
        this.mData = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {
        FileBean bean = mData.get(i);
        MyViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_filelist_main, parent, false);
            holder = new MyViewHolder(
                    (ImageView) convertView.findViewById(R.id.fileType_iv_item_fileList_main),
                    (TextView) convertView.findViewById(R.id.fileName_tv_item_fileList_main),
                    (TextView) convertView.findViewById(R.id.createTime_tv_item_fileList_main)
            );
            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }
        int imageRes = FileUtils.getImageFromType(bean.getFileType());
        holder.fileImage.setImageResource(imageRes == 0 ? R.mipmap.ic_file_model : imageRes);
        holder.fileName.setText(bean.getFileName());
        holder.fileTime.setText(bean.getCreateTime());

        return convertView;
    }

    private class MyViewHolder{
        private ImageView fileImage;
        private TextView fileName;
        private TextView fileTime;

        public MyViewHolder(ImageView fileImage, TextView fileName, TextView fileTime) {
            super();
            this.fileImage = fileImage;
            this.fileName = fileName;
            this.fileTime = fileTime;
        }
    }

    @Override
    public boolean getSwipEnableByPosition(int position) {
        if (position % 2 == 0) {
            return false;
        }
        return true;
    }
}
