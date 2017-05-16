package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models.Dest;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models.Note;
import jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models.User;
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
    private String baseUrl = "https://rails-sandbox-5-0-wkojiro1.c9users.io/";
    private Integer noteId;



    public ApiBase(Context context) {
        this.context = context;
    }


    /////////////////　ログイン　/////////////////

    public Task<String> loginAsync(String email, String password) {

        Log.d("Thread","LoginAsync"+Thread.currentThread().getName());
        final TaskCompletionSource taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        String urlString = "members/sign_in.json";

        final String json =
                "{\"member\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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
    public Task<String> saveUserdata(String jsonData) {

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

    ///////////////// ログアウト //////////////////

    public Task<String> logoutAsync() {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "members/sign_out.json";
        //  String result = null;

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        final String json =
                "{\"members\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"access_token\":\"" + token + "\"" +
                        "}" +
                 "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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
    public Task<String> deleteUserdata(){

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

    ///////// 目的地の取得　///////////

    public Task<String> getDirectionsAsync(){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);

        String urlString = "mypage/mydestinations.json?email="+ email +"&token="+ token +"";
        // String result = null;

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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

    ///////// 目的地の保存　///////////

    public Task<String> saveDestinationdata(String jsonData){
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


    /////////// 目的地の登録　////////////

    public Task<String> createDirectionAsync(String destname, String destemail, String destaddress) {

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

    /////////// 目的地の編集　////////////

    public Task<String> editDirectionAsync(String destname, String destemail, String destaddress, String url){

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

    /////////// 目的地の削除　////////////

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

    /////////// DriveNoteの作成　////////////

    public Task<String> createDriveNote(Float originaldistance){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);
        Integer mid = Utils.getUid(context);
        Integer did = Utils.getDestId(context);


        String urlString = "members/"+ mid +"/drivenotes.json?email=" + email + "&token=" + token + "";

        String result = null;
        long currenttime = System.currentTimeMillis();

        final String json =
                "{\"drivenote\":{" +
                        "\"originaldistance\":" + originaldistance + "," +
                        "\"start_at\":"+ currenttime +"," +
                        "\"arrived_at\":\" \"," +
                        "\"destination_ids\":[\"\","+ did +"]" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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
                    Log.d("debug", jsonData);

                    taskresult.setResult(jsonData);

                }else{
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }

    /////////// ドライブノートの保存　////////////
    public Task<String> saveDriveNotedata(String jsonData){
        Log.d("Thread","SaveDriveNote"+Thread.currentThread().getName());
        // public void saveUserdata(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        Gson gson = new Gson();
        Note note = new Note();

        note = gson.fromJson(jsonData, Note.class);
        if (note != null) {
            noteId = note.getDrivenoteId();
        }
        Utils.setDrivenoteId(context, noteId);
        taskresult.setResult("OK");
        return taskresult.getTask();
    }

    /////////// Trackingの生成　////////////
    public Task<String> postCurrentlocationData(String nowlatitude, String nowlongitude){
        Log.d("Thread","SaveDriveNote"+Thread.currentThread().getName());
        // public void saveUserdata(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);
        Integer mid = Utils.getUid(context);
        //Integer did = Utils.getDestId(context);
        Integer drivenoteid = Utils.getDrivenoteId(context);
        long currenttime = System.currentTimeMillis();

        String urlString = "members/"+ mid +"/drivenotes/"+ drivenoteid +"/trackings.json?email=" + email + "&token=" + token + "";
      //  https://rails-sandbox-5-0-wkojiro1.c9users.io/members/12/drivenotes/2/trackings.json?email=csvuser07@test.com&token=12:NmAS79RAaTwKzmo8s55P
        String result = null;

        final String json =
                "{\"tracking\":{" +
                        "\"latitude\":" + nowlatitude + "," +
                        "\"longitude\":" + nowlongitude + "," +
                        "\"current_at\":"+ currenttime + "" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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
                    Log.d("debug", jsonData);

                    taskresult.setResult(jsonData);

                }else{
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }

    /////////// DriveNoteの作成　////////////

    public Task<String> arriveDriveNote(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String email = Utils.getLoggedInUserEmail(context);
        String token = Utils.getLoggedInUserToken(context);
        Integer mid = Utils.getUid(context);
        Integer drivenoteid = Utils.getDrivenoteId(context);


        String urlString = "members/"+ mid +"/drivenotes/"+ drivenoteid +".json?email=" + email + "&token=" + token + "";

        String result = null;
        long currenttime = System.currentTimeMillis();


        final String json =
                "{\"drivenote\":{" +
                        "\"arrived_at\":"+ currenttime +"" +
                        "}" +
                 "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(baseUrl+urlString)
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

                if (response.isSuccessful()) {

                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

                    taskresult.setResult(jsonData);

                }else{
                    taskresult.setError(new HttpException(response.code(),response.body().string()));
                }
            }
        });
        // リクエストして結果を受け取って
        Log.d("debug","drivenote finish");
        return taskresult.getTask();
    }

    /////////// メール送信　////////////

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
