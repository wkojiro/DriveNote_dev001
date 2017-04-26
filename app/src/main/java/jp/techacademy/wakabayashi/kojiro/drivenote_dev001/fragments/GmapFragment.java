package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Map;

import bolts.Continuation;
import bolts.Task;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.ApiBase;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.BuildConfig;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Const;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.LocationUpdatesService;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.LoginActivity;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.MainActivity;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.SettingActivity;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Utils;

/**
 * Created by wkojiro on 2017/04/24.
 */

public class GmapFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {
    private static final String TAG = GmapFragment.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 134;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    GoogleMap mMap = null;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    String destname;
    Double destlatitude, destlongitude;

    private LatLng latlng;

    private static TextView mTextView;

    Marker destmarker;
    Marker currentMarker;
    MarkerOptions currentMarkerOptions = new MarkerOptions();
    protected LatLng currentlatlng;
    protected Location mCurrentLocation;

    MarkerOptions destMarkerOptions = new MarkerOptions();
    protected LatLng destlatlng;

    Polyline polylineFinal;
    PolylineOptions options;

    private Float originaldistance;
    private Float nowdistance;
    private double referencedistance;

    //memo: set value(スコープを要注意）
    Integer mailCount = 0;

    // Preference elements
    SharedPreferences sp;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d("debug", "Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.d("debug", "Service DisConnected");
        }
    };


    private void resetState(){
        // onGoingをとめる
        Utils.setOnGoing(getActivity(), false);
        //setUIState(Utils.requestingLocationUpdates(MainActivity.this));

        // 目的地を削除する
        Utils.removeThisDest(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);


        resetState();

        mTextView = (TextView) getActivity().findViewById(R.id.textView);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(getActivity())) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {

                mCurrentLocation = location;
                mapState();
                Toast.makeText(context, Utils.getLocationText(mCurrentLocation), Toast.LENGTH_SHORT).show();
                mTextView.setText(Utils.getLocationText(mCurrentLocation));
            }
        }
    }

    private void mapState(){

        if (mCurrentLocation == null){
            defaultMap();
        } else if(!Utils.isEmptyDest(getActivity()) && !Utils.onGoing(getActivity())) {
            firstMap();
        } else if(Utils.onGoing(getActivity())) {
            activeMap();
        } else {
            defaultMap();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("Fragment", "MapFragment onCreateView");

        // public View inflate (int resource, ViewGroup root, boolean attachToRoot)
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
        Toast.makeText(getActivity(), "OnViewCreated", Toast.LENGTH_SHORT).show();
        Log.d("Fragment", "MapFragment onViewCreated");
    }


    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(getActivity(), "OnStart", Toast.LENGTH_SHORT).show();

        mRequestLocationUpdatesButton = (Button) getActivity().findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) getActivity().findViewById(R.id.remove_location_updates_button);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isEmptyUser(getActivity())){
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    if (!checkPermissions()) {
                        requestPermissions();
                    } else {
                        mService.requestLocationUpdates();

                        if (Utils.isEmptyDest(getActivity())) {
                            Intent intent = new Intent(getActivity(), SettingActivity.class);
                            startActivity(intent);

                            //ここでFirstMapが呼ばれる（PermissionOK, UserOK, APIOK, DestOK, onGoingNG)

                        } else if (!Utils.onGoing(getActivity())) {

                            if(mCurrentLocation == null){
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("お知らせ")
                                        .setMessage("通信状況を確認してください。")
                                        .setPositiveButton("OK", null)
                                        .show();


                            } else {

                                Utils.setOnGoing(getActivity(), true);
                                setUIState(Utils.requestingLocationUpdates(getActivity()), Utils.onGoing(getActivity()));
                                new ApiBase(getActivity()).postMailAsync(String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()))
                                        .onSuccess(new Continuation<String, String>() {
                                            @Override
                                            public String then(Task<String> task) throws Exception {
                                                Toast.makeText(null, "メール送信しました。", Toast.LENGTH_SHORT).show();
                                                return null;
                                            }
                                        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                                    @Override
                                    public String then(Task<String> task) throws Exception {
                                        if (task.isFaulted()) {
                                            Toast.makeText(getActivity(), "メール送信に失敗しました。", Toast.LENGTH_SHORT).show();
                                        }
                                        return null;
                                    }
                                }, Task.UI_THREAD_EXECUTOR);
                                if (originaldistance != null) {
                                    Utils.removeOriginalDistance(getActivity());
                                    Utils.setOriginalDistance(getActivity(), String.valueOf(originaldistance));
                                }
                            }


                        }

                    }
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1.Serviceをとめる（＝APIを止める）
                mService.removeLocationUpdates();
                // 2.onGoingをとめる
                Utils.setOnGoing(getActivity(), false);
                //setUIState(Utils.requestingLocationUpdates(MainActivity.this));
                // 3.目的地を削除する
                Utils.removeThisDest(getActivity());
                // 4.地図を戻す
                defaultMap();
                // 5.Buttonを戻す
                setUIState(Utils.requestingLocationUpdates(getActivity()), Utils.onGoing(getActivity()));
            }
        });
        // Restore the state of the buttons when the activity (re)launches.
        setUIState(Utils.requestingLocationUpdates(getActivity()), Utils.onGoing(getActivity()));
        Log.d("debug", "getActivity().getApplicationContext()" + getActivity().getApplicationContext());
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        getActivity().bindService(new Intent(getActivity(), LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        Log.d("debug","onResume");
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
        super.onPause();
        Log.d("debug","onPause");
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getActivity().unbindService(mServiceConnection);
            mBound = false;
        }
        //sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
        Log.d("debug","onStop");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        //memo: locationUpdateのrequestがされたら、Buttonを変更する。
        //memo: ここのFalseはあくまでもDefault値なので、結果では無い。

        Log.d("debug","MapFragment: PreferenceChanged");


        if (s.equals(Utils.LocationUpDateKEY)) {
           // setUIState(sp.getBoolean(Const.LocationUpDateKEY, false), sp.getBoolean(Const.OngoingKEY, false));
            Log.d("debug", "1:LocationUpdate,OnGoingCondition:" + sp.getBoolean(Utils.OngoingKEY, false) + sp.getBoolean(Utils.LocationUpDateKEY, false));

        }
        if (s.equals(Utils.OngoingKEY)) {
           // setUIState(sp.getBoolean(Const.LocationUpDateKEY, false), sp.getBoolean(Const.OngoingKEY, false));
            Log.d("debug", "2:LocationUpdate,OnGoingCondition:" + sp.getBoolean(Utils.OngoingKEY, false) + sp.getBoolean(Utils.LocationUpDateKEY, false));

        }

        /*
        if(s.equals(Utils.EmailKEY)){
            email = sp.getString(Utils.EmailKEY,"");
        }

        if(s.equals(Utils.TokenKey)){
            token = sp.getString(Utils.TokenKey,"");
        }

        if(s.equals(Utils.DestemailKEY)){
            destemail = sp.getString(Utils.DestemailKEY,"");
        }

        if(s.equals(Utils.DestaddressKEY)){
            destaddress = sp.getString(Utils.DestaddressKEY,"");
        }

        if(s.equals(Utils.DestnameKEY)){
            destname = sp.getString(Utils.DestnameKEY,"");
        }
*/


        //memo: ユーザーがログアウトなどして、Emptyになったら、OnGoingを外す。
        if (Utils.isEmptyUser(getActivity())) {
            Utils.setOnGoing(getActivity(), false);
            //setUIState(sp.getBoolean(Const.LocationUpDateKEY, false), sp.getBoolean(Const.OngoingKEY, false));
            Log.d("debug", "3:LocationUpdate,OnGoingCondition:" + sp.getBoolean(Utils.OngoingKEY, false) + sp.getBoolean(Utils.LocationUpDateKEY, false));
        }

        //memo: 目的地が途中で変更されたら、onGoingを外す。
        if (Utils.onGoing(getActivity()) && Utils.isEmptyDest(getActivity())) {
            // 1.Serviceをとめる（＝APIを止める）
            mService.removeLocationUpdates();
            // 2.onGoingをとめる
            Utils.setOnGoing(getActivity(), false);
            //setUIState(Utils.requestingLocationUpdates(MainActivity.this));
            // 3.目的地を削除する
            Utils.removeThisDest(getActivity());
            // 4.地図を戻す
            defaultMap();
            // 5.Buttonを戻す
            setUIState(Utils.requestingLocationUpdates(getActivity()), Utils.onGoing(getActivity()));
            Log.d("debug", "4:LocationUpdate,OnGoingCondition:" + sp.getBoolean(Utils.OngoingKEY, false) + sp.getBoolean(Utils.LocationUpDateKEY, false));
        }



/*
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        System.out.println("caller class:" + ste.getClassName() + " metho:" + ste.getMethodName() + " line:" + ste.getLineNumber());
        Map<String,?> map = sp.getAll();
        for( Map.Entry<String, ?> entry : map.entrySet() )
        {
            // キー
            String key = entry.getKey();
            // 値
            Object value = entry.getValue();

            // ログ出力
            String msg=String.format( "%s = %s", key, value );
            Log.v( "LogSample", msg );
        }
*/

    }
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    //memo:要確認これであっているかどうか。
                    getActivity().findViewById(R.id.drawer_layout),
                    R.string.permission_location,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setUIState(false, false);
                Snackbar.make(
                        getActivity().findViewById(R.id.drawer_layout),
                        R.string.permission_location_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    private void setUIState(boolean requestingLocationUpdates, boolean onGoing) {


        if (requestingLocationUpdates) {


            if (Utils.isEmptyUser(getActivity())) {
                //一度クリックしてRequestでた後に、戻るボタンなどで登録せずに戻ってきた状態。）
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("ユーザー登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (Utils.isEmptyDest(getActivity())) {

                //ユーザー登録後の流れとして、このButtonとなる。リセット後はRequestも削除されるのでここにはこない。
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("目的地登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (!onGoing) {

                //出発！
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("出発");
                mRemoveLocationUpdatesButton.setEnabled(true);
                //FirstMap()
                Toast.makeText(getActivity().getApplicationContext(), "目的地をセットしました", Toast.LENGTH_SHORT).show();
            } else {

                //mail送信
                //ActiveMap()
                Toast.makeText(getActivity().getApplicationContext(), "計測開始しました！", Toast.LENGTH_SHORT).show();
                mRequestLocationUpdatesButton.setEnabled(false);
                mRequestLocationUpdatesButton.setText("計測中");
                mRemoveLocationUpdatesButton.setEnabled(true);


            }

            //リセットボタン押したあと.requestingLocationUpdateも削除するため。
        } else {

            if (Utils.isEmptyUser(getActivity())) {

                //
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("ユーザー登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (Utils.isEmptyDest(getActivity())) {

                //リセット後はここにくる。
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("目的地登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else {
                //
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("目的地を設定");
                mRemoveLocationUpdatesButton.setEnabled(false);

            }


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "when do you call me?");
        mMap = googleMap;

        defaultMap();

    }

    private void defaultMap() {

        if (currentMarker != null) {
            currentMarker.remove();
        }

        //memo: リセットボタンを押した時のDefaultMap()に戻す。ここに書いても消えない。。何故か不明。
        if(destmarker != null){
            destmarker.remove();
            mMap.clear();
            Log.d("debug","defaultMap destmarker");
        }

           // destmarker.remove();

        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        LatLng JAPAN = new LatLng(36, 139);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, (float) 4.8));
        Log.d("debug","defaultMap");

    }

    private void firstMap() {
        mMap.clear();
        if (currentMarker != null) {
            currentMarker.remove();
        }


        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        destlatitude = Double.parseDouble(Utils.getDestLatitude(getActivity()));
        destlongitude = Double.parseDouble(Utils.getDestLongitude(getActivity()));
        destname = Utils.getDestName(getActivity());
        latlng = new LatLng(destlatitude, destlongitude);
        setMarker(destlatitude, destlongitude,destname);


        //memo: Permissionを求められる。
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);


        currentlatlng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        currentMarkerOptions.position(currentlatlng);
        currentMarkerOptions.title("現在位置");
        currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currentMarker = mMap.addMarker(currentMarkerOptions);


         mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));

        //memo:　目的地と現在位置に線を引く（Routeでは無いからあんまり意味ない 、この間に移動を感知すると何回も線を引いてしまう。）


        if (polylineFinal != null) {

            polylineFinal.remove();
        }
        options = new PolylineOptions();
        options.add(currentlatlng); //
        options.add(latlng); //
        options.color(0xcc00ffff);
        options.width(10);
        // options.geodesic(true); // 測地線で表示
        polylineFinal = mMap.addPolyline(options);

        //memo:　目的地と現在位置の距離を取る
        float[] results = new float[1];
        Location.distanceBetween(destlatitude, destlongitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
        Toast.makeText(getActivity(), "距離：" + ((Float) (results[0] / 1000)).toString() + "Km", Toast.LENGTH_LONG).show();

        originaldistance = results[0] / 1000;
        Log.d("debug","originaldistance"+originaldistance);

        referencedistance = originaldistance * 0.3;
        Log.d("debug","referencedistance"+referencedistance);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(destmarker.getPosition());
        builder.include(currentMarker.getPosition());
        LatLngBounds bounds = builder.build();
        mMap.setPadding(50, 250, 50, 250); //   left,        top,       right,  bottom
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 120);

        mMap.moveCamera(cu);


    }

    private void activeMap() {
        mMap.clear();
        if (currentMarker != null) {
            currentMarker.remove();

        }

        // 設定の取得
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

       // currentlatitude = mCurrentLocation.getLatitude();
       // currentlongitude = mCurrentLocation.getLongitude();

        //memo: 目的地をセット
        /*
        destlatitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Const.DestLatitudeKEY, ""));
        destlongitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Const.DestLongitudeKEY, ""));
        destname = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Const.DestnameKEY,"");

        latlng = new LatLng(destlatitude, destlongitude);
        setMarker(destlatitude, destlongitude,destname );
        */
        //memo:　現在位置をセット

        //noinspection MissingPermission,ResourceType
        mMap.setMyLocationEnabled(true);
        currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        currentMarkerOptions.position(currentlatlng);
        currentMarkerOptions.title("現在位置");
        currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currentMarker = mMap.addMarker(currentMarkerOptions);

        polylineFinal.remove();

        //memo:　目的地と現在位置の距離を取る
        float[] results = new float[1];
        Location.distanceBetween(destlatitude, destlongitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
        Toast.makeText(getActivity(), "ActiveMap距離：" + ( (Float)(results[0]/1000) ).toString() + "Km", Toast.LENGTH_LONG).show();

        nowdistance = results[0]/1000;

      //  mDestTextView.setText("ActiveMap目的地までの距離：" + nowdistance + "Km");

        zoomMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());


        Log.d("debug", "activeMap"+String.valueOf(referencedistance));
        Log.d("debug", "activeMap"+String.valueOf(nowdistance));
        Log.d("debug", "activeMap"+String.valueOf(nowdistance - referencedistance));

        Toast.makeText(getActivity(),"activeMap_mailcount" + mailCount +"",Toast.LENGTH_LONG).show();

    }


    private void setMarker(double destlatitude, double destlongitude, String destname) {



        destlatlng = new LatLng(destlatitude,destlongitude);
        destMarkerOptions.position(destlatlng);
        destMarkerOptions.title(destname);
        destmarker = mMap.addMarker(destMarkerOptions);

        // ズーム
        //zoomMap(destlatitude, destlongitude);
    }

    private void zoomMap(double destlatitude, double destlongitude) {
        /*
        memo: 1 ドアップ　0.1　何も見えない　10 海？何も見えない　0.9
         */
        double south = destlatitude * (1 - 0.00005);
        double west = destlongitude * (1 - 0.00005);
        double north = destlatitude * (1 + 0.00005);
        double east = destlongitude * (1 + 0.00005);


        // LatLngBounds (LatLng southwest, LatLng northeast)
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(south, west))
                .include(new LatLng(north, east))
                .build();

        Integer width = getResources().getDisplayMetrics().widthPixels;
        Integer height = getResources().getDisplayMetrics().heightPixels;

        // static CameraUpdate.newLatLngBounds(LatLngBounds bounds, int width, int height, int padding)

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0));

    }

    /*
    public void saveOriginaldistancedata(float originaldistance) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        sp.edit().remove("originaldistance").apply();
        //memo: http://qiita.com/usamao/items/d7fbb19b508dc4cb5521
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();

        //memo:Stringとして保存する。
        editor.putString(Const.ODISTANCEKEY , String.valueOf(originaldistance));

        // editor.putFloat(Const.ODISTANCEKEY, originaldistance, -1.00F);
        editor.commit();
        Log.d("originaldestance", "Value"+ originaldistance);
        Log.d("originaldestance", "set"); //FirstMapで何度も呼ばれ無いように１通目のメールに連動させる。
    }*/
}
