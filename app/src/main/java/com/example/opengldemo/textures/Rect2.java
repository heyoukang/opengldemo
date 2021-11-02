package com.example.opengldemo.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.opengldemo.R;
import com.example.opengldemo.ShaderUtils;
import com.example.opengldemo.shape_2D.BaseShape;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;
import static com.example.opengldemo.ShaderUtils.checkGlError;

public class Rect2 extends BaseShape {

    public static final String A_TEXTURE_COORDINATE = "a_TextureCoordinates";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String TAG = "Rect2";
    private String mVertexShader =
            "attribute vec4 a_Position;\n" +
            "attribute vec2 a_TextureCoordinates;\n" +
            "varying vec2 v_TextureCoordinates;\n" +
            "void main() {\n" +
            "   v_TextureCoordinates = a_TextureCoordinates;\n" +
            "   gl_Position = a_Position;\n" +
            "}\n";
    private String mFragmentShader =
            "precision mediump float;\n" +
            "uniform sampler2D u_TextureUnit;\n" +
            "varying vec2 v_TextureCoordinates;\n" +
            "void main() {\n" +
            "   gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
            "}\n";

    private int maPositionHandle;
    private int maTextureCoordinateHandle;
    private int muTextureUnitHandle;

    private FloatBuffer mVertexFloatBuffer;
    private float[] mVertexes = {
            -0.5f, 0.5f,
            0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
    };

    private FloatBuffer mTextureFloatBuffer;
    private float[] mTextureVertexes = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };
    private int mTextureID;

    public Rect2(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        mProgram = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            Log.e(TAG, "createProgram failed!");
            return;
        }
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        checkGlError("glGetAttribLocation maPositionHandle");
        maTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORDINATE);
        checkGlError("glGetAttribLocation maTextureCoordinateHandle");
        muTextureUnitHandle = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        checkGlError("glGetUniformLocation muTextureUnitHandle");

        mVertexFloatBuffer = ByteBuffer.allocateDirect(mVertexes.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexes);
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexFloatBuffer);
        checkGlError("glVertexAttribPointer maPositionHandle");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        mVertexFloatBuffer.position(0);

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

//        GLES20.glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, IntBuffer.allocate(1));
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        checkGlError("glDrawArrays");
    }

    private int loadTexture(int resourceId) {
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

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
        return textureID;
    }
}
