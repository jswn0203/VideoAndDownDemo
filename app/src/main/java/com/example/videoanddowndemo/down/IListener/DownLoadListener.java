package com.example.videoanddowndemo.down.IListener;

import com.example.videoanddowndemo.down.DownInfo;

public interface DownLoadListener {

    //下载成功
    void onDownSuc(DownInfo info);

    //下载进度
    void onDownProgress(DownInfo info);

    //下载开始
    void onDownStart(DownInfo info);

    //下载暂停
    void onDownPause(DownInfo info);

    //下载失败
    void onDownFaild(DownInfo info);

    //下载已经存在
    void onDownHasExit(DownInfo info);

    //添加下载到队列成功
    void onAddDownQueueSuc(DownInfo info);

    //下载连接中
    void onDownconnect(DownInfo info);

}
