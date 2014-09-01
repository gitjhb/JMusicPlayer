package com.itjhb.player.service;

import java.io.IOException;
import java.util.List;

import com.itjhb.player.activity.IService;
import com.itjhb.player.activity.MainActivity;
import com.itjhb.player.domain.Music;
import com.itjhb.player.utils.MediaUtil;
import com.itjhb.player.utils.Utils;

import coms.itjhb.player.R;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore.Action;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

public class AudioService extends Service implements OnPreparedListener,
		OnErrorListener {

	private static MediaPlayer mPlayer = null;
	private int currentMills = 0;
	Context context;
	private List<Music> musicList = null;
	private Music currentMusic = null;
	//the music position in the list
	private int listPosition = 0;
	private int play_status = 0;
	private String path = null;
	
	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION";	//���¶���
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION";		//���ƶ���
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT";	//��ǰ���ֲ���ʱ����¶���
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";//�����ֳ��ȸ��¶���

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("AudioService", "���񱻰���");
		return null;
	}
	
	/**
	 * handler����������Ϣ�������͹㲥���²���ʱ��
	 */

	private MyReceiver myReceiver;

	



	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("AudioService", "���񱻴�����");
		musicList = MediaUtil.getMp3Infos(getApplicationContext());
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (play_status == 1) { // ����ѭ��
					mPlayer.start();
				} else if (play_status == 2) { // ȫ��ѭ��
					listPosition++;
					if(listPosition > musicList.size() - 1) {	//��Ϊ��һ�׵�λ�ü�������
						listPosition = 0;
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getUrl();
					playMusic(0);
				} else if (play_status == 3) { // ˳�򲥷�
					listPosition++;	//��һ��λ��
					if (listPosition <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
						path = musicList.get(listPosition).getUrl();
						playMusic(0);
					}else {
						mPlayer.seekTo(0);
						listPosition = 0;
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
					}
				} else if(play_status == 4) {	//�������
					
					listPosition = (int) (Math.random() * (musicList.size() - 1));
					System.out.println("currentIndex ->" + listPosition);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getUrl();
					playMusic(0);
				}
			}
		});
		
		myReceiver = new MyReceiver();
	//	handler.post(mRunnable);
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(CTL_ACTION);
//		registerReceiver(myReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		play_status = intent.getIntExtra("play_status", 0);
		listPosition = intent.getIntExtra("position", 0);
		currentMills= intent.getIntExtra("currentTime", 0);
		path = musicList.get(listPosition).getUrl();
		if (action.equals(Utils.ACTION_PLAY)) {
			playMusic(currentMills);
		} else if (action.equals(Utils.ACTION_PAUSE)) {
			pauseMusic();

		} else if (action.equals(Utils.ACTION_STOP_AND_PLAY)) {
			playMusic(0);
		}

		else if (action.equals(Utils.ACTION_NEXT)) {
			nextMusic();

		} else if (action.equals(Utils.ACTION_PREVIOUS)) {
			previousMusic();

		} else if (action.equals(Utils.ACTION_STOP)) {
			stopMusic();
		} else if (action.equals(Utils.ACTION_SEEKTO)) {
			mPlayer.seekTo(currentMills);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void nextMusic() {
			playMusic(0);

		
	}

	private void previousMusic() {
			playMusic(0);
	}

	private void playMusic(int position) {
		// TODO Auto-generated method stub
		Log.i("AudioService", "��������");
		

		if (position == 0) {

			try {
				mPlayer.reset();
				mPlayer.setDataSource(path);
				mPlayer.setOnPreparedListener(AudioService.this);
				mPlayer.prepareAsync();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "��ȡ�����ļ�ʧ��", 0).show();
			}
		} else if (position <= mPlayer.getCurrentPosition()) {
			mPlayer.start();
		}
		//���͹㲥֪ͨ�����沥�Ž���
		sendMainThreadMessage();

	}

	private void pauseMusic() {
		// TODO Auto-generated method stub
		if (mPlayer != null) {
			mPlayer.pause();
			currentMills = mPlayer.getCurrentPosition();
			
		}
		
	}

	private void stopMusic() {
		// TODO Auto-generated method stub
		if (mPlayer != null) {
			mPlayer.stop();
			currentMills = 0;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.start();
		sendMainThreadMessage();
		
	}
	
	public void sendMainThreadMessage(){
		new Thread(){
			public void run(){
				while(mPlayer.isPlaying()){
						Log.i("AudioService", "�߳�����");
						currentMills = mPlayer.getCurrentPosition(); // ��ȡ��ǰ���ֲ��ŵ�λ��
						Intent intent = new Intent();
						intent.setAction(MUSIC_CURRENT);
						intent.putExtra("currentTime", currentMills);
						sendBroadcast(intent); 
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("AudioService", "����������");
		super.onDestroy();
		mPlayer.release();
		mPlayer = null;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int control = intent.getIntExtra("control", -1);
			switch (control) {
			case 1:
				play_status = 1; // ������״̬��Ϊ1��ʾ������ѭ��
				break;
			case 2:
				play_status = 2;	//������״̬��Ϊ2��ʾ��ȫ��ѭ��
				break;
			case 3:
				play_status = 3;	//������״̬��Ϊ3��ʾ��˳�򲥷�
				break;
			case 4:
				play_status = 4;	//������״̬��Ϊ4��ʾ���������
				break;
			}

		}
	}

	/**
	 * 
	 * @param position
	 */
	// private void displaySeek(int position) {
	// if (mediaPlayer != null && position > 0
	// && position < mediaPlayer.getDuration()) {
	// mediaPlayer.seekTo(position);
	// if (iUpdateDisplayState != null) {
	// iUpdateDisplayState.updateSeekBar(position);
	// }
	// }
	//
	// }
	
	public static boolean isPlaying() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			return true;
		}
		return false;
	}
}
