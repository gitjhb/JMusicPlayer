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
		Log.i("AudioService", "���񱻰���");
		return new MyBinder();
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("TestService", "��������");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("TestService", "��ʼ����");
		
		return super.onStartCommand(intent, flags, startId);
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("TestService", "���ٷ���");
	}
	
	
	private class MyBinder extends Binder implements IService {
		// ������ô�����MyBinder�����˷���AudioService���ķ���

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
