package com.dyman.show3dmodel.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dyman.show3dmodel.R;
import com.hanuor.onyx.Onyx;
import com.hanuor.onyx.hub.OnTaskCompletion;

import java.io.InputStream;
import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView resultTv;

    private static final int RESULT_LOAD_IMAGE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initView();
    }


    private void initView() {

        imageView = (ImageView) findViewById(R.id.image_activity_test);
        resultTv = (TextView) findViewById(R.id.result_tv_activity_test);

        findViewById(R.id.check_btn_activity_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            analysisImage(selectedImage.toString());
        }
    }


    private void analysisImage(String url) {
//        Log.i("TAG", "analysisImage: ++++++++++++++"+url);
        url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492014115881&di" +
                "=f0c20a4ba0c91e8de5a6cdb1cc23a8ca&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F110820%2F1369-110R01RZ682.jpg";
        Onyx.with(TestActivity.this).fromURL(url).getTagsandProbability(new OnTaskCompletion() {
            @Override
            public void onComplete(ArrayList<String> response) {

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < response.size(); i++) {
                    String str = response.get(i);
                    sb.append(str+"; ");
                }
                sb.deleteCharAt(sb.length() - 1);
                Log.i("TAG", "onComplete: =========="+sb);
            }
        });
    }
}
