package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;


/**
 * Created by wkojiro on 2017/04/20.
 */



public class Utils {

   // static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Const.LocationUpDateKEY, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Const.LocationUpDateKEY, requestingLocationUpdates)
                .apply();
    }


    //memo: 外部からアクセスできるようにPublicにしていたらダメだった。staticにしたらできた
    public static boolean isEmptyUser(Context context) {

        return (PreferenceManager.getDefaultSharedPreferences(context).getString(Const.EmailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.TokenKey, "").equals(""));

    }

    public static boolean isEmptyDest(Context context){


        return(PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestnameKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestaddressKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestemailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestLatitudeKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestLongitudeKEY, "").equals(""));


    }

    public static void setOnGoing(Context context, boolean onGoing) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Const.OngoingKEY, onGoing)
                .apply();
    }
    public static boolean onGoing(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Const.OngoingKEY,false);
    }


    public static void deleteThisDest(Context context) {
        //memo: 目的地を追加する際にすでにある目的地を消し、その後に追加する。
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //sp.registerOnSharedPreferenceChangeListener(this);
        sp.edit().remove("id").remove("position_id").remove("destname").remove("destaddress").remove("destemail").remove("latitude").remove("longitude").apply();

    }

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
}

