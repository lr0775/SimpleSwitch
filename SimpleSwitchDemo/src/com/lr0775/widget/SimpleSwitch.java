package com.lr0775.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.CompoundButton;

import com.lr0775.simpleswitch.R;

public class SimpleSwitch extends CompoundButton implements AnimationListener {

	private static final float THUMB_ANIMATION_VELOCITY = 0.8f;

	private static final int SHAPE_RECT = 1;
	private static final int SHAPE_CIRCLE = 2;

	private static final int SIDE_LEFT = 1;
	private static final int SIDE_RIGHT = 2;

	private static final int PADDING_DEFAULT = 6;

	// 触发移动事件的最短距离
	private int mTouchSlop;

	private float mTouchDownX;

	private boolean mHitThumb = false;

	private Animation mThumbAnimation;
	private int mAnimDistance;
	private boolean mAnimating = false;
	private int mSide;

	private Paint mPaint;
	private Rect mTrackRect;
	private Rect mThumbRect;

	private RectF mTrackRectF;
	private RectF mThumbRectF;

	private int mAlpha;

	private int mCheckedColor;
	private int mShape;

	private int mMaxThumbLeftMargin;
	private int mMinThumbLeftMargin;
	private int mThumbLeftMargin;
	private int mTempThumbLeftMargin;
	private int mPadding;

	private int mThumbWidth;
	private int mThumbHeight;

	private boolean mThumbMoving = false;

	private int mOldDistance = 0;

	public SimpleSwitch(Context context) {
		this(context, null);
	}

	public SimpleSwitch(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SimpleSwitch);
		mCheckedColor = a.getColor(R.styleable.SimpleSwitch_checkedColor,
				Color.parseColor("#ff00ee00"));
		boolean checked = a.getBoolean(R.styleable.SimpleSwitch_checked, false);
		mShape = a.getBoolean(R.styleable.SimpleSwitch_isRect, true) ? SHAPE_RECT
				: SHAPE_CIRCLE;
		a.recycle();

		final ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPadding = getPaddingLeft() > PADDING_DEFAULT ? getPaddingLeft()
				: PADDING_DEFAULT;

		mThumbAnimation = new ThumbAnimation();
		mThumbAnimation.setInterpolator(new LinearInterpolator());
		mThumbAnimation.setAnimationListener(this);

