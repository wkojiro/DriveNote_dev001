package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.Application;
import io.realm.Realm;


/**
 * Created by wkojiro on 2017/04/20.
 */

public class DriveNote extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);


    }

}
