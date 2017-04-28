package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.fragments.BottomSheetFragment;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.fragments.GmapFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    //alarm Setting for unconnected situation
    private static final int bid1 = 1;

    // The BroadcastReceiver used to listen from broadcasts from the service.
   // private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
   // private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
  //  private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private TextView mTextView;

    private Toolbar mToolbar;
    private int mGenre = 0;

    // Preference elements
    SharedPreferences sp;
    private String email,token,destname,destemail,destaddress,latitude,longitude;

    // Monitors the state of the connection to the service.

    /***** go to fragment
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

    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);




        // ツールバーをアクションバーとしてセット
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを収録する

            }
        });
        */

        Button btnBottomSheetBehavior = (Button)findViewById(R.id.bottomsheet) ;
        btnBottomSheetBehavior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBottomSheetFragment();
            }
        });


        //memo: Login時に保存したユーザーデータを取得
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);


        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        mTextView = (TextView) findViewById(R.id.textView);

        mTextView.setText("ddd");

        // Check that the user hasn't revoked permissions by going to Settings.
        /**** go to fragment
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
        */

        FragmentManager fm = getFragmentManager();
        // fm.beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
        fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        Log.d("Activity","onCreate");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager fm = getFragmentManager();

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            // fm.beginTransaction().replace(R.id, new ImportFragment()).commit();
        } else if (id == R.id.nav_dest) {

            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_note) {

        } else if (id == R.id.logout){

            if(!Utils.isEmptyUser(getApplicationContext())) {
                new ApiBase(MainActivity.this).logoutAsync().onSuccessTask(new Continuation<String, Task<String>>() {
                    @Override
                    public Task<String> then(Task<String> task) throws Exception {


                        return new ApiBase(MainActivity.this).deleteUserdata();
                    }

                }).onSuccess(new Continuation<String, String>() {
                    @Override
                    public String then(Task<String> task) throws Exception {

                        Toast.makeText(MainActivity.this, "ログアウトしました。", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, String.valueOf(Utils.isEmptyUser(MainActivity.this)), Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, String.valueOf(Utils.isEmptyDest(MainActivity.this)), Toast.LENGTH_SHORT).show();
                        //  Utils.isEmptyDest(MainActivity.this);
                        //finish();
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                    @Override
                    public String then(Task<String> task) throws Exception {
                        Log.d("Thread", "LoginActLoginContinuewwith" + Thread.currentThread().getName());

                        //finish();

                        if (task.isFaulted()) {
                            Exception e = task.getError();

                            Log.d("debug2", e.toString());
                            Log.e("hoge", "error", e);
                            //エラー処理

                            Toast.makeText(MainActivity.this, "ログアウトに失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void pendingUpdates() {

        // 時間をセットする
        Calendar calendar = Calendar.getInstance();
        // Calendarを使って現在の時間をミリ秒で取得
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 5秒後に設定
        calendar.add(Calendar.SECOND, 1000); //1000秒（１６分４０秒）

        Intent intent = new Intent(getApplicationContext(), LocationUpdatesService.class);
        PendingIntent pending = PendingIntent.getService(getApplicationContext(), bid1, intent, 0);

        // アラームをセットする
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 100000, pending); //100000(1.67分）
        Log.d("debug", "pendingUpdatesがセットされました");
//１０秒毎


    }

    private void removependingUpdates() {

        Intent intent = new Intent(getApplicationContext(), LocationUpdatesService.class);
        PendingIntent pending = PendingIntent.getService(getApplicationContext(), bid1, intent, 0);
        // アラームを解除する
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(ALARM_SERVICE);
        am.cancel(pending);
        Log.d("debug", "pendingUpdatesはremoveされました");

    }


    private void showBottomSheetFragment(){

        BottomSheetFragment fragmentModalBottomSheet = new BottomSheetFragment();
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }


    /***** go to fragment
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    Log.d("debug","ボタンを押したのでService request");
                    if(Utils.isEmptyUser(getApplicationContext())){

                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);


                    } else if (Utils.isEmptyDest(getApplicationContext())){

                        Intent intent = new Intent(MainActivity.this,
                                SettingActivity.class);
                        startActivity(intent);

                    } else if (!Utils.onGoing(getApplicationContext())){

                        Utils.setOnGoing(MainActivity.this, true);

                    }

                }
            }
        });

    //

    /***** go to fragment
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();

                Utils.setOnGoing(MainActivity.this, false);
                //setUIState(Utils.requestingLocationUpdates(MainActivity.this));
                Utils.deleteThisDest(MainActivity.this);

                Log.d("debug","ボタンを押したのでService remove& 目的地削除");
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setUIState(Utils.requestingLocationUpdates(this),Utils.onGoing(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }
    */ //

    /***** go to fragment
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    */

    /**
     * Returns the current state of the permissions needed.
     */
    /***** go to fragment
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    //memo:要確認これであっているかどうか。
                    findViewById(R.id.drawer_layout),
                    R.string.permission_location,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
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
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

     */


    /**
     * Callback received when a permissions request has been completed.
     */

    /***** go to fragment
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
                        findViewById(R.id.drawer_layout),
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
    */

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */

    /***** go to fragment
    public static class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(context, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();

               // mTextView.setText(Utils.getLocationText(location));
            }
        }
    }
     */


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
        Log.d("debug","MainActivity: PreferenceChanged");



    }


    /***** go to fragment
    private void setUIState(boolean requestingLocationUpdates,boolean onGoing) {


        if(requestingLocationUpdates) {
            if (Utils.isEmptyUser(getApplicationContext())) {
                //ユーザーを登録してください。
                mRequestLocationUpdatesButton.setEnabled(true);
                mRequestLocationUpdatesButton.setText("ユーザー登録");
                mRemoveLocationUpdatesButton.setEnabled(false);

            } else if (Utils.isEmptyDest(getApplicationContext())) {

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
                Toast.makeText(this, "目的地をセットしました", Toast.LENGTH_SHORT).show();
            } else {

                //mail送信
                //ActiveMap()
                Toast.makeText(this, "計測開始しました！", Toast.LENGTH_SHORT).show();
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


        //memo:Serviceも走って、ユーザーもいて、目的地もある状態。しかし、ユーザーのongoingサインはまだ。
        if (requestingLocationUpdates && Utils.onGoing(getApplicationContext())) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else if(Utils.isEmptyUser(getApplicationContext())) {
        //ユーザーを登録してください。
            mRequestLocationUpdatesButton.setEnabled(true);
            mRequestLocationUpdatesButton.setText("ユーザー登録");
            mRemoveLocationUpdatesButton.setEnabled(false);

        } else if(Utils.isEmptyDest(getApplicationContext())) {
        //目的地を登録してください。
            mRequestLocationUpdatesButton.setEnabled(true);
            mRequestLocationUpdatesButton.setText("目的地登録");
            mRemoveLocationUpdatesButton.setEnabled(false);

        } else if(!Utils.onGoing(getApplicationContext())) {

            mRequestLocationUpdatesButton.setEnabled(true);
            mRequestLocationUpdatesButton.setText("計測スタート");
            mRemoveLocationUpdatesButton.setEnabled(false);

        } else {

            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }

    }
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
