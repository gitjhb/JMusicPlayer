package com.itjhb.player.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

public class Utils {
	 public static final String ACTION_PLAY = "com.itjhb.player.PLAY";
	public static final String ACTION_PAUSE = "com.itjhb.player.PAUSE";
	public static final String ACTION_NEXT = "com.itjhb.player.NEXT";
	public static final String ACTION_PREVIOUS = "com.itjhb.player.PREVIOUS";
	public static final String ACTION_STOP = "com.itjhb.player.STOP";
	public static final String ACTION_STOP_AND_PLAY = "com.itjhb.player.STOP_and_PLAY";
	public static final String ACTION_SEEKTO = "com.itjhb.player.SEEKTO";
	
	public static final String ACTION_BROADCAST_CURRENT_TIME = "com.itjhb.player.broadcast";
	
	
	
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;

			left = 0;
			top = 0;
			right = width;
			bottom = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;

			float clip = (width - height) / 2;

			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0); 

		
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint); 

		return output;
	}
	
	 

}
