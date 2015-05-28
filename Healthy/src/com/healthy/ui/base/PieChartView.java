package com.healthy.ui.base;

import com.healthy.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;

@SuppressLint("FloatMath")
public class PieChartView extends ImageView implements Runnable {

	private static final int FLING_MIN_VELOCITY = 3500;// 最小快速滑动速度

	private int[] mItemColors;// 条目颜色
	private float[] mItemRatios;// 条目所占的比例
	private String[] mItemNames;// 条目名称
	private float[] mItemPos;// 每个条目的起始位置

	/* 绘制饼图所需的矩形区域 */
	private RectF mPieChartArea = new RectF();

	// 圆心坐标
	private PointF mCenter = new PointF();

	private float mStartAngle = 0;// 绘制饼图的初始角度
	private float mDeltaAngle = 0;// 饼图旋转的角度

	private Paint mPaint;
	private Paint mTextPaint;// 绘制字符所用的paint.

	/* 处理饼图随手势滚动的相关变量 */
	private VelocityTracker mVelocityTracker;// 用于记录用户的滑动速度
	private PointF mPrePoint = new PointF();// 触摸点坐标
	private PointF mCurPoint = new PointF();
	private PointF mOriginalPoint = new PointF();// 手指按下的点坐标
	private float mAcceleration = 0;// 滚动时候的加速度
	private float mSpeed = 0;// 减速滚动时候的瞬时速度
	private float mPreSpeed = 0;// 上一时刻的瞬时速度，如果二者相乘为负数，则停止转动转盘。
	private boolean mFastRotating = false;// 判断当前饼图是否正处于快速旋转状态
	private int mClockWise = 0;// 顺时针方向标志位,为了防止因为用户抖动而导致判断错误，这里并没有使用boolean类型。

	private OnCompleteRotating mListener;

	public PieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		mItemColors = getResources().getIntArray(R.array.pie_chart_colors);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(23.0f);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		/* 获得绘制饼图所需要的矩形区域 */
		mPieChartArea.left = 0;
		mPieChartArea.top = 0;
		mPieChartArea.right = getWidth();
		mPieChartArea.bottom = getHeight();
		mCenter.set(getWidth() / 2, getHeight() / 2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		/*
		 * 为了节省资源，采用移动画布的方式来旋转图像 如果采用固定画布，旋转图像的方法话，会出现卡顿并且掉帧的情况
		 */
		canvas.save();
		// 对于画布而言，旋转角度为正时候画布进行逆时针方向转动。
		canvas.rotate(mDeltaAngle, mCenter.x, mCenter.y);
		/* 绘制每个条目在饼图中所对应的区域 */
		for (int i = 0; i < mItemNames.length; i++) {
			mPaint.setColor(mItemColors[i]);
			float sweepAngle = (float) (360 * mItemRatios[i]);
			canvas.drawArc(mPieChartArea, mStartAngle, sweepAngle, true, mPaint);
			mStartAngle += sweepAngle;
		}
		mStartAngle = 0;
		canvas.restore();
		super.onDraw(canvas);
		/* 绘制文字，文字要水平竖直居中 */
		FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		String name = mItemNames[getPointerPos()];
		String ratio = String
				.format("%.2f", mItemRatios[getPointerPos()] * 100) + "%";
		/* 绘制类别 */
		canvas.drawText(name, mCenter.x, mCenter.y - fontMetrics.bottom,
				mTextPaint);
		/* 绘制所占百分比 */
		canvas.drawText(ratio, mCenter.x, mCenter.y - fontMetrics.top,
				mTextPaint);
	}

