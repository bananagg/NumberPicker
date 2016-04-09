package com.gty.picker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author gty 434758632@qq.com:
 * @version 创建时间：2016年4月6日 下午7:48:15
 * 类说明
 */
public class PickerView extends View{

	/*
    *注意如果觉得圆过小或过大，到drawRing(Canvas canvas,boolean isFocus)方法中设置半径
    **/

	public static final String TAG = "PickerView";
	/**
	 * text之间间距和minTextSize之比
	 */
	public static final float MARGIN_ALPHA = 2.8f;
	/**
	 * 自动回滚到中间的速度
	 */
	public static final float SPEED = 2;

	private List<String> mDataList;
	/**
	 * 选中的位置，这个位置是mDataList的中心位置，一直不变
	 */
	private int mCurrentSelected;
	private Paint mPaint,mPaint1;

	private float mMaxTextSize = 80;
	private float mMinTextSize = 40;

	private float mMaxTextAlpha = 255;
	private float mMinTextAlpha = 120;

	private int mColorText = 0xffffff;

	private int mViewHeight;
	private int mViewWidth;

	private Canvas mCanvas;

	private boolean focus =false;

	private float mLastDownY;
	/**
	 * 滑动的距离
	 */
	private float mMoveLen = 0;
	private boolean isInit = false;
	private onSelectListener mSelectListener;
	private Timer timer;
	private MyTimerTask mTask;

	public Canvas getCanvas() {
		return mCanvas;
	}

