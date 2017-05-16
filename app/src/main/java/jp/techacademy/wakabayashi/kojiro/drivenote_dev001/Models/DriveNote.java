package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;


/**
 * Created by wkojiro on 2017/04/20.
 */

public class DriveNote extends Application {

    private static DriveNote sInstance;


    private final String TAG = "DEBUG-APPLICATION";
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        sInstance = this;
    }

    /*

    public static synchronized DriveNote getInstance() {
        return sInstance;
    }

*/
    @Override
    public void onTerminate() {
        super.onTerminate();
        /** This Method Called when this Application finished. */
        Log.v(TAG,"--- onTerminate() in ---");
    }




}
