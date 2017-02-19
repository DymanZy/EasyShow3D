package com.dyman.show3dmodel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.adapter.listener.OnAdapterItemListener;
import com.dyman.show3dmodel.bean.FileBean;
import com.dyman.show3dmodel.utils.FileUtils;

import java.util.List;


/**
 * Created by dyman on 16/7/24.
 */
public class FileRvListAdapter extends RecyclerView.Adapter<FileRvListAdapter.MyViewHolder> {

    private List<FileBean> mData;
    private Context context;

    public FileRvListAdapter(Context context, List<FileBean> data) {
        this.mData = data;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filelist_main, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileBean bean = mData.get(position);

        holder.fileNameTv.setText(bean.getFileName());
        int fileImage = FileUtils.getImageFromType(bean.getFileType());
        holder.fileImageIv.setImageResource(fileImage == 0 ? R.mipmap.ic_file_model : fileImage);
        holder.updateTimeTv.setText(bean.getCreateTime());

    }

    //自定义回调接口
    public OnAdapterItemListener itemListener;
    public void setOnItemClickListener(OnAdapterItemListener itemClickListener) {
        this.itemListener = itemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private LinearLayout openFile;
        private TextView fileNameTv;
        private ImageView fileImageIv;
        private TextView updateTimeTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            openFile = (LinearLayout) itemView.findViewById(R.id.openFile_ll_item_fileList_main);
            fileNameTv = (TextView) itemView.findViewById(R.id.fileName_tv_item_fileList_main);
            fileImageIv = (ImageView) itemView.findViewById(R.id.fileType_iv_item_fileList_main);
            updateTimeTv = (TextView) itemView.findViewById(R.id.createTime_tv_item_fileList_main);

            openFile.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemListener != null){
                itemListener.onItemClick(view, getLayoutPosition());
            }
        }
    }

}
