package com.itjhb.player.adapter;


import java.util.List;

import com.itjhb.player.domain.Music;
import com.itjhb.player.utils.MediaUtil;

import coms.itjhb.player.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class MusicListAdapter extends BaseAdapter{
	private Context context;		//�����Ķ�������
	private List<Music> mp3Infos;	//���Mp3Info���õļ���
	private Music music;		//Mp3Info��������
	private int pos = -1;			//�б�λ��
	private int height;
	

	
	/**
	 * ���캯��
	 * @param context	������
	 * @param mp3Infos  ���϶���
	 */
	public MusicListAdapter(Context context, List<Music> mp3Infos, int height) {
		this.context = context;
		this.mp3Infos = mp3Infos;
		this.height=height;
	}

	@Override
	public int getCount() {
		return mp3Infos.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null)
		{
			viewHolder = new ViewHolder();
			if(height>900){
				convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item_layout, null);
			} else {
				convertView = LayoutInflater.from(context).inflate(R.layout.small_music_list_item_layout, null);
			}
			viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
			viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
			viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.music_Artist);
			viewHolder.musicDuration = (TextView) convertView.findViewById(R.id.music_duration);
			convertView.setTag(viewHolder);			//��ʾ��View���һ����������ݣ�
		} else {
			viewHolder = (ViewHolder)convertView.getTag();//ͨ��getTag�ķ���������ȡ����
		}
		music = mp3Infos.get(position);
		if(position == pos) {
			viewHolder.albumImage.setImageResource(R.drawable.item);
		} else {
			Bitmap bitmap;
			if(height>900){
				
				 bitmap = MediaUtil.getArtwork(context, music.getId(),music.getAlbumId(), true, false);
			}else {
				
				 bitmap = MediaUtil.getArtwork(context, music.getId(),music.getAlbumId(), true, true);
			}
			if(bitmap == null) {
				viewHolder.albumImage.setImageResource(R.drawable.music5);
			} else {
				viewHolder.albumImage.setImageBitmap(bitmap);
			}
			
		}
		viewHolder.musicTitle.setText(music.getTitle());			//��ʾ����
		viewHolder.musicArtist.setText(music.getArtist());		//��ʾ������
		viewHolder.musicDuration.setText(MediaUtil.formatTime(music.getDuration()));//��ʾʱ��
		
		return convertView;
	}
	
	
	/**
	 * ����һ���ڲ���
	 * ������Ӧ�Ŀؼ�����
	 * @author wwj
	 *
	 */
	public class ViewHolder {
		//���пؼ���������
		public ImageView albumImage;	//ר��ͼƬ
		public TextView musicTitle;		//���ֱ���
		public TextView musicDuration;	//����ʱ��
		public TextView musicArtist;	//����������
	}
}
