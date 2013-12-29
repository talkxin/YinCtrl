package org.yinctrl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.yinctrl.dao.YinCtrlDao;
import org.yinctrl.pojo.Input;
import org.yinctrl.service.MyYinCtrlThread;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	// 1、首先通过读取cat /proc/bus/input/devices文件来获取硬件驱动所对应的input信息
	// I: Bus=0019 Vendor=0001 Product=0001 Version=0100
	// N: Name="gpio-keys"
	// P: Phys=gpio-keys/input0
	// S: Sysfs=/devices/platform/gpio-keys.0/input/input12
	// U: Uniq=
	// H: Handlers=kbd event12 keychord
	// B: PROP=0
	// B: EV=23
	// B: KEY=1000 0 1c0000 0 0 0
	// B: SW=200000
	// 2、通过获取该驱动的sysfs来监听其事件获取信息，并返回至service

	// @Override
	// protected void onCreate(Bundle savedInstanceSdtate) {
	// super.onCreate(savedInstanceSdtate);
	// // new MyYinCtrlThread(this).start();
	//
	// }
	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;
	// 录音按钮
	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;
	// 回放按钮
	private PlayButton mPlayButton = null;
	private MediaPlayer mPlayer = null;

	// 当录音按钮被click时调用此方法，开始或停止录音
	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	// 当播放按钮被click时调用此方法，开始或停止播放
	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			// 设置要播放的文件
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			// 播放之
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	// 停止播放
	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		// 设置音源为Micphone
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置封装格式
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setOutputFile(mFileName);
		// 设置编码格式
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
//		mRecorder = null;
	}

	// 定义录音按钮
	class RecordButton extends Button {
		boolean mStartRecording = true;

		OnClickListener clicker = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				onRecord(mStartRecording);
				if (mStartRecording) {
					setText("Stop recording");
				} else {
					setText("Start recording");
				}
				mStartRecording = !mStartRecording;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setText("Start recording");
			setOnClickListener(clicker);
		}
	}

	// 定义播放按钮
	class PlayButton extends Button {
		boolean mStartPlaying = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					setText("Stop playing");
				} else {
					setText("Start playing");
				}
				mStartPlaying = !mStartPlaying;
			}
		};

		public PlayButton(Context ctx) {
			super(ctx);
			setText("Start playing");
			setOnClickListener(clicker);
		}
	}

	// 构造方法
	public MainActivity() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.aac";
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		new MyYinCtrlThread(this).start();
		// 构造界面
		LinearLayout ll = new LinearLayout(this);
		mRecordButton = new RecordButton(this);
		ll.addView(mRecordButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0));
		mPlayButton = new PlayButton(this);
		ll.addView(mPlayButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0));
		setContentView(ll);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Activity暂停时释放录音和播放对象
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
