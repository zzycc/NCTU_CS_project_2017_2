package com.grasping.yuche.pointcloudtouchlistener;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class Vertices {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 color;"+
                    "varying vec4 vColor;"+
                    "void main() {" +
                    "gl_Position = uMVPMatrix*vPosition;" +
                    //  "gl_Position = vPosition;" +
                    "gl_PointSize = 1.0;"+
                    "vColor = color;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    //        "uniform vec4 vColor;" +
                    "varying vec4 vColor;"+
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    private final int COORDS_PER_VERTEX = 4;//xyzw
    private final int COLORS_PER_VERTEX = 4;//rgba

    private final int mProgram;

    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int colorStride = COLORS_PER_VERTEX *4;
    private int vertexCount = 0;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    public Vertices(){
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);

    }

    public void setVertexBuffer(FloatBuffer vb){
        this.vertexBuffer = vb;
        this.vertexCount = vb.capacity()/COORDS_PER_VERTEX;
    }
    public void setColorBuffer(FloatBuffer cb){
        this.colorBuffer = cb;
    }
    public void draw(float[] mvpMatrix){
        if(this.vertexCount!=0){
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            // get handle to fragment shader's vColor member
            //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the triangle
            //GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            mColorHandle = GLES20.glGetAttribLocation(mProgram,"color");
            GLES20.glEnableVertexAttribArray(mColorHandle);
            GLES20.glVertexAttribPointer(mColorHandle, 4,GLES20.GL_FLOAT,false,colorStride,colorBuffer);
            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
}