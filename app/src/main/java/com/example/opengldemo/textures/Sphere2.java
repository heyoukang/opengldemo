package com.example.opengldemo.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.opengldemo.R;
import com.example.opengldemo.ShaderUtils;
import com.example.opengldemo.shape_2D.BaseShape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenerateMipmap;
import static com.example.opengldemo.ShaderUtils.checkGlError;

/**
 * https://www.cnblogs.com/mqxnongmin/p/10634765.html
 */
public class Sphere2 extends BaseShape {

    public static final boolean USE_GL_DRAW_ARRAYS = true;

    public static final String A_TEXTURE_COORDINATE = "a_TextureCoordinates";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";
    public static final String U_MMATRIX = "u_MMatrix";
    private static final String TAG = "Sphere2";
    private String mVertexShader =
             "uniform mat4 u_MVPMatrix;\n" +
             "attribute vec3 a_Position;\n" +
             "attribute vec2 a_TextureCoordinates;\n" +
             "varying vec2 v_TextureCoordinates;\n" +
              // 环境光
             "varying vec4 v_Ambient;\n" +
              // 2.
             "uniform mat4 u_MMatrix;\n" + // 模型矩阵
             "varying vec4 v_Diffuse;\n" +
             "uniform vec3 u_LightLocation;\n" + //光源位置
             "attribute vec3 a_Normal;\n" + // 法向量
             "void pointLight (in vec3 normal, inout vec4 diffuse, in vec3 lightLocation, in vec4 lightDiffuse){\n" +
             "   vec3 normalTarget = a_Position + normal;\n" +
             "   vec3 newNormal = (u_MMatrix * vec4(normalTarget, 1)).xyz - (u_MMatrix*vec4(a_Position,1)).xyz;\n" +
                     "newNormal=normalize(newNormal);\n" +
                     "vec3 vp= normalize(lightLocation-(u_MMatrix*vec4(a_Position,1)).xyz);\n" +
                     "vp=normalize(vp);\n" +
                     "float nDotViewPosition = max(0.0,dot(newNormal,vp));\n" +
                     "diffuse = lightDiffuse * nDotViewPosition;\n" +
             "}\n" +
             "void main() {\n" +
             "   gl_Position = u_MVPMatrix * vec4(a_Position,1);\n" +
             "   v_TextureCoordinates = a_TextureCoordinates;\n" +
             "   v_Ambient = vec4(0.35,0.35,0.35,1.0);\n" +
//             "   v_Ambient = vec4(1.0,1.0,1.0,1.0);\n" +
             "   vec4 diffuseTemp=vec4(0.0,0.0,0.0,0.0);\n" +
             "   pointLight(normalize(a_Normal), diffuseTemp, u_LightLocation, vec4(0.8,0.8,0.8,1.0));\n" +
             "   v_Diffuse = diffuseTemp;\n" +
             "}\n";
    private String mFragmentShader =
            "precision mediump float;\n" +
            "uniform sampler2D u_TextureUnit;\n" +
            "varying vec2 v_TextureCoordinates;\n" +
            "varying vec4 v_Ambient;\n" +
            "varying vec4 v_Diffuse;\n" +
            "void main() {\n" +
            "   vec4 objectColor = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
            "   gl_FragColor = (v_Diffuse + v_Ambient) * objectColor;\n" +
            "}\n";

    private int maPositionHandle;
    private int muColorHandle;
    private int muMVPMatrixHandle;
    private int maTextureCoordinateHandle;
    private int muTextureUnitHandle;
    private int muMMatrixHandle;
    private int muLightLocationHandle;
    private int maNormalHandle;

    private float[] mVertexes;
    private short[] mIndexes;

    private FloatBuffer mVertexFloatBuffer;
    private ShortBuffer mIndexByteBuffer;

