package com.itjhb.player.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.itjhb.player.adapter.MusicListAdapter;
import com.itjhb.player.domain.Music;
import com.itjhb.player.service.AudioService;
import com.itjhb.player.utils.MediaUtil;
import com.itjhb.player.utils.Utils;

import coms.itjhb.player.R;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener {

	Button btn_play, btn_pasue, btn_previous;
	private FrameLayout record_control;
	private Music currentMusic;
	private IService myBinder;
	private List<Music> musicList;
	private boolean isPlaying = false;
	private boolean isPause = false;
	private int play_status = 0;

	ListView music_list;
	public TextView currentProgress; // 当前进度消耗的时间
	private SeekBar music_progressBar; // 歌曲进度控件
	private TextView finalProgress; // 歌曲时间
	private PlayerReceiver playerReceiver;

	private int listPosition = 0; // 播放歌曲在mp3Infos的位置
	public int currentMills = 0;// 当前歌曲播放时间
	private int duration; // 歌曲长度
	private int lastClick=0;
	private View preView=null;

	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT"; // 当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";// 新音乐长度更新动作

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 去掉Activity上面的状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.music_play_layout);

		// Intent intent = new Intent(this, MyAnimation.class);

		// 得到SD卡上的所有音乐，放在全局变量musiclist中
		initialMusic();
		registerReceiver();
		findViewById();

		MusicListAdapter myAdapter = new MusicListAdapter(this, musicList, 1000);
		music_list.setAdapter(myAdapter);
		music_list.setOnItemClickListener(new MyOnItemClickListener());

		music_progressBar.setMax(duration);
		finalProgress.setText(MediaUtil.formatTime(duration));
		music_progressBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						if (isPlaying) {
							currentMills = seekBar.getProgress();
							executeAction(Utils.ACTION_SEEKTO);
						}
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}
				});

		btn_play.setOnClickListener(this);
		btn_pasue.setOnClickListener(this);
		btn_previous.setOnClickListener(this);

	}

	private void findViewById() {
		btn_play = (Button) findViewById(R.id.play_music);
		btn_pasue = (Button) findViewById(R.id.next_music);
		btn_previous = (Button) findViewById(R.id.previous_music);
		music_list = (ListView) findViewById(R.id.music_list);
		music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
		currentProgress = (TextView) findViewById(R.id.current_progress);
		finalProgress = (TextView) findViewById(R.id.final_progress);
		
	}

	private void initialMusic() {
		// TODO Auto-generated method stub
		musicList = MediaUtil.getMp3Infos(this);
		if (musicList == null) {
			Toast.makeText(this, "音乐列表初始化失败", 0).show();
			return;
		}
		duration = (int) musicList.get(0).getDuration();

	}

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			listPosition = position; // 获取列表点击的位置

			executeAction(Utils.ACTION_STOP_AND_PLAY);

			isPlaying = true;
			btn_play.setBackgroundResource(R.drawable.pause_selector);
			finalProgress.setText(MediaUtil.formatTime(duration));
			view.setBackgroundColor(Color.argb(0xAA, 0x33, 0x33, 0x33));
			if(preView!=null) preView.setBackgroundColor(Color.TRANSPARENT);
			lastClick=position;
			preView=view;
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Intent intent = new Intent(this, AudioService.class);
		stopService(intent);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Intent intent = new Intent(this, AudioService.class);
		Intent intent = new Intent(getApplicationContext(), AudioService.class);
		Bundle bundle = new Bundle();
		int id = v.getId();
		if (id == R.id.play_music) {
			if (isPlaying) {
				btn_play.setBackgroundResource(R.drawable.play_selector);
				executeAction(Utils.ACTION_PAUSE);
				isPlaying = false;

			} else {
				btn_play.setBackgroundResource(R.drawable.pause_selector);
				executeAction(Utils.ACTION_PLAY);
				currentMills = 0;
				isPlaying = true;
			}
		} else if (id == R.id.previous_music) {
			isPlaying = true;
			btn_play.setBackgroundResource(R.drawable.pause_selector);
			if (listPosition > 0) {
				listPosition = listPosition - 1;
				executeAction(Utils.ACTION_PREVIOUS);
			} else {
				Toast.makeText(getApplicationContext(), "前面没有了", 0).show();
			}
		} else if (id == R.id.next_music) {
			isPlaying = true;
			btn_play.setBackgroundResource(R.drawable.pause_selector);
			if (listPosition < musicList.size() - 1) {
				listPosition = listPosition + 1;
				executeAction(Utils.ACTION_NEXT);
			} else {
				Toast.makeText(getApplicationContext(), "最后一首了", 0).show();
			}
		}
	}

	public void executeAction(String action) {
		Intent intent = new Intent(MainActivity.this, AudioService.class);
		intent.setAction(action);
		intent.putExtra("status", play_status);
		intent.putExtra("position", listPosition);
		intent.putExtra("currentTime", currentMills);
		startService(intent);

	}

	public void bind() {
		Intent intent = new Intent(this, AudioService.class);
		// conn: 用来和服务建立联系，一定不能为空，不然就没有意义了。相当于中间代理人，让Activity和Service联系起来.
		MyConnection conn = new MyConnection();
		bindService(intent, conn, BIND_AUTO_CREATE);

	}

	private class MyConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// 在服务被成功绑定的时候调用的方法
			myBinder = (IService) service;
			System.out.println("服务把代理人返回了");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			// 在服务失去绑定的时候调用的方法:只有程序异常，进程异常，中止后才会被调用

		}

	}

	private void registerReceiver() {
		// 定义和注册广播接收器
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		registerReceiver(playerReceiver, filter);
	}

	public class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				System.out.println("收到广播");
				currentMills = intent.getIntExtra("currentTime", -1);
				currentProgress.setText(MediaUtil.formatTime(currentMills));
				music_progressBar.setProgress(currentMills);
			} else if (action.equals(MUSIC_DURATION)) {
				// int duration = intent.getIntExtra("duration", -1);
				// music_progressBar.setMax(duration);
				// finalProgress.setText(MediaUtil.formatTime(duration));
			} else if (action.equals(UPDATE_ACTION)) {
				// 获取Intent中的current消息，current代表当前正在播放的歌曲
				// listPosition = intent.getIntExtra("current", -1);
				// path = music_list.get(listPosition).getUrl();
				// if (listPosition >= 0) {
				// musicTitle.setText(music_list.get(listPosition).getTitle());
				// musicArtist.setText(music_list.get(listPosition).getArtist());
				// }
				// if (listPosition == 0) {
				// finalProgress.setText(MediaUtil.formatTime(music_list.get(
				// listPosition).getDuration()));
				// playBtn.setBackgroundResource(R.drawable.pause_selector);
				// isPause = true;
				// }
			}
		}
	}

}
