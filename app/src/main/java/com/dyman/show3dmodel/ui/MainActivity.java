package com.dyman.show3dmodel.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.adapter.FileListAdapter;
import com.dyman.show3dmodel.db.DatabaseHelper;
import com.dyman.show3dmodel.bean.FileBean;
import com.dyman.show3dmodel.manager.SharePreferenceManager;
import com.dyman.show3dmodel.utils.DialogUtils;
import com.dyman.show3dmodel.utils.FileUtils;
import com.dyman.show3dmodel.utils.SnackBarUtils;
import com.dyman.show3dmodel.utils.TimeUtils;
import com.dyman.show3dmodel.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity{

    private static final String TAG = "MainActivity";

    private LinearLayout openFileLl;
    private SwipeMenuListView swipeLv;
    private FileListAdapter adapter;
    private List<FileBean> fileList = new ArrayList<>();

    DatabaseHelper databaseHelper;
    private boolean sureDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_main);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(MainActivity.this);

        initView();
        initDatas();
        checkAcceptFile();
    }


    private void initView() {
        openFileLl = (LinearLayout) findViewById(R.id.openFile_ll_content_main);
        openFileLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, OpenFileActivity.class);
                startActivity(it);
            }
        });

        swipeLv = (SwipeMenuListView) findViewById(R.id.recentFileList_swipeLv_content_main);
        swipeLv.setDividerHeight(0);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem shareItem = new SwipeMenuItem(getApplicationContext());
                shareItem.setBackground(new ColorDrawable((Color.rgb(0x3E, 0x82, 0xE9))));
                shareItem.setWidth(dp2px(90));
                shareItem.setTitle("Share");
                shareItem.setTitleSize(18);
                shareItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(shareItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setIcon(R.mipmap.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        swipeLv.setMenuCreator(creator);
        swipeLv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                final FileBean fileBean = fileList.get(position);
                switch (index) {
                    case 0://share
                        Uri fileUri = Uri.fromFile(new File(fileBean.getFilePath()));
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.setType("text/plain");
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.tip_share_to)));

                        break;
                    case 1://delete
                        sureDelete = true;
                        final int fileIndex = fileList.indexOf(fileBean);
                        fileList.remove(fileIndex);
                        adapter.notifyDataSetChanged();
                        SnackBarUtils.makeLong(swipeLv, getString(R.string.tip_already_delete_record))
                                .show(getString(R.string.tip_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sureDelete = false;
                                fileList.add(fileIndex, fileBean);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (sureDelete) {
                                    databaseHelper.delete(fileBean.getId());
                                }
                            }
                        }, 4000);

                        break;
                }
                return false;
            }
        });
        swipeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final FileBean fileBean = fileList.get(i);
                File file = new File(fileBean.getFilePath());
                if (file.canRead()) {
                    open3DFile(file);
                } else {
                    DialogUtils.showAlerDialog(MainActivity.this, getString(R.string.tip_is_delete_invalid_file),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            databaseHelper.delete(fileBean.getId());
                            dialogInterface.dismiss();
                        }
                    });
                }
            }
        });
    }


    private void initDatas() {
        //从数据库中读出打开过的3D文件
        fileList = databaseHelper.selectAll();
        adapter = new FileListAdapter(MainActivity.this, fileList);
        swipeLv.setAdapter(adapter);
    }


    /**
     *  检测是否接收到关联文件
     */
    private void checkAcceptFile() {
        Uri uri = (Uri) getIntent().getData();
        if (uri != null) {
            File file = new File(uri.getPath());
            open3DFile(file);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initDatas();
    }

    private void open3DFile(File file) {
        //  将文件信息存放数据库中
        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
        FileBean fileBean = new FileBean();
        fileBean.setFilePath(file.getAbsolutePath());
        fileBean.setFileName(file.getName());
        fileBean.setFileType(FileUtils.getType(file.getName()));
        fileBean.setCreateTime(TimeUtils.getTimeFormat(file.lastModified()));
        databaseHelper.insert(fileBean);

        Log.i(TAG, "openFile: 3D文件的大小= "+file.length()/1024/1024+"M");
        if (file.length()>10*1024*1024) {
            ToastUtils.showLong(MainActivity.this, getString(R.string.tip_file_maybe_analysis_fail));
        }
        Intent it = new Intent(MainActivity.this, ShowModelActivity.class);
//        Intent it = new Intent(MainActivity.this, ShowModelActivity.class);
        it.putExtra("filePath", file.getAbsolutePath());
        startActivity(it);
    }

    /**
     *  dp转px
     * @param dp
     * @return
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about){
            Intent it_about = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(it_about);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}