package com.example.videoanddowndemo.down;

import com.example.videoanddowndemo.dao.DownInfoDao;
import com.example.videoanddowndemo.dbUtil.DatabaseManager;
import com.example.videoanddowndemo.dbUtil.DownInfoDbUtils;
import com.example.videoanddowndemo.down.IListener.DownLoadListener;
import com.example.videoanddowndemo.down.IListener.IDownListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DownVideoManager implements IDownListener {
    private static DownVideoManager sManger;
    private DownLoadListener mListener;
    private DownLoader mDownLoad;
    private DownInfo mCurrentInfo;
    private ArrayList<DownInfo> mDownQueue;//下载队列

    private String mDownDir;//下载文件夹

    public static DownVideoManager getInstance() {
        if (sManger == null) {
            sManger = new DownVideoManager();
        }
        return sManger;
    }

    private DownVideoManager() {
        mDownQueue = new ArrayList<>();
        mDownLoad = new DownLoader();
        mDownLoad.setDownListener(this);
    }

    public void setFielDir(String dir) {
        mDownDir = dir;
    }

    public void setDownListener(DownLoadListener listener) {
        mListener = listener;
    }

    public void addDownQueue(String url, String fileName, String id) {
        DownInfoDao dao = DatabaseManager.getInstance().getDaoSession()
                .getDownInfoDao();

        DownInfo checkData = dao.queryBuilder()
                .where(DownInfoDao.Properties.MUserId.eq(id)
                        , DownInfoDao.Properties.FileName.eq(fileName)
                        , DownInfoDao.Properties.MUrlTag.eq(url))
                .unique();
        if (checkData != null) {
            //检测是否存在对应id 的相同视频文件已经添加到视频下载记录表里
            File file = new File(mDownDir + "/" + id + "/" + checkData.getFileName());
            if (!file.exists()) {
                DownInfoDbUtils.getInstance().delete(checkData);
            } else {
                if (mListener != null) {
                    mListener.onDownHasExit(checkData);
                }
                return;
            }
        }
        DownInfo info = new DownInfo();
        info.setMUrlTag(url);
        info.setMSate(DownState.WILL_START);
        info.setFileName(fileName);
        info.setFileDir(mDownDir + "/" + id + "/");
        info.setMUserId(id);
        File file = new File(info.getFileDir());
        try {
            if (!file.exists())
                file.mkdirs();
            File videoFile = new File(file, info.getFileName());
            videoFile.createNewFile();
            DownInfoDbUtils.getInstance().add(info);
            mDownQueue.add(info);
            if (mListener != null) {
                mListener.onAddDownQueueSuc(info);
            }
            //加入队列时，如果队列没有在下载，则将这个添加的自动下载
            if (mDownQueue.size() == 1)
                startFirst(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    //下载文件记录存在时加入队列
//    public void addQueue(DownInfo info) {
//        boolean isRepeatInQueue = false;
//        for (int i = 0; i < mDownQueue.size(); i++) {
//            if (info.getFileName().equals(mDownQueue.get(i).getFileName())
//                    && info.getMUrlTag().equals(mDownQueue.get(i).getMUrlTag())
//                    && info.getMUserId().equals(mDownQueue.get(i).getMUserId())) {
//                isRepeatInQueue = true;
//            }
//        }
//
//        if (isRepeatInQueue == true) {
//            startQueue(info);
//        } else {
//            info.setMSate(DownState.WILL_START);
//            mDownQueue.add(info);
//            //加入队列时，如果队列没有在下载，则将这个添加的自动下载
//            if (mDownQueue.size() == 1)
//                startFirst(info);
//        }
//    }

    /**
     * 切换用户时进行切断全部下载队列操作
     */
    public void clearDown() {
        mDownQueue.clear();
        mDownLoad.cancelDown();
    }

    /**
     * 添加队列时对应的下载
     *
     * @param info
     */
    private void startFirst(DownInfo info) {
        File file = new File(info.getFileDir() + info.getFileName());
        DownInfo queryData = DownInfoDbUtils.getInstance().query(info.getMUserId(), info.getFileName(), info.getMUrlTag());
        if (queryData == null) {
            if (!file.exists()) {
                info.setMCurrentSize(0);
                DownInfoDbUtils.getInstance().add(info);
                mDownLoad.startDownInfo(info);
            } else {
                info.setMCurrentSize(file.length());
                DownInfoDbUtils.getInstance().add(info);
                mDownLoad.startDownInfo(info);
            }

        } else {
            if (!file.exists()) {
                info.setMCurrentSize(0);
                DownInfoDbUtils.getInstance().up(info);
                mDownLoad.startDownInfo(info);
            } else {
                info.setMCurrentSize(file.length());
                DownInfoDbUtils.getInstance().up(info);
                mDownLoad.startDownInfo(info);
            }
        }
    }


    /**
     * 下载，没有在下载队列时，添加到下载队列
     *
     * @param info
     */
    public void start(DownInfo info) {
        boolean isInQueue = false;
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (info.getFileName().equals(mDownQueue.get(i).getFileName())
                    && info.getMUserId().equals(mDownQueue.get(i).getMUserId())
                    && info.getMUrlTag().equals(mDownQueue.get(i).getMUrlTag())) {
                isInQueue = true;
                break;
            }
        }
        if (isInQueue != true) {
            info.setMSate(DownState.WILL_START);
            DownInfoDbUtils.getInstance().up(info);
            mDownQueue.add(info);
            if (mListener != null) {
                mListener.onAddDownQueueSuc(info);
            }
            if (mDownQueue.size() == 1) {
                startFirst(info);
            }
        } else {
            startFirst(info);
        }

    }

    /**
     * 停止对应的文件下载
     *
     * @param info
     */
    public void stop(DownInfo info) {
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (mDownQueue.get(i).getFileName().equals(info.getFileName())
                    && mDownQueue.get(i).getMUrlTag().equals(info.getMUrlTag())
                    && mDownQueue.get(i).getMUserId().equals(info.getMUserId())
            ) {
                //暂停了要将其从队列中移除掉
                mDownQueue.remove(i);
                break;
            }
        }
        if (info.getMSate() == DownState.START || info.getMSate() == DownState.REQUEST || info.getMSate() == DownState.DOWN) {
            mDownLoad.stopDown(info);
            mCurrentInfo = null;
            //只有停止了正在下载的才继续下载下一个,判断暂停下载后，队列还有等待下载的，则取出队列头继续接着下载
            if (mDownQueue.size() > 0) {
                start(mDownQueue.get(0));
            }
        } else {
            info.setMSate(DownState.STOP);
            DownInfoDbUtils.getInstance().up(info);
            if (mListener != null)
                mListener.onDownPause(info);
        }
    }

    /**
     * 中断下载
     *
     * @param info
     */
    private void stopDownQueue(DownInfo info) {
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (mDownQueue.get(i).getFileName().equals(info.getFileName())
                    && mDownQueue.get(i).getMUrlTag().equals(info.getMUrlTag())
                    && mDownQueue.get(i).getMUserId().equals(info.getMUserId())
            ) {
                //暂停了要将其从队列中移除掉
                mDownQueue.remove(i);
                break;
            }
        }
        if (info.getMSate() == DownState.START || info.getMSate() == DownState.REQUEST || info.getMSate() == DownState.DOWN) {
            mDownLoad.stopDown(info);
            mCurrentInfo = null;
        } else {
            info.setMSate(DownState.STOP);
            DownInfoDbUtils.getInstance().up(info);
            if (mListener != null)
                mListener.onDownPause(info);
        }

    }


    /**
     * 获取对应id 的全部文件
     *
     * @param id
     * @return
     */
    public ArrayList<DownInfo> getFileList(String id) {
        ArrayList<DownInfo> dataList = (ArrayList<DownInfo>) DatabaseManager.getInstance()
                .getDaoSession().getDownInfoDao()
                .queryBuilder()
                .where(DownInfoDao.Properties.MUserId.eq(id))
                .list();
        if (dataList == null || dataList.size() == 0) {
            //没有数据时，判断数据库的数据与文件夹的数据是否同步对应，不同步时删除掉该记录
            File file = new File(mDownDir + "/" + id);
            if (!file.exists()) {
                return null;
            } else {
                if (file.listFiles() == null || file.listFiles().length == 0) {

                } else {
                    file.delete();
                }
            }
            return null;
        }
        for (int i = dataList.size() - 1; i >= 0; i--) {
            if (mDownLoad.isDown() != true && dataList.get(i).getMSate() != DownState.OVER) {
                dataList.get(i).setMSate(DownState.STOP);
                DownInfoDbUtils.getInstance().up(dataList.get(i));
            }
            //有数据时，判断数据库的数据与文件夹的数据是否同步对应，不同步时删除掉该记录
            File file = new File(dataList.get(i).getFileDir() + dataList.get(i).getFileName());
            if (!file.exists()) {
                //判断是否文件夹中有这个文件，如果没有，但有数据库记录，则要删掉数据库的对应数据
                DownInfoDbUtils.getInstance().delete(dataList.get(i));
                dataList.remove(i);
            } else {
                dataList.get(i).setMCurrentSize(file.length());
                DownInfoDbUtils.getInstance().up(dataList.get(i));
            }
        }
        return dataList;
    }

    public void deleteFile(DownInfo info) {
        if (mDownLoad.isDown()) {
            if (info.getMSate() == DownState.REQUEST
                    || info.getMSate() == DownState.DOWN
                    || info.getMSate() == DownState.START
                    || info.getMSate() == DownState.WILL_START) {
                stop(info);
            }
        }

        File file = new File(info.getFileDir() + info.getFileName());
        file.delete();
        DownInfoDbUtils.getInstance().delete(info);
    }

    public void deleteAllFile() {
        mDownQueue.clear();
        DownInfoDbUtils.getInstance().deleteAll();
        DeleteFileUtil.deleteDirectory(mDownDir + "/");
    }

    public void deleteUserIdFiles(String uesrId) {
        mDownQueue.clear();
        mDownLoad.cancelDown();
        DownInfoDbUtils.getInstance().deleteUserAll(uesrId);
        DeleteFileUtil.deleteDirectory(mDownDir + "/" + uesrId + "/");
    }

    @Override
    public void onDownSuc(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (mDownQueue.get(i).getFileName().equals(info.getFileName())
                    && mDownQueue.get(i).getMUserId().equals(info.getMUserId())
                    && mDownQueue.get(i).getMUrlTag().equals(info.getMUrlTag())) {
                mDownQueue.remove(i);
                break;
            }
        }
        if (mListener != null)
            mListener.onDownSuc(info);
        if (mDownQueue.size() > 0) {
            start(mDownQueue.get(0));
        }
    }

    @Override
    public void onDownProgress(DownInfo info) {
        float size = (float) info.getMCurrentSize() / (float) info.getMTotalSize();
        int itemSize = (int) (size * 100);
        if (itemSize % 5 == 0) {
        }
        if (mListener != null)
            mListener.onDownProgress(info);
    }

    @Override
    public void onDownSatrt(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        if (mListener != null)
            mListener.onDownStart(info);
    }

    @Override
    public void onDownPause(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        if (mListener != null)
            mListener.onDownPause(info);
    }

    @Override
    public void onDownFaile(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (mDownQueue.get(i).getFileName().equals(info.getFileName())
                    && mDownQueue.get(i).getMUserId().equals(info.getMUserId())
                    && mDownQueue.get(i).getMUrlTag().equals(info.getMUrlTag())) {
                mDownQueue.remove(i);
                break;
            }
        }

        if (mListener != null)
            mListener.onDownFaild(info);
    }

    @Override
    public void onDownHeadInfoSuc(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        mCurrentInfo = info;
        mDownLoad.startDown(info);
    }

    @Override
    public void onDownHeadInfoFaile(DownInfo info) {
        DownInfoDbUtils.getInstance().up(info);
        for (int i = 0; i < mDownQueue.size(); i++) {
            if (mDownQueue.get(i).getFileName().equals(info.getFileName())
                    && mDownQueue.get(i).getMUserId().equals(info.getMUserId())
                    && mDownQueue.get(i).getMUrlTag().equals(info.getMUrlTag())) {
                mDownQueue.remove(i);
                break;
            }
        }
        if (mListener != null) {
            mListener.onDownFaild(info);
        }
    }

    @Override
    public void onDownInfoRequestConnect(DownInfo info) {
        if (mListener != null) {
            mListener.onDownconnect(info);
        }
    }
}
