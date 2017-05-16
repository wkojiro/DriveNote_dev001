package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities.MainActivity;

/**
 * Created by wkojiro on 2017/05/15.
 */

public class SplashScreen extends Activity {

    // Splash screen timer

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){

            @Override
            public void run(){
                Intent i = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        },SPLASH_TIME_OUT);




    }

}
