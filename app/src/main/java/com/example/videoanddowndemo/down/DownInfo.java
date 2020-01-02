package com.example.videoanddowndemo.down;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "down_info")
public class DownInfo {

    @Id(autoincrement = true)
    private Long id;
    private String mUserId;//文件的id
    private String mUrlTag;//文件的urlTag
    private String fileDir;//文件夹
    private String fileName;//文件名称
    private int mSate = 0;//下载状态
    private long mTotalSize = 0;//文件大小
    private long mCurrentSize = 0;//当前下载的文件大小

    @Generated(hash = 415686580)
    public DownInfo(Long id, String mUserId, String mUrlTag, String fileDir,
            String fileName, int mSate, long mTotalSize, long mCurrentSize) {
        this.id = id;
        this.mUserId = mUserId;
        this.mUrlTag = mUrlTag;
        this.fileDir = fileDir;
        this.fileName = fileName;
        this.mSate = mSate;
        this.mTotalSize = mTotalSize;
        this.mCurrentSize = mCurrentSize;
    }
    @Generated(hash = 928324469)
    public DownInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMUserId() {
        return this.mUserId;
    }
    public void setMUserId(String mUserId) {
        this.mUserId = mUserId;
    }
    public String getMUrlTag() {
        return this.mUrlTag;
    }
    public void setMUrlTag(String mUrlTag) {
        this.mUrlTag = mUrlTag;
    }
    public String getFileDir() {
        return this.fileDir;
    }
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public int getMSate() {
        return this.mSate;
    }
    public void setMSate(int mSate) {
        this.mSate = mSate;
    }
    public long getMTotalSize() {
        return this.mTotalSize;
    }
    public void setMTotalSize(long mTotalSize) {
        this.mTotalSize = mTotalSize;
    }
    public long getMCurrentSize() {
        return this.mCurrentSize;
    }
    public void setMCurrentSize(long mCurrentSize) {
        this.mCurrentSize = mCurrentSize;
    }
}
