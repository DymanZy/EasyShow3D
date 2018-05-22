package com.dyman.easyshow3d.bean;

import android.content.Context;
import android.util.Log;

import com.dyman.easyshow3d.imp.ModelLoaderListener;
import com.dyman.easyshow3d.thread.AnalysisThreadHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by dyman on 18/5/16.
 */

public class ObjProObject extends ModelObject{

    private static final String TAG = ObjProObject.class.getName();

    ModelLoaderListener listener;
    AnalysisThreadHelper helper;


    public ObjProObject(byte[] objByte, Context context, int drawMode, ModelLoaderListener listener) {
        this.modelType = "obj";
        this.drawWay = drawMode;
        this.listener = listener;

        parseModel(objByte, context);
    }

    @Override
    public void parseModel(byte[] data, Context context) {
        Log.e(TAG, "start parse model");
        helper = new AnalysisThreadHelper(3, this, listener);
        helper.analysis(data);
    }

    /**
     * 初始化顶点坐标与着色数据的方法(Obj和Stl的不同)
     * @param vertices
     * @param normals
     */
    public void initVertexData(float[] vertices,float[] normals) {
        //顶点坐标数据的初始化================begin============================
        vCount=vertices.length/3;


        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================



        //顶点法向量数据的初始化================begin============================
        ByteBuffer cbb = ByteBuffer.allocateDirect(normals.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mNormalBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点着色数据的初始化================end============================
    }

    @Override
    public void cancelTask() {
        //  TODO: 停止AnalysisThreadHelper的工作
        Log.e(TAG, "cancelTask:     TODO: 停止AnalysisThreadHelper的工作");
    }
}
