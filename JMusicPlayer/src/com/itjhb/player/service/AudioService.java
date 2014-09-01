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
	
	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION";	//更新动作
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION";		//控制动作
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT";	//当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";//新音乐长度更新动作

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("AudioService", "服务被绑定了");
		return null;
	}
	
	/**
	 * handler用来接收消息，来发送广播更新播放时间
	 */

	private MyReceiver myReceiver;

	



	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("AudioService", "服务被创建了");
		musicList = MediaUtil.getMp3Infos(getApplicationContext());
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (play_status == 1) { // 单曲循环
					mPlayer.start();
				} else if (play_status == 2) { // 全部循环
					listPosition++;
					if(listPosition > musicList.size() - 1) {	//变为第一首的位置继续播放
						listPosition = 0;
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getUrl();
					playMusic(0);
				} else if (play_status == 3) { // 顺序播放
					listPosition++;	//下一首位置
					if (listPosition <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
						path = musicList.get(listPosition).getUrl();
						playMusic(0);
					}else {
						mPlayer.seekTo(0);
						listPosition = 0;
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
					}
				} else if(play_status == 4) {	//随机播放
					
					listPosition = (int) (Math.random() * (musicList.size() - 1));
					System.out.println("currentIndex ->" + listPosition);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
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
		Log.i("AudioService", "播放音乐");
		

		if (position == 0) {

			try {
				mPlayer.reset();
				mPlayer.setDataSource(path);
				mPlayer.setOnPreparedListener(AudioService.this);
				mPlayer.prepareAsync();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "获取音乐文件失败", 0).show();
			}
		} else if (position <= mPlayer.getCurrentPosition()) {
			mPlayer.start();
		}
		//发送广播通知主界面播放进度
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
						Log.i("AudioService", "线程运行");
						currentMills = mPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
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
		Log.i("AudioService", "服务被销毁了");
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
				play_status = 1; // 将播放状态置为1表示：单曲循环
				break;
			case 2:
				play_status = 2;	//将播放状态置为2表示：全部循环
				break;
			case 3:
				play_status = 3;	//将播放状态置为3表示：顺序播放
				break;
			case 4:
				play_status = 4;	//将播放状态置为4表示：随机播放
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
