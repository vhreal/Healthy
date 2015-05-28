package com.healthy.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class FlipperLayout extends ViewGroup {

	private final static int SCROLL_TIME = 400;// 250
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mWidth;

	/* 菜单开启状态 */
	public static final int MENU_STATE_CLOSE = 0;// 菜单关闭状态
	public static final int MENU_STATE_OPEN = 1;// 菜单打开状态
	private int mMenuState = 0;// 记录当前菜单的打开状态

	/* 当前触摸状态 */
	public static final int TOUCH_STATE_RESTART = 0;// 开始滚动
	public static final int TOUCH_STATE_SCROLLING = 1;// 当前正在进行滚动
	private int mTouchState = 0;

	/* 是否允许滚动 */
	public static final int SCROLL_STATE_NO_ALLOW = 0;
	public static final int SCROLL_STATE_ALLOW = 1;
	private int mScrollState = 0;

	private int mVelocityValue = 0;

	/*
	 * 当菜单处于打开状态时候，用户可以直接点击靠近屏幕右边mWidth以内的区域， 软件自动向左滑动关闭主菜单，该变量为用户是否成功点击的标志位
	 */
	private boolean mOnClick = false;

	public FlipperLayout(Context context) {
		super(context);
		mScroller = new Scroller(context);

		// 设定mWidth的值为54dip，该宽度为打开菜单时候，主界面还可以显示的宽度
		// 54dip同样是用户可以进行拖拽的距离左侧屏幕边界最小宽度
		mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				54, getResources().getDisplayMetrics());

	}

	public FlipperLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlipperLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		// 将各个子视图覆盖进行绘制
		// 在该应用中，只有两个子视图，分别是主菜单和主页面
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int height = child.getMeasuredHeight();
			int width = child.getMeasuredWidth();
			child.layout(0, 0, width, height);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		obtainVelocityTracker(event);// 调用该函数以记录滑动速度
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 该case语句的
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_RESTART) {
				int x = (int) event.getX();
				int screenWidth = getWidth();
				if (x <= mWidth
						&& mMenuState == MENU_STATE_CLOSE
						&& mTouchState == TOUCH_STATE_RESTART// 当前用户正在打开菜单
						|| x >= screenWidth - mWidth
						&& mMenuState == MENU_STATE_OPEN
						&& mTouchState == TOUCH_STATE_RESTART) {// 当前用户正在关闭菜单
					// 当菜单开启时候，表示用户点击了靠近屏幕右边界mWidth以内的区域，若用户紧接着不发出ACTION_MOVE的操作，则软件在ACTION_UP事件中自动滑动关闭菜单
					if (mMenuState == MENU_STATE_OPEN) {
						mOnClick = true;
					}
					mScrollState = SCROLL_STATE_ALLOW;// 允许滚动屏幕
				} else {
					mOnClick = false;
					mScrollState = SCROLL_STATE_NO_ALLOW;// 禁止滚动屏幕
				}
			} else {// 菜单正处于滚动状态，不进行任何操作
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			if (mScrollState == SCROLL_STATE_ALLOW
					&& getWidth() - (int) event.getX() < mWidth) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			releaseVelocityTracker();
			if (mOnClick) {// 当前菜单已经打开，需要关闭
				mOnClick = false;
				mMenuState = MENU_STATE_CLOSE;
				mScroller.startScroll(getChildAt(1).getScrollX(), 0,
						-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 800
				invalidate();
				return true;
			}
			break;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_SCROLLING) {// 滚动没有结束
				return false;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			mOnClick = false;
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			if (mScrollState == SCROLL_STATE_ALLOW
					&& Math.abs(mVelocityTracker.getXVelocity()) > 200) {
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			releaseVelocityTracker();
			if (mScrollState == SCROLL_STATE_ALLOW
					&& mMenuState == MENU_STATE_OPEN) {
				return true;
			}
			break;
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_SCROLLING) {// 滚动没有结束
				return false;
			}
			break;

		case MotionEvent.ACTION_MOVE:// 开始滑动屏幕
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			mVelocityValue = (int) mVelocityTracker.getXVelocity();

			/*
			 * 为什么是-(int) event.getX(),这里需要解释一下函数scrollTo(int x, int y)的作用是将当前视图
			 * 内容的左上角坐标偏移至(x,y)处，即可视区域的左上角位于(x , y)坐标处。当我们向右滑动的时候，我们需要将该视图的可
			 * 显示区域向左进行扩展，这样在父视图中才能显示出向右滑动后左侧露出的部分（如果没有定义则是透明的）
			 * 即向右滑动则意味着将可视区域向左侧滑动
			 */
			getChildAt(1).scrollTo(-(int) event.getX(), 0);
			break;

		// 在ACTION_UP事件中，来判断是否打开或者关闭菜单
		case MotionEvent.ACTION_UP:
			if (mScrollState == SCROLL_STATE_ALLOW) {
				if (mVelocityValue > 2000) {// 向右快速滑动，打开菜单
					mMenuState = MENU_STATE_OPEN;
					mScroller
							.startScroll(
									getChildAt(1).getScrollX(),
									0,
									-(getWidth()
											- Math.abs(getChildAt(1)
													.getScrollX()) - mWidth),
									0, SCROLL_TIME);// 250
					invalidate();
				} else if (mVelocityValue < -2000) {// 向左快速滑动关闭菜单
					mMenuState = MENU_STATE_CLOSE;
					mScroller.startScroll(getChildAt(1).getScrollX(), 0,
							-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 250
					invalidate();
				} else if (event.getX() < getWidth() / 2) {
					mMenuState = MENU_STATE_CLOSE;
					mScroller.startScroll(getChildAt(1).getScrollX(), 0,
							-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 800
					invalidate();
				} else {
					mMenuState = MENU_STATE_OPEN;
					mScroller
							.startScroll(
									getChildAt(1).getScrollX(),
									0,
									-(getWidth()
											- Math.abs(getChildAt(1)
													.getScrollX()) - mWidth),
									0, SCROLL_TIME);// 800
					invalidate();
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	// 向右滚动打开菜单
	public void open() {
		mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
				: TOUCH_STATE_SCROLLING;
		if (mTouchState == TOUCH_STATE_RESTART) {
			mMenuState = MENU_STATE_OPEN;
			mScroller.startScroll(getChildAt(1).getScrollX(), 0, -(getWidth()
					- Math.abs(getChildAt(1).getScrollX()) - mWidth), 0,
					SCROLL_TIME);// 800
			invalidate();
		}
	}

	// 向左滚动关闭菜单
	public void changeContentView(View view) {
		if (mMenuState == MENU_STATE_OPEN) {
			mMenuState = MENU_STATE_CLOSE;
			mScroller.startScroll(getChildAt(1).getScrollX(), 0, -getChildAt(1)
					.getScrollX(), 0, SCROLL_TIME);// 800
			invalidate();
		}
		setContentView(view);
	}

	public void setContentView(View view) {
		removeViewAt(1);
		addView(view, 1, getLayoutParams());
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

	public interface OnOpenListener {
		public abstract void open();
	}

	public interface OnCloseListener {
		public abstract void close();
	}

	/**
	 * 获取当前的菜单状态：开启菜单或者关闭菜单
	 * 
	 * @return MENU_STATE_OPEN 或者 MENU_STATE_CLOSE
	 * */
	public int getScreenState() {
		return mMenuState;
	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			getChildAt(1).scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

}
