package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by wkojiro on 2017/04/20.
 */



public class Utils {

    // static final => クラス定数（再代入できない）何故これを定義するのか、よく分からない。
    public static final String LocationUpDateKEY = "locaction_updates";
    public static final String UidKEY = "id";
    public static final String UnameKEY = "membername"; //Preferenceにusernameを保存する時のキー
    public static final String EmailKEY = "email"; // PreferenceにEmailを保存する時のキー
    public static final String TokenKEY = "access_token"; // PreferenceにTokenを保存する時のキー
    public static final String RailsKEY = "id";// PreferenceにDestのIDを保存する時のキー
    public static final String BranchidKEY = "branch_id";
    public static final String DestnameKEY = "destname";
    public static final String DestemailKEY = "destemail";
    public static final String DestaddressKEY = "destaddress";
    public static final String DestLatitudeKEY = "latitude";
    public static final String DestLongitudeKEY = "longitude";
    public static final String OngoingKEY = "ongoing";
    public static final String LoginKEY = "login";
    public static final String ArrivalKEY = "arrived";

    public static final String DriveNoteIDKEY = "drivenote_id";
    public static final String ODISTANCEKEY = "originaldistance";



    //memo:　ask RequestLocationUpdates
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LocationUpDateKEY, false);
    }

    //memo: Locationupdat　が走っていたらTrue
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LocationUpDateKEY, requestingLocationUpdates)
                .apply();
    }


    //memo: ユーザー登録(useridは本当はいらないかもしれない）
    public static void setLoggedInUser(Context context, int userid , String username , String email , String token){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(UidKEY,userid)
                .putString(UnameKEY,username)
                .putString(EmailKEY,email)
                .putString(TokenKEY,token)
                .apply();
    }

    //memo: ドライブノートID情報取得(ID)
    public static Integer getDrivenoteId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(DriveNoteIDKEY,-1);
    }

    //memo: ドライブノートIDの保存
    public static void setDrivenoteId(Context context, int drivenote_id){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(DriveNoteIDKEY,drivenote_id)
                .apply();
    }

    //memo:ユーザーのログアウト
    public static void removeDrivenoteId(Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(DriveNoteIDKEY)
                .commit();
    }

    //memo: ユーザーID情報取得(ID)
    public static Integer getUid(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(UidKEY,-1);
    }

    //memo: ユーザー情報取得(Email)
    public static String getLoggedInUserEmail(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(EmailKEY,"");
    }

    //memo: ユーザー情報取得(Token)
    public static String getLoggedInUserToken(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(TokenKEY,"");
    }

    //memo: ユーザー情報取得(Username)
    public static String getLoggedInUserName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(UnameKEY,"");
    }


    //memo:ユーザーの存在確認（NewVersion)
    public static void setUserLoginStatus(Context context, boolean status){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LoginKEY, status)
                .apply();
    }

    public boolean getUserStatus(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LoginKEY,false);
    }

    //memo:ユーザーのログアウト
    public static void removeUserInfo(Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(UnameKEY)
                .remove(EmailKEY)
                .remove(TokenKEY)
                .apply();
    }


    //memo:  選択された目的地の登録
    //IMPORTANT commit needed
    public static void setDestination
    (Context context, Integer railsid ,Integer positionid,String destname, String destaddress , String destemail, String destlatitude, String destlongitude){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(RailsKEY, railsid)
                .putInt(BranchidKEY, positionid)
                .putString(DestnameKEY, destname)
                .putString(DestaddressKEY, destaddress)
                .putString(DestemailKEY, destemail)
                .putString(DestLatitudeKEY,destlatitude)
                .putString(DestLongitudeKEY,destlongitude)
                .commit();
    }

    //memo:
    public static void removeThisDest(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(RailsKEY)
                .remove(BranchidKEY)
                .remove(DestnameKEY)
                .remove(DestaddressKEY)
                .remove(DestemailKEY)
                .remove(DestLatitudeKEY)
                .remove(DestLongitudeKEY)
                .commit();
    }

    //memo:RailsIDの取得
    public static Integer getDestId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(RailsKEY,-1);
    }

    //memo:
    public static String getDestName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DestnameKEY,"");
    }

    public static String getDestAddress(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DestaddressKEY,"");
    }

    public static String getDestEmail(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DestemailKEY,"");
    }

    public static String getDestLatitude(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DestLatitudeKEY,"");
    }

    public static String getDestLongitude(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DestLongitudeKEY,"");
    }




    //memo:　ユーザー存在確認
    public static boolean isEmptyUser(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(EmailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(TokenKEY, "").equals("");
    }

    //memo:　目的地の存在確認
    public static boolean isEmptyDest(Context context){

        return PreferenceManager.getDefaultSharedPreferences(context).getString(DestnameKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(DestaddressKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(DestemailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(DestLatitudeKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(DestLongitudeKEY, "").equals("");

    }

    //memo:　計測中かどうかの設定
    public static void setOnGoing(Context context, boolean onGoing) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(OngoingKEY, onGoing)
                .apply();
    }

    public static boolean onGoing(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OngoingKEY,false);
    }


    public static void removeOriginalDistance(Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(ODISTANCEKEY)
                .apply();


    }
    //memo: Floatとして保存するとうまくいかないので、Stringで保存して使う時にFloatにする。

    public static void setOriginalDistance(Context context, String originaldistance){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(ODISTANCEKEY, originaldistance)
                .apply();

    /*
    public static void setOriginalDistance(Context context, float originaldistance){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(ODISTANCEKEY, originaldistance)
                .apply();
    */


    }

    public static String getOriginalDistance(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ODISTANCEKEY,"");

    }


/* onGoingと機能が被ると思うので削除
    public static void setArrivalkey(Context context, Boolean arrival ){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ArrivalKEY, arrival)
                .apply();
    }

    public static boolean getArrival(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ArrivalKEY,false);
    }
*/
    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static void locationdataalert(Context context){
        new AlertDialog.Builder(context)
                .setTitle("お知らせ")
                .setMessage("通信状況を確認してください。")
                .setPositiveButton("OK", null)
                .show();
    }

    static void resetApp(Context context) {

        //service を止める
        //Destinationを削除する
        Intent intent = new Intent(context, LocationUpdatesService.class);
        context.stopService(intent);
        setRequestingLocationUpdates(context,false);
        removeThisDest(context);

        Toast.makeText(context,"リセットしました。",Toast.LENGTH_SHORT).show();
        Log.d("debug","resetApp");

    }


}

