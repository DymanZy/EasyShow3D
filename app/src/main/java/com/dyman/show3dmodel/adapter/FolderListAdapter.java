package com.dyman.show3dmodel.adapter;

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
import com.dyman.show3dmodel.bean.FolderBean;

import java.util.List;


/**
 * Created by dyman on 16/7/24.
 */
public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.MyViewHolder> {

    private List<FolderBean> mData;

    public FolderListAdapter(List<FolderBean> data) {
        this.mData = data;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frequent_folder, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FolderBean bean = mData.get(position);
        holder.folderName.setText(bean.getFolderName());

    }

    //自定义回调接口
    public OnAdapterItemListener itemListener;
    public void setOnItemClickListener(OnAdapterItemListener itemClickListener) {
        this.itemListener = itemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private LinearLayout openFolder;
        private TextView folderName;

        public MyViewHolder(View itemView) {
            super(itemView);
            openFolder = (LinearLayout) itemView.findViewById(R.id.openFolder_ll_item_frequent_folder);
            folderName = (TextView) itemView.findViewById(R.id.folderName_tv_item_frequent_folder);

            openFolder.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemListener != null){
                itemListener.onItemClick(view, getLayoutPosition());
            }
        }
    }

}
