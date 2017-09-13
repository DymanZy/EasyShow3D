package com.dyman.show3dmodel.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dyman.easyshow3d.ModelFactory;
import com.dyman.easyshow3d.bean.ModelObject;
import com.dyman.easyshow3d.imp.ModelLoaderListener;
import com.dyman.easyshow3d.view.ShowModelView;
import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.config.MyConfig;
import com.dyman.show3dmodel.utils.DialogUtils;
import com.dyman.show3dmodel.utils.ToastUtils;

import java.io.File;

public class ShowModelActivity extends BaseActivity {

    private static final String TAG = "ShowModelActivity";
    private ShowModelView sModelView;
    private String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_model);
        filePath = getIntent().getStringExtra("filePath");


        initToolBar();
        initView();
        isHaveFile(filePath);
    }


    /**
     * 初始化Toolbar
     */
    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_show_model);
        setSupportActionBar(toolbar);

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        if (fileName.length() > 15) {
            toolbar.setTitle(fileName.substring(0, 15) + "...");
        } else if (fileName.length() > 0) {
            toolbar.setTitle(fileName);
        }

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
        sModelView = (ShowModelView) findViewById(R.id.showModelView);
    }


    /**
     * 检查模型是否存在，存在则进行解析
     *
     * @param filePath
     */
    private void isHaveFile(String filePath) {
        if (filePath != null && !filePath.equals("")) {
            loadModel(filePath);
        } else {
            ToastUtils.showShort(ShowModelActivity.this, getString(R.string.text_file_not_exist));
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_model, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_setting:
                Dialog showDialog = DialogUtils.showSettingDialog(ShowModelActivity.this);
                showDialog.show();
                break;

            case R.id.menu_renderer_setting:
                Dialog renderDialog = DialogUtils.renderSettingDialog(ShowModelActivity.this);
                renderDialog.show();
                break;

            case R.id.menu_share:
                Uri fileUri = Uri.fromFile(new File(filePath));
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.tip_share_to)));
                break;
        }
        return true;
    }


    private void loadModel(String filePath) {
        ModelFactory.decodeFile(ShowModelActivity.this, filePath, new ModelLoaderListener() {
            @Override
            public void loadedUpdate(float progress) {
                Log.i(TAG, "模型解析进度： " + progress);
            }

            @Override
            public void loadedFinish(ModelObject modelObject) {
                sModelView.setModelObject(modelObject);
            }

            @Override
            public void loaderCancel() {

            }
        });
    }
}
