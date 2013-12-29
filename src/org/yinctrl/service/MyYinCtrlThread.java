package org.yinctrl.service;

import java.io.DataOutputStream;
import java.io.IOException;

import org.yinctrl.dao.YinCtrlDao;
import org.yinctrl.pojo.Input;
import org.yinctrl.pojo.InputEnum;

import android.content.Context;
import android.widget.Toast;

public class MyYinCtrlThread extends Thread {
	YinCtrlDao dao;
	Context context;

	public MyYinCtrlThread(Context context) {
		dao = new YinCtrlDao(context);
		this.context = context;
	}

	@Override
	public void run() {
		try {
			startInput();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startInput() throws IllegalStateException, IOException {
		int i = dao.startEpoll();
		if (i != -1) {
			while (true) {
				Input input = dao.goEpoll();
				// System.out.printf(
				// "key value:type=%d, code=%d, value=%d,time=%d\n",
				// input.type, input.code, input.value, input.time_sce);
				dao.inputGo(input, InputEnum.Audio);
			}
		} else {
			try {
				Process process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(
						process.getOutputStream());
				os.writeBytes("chmod 755 " + dao.getSystemReadpower() + "\n");
				os.writeBytes("exit\n");
				os.flush();
				process.waitFor();
				startInput();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "无法获取root权限，无法运行", Toast.LENGTH_LONG)
						.show();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "无法获取root权限，无法运行", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}
