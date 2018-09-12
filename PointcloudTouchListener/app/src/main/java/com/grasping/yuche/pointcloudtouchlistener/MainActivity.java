package com.grasping.yuche.pointcloudtouchlistener;

import android.os.Bundle;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.PointCloud2;

public class MainActivity extends RosActivity{
    private PointcloudView pclsurface;
    public MainActivity(){ super("subscriber","subscriber");}

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        pclsurface = new PointcloudView(this);
        pclsurface.setTopicName("/camera/depth_registered/points");
        pclsurface.setMessageType(PointCloud2._TYPE);
        setContentView(pclsurface);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(pclsurface, nodeConfiguration);
    }
}
