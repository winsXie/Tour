package com.bn.thread;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.content.Context;
import android.os.Handler;

public class LocationThread extends Thread {
    private Context context;
    private Handler handler;
    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private LocationMode tempMode = LocationMode.Hight_Accuracy;// 定位模式：高精度
    private String tempcoor = "gcj02";// 坐标系：国测局加密经纬度
    private String mlocation = null;
    private boolean stop = false;

    // tempMode = LocationMode.Battery_Saving;//定位模式：低功耗
    // tempMode = LocationMode.Device_Sensors;//定位模式：仅设备
    //
    // tempcoor = "bd09ll";//坐标系：百度加密经纬度
    // tempcoor = "bd09";//坐标系：百度加密墨卡托

    public LocationThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        InitLocation();
    }

    @Override
    public void run() {
        // 若定位失败则继续定位20秒
        while (mlocation == null && !stop) {
        }

        if (mlocation != null) {
            mLocationClient.stop();// 停止定位
            handler.obtainMessage(1, mlocation).sendToTarget();// 成功获取地址
        } else {
            mLocationClient.stop();// 停止定位
            handler.obtainMessage(2, mlocation).sendToTarget();// 获取地址失败
        }
        super.run();
    }

    private void InitLocation() {

        mLocationClient = new LocationClient(context);

        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);// 设置实时监听

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);// 设置定位模式
        option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true);// 设置显示地理位置
        mLocationClient.setLocOption(option);

        mLocationClient.start();
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {
        int i = 0;

        @Override
        public void onReceiveLocation(BDLocation location) {
            mlocation = location.getAddrStr();

            i++;
            if (i > 20) {
                stop = true;// 定位超过20秒则停止定位
            }
        }

    }

}
