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
	public TextView currentProgress; // ��ǰ�������ĵ�ʱ��
	private SeekBar music_progressBar; // �������ȿؼ�
	private TextView finalProgress; // ����ʱ��
	private PlayerReceiver playerReceiver;

	private int listPosition = 0; // ���Ÿ�����mp3Infos��λ��
	public int currentMills = 0;// ��ǰ��������ʱ��
	private int duration; // ��������
	private int lastClick=0;
	private View preView=null;

	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT"; // ��ǰ���ֲ���ʱ����¶���
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";// �����ֳ��ȸ��¶���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ȥ��Activity�����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.music_play_layout);

		// Intent intent = new Intent(this, MyAnimation.class);

		// �õ�SD���ϵ��������֣�����ȫ�ֱ���musiclist��
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
			Toast.makeText(this, "�����б��ʼ��ʧ��", 0).show();
			return;
		}
		duration = (int) musicList.get(0).getDuration();

	}

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			listPosition = position; // ��ȡ�б�����λ��

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
				Toast.makeText(getApplicationContext(), "ǰ��û����", 0).show();
			}
		} else if (id == R.id.next_music) {
			isPlaying = true;
			btn_play.setBackgroundResource(R.drawable.pause_selector);
			if (listPosition < musicList.size() - 1) {
				listPosition = listPosition + 1;
				executeAction(Utils.ACTION_NEXT);
			} else {
				Toast.makeText(getApplicationContext(), "���һ����", 0).show();
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
		// conn: �����ͷ�������ϵ��һ������Ϊ�գ���Ȼ��û�������ˡ��൱���м�����ˣ���Activity��Service��ϵ����.
		MyConnection conn = new MyConnection();
		bindService(intent, conn, BIND_AUTO_CREATE);

	}

	private class MyConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// �ڷ��񱻳ɹ��󶨵�ʱ����õķ���
			myBinder = (IService) service;
			System.out.println("����Ѵ����˷�����");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			// �ڷ���ʧȥ�󶨵�ʱ����õķ���:ֻ�г����쳣�������쳣����ֹ��Żᱻ����

		}

	}

	private void registerReceiver() {
		// �����ע��㲥������
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
				System.out.println("�յ��㲥");
				currentMills = intent.getIntExtra("currentTime", -1);
				currentProgress.setText(MediaUtil.formatTime(currentMills));
				music_progressBar.setProgress(currentMills);
			} else if (action.equals(MUSIC_DURATION)) {
				// int duration = intent.getIntExtra("duration", -1);
				// music_progressBar.setMax(duration);
				// finalProgress.setText(MediaUtil.formatTime(duration));
			} else if (action.equals(UPDATE_ACTION)) {
				// ��ȡIntent�е�current��Ϣ��current����ǰ���ڲ��ŵĸ���
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
