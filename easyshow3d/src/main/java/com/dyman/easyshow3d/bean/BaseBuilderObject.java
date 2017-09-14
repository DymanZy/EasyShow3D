package com.dyman.easyshow3d.bean;

import android.opengl.GLES20;

import com.dyman.easyshow3d.utils.MatrixState;
import com.dyman.easyshow3d.utils.ShaderUtil;
import com.dyman.easyshow3d.view.ModelView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyman on 2016/11/18.
 *  3D显示基础舞台的搭建，包括网格和坐标系
 */

public class BaseBuilderObject {
    private static final String TAG = "BaseBuilderObject";

    private ModelView modelView;
    /** 模型的大小 */
    private float mHeight = 0f;
    /** 打印机底座宽度的大小 */
    private int mWidth = 100;
    private int mUnit = 5;
    private float lineWidth = 3f;
    /** 渲染颜色 */
    private float[] color = new float[]{0.9f, 0.9f, 0.9f, 1f};
    private float[] color1 = new float[]{0.9f, 0f, 0f, 1f};

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

    public BaseBuilderObject(ModelView modelView) {
        this.modelView = modelView;
    }

    public BaseBuilderObject(ModelView modelView, float modelHeight, int printerWidth) {
        this.modelView = modelView;
        this.mHeight = modelHeight;
        this.mWidth = printerWidth;
    }

    public BaseBuilderObject(ModelView modelView, float modelHeight, int printerWidth, int lineUnit, int lineWidth) {
        this.modelView = modelView;
        this.mHeight = modelHeight;
        this.mWidth = printerWidth;
        this.mUnit = lineUnit;
        this.lineWidth = lineWidth;
    }

    /**
     *  设置最大模型的高度
     * @param mHeight
     */
    public void setHeight(float mHeight) {
        this.mHeight = mHeight;
    }


    /**
     * 初始化基础着色器
     * @param modelView
     */
    public void initShader(ModelView modelView) {
        //加载顶点着色器的脚本内容
        mVertexShader= ShaderUtil.loadFromAssetsFile("easy_show_vertex.sh", modelView.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader=ShaderUtil.loadFromAssetsFile("easy_show_frag_color.sh", modelView.getResources());
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


    public void drawOrigin() {
        if (mProgram == -1){
            initShader(modelView);
        }
        GLES20.glLineWidth(lineWidth);
        float[] vertexArray = {
//                0, 0, 0, 100, 0, 0
//                0, 0, 0, 0, 100, 0
                0, 0, 0, 0, 0, 100
        };
        FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);


        GLES20.glLineWidth(5f);
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
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3*4, lineBuffer);
        //启用顶点位置、法向量数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glUniform4fv(muColorHandle, 1, color1, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);


    }


    /**
     * 画网格
     */
    public void drawGrids() {
        if (mProgram == -1){
            initShader(modelView);
        }

        List<Float> lineList = new ArrayList<Float>();
        //  xy面的网格，总长200毫米，各方向40格
        for (int x = -mWidth; x <= mWidth; x += mUnit) {
            lineList.add(-100f);
            lineList.add(-mHeight);
            lineList.add((float) x);
            lineList.add(100f);
            lineList.add(-mHeight);
            lineList.add((float) x);
        }
        for (int y = -mWidth; y <= mWidth; y += mUnit) {
            lineList.add((float) y);
            lineList.add(-mHeight);
            lineList.add(-100f);
            lineList.add((float) y);
            lineList.add(-mHeight);
            lineList.add(100f);
        }
        // 将坐标点转到FloatBuffer里面，再传入渲染管线
        FloatBuffer lineBuffer = getFloatBufferFromList(lineList);

        GLES20.glLineWidth(lineWidth);
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
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3*4, lineBuffer);
        //传入顶点颜色数据
        GLES20.glUniform4fv(muColorHandle, 1, color, 0);
        //启用顶点位置、法向量数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        //绘制加载的物体
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, lineList.size()/3);
    }


    /**
     *  画打印机的方框，标示打印范围
     */
    public void drawBorder() {
        if (mProgram == -1){
            initShader(modelView);
        }
        float borderHeight = 200 - mHeight;  // 打印机的高：200
        GLES20.glLineWidth(lineWidth);
        float[] vertexArray = {
                -100, -mHeight, -100, -100, borderHeight, -100,
                -100, -mHeight, 100, -100, borderHeight, 100,
                100, -mHeight, 100, 100, borderHeight, 100,
                100, -mHeight, -100, 100, borderHeight, -100,
                -100, borderHeight, -100, -100, borderHeight, 100,
                -100, borderHeight, 100, 100, borderHeight, 100,
                100, borderHeight, 100, 100, borderHeight, -100,
                100, borderHeight, -100, -100, borderHeight, -100
        };
        FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);


        GLES20.glLineWidth(5f);
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
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3*4, lineBuffer);
        //传入顶点颜色数据
        GLES20.glUniform4fv(muColorHandle, 1, color, 0);
        //启用顶点位置、法向量数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);


        //绘制加载的物体
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexArray.length/3);
    }


    public FloatBuffer fromArrayToBuff(float[] a) {
        ByteBuffer llbb = ByteBuffer.allocateDirect(a.length*4);
        llbb.order(ByteOrder.nativeOrder());//设置字节顺序
        FloatBuffer result=llbb.asFloatBuffer();
        result.put(a);
        result.position(0);
        return result;
    }


    /**
     *  List<Float> 转 FloatBuffer
     * @param vertexList
     * @return
     */
    private FloatBuffer getFloatBufferFromList(List<Float> vertexList) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexList.size() * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer triangleBuffer = vbb.asFloatBuffer();
        float[] array = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            array[i] = vertexList.get(i);
        }
        triangleBuffer.put(array);
        triangleBuffer.position(0);
        return triangleBuffer;
    }


    private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer triangleBuffer = vbb.asFloatBuffer();
        triangleBuffer.put(vertexArray);
        triangleBuffer.position(0);
        return triangleBuffer;
    }


}