	@Override
	public void run() {// 减速滚动线程
		// TODO Auto-generated method stub
		while (mFastRotating) {// 速度没有减少到0
			if (mPreSpeed * mSpeed > 0) {
				rotatePie(mSpeed);
				mPreSpeed = mSpeed;
				mSpeed += mAcceleration;
			} else {// 速度已经减少到0，停止转动
				mFastRotating = false;
				setPointerHoming();
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 使指针指向当前所在条目的中间位置
	 * 
	 * */
	private void setPointerHoming() {

		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				int pos = getPointerPos();
				float startAngle = (90 - mDeltaAngle + 360) % 360;
				float endAngle = (mItemPos[pos] + (mItemPos[pos + 1] - mItemPos[pos]) / 2) * 360;
				float deltaAngle = endAngle - startAngle;
				float preDeltaAngle = deltaAngle;
				while (preDeltaAngle * deltaAngle > 0) {// 匀速滑动到指定位置
					float speed = deltaAngle > 0 ? 3 : -3;
					rotatePie(-speed);
					preDeltaAngle = deltaAngle;
					deltaAngle -= speed;
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (mListener != null) {
					mListener.onCompleteRotating(pos);
				}
			}

		}.start();

	}

	/**
	 * 获取当前指针的位置
	 * 
	 * @return 当前指针所指向的条目索引
	 * */
	public int getPointerPos() {
		/* 处理指针的指向，使得指针指向某个元素的正中间 */
		float pointerPos = (90 - mDeltaAngle + 360) % 360;// 以原饼图的0度初始位置为起点，当前指针的角度
		int pos = 0;
		for (; pos < mItemNames.length; pos++) {
			if (pointerPos < mItemPos[pos] * 360)
				break;
		}
		return --pos;
	}

	/**
	 * 绑定数据
	 * 
	 * @param itemName
	 *            条目名称
	 * @param itemRatio
	 *            条目所占比例
	 * */
	public void initData(String[] itemNames, float[] itemRatios) {
		mItemNames = itemNames;
		mItemRatios = itemRatios;
		/* 计算每个条目的起始位置 */
		mItemPos = new float[itemNames.length + 1];
		mItemPos[0] = 0;
		mItemPos[itemNames.length] = 1;
		for (int i = 1; i < mItemNames.length; i++) {
			mItemPos[i] = mItemPos[i - 1] + mItemRatios[i - 1];
		}
		setPointerHoming();// 将指针指向某一个条目的正中间
	}

	/**
	 * 按照给定的角度旋转饼图
	 * 
	 * @param deltaAngle
	 *            旋转的角度，正表示顺时针旋转，负表示逆时针旋转
	 * */
	public void rotatePie(double deltaAngle) {
		if (deltaAngle == 0)
			return;
		mDeltaAngle += deltaAngle;
		mDeltaAngle %= 360;
		this.postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mOriginalPoint.set(event.getX(), event.getY());
			mCurPoint.set(mOriginalPoint);
			mPrePoint.set(mOriginalPoint);
			mFastRotating = false;// 如果当前饼图正处于旋转状态，应停止转动
			mClockWise = 0;
			return true;
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			float xVelocity = mVelocityTracker.getXVelocity();
			float yVelocity = mVelocityTracker.getYVelocity();
			mPrePoint.set(mCurPoint);
			mCurPoint.set(event.getX(), event.getY());
			boolean clockWise = currentDirection(mPrePoint, mCurPoint);// 判断是顺时针方向还是逆时针方向
			mClockWise = clockWise ? mClockWise + 1 : mClockWise - 1;
			if (Math.abs(xVelocity) + Math.abs(yVelocity) > FLING_MIN_VELOCITY) {// 用户进行了快速滑动的操作
				mFastRotating = true;
			}
			rotatePie(getDeltaAngle(mPrePoint, mCurPoint));
			break;
		case MotionEvent.ACTION_UP:
			if (mFastRotating) {// 匀减速滚动
				/* 活动用户的瞬时滑动速度 */
				float velocity = (float) Math.sqrt(Math.pow(
						mVelocityTracker.getXVelocity(), 2)
						+ Math.pow(mVelocityTracker.getYVelocity(), 2));
				mSpeed = velocity / 300;
				mSpeed = mClockWise > 0 ? Math.abs(mSpeed) : -Math.abs(mSpeed);
				mPreSpeed = mSpeed;
				mAcceleration = -mSpeed / 50;
				new Thread(this).start();
			} else {
				setPointerHoming();
			}
			releaseVelocityTracker();
			break;
		}
		return super.onTouchEvent(event);
	}

	private void obtainVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * 根据触摸点，计算滑动的角度
	 * */
	private double getDeltaAngle(PointF startPoint, PointF endPoint) {
		// Converts rectangular coordinates (x, y) to polar coordinate (r,
		// theta) and returns theta (-pi~pi).
		double endAngle = 0;
		double startAngle = 0;
		double deltaAngle = 0;
		endAngle = Math.atan2(endPoint.y - mCenter.y, endPoint.x - mCenter.x)
				* 180 / Math.PI;
		startAngle = Math.atan2(startPoint.y - mCenter.y, startPoint.x
				- mCenter.x)
				* 180 / Math.PI;
		/* 处理用户手指划过180°的特殊情况 */
		if (startPoint.y < mCenter.y && endPoint.y > mCenter.y// 处理逆时针滑动时候，左侧区域临界切换的问题
				&& endAngle - startAngle > 180) {// 两个触摸点都在view左半区域
			deltaAngle = endAngle - startAngle - 360;
		} else if (startPoint.y > mCenter.y && endPoint.y < mCenter.y// 处理顺时针滑动时候，左侧区域临界切换的问题
				&& endAngle - startAngle < -180) {// 两个触摸点都在view左半区域
			deltaAngle = endAngle - startAngle + 360;
		} else
			deltaAngle = endAngle - startAngle;
		return deltaAngle;
	}

	/**
	 * 根据相邻的两个触摸点，判断当前滚动方向
	 * 
	 * @return 如果是顺时针方向返回true，否则返回false
	 * */
	private boolean currentDirection(PointF startPoint, PointF endPoint) {
		if (getDeltaAngle(startPoint, endPoint) > 0)
			return true;
		return false;
	}

	public interface OnCompleteRotating {
		/**
		 * 圆盘停止转动时，调用该函数
		 * 
		 * @param pos
		 *            指针当前位置
		 * */
		public void onCompleteRotating(int pos);
	}

	public void setOnCompleteRotatingListener(OnCompleteRotating listener) {
		mListener = listener;
	}
}
