package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

public class DestActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener  {

    private Dest mDest;

    //パーツの定義
    private EditText mDestNameText;
    private EditText mDestEmailText;
    private EditText mDestAddressText;
    private Button createButton;

    //パーツから受け取るためのパラメータ
    private Dest dest;
    private String destname;
    private String destemail;
    private String destaddress;
    private String desturl;




    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    private String username;
    private String email;
    private String access_token;

    //preferenceから取得用
    private String name;
    private String address;
    private String demail;
    private String dlatitude;
    private String dlongitude;






    private String result = "";


    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更", "Destに書かれているLogです。");


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest);

        setTitle("目的地の追加");

        //memo: 保存されているユーザー情報をあらかじめ取得しておく。API用
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        username = sp.getString(Utils.UnameKEY, "");
        email = sp.getString(Utils.EmailKEY, "");
        access_token = sp.getString(Utils.TokenKey, "");


        //memo: 保存されている目的地情報をあらかじめ取得しておく。（用途：　　　）
        name = sp.getString(Utils.DestnameKEY, "");
        address = sp.getString(Utils.DestaddressKEY, "");
        demail = sp.getString(Utils.DestemailKEY,"");
        dlatitude = sp.getString(Utils.DestLatitudeKEY,"");
        dlongitude = sp.getString(Utils.DestLongitudeKEY,"");

        Log.d("user name",String.valueOf(username));
        Log.d("Email",String.valueOf(username));
        Log.d("トークン",String.valueOf(access_token));
        Log.d("name",String.valueOf(name));
        Log.d("address",String.valueOf(address));
        Log.d("dlatitude",String.valueOf(dlatitude));

        //memo: SettingActivityからもらってきたdestID情報（用途：目的地編集用）
        Intent intent = getIntent();
        int destId = intent.getIntExtra(SettingActivity.EXTRA_DEST, -1);
        Log.d("destId",String.valueOf(destId));

        //memo: SettubgActivityに戻るためのIntent
        //  Intent backintent = new Intent();



        //memo: Realmを用意（用途：ここでは目的地編集用及び新規登録用）
        Realm realm = Realm.getDefaultInstance();
        mDest = realm.where(Dest.class).equalTo("id", destId).findFirst();
        realm.close();


        //memo: 該当のIDがない場合（新規登録の場合）
        if (mDest == null) {
            mDestNameText = (EditText) findViewById(R.id.destNameText);
            mDestEmailText = (EditText) findViewById(R.id.destEmailText);
            mDestAddressText = (EditText) findViewById(R.id.destAddressText);
        } else
        //memo: IDがある場合（新規登録の場合）
        {
            mDestNameText = (EditText) findViewById(R.id.destNameText);
            mDestEmailText = (EditText) findViewById(R.id.destEmailText);
            mDestAddressText = (EditText) findViewById(R.id.destAddressText);

            //memo: まず今ある情報を表示しておく。（この時点でrailsidは必要なし）
            mDestNameText.setText(mDest.getDestName());
            mDestEmailText.setText(mDest.getDestEmail());
            mDestAddressText.setText(mDest.getDestAddress());
        }


        createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if(mDest == null) {
                    //新規登録
                    destname = mDestNameText.getText().toString();
                    destemail = mDestEmailText.getText().toString();
                    destaddress = mDestAddressText.getText().toString();

                    if (destname.length() != 0 && destemail.length() != 0 && destaddress.length() != 0) {

                        // new DestActivity.createDestination().execute(destname,destemail,destaddress);


                        mProgress.show();


                        new ApiBase(DestActivity.this).createDirectionAsync(destname,destemail,destaddress).onSuccessTask(new Continuation<String ,Task<String>>(){
                            @Override
                            public Task<String> then(Task<String> task) throws Exception {


                                return new ApiBase(DestActivity.this).getDirectionsAsync();
                            }

                        }).onSuccessTask(new Continuation<String, Task<String>>(){
                            @Override
                            public Task<String> then(Task<String> task) throws Exception{


                                return new ApiBase(DestActivity.this).saveDestinationdata(task.getResult());
                            }



                        }).onSuccess(new Continuation<String, String>(){
                            @Override
                            public String then(Task<String> task) throws Exception {

                                Toast.makeText(DestActivity.this,"目的地情報を更新しました。",Toast.LENGTH_SHORT).show();
                                finish();
                                return null;
                            }
                        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                            @Override
                            public String then(Task<String> task) throws Exception {
                                Log.d("Thread","LoginActLoginContinuewwith"+Thread.currentThread().getName());
                                mProgress.dismiss();
                                //finish();

                                if (task.isFaulted()) {
                                    Exception e = task.getError();

                                    Log.d("debug2",e.toString());
                                    Log.e("hoge","error", e);
                                    //エラー処理

                                    Toast.makeText(DestActivity.this,"目的地情報を更新しました。",Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }
                        }, Task.UI_THREAD_EXECUTOR);


                    } else {
                        Log.d("目的地登録エラー", "ddd");
                        // エラーを表示する
                        Snackbar.make(v, "目的地の情報が正しく入力されていません", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    //更新登録
                    Log.d("更新登録", "ddd");

                    destname = mDestNameText.getText().toString();
                    destemail = mDestEmailText.getText().toString();
                    destaddress = mDestAddressText.getText().toString();
                    desturl = mDest.getDestUrl();
                    Log.d("更新登録", desturl);
                    //    new DestActivity.editDestination().execute(destname,destemail,destaddress,desturl);


                    new ApiBase(DestActivity.this).editDirectionAsync(destname, destemail, destaddress, desturl).onSuccessTask(new Continuation<String ,Task<String>>(){
                        @Override
                        public Task<String> then(Task<String> task) throws Exception {


                            return new ApiBase(DestActivity.this).getDirectionsAsync();
                        }

                    }).onSuccessTask(new Continuation<String, Task<String>>(){
                        @Override
                        public Task<String> then(Task<String> task) throws Exception{


                            return new ApiBase(DestActivity.this).saveDestinationdata(task.getResult());
                        }



                    }).onSuccess(new Continuation<String, String>(){
                        @Override
                        public String then(Task<String> task) throws Exception {

                            Toast.makeText(DestActivity.this,"目的地情報を更新しました。",Toast.LENGTH_SHORT).show();
                            // finish();
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
                        @Override
                        public String then(Task<String> task) throws Exception {
                            Log.d("Thread","LoginActLoginContinuewwith"+Thread.currentThread().getName());
                            mProgress.dismiss();
                            //finish();

                            if (task.isFaulted()) {
                                Exception e = task.getError();

                                Log.d("debug2",e.toString());
                                Log.e("hoge","error", e);
                                //エラー処理

                                Toast.makeText(DestActivity.this,"目的地情報を更新しました。",Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);



                }
            }

        });
    }
}
