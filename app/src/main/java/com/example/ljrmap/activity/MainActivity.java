package com.example.ljrmap.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.cloud.CloudRgcResult;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.ljrmap.R;
import com.example.ljrmap.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiver;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    //判断首次定位
    private boolean isFirstLocate = true;
    private EditText mSearch;
    private Button mSearchBtn;
    private PoiSearch mPoiSearch;
    private LatLng mLatLng;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        //权限操作---针对6.0以上的手机
        requestPermissions();


    }

    //初始化布局
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        mSearch = (EditText) findViewById(R.id.search);
        mSearchBtn = (Button) findViewById(R.id.search_btn);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        //poi搜索
        mPoiSearch = PoiSearch.newInstance();
        //创建监听者
        OnGetPoiSearchResultListener poiSearchResultListener = new PoiSearchResultListener();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
    }


    //触发搜索
    public void search(View view) {
        String name = mSearch.getText().toString().trim();
        if (name.equals("")) {
            ToastUtils.showToast(this, "搜索内容不能为空！");
        } else {
            mPoiSearch.searchNearby(getSearchParams(name));
        }


    }
    private PoiNearbySearchOption getSearchParams(String name) {
        PoiNearbySearchOption params = new PoiNearbySearchOption();
        params.location(mLatLng);//当前位置
        params.radius(5000);//搜索范围
        params.keyword(name);
        return params;
    }

    /**
     * 运行时权限操作
     */
    private void requestPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 0);
        } else {
            requestLocation();
        }
    }

    //开始定位
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    //自定义定位设置
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    //监听定位
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocate) {
            ToastUtils.showToast(this, "现在位于" + bdLocation.getAddrStr());
            mLatLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLng(mLatLng);
            mBaiduMap.animateMapStatus(statusUpdate);
            statusUpdate = MapStatusUpdateFactory.zoomTo(18f);
            mBaiduMap.animateMapStatus(statusUpdate);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    //监听搜索
    private class PoiSearchResultListener implements OnGetPoiSearchResultListener {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                ToastUtils.showToast(MainActivity.this, "没有搜索到结果");
                return;
            }
            Log.d(TAG, "================onGetPoiResult: jiegou");
            //获取POI检索结果
            List<PoiInfo> allAddr = poiResult.getAllPoi();
            for (PoiInfo p: allAddr) {
                Log.d(TAG, "onGetPoiResult: sssssssssssssssssssssssssssssssssss");
                Log.d(TAG, "onGetPoiResult: "+"p.name--->" + p.name +"p.phoneNum" + p.phoneNum +" -->p.address:" + p.address + "p.location" + p.location);
            }
            Log.d(TAG, "================onGetPoiResult: jiegoddddddddddddddddddddddddddddddddd");
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    }

    //加载toolbar.xml菜单文件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //处理toolbar点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.satellite:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                mBaiduMap.setBaiduHeatMapEnabled(false);
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.traffic:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(true);
                break;
            case R.id.heatMap:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                mBaiduMap.setBaiduHeatMapEnabled(true);
                break;
            case R.id.normal:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                mBaiduMap.setBaiduHeatMapEnabled(false);
                break;
        }
        return true;
    }

    /**
     * 请求权限结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ToastUtils.showToast(this, "必须同意所有权限才能使用本程序");
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    ToastUtils.showToast(this, "发生未知错误!!");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        unregisterReceiver(mReceiver);//停止监听网络状态和key错误
        super.onDestroy();
        mLocationClient.stop();//停止定位更新
        mMapView.onDestroy();//
        mBaiduMap.setMyLocationEnabled(false);
        mPoiSearch.destroy();
    }
}
