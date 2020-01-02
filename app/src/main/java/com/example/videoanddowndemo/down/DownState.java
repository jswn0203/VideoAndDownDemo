package com.example.videoanddowndemo.down;

public class DownState {
    public static int REQUEST = 1;//请求下载中
    public static int START = 2;//开始下载
    public static int WILL_START = 3;//将要下载，即等待轮到它下载
    public static int STOP = 4;//下载已停止
    public static int UNKNOW = 5;//未知情况的状态
    public static int DOWN = 6;//正在下载中
    public static int OVER = 7;//下载完成
    public static int FAIL = 8;//下载失败
}
