package com.example.videoanddowndemo;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.videoanddowndemo.adapter.DownItemAdapter;
import com.example.videoanddowndemo.dbUtil.DownInfoDbUtils;
import com.example.videoanddowndemo.down.DownInfo;
import com.example.videoanddowndemo.down.DownListItem;
import com.example.videoanddowndemo.down.DownState;
import com.example.videoanddowndemo.down.DownVideoManager;
import com.example.videoanddowndemo.down.IListener.DownLoadListener;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DownActivity extends AppCompatActivity implements DownLoadListener {

    @BindView(R.id.bt_down)
    Button mBtDown;
    @BindView(R.id.bt_cancel)
    Button mBtCancel;
    @BindView(R.id.rlv_down)
    RecyclerView mRlv;

    private DownItemAdapter mAdapter;

    private ArrayList<String> listUrl = new ArrayList<>();

    public ArrayList<DownListItem> mData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_demo);
        ButterKnife.bind(this);

        listUrl.add("http://edu.kunzejiaoyu.com:8082/shopweb/static/app/daishuketang_1.2.97.apk");
        listUrl.add("http://zbywsvod.zhiboyun.eaydu.com/vod/zby/1191_4598_1191_20171195_7.mp4");
        listUrl.add("http://edu.kunzejiaoyu.com:8082/shopweb/static/app/daishuketang_1.2.97.apk");
        listUrl.add("http://zbywsvod.zhiboyun.eaydu.com/vod/zby/1191_4598_1191_20171195_7.mp4");
        listUrl.add("http://edu.kunzejiaoyu.com:8082/shopweb/static/app/daishuketang_1.2.97.apk");

        mData = new ArrayList<>();
        DownVideoManager.getInstance().setDownListener(this);
        DownVideoManager.getInstance().setFielDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ADOWN");
        ArrayList<DownInfo> a = DownVideoManager.getInstance().getFileList("13247328832");
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                DownListItem item = new DownListItem();
                item.info = a.get(i);
                mData.add(item);
            }
        }

        mAdapter = new DownItemAdapter(R.layout.item_down, mData);
        mRlv.setLayoutManager(new LinearLayoutManager(this));
        mRlv.setAdapter(mAdapter);

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (mData.get(position).info.getMSate() == DownState.STOP
                        || mData.get(position).info.getMSate() == DownState.FAIL) {
                    mAdapter.notifyItemChanged(position);
                    DownVideoManager.getInstance().start(mData.get(position).info);
                } else if (mData.get(position).info.getMSate() == DownState.DOWN
                        || mData.get(position).info.getMSate() == DownState.START
                        || mData.get(position).info.getMSate() == DownState.WILL_START
                        || mData.get(position).info.getMSate() == DownState.REQUEST) {
                    DownVideoManager.getInstance().stop(mData.get(position).info);
                } else if (mData.get(position).info.getMSate() == DownState.OVER) {
                    Toast.makeText(DownActivity.this, "观看", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(DownActivity.this, MainActivity.class);
                    intent.putExtra("path", mData.get(position).info.getFileDir() + mData.get(position).info.getFileName());
                    startActivity(intent);
                }
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mData.get(position).isSelect = !mData.get(position).isSelect;
            }
        });

    }

    public String mPhone = "13247328832";
    public int mIndex = 0;

    @OnClick({R.id.bt_down, R.id.bt_cancel, R.id.bt_re_start, R.id.qiehuan, R.id.delete_selet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.qiehuan:

                mData.clear();
                DownVideoManager.getInstance().clearDown();
                if (mPhone.equals("13247328832")) {
                    mPhone = "15013413156";
                } else {
                    mPhone = "13247328832";
                }
                ArrayList<DownInfo> a0 = DownVideoManager.getInstance().getFileList(mPhone);
                if (a0 != null) {
                    for (int i = 0; i < a0.size(); i++) {
                        DownListItem item = new DownListItem();
                        item.info = a0.get(i);
                        mData.add(item);
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.bt_down:
                mBtDown.setText("获取列表");
                mData.clear();
                ArrayList<DownInfo> a = DownVideoManager.getInstance().getFileList(mPhone);
                if (a != null) {
                    for (int i = 0; i < a.size(); i++) {
                        DownListItem item = new DownListItem();
                        item.info = a.get(i);
                        mData.add(item);
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.bt_cancel:
                DownVideoManager.getInstance().deleteUserIdFiles(mPhone);
                mData.clear();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_selet:
                ArrayList<DownListItem> deleteData = new ArrayList<>();
                for (int i = mData.size() - 1; i >= 0; i--) {
                    if (mData.get(i).isSelect == true) {
                        deleteData.add(mData.get(i));
                        DownVideoManager.getInstance().deleteFile(mData.get(i).info);
                        mData.remove(i);
                    }
                }
                mAdapter.notifyDataSetChanged();
                if (deleteData.size() == 0) {
                    Toast.makeText(this, "没有选择", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_re_start:
                mIndex += 1;
                if (mIndex > 4) {
                    mIndex = 0;
                }

                int index = listUrl.get(mIndex).lastIndexOf("/");
                String name = listUrl.get(mIndex).substring(index + 1);

                DownVideoManager.getInstance()
                        .addDownQueue(listUrl.get(mIndex), "测试" + name, mPhone);
                mData.clear();
                mAdapter.notifyDataSetChanged();
                ArrayList<DownInfo> a1 = DownVideoManager.getInstance().getFileList(mPhone);
                if (a1 != null) {
                    for (int i = 0; i < a1.size(); i++) {
                        DownListItem item = new DownListItem();
                        item.info = a1.get(i);
                        mData.add(item);
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
        }
    }


    public void checkAndUpDownListState(DownInfo info) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).info.getFileName().equals(info.getFileName())
                    && mData.get(i).info.getMUrlTag().equals(info.getMUrlTag())
                    && mData.get(i).info.getMUserId().equals(info.getMUserId())) {
                mData.get(i).info.setMSate(info.getMSate());
                mData.get(i).info.setFileName(info.getFileName());
                mData.get(i).info.setMCurrentSize(info.getMCurrentSize());
                mData.get(i).info.setMTotalSize(info.getMTotalSize());
                mData.get(i).info.setMUrlTag(info.getMUrlTag());
                mData.get(i).info.setMUserId(info.getMUserId());
                mData.get(i).info.setId(info.getId());
                mData.get(i).info.setFileDir(info.getFileDir());
                mAdapter.notifyItemChanged(i);
            }
        }
    }


    @Override
    public void onDownSuc(final DownInfo info) {
        Log.e("test", "下载成功" + info.getFileName());
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
                mBtDown.setText("下载成功" + info.getMTotalSize());
            }
        });
    }

    @Override
    public void onDownProgress(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mData.size(); i++) {
                    if (mData.get(i).info.getFileName().equals(info.getFileName())
                            && mData.get(i).info.getMUrlTag().equals(info.getMUrlTag())
                            && mData.get(i).info.getMUserId().equals(info.getMUserId())) {
                        mData.get(i).info.setMCurrentSize(info.getMCurrentSize());
                        mData.get(i).info.setMSate(info.getMSate());
                        mAdapter.notifyItemChanged(i);
                    }
                }
            }
        });
    }

    @Override
    public void onDownStart(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
            }
        });

    }

    @Override
    public void onDownPause(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
            }
        });
    }

    @Override
    public void onDownFaild(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
            }
        });

    }

    @Override
    public void onDownHasExit(DownInfo info) {
        Toast.makeText(this, info.getFileName() + " 已存在", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddDownQueueSuc(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
            }
        });
    }

    @Override
    public void onDownconnect(final DownInfo info) {
        mBtDown.post(new Runnable() {
            @Override
            public void run() {
                checkAndUpDownListState(info);
            }
        });
    }
}
