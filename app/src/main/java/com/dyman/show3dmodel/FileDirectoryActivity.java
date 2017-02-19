package com.dyman.show3dmodel;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyman.show3dmodel.adapter.FileRvListAdapter;
import com.dyman.show3dmodel.adapter.listener.OnAdapterItemListener;
import com.dyman.show3dmodel.bean.FileBean;
import com.dyman.show3dmodel.config.IntentKey;
import com.dyman.show3dmodel.config.MyConfig;
import com.dyman.show3dmodel.db.DatabaseHelper;
import com.dyman.show3dmodel.utils.FileUtils;
import com.dyman.show3dmodel.utils.TimeUtils;
import com.dyman.show3dmodel.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *  浏览文件列表的Activity
 *      配有目录级和当前目录下的文件列表
 */
public class FileDirectoryActivity extends BaseActivity implements OnAdapterItemListener{

    private static final String TAG = "FileDirectoryActivity";

    /** 文件目录级相关 */
    private RecyclerView directoryLevelRv;
    private LevelAdapter adapter;
    private List<String> levelList = new ArrayList<>();
    /** 文件列表相关 */
    private RecyclerView fileListRv;
    private FileRvListAdapter fileAdapter;
    private List<FileBean> fileList = new ArrayList<>();

    private String mDirectoryPath;
    private String mTitle;
    private String mSearchType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_directory);

        Intent it = getIntent();
        mTitle = it.getStringExtra(IntentKey.TITLE);
        mDirectoryPath = it.getStringExtra(IntentKey.DIRECTORY_PATH);
        mSearchType = it.getStringExtra(IntentKey.KEY_TYPE);
        initToolbar();
        initView();
        if (mSearchType.equals(IntentKey.DIRECTORY_KEY)) {
            readDirectory(mDirectoryPath);
        } else {
            searchFile();
        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_file_directory);
        toolbar.setTitle(mTitle.equals("") ? "常用" : mTitle);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, MyConfig.POST_DELAYED_TIME);
            }
        });
    }


    private void initView() {
        directoryLevelRv = (RecyclerView) findViewById(R.id.directoryLevel_rv_activity_file_directory);
        directoryLevelRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        directoryLevelRv.setItemAnimator(new DefaultItemAnimator());
        directoryLevelRv.setHasFixedSize(true);

        fileListRv = (RecyclerView) findViewById(R.id.fileList_rv_activity_file_directory);
        fileListRv.setLayoutManager(new LinearLayoutManager(this));
        fileListRv.setItemAnimator(new DefaultItemAnimator());
        fileListRv.setHasFixedSize(true);
    }


    private void readDirectory(String path) {
        // 配置目录级
        levelList.add(path);
        adapter = new LevelAdapter(levelList);
        adapter.setOnItemClickListener(new OnAdapterItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                final String directoryPath = levelList.get(position);
                Log.e(TAG, "-------------------------position="+position);
                Log.e(TAG, "-------------------------levelList.size="+levelList.size());
                int removeNum = levelList.size() - position;
                for (int i = 0; i < removeNum; i++) {
                    levelList.remove(levelList.size() - 1);//移除后levelList.size会自减
                }
                /** 延时200ms,以显示点击效果 */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        readDirectory(directoryPath);

                    }
                }, MyConfig.POST_DELAYED_TIME);
            }
        });
        directoryLevelRv.setAdapter(adapter);

        //配置文件列表
        if (!fileList.isEmpty()) { fileList.clear(); }
        File[] files = new File(path).listFiles();
        if (files == null) {
            Log.e(TAG, "-------------------------------     files == null, path="+path);
        } else {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File object1, File object2) {
                    return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                }
            });
            for (File file : files) {
                if (!file.canRead()){
                    continue;
                }
                if (file.isDirectory()) {
                    if (!file.getName().startsWith(".")) {
                        FileBean bean = new FileBean();
                        bean.setFileName(file.getName());
                        bean.setFilePath(file.getAbsolutePath());
                        bean.setFileType("dir");
                        bean.setCreateTime("");
                        fileList.add(bean);
                    }
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".stl") || fileName.endsWith(".obj") || fileName.endsWith(".3ds")) {
                        FileBean bean = new FileBean();
                        bean.setFileName(file.getName());
                        bean.setFilePath(file.getAbsolutePath());
                        bean.setFileType(FileUtils.getType(file.getAbsolutePath()));
                        bean.setCreateTime(TimeUtils.getTimeFormat(file.lastModified()));
                        fileList.add(bean);
                    }
                }
            }
        }
        fileAdapter = new FileRvListAdapter(this, fileList);
        fileAdapter.setOnItemClickListener(new OnAdapterItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                final String filepath = fileList.get(position).getFilePath();
                File file = new File(filepath);
                if (file.isDirectory()){
                    /** 延时300ms,以显示点击效果 */
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readDirectory(filepath);

                        }
                    }, 500);
                } else {
                    Log.e(TAG, "--------------------打开3D文件："+file.getName());
                    open3DFile(file);
                }
            }
        });
        fileListRv.setAdapter(fileAdapter);
    }


    /**
     *  打开模型文件
     * @param file
     */
    private void open3DFile(File file) {
        //  将文件信息存放数据库中
        DatabaseHelper databaseHelper = new DatabaseHelper(FileDirectoryActivity.this);
        FileBean fileBean = new FileBean();
        fileBean.setFilePath(file.getAbsolutePath());
        fileBean.setFileName(file.getName());
        fileBean.setFileType(FileUtils.getType(file.getName()));
        fileBean.setCreateTime(TimeUtils.getTimeFormat(file.lastModified()));
        databaseHelper.insert(fileBean);


        Log.e(TAG, "openFile: 3D文件的大小= "+file.length()/1024/1024+"M");
        if (file.length()>10*1024*1024) {
            ToastUtils.showLong(FileDirectoryActivity.this, "文件大于10M可能会解析失败");
        }
        Intent it = new Intent(FileDirectoryActivity.this, ShowModelActivity.class);
        it.putExtra("filePath", file.getAbsolutePath());
        startActivity(it);
    }


    private void searchFile() {
        // 配置目录级
        levelList.add(mSearchType);
        adapter = new LevelAdapter(levelList);
        directoryLevelRv.setAdapter(adapter);

        //检索各个常用文件夹
        String[] indexs = new String[]{MyConfig.QQFilePath, MyConfig.SystemDownloadPath, MyConfig.WPSFilePath, MyConfig.WXFilePath};
        for (int i = 0; i < indexs.length; i++) {//--------for1
            File[] files = new File(indexs[i]).listFiles();
            if (files == null) {
                Log.i(TAG, "searchFile: ------------目录："+indexs[i]+" 为空");
            } else {
                for (File file : files) {//--------for2
                    if (!file.canRead()) {
                        continue;
                    }

                    String fileType = FileUtils.getType(file.getName()).toLowerCase();
                    if (isSupportType(fileType)) {
                        FileBean bean = new FileBean();
                        bean.setFileName(file.getName());
                        bean.setFilePath(file.getAbsolutePath());
                        bean.setFileType(FileUtils.getType(file.getAbsolutePath()));
                        bean.setCreateTime(TimeUtils.getTimeFormat(file.lastModified()));
                        fileList.add(bean);
                    }
                }//for2
            }
        }//for1
        fileAdapter = new FileRvListAdapter(this, fileList);
        fileAdapter.setOnItemClickListener(this);
        fileListRv.setAdapter(fileAdapter);
    }


    private boolean isSupportType(String fileType) {
        if (mSearchType.equals("stl_key")) {
            if (fileType.equals("stl")) return true;
        } else if (mSearchType.equals("obj_key")) {
            if (fileType.equals("obj")) return true;
        } else if (mSearchType.equals("3ds_key")) {
            if (fileType.equals("3ds")) return true;
        } else if (mSearchType.equals("doc_key")) {
            if (fileType.equals("doc") || fileType.equals("docx")) return true;
        } else if (mSearchType.equals("ppt_key")) {
            if (fileType.equals("ppt") || fileType.equals("pptx")) return true;
        } else if (mSearchType.equals("xls_key")) {
            if (fileType.equals("xls") || fileType.equals("xlsx")) return true;
        } else if (mSearchType.equals("pdf_key")) {
            if (fileType.equals("pdf")) return true;
        }
        return false;
    }


    @Override
    public void onItemClick(View v, int position) {
        //TODO
        final String filepath = fileList.get(position).getFilePath();
        File file = new File(filepath);
        if (file.isDirectory()){
            /** 延时300ms,以显示点击效果 */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    readDirectory(filepath);

                }
            }, 500);
        } else {
            Log.e(TAG, "--------------------打开3D文件："+file.getName());
            open3DFile(file);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (levelList.size() > 1){//小于1时已为主目录，直接退出
                String parentPath = levelList.get(levelList.size()-2);
                //  移除当前目录和父级目录，父级目录会在readDirectory()中再次打开
                levelList.remove(levelList.size() - 1);
                levelList.remove(levelList.size() - 1);
                readDirectory(parentPath);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     *  目录级的适配器
     */
    private class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.MyViewHolder>{
        private List<String> mData;

        public LevelAdapter(List<String> data) {
            this.mData = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_directory_level, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.directoryName.setText(getDirectoryName(mData.get(position)));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }


        private String getDirectoryName(String directoryPath) {
            String name = directoryPath.substring(directoryPath.lastIndexOf("/") + 1);
            Log.i(TAG, "getDirectoryName: -------------name="+name);
            return name;
        }



        //自定义回调接口
        public OnAdapterItemListener itemListener;
        public void setOnItemClickListener(OnAdapterItemListener itemClickListener) {
            this.itemListener = itemClickListener;
        }
        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private LinearLayout directoryBody;
            private TextView directoryName;

            public MyViewHolder(View itemView) {
                super(itemView);
                directoryBody = (LinearLayout) itemView.findViewById(R.id.directory_ll_item_directory_level);
                directoryName = (TextView) itemView.findViewById(R.id.directoryName_ll_item_directory_level);

                directoryBody.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (itemListener != null){
                    itemListener.onItemClick(view, getLayoutPosition());
                }
            }
        }
    }

}
