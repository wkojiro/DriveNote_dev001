package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;


//Realm関連
import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.ApiBase;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models.Dest;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.DestAdapter;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Utils;

public class SettingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    public final static String EXTRA_DEST = "jp.techacademy.wakabayashi.kojiro.tochaku.DEST";
    private static final int REQUEST_DEST_CODE = 123;


    //パーツの定義
    private TextView mUserNameText;
    private TextView mEmailText;
    private TextView mDestCountText;
    private Button mProfileButton;
    private Button mLogoutButton;

    public Dest destRealm;
    private Realm mRealm;
    private RealmResults<Dest> mDestRealmResults;
    private RealmChangeListener<Realm> mRealmListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {

            //memo: 目的地一覧を取得
            Log.d("Reload","reload");
            reloadListView();
        }
    };


    private ListView mListView;
    //GetのResponseを受けるパラメータ
    private ArrayList<Dest> mDestArrayList;
    private DestAdapter mDestAdapter;

    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    private String apiusername;
    private String apiemail;
    private String apitoken;

    //削除する目的地をいれておく
    private Dest dest;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SettingActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        toolbar.setTitle("目的地設定");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        //memo: Login時に保存したユーザーデータを取得
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        //memo: API用に取得しておく
        apiusername = sp.getString(Utils.UnameKEY, "");
        apiemail = sp.getString(Utils.EmailKEY, "");
        apitoken = sp.getString(Utils.TokenKEY, "");

        //memo: Fixed features

        /*
        mUserNameText = (TextView) findViewById(R.id.userNameText);
        mUserNameText.setText(apiusername);
        mEmailText = (TextView) findViewById(R.id.EmailText);
        mEmailText.setText(apiemail);
        mDestCountText = (TextView) findViewById(R.id.DestsText);
*/


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                //memo: 目的地を追加する

                    Intent intent = new Intent(getApplicationContext(), DestActivity.class);
                    startActivityForResult(intent,REQUEST_DEST_CODE);

            }
        });


        // ListViewの設定
        //
        mDestAdapter = new DestAdapter(this,this);
        mListView = (ListView) findViewById(R.id.listView);
        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                return true;
            }
        });

        //memo: 目的地一覧を取得
        //  new getDestinations().execute();
        getDest();


        //memo: 現在保存されているRealmの中身を取得＆並べ替え
        mRealm = Realm.getDefaultInstance();
        mDestRealmResults = mRealm.where(Dest.class).findAllSorted("id",Sort.DESCENDING);
        //   mDestRealmResults.sort("id",Sort.ASCENDING);


        //memo: リスナーの設定
        mRealm.addChangeListener(mRealmListener);
        Log.d("1.リザルト",String.valueOf(mDestRealmResults.size()));


        // ListViewの設定（器のみ）
        //DestAdapterに何を放り込むか？
        mDestAdapter = new DestAdapter(this,this);
        mListView = (ListView) findViewById(R.id.listView);

        //memo: 機能していない？
        mDestAdapter.notifyDataSetChanged();

        //memo: ListViewをタップしたときの処理(目的地の詳細画面に行く時のリスナー）
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ①入力・編集する画面に遷移させる ②トチェックを表示して、この目的地を選択し、選択されたものはPreferenceに保存される。

                dest = (Dest) parent.getAdapter().getItem(position);

                Log.d("BranchID",String.valueOf(dest.getBranchId()));
                Log.d("id",String.valueOf(dest.getId()));
                Log.d("name",String.valueOf(dest.getDestName()));
                Log.d("Url",String.valueOf(dest.getDestUrl()));

                //memo: destのIDを送る
                Intent intent = new Intent(SettingActivity.this, DestDetailActivity.class);
                intent.putExtra(EXTRA_DEST, dest.getId());
                //startActivity(intent);
                startActivityForResult(intent,REQUEST_DEST_CODE);

            }
        });
