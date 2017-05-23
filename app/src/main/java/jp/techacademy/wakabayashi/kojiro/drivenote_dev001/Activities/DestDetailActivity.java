package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models.Dest;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.R;

public class DestDetailActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback  {

    private Dest mDest;
    private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);
    //パーツの定義
    private TextView mDestNameText,mDestEmailText,mDestAddressText;
    private Double mDestlatitude,mDestlongitude;
    ImageView tapView,tapView2,tapView3,tapView4;

    GoogleMap mMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_detail);

        //memo: SettingActivityからもらってきたdestID情報（用途：目的地のIDをもらったのでどの目的地の詳細画面かを確認できます。）
        Intent intent = getIntent();
        int destId = intent.getIntExtra(SettingActivity.EXTRA_DEST, -1);
        Log.d("destId",String.valueOf(destId));

        tapView = (ImageView)findViewById(R.id.imageView);
        tapView2 = (ImageView)findViewById(R.id.imageView2);
        tapView3 = (ImageView)findViewById(R.id.imageView3);
        tapView4 = (ImageView)findViewById(R.id.imageView4);

        tapView.setOnClickListener(this);
        tapView2.setOnClickListener(this);
        tapView3.setOnClickListener(this);
        tapView4.setOnClickListener(this);
        // Get the map and register for the ready callback
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        toolbar.setTitle("目的地の詳細");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //memo: Realmを用意（用途：ここでは目的地編集用及び新規登録用）
        Realm realm = Realm.getDefaultInstance();
        mDest = realm.where(Dest.class).equalTo("id", destId).findFirst();
        realm.close();

        //memo: 該当のIDがない場合（新規登録の場合）
        if (mDest == null) {

            //alertで前の画面に戻す！？。

        } else
        //memo: IDがある場合（新規登録の場合）
        {
            mDestNameText = (TextView) findViewById(R.id.destNameText);
            mDestEmailText = (TextView) findViewById(R.id.destEmailText);
            mDestAddressText = (TextView) findViewById(R.id.destAddressText);

            //memo: まず今ある情報を表示しておく。（この時点でrailsidは必要なし）
            mDestNameText.setText(mDest.getDestName());
            mDestEmailText.setText(mDest.getDestEmail());
            mDestAddressText.setText(mDest.getDestAddress());

            mDestlatitude = Double.valueOf(mDest.getDestLatitude());
            mDestlongitude = Double.valueOf(mDest.getDestLongitude());
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "when do you call me?");
        mMap = googleMap;
        LatLng MyDest = new LatLng(mDestlatitude, mDestlongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyDest, (float) 15.8));
        Log.d("debug","defaultMap");
        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        setMarker(mDestlatitude, mDestlongitude, mDest.getDestName());


    }


    private void setMarker(double destlatitude, double destlongitude, String destname) {

        MarkerOptions destMarkerOptions = new MarkerOptions();
        LatLng destlatlng = new LatLng(destlatitude,destlongitude);
        destMarkerOptions.position(destlatlng);
        destMarkerOptions.title(destname);
        Marker destmarker = mMap.addMarker(destMarkerOptions);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                Toast.makeText(this,"Image01",Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageView2:
                Toast.makeText(this,"Image02",Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageView3:
                Toast.makeText(this,"Image03",Toast.LENGTH_SHORT).show();
                break;
            case R.id.imageView4:
                Toast.makeText(this,"Image04",Toast.LENGTH_SHORT).show();
                break;
        }
        popImage((ImageView) v);
    }

    public void popImage(ImageView v){
        ImageView imageView = new ImageView(this);
        Bitmap bitmap = ((BitmapDrawable) v.getDrawable()).getBitmap();

        imageView.setImageBitmap(bitmap);
        // ディスプレイの幅を取得する（API 13以上）
        Display display =  getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        float factor =  width / bitmap.getWidth();
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // ダイアログを作成する
        Dialog dialog = new Dialog(this);
        // タイトルを非表示にする
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(imageView);
        dialog.getWindow().setLayout((int)(bitmap.getWidth()*factor), (int)(bitmap.getHeight()*factor));
        // ダイアログを表示する
        dialog.show();

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
