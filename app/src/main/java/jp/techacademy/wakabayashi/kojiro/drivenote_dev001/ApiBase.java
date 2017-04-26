package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
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

public class ApiBase {

    protected final Context context;


    //memo: JsonからUserデータを受け取るために利用している。
    private String user_name;
    private String user_token;
    private String user_id;
    private String user_email;


    public ApiBase(Context context) {
        this.context = context;
    }

    /////////////////　会員登録　/////////////////

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
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }

    /////////////////　ログイン　/////////////////

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
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        return taskresult.getTask();
    }


    /////////////////　ユーザー情報の保存　/////////////////
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
            user_name = user.getUserName();
            user_email = user.getEmail();

        }

        Utils.setLoggedInUser(context, Integer.parseInt(user_id),user_name,user_email,user_token);

        taskresult.setResult("OK");
        //return taskresult.getTask();

        Log.d("debug", "Login/Create:SaveUserInfo success");
        return taskresult.getTask();
    }

    /////////////////

    protected Task<String> logoutAsync() {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_out.json";
        //  String result = null;

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"access_token\":\"" + token + "\"" +
                        "}" +
                        "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .delete(body)
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
                Log.d("debug",response.toString());
                if (response.isSuccessful()) {
                    taskresult.setResult(s);


                    // deleteUserdata();
                } else {
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        return taskresult.getTask();
    }

    //logout
    protected Task<String> deleteUserdata(){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        Utils.removeUserInfo(context);
        Utils.removeThisDest(context);

        Realm mRealm = Realm.getDefaultInstance();
        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();
        taskresult.setResult("OK");
        Log.d("debug","Logout:deleteUserdata_done");

        return taskresult.getTask();

    }


    protected Task<String> getDirectionsAsync(){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ token +"";
        // String result = null;

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                .get()
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
                // String s = "OK";
                if (response.isSuccessful()) {


                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

                    taskresult.setResult(jsonData);

                } else {
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }

    protected Task<String> saveDestinationdata(String jsonData){
        Log.d("Thread","SaveUserdata"+Thread.currentThread().getName());
        // public void saveUserdata(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        JSONArray jsonarray = null;
        try {
            jsonarray = new JSONArray(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonarray.length(); i++) {
            try {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Realm mrealm = Realm.getDefaultInstance();
        Realm mRealm= Realm.getDefaultInstance();
        mRealm.beginTransaction();
        Log.d("デリート前",String.valueOf(mRealm.isEmpty()));
        mRealm.where(Dest.class).findAll().deleteAllFromRealm();
        Log.d("デリート後",String.valueOf(mRealm.isEmpty()));
        mRealm.createOrUpdateAllFromJson(Dest.class,jsonarray); //Realm にそのまま吸い込まれた
        Log.d("後",String.valueOf(mRealm.isEmpty()));
        mRealm.commitTransaction();

        Log.d("debug", "doPost success");


        taskresult.setResult("OK");
        return taskresult.getTask();
    }

    protected Task<String> createDirectionAsync(String destname, String destemail, String destaddress) {


        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email=" + email + "&token=" + token + "";

        String result = null;
        Dest dest = new Dest();
        dest.setDestName(destname);
        dest.setDestEmail(destemail);
        dest.setDestAddress(destaddress);

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"" + destaddress + "\"" +
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
                String s = "OK";

                if (response.isSuccessful()) {
                    taskresult.setResult(s);
                }else{
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }


    protected Task<String> editDirectionAsync(String destname, String destemail, String destaddress, String url){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        String urlString = url +"?email="+ email +"&token="+ token +"";

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"" + destaddress + "\"" +
                        "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .put(body)
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

                if(response.isSuccessful()) {
                    taskresult.setResult(s);
                } else {
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }

            }
        });

        return taskresult.getTask();
    }

    protected Task<String> deleteDirectionAsync(String url){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        String urlString =  url+"?email="+ email +"&token="+ token +"";
        String result = null;

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .delete()
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
                if(response.isSuccessful()) {
                    taskresult.setResult(s);
                } else {
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }

            }
        });


        return taskresult.getTask();
    }

    public Task<String>  postMailAsync(String nowlatitude, String nowlongitude) {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);
        String destname = Utils.getDestName(context);
        String destemail = Utils.getDestEmail(context);

        String urlString = "https://rails5api-wkojiro1.c9users.io/trackings.json?email="+email+"&token="+token+"";

        // String[] params = {"東京駅","wkojiro22@gmail.com","35.681298","139.766247"};

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"\"," +
                        "\"nowlatitude\":\"" + nowlatitude + "\"," +
                        "\"nowlongitude\":\"" + nowlongitude + "\"" +
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
                Log.d("debug", "mail_error"+e);
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                Log.d("debug","response1"+response);
                if (response.isSuccessful()){
                    taskresult.setResult(s);
                }else{
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                    Log.d("debug","response2"+response);
                }
            }
        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }

}
