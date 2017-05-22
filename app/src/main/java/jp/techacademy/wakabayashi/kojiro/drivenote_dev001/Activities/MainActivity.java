package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.support.annotation.NonNull;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.ApiBase;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.DestItem;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.DestRecAdapter;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.LocationUpdatesService;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Utils;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.fragments.FragmentModalBottomSheet;
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
    //   private Button mRequestLocationUpdatesButton;
    //   private Button mRemoveLocationUpdatesButton;

    private FloatingActionButton mRequestLocationUpdatesButton;
    private FloatingActionButton mRemoveLocationUpdatesButton;
    private TextView mTextView, mEmailTextView, mUsernameTextView;
    private ImageView mSignalView01, mSignalView02, mSignalView03, mUserImageView;
    private AppBarLayout mBarLayout, mBarLayout2;
    private Toolbar mToolbar, mToolbar2;
    private NavigationView navigationView;

    private Button mBottomSheetButton;
    private BottomSheetBehavior behavior;

    String red = "#F44336";
    String blue = "#2196F3";
    String yellow = "#FFEB3B";
    private int mGenre = 0;

    // Preference elements
    SharedPreferences sp;
    private String email, token, destname, destemail, destaddress, latitude, longitude;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);

        mBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        Log.d("debug","user"+ Utils.getLoggedInUserEmail(getApplicationContext()));

      //   mEmailTextView.setText(Utils.getLoggedInUserEmail(MainActivity.this));
      //  mUsernameTextView.setText(Utils.getLoggedInUserName(getApplication()));

