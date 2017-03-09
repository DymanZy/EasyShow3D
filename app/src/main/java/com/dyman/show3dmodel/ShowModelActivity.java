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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.dyman.show3dmodel.bean.ModelObject;
import com.dyman.show3dmodel.bean.ObjObject;
import com.dyman.show3dmodel.bean.ObjProObject;
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

public class ShowModelActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "ShowModelActivity";
    private FrameLayout showLayout;
    private FloatingActionButton printFab;
    private byte[] modelBytes;
    private ShowModelView sModelView;
    private String filePath;
    private ModelObject modelObject;

    private TextView showLevelTv;
    private SeekBar changeModelLevelSb;
    private Button leftBtn;
    private Button rightBtn;
    private ImageView xMode;
    private ImageView zMode;
    private ImageView zoomMode;

    private final static int CHANGE_X_MODE = 300;     // 改变x轴角度
    private final static int CHANGE_Z_MODE = 301;     // 改变z轴角度
    private final static int CHANGE_ZOOM_MODE = 302;  // 改变打印大小
    private int CHANGE_MODE = CHANGE_ZOOM_MODE;


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
        changeModelLevelSb = (SeekBar) findViewById(R.id.changeModelLevel_seekBar_activity_show_model);
        changeModelLevelSb.setOnSeekBarChangeListener(this);

        showLevelTv = (TextView) findViewById(R.id.level_tv_activity_show_model);
        leftBtn = (Button) findViewById(R.id.left_btn_activity_show_model);
        rightBtn = (Button) findViewById(R.id.right_btn_activity_show_model);
        xMode = (ImageView) findViewById(R.id.xMode_iv_activity_show_model);
        zMode = (ImageView) findViewById(R.id.zMode_iv_activity_show_model);
        zoomMode = (ImageView) findViewById(R.id.zoomMode_iv_activity_show_model);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);
        xMode.setOnClickListener(this);
        zMode.setOnClickListener(this);
        zoomMode.setOnClickListener(this);


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


    // 只改x和z轴转向
    @Override
    public void onClick(View v) {
        int progress = 0;
        switch (v.getId()) {
            case R.id.xMode_iv_activity_show_model:
                CHANGE_MODE = CHANGE_X_MODE;
                progress = (int)modelObject.xRotateAngle + 180;
                changeModelLevelSb.setMax(360);
                changeModelLevelSb.setProgress(progress);
                showLevelTv.setText(String.valueOf(modelObject.xRotateAngle)+"°");
                break;

            case R.id.zoomMode_iv_activity_show_model:
                progress = (int)(modelObject.printScale*10);
                CHANGE_MODE = CHANGE_ZOOM_MODE;
                changeModelLevelSb.setMax(20);
                changeModelLevelSb.setProgress(progress);
                showLevelTv.setText(String.valueOf(modelObject.printScale)+"倍");
                break;

            case R.id.zMode_iv_activity_show_model:
                progress = (int)modelObject.zRotateAngle + 180;
                CHANGE_MODE = CHANGE_Z_MODE;
                changeModelLevelSb.setMax(360);
                changeModelLevelSb.setProgress(progress);
                showLevelTv.setText(String.valueOf(modelObject.zRotateAngle)+"°");
                break;

            case R.id.right_btn_activity_show_model:
                clickAdjustBtn(1);
                break;

            case R.id.left_btn_activity_show_model:
                clickAdjustBtn(0);
                break;
        }
    }



    private void clickAdjustBtn (int side) {
        float range;
        if (CHANGE_MODE == CHANGE_ZOOM_MODE) {
            range = 0.1f;
        } else {
            range = 90f;
        }

        if (side == 0) {
            range = -range;
        }

        switch (CHANGE_MODE) {
            case CHANGE_X_MODE:
                modelObject.xRotateAngle += range;
                if (modelObject.xRotateAngle < -180f) modelObject.xRotateAngle = -180f;
                if (modelObject.xRotateAngle > 180f) modelObject.xRotateAngle = 180f;
                changeModelLevelSb.setProgress((int) modelObject.xRotateAngle + 180);
                showLevelTv.setText(String.valueOf(modelObject.xRotateAngle)+"°");
                break;
            case CHANGE_ZOOM_MODE:
                modelObject.printScale += range;
                if (modelObject.printScale < 0.1f) modelObject.printScale = 0.1f;
                if (modelObject.printScale > 2.0f) modelObject.printScale = 2.0f;
                changeModelLevelSb.setProgress((int) (modelObject.printScale*10));
                showLevelTv.setText(String.valueOf(modelObject.printScale)+"倍");
                break;
            case CHANGE_Z_MODE:
                modelObject.zRotateAngle += range;
                if (modelObject.zRotateAngle < -180f) modelObject.zRotateAngle = -180f;
                if (modelObject.zRotateAngle > 180f) modelObject.zRotateAngle = 180f;
                changeModelLevelSb.setProgress((int) modelObject.zRotateAngle + 180);
                showLevelTv.setText(String.valueOf(modelObject.zRotateAngle)+"°");
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (CHANGE_MODE) {
            case CHANGE_X_MODE:
                Log.i(TAG, "onProgressChanged: ----x---- progress = "+progress);
                modelObject.xRotateAngle = (float) (progress - 180);
                showLevelTv.setText(String.valueOf(modelObject.xRotateAngle)+"°");
                break;
            case CHANGE_ZOOM_MODE:
                Log.i(TAG, "onProgressChanged: ----x---- progress = "+progress);
                if (modelObject != null) {
                    modelObject.printScale = (float)progress / 10;
                    showLevelTv.setText(String.valueOf(modelObject.printScale)+"倍");
                }
                break;
            case CHANGE_Z_MODE:
                Log.i(TAG, "onProgressChanged: ----x---- progress = "+progress);
                modelObject.zRotateAngle = (float) (progress - 180);
                showLevelTv.setText(String.valueOf(modelObject.zRotateAngle)+"°");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { // do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { // do nothing
    }


    /**
     *  3D文件解析结果的回调
     */
    class ReadFinish implements ModelObject.IFinishCallBack{

        @Override
        public void readModelFinish() {
            if (modelObject != null) {
                if (sModelView == null){
                    sModelView = new ShowModelView(ShowModelActivity.this, modelObject);
                    showLayout.addView(sModelView);
                    sModelView.requestFocus();
                    sModelView.setFocusableInTouchMode(true);
                    Log.e(TAG, "readModelFinish: -----------------------------------------------");
                    Log.e(TAG, "xRotateAngle = "+modelObject.xRotateAngle);
                    Log.e(TAG, "zRotateAngle = "+modelObject.zRotateAngle);
                    Log.e(TAG, "printScale = "+modelObject.printScale);
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
