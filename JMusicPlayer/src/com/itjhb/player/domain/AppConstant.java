package com.itjhb.player.domain;

public class AppConstant {
	
	public static final String UPDATE_ACTION = "com.itjhb.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.itjhb.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.itjhb.action.MUSIC_CURRENT"; // ��ǰ���ֲ���ʱ����¶���
	public static final String MUSIC_DURATION = "com.itjhb.action.MUSIC_DURATION";// �����ֳ��ȸ��¶���

	
	public class PlayerMsg {
		public static final int PLAY_MSG = 1;		//����
		public static final int PAUSE_MSG = 2;		//��ͣ
		public static final int STOP_MSG = 3;		//ֹͣ
		public static final int CONTINUE_MSG = 4;	//����
		public static final int PRIVIOUS_MSG = 5;	//��һ��
		public static final int NEXT_MSG = 6;		//��һ��
		public static final int PROGRESS_CHANGE = 7;//���ȸı�
		public static final int PLAYING_MSG = 8;	//���ڲ���
	}
}