    private FloatBuffer mTextureFloatBuffer;
    private float[] mTextureVertexes = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };
    private int mTextureID;

    public Sphere2(Context context) {
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
        maTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORDINATE);
        checkGlError("glGetAttribLocation maTextureCoordinateHandle");
        muTextureUnitHandle = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        checkGlError("glGetUniformLocation muTextureUnitHandle");
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, U_MMATRIX);
        checkGlError("glGetUniformLocation muMMatrixHandle");
        muLightLocationHandle = GLES20.glGetUniformLocation(mProgram, "u_LightLocation");
        checkGlError("glGetUniformLocation muLightLocationHandle");
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        checkGlError("glGetAttribLocation maNormalHandle");


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

        mTextureFloatBuffer = ByteBuffer.allocateDirect(mTextureVertexes.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureVertexes);
        mTextureFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(maTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureFloatBuffer);
        checkGlError("glVertexAttribPointer maTextureCoordinateHandle");
        GLES20.glEnableVertexAttribArray(maTextureCoordinateHandle);
        checkGlError("glEnableVertexAttribArray maTextureCoordinateHandle");
        mTextureFloatBuffer.position(0);
        mTextureID = loadTexture(R.drawable.texture);
        Log.e(TAG, "loadTexture=" + mTextureID);

        GLES20.glUniform1i(muTextureUnitHandle, 1);
        checkGlError("glUniform1i muTextureUnitHandle");

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0);
    }

    private int loadTexture(int resourceId) {
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.e(TAG, "decode bitmap error!!!" );
            return 0;
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        // 为当前绑定的纹理自动生成所有需要的多级渐远纹理
        // 生成 MIP 贴图
        glGenerateMipmap(GL_TEXTURE_2D);

        // 解除与纹理的绑定，避免用其他的纹理方法意外地改变这个纹理
        glBindTexture(GL_TEXTURE_2D, 0);

        // TODO 真正绘制的时候，是按照顶点顺序绘制的，还是同时绘制 ???
        GLES20.glEnable(GL_DEPTH_TEST);
        return textureID;
    }

    private void initGlDrawArraysVertex() {
        int rings = 180;
        int sectors = 180;
        ArrayList<Float> data = new ArrayList<>();
        ArrayList<Float> tmp = new ArrayList<>();
        float sina, cosa, sinb, cosb, sina2, cosa2;
        float R = 0.3f;
        int step1 = 180 / rings;

        Log.e(TAG, "initGlDrawArraysVertex: start ----------------------------");
        for (int i = -90; i <= 90; i += step1) {
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

                Log.e(TAG, "1111 x=" + (R * cosa2 * sinb) + " y=" + (R * sina2) + " z=" + (R * cosa2 * cosb));

                data.add(R * cosa * sinb);
                data.add(R * sina);
                data.add(R * cosa * cosb);
                Log.e(TAG, "2222 x=" + (R * cosa * sinb) + " y=" + (R * sina) + " z=" + (R * cosa * cosb));

                float textTureX = j / 360.0f;
                float textTureY = (float) ((i + 90.0) / 180.0f);

                tmp.add(textTureX);
                tmp.add(textTureY + step1 / 180.0f);
                Log.e(TAG, "1111 texture x=" + textTureX + " y + step3=" + (textTureY + step2 / 360.0f) + " i=" + i);

                tmp.add(textTureX);
                tmp.add(textTureY);
                Log.e(TAG, "1111 texture x=" + textTureX + " y=" + textTureY);
            }
        }
        Log.e(TAG, "initGlDrawArraysVertex: end ----------------------------");
        mVertexes = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            mVertexes[i] = data.get(i);
        }

        mTextureVertexes = new float[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            mTextureVertexes[i] = tmp.get(i);
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
        if (mProgram == 0) {
            return;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        glActiveTexture(GLES20.GL_TEXTURE1);
        glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        long time = SystemClock.uptimeMillis() % 10000L;
        float angle = (360.0f / 10000.0f) * ((int) time);
//        double test = (angle / 180) * Math.PI;
        Matrix.setRotateM(mMMatrix, 0, angle, 0f, 1f, 0f);
//        Matrix.setLookAtM(mVMatrix, 0, (float) (5 * Math.sin(test)), 0, (float) (5 * Math.cos(test)), 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);


        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, mMMatrix, 0);
        GLES20.glUniform3f(muLightLocationHandle, -4, 0, 1.5f);

        //将顶点法向量数据传入渲染管线
        GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(maNormalHandle);// 启用顶点法向量数据数组

        if (USE_GL_DRAW_ARRAYS) {
            glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexes.length / 3);
        } else {
            glDrawElements(GL_TRIANGLES, mIndexes.length, GL_UNSIGNED_SHORT, mIndexByteBuffer);
        }
        checkGlError("glDrawElements");
    }
}
