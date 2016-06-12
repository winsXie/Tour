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
    private LocationMode tempMode = LocationMode.Hight_Accuracy;// ��λģʽ���߾���
    private String tempcoor = "gcj02";// ����ϵ������ּ��ܾ�γ��
    private String mlocation = null;
    private boolean stop = false;

    // tempMode = LocationMode.Battery_Saving;//��λģʽ���͹���
    // tempMode = LocationMode.Device_Sensors;//��λģʽ�����豸
    //
    // tempcoor = "bd09ll";//����ϵ���ٶȼ��ܾ�γ��
    // tempcoor = "bd09";//����ϵ���ٶȼ���ī����

    public LocationThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        InitLocation();
    }

    @Override
    public void run() {
        // ����λʧ���������λ20��
        while (mlocation == null && !stop) {
        }

        if (mlocation != null) {
            mLocationClient.stop();// ֹͣ��λ
            handler.obtainMessage(1, mlocation).sendToTarget();// �ɹ���ȡ��ַ
        } else {
            mLocationClient.stop();// ֹͣ��λ
            handler.obtainMessage(2, mlocation).sendToTarget();// ��ȡ��ַʧ��
        }
        super.run();
    }

    private void InitLocation() {

        mLocationClient = new LocationClient(context);

        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);// ����ʵʱ����

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);// ���ö�λģʽ
        option.setCoorType(tempcoor);// ���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
        option.setScanSpan(1000);// ���÷���λ����ļ��ʱ��Ϊ1000ms
        option.setIsNeedAddress(true);// ������ʾ����λ��
        mLocationClient.setLocOption(option);

        mLocationClient.start();
    }

    /**
     * ʵ��ʵλ�ص�����
     */
    public class MyLocationListener implements BDLocationListener {
        int i = 0;

        @Override
        public void onReceiveLocation(BDLocation location) {
            mlocation = location.getAddrStr();

            i++;
            if (i > 20) {
                stop = true;// ��λ����20����ֹͣ��λ
            }
        }

    }

}
