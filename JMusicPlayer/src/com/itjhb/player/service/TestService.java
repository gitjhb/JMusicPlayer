package com.itjhb.player.service;

import com.itjhb.player.activity.IService;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service{


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("AudioService", "服务被绑定了");
		return new MyBinder();
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("TestService", "创建服务");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("TestService", "开始服务");
		
		return super.onStartCommand(intent, flags, startId);
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("TestService", "销毁服务");
	}
	
	
	private class MyBinder extends Binder implements IService {
		// 间接利用代理人MyBinder调用了服务（AudioService）的方法

		@Override
		public void play(int process) {
			// TODO Auto-generated method stub

		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}
		



	}
	

}
