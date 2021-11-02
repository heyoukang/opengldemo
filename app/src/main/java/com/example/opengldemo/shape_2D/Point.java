package com.example.opengldemo.shape_2D;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.opengldemo.ShaderUtils.createProgram;

public class Point extends BaseShape {

    private static final String TAG = "Point";
    String mVertexShader =
            "attribute vec4 a_Position;\n" +
            "void main() {\n" +
            "   gl_Position = a_Position;\n" +
            "   gl_PointSize = 80.0;\n" +
            "}\n";
    String mFragmentShader = SIMPLE_FRAGMENT_SHADER;

    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private int uColor;
    private int aPosition;

    private float[] mVertexData = {
            -0.5f, 0.5f,
            0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
    };

    private FloatBuffer floatBuffer;

    public Point(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 1. create program
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        GLES20.glUseProgram(mProgram);

        // 2. 绑定值
        uColor = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        aPosition = GLES20.glGetAttribLocation(mProgram, A_POSITION);

        // 3. 赋值
        floatBuffer = ByteBuffer
                .allocateDirect(mVertexData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
        floatBuffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, floatBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);
        floatBuffer.position(0);

        GLES20.glUniform4f(uColor, 1f, 0f, 1f, 1f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexData.length / 2);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