//memo: 何故INVISIBLEが機能しないか。

     //   mBarLayout2 = (AppBarLayout) findViewById(R.id.appbar2);
     //   mToolbar2 = (Toolbar) findViewById(R.id.toolbar2);

      ///  setupToolbar();
        //setupBottomSheetBehavior();
       // setupRecyclerView();
        setSupportActionBar(mToolbar);

        //memo: Login時に保存したユーザーデータを取得
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (FloatingActionButton)findViewById(R.id.fab_start);
        mRemoveLocationUpdatesButton = (FloatingActionButton)findViewById(R.id.fab_stop);
        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        /*View view=navigationView.inflateHeaderView(R.layout.nav_header_main);*/

        mUsernameTextView = (TextView)header.findViewById(R.id.usernameTextView);
        mEmailTextView = (TextView)header.findViewById(R.id.emailtextView);
        mUsernameTextView.setText(Utils.getLoggedInUserName(getApplicationContext()));

        Log.d("debug","email"+Utils.getLoggedInUserEmail(getApplicationContext()));
        mEmailTextView.setText(Utils.getLoggedInUserEmail(getApplicationContext()));

        if(Utils.isEmptyUser(getApplicationContext())){
            navigationView.getMenu().findItem(R.id.login).setVisible(true);
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
        } else {
            navigationView.getMenu().findItem(R.id.login).setVisible(false);
            navigationView.getMenu().findItem(R.id.logout).setVisible(true);
        }

        mSignalView01 = (ImageView) findViewById(R.id.Signal01);
        // mSignalView02 = (ImageView) getActivity().findViewById(R.id.Signal02);
        // mSignalView03 = (ImageView) getActivity().findViewById(R.id.Signal03);

        mTextView = (TextView) findViewById(R.id.textView);
      //  mTextView.setText("ddd");

        FragmentManager fm = getFragmentManager();
        // fm.beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
        fm.beginTransaction().replace(R.id.content_frame, new GmapFragment()).commit();
        Log.d("Activity","onCreate");

        mBottomSheetButton = (Button)findViewById(R.id.bottomsheet);
        mBottomSheetButton.setText("目的地を設定");

        mBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (!behavior.isHideable()) {
                    showOrHideBottomSheet(false);
                }else{
                    showOrHideBottomSheet(true);
                }*/

                showOrHideBottomSheet(true);

            }
        });
        //Log.d("debug","bottomSheet"+behavior.isHideable());
    }

    @Override
    public void onStart(){
        super.onStart();
      //  Log.d("debug","bottomSheet Activity OnStart"+behavior.isHideable());

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


        } else if (id == R.id.nav_conf) {
            Intent intent = new Intent(getApplicationContext(), ConfigAppActivity.class);
            startActivity(intent);


        } else if (id == R.id.login){
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);

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
                        //finish();
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                    @Override
                    public String then(Task<String> task) throws Exception {
                        Log.d("Thread", "LoginActLoginContinuewwith" + Thread.currentThread().getName());

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

/*
    private void setupBottomSheetBehavior() {

        behavior = BottomSheetBehavior.from(findViewById(R.id.recyclerView));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change

                if (newState == BottomSheetBehavior.STATE_DRAGGING) {

                  //  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //Collapseは本当はButtonのみに反応させたい。地図が動いてしまうので。
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                mRequestLocationUpdatesButton.setEnabled(newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN);
                mRemoveLocationUpdatesButton.setEnabled(newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN);
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("debug","onSlide"+slideOffset);
                if (slideOffset >= 0) {
                    setToolbarTranslationOffset(1.0f - slideOffset);

                }

            }
        });
        showOrHideBottomSheet(false);
    }

    */

/*
    private void showOrHideBottomSheet(boolean show) {
        if (show) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
           // behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            behavior.setHideable(false);

            mBarLayout2.setVisibility(View.VISIBLE);
            mToolbar2.setVisibility(View.VISIBLE);

        } else {
            //bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            behavior.setHideable(true);
            Toast.makeText(this,"bottomsheet.setHideable(true)",Toast.LENGTH_SHORT).show();
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mBarLayout2.setVisibility(View.GONE);
            mToolbar2.setVisibility(View.GONE);


        }
    }
    */

    private void showOrHideBottomSheet(boolean show) {

        if(show){
            FragmentModalBottomSheet fragmentModalBottomSheet = new FragmentModalBottomSheet();
            fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
        }
    }

/*
    private void setupToolbar(){
        // ツールバーをアクションバーとしてセット
        //
        // offset=0: 完全に見せる
        // offset=1: 完全に画面外
        //
        mBarLayout.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.VISIBLE);
        mToolbar2.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom > top) {
                    setToolbarTranslationOffset(0);
                    mBarLayout2.setVisibility(View.VISIBLE);
                    mToolbar2.setVisibility(View.VISIBLE);

                    mToolbar2.setTitle("目的地設定");

                    mToolbar2.removeOnLayoutChangeListener(this);
                } else {
                    setToolbarTranslationOffset(1);
                }
            }
        });
    }
*/

    /*

    private void setToolbarTranslationOffset(float offset) {
        mBarLayout2.setTranslationY(-mBarLayout2.getHeight() * offset);
        mToolbar2.setTranslationY(-mToolbar2.getHeight() * offset);
      //  mToolbar.setVisibility(View.INVISIBLE);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ArrayList<DestItem> data = new ArrayList<>();
        for (int i=0; i<40; i++) {
            data.add(new DestItem(i+1));
        }

        recyclerView.setAdapter(new DestRecAdapter(data));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }*/

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
        Log.d("debug","MainActivity: PreferenceChanged");
        if(Utils.isEmptyUser(getApplicationContext())){
            navigationView.getMenu().findItem(R.id.login).setVisible(true);
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
            mEmailTextView.setText("");
            mUsernameTextView.setText("");
        } else {
            mEmailTextView.setText(Utils.getLoggedInUserEmail(getApplicationContext()));
            mUsernameTextView.setText(Utils.getLoggedInUserName(getApplicationContext()));
            navigationView.getMenu().findItem(R.id.login).setVisible(false);
            navigationView.getMenu().findItem(R.id.logout).setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        //memo:ボトムシートが出ているときは引っ込める
        if(behavior.getState() != BottomSheetBehavior.STATE_HIDDEN){
             showOrHideBottomSheet(false);
            return;
        }
        super.onBackPressed();

    }
}
