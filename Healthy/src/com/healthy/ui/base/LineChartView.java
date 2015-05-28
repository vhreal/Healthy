package com.healthy.ui.base;

import com.healthy.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class LineChartView extends View {
	
	/*测试用例*/
	private String[] mItemTags = { "01月", "02月", "03月", "04月", "05月", "06月",
			"07月", "08月", "09月", "10月", "11月", "12月" };
	private float[] mItemValues = new float[12];

	/* 当前view的size信息 */
	private int mWidth = 0;
	private int mHeight = 0;
	private Point mCenter = new Point();// 中心坐标
	private Rect mContentLocation = new Rect();// 要绘制的内容在该视图中的位置，排除了padding的影响

	/* 处理折线图随手势进行滑动的相关变量 */
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private PointF mPrePoint = new PointF();
	private PointF mCurPoint = new PointF();
	private static final int FLING_MIN_VELOCITY = 6000;// 最小快速滑动速度
	private boolean mAdjusted = false;

	/* 图像绘制相关变量 */
	private PathEffect mPathEffect = null;// 绘制虚线的时候使用
	private Paint mPaint = new Paint();
	private NinePatch mArrowNinePatch;
	private NinePatch mCurDataNinePath;// 当前被选择的数据
	private Rect mLocation = new Rect();// 图像绘制的位置
	private FontMetrics mFontMetrics = new FontMetrics();
	private Rect mTextBounds = new Rect();
	private int mLength = 0;// 用于记录折线的长度，以像素为单位

	/* 内容相关 */
	private String mAvgTip = "";// 数据平均值的标题，例如“平均值”
	private float mAvgValue = 0;
	private String mStrAvg;// 平均值大小，string表示
	private PointF[] mItemPoses;// 每个数据点在视图中所对应的位置
	private int mCurSelectedPos = 0;// 当前居中的条目
	private float mMaxTagHeight = 0;// 横坐标标签的的最大高度
	private float mMaxTagWidth = 0;// 横坐标标签的最大宽度
	private final static float TAG_SPACING = 20;// 横坐标之间的间距

	public LineChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public LineChartView(Context context) {
		this(context, null, 0);
	}

	public LineChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private void init() {
		
		mScroller = new Scroller(getContext());
		mPathEffect = new DashPathEffect(new float[] { 8.0f, 8.0f }, 0);// 绘制虚线，空白和实线的长度均为10
		/* 绘制中间的箭头提示区域，使用NinePatch背景 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg_arrow_tag);
		mArrowNinePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(),
				null);
		/* 当前所选择的点的背景 */
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg_calories_detail);
		mCurDataNinePath = new NinePatch(bitmap, bitmap.getNinePatchChunk(),
				null);
		mAvgTip = getResources().getString(R.string.tip_avg_value);
		
		/**
		 * 注意：测试用例，使用中要将这两句话删除，转而调用
		 * initData()函数来初始化数据
		 * */
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// 平均值的字符串表示,保留两位小数
		
	}

	/**
	 * 初始化数据
	 * 
	 * @param itemTags
	 *            横坐标标签
	 * @param itemValues
	 *            具体的数据值
	 * */
	public void initData(String[] itemTags, float[] itemValues) {
		if (itemTags == null || itemValues == null)
			return;
		mItemTags = itemTags;
		mItemValues = itemValues;
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// 平均值的字符串表示,保留两位小数
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawCoordinate(canvas);// 绘制坐标系信息
		drawDataPoint(canvas);// 绘制数据点及其之间的连线
		if (!mAdjusted && mScroller.isFinished()) {
			setPointerHoming();
			mAdjusted = true;
		}
	}

	/** 绘制坐标系 */
	private void drawCoordinate(Canvas canvas) {
		int sc = canvas.save();
		/* 将画布移动到getScrollX()再绘制坐标系,以保持用户滚动时候，坐标系仍保持不动 */
		canvas.translate(getScrollX(), 0);
		/*
		 * 绘制中间的横向虚线，该虚线表示数据的平均值，该线位于视图的正中间，并以此轴的y值为0 向上y值增大，向下降低
		 */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2.5f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(getResources().getColor(R.color.light_gray));
		mPaint.setPathEffect(mPathEffect);
		canvas.drawLine(mContentLocation.left, mCenter.y,
				mContentLocation.right, mCenter.y, mPaint);
		/* 绘制位于中间的纵向实线 */
		mPaint.setPathEffect(null);
		canvas.drawLine(mCenter.x, mContentLocation.top, mCenter.x,
				mContentLocation.bottom, mPaint);
		/* 绘制中间的箭头提示区域 */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xffffffff);// 设定颜色为白色
		mPaint.setTextSize(19.0f);
		mPaint.setTextAlign(Align.CENTER);
		/* 获取文字大小信息 */
		mPaint.getFontMetrics(mFontMetrics);
		int fontHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);// 获取文字高度
		/* 根据文字大小确定提示信息的具体大小和位置 */
		mLocation.left = mContentLocation.left + 10;
		mLocation.top = mCenter.y - fontHeight;
		mLocation.bottom = mCenter.y + fontHeight;
		mLocation.right = mLocation.left
				+ 15
				+ (int) Math.max(mPaint.measureText(mAvgTip),
						mPaint.measureText(mStrAvg)) + 25;
		mArrowNinePatch.draw(canvas, mLocation);// 绘制提示信息背景
		/* 绘制提示信息文字 */
		canvas.drawText(mAvgTip, mLocation.left
				+ (mLocation.right - mLocation.left - 15) / 2, mCenter.y
				- mFontMetrics.bottom, mPaint);
		canvas.drawText(mStrAvg, mLocation.left
				+ (mLocation.right - mLocation.left - 15) / 2, mCenter.y
				- mFontMetrics.top, mPaint);
		canvas.restoreToCount(sc);
	}

	/** 绘制数据信息 */
	private void drawDataPoint(Canvas canvas) {

		mPaint.reset();
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setTextSize(25.0f);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(5.0f);
		mPaint.setColor(getResources().getColor(R.color.red));
		mPaint.setStyle(Style.STROKE);

		mPaint.getFontMetrics(mFontMetrics);// 获取字体的大小
		/* 绘制路径 */
		for (int i = 1; i < mItemPoses.length; i++) {
			canvas.drawLine(mItemPoses[i - 1].x, mItemPoses[i - 1].y,
					mItemPoses[i].x, mItemPoses[i].y, mPaint);
		}

		/* 绘制数据点和坐标标签 */
		for (int i = 0; i < mItemValues.length; i++) {
			if (i == mCurSelectedPos) {// 当前点为被选择的点 绘制数据点
				mPaint.setStrokeWidth(6.0f);
				mPaint.setColor(getResources().getColor(android.R.color.white));
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 10.0f,
						mPaint);
				mPaint.setColor(getResources().getColor(R.color.red));
				mPaint.setStyle(Style.STROKE);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 10.0f,
						mPaint);
				/* 绘制焦点矩形框 */
				mPaint.setStyle(Style.FILL);
				canvas.drawRect(mItemPoses[i].x - mMaxTagWidth / 2 - 5,
						mContentLocation.bottom - mFontMetrics.bottom
								+ mFontMetrics.top - 20, mItemPoses[i].x
								+ mMaxTagWidth / 2 + 5,
						mContentLocation.bottom - 20, mPaint); /* 绘制横坐标标签 */
				mPaint.setColor(getResources().getColor(android.R.color.white));
				canvas.drawText(mItemTags[i], mItemPoses[i].x,
						mContentLocation.bottom - mFontMetrics.bottom - 20,
						mPaint);
				/* 绘制当前选择的数据点的的数据 */
				mPaint.setColor(getResources().getColor(R.color.red));
				float hwidth = mPaint.measureText(mItemValues[i] + "") / 2;
				float fontheight = mFontMetrics.bottom - mFontMetrics.top;
				mLocation.set((int) (mItemPoses[i].x - hwidth - 10),
						(int) (mItemPoses[i].y - 20 - fontheight - 15),
						(int) (mItemPoses[i].x + hwidth + 10),
						(int) (mItemPoses[i].y - 20));
				mCurDataNinePath.draw(canvas, mLocation);
				canvas.drawText(mItemValues[i] + "",
						(mLocation.left + mLocation.right) / 2,
						mLocation.bottom - mFontMetrics.bottom - 15, mPaint);
			} else {
				/* 绘制数据点 */
				mPaint.setColor(getResources().getColor(R.color.red));
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 8.0f,
						mPaint);
				/* 绘制横坐标标签 */
				mPaint.setColor(getResources().getColor(android.R.color.black));
				canvas.drawText(mItemTags[i], mItemPoses[i].x,
						mContentLocation.bottom - mFontMetrics.bottom - 20,
						mPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mCurPoint.set(event.getX(), event.getY());
			return true;
		case MotionEvent.ACTION_MOVE:
			mPrePoint.set(mCurPoint);
			mCurPoint.set(event.getX(), event.getY());
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			float xVelocity = mVelocityTracker.getXVelocity();
			if (Math.abs(xVelocity) > FLING_MIN_VELOCITY
					&& mScroller.isFinished()) {
				int distance = 0;
				if (xVelocity < 0) {// 向左滚动,该条件语句是为了保证不超过最大滚动距离
					distance = (int) Math.min(getScrollX() - xVelocity / 15,
							100 + mLength) - getScrollX();
				} else {// 向右滚动
					distance = (int) Math.max(getScrollX() - xVelocity / 15,
							-100) - getScrollX();
				}
				mScroller.startScroll(getScrollX(), 0, distance, 0, 1000);
			} else {
				int distance = (int) ((mPrePoint.x - mCurPoint.x) / 2);
				if (xVelocity < 0 && getScrollX() + distance < 100 + mLength// 向左滚动,并且没有超过最大滚动距离
						|| xVelocity > 0 && getScrollX() + distance > -100) {// 向右滚动，并且没有超过最大滚动距离
					scrollBy(distance, 0);
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			mAdjusted = false;
			postInvalidate();
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
	 * 设定横轴的提示信息
	 * */
	public void setArrowTip(String tip) {
		mAvgTip = tip;
	}

	/** 获取数据的平均值 */
	public float getDataAvgValue() {
		if (mItemValues == null)
			return 0;
		float sum = 0;
		for (int i = 0; i < mItemValues.length; i++) {
			sum += mItemValues[i];
		}
		return sum /= mItemValues.length;
	}

	/**
	 * 将数据值投射到视图上的某个位置
	 * 
	 * @param textPaint
	 *            绘制横坐标标签所使用的画笔
	 */
	private void value2Pos(Paint textPaint) {

		if (mItemValues == null)
			return;
		mItemPoses = new PointF[mItemValues.length];
		float min = 0, max = 0;

		/* 搜索数据中的最大值和最小值 */
		for (int i = 0; i < mItemValues.length; i++) {
			if (min > mItemValues[i])
				min = mItemValues[i];
			else if (max < mItemValues[i])
				max = mItemValues[i];
		}

		/* 计算每一个数据点在视图中纵坐标 */
		for (int i = 0; i < mItemValues.length; i++) {
			mItemPoses[i] = new PointF();
			if(max!=min){
				mItemPoses[i].y = (1 - (mItemValues[i] - min) / (max - min))
						* (mContentLocation.bottom - mContentLocation.top - 220)
						+ 120;
			}else{
				mItemPoses[i].y = mContentLocation.bottom - mContentLocation.top - 220 + 120;
			}
		}

		/* 获取最长的字符串宽度和高度 */
		mMaxTagHeight = 0;
		mMaxTagWidth = 0;
		float height = 0;
		float width = 0;
		for (int i = 0; i < mItemTags.length; i++) {
			textPaint.getTextBounds(mItemTags[i], 0, mItemTags[i].length(),
					mTextBounds);
			height = mTextBounds.bottom - mTextBounds.top;
			width = mTextBounds.right - mTextBounds.left;
			if (mMaxTagHeight < height)
				mMaxTagHeight = height;
			if (mMaxTagWidth < width)
				mMaxTagWidth = width;
		}
		/* 计算每一个数据点的横坐标的位置 */
		for (int i = 0; i < mItemTags.length; i++) {
			mItemPoses[i].x = mCenter.x + (mMaxTagWidth + TAG_SPACING) * i;
		}
		mLength = (int) (mItemPoses[mItemPoses.length - 1].x - mItemPoses[0].x);// 最左侧和最右侧的半圆半径没有计算在内
	}

	/** 获取指针当前应该指向的调条目 */
	public int getPointerPos() {

		int x = mWidth / 2 + getScrollX();
		if (x <= mItemPoses[0].x)// 滚动到最左侧
			return 0;
		if (x >= mItemPoses[mItemPoses.length - 1].x)// 滚动到最右侧
			return mItemPoses.length - 1;
		int i = mItemPoses.length - 1;
		for (; i >= 0; i--) {
			if (x >= mItemPoses[i].x)
				break;
		}
		if (x - mItemPoses[i].x < (mItemPoses[i + 1].x - mItemPoses[i].x) / 2)
			return i;
		else
			return i + 1;

	}

	/**
	 * 使指针指向当前所在条目
	 * 
	 * */
	private void setPointerHoming() {
		mCurSelectedPos = getPointerPos();
		int distance = (int) ((mItemPoses[mCurSelectedPos].x - mWidth / 2) - getScrollX());
		if (mScroller.isFinished()) {
			// Scroller has nothing to do with the UI - it's just a helper class
			// that helps to compute position based on initial position and
			// initial velocity
			mScroller.startScroll(getScrollX(), 0, distance, 0, 500);
			invalidate();
		}

	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		mWidth = getWidth();
		mHeight = getHeight();
		mCenter.set(mWidth / 2, mHeight / 2);

		mContentLocation.set(getPaddingLeft(), getPaddingTop(), mWidth
				- getPaddingRight(), mHeight - getPaddingBottom());
		
		/* 投射每个数据在视图中的位置 */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(25.0f);
		mPaint.setTextAlign(Align.CENTER);
		value2Pos(mPaint);
	
	}
	
	public void setItemValues(float[] itemValues){
		mItemValues = itemValues;
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// 平均值的字符串表示,保留两位小数
		invalidate();
	}

}
