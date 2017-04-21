package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wkojiro on 2017/04/20.
 */

public class ApiBase implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected final Context context;

    private String user_token;
    private String user_id;
    private String user_email;

    protected ApiBase(Context context) {
        this.context = context;
    }

    protected Task<String> createAccountAsync(String username, String email, String password) {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        String urlString = "https://rails5api-wkojiro1.c9users.io/users.json"; //tocakudemo
        String dammyname = "dammy";

        final String json =
                "{\"user\":{" +
                        "\"username\":\"" + dammyname + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"," +
                        "\"password_confirmation\":\"" + password + "\"" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .post(body)
                .build();

        // クライアントオブジェクトを作って
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    taskresult.setResult(jsonData);
                    Log.d("debug", jsonData);

                }else{
                    taskresult.setError(new HttpException(response.code()));
                }
            }
        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }

    protected Task<String> loginAsync(String email, String password) {

        Log.d("Thread","LoginAsync"+Thread.currentThread().getName());
        final TaskCompletionSource taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .post(body)
                .build();

        // クライアントオブジェクトを作って
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //String s = "OK";
                Log.d("Thread","LoginAsynconResponce"+Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    taskresult.setResult(jsonData);
                }else{
                    taskresult.setError(new HttpException(response.code()));
                }
            }
        });
        return taskresult.getTask();
    }


    //createAccount Or Login
    protected Task<String> saveUserdata(String jsonData) {

        Log.d("Thread","SaveUserdata"+Thread.currentThread().getName());
        // public void saveUserdata(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        Gson gson = new Gson();
        User user = new User();

        user = gson.fromJson(jsonData, User.class);
        if (user != null) {
            user_id = user.getUid();
            user_token = user.getToken();
        //  String user_username = user.getUserName();
            user_email = user.getEmail();

        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , user_id);
        editor.putString(Const.EmailKEY, user_email);
        editor.putString(Const.TokenKey, user_token);

        editor.apply();

        taskresult.setResult("OK");
        //return taskresult.getTask();

        Log.d("debug", "doSaveUserInfo success");
        return taskresult.getTask();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
