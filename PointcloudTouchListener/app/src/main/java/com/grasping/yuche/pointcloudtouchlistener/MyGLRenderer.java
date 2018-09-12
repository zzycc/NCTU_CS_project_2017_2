package com.grasping.yuche.pointcloudtouchlistener;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] pcWorldCoord;
    private float[] pcClipCoord;
    private int ViewHeight;
    private int ViewWidth;


    private Vertices pc;

    public void  onSurfaceCreated(GL10 unused, EGLConfig config){
        //set the background frame color
        GLES20.glClearColor(0.18823529411f, 0.18823529411f, 0.18823529411f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        pc = new Vertices();
    }

    public void onDrawFrame(GL10 unused){
        //Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0f, 0f, 0f, 3f, 0.0f, -1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        pc.draw(mMVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 usused, int width,int height){
        GLES20.glViewport(0, 0, width, height);
        ViewHeight = height;
        ViewWidth = width;
        float ratio = (float) width / height/5;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -0.2f, 0.2f, 0.5f, 3f);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setPc(FloatBuffer vb,FloatBuffer cb){
        pcWorldCoord = new float[vb.capacity()];
        pcClipCoord = new float[vb.capacity()];
        vb.get(pcWorldCoord);
        vb.position(0);
        this.pc.setVertexBuffer(vb);
        this.pc.setColorBuffer(cb);
        for(int i=0 ; i<vb.capacity();i+=16)
            Matrix.multiplyMM(pcClipCoord,i,mMVPMatrix,0,pcWorldCoord,i);
    }
    public void getPoint(float touchx,float touchy){
        Log.i("Point", "x: "+String.valueOf(touchx)+" y: "+String.valueOf(touchy));
        if(pcWorldCoord!=null&&pcClipCoord!=null){
            float ndcx,ndcy;
            ndcx = 2*touchx/(float)ViewWidth-1;
            ndcy = 1 - 2*touchy/(float)ViewHeight;
            Log.i("ndc coordinate","ndc x :"+String.valueOf(ndcx)+" ndc y :"+String.valueOf(ndcy));
            int targetindex=0;
            float minierror = 100;
            float depth=1000;
            for(int i=0;i<pcClipCoord.length;i+=4) {
                //Log.i("counting","i :"+String.valueOf(i));
                float x = pcClipCoord[i]/pcClipCoord[i+3];
                float y = pcClipCoord[i + 1]/pcClipCoord[i+3];
                float z = pcClipCoord[i + 2]/pcClipCoord[i+3];
                float w = pcClipCoord[i + 3];
                if (Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1){
                    //Log.i("counting","i :"+String.valueOf(i));
                    if((ndcx-x)*(ndcx-x)+(ndcy-y)*(ndcy-y)<minierror){
                        minierror = (ndcx-x)*(ndcx-x)+(ndcy-y)*(ndcy-y);
                        targetindex = i;
                        depth = w;
                    }
                    else if((ndcx-x)*(ndcx-x)+(ndcy-y)*(ndcy-y)==minierror && w < depth){
                        targetindex = i;
                        depth = w;
                    }
                }
            }
            Log.i("target","target is at"+String.valueOf(targetindex)+" with error: "+String.valueOf(minierror));
            Log.i("ClipCoordinate point", "CCx: "+String.valueOf(pcClipCoord[targetindex])+" CCy: "+String.valueOf(pcClipCoord[targetindex+1])+" CCz: "+String.valueOf(pcClipCoord[targetindex+2])+" CCw: "+String.valueOf(pcClipCoord[targetindex+3]));
            Log.i("WorldCoordinate point", "WCx: "+String.valueOf(pcWorldCoord[targetindex])+" WCy: "+String.valueOf(pcWorldCoord[targetindex+1])+" WCz: "+String.valueOf(pcWorldCoord[targetindex+2])+" WCw: "+String.valueOf(pcWorldCoord[targetindex+3]));
        }
    }
}
