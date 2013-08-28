package com.andreashedin.infowallpaper.lite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;

import com.andreashedin.general.ColorPickerDialog;
import com.andreashedin.general.SliderDialog;
import com.andreashedin.infowallpaper.lite.LiveInfoWallpaper_lite;

public class LiveInfoSettings_lite extends PreferenceActivity
implements 
SharedPreferences.OnSharedPreferenceChangeListener,
Preference.OnPreferenceChangeListener,
Preference.OnPreferenceClickListener,
ColorPickerDialog.OnColorChangedListener,
SliderDialog.OnValueChangedListener {

	private final int FLAG_BACKGROUNDCOLOR = 0;
	private final int FLAG_BATTERYTEXTCOLOR = 1;
	private final int FLAG_DATETEXTCOLOR = 2;
	private final int FLAG_TIMETEXTCOLOR = 3;
	private final int FLAG_BATTERYTEXTPOSITION = 4;
	private final int FLAG_DATETEXTPOSITION = 5;
	private final int FLAG_TIMETEXTPOSITION = 6;
	private final int FLAG_BATTERYTEXTTRANSPARENCY = 7;
	private final int FLAG_DATETEXTTRANSPARENCY = 8;
	private final int FLAG_TIMETEXTTRANSPARENCY= 9;
	private final int FLAG_BATTERYTEXTSIZE= 10;
	private final int FLAG_DATETEXTSIZE = 11;
	private final int FLAG_TIMETEXTSIZE= 12;
	
	@Override
	protected void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    getPreferenceManager().setSharedPreferencesName(
	            LiveInfoWallpaper_lite.SHARED_PREFERENCES_NAME);
	    addPreferencesFromResource(LiveInfoWallpaper_lite.SETTINGS_FILE);
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
	            this);
	    
	    Preference pref = getPreferenceManager().findPreference("background");
	    if(pref != null) // Background
	    	pref.setOnPreferenceChangeListener(this);
	    
	    pref = getPreferenceManager().findPreference("dateFormat");
	    if(pref != null)
	    	pref.setOnPreferenceChangeListener(this);
	    
	    // Text colors
	    pref = getPreferenceManager().findPreference("batteryTextColor");
	    if(pref != null)
	    	pref.setOnPreferenceChangeListener(this);
	    pref = getPreferenceManager().findPreference("dateTextColor");
	    if(pref != null)
	    	pref.setOnPreferenceChangeListener(this);
	    pref = getPreferenceManager().findPreference("timeTextColor");
	    if(pref != null)
	    	pref.setOnPreferenceChangeListener(this);
	    
	   // Text positions
	   pref = getPreferenceManager().findPreference("batteryTextPosition");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("dateTextPosition");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("timeTextPosition");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }   
	   // Text transparency
	   pref = getPreferenceManager().findPreference("batteryTextTransparency");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("dateTextTransparency");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("timeTextTransparency");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   
	// Text size
	   pref = getPreferenceManager().findPreference("batteryTextSize");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("dateTextSize");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("timeTextSize");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("toWebsite");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	   pref = getPreferenceManager().findPreference("toMarket");
	   if(pref != null) {
		   pref.setOnPreferenceClickListener(this);
	   }
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	}
	
	@Override
	protected void onDestroy() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    super.onDestroy();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		int flag = -1;
		boolean showColorPicker = true;
		
		if(preference.getKey().compareTo("background") == 0)
			flag = FLAG_BACKGROUNDCOLOR;
		else if(preference.getKey().compareTo("batteryTextColor") == 0)
			flag = FLAG_BATTERYTEXTCOLOR;
		else if(preference.getKey().compareTo("dateTextColor") == 0)
			flag = FLAG_DATETEXTCOLOR;
		else if(preference.getKey().compareTo("timeTextColor") == 0)
			flag = FLAG_TIMETEXTCOLOR;
		
		String value = newValue.toString();
		
		if(showColorPicker == true)
			handleColorAndBackground(preference, flag, value);
		
		return true;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		int flag = -1;
		int currVal = 400;
		int max = 100;
		int multiplier = 100;
		String label = "";
		SharedPreferences sp = preference.getSharedPreferences();
		int screenHeight = preference.getContext().getResources().getDisplayMetrics().heightPixels;
		
		if(preference.getKey().compareTo("toWebsite") == 0) {
			gotoWebsite(preference.getContext());
			return true;
		}
		else if(preference.getKey().compareTo("toMarket") == 0) {
			gotoMarket(preference.getContext());
			return true;
		}
		else if(preference.getKey().compareTo("batteryTextPosition") == 0) {
			flag = FLAG_BATTERYTEXTPOSITION;
			currVal = sp.getInt("batteryTextPositionChoice", 50);
			label = "Text position: ";
			multiplier = screenHeight;
		}
		else if(preference.getKey().compareTo("dateTextPosition") == 0) {
			flag = FLAG_DATETEXTPOSITION;
			currVal = sp.getInt("dateTextPositionChoice", 50);
			label = "Text position: ";
			multiplier = screenHeight;
		}
		else if(preference.getKey().compareTo("timeTextPosition") == 0) {
			flag = FLAG_TIMETEXTPOSITION;
			currVal = sp.getInt("timeTextPositionChoice", 50);
			label = "Text position: ";
			multiplier = screenHeight;
		}
		else if(preference.getKey().compareTo("batteryTextTransparency") == 0) {
			flag = FLAG_BATTERYTEXTTRANSPARENCY;
			currVal = sp.getInt("batteryTextTransparencyChoice", 80);
			label = "Text transparency: ";
		}
		else if(preference.getKey().compareTo("dateTextTransparency") == 0) {
			flag = FLAG_DATETEXTTRANSPARENCY;
			currVal = sp.getInt("dateTextTransparencyChoice", 80);
			label = "Text transparency: ";
		}
		else if(preference.getKey().compareTo("timeTextTransparency") == 0) {
			flag = FLAG_TIMETEXTTRANSPARENCY;
			currVal = sp.getInt("timeTextTransparencyChoice", 80);
			label = "Text transparency: ";
		}
		else if(preference.getKey().compareTo("batteryTextSize") == 0) {
			flag = FLAG_BATTERYTEXTSIZE;
			currVal = sp.getInt("batteryTextSizeChoice", 40);
			label = "Text size: ";
			max = 200;
			multiplier = 200;
		}
		else if(preference.getKey().compareTo("dateTextSize") == 0) {
			flag = FLAG_DATETEXTSIZE;
			currVal = sp.getInt("dateTextSizeChoice", 40);
			label = "Text size: ";
			max = 200;
			multiplier = 200;
		}
		else if(preference.getKey().compareTo("timeTextSize") == 0) {
			flag = FLAG_TIMETEXTSIZE;
			currVal = sp.getInt("timeTextSizeChoice", 40);
			label = "Text size: ";
			max = 200;
			multiplier = 200;
		}
		
		sp = null;
		openSliderDialog(preference, flag, currVal, max, multiplier, label);
		
		return true;
	}
	
	private void gotoWebsite(Context context) {
		Intent intent = new Intent();
		String url = "http://www.andreashedin.com";
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void gotoMarket(Context context) {
		Intent intent = new Intent();
		String url = "http://market.android.com/details?id=com.andreashedin.infowallpaper";
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void openSliderDialog(Preference preference, int flag, int currVal, int max, int multiplier, String label) {
		new SliderDialog(preference.getContext(), this, 0, max, multiplier, currVal, flag, label).show();
	}
	
	private void handleColorAndBackground(Preference preference, int flag, String value) {
		if(value.compareTo("color") == 0) {
			new ColorPickerDialog(preference.getContext(), this, ColorPickerDialog.MODE_COLOR, Color.argb(255, 0, 0, 0), flag).show();
		}
		else if(value.compareTo("black") == 0) {
			colorChanged(0xff000000, flag);
		}
		else if(value.compareTo("white") == 0) {
			colorChanged(0xffffffff, flag);
		}
		else if(value.compareTo("grayscale") == 0) {
			new ColorPickerDialog(preference.getContext(), this, ColorPickerDialog.MODE_GRAY_SCALE, Color.argb(255, 0, 0, 0), flag).show();
		}
		else if(value.compareTo("image") == 0) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select image"), 1);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	        if (requestCode == 1) {
	            Uri imageUri = data.getData();
	            String imagePath = getPath(imageUri);
	
	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
	            editor.putString("imagePath", imagePath);
	            editor.commit();
	            editor = null;
	        }
	    }
	}
	
	public String getPath(Uri uri) {
		String path = "";
		
		if(uri.getScheme().equals("content")) {
			try {
				String[] projection = { MediaStore.Images.Media.DATA };
			    Cursor cursor = managedQuery(uri, projection, null, null, null);
			    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			    cursor.moveToFirst();
			    path = cursor.getString(column_index);
			}
			catch(Exception ex) {
				Log.i("mytag", "getPath failed");
			}
        }
        else if(uri.getScheme().equals("file")) {
        	path = uri.toString().replace("file://", "");
        }
		
		return path;
	}
	
	@Override
	public void colorChanged(int color, int flag) {
		String key = "";
		
		if(flag == FLAG_BACKGROUNDCOLOR) {
			key = "backgroundColor";
		}
		else if(flag == FLAG_BATTERYTEXTCOLOR)
			key = "batteryTextColorChoice";
		else if(flag == FLAG_DATETEXTCOLOR)
			key = "dateTextColorChoice";
		else if(flag == FLAG_TIMETEXTCOLOR)
			key = "timeTextColorChoice";
		
		if(key.length() > 0) {
			Editor editor = getPreferenceManager().getSharedPreferences().edit();
	        editor.putInt(key, color);

	        if(flag == FLAG_BACKGROUNDCOLOR) 
	        	editor.putString("imagePath", "");
	        
	        editor.commit();
	        editor = null;
		}
	}
	
	@Override
	public void valueChanged(int value, int flag) {
		String key = "";
		
		if(flag == FLAG_BATTERYTEXTPOSITION)
			key = "batteryTextPositionChoice";
		else if(flag == FLAG_DATETEXTPOSITION) 
			key = "dateTextPositionChoice";
		else if(flag == FLAG_TIMETEXTPOSITION)
			key = "timeTextPositionChoice";
		else if(flag == FLAG_BATTERYTEXTTRANSPARENCY)
			key = "batteryTextTransparencyChoice";
		else if(flag == FLAG_DATETEXTTRANSPARENCY) 
			key = "dateTextTransparencyChoice";
		else if(flag == FLAG_TIMETEXTTRANSPARENCY)
			key = "timeTextTransparencyChoice";
		else if(flag == FLAG_BATTERYTEXTSIZE)
			key = "batteryTextSizeChoice";
		else if(flag == FLAG_DATETEXTSIZE) 
			key = "dateTextSizeChoice";
		else if(flag == FLAG_TIMETEXTSIZE)
			key = "timeTextSizeChoice";
		
		if(key.length() > 0) {
			Editor editor = getPreferenceManager().getSharedPreferences().edit();
			editor.putInt(key, value);
			editor.commit();
			editor = null;
		}
	}
}
