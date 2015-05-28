package com.healthy.ui.tourtracker;

import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Symbol;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class HistoryTrackerActivity extends Activity{

	private MapView mMapView = null;
	private BMapManager mBMapManager = null;
	private static final String KEY = "39E3359ab5dccbb33dae0b5621dab52e";
	private GraphicsOverlay graphicsOverlay = null;
	private int id;//用户点击的轨迹号
	private ImageView mImageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/* 初始化地图服务 */
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(this);
			mBMapManager.init(KEY, new MKGeneralListener() {
	
				@Override
				public void onGetPermissionState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_PERMISSION_DENIED) {
						// 授权Key错误：
						Toast.makeText(
								HistoryTrackerActivity.this,
								"请在 DemoApplication.java文件输入正确的授权Key！",
								Toast.LENGTH_LONG).show();
					}
				}
	
				@Override
				public void onGetNetworkState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_NETWORK_CONNECT) {
						Toast.makeText(
								HistoryTrackerActivity.this, "您的网络出错啦！",
								Toast.LENGTH_LONG).show();
					} else if (error == MKEvent.ERROR_NETWORK_DATA) {
						Toast.makeText(
								HistoryTrackerActivity.this, "输入正确的检索条件！",
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}
		setContentView(R.layout.activity_tracker_history);
		mMapView = (MapView)findViewById(R.id.bmapsView);
		mImageView = (ImageView)findViewById(R.id.back_tracker_btn);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		id = bundle.getInt("id");
		
		List<GeoPoint> pointList = HealthyApplication.mDbUtil.queryLocationById(id);
		GeoPoint point = new GeoPoint(pointList.get(0).getLongitudeE6(),pointList.get(0).getLatitudeE6());
		mMapView.getController().setCenter(point);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(17);
		mMapView.getController().animateTo(point);
		
		graphicsOverlay = new GraphicsOverlay(mMapView);
        mMapView.getOverlays().add(graphicsOverlay);
		drawLine();
		
		mImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HistoryTrackerActivity.this.finish();
				overridePendingTransition(0,R.anim.roll_down);
			}
		});
	}
	
	/**
	 * 通过发送id号查询信息，还原轨迹
	 */
	private void drawLine()
	{
		List<GeoPoint> pointList = HealthyApplication.mDbUtil.queryLocationById(id);
		int size = pointList.size();
		GeoPoint[] point = new GeoPoint[size];
		for(int i = 0;i<size;i++)
		{
			GeoPoint mPoint = new GeoPoint(pointList.get(i).getLongitudeE6(),pointList.get(i).getLatitudeE6());
			point[i]=mPoint;
		}
		Geometry mGeometry = new Geometry();
		mGeometry.setPolyLine(point);
		
		Symbol lineSymbol = new Symbol();
  		Symbol.Color lineColor = lineSymbol.new Color();
  		lineColor.red = 255;
  		lineColor.green = 0;
  		lineColor.blue = 0;
  		lineColor.alpha = 255;
  		lineSymbol.setLineSymbol(lineColor, 6);
  		
  		Graphic lineGraphic = new Graphic(mGeometry, lineSymbol);
  		
  		graphicsOverlay.setData(lineGraphic);
  		mMapView.refresh();
		 
	}
	
	
	@Override
	protected void onDestroy() {
		 mMapView.destroy();
	        if(mBMapManager!=null){
	                mBMapManager.destroy();
	                mBMapManager=null;
	        }
	    
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		 mMapView.onPause();
	        if(mBMapManager!=null){
	                mBMapManager.stop();
	        }
	        overridePendingTransition(0,R.anim.roll_down);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
        if(mBMapManager!=null){
                mBMapManager.start();
        }
        overridePendingTransition(R.anim.roll_up,R.anim.roll);
		super.onResume();
	}
	
}
