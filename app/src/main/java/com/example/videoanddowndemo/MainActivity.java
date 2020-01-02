package com.example.videoanddowndemo;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.video)
    VideoView video;
    @BindView(R.id.tv_speed)
    TextView tvSpeed;
    @BindView(R.id.bottom_pause)
    TextView bottomPause;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.time)
    TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        String path = getIntent().getStringExtra("path");
        video.setVideoPath(path);
        video.start();
    }

    @OnClick({R.id.back, R.id.down, R.id.tv_speed, R.id.bottom_pause})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.down:
                break;
            case R.id.tv_speed:
                break;
            case R.id.bottom_pause:
                break;
        }
    }
}
