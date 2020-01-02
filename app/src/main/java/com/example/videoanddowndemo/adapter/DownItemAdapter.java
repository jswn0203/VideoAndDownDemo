package com.example.videoanddowndemo.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.videoanddowndemo.R;
import com.example.videoanddowndemo.down.DownInfo;
import com.example.videoanddowndemo.down.DownListItem;
import com.example.videoanddowndemo.down.DownState;

import java.util.List;

public class DownItemAdapter extends BaseQuickAdapter<DownListItem, BaseViewHolder> {
    public DownItemAdapter(int layoutResId, @Nullable List<DownListItem> data) {
        super(layoutResId, data);
    }

    public DownItemAdapter(@Nullable List<DownListItem> data) {
        super(data);
    }

    public DownItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder helper, DownListItem item) {
        int totalSize = (int) (item.info.getMTotalSize() / 1024 / 1024);
        int currentSize = (int) (item.info.getMCurrentSize() / 1024 / 1024);
        helper.setText(R.id.tv_down_size, currentSize + "M/" + totalSize + "M" + "   " + item.info.getFileName());
        helper.setProgress(R.id.item_pb, currentSize, totalSize);
        helper.addOnClickListener(R.id.tv_down_state);
        if (item.info.getMSate() == DownState.DOWN) {
            helper.setText(R.id.tv_down_state, "下载中");
        } else if (item.info.getMSate() == DownState.WILL_START) {
            helper.setText(R.id.tv_down_state, "等待下载");
        } else if (item.info.getMSate() == DownState.STOP) {
            helper.setText(R.id.tv_down_state, "已停止");
        } else if (item.info.getMSate() == DownState.START) {
            helper.setText(R.id.tv_down_state, "开始下载");
        } else if (item.info.getMSate() == DownState.OVER) {
            helper.setText(R.id.tv_down_state, "观看");
        } else if (item.info.getMSate() == DownState.FAIL) {
            helper.setText(R.id.tv_down_state, "下载失败");
        } else if (item.info.getMSate() == DownState.REQUEST) {
            helper.setText(R.id.tv_down_state, "请求连接中");
        }
    }
}
