package com.example.opengldemo.shape_2D;

import android.content.Context;
import android.graphics.Matrix;
import android.opengl.GLES20;

import com.example.opengldemo.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  FIX_LOCATION 可以调整矩形形状
 *  VERTEX_DATA_NUM 可以调整成圆形
 *  FULL_RADIAN 可以调整画的角度
 */
public class Rect extends BaseShape {

    String mVertexShader = SIMPLE_VERTEX_SHADER;
    String mFragmentShader = SIMPLE_FRAGMENT_SHADER;
    private int uColor;
    private int aPosition;

    public static final boolean FIX_LOCATION = false;
    private static final int VERTEX_DATA_NUM = 4;
    public static final double FULL_RADIAN = Math.PI * 2;
    private float[] mVertexData;

    private FloatBuffer floatBuffer;

    public Rect(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        int program = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        if (program == 0) {
            return;
        }
        GLES20.glUseProgram(program);

        uColor = GLES20.glGetUniformLocation(program, U_COLOR);
        aPosition = GLES20.glGetAttribLocation(program, A_POSITION);

        initVertexData();

        floatBuffer = ByteBuffer
                .allocateDirect(mVertexData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
        floatBuffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, floatBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);

        GLES20.glUniform4f(uColor, 1f, 0f, 1f, 1f);
    }

    private void initVertexData() {
        mVertexData = new float[VERTEX_DATA_NUM * 2 + 4];
        float radian = (float) (FULL_RADIAN / VERTEX_DATA_NUM);
        float radius = 0.5f;
        mVertexData[0] = 0;
        mVertexData[1] = 0;

        for (int i = 0; i < VERTEX_DATA_NUM; i++) {
            mVertexData[i * 2 + 2] = (float) (radius * Math.cos(radian * i));
            mVertexData[i * 2 + 3] = (float) (radius * Math.sin(radian * i));
        }
        mVertexData[VERTEX_DATA_NUM * 2 + 2] = (float) (radius * Math.cos(radian * 0));
        mVertexData[VERTEX_DATA_NUM * 2 + 3] = (float) (radius * Math.sin(radian * 0));

        // 修正矩形位置
        if (FIX_LOCATION) {
            Matrix matrix = new Matrix();
            matrix.setRotate((float) ((360.0 * FULL_RADIAN / (2 * Math.PI)) / VERTEX_DATA_NUM / 2));
            float[] point = new float[2];
            for (int i = 0; i < VERTEX_DATA_NUM * 2 + 4; i += 2) {
                point[0] = mVertexData[i];
                point[1] = mVertexData[i + 1];
                matrix.mapPoints(point);
                mVertexData[i] = point[0];
                mVertexData[i + 1] = point[1];
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_DATA_NUM + 2);
    }
}