package com.example.videoanddowndemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videoanddowndemo.ascii.AsciiVideoProcessor;
import com.example.videoanddowndemo.ascii.AsciiView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 视频转文字动画播放页：
 * 先把视频抽帧并转成 ASCII 字符画，再按原速率循环播放字符动画。
 */
public class AsciiActivity extends AppCompatActivity implements AsciiVideoProcessor.Callback {

    private static final int COLS = 96;
    private static final String MSG_FORMAT = "%02d:%02d:%02d / %02d:%02d:%02d";

    @BindView(R.id.ascii_view)
    AsciiView asciiView;
    @BindView(R.id.tv_ascii_progress)
    TextView tvProgress;
    @BindView(R.id.progress_ascii)
    ProgressBar progressBar;
    @BindView(R.id.bottom_pause)
    TextView bottomPause;
    @BindView(R.id.tv_ascii_time)
    TextView tvTime;

    private final AsciiVideoProcessor processor = new AsciiVideoProcessor();

    private List<String> frames;
    private long frameIntervalMs;
    private int frameIndex;
    private boolean playing;
    private long elapsedMs;

    private final Handler playHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (playing) {
                advanceFrame();
                playHandler.sendEmptyMessageDelayed(0, frameIntervalMs);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ascii);
        ButterKnife.bind(this);

        String path = getIntent().getStringExtra("path");
        if (path == null || path.length() == 0) {
            Toast.makeText(this, "没有可用的视频", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        asciiView.setVisibility(View.INVISIBLE);
        processor.start(path, COLS, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay();
        processor.cancel();
    }

    @OnClick({R.id.back, R.id.bottom_pause, R.id.ascii_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.bottom_pause:
            case R.id.ascii_view:
                togglePlay();
                break;
            default:
                break;
        }
    }

    private void togglePlay() {
        if (frames == null || frames.isEmpty()) {
            return;
        }
        if (playing) {
            stopPlay();
        } else {
            startPlay();
        }
    }

    private void startPlay() {
        playing = true;
        bottomPause.setText("暂停");
        playHandler.removeMessages(0);
        playHandler.sendEmptyMessageDelayed(0, frameIntervalMs);
    }

    private void stopPlay() {
        playing = false;
        playHandler.removeMessages(0);
        if (bottomPause != null) {
            bottomPause.setText("播放");
        }
    }

    private void advanceFrame() {
        if (frames == null || frames.isEmpty()) {
            return;
        }
        frameIndex = (frameIndex + 1) % frames.size();
        elapsedMs += frameIntervalMs;
        asciiView.setFrame(frames.get(frameIndex));
        tvTime.setText(formatTime(elapsedMs));
    }

    private static String formatTime(long ms) {
        long s = ms / 1000;
        return String.format(MSG_FORMAT,
                s / 3600, s / 60 % 60, s % 60,
                s / 3600, s / 60 % 60, s % 60);
    }

    // ---- AsciiVideoProcessor.Callback ----

    @Override
    public void onProgress(int done, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(done);
        tvProgress.setText("正在解析视频 " + done + "/" + total);
    }

    @Override
    public void onDone(List<String> frames, long frameIntervalMs) {
        this.frames = frames;
        this.frameIntervalMs = Math.max(30, frameIntervalMs);
        this.frameIndex = 0;
        this.elapsedMs = 0;
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        asciiView.setVisibility(View.VISIBLE);
        asciiView.setFrame(frames.get(0));
        startPlay();
    }

    @Override
    public void onError(String message) {
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
