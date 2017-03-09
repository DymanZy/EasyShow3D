package com.dyman.show3dmodel.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dyman.show3dmodel.bean.FileBean;
import com.dyman.show3dmodel.manager.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyman on 16/8/18.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private Context context;
    private static final String DATABASE_NAME = "show3d_db";
    private static final String TABLE_NAME = "opened_file";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public DatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.context = context;
    }

    public DatabaseHelper(Context context){
        this(context, DATABASE_NAME, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                FileBean.ID + " integer primary key autoincrement," +
                FileBean.FILEPATH + " varchar," +
                FileBean.FILENAME + " varchar," +
                FileBean.FILETYPE + " varchar," +
                FileBean.CREATETIME + " varchar," +
                "recentOpenTime" + " long" +
                ")";
        db.execSQL(sql);
        System.out.println("create a database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        System.out.println("upgrade a database");
    }


    /**
     * 插入数据，自检测是否重复插入
     * @param bean
     */
    public void insert(FileBean bean) {
        ContentValues values = new ContentValues();

        values.put(FileBean.FILEPATH, bean.getFilePath());
        values.put(FileBean.FILENAME, bean.getFileName());
        values.put(FileBean.FILETYPE, bean.getFileType().toLowerCase());
        values.put(FileBean.CREATETIME, bean.getCreateTime());
        values.put("recentOpenTime", System.currentTimeMillis());

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase sqliteDatabase = databaseHelper.getWritableDatabase();
        if (!isHave(bean.getFilePath())) {
            sqliteDatabase.insert(TABLE_NAME, null, values);
            System.out.println("--------------sql---------insert----------fileName="+bean.getFileName());
        } else {
            sqliteDatabase.update(TABLE_NAME, values, FileBean.FILEPATH+"=?", new String[]{bean.getFilePath
                    ()});
        }

        sqliteDatabase.close();
        databaseHelper.close();
    }


    public List<FileBean> selectAll() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, "recentOpenTime desc");

        List<FileBean> list = new ArrayList<>();
        while(cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(FileBean.ID));
            String filePath = cursor.getString(cursor.getColumnIndex(FileBean.FILEPATH));
            String fileName = cursor.getString(cursor.getColumnIndex(FileBean.FILENAME));
            String fileType = cursor.getString(cursor.getColumnIndex(FileBean.FILETYPE));
            String createTime = cursor.getString(cursor.getColumnIndex(FileBean.CREATETIME));

            FileBean bean = new FileBean();
            bean.setId(id);
            bean.setFilePath(filePath);
            bean.setFileName(fileName);
            bean.setFileType(fileType);
            bean.setCreateTime(createTime);

            list.add(bean);
        }

        cursor.close();
        sqLiteDatabase.close();
        databaseHelper.close();

        return  list;
    }


    public void delete(String fileID) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME, FileBean.ID+"=?", new String[]{fileID});
        System.out.println("---------------sql-------delete-------------- fileID="+fileID);
        sqLiteDatabase.close();
        databaseHelper.close();
    }

    private boolean isHave(String filePath) {
        boolean result = false;

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NAME,
                new String[] {FileBean.ID},
                FileBean.FILEPATH+"=?",
                new String[]{filePath},
                null, null, null);

        if (cursor.moveToFirst()) {
            System.out.println("该数据存在");
            result = true;
        } else {
            System.out.println("该数据不存在");
        }

        cursor.close();
        sqLiteDatabase.close();
        databaseHelper.close();
        return result;
    }

}
