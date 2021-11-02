package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.shape_2D.BaseShape;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES20Renderer implements GLSurfaceView.Renderer {

    private Context mContext;
    private BaseShape mShape;

    public GLES20Renderer(Context context, BaseShape shape) {
        mContext = context;
        mShape = shape;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mShape.onSurfaceCreated(gl10, eglConfig);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mShape.onSurfaceChanged(gl10, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mShape.onDrawFrame(gl10);
    }
}
