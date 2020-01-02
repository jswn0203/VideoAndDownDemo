package com.example.videoanddowndemo.down;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {
    private static OkHttpUtil INSTANCE;
    private OkHttpClient mClient;
    private Call mCall;

    public static OkHttpUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OkHttpUtil();
        }
        return INSTANCE;
    }

    private OkHttpUtil() {
        mClient = new OkHttpClient();
    }

    private void setDownLoadListenr() {

    }

    public void header(String url, Callback callback) {
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("RANGE", "bytes=0" + "-")
                .head()
                .build();
        mCall = mClient.newCall(request);
        mCall.enqueue(callback);
    }

    public void down(String url, long startSize, Callback callback) {
        Request request = new Request.Builder()
                .tag(url)
                .url(url)
                .addHeader("RANGE", "bytes=" + startSize + "-")
                .build();
        mCall = mClient.newCall(request);
        mCall.enqueue(callback);
    }

    public void cancelDown() {
        if (mCall != null)
            mCall.cancel();
        mCall = null;
    }

    public boolean isDown() {
        if (mCall == null) {
            return false;
        } else {
            if (mCall.isCanceled()) {
                return false;
            }
            if (!mCall.isExecuted()) {
                return false;
            }
        }
        return true;
    }


}