	Handler updateHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			if (Math.abs(mMoveLen) < SPEED)
			{
				mMoveLen = 0;
				if (mTask != null)
				{
					mTask.cancel();
					mTask = null;
					performSelect();
				}
			} else
				// 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
				mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;
			invalidate();
		}

	};

	public PickerView(Context context)
	{
		super(context);
		init();
	}

	public PickerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public void setOnSelectListener(onSelectListener listener)
	{
		mSelectListener = listener;
	}

	private void performSelect()
	{
		if (mSelectListener != null)
			mSelectListener.onSelect(mDataList.get(mCurrentSelected));
	}
	//获取被选中的数据
	public String getSelData()
	{
		// TODO Auto-generated method stub
		return mDataList.get(mCurrentSelected);
	}

	public void setData(List<String> datas)
	{
		mDataList = datas;
		mCurrentSelected = datas.size() / 2;
		invalidate();
	}

	public void setSelected(int selected)
	{
		mCurrentSelected = selected;
		//distance用来判断是向上移动还是向下移动
		int distance = mDataList.size() / 2 - mCurrentSelected;		
		//不管向上还是向下移动  均保持被选中的item上下两边的item数量相等
		if (distance < 0)
			for (int i = 0; i < -distance; i++)
			{
				moveHeadToTail();
				mCurrentSelected--;
			}
		else if (distance > 0)
			for (int i = 0; i < distance; i++)
			{
				moveTailToHead();
				mCurrentSelected++;
			}
		invalidate();
	}

	public void setSelected(String mSelectItem)
	{
		for (int i = 0; i < mDataList.size(); i++)
			if (mDataList.get(i).equals(mSelectItem))
			{
				setSelected(i);
				break;
			}
	}

	private void moveHeadToTail()
	{
		String head = mDataList.get(0);
		mDataList.remove(0);
		mDataList.add(head);
	}

	private void moveTailToHead()
	{
		String tail = mDataList.get(mDataList.size() - 1);
		mDataList.remove(mDataList.size() - 1);
		mDataList.add(0, tail);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewHeight = getMeasuredHeight();
		mViewWidth = getMeasuredWidth();
		// 按照View的高度计算字体大小
		mMaxTextSize = mViewHeight / 4.0f;
		mMinTextSize = mMaxTextSize / 2f;
		isInit = true;
		//		清屏刷新
		invalidate();
	}

	private void init()
	{
		timer = new Timer();
		mDataList = new ArrayList<String>();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Style.FILL);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setColor(mColorText);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		mCanvas =canvas;
		super.onDraw(canvas);
		// 根据index绘制view
		if (isInit)
			drawData(canvas,focus);
	}

	public void setIsFocusable(boolean f){
		this.focus = f;
		this.invalidate();
	}

	private void drawData(Canvas canvas,boolean isFocus)
	{
//		focus = isFocus;
		// 先绘制选中的text再往上往下绘制其余的text
		drawRing(canvas, isFocus);
		float scale = parabola(mViewHeight / 4.0f, mMoveLen);
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mPaint.setTextSize(size);
		mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
		// text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
		float x = (float) (mViewWidth / 2.0);
		float y = (float) (mViewHeight / 2.0 + mMoveLen);
		FontMetricsInt fmi = mPaint.getFontMetricsInt();
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));

		canvas.drawText(mDataList.get(mCurrentSelected), x, baseline, mPaint);

		//		if(isFocus){
		// 绘制上方data
		for (int i = 1; (mCurrentSelected - i) >= 0; i++)
		{
			drawOtherText(canvas, i, -1);
		}
		// 绘制下方data
		for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++)
		{
			drawOtherText(canvas, i, 1);
		}
		//		}	
	}

	public void drawRing(Canvas canvas,boolean isFocus){
		mPaint1 = new Paint();
		mPaint1.setAntiAlias(true);
		mPaint1.setStyle(Style.STROKE);         //设置风格为空心圆
		int centerx = mViewWidth/2;
		int centery = mViewHeight/2;
		int innerCircle = dip2px(getContext(),centerx/3); //设置内圆半径
		int ringWidth = dip2px(getContext(), 2);
		if(isFocus){
			mPaint1.setARGB(255, 45 ,130, 236);  
			mPaint1.setStrokeWidth(1);
			canvas.drawCircle(centerx, centery, innerCircle, mPaint1);

			mPaint1.setARGB(255, 45 ,130, 236);  
			this.mPaint1.setStrokeWidth(ringWidth);  
			canvas.drawCircle(centerx,centery, innerCircle+1+ringWidth/2, mPaint1);

			//绘制外圆  
			mPaint1.setARGB(255, 45 ,130, 236);   
			mPaint1.setStrokeWidth(1);  
			canvas.drawCircle(centerx,centery, innerCircle+ringWidth, mPaint1);  
		}else{
			mPaint1.setARGB(255, 255 ,255, 255);  
			mPaint1.setStrokeWidth(1);
			canvas.drawCircle(centerx, centery, innerCircle, mPaint1);

			mPaint1.setARGB(255, 255 ,255, 255);  
			this.mPaint1.setStrokeWidth(ringWidth);  
			canvas.drawCircle(centerx,centery, innerCircle+1+ringWidth/2, mPaint1);

			//绘制外圆  
			mPaint1.setARGB(255, 255 ,255, 255);   
			mPaint1.setStrokeWidth(1);  
			canvas.drawCircle(centerx,centery, innerCircle+ringWidth, mPaint1);  
		}
	}

	public static int dip2px(Context context, float dpValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (dpValue * scale + 0.5f);  
	} 

	/**
	 * @param canvas
	 * @param position
	 *            距离mCurrentSelected的差值
	 * @param type
	 *            1表示向下绘制，-1表示向上绘制
	 */
	private void drawOtherText(Canvas canvas, int position, int type)
	{
		float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type
				* mMoveLen);
		float scale = parabola(mViewHeight / 4.0f, d);
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mPaint.setTextSize(size);
		mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
		float y = (float) (mViewHeight / 2.0 + type * d);
		FontMetricsInt fmi = mPaint.getFontMetricsInt();
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
		canvas.drawText(mDataList.get(mCurrentSelected + type * position),
				(float) (mViewWidth / 2.0), baseline, mPaint);
	}

	/**
	 * 抛物线
	 * 
	 * @param zero
	 *            零点坐标
	 * @param x
	 *            偏移量
	 * @return scale
	 */
	private float parabola(float zero, float x)
	{
		float f = (float) (1 - Math.pow(x / zero, 2));
		return f < 0 ? 0 : f;
	}

	//按键监听
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_DPAD_UP://上键
			//			AutoChangeTimer.getInstance().addEventCount();		//？？�?
			mMoveLen = -100;				//???	mMoveLen是个�?么�??
			mCurrentSelected--;				//???	mCurrentSelected是个�?么�??
			setSelected(mCurrentSelected);
			doUp(null);
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN://下键
			//			AutoChangeTimer.getInstance().addEventCount();
			mMoveLen = 100;
			mCurrentSelected++;
			setSelected(mCurrentSelected);
			doUp(null);
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getActionMasked())
		{
		case MotionEvent.ACTION_DOWN:
			doDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			doMove(event);
			break;
		case MotionEvent.ACTION_UP:
			doUp(event);
			break;
		}
		return true;
	}

	private void doDown(MotionEvent event)
	{
		if (mTask != null)
		{
			mTask.cancel();
			mTask = null;
		}
		mLastDownY = event.getY();
	}

	private void doMove(MotionEvent event)
	{

		mMoveLen += (event.getY() - mLastDownY);

		if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2)
		{
			// 往下滑超过离开距离
			moveTailToHead();
			mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
		} else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2)
		{
			// 往上滑超过离开距离
			moveHeadToTail();
			mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
		}

		mLastDownY = event.getY();
		invalidate();
	}

	private void doUp(MotionEvent event)
	{
		// 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
		if (Math.abs(mMoveLen) < 0.0001)
		{
			mMoveLen = 0;
			return;
		}
		if (mTask != null)
		{
			mTask.cancel();
			mTask = null;
		}
		mTask = new MyTimerTask(updateHandler);
		timer.schedule(mTask, 0, 10);
	}

	class MyTimerTask extends TimerTask
	{
		Handler handler;

		public MyTimerTask(Handler handler)
		{
			this.handler = handler;
		}

		@Override
		public void run()
		{
			handler.sendMessage(handler.obtainMessage());
		}

	}

	public interface onSelectListener
	{
		void onSelect(String text);
	}

}
