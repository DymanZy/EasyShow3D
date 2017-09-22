package com.dyman.easyshow3d.bean;

import android.content.Context;
import android.opengl.GLES20;


import com.dyman.easyshow3d.utils.LoadUtil;
import com.dyman.easyshow3d.utils.MatrixState;
import com.dyman.easyshow3d.utils.ShaderUtil;
import com.dyman.easyshow3d.view.ModelView;

import java.nio.FloatBuffer;

/**
 * Created by dyman on 16/7/25.
 */
public abstract class ModelObject {


    public abstract void parseModel(byte[] data, Context context);

    public abstract void initVertexData(float[] vertices, float[] normals);

    public abstract void cancelTask();

    private static final String TAG = "ModelObject";

    public static final int DRAW_MODEL = 0;//画基础模型
    public static final int DRAW_PROGRESS = 1;//画带进度的模型
    public int drawWay = DRAW_MODEL;

    public String modelType;
    public float[] color = new float[] {0.8f, 0.8f, 0.8f, 1};

    public float maxX;
    public float maxY;
    public float maxZ;
    public float minX;
    public float minY;
    public float minZ;

    /**
     *  模型打印大小比例
     */
    public float printScale = 1f;
    public float xRotateAngle = 0f;
    public float yRotateAngle = 0f;
    public float zRotateAngle = 0f;

    int mProgram = -1;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int muMMatrixHandle;//位置、旋转变换矩阵
    int maPositionHandle; //顶点位置属性引用
    int maNormalHandle; //顶点法向量属性引用
    int maLightLocationHandle;//光源位置属性引用
    int maCameraHandle; //摄像机位置属性引用
    int muColorHandle; //顶点颜色
    int muClipHandle; //剪裁平面属性引用

    String mVertexShader;//顶点着色器代码脚本
    String mFragmentShader;//片元着色器代码脚本

    FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer   mNormalBuffer;//顶点法向量数据缓冲
    int vCount=0;


    /**
     *  修正模型的大小
     * @param x
     * @param y
     * @param z
     */
    public void adjustMaxMin(float x, float y, float z) {
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (z > maxZ) {
            maxZ = z;
        }
        if (x < minX) {
            minX = x;
        }
        if (y < minY) {
            minY = y;
        }
        if (z < minZ) {
            minZ = z;
        }
    }


    /**
     * 初始化基础着色器
     * @param objView
     */
    public void initShader(ModelView objView) {
        //加载顶点着色器的脚本内容
        mVertexShader= ShaderUtil.loadFromAssetsFile("easy_show_vertex.sh", objView.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader=ShaderUtil.loadFromAssetsFile("easy_show_frag_color.sh", objView.getResources());
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用
        maNormalHandle= GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        //获取程序中光源位置引用
        maLightLocationHandle=GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        //获取程序中摄像机位置引用
        maCameraHandle=GLES20.glGetUniformLocation(mProgram, "uCamera");
        //获取程序中物体颜色的引用
        muColorHandle=GLES20.glGetUniformLocation(mProgram, "aColor");
    }


    /**
     *  初始化带剪切面的着色器
     * @param objView
     */
    public void initShaderWithClipPlane(ModelView objView) {
        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtil.loadFromAssetsFile("easy_show_vertex_clipplane.sh", objView.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtil.loadFromAssetsFile("easy_show_frag_clipplane.sh", objView.getResources());
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用
        maNormalHandle= GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        //获取程序中光源位置引用
        maLightLocationHandle=GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        //获取程序中摄像机位置引用
        maCameraHandle=GLES20.glGetUniformLocation(mProgram, "uCamera");
        //获取程序中剪裁平面引用
        muClipHandle=GLES20.glGetUniformLocation(mProgram, "u_clipPlane");
    }


    /**
     *  绘制基础模型
     */
    public void drawSelf(ModelView modelView) {
        if (mVertexBuffer == null || mNormalBuffer == null){ return; }

        if (mProgram == -1){
            initShader(modelView);
        }
        GLES20.glLineWidth(1f);
        //制定使用某套着色器程序
        GLES20.glUseProgram(mProgram);
        //将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        //将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
        //将光源位置传入着色器程序
        GLES20.glUniform3fv(maLightLocationHandle, 1, MatrixState.lightPositionFB);
        //将摄像机位置传入着色器程序
        GLES20.glUniform3fv(maCameraHandle, 1, MatrixState.cameraFB);
        // 将顶点位置数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                    maPositionHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    3*4,
                    mVertexBuffer
                );

        //传入顶点颜色数据
        GLES20.glUniform4fv(muColorHandle, 1, color, 0);

        //将顶点法向量数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                    maNormalHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    3*4,
                    mNormalBuffer
                );
        //启用顶点位置、法向量数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maNormalHandle);
        //绘制加载的物体
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }


    /**
     *  绘制带进度显示的模型
     */
    public void drawSelfWithProgress(float[] clipPlane, int drawMode, ModelView modelView) {
        if (mVertexBuffer == null || mNormalBuffer == null){ return; }

        if (mProgram == -1){
            initShaderWithClipPlane(modelView);
        }

        //制定使用某套着色器程序
        GLES20.glUseProgram(mProgram);
        //将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        //将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
        //将光源位置传入着色器程序
        GLES20.glUniform3fv(maLightLocationHandle, 1, MatrixState.lightPositionFB);
        //将摄像机位置传入着色器程序
        GLES20.glUniform3fv(maCameraHandle, 1, MatrixState.cameraFB);
        //将剪裁平面传入shader程序
        GLES20.glUniform4fv(muClipHandle, 1, LoadUtil.fromArrayToBuff(clipPlane));
        // 将顶点位置数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                    maPositionHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    3*4,
                    mVertexBuffer
                );
        //将顶点法向量数据传入渲染管线
        GLES20.glVertexAttribPointer
                (
                    maNormalHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    3*4,
                    mNormalBuffer
                );
        //启用顶点位置、法向量数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maNormalHandle);
        //绘制加载的物体
        GLES20.glDrawArrays(drawMode, 0, vCount);
    }

}
