package com.dyman.show3dmodel.test;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.utils.MatrixState;
import com.dyman.show3dmodel.utils.ShaderUtil;
import com.dyman.show3dmodel.view.ModelView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DrawGridActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_grid);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.id_mGLSurfaceView);

        MyRenderer renderer = new MyRenderer();
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.requestRender();
    }



    class MyRenderer implements GLSurfaceView.Renderer {



        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glEnable(GL10.GL_BLEND);
//		 gl.glEnable(GL10.GL_TEXTURE_2D);
//		 gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR);
            // FIXME This line seems not to be needed?
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(3152, 4354);
            gl.glEnable(GL10.GL_NORMALIZE);
            gl.glShadeModel(GL10.GL_SMOOTH);

            gl.glMatrixMode(GL10.GL_PROJECTION);

            // Lighting
            gl.glEnable(GL10.GL_LIGHTING);
            gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, getFloatBufferFromArray(new  float[]{0.5f,0.5f,0.5f,1.0f}));// 全局环境光
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT_AND_DIFFUSE, new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { 0f, 0f, 1000f, 1.0f }, 0);
            gl.glEnable(GL10.GL_LIGHT0);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            float aspectRatio = (float) width / height;

            gl.glViewport(0, 0, width, height);

            gl.glLoadIdentity();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            GLU.gluLookAt(gl, 0, 0, 100f, 0, 0, 0, 0, 1f, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glLoadIdentity();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // draw X-Y field
            drawGrids(gl);
            drawLines(gl);
        }


        private void drawGrids(GL10 gl) {
            List<Float> lineList = new ArrayList<Float>();

            for (int x = -100; x <= 100; x += 5) {
                lineList.add((float) x);
                lineList.add(-100f);
                lineList.add(0f);
                lineList.add((float)x);
                lineList.add(100f);
                lineList.add(0f);
            }
            for (int y = -100; y <= 100; y += 5) {
                lineList.add(-100f);
                lineList.add((float) y);
                lineList.add(0f);
                lineList.add(100f);
                lineList.add((float) y);
                lineList.add(0f);
            }

            FloatBuffer lineBuffer = getFloatBufferFromList(lineList);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);


            gl.glLineWidth(1f);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
            gl.glDrawArrays(GL10.GL_LINES, 0, lineList.size() / 3);
        }


        /**
         * 画坐标
         * @param gl
         */
        private void drawLines(GL10 gl){
            gl.glLineWidth(3f);
            float[] vertexArray = {
                    -100, 0, 0, 100, 0, 0,      // x
                    0, -100, 0, 0, 100, 0,      // y
                    0, 0, -100, 0, 0, 100 };    // z
            FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

            // X : red
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 1.0f, 0f, 0f, 0.75f }, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 1.0f, 0f, 0f, 0.5f }, 0);
            gl.glDrawArrays(GL10.GL_LINES, 0, 2);

            // Y : blue
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 0f, 1.0f, 0.75f }, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 0f, 1.0f, 0.5f }, 0);
            gl.glDrawArrays(GL10.GL_LINES, 2, 2);

            // Z : green
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 1.0f, 0f, 0.75f }, 0);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 1.0f, 0f, 0.5f }, 0);
            gl.glDrawArrays(GL10.GL_LINES, 4, 2);
        }

        private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            FloatBuffer triangleBuffer = vbb.asFloatBuffer();
            triangleBuffer.put(vertexArray);
            triangleBuffer.position(0);
            return triangleBuffer;
        }

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


    }






}
