package com.example.opengldemo.shape_3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengldemo.ShaderUtils;
import com.example.opengldemo.shape_2D.BaseShape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static com.example.opengldemo.ShaderUtils.checkGlError;

/**
 * https://www.cnblogs.com/mqxnongmin/p/10634765.html
 */
public class Sphere extends BaseShape {

    public static final boolean USE_GL_DRAW_ARRAYS = true;
    private String mVertexShader = MVP_VERTEX_SHADER;
    private String mFragmentShader = SIMPLE_FRAGMENT_SHADER;

    private int maPositionHandle;
    private int muColorHandle;
    private int muMVPMatrixHandle;

    private float[] mVertexes;
    private short[] mIndexes;

    private FloatBuffer mVertexFloatBuffer;
    private ShortBuffer mIndexByteBuffer;

    public Sphere(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        mProgram = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        checkGlError("glGetAttribLocation maPositionHandle");
        muColorHandle = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        checkGlError("glGetUniformLocation muColorHandle");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, U_MVP_MATRIX);
        checkGlError("glGetUniformLocation muMVPMatrixHandle");

        if (USE_GL_DRAW_ARRAYS) {
            initGlDrawArraysVertex();
        } else {
            initVertexAndIndex();
            mIndexByteBuffer = ByteBuffer
                    .allocateDirect(mIndexes.length * SHORT_SIZE_BYTE)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
                    .put(mIndexes);
            mIndexByteBuffer.position(0);
        }

        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(mVertexes.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexes);
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexFloatBuffer);
        checkGlError("glVertexAttribPointer maPositionHandle");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        mVertexFloatBuffer.position(0);

        GLES20.glUniform4f(muColorHandle, 1.0f, 0f, 1f, 1f);
        checkGlError("glUniform4f muColorHandle");

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0);
    }

    private void initGlDrawArraysVertex() {
        int rings = 90;
        int sectors = 90;
        ArrayList<Float> data = new ArrayList<>();
        float sina, cosa, sinb, cosb, sina2, cosa2;
        float R = 1;
        int step1 = 180 / rings;
        for (int i = -90; i < 90f; i += step1) {
            cosa = (float) Math.cos(i * Math.PI / 180);
            sina = (float) Math.sin(i * Math.PI / 180);

            cosa2 = (float) Math.cos((i + step1) * Math.PI / 180);
            sina2 = (float) Math.sin((i + step1) * Math.PI / 180);
            int step2 = 360 / sectors;
            for (int j = 0; j <= 360; j += step2) {
                cosb = (float) Math.cos(j * Math.PI / 180);
                sinb = (float) Math.sin(j * Math.PI / 180);
                data.add(R * cosa2 * sinb);
                data.add(R * sina2);
                data.add(R * cosa2 * cosb);

                data.add(R * cosa * sinb);
                data.add(R * sina);
                data.add(R * cosa * cosb);
            }
        }
        mVertexes = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            mVertexes[i] = data.get(i);
        }
    }

    private void initVertexAndIndex() {
        int rings = 90;
        int sectors = 90;
        mVertexes = new float[rings * sectors * 3];
        float sina, cosa, sinb, cosb;
        float R = 1;
        int count = 0;
        for (int i = -90; i < 90f; i += (180.0 / rings)) {
            cosa = (float) Math.cos(i * Math.PI / 180);
            sina = (float) Math.sin(i * Math.PI / 180);

            for (int j = 0; j < 360; j += (360.0 / sectors)) {
                cosb = (float) Math.cos(j * Math.PI / 180);
                sinb = (float) Math.sin(j * Math.PI / 180);
                mVertexes[count++] = R * cosa * sinb;
                mVertexes[count++] = R * sina;
                mVertexes[count++] = R * cosa * cosb;
            }
        }

        count = 0;
        mIndexes = new short[rings * sectors * 6];
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < sectors; j++) {
                mIndexes[count++] = (short) (i * sectors + j);                  //(a)
                mIndexes[count++] = (short) (i * sectors + (j + 1));            //(b)
                mIndexes[count++] = (short) ((i + 1) * sectors + j);            //(c)
                mIndexes[count++] = (short) ((i + 1) * sectors + j);            //(c)
                mIndexes[count++] = (short) (i * sectors + (j + 1));            //(b)
                mIndexes[count++] = (short) ((i + 1) * sectors + (j + 1));      //(d)
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 9);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 10000L;
        float angle = (360.0f / 10000.0f) * ((int) time);
        Matrix.setRotateM(mMMatrix, 0, angle, 0, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        if (USE_GL_DRAW_ARRAYS) {
            glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexes.length / 3);
        } else {
            glDrawElements(GL_TRIANGLES, mIndexes.length, GL_UNSIGNED_SHORT, mIndexByteBuffer);
        }
        checkGlError("glDrawElements");
    }
}
