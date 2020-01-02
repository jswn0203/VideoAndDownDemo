package com.example.videoanddowndemo.down.IListener;

import com.example.videoanddowndemo.down.DownInfo;

public interface IDownListener {
    //下载成功
    void onDownSuc(DownInfo info);

    //下载进度
    void onDownProgress(DownInfo info);

    //开始下载
    void onDownSatrt(DownInfo info);

    //下载暂停
    void onDownPause(DownInfo info);

    //下载失败
    void onDownFaile(DownInfo info);

    //请求下载头成功
    void onDownHeadInfoSuc(DownInfo info);

    //请求下载头失败
    void onDownHeadInfoFaile(DownInfo info);

    //请求连接中
    void onDownInfoRequestConnect(DownInfo info);


}