		setChecked(checked);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureDimension(280, widthMeasureSpec);
		int height = measureDimension(140, heightMeasureSpec);
		if (mShape == SHAPE_CIRCLE && width < height) {
			width = height * 2;
		}
		setMeasuredDimension(width, height);
		initDrawingVal();
	}

	public int measureDimension(int defaultSize, int measureSpec) {
		int result;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			result = defaultSize; // UNSPECIFIED
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	public void initDrawingVal() {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		mThumbHeight = height - 2 * mPadding;

		mTrackRect = new Rect(0, 0, width, height);
		mThumbRect = new Rect();

		mTrackRectF = new RectF(mTrackRect);
		mThumbRectF = new RectF(mThumbRect);

		if (mShape == SHAPE_RECT) {
			mMaxThumbLeftMargin = width / 2;
			mThumbWidth = getMeasuredWidth() / 2 - mPadding;
		} else {
			mMaxThumbLeftMargin = width - mThumbHeight - mPadding;
			mThumbWidth = mThumbHeight;
		}
		mMinThumbLeftMargin = mPadding;
		Log.w("switch", "mMaxThumbLeftMargin = " + mMaxThumbLeftMargin
				+ "\n mMinThumbLeftMargin = " + mMinThumbLeftMargin);

		if (isChecked()) {
			mThumbLeftMargin = mMaxThumbLeftMargin;
			mAlpha = 255;
		} else {
			mThumbLeftMargin = mPadding;
			mAlpha = 0;
		}
		mTempThumbLeftMargin = mThumbLeftMargin;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mShape == SHAPE_RECT) {
			mPaint.setColor(Color.GRAY);
			canvas.drawRect(mTrackRect, mPaint);
			mPaint.setColor(mCheckedColor);
			mPaint.setAlpha(mAlpha);
			canvas.drawRect(mTrackRect, mPaint);

			mThumbRect.set(mThumbLeftMargin, mPadding, mThumbLeftMargin
					+ mThumbWidth, getMeasuredHeight() - mPadding);
			mPaint.setColor(Color.WHITE);
			canvas.drawRect(mThumbRect, mPaint);
		} else {
			int radius = mTrackRect.height() / 2 - mPadding;
			mPaint.setColor(Color.GRAY);
			canvas.drawRoundRect(mTrackRectF, radius, radius, mPaint);
			mPaint.setColor(mCheckedColor);
			mPaint.setAlpha(mAlpha);
			canvas.drawRoundRect(mTrackRectF, radius, radius, mPaint);
			mThumbRect.set(mThumbLeftMargin, mPadding, mThumbLeftMargin
					+ mThumbHeight, getMeasuredHeight() - mPadding);
			mPaint.setColor(Color.WHITE);
			canvas.drawRoundRect(mThumbRectF, radius, radius, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mAnimating) {
			return true;
		}
		final int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = event.getX();
			final float y = event.getY();
			mTouchDownX = x;
			mHitThumb = hitThumb(x, y);
			mThumbMoving = false;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final float x = event.getX();
			final float thumbScrollOffset = x - mTouchDownX;
			if (mHitThumb && Math.abs(thumbScrollOffset) > mTouchSlop) {
				getParent().requestDisallowInterceptTouchEvent(true);
				float tempX = thumbScrollOffset + mTempThumbLeftMargin;
				setThumbLeftMargin(tempX);
				mThumbMoving = true;
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			getParent().requestDisallowInterceptTouchEvent(false);
			mTempThumbLeftMargin = mThumbLeftMargin;
			animToSide();
			break;
		}
		default:
			break;
		}
		return true;
	}

	private boolean hitThumb(float x, float y) {
		boolean hit = false;
		hit = x > mThumbLeftMargin && x < mThumbLeftMargin + mThumbWidth
				&& y > mPadding && y < getMeasuredHeight() - mPadding;
		return hit;
	}

	private void setThumbLeftMargin(float tempX) {
		if (tempX > mMaxThumbLeftMargin) {
			tempX = mMaxThumbLeftMargin;
		} else if (tempX < mMinThumbLeftMargin) {
			tempX = mMinThumbLeftMargin;
		}
		mThumbLeftMargin = (int) tempX;
		mAlpha = (int) (255 * tempX / mMaxThumbLeftMargin);
		invalidate();
	}

	private void animToSide() {
		if (mThumbMoving) {
			if (mThumbLeftMargin == mMinThumbLeftMargin) {
				setChecked(false);
				return;
			} else if (mThumbLeftMargin == mMaxThumbLeftMargin) {
				setChecked(true);
				return;
			}
		}
		if (mThumbLeftMargin == mMinThumbLeftMargin) {
			mAnimDistance = mMaxThumbLeftMargin - mMinThumbLeftMargin;
			mSide = SIDE_RIGHT;
		} else if (mThumbLeftMargin == mMaxThumbLeftMargin) {
			mAnimDistance = mMaxThumbLeftMargin - mMinThumbLeftMargin;
			mSide = SIDE_LEFT;
		} else if ((mThumbLeftMargin + mThumbWidth / 2) > (getMeasuredWidth() / 2)) {
			mAnimDistance = mMaxThumbLeftMargin - mThumbLeftMargin;
			mSide = SIDE_RIGHT;
		} else {
			mAnimDistance = mThumbLeftMargin - mPadding;
			mSide = SIDE_LEFT;
		}
		long duration = (long) (mAnimDistance / THUMB_ANIMATION_VELOCITY);
		Log.w("switch", "distance = " + mAnimDistance + "\n duration = "
				+ duration);
		mThumbAnimation.setDuration(duration);
		startAnimation(mThumbAnimation);
	}

	private class ThumbAnimation extends Animation {

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			float curren = interpolatedTime * mAnimDistance;
			if ((int) curren <= mOldDistance) {
				return;
			}
			float tempX = 0;
			if (mSide == SIDE_LEFT) {
				tempX = mTempThumbLeftMargin - curren;
			} else {
				tempX = mTempThumbLeftMargin + curren;
			}
			setThumbLeftMargin(tempX);
			Log.w("switch anim", "interpolatedTime = " + interpolatedTime
					+ ",curren distance = " + tempX + ",curren = "
					+ (int) curren + ",mOldDistance = " + mOldDistance);
			mOldDistance = (int) curren;
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		mAnimating = true;
		mOldDistance = 0;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mAnimating = false;
		mTempThumbLeftMargin = mThumbLeftMargin;
		if (mSide == SIDE_LEFT) {
			setChecked(false);
		} else if (mSide == SIDE_RIGHT) {
			setChecked(true);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (checked && mThumbLeftMargin < mMaxThumbLeftMargin) {
			setThumbLeftMargin(mMaxThumbLeftMargin);
		} else if (!checked && mThumbLeftMargin > mMinThumbLeftMargin) {
			setThumbLeftMargin(mMinThumbLeftMargin);
		}
	}

}
