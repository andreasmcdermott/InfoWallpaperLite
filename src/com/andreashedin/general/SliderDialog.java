package com.andreashedin.general;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class SliderDialog extends Dialog {

	public interface OnValueChangedListener {
		void valueChanged(int value, int flag);
	}
	
	private static class SliderView extends View 
		implements View.OnClickListener {

		private OnValueChangedListener mListener;
        private int mFlag;
        private int mMax;
        private int mMulti;
        private String mLabel;
        private Paint mBasePaint;
        private Paint mTopPaint;
        private Paint mTextPaint;
        private int mCenterX;
        private int mCenterY;
        private int mWinHeight;
        private int mWinWidth;
        private int mSliderHeight;
        private int mSliderWidth;
        private float mCurrentValue;
        
		public SliderView(Context context, OnValueChangedListener listener, int min, int max, int multiplier, int startValue, int flag, String label) {
			super(context);
			
			mMulti = multiplier;
			mLabel = label;
			mFlag = flag;
			mMax = max;
			if(startValue < min)
				startValue = min;
			if(startValue > max)
				startValue = max;
			mListener = listener;
			mCurrentValue = (float)startValue / (float)max;
			
			mWinWidth = context.getResources().getDisplayMetrics().widthPixels;
			mWinHeight = context.getResources().getDisplayMetrics().heightPixels;
			mSliderWidth = (int)(mWinWidth * 0.12f);
			
			mBasePaint = new Paint();
			mBasePaint.setColor(0xffffffff);
			mBasePaint.setStrokeWidth(mSliderWidth);
			mBasePaint.setStyle(Style.FILL);
			
			mTopPaint = new Paint();
			mTopPaint.setColor(0xfff78b18);
			mTopPaint.setStrokeWidth(mSliderWidth);
			mTopPaint.setStyle(Style.FILL);
			
			mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mTextPaint.setColor(0xffffffff);
			mTextPaint.setTextSize(20.0f);
			mTextPaint.setTypeface(Typeface.DEFAULT);
			
			mSliderHeight = (int)(mWinHeight * 0.75f);
			mCenterX = mWinWidth / 2;
			mCenterY = mWinHeight / 2;
		}
		
		@Override 
        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.translate(mCenterX, mCenterY);
            
            int value = (int)((mCurrentValue * mSliderHeight) + 0.5f);
            if(value < (mSliderWidth / 2)) {
            	value = mSliderWidth / 2;
            }
            else if(value > mSliderHeight - mSliderWidth / 2) {
            	value = mSliderHeight - mSliderWidth / 2;
            }
            
            //canvas.drawText("Screen top", -mCenterX + 10, -mCenterY + 25, mTextPaint);
            //canvas.drawText("Screen bottom", -mCenterX + 10, -mCenterY + mSliderHeight - 5, mTextPaint);
            //canvas.drawText("Screen height: " + mWinHeight, -mCenterX + 10, -mCenterY + 50, mTextPaint);
            canvas.drawText(mLabel + (int)((mCurrentValue * (float)mMulti) + 0.5f), -mCenterX + mSliderWidth * 0.5f, -(mCenterY * 0.5f), mTextPaint);
            
            canvas.drawLine(mCenterX - mSliderWidth * 1.5f, -mCenterY, mCenterX - mSliderWidth * 1.5f, (-mCenterY + mSliderHeight), mBasePaint);
            canvas.drawLine(mCenterX - (mSliderWidth * 2.25f), -mCenterY + value, mCenterX - mSliderWidth * 0.75f, -mCenterY + value, mTopPaint);
            
            canvas.restore();
        }
		
		private boolean mAdjust = false;
		
		@Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - mCenterX;
            float y = event.getY() - mCenterY;
            
            float barLeft = mCenterX - mSliderWidth * 2.0f;
            float barRight = mCenterX - mSliderWidth * 1.0f;
            float barTop = -mCenterY;
            float barBottom = -mCenterY + mSliderHeight;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	if(x < barRight && x > barLeft && y < barBottom && y > barTop) {
                		mAdjust = true;
                	}
                    break;
                case MotionEvent.ACTION_MOVE:
                	if(mAdjust == true) {
                		invalidate();
                	}
                    break;
                case MotionEvent.ACTION_UP:
                	if(mAdjust == true) {
                		mAdjust = false;
	                    invalidate();
                	}
                    break;
            }
            
            if(mAdjust) {
            	mCurrentValue = ((y - barTop) / (float)mSliderHeight);
            	if(mCurrentValue < 0.0f)
            		mCurrentValue = 0.0f;
            	else if(mCurrentValue > 1.0f)
            		mCurrentValue = 1.0f;
            }
            
            return true;
        }
		
		@Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension((int)(mWinWidth), (int)(mWinHeight));
        }

		@Override
		public void onClick(View v) {
			int value = (int)((mCurrentValue * (float)mMax) + 0.5f);
			mListener.valueChanged(value, mFlag);
		}
	}

	private int mFlag;
	private int mMin;
	private int mMax;
	private int mMulti;
	private int mStartValue;
	private String mLabel;
	private OnValueChangedListener mListener;
	
	public SliderDialog(Context context, OnValueChangedListener listener, int min, int max, int multiplier, int startValue, int flag, String label) {
		super(context);
		
		mMulti = multiplier;
		mLabel = label;
		mListener = listener;
		mMin = min;
		mMax = max;
		mFlag = flag;
		mStartValue = startValue;
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnValueChangedListener l = new OnValueChangedListener() {
            public void valueChanged(int value, int flag) {
                mListener.valueChanged(value, flag);
                dismiss();
            }
        };

        RelativeLayout groupView = new RelativeLayout(getContext());
        SliderView sv = new SliderView(getContext(), l, mMin, mMax, mMulti, mStartValue, mFlag, mLabel);
        groupView.addView(sv);
        Button b = new Button(getContext());
        b.setText("OK");
        b.setOnClickListener(sv);
        b.setGravity(Gravity.CENTER);
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.6f), 
        		RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        b.setLayoutParams(lp);
        groupView.addView(b);
        setContentView(groupView);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Press OK when done");
    }
}
