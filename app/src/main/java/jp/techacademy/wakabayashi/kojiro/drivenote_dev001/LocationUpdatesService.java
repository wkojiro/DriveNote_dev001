package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import bolts.Continuation;
import bolts.Task;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities.ArrivalActivity;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities.MainActivity;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities.SettingActivity;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PACKAGE_NAME =
            "jp.techacademy.wakabayashi.kojiro.drivenote_dev001";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    static final String EXTRA_DESTANCE = PACKAGE_NAME + ".destance";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    private Handler mServiceHandler;

    /**
     * The current location.
     */


    private Float distance;


    //memo: preferenceから現在登録されているユーザーを受け取る為の変数
    String username;
    String email;
    String token;


    //memo: preferenceから現在登録されている目的地を受け取る為の変数
    String address;
    String latitude; //StringにしているけどFloat
    String longitude;//StringにしているけどFloat
    String destname;
    String destemail;
    float originaldistance;
    private Double destlatitude, destlongitude,currentlatitude,currentlongitude;
    private LatLng latlng;

    Float nowdistance;

    Integer mailCount = 0;
    Integer arraivalCount = 0;

    protected Location mCurrentLocation;
    protected LatLng currentlatlng;


    /**
     * The current location.
     */
    private Location mLocation;


    private SoundPool mSoundPool;
    private int mSoundId;



    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 予め音声データを読み込む
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundId = mSoundPool.load(getApplicationContext(), R.raw.arraived01, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        mGoogleApiClient.disconnect();
        // リリース
        mSoundPool.release();
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, LocationUpdatesService.this);
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,LocationUpdatesService.this);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        //memo: ここでActivityに戻れるが、振り出しに戻ってしまう。（対策として下記を実装）
        Intent MainIntent = new Intent(this,MainActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        return new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // In this example, we merely log the suspension.
        Log.e(TAG, "GoogleApiClient connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // In this example, we merely log the failure.
        Log.e(TAG, "GoogleApiClient connection failed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);

        //memo: onLocationChangedが呼ばれるたびに呼ばれる。（目的地があれば現在位置との距離を測れる）
        if (!Utils.isEmptyDest(this)) {
            //memo: 目的地がないと落ちそう。
            Double distance = getDistance(location);
            Log.d("debug","呼ばれるたびにgetDistance");
            //memo: 目的地がないと落ちそう。この設定だと目的地があれば何度もメールを送ることになる。取り合えずこのままにしておく。

            //memo: DriveNoteIDの保存前に発火する分はスルーする。
            if(Utils.getDrivenoteId(getApplicationContext()) != -1) {

                if (Utils.onGoing(getApplicationContext())) {
                    postCurrentPosition(mLocation);
                }
            }

            intent.putExtra(EXTRA_DESTANCE, distance);
        }
        intent.putExtra(EXTRA_LOCATION, location);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
    /*
        Log.d("service","onSharedPreferenceChanged");
        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");

        destname = sp.getString(Const.DestnameKEY, "");
        address = sp.getString(Const.DestaddressKEY, "");
        destemail = sp.getString(Const.DestemailKEY, "");
        latitude = sp.getString(Const.DestLatitudeKEY, "");
        longitude = sp.getString(Const.DestLongitudeKEY, "");


        //originaldistance = sp.getFloat(Const.ODISTANCEKEY, -1.00F);

        Log.d("debug", latitude);
        //本来はoriginaldestanceが欲しい

        /*
        if (!Utils.isEmptyDest(this)) {
            destlatitude = Double.parseDouble(latitude);
            destlongitude = Double.parseDouble(longitude);

            latlng = new LatLng(destlatitude, destlongitude);
            Log.d("debug", "onSharedPreferenceChangedListner_setMarkerが呼ばれる");
            // 標準のマーカー
            //setMarker(destlatitude, destlongitude);


            //originaldistanceがすんなり取れるとは思えない。
            if(sp.getString(Const.ODISTANCEKEY, "") != "") {
                originaldistance = Float.parseFloat(sp.getString(Const.ODISTANCEKEY, ""));
                Log.d("debug", "onSharedPreferenceChangedListnerでoriginaldistanceが保存された");
            }
        }

    */
    }


    //memo:　目的地と現在位置の距離を取る（onLocationChangedが呼ばれるたびに計算する）
    private Double getDistance(Location location){

        currentlatitude = location.getLatitude();
        currentlongitude = location.getLongitude();


        email = Utils.getLoggedInUserEmail(getApplicationContext());
        token = Utils.getLoggedInUserToken(getApplicationContext());
        destlatitude = Double.parseDouble(Utils.getDestLatitude(getApplicationContext()));
        destlongitude = Double.parseDouble(Utils.getDestLongitude(getApplicationContext()));
        destname = Utils.getDestName(getApplicationContext());
        destemail = Utils.getDestEmail(getApplicationContext());


        latlng = new LatLng(destlatitude, destlongitude);

        float[] results = new float[1];
        Location.distanceBetween(destlatitude, destlongitude, currentlatitude, currentlongitude, results);
        nowdistance = results[0]/1000;
        Log.d("debug","getDistance_method"+nowdistance);

        Double result = Double.parseDouble(nowdistance.toString());
        Log.d("debug","getDistance_method"+result);

        return result;
    }


    private void postCurrentPosition(Location location){

        final String nowlatitude = String.valueOf(location.getLatitude());
        final String nowlongitude = String.valueOf(location.getLongitude());

        new ApiBase(getApplicationContext()).postCurrentlocationData(nowlatitude,nowlongitude).onSuccess(new Continuation<String, String>() {
            @Override
            public String then(Task<String> task) throws Exception {
                Log.d("debug","LocationUpdate"+nowlongitude);

                Toast.makeText(getApplicationContext(),"位置情報の更新に成功しました。",Toast.LENGTH_SHORT).show();

                originaldistance = Float.parseFloat(Utils.getOriginalDistance(getApplicationContext()));

                double referencedistance = originaldistance * 0.3;
                Log.d("debug", "基準距離" + originaldistance);
                Log.d("debug", "閾値" + referencedistance);
                Log.d("debug", "現在の距離" + nowdistance);

                double ans = nowdistance - referencedistance;

                if(ans <= 0){
                    Log.d("debug","0以下");
                }

                Log.d("debug","answer"+ans);

                destlatitude = Double.parseDouble(Utils.getDestLatitude(getApplicationContext()));
                destlongitude = Double.parseDouble(Utils.getDestLongitude(getApplicationContext()));

                if (nowdistance <= 0.08 && !Utils.getArrival(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "お疲れ様でした。到着しました", Toast.LENGTH_LONG).show();
                    finishDrivenote();
                    Utils.setArrivalkey(getApplicationContext(),true);

                    mSoundPool.play(mSoundId, 1.0F, 1.0F, 0, 0, 1.0F);
                }



                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
            @Override
            public String then(Task<String> task) throws Exception {
                Log.d("Thread","postCurrentPosition"+Thread.currentThread().getName());

                //finish();
                if (task.isFaulted()) {
                    Exception e = task.getError();
                    Log.d("debug2",e.toString());
                    Log.e("hoge","error", e);
                    //エラー処理
                    Toast.makeText(getApplicationContext(),"位置情報の更新に失敗しました。",Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


/*
    private void calMyPosition(Double nowdistance) {

        originaldistance = Float.parseFloat(Utils.getOriginalDistance(getApplicationContext()));

        double referencedistance = originaldistance * 0.3;
        Log.d("debug", "基準距離" + originaldistance);
        Log.d("debug", "閾値" + referencedistance);
        Log.d("debug", "現在の距離" + nowdistance);

        double ans = nowdistance - referencedistance;

        if(ans <= 0){
            Log.d("debug","0以下");
        }

        Log.d("debug","answer"+ans);

        destlatitude = Double.parseDouble(Utils.getDestLatitude(getApplicationContext()));
        destlongitude = Double.parseDouble(Utils.getDestLongitude(getApplicationContext()));

     //   if (nowdistance - referencedistance <= 0 && mailCount == 0) {

        if(ans <= 0 && mailCount == 0){

            Log.d("debug", "メールまでもう少し" +nowdistance);

            String nowlatitude = String.valueOf(currentlatitude);
            String nowlongitude = String.valueOf(currentlongitude);

            new ApiBase(getApplicationContext()).postMailAsync(nowlatitude, nowlongitude)
                    .onSuccess(new Continuation<String, String>() {
                        @Override
                        public String then(Task<String> task) throws Exception {
                            Toast.makeText(getApplicationContext(), "メール送信しました。", Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                @Override
                public String then(Task<String> task) throws Exception {
                    Log.d("Thread", "LoginActLoginContinuewwith" + Thread.currentThread().getName());

                    if (task.isFaulted()) {
                        Toast.makeText(getApplicationContext(), "メール送信に失敗しました。", Toast.LENGTH_SHORT).show();
                        Log.d("debug", "70%メール送信していません。");
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
            Toast.makeText(this, "全行程の７０％を通過しました。", Toast.LENGTH_LONG).show();

            mailCount = 1;

        }

        if (nowdistance <= 0.08 ) {
            Toast.makeText(getApplicationContext(), "お疲れ様でした。到着しました", Toast.LENGTH_LONG).show();
            finishDrivenote();
        }
    }
    */

    private void finishDrivenote(){
        new ApiBase(getApplicationContext()).arriveDriveNote().onSuccess(new Continuation<String, String>() {
            @Override
            public String then(Task<String> task) throws Exception {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    Utils.resetApp(getApplicationContext());
                    }
                }, 10000);

                Intent intent = new Intent().setClass(getApplicationContext(), ArrivalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

// Launch the new activity and add the additional flags to the intent
                getApplicationContext().startActivity(intent);

                Log.d("debug","finishDrivenote");
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
            @Override
            public String then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    Exception e = task.getError();
                    Log.d("debug2", e.toString());
                    Log.e("hoge", "error", e);
                    Log.d("debug", "PostMail : " + task);
                    Toast.makeText(getApplicationContext(), "DriveNoteの更新にしっぱいしました。", Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }

}
