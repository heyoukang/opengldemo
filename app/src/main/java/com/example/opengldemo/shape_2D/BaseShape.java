package com.example.opengldemo.shape_2D;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseShape {

    protected Context mContext;

    protected int mProgram;
    protected float[] mProjMatrix = new float[16];
    protected float[] mMMatrix = new float[16];
    protected float[] mVMatrix = new float[16];
    protected float[] mMVPMatrix = new float[16];

    public static final String SIMPLE_VERTEX_SHADER =
            "attribute vec4 a_Position;\n" +
            "void main() {\n" +
            "   gl_Position = a_Position;\n" +
            "}\n";

    public static final String SIMPLE_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "uniform vec4 u_Color;\n" +
            "void main() {\n" +
            "   gl_FragColor = u_Color;\n" +
            "}\n";

    public static final String MVP_VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "void main() {\n" +
            "  gl_Position = u_MVPMatrix * a_Position;\n" +
            "}\n";

    public static final String U_MVP_MATRIX = "u_MVPMatrix";
    public static final String U_COLOR = "u_Color";
    public static final String A_POSITION = "a_Position";

    public static final int FLOAT_SIZE_BYTES = 4;

    public static final int BYTES_SIZE_BYTE = 1;

    public static final int SHORT_SIZE_BYTE = 2;

    public BaseShape(Context context) {
        mContext = context;
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    }

    public void onSurfaceChanged(GL10 gl10, int width, int height) {
    }

    public void onDrawFrame(GL10 gl10) {
    }
}
