package com.grasping.yuche.pointcloudtouchlistener;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Preconditions;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import sensor_msgs.PointCloud2;
import sensor_msgs.PointField;


public class PointcloudView extends GLSurfaceView implements NodeMain{
    private final MyGLRenderer mRenderer;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer setvertexBuffer;
    private FloatBuffer setcolorBuffer;

    private String topicName;
    private String messageType;

    private MessageCallable<String,PointCloud2> callable;
    private int count;

    private Subscriber<PointCloud2> subscriber;

    public void setTopicName(String topicName){
        this.topicName=topicName;
    }

    public void setMessageType(String messageType){
        this.messageType=messageType;
    }

    public void setMessageToStringCallable(MessageCallable<String, PointCloud2> callable) {
        this.callable=callable;
    }


    public PointcloudView(Context context){
        super(context);

        setEGLContextClientVersion(2);
        mRenderer = new MyGLRenderer();

        setRenderer(mRenderer);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_UP:
                        float x,y;
                        x=event.getX();
                        y=event.getY();
                        int height = mRenderer.getViewHeight();
                        int width = mRenderer.getViewWidth();
                        if(x<(float)width/10.0f && y<(float)height/5.0f){
                            mRenderer.seteye(0.0f,0.0f,0.0f);
                        }
                        else if(x<(float)width/10.0f && y>(float)height/5.0f*4.0f){
                            mRenderer.setMode((mRenderer.getMode()+2)%3);
                        }
                        else if(x>(float)width/10.0f*9.0f&& y>(float)height/5.0f*4.0f){
                            mRenderer.setMode((mRenderer.getMode()+1)%3);
                        }
                        else {
                            if(mRenderer.getMode()==2)
                                mRenderer.getPoint(x, y);
                            if(mRenderer.getMode()==1){
                                if(x<(float)width/4)
                                    mRenderer.movePosition(3);
                                else if(x>(float)width/4*3)
                                    mRenderer.movePosition(4);
                                else if(y<(float)height/2)
                                    mRenderer.movePosition(1);
                                else if(y>(float)height/2)
                                    mRenderer.movePosition(2);
                            }
                        }
                        Log.i("ontouchMessage", "height: "+String.valueOf(height)+" width: "+String.valueOf(width));
                        Log.i("ontouchMessage", "x: "+String.valueOf(x)+" y: "+String.valueOf(y));
                        Log.i("ontouchMessage", "mode: "+String.valueOf(mRenderer.getMode()));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }

    public void checkPointCloudData(PointCloud2 pointcloud){
        ChannelBuffer buffer = pointcloud.getData();
        int tmpcount = 0;
        while(buffer.readable()){
            int tmp = (int)buffer.readByte() & 0xFF;
            Log.d(String.valueOf(tmpcount%32+1)+" byte data",String.valueOf(tmp)+"  "+String.valueOf(tmpcount%32+1));
            tmpcount++;
        }
    }
    synchronized public void updateBuffer(PointCloud2 pointcloud){
        /* for intel realsense D345
        Preconditions.checkArgument(pointcloud.getHeight() == 480);
        Preconditions.checkArgument(pointcloud.getWidth() == 640);
        Preconditions.checkArgument(pointcloud.getFields().get(0).getDatatype() == PointField.FLOAT32);//x
        Preconditions.checkArgument(pointcloud.getFields().get(1).getDatatype() == PointField.FLOAT32);//y
        Preconditions.checkArgument(pointcloud.getFields().get(2).getDatatype() == PointField.FLOAT32);//z
        Preconditions.checkArgument(pointcloud.getFields().get(3).getDatatype() == PointField.FLOAT32);//rgb
*/
        int PointNums = (pointcloud.getRowStep()*pointcloud.getHeight() / pointcloud.getPointStep());
        int VertexSize = PointNums * 4;//xyzw
        int ColorSize = PointNums * 4;//rgba

        if(vertexBuffer == null || vertexBuffer.capacity() < VertexSize){
            ByteBuffer bb = ByteBuffer.allocateDirect(VertexSize * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
        }
//        Log.d("vertexbuffer size",""+String.valueOf(VertexSize));
        vertexBuffer.clear();

        if(colorBuffer == null || colorBuffer.capacity() < ColorSize){
            ByteBuffer bb = ByteBuffer.allocateDirect(ColorSize * 4);
            bb.order(ByteOrder.nativeOrder());
            colorBuffer = bb.asFloatBuffer();
        }

        colorBuffer.clear();

//        int tmpcount=0;
        ChannelBuffer buffer = pointcloud.getData();
        while(buffer.readable()){
//            tmpcount++;
            int pointBegin = buffer.readerIndex();
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();

            vertexBuffer.put(x);//x
            vertexBuffer.put(y);//y
            vertexBuffer.put(z);//z
            vertexBuffer.put(1.0f);//w
//            Log.d("poistion of point "+String.valueOf(tmpcount), "x: "+String.valueOf(x)+"  y: "+String.valueOf(y)+"    z: "+String.valueOf(z));
            buffer.readFloat();//discard 4 byte offset

            int b8s = (int)buffer.readByte() & 0xFF;
            int g8s = (int)buffer.readByte() & 0xFF;
            int r8s = (int)buffer.readByte() & 0xFF;
//            Log.d("color of point "+String.valueOf(tmpcount),"r: "+String.valueOf(r8s)+"  g: "+String.valueOf(g8s)+"    b: "+String.valueOf(b8s));
            float r = (float)r8s /255.0f;
            float g = (float)g8s /255.0f;
            float b = (float)b8s /255.0f;

            colorBuffer.put(r);//r
            colorBuffer.put(g);//g
            colorBuffer.put(b);//b
            colorBuffer.put(1.0f);//a

            int totalRead = buffer.readerIndex() - pointBegin;
//            Log.d("Read status: ","Total Read: "+String.valueOf(totalRead)+"    Point size"+String.valueOf(pointcloud.getPointStep()));
            buffer.readBytes(pointcloud.getPointStep() - totalRead);
        }

        vertexBuffer.position(0);
        colorBuffer.position(0);

        FloatBuffer tmpvertexBuffer, tmpcolorBuffer;
        tmpvertexBuffer = setvertexBuffer;
        tmpcolorBuffer = setcolorBuffer;
        setvertexBuffer = vertexBuffer;
        setcolorBuffer = colorBuffer;
        vertexBuffer = tmpvertexBuffer;
        colorBuffer = tmpcolorBuffer;
        this.mRenderer.setPc(setvertexBuffer, setcolorBuffer);
    }
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("PointCloudView_yuche");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        count = 0;
        subscriber = connectedNode.newSubscriber(topicName,messageType);
        subscriber.addMessageListener(new MessageListener<PointCloud2>() {
            @Override
            public void onNewMessage(PointCloud2 message) {
                count++;
                Log.i("MessageCount", "onNewMessage: "+String.valueOf(count));
                updateBuffer(message);
            }
        });
    }
    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    public void setRenderAngle(float Amizuth, float Pitch, float Roll){
        mRenderer.setVRAngle(Amizuth,Pitch,Roll);
    }
}