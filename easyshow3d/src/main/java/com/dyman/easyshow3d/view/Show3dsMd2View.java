package com.dyman.easyshow3d.view;

import android.content.Context;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dyman on 16/8/13.
 */
public class Show3dsMd2View extends GLSurfaceView{
    private static final String TAG = "Show3dsMd2View";
    private MyRender mRender;

    private static int TOUCH_NONE = 0;
    private static int TOUCH_DRAG = 1;
    private static int TOUCH_ZOOM = 2;
    private int touchMode = TOUCH_NONE;

    private float xpos = -1;
    private float ypos = -1;
    private float pinchStartDistance = 0;
    private float currScale = 1;
    private float saveScale = 1;

    public Show3dsMd2View(Context context, String filePath) {
        super(context);
        setEGLConfigChooser(new EGLConfigChooser(){

            @Override
            public EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eglDisplay) {
                //Ensure that we get a 16bit framebuffer. Otherwise, we'll fall back to Pixelflinger on some device (read:
                // Samsung I7500)
                int[] attributes = new int[] {EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
                EGLConfig[] configs = new EGLConfig[1];
                int[] result = new int[1];
                egl10.eglChooseConfig(eglDisplay, attributes, configs, 1, result);
                return configs[0];
            }
        });
        mRender = new MyRender(context, filePath);
        setRenderer(mRender);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (touchMode == TOUCH_NONE && e.getPointerCount() == 1){
                    touchMode = TOUCH_DRAG;
                    xpos = e.getX();
                    ypos = e.getY();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (e.getPointerCount() >= 2) {
                    pinchStartDistance = getPinchDistance(e);
                    if (pinchStartDistance >= 50f){
                        touchMode = TOUCH_ZOOM;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0) {
                    float temp = getPinchDistance(e) / pinchStartDistance;
                    currScale = temp * saveScale;
                    mRender.setZoomSize(currScale);
                }else if(touchMode == TOUCH_DRAG) {
                    float xd = e.getX() - xpos;
                    float yd = e.getY() - ypos;

                    mRender.setRotateY(xd / -100f);
                    mRender.setRotateZ(yd / -100f);

                    xpos = e.getX();
                    ypos = e.getY();

                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (touchMode == TOUCH_ZOOM) {
                    touchMode = TOUCH_NONE;
                    saveScale = currScale;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (touchMode == TOUCH_DRAG) {
                    touchMode = TOUCH_NONE;
                }
                break;
        }

        return true;
    }

    /**
     *  计算两指间的距离
     * @param event
     * @return
     */
    private float getPinchDistance(MotionEvent event) {
        float x=0;
        float y=0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }



    class MyRender implements Renderer{

        private Context mContext;
        private String filePath;

        private Object3D model = null;
        private Object3D cube = null;
        private World world;
        private Light sun = null;
        private FrameBuffer fb = null;

        private float rotateY = 0;//水平旋转角度
        private float rotateZ = 0;//垂直旋转角度
        private float zoomSize = 1;//整体缩放度

        public MyRender(Context c, String filePath){
            mContext = c;
            this.filePath = filePath;
        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

            world = new World();
            world.setAmbientLight(500, 500, 500);

            cube = Primitives.getCube(10);
            cube.calcTextureWrapSpherical();
            cube.strip();
            cube.build();

            model = loadModel(filePath, 1);
            model.strip();
            model.build();

            world.addObject(model);
            //光源设置
            sun = new Light(world);
            sun.setIntensity(250,250,250);
            //镜头设置
            Camera cam = world.getCamera();
            cam.moveCamera(Camera.CAMERA_MOVEOUT, 10);
            cam.lookAt(cube.getTransformedCenter());

            SimpleVector sv = new SimpleVector();
            sv.set(cube.getTransformedCenter());
            sv.y -= 100;
            sv.z -= 100;
            sun.setPosition(sv);
            MemoryHelper.compact();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int w, int h) {
            if (fb != null){
                fb.dispose();
            }
            fb = new FrameBuffer(gl10, w, h);
            GLES20.glViewport(0, 0, w, h);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            fb.clear(RGBColor.BLACK);
            world.renderScene(fb);
            world.draw(fb);
            fb.display();

            if (rotateY != 0) {
                model.rotateY(rotateY);
                rotateY = 0;
            }

            if (rotateZ != 0){
                model.rotateZ(rotateZ);
                rotateZ = 0;
            }

            if (zoomSize != 1) {
                model.setScale(zoomSize);
                zoomSize = 1;
            }
        }


        /**
         *  加载模型，3DS格式的
         * @param filename
         * @param scale
         * @return
         */
        public Object3D loadModel(String filename, float scale){
            InputStream is = null;
            try {
                Uri uri = Uri.fromFile(new File(filePath));
                is = mContext.getContentResolver().openInputStream(uri);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Object3D[] model = Loader.load3DS(is, scale);
            Object3D o3d = new Object3D(0);
            Object3D temp = null;
            for (int i = 0; i < model.length; i++) {
                temp = model[i];
                temp.setCenter(SimpleVector.ORIGIN);
                temp.rotateX((float)( -.5*Math.PI));
                temp.rotateMesh();
                temp.setRotationMatrix(new Matrix());
                o3d = Object3D.mergeObjects(o3d, temp);
                o3d.build();
            }
            return o3d;
        }

        public void setRotateY(float count) {this.rotateY = count;}

        public void setRotateZ(float count) {this.rotateZ = count;}

        public void setZoomSize(float count) {this.zoomSize = count;}
    }
}
