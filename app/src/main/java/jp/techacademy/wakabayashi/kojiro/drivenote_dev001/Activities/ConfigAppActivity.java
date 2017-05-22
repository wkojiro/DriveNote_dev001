package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Utils;

public class ConfigAppActivity extends AppCompatActivity {

    private TextView mTextViewTitle,mTextViewSummay;
    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_app);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextViewTitle = (TextView) findViewById(R.id.textViewTitle);
        mTextViewSummay = (TextView) findViewById(R.id.textViewSummary);
        mSwitch = (Switch) findViewById(R.id.switch_arrival_music);

        mTextViewTitle.setText("到着時メロディ");

        if(Utils.getArrivalMusicConfig(getApplicationContext())){
            mTextViewSummay.setText("ON");
            mSwitch.setChecked(Utils.getArrivalMusicConfig(getApplicationContext()));
        } else {
            mTextViewSummay.setText("OFF");
        }

        mSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((Switch) v).isChecked()) {
                    Utils.setArrivalmusickey(getApplicationContext(),true);
                    mTextViewSummay.setText("ON");
                }
                else {
                    Utils.setArrivalmusickey(getApplicationContext(), false);
                    mTextViewSummay.setText("OFF");
                }

            }
        });

        /*
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    Utils.setArrivalmusickey(getApplicationContext(),true);
                    mTextViewSummay.setText("ON");
                }else{

                    Utils.setArrivalmusickey(getApplicationContext(),true);
                    mTextViewSummay.setText("ON");
                }

            }
        });
        */


        //
        toolbar.setTitle("アプリの設定");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
