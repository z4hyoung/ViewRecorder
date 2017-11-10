package com.z4hyoung.recorder.demo;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.z4hyoung.demo.R;
import com.z4hyoung.recorder.ViewRecorder;

import java.io.File;
import java.io.IOException;

public class ViewRecorderDemoActivity extends AppCompatActivity {
    private static final String TAG = "ViewRecorderDemo";

    private Context mAppContext;

    private View mRootView;

    private Button mButtonRecord;

    private Button mButtonSwitch;

    private TextView mTextView;

    private Handler mMainHandler;

    private Handler mWorkerHandler;

    private ViewRecorder mViewRecorder;

    private static int mNumber = 0;

    private boolean mRecording = false;

    private boolean mFullscreen = false;

    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            mTextView.setText(String.valueOf(mNumber++));
            mMainHandler.postDelayed(this, 500);
        }
    };

    private final View.OnClickListener mRecordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mButtonRecord.setEnabled(false);
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRecording) {
                        stopRecord();
                    } else {
                        startRecord();
                    }
                    updateRecordButtonText();
                }
            });
        }
    };

    private final View.OnClickListener mSwitchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mButtonSwitch.setEnabled(false);
            if (mRecording) {
                mViewRecorder.setRecordedView(mFullscreen ? mTextView : mRootView);
                mFullscreen = !mFullscreen;
                mButtonSwitch.setText(mFullscreen ? R.string.center_view : R.string.full_screen);
                mButtonSwitch.setEnabled(true);
            }
        }
    };

    private final MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e(TAG, "MediaRecorder error: type = " + what + ", code = " + extra);
            mViewRecorder.reset();
            mViewRecorder.release();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();

        mRootView = findViewById(R.id.root);
        mTextView = (TextView) findViewById(R.id.text);
        mButtonRecord = (Button) findViewById(R.id.record);
        mButtonRecord.setOnClickListener(mRecordOnClickListener);
        mButtonSwitch = (Button) findViewById(R.id.switcher);
        mButtonSwitch.setOnClickListener(mSwitchOnClickListener);

        mMainHandler = new Handler();
        HandlerThread ht = new HandlerThread("bg_view_recorder");
        ht.start();
        mWorkerHandler = new Handler(ht.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainHandler.removeCallbacks(mUpdateTextRunnable);
        if (mRecording) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    updateRecordButtonText();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainHandler.post(mUpdateTextRunnable);
        updateRecordButtonText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkerHandler.getLooper().quit();
    }

    private void updateRecordButtonText() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mButtonRecord.setText(mRecording ? R.string.stop_record : R.string.start_record);
                mButtonRecord.setEnabled(true);

                mButtonSwitch.setEnabled(mRecording);
                if (mRecording) {
                    mFullscreen = false;
                    mButtonSwitch.setText(R.string.full_screen);
                }
            }
        });
    }

    private void startRecord() {
        File directory = mAppContext.getExternalCacheDir();
        if (directory != null) {
            directory.mkdirs();
            if (!directory.exists()) {
                Log.w(TAG, "startRecord failed: " + directory + " does not exist!");
                return;
            }
        }

        mViewRecorder = new ViewRecorder();
        mViewRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // uncomment this line if audio required
        mViewRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mViewRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mViewRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mViewRecorder.setVideoFrameRate(5); // 5fps
        mViewRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mViewRecorder.setVideoSize(720, 1280);
        mViewRecorder.setVideoEncodingBitRate(2000 * 1000);
        mViewRecorder.setOutputFile(getExternalCacheDir() + "/" + System.currentTimeMillis() + ".mp4");
        mViewRecorder.setOnErrorListener(mOnErrorListener);

        mViewRecorder.setRecordedView(mTextView);
        try {
            mViewRecorder.prepare();
            mViewRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "startRecord failed", e);
            return;
        }

        Log.d(TAG, "startRecord successfully!");
        mRecording = true;
    }

    private void stopRecord() {
        try {
            mViewRecorder.stop();
            mViewRecorder.reset();
            mViewRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecording = false;
        Log.d(TAG, "stopRecord successfully!");
    }
}
