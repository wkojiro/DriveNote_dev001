package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Utils;

public class ArrivalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());
        finish();
        Intent intent = new Intent(this, MainActivity.class);
       // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }
}
