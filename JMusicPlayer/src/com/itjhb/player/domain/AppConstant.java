package com.itjhb.player.domain;

public class AppConstant {
	
	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT"; // 当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";// 新音乐长度更新动作

	
	public class PlayerMsg {
		public static final int PLAY_MSG = 1;		//播放
		public static final int PAUSE_MSG = 2;		//暂停
		public static final int STOP_MSG = 3;		//停止
		public static final int CONTINUE_MSG = 4;	//继续
		public static final int PRIVIOUS_MSG = 5;	//上一首
		public static final int NEXT_MSG = 6;		//下一首
		public static final int PROGRESS_CHANGE = 7;//进度改变
		public static final int PLAYING_MSG = 8;	//正在播放
	}
}
