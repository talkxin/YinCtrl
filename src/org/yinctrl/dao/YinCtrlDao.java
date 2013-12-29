package org.yinctrl.dao;

import java.io.IOException;
import java.lang.reflect.Method;

import org.yinctrl.pojo.Input;
import org.yinctrl.pojo.InputEnum;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class YinCtrlDao {
	// 录音對象
	private MediaRecorder mRecorder = null;
	// 錄音播放對象
	private MediaPlayer mPlayer = null;
	// 錄音存儲的位置
	private static String mFileName = null;

	final int ID_LED = 19871103;

	private Context context;
	// 閃光通知
	private NotificationManager nm;
	private Notification notification;

	public YinCtrlDao(Context context) {
		// TODO Auto-generated constructor stub
		// 加載類庫
		System.loadLibrary("yinctrl");
		// 获取context
		this.context = context;
		// 閃光通知
		nm = (NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE);
		notification = new Notification();
		// notification.ledARGB = 0xFF0000;
		// //这里是颜色，我们可以尝试改变，理论上0xFF0000是红色，0x00FF00是绿色
		notification.ledOnMS = 100;
		notification.ledOffMS = 100;
	}

	/**
	 * 返回硬件配置中的按键event
	 * 
	 * @return
	 */
	public String getSystemReadpower() {
		String[] infos = getdevices().replaceAll("\\n", "").split("/");
		String info = infos[infos.length - 1].replaceAll("input", "");
		return "/dev/input/event" + info;
	}

	/**
	 * 初始化检测event的epoll
	 * 
	 * @return
	 */
	public int startEpoll() {
		String path = getSystemReadpower();
		return startepoll(path);
	}

	/**
	 * 通过epoll返回按键的监听
	 * 
	 * @return
	 */
	public Input goEpoll() {
		return getReadpower();
	}

	/**
	 * 开始触发方法
	 * 
	 * @param in
	 * @param en
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void inputGo(Input in, InputEnum en) throws IllegalStateException,
			IOException {
		switch (en) {
		case Audio:
			if (in.code == 114 || in.code == 115)
				toAudio(in, en);
			break;
		}
	}

	/**
	 * 录音的私有方法
	 * 
	 * @param in
	 * @param en
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private void toAudio(Input in, InputEnum en) {
		// 新建一个录音
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.aac";
		// 按下下音量键
		if (in.code == 114) {
			if (in.value == 1) {
				// 开始录音
				setFlashlightEnabled(true, 0xFF0000, true);
				mRecorder = new MediaRecorder();
				// 设置音源为Micphone
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				// 设置封装格式
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mRecorder.setOutputFile(mFileName);
				// 设置编码格式
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

				try {
					mRecorder.prepare();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mRecorder.start();
			} else {
				// 录音结束，回位
				setFlashlightEnabled(false, 0xFF0000, false);
				mRecorder.stop();
				mRecorder.release();
			}
		} else if (in.code == 115) {
			if (in.value == 1) {
				setFlashlightEnabled(true, 0x00FF00, false);
				mPlayer = new MediaPlayer();
				// 设置要播放的文件
				try {
					mPlayer.setDataSource("/storage/emulated/0/audiorecordtest.aac");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						// 播放完毕事件
						setFlashlightEnabled(false, 0x00FF00, false);
					}
				});
				try {
					mPlayer.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 播放之
				mPlayer.start();
			} else {
			}
		}
	}

	/**
	 * 设置闪光灯的开启和关闭
	 * 
	 * @param isEnable
	 * @author linc
	 * @date 2012-3-18
	 */
	private void setFlashlightEnabled(boolean isEnable, int color,
			boolean vibrate) {
		// 控制led灯的颜色
		notification.ledARGB = color;
		// 闪烁模式
		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		// 震动
		if (vibrate)
			notification.defaults = Notification.DEFAULT_VIBRATE;
		else
			notification.defaults = 0;
		if (isEnable) {
			nm.notify(ID_LED, notification);
		} else {
			nm.cancel(ID_LED);
		}
	}

	// 讀取按鍵信息
	private native Input getReadpower();

	// 返回驱动信息
	private native String getdevices();

	// 初始化epoll函数
	private native int startepoll(String path);
}
