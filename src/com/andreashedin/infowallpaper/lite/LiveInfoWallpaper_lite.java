package com.andreashedin.infowallpaper.lite;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

public class LiveInfoWallpaper_lite extends WallpaperService {

	public static final String SHARED_PREFERENCES_NAME = "LiveInfoSettings_lite";
	public static int SETTINGS_FILE = R.xml.settings;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	class WallpaperEngine extends Engine 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private SharedPreferences mPrefs = null;
		private Handler mHandler = new Handler();
		public Paint mPaint = new Paint();
		private boolean mVisible = false;
		private float mCenterX = 0.0f;
		private float mCenterY = 0.0f;
		
		//Settings
		private boolean mWriteBatteryText = true;
		private boolean m24hFormat = true;
		private Bitmap mBackground = null; 
		private int mBackgroundColor = 0xff000000;
		public boolean mShowBattery = true;
		public boolean mShowTime = true;
		public boolean mShowDate = true;
		private TextDescription mBattery = null;
		private TextDescription mDate = null;
		private TextDescription mTime = null;
		
		private final Runnable mExternalStorageRunner = new Runnable() {
			@Override
			public void run() {
				setBackgroundImage();
			}
		};
		
		private final BroadcastReceiver mDateAndTimeChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateDateAndTime();
				drawFrame();
			}
		};
		
		private final BroadcastReceiver mBatteryStatusReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateBatteryStatus(intent);
				drawFrame();
			}
		};
		
		WallpaperEngine() {	
			mBattery = new TextDescription();
			mDate = new TextDescription();
			mTime = new TextDescription();
			mBackground = null;
			mBackgroundColor = 0xff000000;
            mBattery.yPercentage = 0.3f;
            mDate.yPercentage = 0.4f;
            mTime.yPercentage = 0.5f;
            
            registerReceiver(mBatteryStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
            registerReceiver(mDateAndTimeChangedReceiver, intentFilter);
            
            mPrefs = LiveInfoWallpaper_lite.this.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
		}
		
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			String value = "";
			
			value = prefs.getString("imagePath", "");
			setBackgroundImage(value);
			
			mBackgroundColor = prefs.getInt("backgroundColor", Color.argb(255, 0, 0, 0));
			
			mShowTime = prefs.getBoolean("showTime", true);
			
			if(mShowTime) {
				m24hFormat = prefs.getBoolean("24hFormat", true);
				int color = prefs.getInt("timeTextColorChoice", 0xffffffff);
				int alpha = prefs.getInt("timeTextTransparencyChoice", 80);
				float a = ((float)alpha) / 100.0f;
				mTime.setColor((int)((a * 255) + 0.5f), Color.red(color), Color.green(color), Color.blue(color));
				
				int pos = prefs.getInt("timeTextPositionChoice", 50);
				mTime.yPercentage = ((float)pos) / 100.0f;
							
				mTime.size = prefs.getInt("timeTextSizeChoice", 40);
				
				value = prefs.getString("timeFont", "");
				mTime.font = LoadTypeface(value);
				
				mTime.setAlign(prefs.getString("timeTextAlign", "center"));
			}
			
			mShowBattery = prefs.getBoolean("showBattery", true);
			
			if(mShowBattery) {
				mWriteBatteryText = prefs.getBoolean("showBatteryText", true);
				
				int color = prefs.getInt("batteryTextColorChoice", 0xffffffff);
				int alpha = prefs.getInt("batteryTextTransparencyChoice", 80);
				float a = ((float)alpha) / 100.0f;
				mBattery.setColor((int)((a * 255) + 0.5f), Color.red(color), Color.green(color), Color.blue(color));
				
				mBattery.size = prefs.getInt("batteryTextSizeChoice", 40);
				
				int pos = prefs.getInt("batteryTextPositionChoice", 30);
				mBattery.yPercentage = ((float)pos) / 100.0f;
				
				value = prefs.getString("batteryFont", "");
				mBattery.font = LoadTypeface(value);
				
				mBattery.setAlign(prefs.getString("batteryTextAlign", "center"));
			}
			
			mShowDate = prefs.getBoolean("showDate", true);
			
			if(mShowDate) {
				int color = prefs.getInt("dateTextColorChoice", 0xffffffff);
				int alpha = prefs.getInt("dateTextTransparencyChoice", 80);
				float a = ((float)alpha) / 100.0f;
				mDate.setColor((int)((a * 255) + 0.5f), Color.red(color), Color.green(color), Color.blue(color));
				
				mDate.size = prefs.getInt("dateTextSizeChoice", 40);
				
				int pos = prefs.getInt("dateTextPositionChoice", 40);
				mDate.yPercentage = ((float)pos) / 100.0f;
				
				value = prefs.getString("dateFont", "");
				mDate.font = LoadTypeface(value);
				
				mDate.setAlign(prefs.getString("dateTextAlign", "center"));
				
				mDate.format = prefs.getString("dateFormat", "mm/dd/yyyy");
			}	
			
			updateDateAndTime();
			drawFrame();
        }
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}
		
		@Override
		public void onDestroy() {
			mPrefs.unregisterOnSharedPreferenceChangeListener(this);
			super.onDestroy();
			mHandler.removeCallbacks(mExternalStorageRunner);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if(visible) {
				drawFrame();
			}
		}
		
		@Override
		public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
			super.onSurfaceCreated(surfaceHolder);
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
			super.onSurfaceChanged(surfaceHolder, format, width, height);
			mCenterX = width / 2.0f;
			mCenterY = height / 2.0f;
			drawFrame();
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
			super.onSurfaceDestroyed(surfaceHolder);
			mVisible = false;
		}
		
		@Override
		public void onOffsetsChanged(float offsetX, float offsetY, float stepX, float stepY, int pixelsX, int pixelsY) {
			drawFrame();
		}
		
		Typeface LoadTypeface(String path) {
			Typeface tf;
			
			if(path.length() > 0) {
				try{
					tf = Typeface.createFromAsset(getAssets(), "fonts/" + path);
				}
				catch(Exception e) {
					tf = Typeface.DEFAULT;
				}
			}
			else
				tf = Typeface.DEFAULT;
			
			return tf;
		}
		
		private String mStoredPath = null;
		void setBackgroundImage() {
			mHandler.removeCallbacks(mExternalStorageRunner);
			
			if(externalStorageAvailabe() == true) {
				if(loadBackgroundImage(mStoredPath)) {
					mStoredPath = null;
				}
			}
			
			if(mStoredPath != null) {
				mHandler.postDelayed(mExternalStorageRunner, 100);
			}
		}
		
		void setBackgroundImage(String path) {
			boolean startRunner = false;
			
			if(externalStorageAvailabe() == false) {
				startRunner = true;
			}
			else {
				if(loadBackgroundImage(path) == false) {
					
				}
			}
			
			if(startRunner) {
				mStoredPath = path;
				mHandler.postDelayed(mExternalStorageRunner, 100);
			}
		}
		
		private String mCurrentBackground = null;
		boolean loadBackgroundImage(String path) {
			if(mCurrentBackground != null && mCurrentBackground == path && mBackground != null)
				return true;
			
			if(mBackground != null) {
				mBackground = null;
			}

			try {
				mBackground = BitmapFactory.decodeFile(path);
				mCurrentBackground = path;
			}
			finally {
			}
			
			return (mBackground != null);
		}
		
		boolean externalStorageAvailabe() {
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// Write and read
			    return true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // Only read
			    return true;
			} 

			return false;
		}
		
		void drawFrame() {
			final SurfaceHolder surfaceHolder = getSurfaceHolder();
			
			if(!mVisible)
				return;
			
			Canvas canvas = null;
			
			try {
				canvas = surfaceHolder.lockCanvas();
				if(canvas != null) {
					draw(canvas);
				}
			}
			finally {
				if(canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
					canvas = null;
				}
			}
		}
		
		int mStartX = 0;
		int mStartY = 0;
		int mEndX = 0;
		int mEndY = 0;
		int mImageWidth = 0;
		int mImageHeight = 0;
		
		void draw(Canvas canvas) {
			canvas.save();
			
			canvas.drawColor(0xff000000, PorterDuff.Mode.CLEAR);
			if(mBackground != null) {
				calculateImageBounds(canvas);
				Rect src = new Rect(mStartX, mStartY, mEndX, mEndY);
				Rect dst = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
				
				canvas.drawBitmap(mBackground, src, dst, mPaint);
			}
			else {
				canvas.drawColor(mBackgroundColor);
			}
			
			canvas.translate(mCenterX, mCenterY);
			if(mShowBattery)
				drawText(canvas, mBattery);
			if(mShowDate)
				drawText(canvas, mDate);
			if(mShowTime)
				drawText(canvas, mTime);
			canvas.restore();
		}
		
		void calculateImageBounds(Canvas canvas) {
			if(mImageWidth != mBackground.getWidth() && mImageHeight != mBackground.getHeight()) {
				int imgHeight = mBackground.getHeight();
				int imgWidth = mBackground.getWidth();
				int canvasHeight = canvas.getHeight();
				int canvasWidth = canvas.getWidth();
				float ratioWH = ((float)imgWidth) / ((float)imgHeight);
				
				mImageWidth = imgWidth;
				mImageHeight = imgHeight;
				
				int newImgHeight = 0;
				int newImgWidth = 0;
				float oldToNewRatio = 1.0f;
				
				if(imgWidth > imgHeight) {
					newImgHeight = canvasHeight;
					newImgWidth = (int)((newImgHeight * ratioWH) + 0.5f);
					
					oldToNewRatio = (float)imgHeight / (float)newImgHeight;
				}
				else {
					newImgWidth = canvasWidth;
					newImgHeight = (int)((newImgWidth / ratioWH) + 0.5f);
					
					oldToNewRatio = (float)imgWidth / (float)newImgWidth;
				}
				
				mStartX = (newImgWidth - canvasWidth) / 2;
				mEndX = mStartX + canvasWidth;
				
				mStartY = (newImgHeight - canvasHeight) / 2;
				mEndY = mStartY + canvasHeight;
				
				mStartX = (int)(mStartX * oldToNewRatio + 0.5f);
				mStartY = (int)(mStartY * oldToNewRatio + 0.5f);
				mEndX = (int)(mEndX * oldToNewRatio + 0.5f);
				mEndY = (int)(mEndY * oldToNewRatio + 0.5f);
			}			
			
		}
		
		void drawText(Canvas canvas, TextDescription text) {
			mPaint.setTypeface(text.font);
			mPaint.setTextSize(text.size);
			mPaint.setAntiAlias(text.antiAlias);
			mPaint.setColor(text.color);
			mPaint.setTextAlign(text.align);
			mPaint.setTextScaleX(text.scaleX);
			canvas.drawText(text.text, text.getX(), text.getY(), mPaint);
		}
		
		void updateDateAndTime() {
			Calendar cal = Calendar.getInstance();
			
			int hour = 0;
			int ampm = cal.get(Calendar.AM_PM);
			
			if(m24hFormat == true) {
				hour = cal.get(Calendar.HOUR_OF_DAY);
			}
			else {
				hour = cal.get(Calendar.HOUR);
			}
			
			int minute = cal.get(Calendar.MINUTE);
			
			mTime.text = buildTimeString(hour, minute, ampm);
			
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			
			if(mDate.format.length() > 0) {
				if(mDate.format.equals("mm/dd/yyyy"))
					mDate.text = month + "/" + getPadded(day) + "/" + year;
				else if(mDate.format.equals("yyyy-mm-dd"))
					mDate.text = year + "-" + getPadded(month) + "-" + getPadded(day);
				else
					mDate.text = getPadded(day) + "/" + getPadded(month) + "/" + year;
			}
			else
				mDate.text = month + "/" + getPadded(day) + "/" + year;
			
			cal = null;
		}
		
		String buildTimeString(int hour, int minute, int ampm) {
			String str = getPadded(hour) + ":" + getPadded(minute);
			
			if(m24hFormat == false) {
				String ampmStr = "PM";
				if(ampm == Calendar.AM)
					ampmStr = "AM";
				
				str += " " + ampmStr;
			} 
			
			return str;
		}
		
		void updateBatteryStatus(Intent intent) {
			int level = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int plugged = intent.getIntExtra("plugged", -1);
			int batteryStatus = 0;
			if(level >= 0 && scale >= 0) {
				batteryStatus = (level * 100) / scale;
			}
			if(plugged == 0) {
				mBattery.text = getString(R.string.batteryText) + " ";
			}
			else {
				mBattery.text = getString(R.string.chargeText) + " ";
			}
			
			if(mWriteBatteryText == false)
				mBattery.text = "";
			
			mBattery.text += batteryStatus + "%";
		}
		
		String getPadded(int value) {
			if(value < 10)
				return "0" + value;
			
			return "" + value;
		}
	}
	
	public class VerticalAlign {
		public static final int TOP = 1;
		public static final int MIDDLE = 2;
		public static final int BOTTOM = 3;
	}
	
	public class TextDescription {
		
		public Typeface font = Typeface.DEFAULT;
		public String text = "";
		public int size = 50;
		public float scaleX = 1.0f;
		public boolean antiAlias = true; 
		public int color = Color.argb(185, 255, 255, 255);
		public Align align = Align.CENTER;
		public float yPercentage = 0.5f;
		private int width = 0;
		private int height = 0;
		private int halfWidth = 0;
		private int halfHeight = 0;
		private String format = "";
		
		TextDescription() {
			DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
			height = metrics.heightPixels;
			width = metrics.widthPixels;
			halfHeight = height / 2;
			halfWidth = width / 2;
			metrics = null;
		}
		
		public void setColor(int alpha, int red, int green, int blue) {
			color = Color.argb(alpha, red, green, blue);
		}
		
		public void setAlign(String value) {
			if(value.toLowerCase().compareTo("left") == 0) {
				align = Align.LEFT;
			}
			else if(value.toLowerCase().compareTo("right") == 0) {
				align = Align.RIGHT;
			}
			else {
				align = Align.CENTER;
			}
		}
		
		public float getX() {
			float x = 0.0f;
			
			if(align == Align.LEFT) {
				x = -halfWidth + 5;
			}
			else if(align == Align.RIGHT) {
				x = halfWidth - 5;
			}

			return x;
		}
		
		public float getY() {
			return yPercentage * height - halfHeight;
		}
	}
}