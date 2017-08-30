package com.dyman.show3dmodel.ui;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.adapter.FolderListAdapter;
import com.dyman.show3dmodel.adapter.listener.OnAdapterItemListener;
import com.dyman.show3dmodel.bean.FolderBean;
import com.dyman.show3dmodel.config.IntentKey;
import com.dyman.show3dmodel.config.MyConfig;
import com.dyman.show3dmodel.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *  文件列表菜单Activity
 */
public class OpenFileActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "OpenFileActivity";
    private LinearLayout openRootDir;
    private RecyclerView folderRv;
    private FolderListAdapter adapter;
    private List<FolderBean> folderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);
        initToolbar();
        initView();
        initDatas();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_openFile);
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
        openRootDir = (LinearLayout) findViewById(R.id.openRootDir_ll_activity_open_file);
        folderRv = (RecyclerView) findViewById(R.id.frequentFolder_rv_activity_open_file);
        folderRv.setLayoutManager(new LinearLayoutManager(this));
        folderRv.setItemAnimator(new DefaultItemAnimator());
        folderRv.setHasFixedSize(true);

        openRootDir.setOnClickListener(this);
        findViewById(R.id.stlFile_rl_activity_open_file).setOnClickListener(this);
        findViewById(R.id.objFile_rl_activity_open_file).setOnClickListener(this);
        findViewById(R.id.d3sFile_rl_activity_open_file).setOnClickListener(this);
    }


    private void initDatas() {
        //能否改为动态检测常用文件夹？
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!folderList.isEmpty()) { folderList.clear(); }

        FolderBean folderBean = new FolderBean();
        folderBean.setFolderName(getString(R.string.text_qq));
        folderBean.setFolderPath(rootPath+File.separator+"tencent"+File.separator+"QQfile_recv");
        if (new File(folderBean.getFolderPath()).canRead()) {
            folderList.add(folderBean);
        }

        folderBean = new FolderBean();
        folderBean.setFolderName(getString(R.string.text_download));
        folderBean.setFolderPath(rootPath+File.separator+"Download");
        if (new File(folderBean.getFolderPath()).canRead()) {
            folderList.add(folderBean);
        }

        folderBean = new FolderBean();
        folderBean.setFolderName(getString(R.string.text_document));
        folderBean.setFolderPath(rootPath+File.separator+"documents");
        if (new File(folderBean.getFolderPath()).canRead()) {
            folderList.add(folderBean);
        }

        folderBean = new FolderBean();
        folderBean.setFolderName(getString(R.string.text_weixin));
        folderBean.setFolderPath(rootPath+File.separator+"tencent"+File.separator+"MicroMsg"+File.separator+"Download");
        if (new File(folderBean.getFolderPath()).canRead()) {
            folderList.add(folderBean);
        }

        adapter = new FolderListAdapter(folderList);
        adapter.setOnItemClickListener(new OnAdapterItemListener() {
            @Override
            public void onItemClick(View v, int position) {
                FolderBean bean = folderList.get(position);
                Intent it = new Intent(OpenFileActivity.this, FileDirectoryActivity.class);
                it.putExtra(IntentKey.TITLE, bean.getFolderName());
                it.putExtra(IntentKey.KEY_TYPE, IntentKey.DIRECTORY_KEY);
                it.putExtra(IntentKey.DIRECTORY_PATH, bean.getFolderPath());
                startActivity(it);
            }
        });

        folderRv.setAdapter(adapter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openRootDir_ll_activity_open_file:
                ToastUtils.showShort(OpenFileActivity.this, getString(R.string.text_open_root_dir));
                Intent it = new Intent(OpenFileActivity.this, FileDirectoryActivity.class);
                it.putExtra(IntentKey.TITLE, "手机");
                it.putExtra(IntentKey.KEY_TYPE, IntentKey.DIRECTORY_KEY);
                it.putExtra(IntentKey.DIRECTORY_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
                startActivity(it);
                break;
            case R.id.stlFile_rl_activity_open_file:
                ToastUtils.showShort(OpenFileActivity.this, getString(R.string.text_search_stl));
                searchFileWithType("STL文件", "stl_key");
                break;
            case R.id.objFile_rl_activity_open_file:
                ToastUtils.showShort(OpenFileActivity.this, getString(R.string.text_search_obj));
                searchFileWithType("STL文件", "obj_key");
                break;
            case R.id.d3sFile_rl_activity_open_file:
                ToastUtils.showShort(OpenFileActivity.this, getString(R.string.text_search_ds));
                searchFileWithType("STL文件", "3ds_key");
                break;
        }
    }


    private void searchFileWithType(String title, String typeKey) {
        Intent it = new Intent(OpenFileActivity.this, FileDirectoryActivity.class);
        it.putExtra(IntentKey.TITLE, title);
        it.putExtra(IntentKey.KEY_TYPE, typeKey);
        startActivity(it);
    }
}