/*
        //memo: ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する
                Log.d("タスク","削除");
                //   new delete().execute();

                dest = (Dest) parent.getAdapter().getItem(position);
                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("削除");
                builder.setMessage(dest.getDestName() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Rails側削除
                        //   new deletedest().execute(String.valueOf(dest.getDestUrl()));
                        String url = String.valueOf(dest.getDestUrl());
                        mProgress.show();
                        new ApiBase(SettingActivity.this).deleteDirectionAsync(apiemail,apitoken,url).onSuccessTask(new Continuation<String ,Task<String>>(){
                            @Override
                            public Task<String> then(Task<String> task) throws Exception {
                                return new ApiBase(SettingActivity.this).getDirectionsAsync(apiemail,apitoken);
                            }

                        }).onSuccessTask(new Continuation<String, Task<String>>(){
                            @Override
                            public Task<String> then(Task<String> task) throws Exception{

                                return new ApiBase(SettingActivity.this).saveDestinationdata(task.getResult());
                            }



                        }).onSuccess(new Continuation<String, String>(){
                            @Override
                            public String then(Task<String> task) throws Exception {

                                Toast.makeText(SettingActivity.this,"目的地を削除しました。",Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SettingActivity.this,"目的地の削除に失敗しました。",Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }
                        }, Task.UI_THREAD_EXECUTOR);
                    }
                });
                // アラートダイアログのキャンセルボタンを設定します。nullは何もしない。
                builder.setNegativeButton("CANCEL", null);
                //AlertDialog dialog = builder.create();
                // アラートダイアログを表示します
                builder.show();
                return true;
            }
        });

*/
        reloadListView();

    }
    private void reloadListView() {
        //memo: 現在保存されているRealmの中身を取得＆並べ替え
        mRealm = Realm.getDefaultInstance();

        //memo:昇順降順を逆にしてもCheckboXは感知できるようにIDをみるようにした。
        mDestRealmResults = mRealm.where(Dest.class).findAllSorted("id",Sort.DESCENDING);
        Log.d("ReloadView.リザルト",String.valueOf(mDestRealmResults.size()));

        if(mDestRealmResults.size() != 0) {

            //memo: 仮説：現状だとmDestRealmResultsが変わったあとにListViewに表示している。
            mDestArrayList = new ArrayList<>();

            for (int i = 0; i < mDestRealmResults.size(); i++) {
                if (!mDestRealmResults.get(i).isValid()) continue;

                Dest dest = new Dest();

                dest.setId(mDestRealmResults.get(i).getId());
                dest.setBranchId(mDestRealmResults.get(i).getBranchId());
                dest.setDestName(mDestRealmResults.get(i).getDestName());
                dest.setDestEmail(mDestRealmResults.get(i).getDestEmail());
                dest.setDestAddress(mDestRealmResults.get(i).getDestAddress());
                dest.setDestLatitude(mDestRealmResults.get(i).getDestLatitude());
                dest.setDestLongitude(mDestRealmResults.get(i).getDestLongitude());
                dest.setDestUrl(mDestRealmResults.get(i).getDestUrl());

                mDestArrayList.add(dest);
            }


            mDestAdapter.setDestArrayList(mDestArrayList);
            mListView.setAdapter(mDestAdapter);
            mDestAdapter.notifyDataSetChanged();

        } else {

            Toast.makeText(this, "登録されている目的地がありません。", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("debug","SettingActivity: PreferenceChanged");

       Log.d("debug","isEmptyDest"+Utils.isEmptyDest(this));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getDest(){

        new ApiBase(SettingActivity.this).getDirectionsAsync().onSuccessTask(new Continuation<String, Task<String>>(){
            @Override
            public Task<String> then(Task<String> task) throws Exception {


                return new ApiBase(SettingActivity.this).saveDestinationdata(task.getResult());
            }

        }).onSuccess(new Continuation<String, String>(){
            @Override
            public String then(Task<String> task) throws Exception {

                Toast.makeText(SettingActivity.this,"目的地情報を更新しました。",Toast.LENGTH_SHORT).show();
                //finish();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<String, String>() {
            @Override
            public String then(Task<String> task) throws Exception {
                Log.d("Thread","SettingActivity"+Thread.currentThread().getName());
                mProgress.dismiss();
                //finish();

                if (task.isFaulted()) {
                    Exception e = task.getError();

                    Log.d("debug2",e.toString());
                    Log.e("hoge","error", e);
                    //エラー処理

                    Toast.makeText(SettingActivity.this,"目的地情報の更新に失敗しました。",Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }


    //addというよりselectがニュアンス的に正しい。
    public void addDestination(Integer selected_id) {

        Realm realm = Realm.getDefaultInstance();

        // RailsIdを元に検索をかけている。
        destRealm = realm.where(Dest.class).equalTo("id", selected_id ).findFirst();
        realm.close();

        //memo: 目的地を追加する際にすでにある目的地を消し、その後に追加する。
        Utils.removeThisDest(this);
        Utils.setDestination(this,destRealm.getId(),destRealm.getBranchId(),destRealm.getDestName(),destRealm.getDestAddress(),destRealm.getDestEmail(),destRealm.getDestLatitude(),destRealm.getDestLongitude());

        Toast.makeText(this, "目的地を設定しました", Toast.LENGTH_LONG).show();

        //memo: ここで目的地が変更されるなどした場合に地図などをClear（Reset)にするためのIntentを戻している。
        Intent resultintent = new Intent();
        resultintent.putExtra("Result","OK");
        setResult(RESULT_OK,resultintent);
        finish();

    }
}

