package com.example.videoanddowndemo.down;

import android.os.Environment;
import android.os.StatFs;

import com.example.videoanddowndemo.down.IListener.IDownListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DownLoader {
    private IDownListener mListener;

    public void setDownListener(IDownListener listener) {
        mListener = listener;
    }

    public DownLoader() {
    }

    public void startDownInfo(final DownInfo info) {
        if (mListener != null) {
            info.setMSate(DownState.REQUEST);
            mListener.onDownInfoRequestConnect(info);
        }
        OkHttpUtil.getInstance().header(info.getMUrlTag(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mListener != null) {
                    info.setMSate(DownState.FAIL);
                    mListener.onDownHeadInfoFaile(info);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() != true) {
                    if (mListener != null) {
                        info.setMSate(DownState.FAIL);
                        mListener.onDownHeadInfoFaile(info);
                    }
                } else {
                    String size = response.headers().get("Content-Length");
                    String accpect = response.headers().get("Accept-Ranges");

                    /**
                     * 判断链接的服务器是否支持断点下载，不支持时，一定要将记录表的数据current size重新设置为0
                     * 否则在暂停后重新下载将会在下载完后，出现记录表的当前大小超出总大小
                     */
                    if (!"bytes".equals(accpect)){
                        info.setMCurrentSize(0);
                    }
                    info.setMTotalSize(Long.valueOf(size));
                    if (info.getMCurrentSize() == info.getMTotalSize()) {
                        if (mListener != null) {
                            info.setMSate(DownState.OVER);
                            mListener.onDownSuc(info);
                        }
                        return;
                    }
                    if (mListener != null) {
                        mListener.onDownHeadInfoSuc(info);
                    }
                }
            }
        });
    }

    public void startDown(final DownInfo info) {
        info.setMSate(DownState.START);
        if (mListener != null) {
            mListener.onDownSatrt(info);
        }
        OkHttpUtil.getInstance().down(info.getMUrlTag(), info.getMCurrentSize(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mListener != null) {
                    info.setMSate(DownState.FAIL);
                    mListener.onDownFaile(info);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (mListener != null) {
                        info.setMSate(DownState.FAIL);
                        mListener.onDownFaile(info);
                    }
                } else {
                    InputStream inputStream = null;
                    RandomAccessFile randomOutput = null;
                    try {
                        inputStream = response.body().byteStream();
                        File file = createFile(info);
                        if (file == null) {
                            OkHttpUtil.getInstance().cancelDown();
                            return;
                        }
                        randomOutput = new RandomAccessFile(file, "rw");
                        int length = 0;
                        byte[] buff = new byte[1024 * 1024];

                        randomOutput.seek(info.getMCurrentSize());
                        while ((length = inputStream.read(buff)) != -1) {
                            randomOutput.write(buff, 0, length);
                            if (mListener != null) {
                                info.setMSate(DownState.DOWN);
                                info.setMCurrentSize(info.getMCurrentSize() + length);
                                mListener.onDownProgress(info);
                            }
                        }

                        if (mListener != null) {
                            info.setMSate(DownState.OVER);
                            mListener.onDownSuc(info);
                        }
                    } catch (SocketException se) {
                        if (mListener != null) {
                            info.setMSate(DownState.STOP);
                            mListener.onDownPause(info);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mListener != null) {
                            info.setMSate(DownState.FAIL);
                            mListener.onDownFaile(info);
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        });
    }

    private File createFile(DownInfo info) throws Exception {
        if (info == null)
            return null;
        if (info.getFileDir() == null) {
            throw new Exception("文件夹没有创建");
        }
        File file = new File(info.getFileDir());
        if (!file.exists()) {
            file.mkdirs();
        }
        if (info.getFileName() == null) {
            throw new Exception("文件名没有创建");
        }

        File itemFile = new File(file, info.getFileName());
        return itemFile;
    }

    public void stopDown(DownInfo info) {
        OkHttpUtil.getInstance().cancelDown();
        if (mListener != null) {
            if (info != null)
                info.setMSate(DownState.STOP);
            mListener.onDownPause(info);
        }
    }

    public boolean isDown() {
        return OkHttpUtil.getInstance().isDown();
    }

    public void cancelDown() {
        OkHttpUtil.getInstance().cancelDown();
    }

}
