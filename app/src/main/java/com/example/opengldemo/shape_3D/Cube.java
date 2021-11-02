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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.opengldemo.ShaderUtils.checkGlError;

/**
 * 1. 3D图形必须要添加MVP矩阵，否则只能看到一个面，即使你旋转这个图形，由于默认的投影[P]矩阵是正交投影，所以旋转过程中不会远近的概念，所以看着还是一个2D的效果；
 *      经过试验，默认投影矩阵的视景体是一个边长为1以原点居中的正方体（即代表camera在z为1或者-1，near为0，far为2）
 * 2. 如果图形看不见，解决方案 (以下两个错误都有犯过)
 *      * 充分利用 GLES20.glGetError() 方法来检查，特别是刚开始学习的过程中，自己写很容易犯低级错误
 *      * 检查MVP矩阵，确保物体是在视景体之内
 * 3. 可以通过调整投影矩阵来修复屏幕拉伸问题
 * 4. glDrawElements和glDrawArrays的区别 https://mp.weixin.qq.com/s/WcWdYE5j8Ycw2dtJYS-Cxg
 */
public class Cube extends BaseShape {

    String mVertexShader = MVP_VERTEX_SHADER;
    String mFragmentShader = SIMPLE_FRAGMENT_SHADER;
    FloatBuffer mVertexFloatBuffer;
    ByteBuffer mIndexByteBuffer;
    private int muColorHandle;
    private int maPositionHandle;
    private int muMVPMatrixHandle;

    private final float[] mVertexes = {
            // TODO 用屏幕坐标坐标，不用归一化后的坐标
            //立方体前面的四个点
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            // 立方体后面的四个点
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
    };

    private final byte[] mIndexes = {
            // 前面索引
            0, 1, 2,    3, 2, 1,
            // 后面索引
            4, 5, 6,    7, 6, 5,
            // 上面索引
            0, 1, 4,    5, 4, 1,
            // 下面索引
            2, 3, 6,    7, 6, 3,
            // 左面索引
            0, 4, 2,    6, 2, 4,
            // 右侧索引
            1, 5, 3,    7, 3, 5
    };

    public Cube(Context context) {
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

        muColorHandle = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        checkGlError("glGetUniformLocation muColorHandle");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        checkGlError("glGetAttribLocation maPositionHandle");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, U_MVP_MATRIX);
        checkGlError("glGetUniformLocation muMVPMatrixHandle");


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

        GLES20.glUniform4f(muColorHandle, 1f, 0f, 1f, 1f);
        checkGlError("glUniform4f muColorHandle");

        mIndexByteBuffer = ByteBuffer
                .allocateDirect(mIndexes.length * BYTES_SIZE_BYTE)
                .put(mIndexes);
        mIndexByteBuffer.position(0);

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1f, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        super.onSurfaceChanged(gl10, width, height);
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long time = SystemClock.uptimeMillis() % 10000L;
        float angle = (360.0f / 10000.0f) * ((int) time);
        Matrix.setRotateM(mMMatrix, 0, angle, 0, 1f, 0f);
//
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexes.length, GLES20.GL_UNSIGNED_BYTE, mIndexByteBuffer);
        checkGlError("glDrawElements");
    }
}
