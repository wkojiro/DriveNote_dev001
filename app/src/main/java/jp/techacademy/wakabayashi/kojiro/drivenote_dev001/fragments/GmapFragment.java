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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

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

public class GmapFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

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
   // private TextView mTextView;
    private String email,token,destname,destemail,destaddress,latitude,longitude;


    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d("debug","Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.d("debug","Service DisConnected");
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(getActivity())) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }



    }
    public static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {


               // Toast.makeText(context,Utils.getLocationText(location),Toast.LENGTH_SHORT).show();

                //mTextView.setText(Utils.getLocationText(location));
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("Fragment","MapFragment onCreateView");
        return inflater.inflate(R.layout.fragment_map, container, false);




        /*
        public View inflate (int resource, ViewGroup root, boolean attachToRoot)
        true:rootで指定したものをルートにする
        false:rootで指定したものをルートにしない

        さて、この場合はrootをどこでどのように指定しているか、
         */


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        //memo: locationUpdateのrequestがされたら、Buttonを変更する。
        //memo: ここのFalseはあくまでもDefault値なので、結果では無い。
        if (s.equals(Const.LocationUpDateKEY)) {
          //  setUIState(sp.getBoolean(Const.LocationUpDateKEY,false),sp.getBoolean(Const.OngoingKEY,false));
            Log.d("debug","1:LocationUpdate,OnGoingCondition:"+sp.getBoolean(Const.OngoingKEY,false)+sp.getBoolean(Const.LocationUpDateKEY,false));

        }
        if (s.equals(Const.OngoingKEY)) {
          //  setUIState(sp.getBoolean(Const.LocationUpDateKEY,false),sp.getBoolean(Const.OngoingKEY,false));
            Log.d("debug","2:LocationUpdate,OnGoingCondition:"+sp.getBoolean(Const.OngoingKEY,false)+sp.getBoolean(Const.LocationUpDateKEY,false));

        }

        email = sp.getString(Const.EmailKEY, "");
        token = sp.getString(Const.TokenKey, "");
        //memo: 現在設定されている目的地の取得


        destname = sp.getString(Const.DestnameKEY, "");
        destaddress = sp.getString(Const.DestaddressKEY, "");
        destemail = sp.getString(Const.DestemailKEY, "");
        latitude = sp.getString(Const.DestLatitudeKEY, "");
        longitude = sp.getString(Const.DestLongitudeKEY, "");


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        Toast.makeText(getActivity(),"OnViewCreated",Toast.LENGTH_SHORT).show();



    }


    @Override
    public void onStart(){
        super.onStart();
        Toast.makeText(getActivity(),"OnStart",Toast.LENGTH_SHORT).show();

        //memo: **** check this point ***
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);

        mRequestLocationUpdatesButton = (Button) getActivity().findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) getActivity().findViewById(R.id.remove_location_updates_button);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    Log.d("debug","ボタンを押したのでService request");
                    if(Utils.isEmptyUser(getActivity())){

                        Intent intent = new Intent(getActivity(),LoginActivity.class);
                        startActivity(intent);


                    } else if (Utils.isEmptyDest(getActivity())){

                        Intent intent = new Intent(getActivity(),SettingActivity.class);
                        startActivity(intent);

                    } else if (!Utils.onGoing(getActivity())){

                        Utils.setOnGoing(getActivity(), true);
                        setUIState(Utils.requestingLocationUpdates(getActivity()),Utils.onGoing(getActivity()));

                    }

                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();

                Utils.setOnGoing(getActivity(), false);
                //setUIState(Utils.requestingLocationUpdates(MainActivity.this));
                Utils.deleteThisDest(getActivity());
                setUIState(Utils.requestingLocationUpdates(getActivity()),Utils.onGoing(getActivity()));

                Log.d("debug","ボタンを押したのでService remove& 目的地削除");
            }
        });
        // Restore the state of the buttons when the activity (re)launches.
        setUIState(Utils.requestingLocationUpdates(getActivity()),Utils.onGoing(getActivity()));
        Log.d("debug","getActivity().getApplicationContext()"+getActivity().getApplicationContext());
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        getActivity().bindService(new Intent(getActivity(), LocationUpdatesService.class), mServiceConnection,Context.BIND_AUTO_CREATE);



    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
        super.onPause();
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
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }



    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getActivity(),
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
                setUIState(false,false);
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


    private void setUIState(boolean requestingLocationUpdates,boolean onGoing) {


        if (requestingLocationUpdates) {
            if (Utils.isEmptyUser(getActivity())) {
                //ユーザーを登録してください。
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("ユーザー登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (Utils.isEmptyDest(getActivity())) {

                //目的地を登録してください。
                //リセット後はここに戻る。
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("目的地登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (!onGoing) {

                //出発！
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("出発");
                mRemoveLocationUpdatesButton.setEnabled(false);
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

            //リセットボタン押したあと
        } else {

            //DefalutMap()
            mRequestLocationUpdatesButton.setEnabled(true);
            mRequestLocationUpdatesButton.setText("目的地を設定");
            mRemoveLocationUpdatesButton.setEnabled(false);


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "when do you call me?");
        mMap = googleMap;

        defaultMap();



    }

    private void defaultMap() {

        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        LatLng JAPAN = new LatLng(36, 139);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, (float) 4.8));
    }

}
