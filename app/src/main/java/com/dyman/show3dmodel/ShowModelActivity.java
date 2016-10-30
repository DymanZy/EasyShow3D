package com.dyman.show3dmodel;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;


import com.dyman.show3dmodel.bean.ModelObject;
import com.dyman.show3dmodel.bean.ObjObject;
import com.dyman.show3dmodel.bean.StlObject;
import com.dyman.show3dmodel.config.MyConfig;
import com.dyman.show3dmodel.utils.DialogUtils;
import com.dyman.show3dmodel.utils.FileUtils;
import com.dyman.show3dmodel.utils.IOUtils;
import com.dyman.show3dmodel.utils.ToastUtils;
import com.dyman.show3dmodel.view.Show3dsMd2View;
import com.dyman.show3dmodel.view.ShowModelView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ShowModelActivity extends BaseActivity {

    private static final String TAG = "ShowModelActivity";
    private FrameLayout showLayout;
    private FloatingActionButton printFab;
    private byte[] modelBytes;
    private ShowModelView sModelView;
    private String filePath;
    private ModelObject modelObject;

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
     *  初始化Toolbar
     */
    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_show_model);
        setSupportActionBar(toolbar);

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        if (fileName.length() > 15){
            toolbar.setTitle(fileName.substring(0,15)+"...");
        } else if (fileName.length() > 0){
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
        showLayout = (FrameLayout) findViewById(R.id.showLayout_fl_activity_show_model);
    }

    /**
     *  检查模型是否存在，存在则进行解析
     * @param filePath
     */
    private void isHaveFile(String filePath){
        if (filePath != null && !filePath.equals("")) {
            File file = new File(filePath);
            openModelFile(file);
        } else {
            ToastUtils.showShort(ShowModelActivity.this, "文件不存在");
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
                ToastUtils.showShort(ShowModelActivity.this, "点击了设置显示");
                Dialog showDialog = DialogUtils.showSettingDialog(ShowModelActivity.this);
                showDialog.show();
                break;

            case R.id.menu_renderer_setting:
                ToastUtils.showShort(ShowModelActivity.this, "点击了设置渲染");
                Dialog renderDialog = DialogUtils.renderSettingDialog(ShowModelActivity.this);
                renderDialog.show();
                break;

            case R.id.menu_share:
                Uri fileUri = Uri.fromFile(new File(filePath));
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "分享到"));
                break;
        }
        return true;
    }

    /**
     *  获取文件字节
     * @param context
     * @param uri
     * @return
     * @throws IOException
     */
    private byte[] getFileBytes(Context context, Uri uri) throws IOException {
        byte[] objBytes = null;
        InputStream inputStream = null;
        inputStream = context.getContentResolver().openInputStream(uri);
        objBytes = IOUtils.toByteArray(inputStream);
        return  objBytes;
    }


    class ReadFinish implements ModelObject.IFinishCallBack{
        @Override
        public void readModelFinish() {
            if (modelObject != null) {
                if (sModelView == null){
                    sModelView = new ShowModelView(ShowModelActivity.this, modelObject);
                    showLayout.addView(sModelView);
                    sModelView.requestFocus();
                    sModelView.setFocusableInTouchMode(true);
                }
            }
        }

        @Override
        public void cancelLoadModel() {
            finish();
        }
    }


    /**
     * 打开并解析模型文件，自动区分obj和stl文件处理
     * @param file
     */
    private void openModelFile(File file) {
        if (sModelView != null){
            showLayout.removeAllViews();
            System.gc();
        }

        try{
            modelBytes = getFileBytes(ShowModelActivity.this, Uri.fromFile(file));
        } catch (Exception e){
            ToastUtils.showShort(ShowModelActivity.this, "文件过大，解析失败");
        }

        if (modelBytes == null){
            ToastUtils.showShort(ShowModelActivity.this, "解析文件失败");
        }
        String fileType = FileUtils.getType(file.getName()).toLowerCase();
        if (fileType.equals("obj")) {
            modelObject = new ObjObject(modelBytes, ShowModelActivity.this,
                    ModelObject.DRAW_MODEL, new ReadFinish());
        } else if(fileType.equals("stl")){
            modelObject = new StlObject(modelBytes, ShowModelActivity.this,
                    ModelObject.DRAW_MODEL, new ReadFinish());
        } else if(fileType.equals("3ds")) {
            Show3dsMd2View sv = new Show3dsMd2View(ShowModelActivity.this, file.getAbsolutePath());
            showLayout.addView(sv);
            sv.requestFocus();
            sv.setFocusableInTouchMode(true);
        }
    }
}
