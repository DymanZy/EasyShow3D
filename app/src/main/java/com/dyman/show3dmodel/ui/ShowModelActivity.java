package com.dyman.show3dmodel.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import com.dyman.show3dmodel.utils.FileUtils;
import com.dyman.show3dmodel.utils.TimeUtils;
import com.dyman.show3dmodel.utils.ToastUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShowModelActivity extends BaseActivity {

    private static final String TAG = "ShowModelActivity";
    private ShowModelView sModelView;
    private String filePath;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_model);
        filePath = getIntent().getStringExtra("filePath");

        initToolBar();
        initView();
        isExist(filePath);

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

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.model_load_progress_title));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ModelFactory.cancelDecode();
            }
        });
        dialog.setMax(100);
    }


    /**
     * 检查模型是否存在，存在则进行解析
     *
     * @param filePath
     */
    private void isExist(String filePath) {
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


    long loadTime = 0;
    private void loadModel(String filePath) {

        dialog.show();
        ModelFactory.decodeFile(ShowModelActivity.this, filePath, new ModelLoaderListener() {
            @Override
            public void loadBegin() {
                loadTime = System.currentTimeMillis();
            }

            @Override
            public void loadedUpdate(float progress) {
                dialog.setProgress((int) (progress * 100));
            }

            @Override
            public void loadedFinish(ModelObject modelObject) {
                if (modelObject != null) {
                    sModelView.setModelObject(modelObject);
                    dialog.dismiss();

                    Log.e(TAG, FileUtils.getName(filePath) +
                            " time：" + TimeUtils.timeTransfer(System.currentTimeMillis() - loadTime) +
                            " size：" + FileUtils.fileSizeTransfer(FileUtils.getSize(filePath)));
                }
            }

            @Override
            public void loaderCancel() {
                Log.i(TAG, "loaderCancel() was be called!");
                ToastUtils.showShort(ShowModelActivity.this, "已取消");
                dialog.dismiss();
                finish();
            }

            @Override
            public void loaderFailure() {
                Log.e(TAG, "decode model file failure!");
                ToastUtils.showShort(ShowModelActivity.this, "模型解析失败");
                dialog.dismiss();
            }
        });
    }
}
