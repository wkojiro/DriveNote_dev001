package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models;

/**
 * Created by wkojiro on 2017/04/20.
 */

public class User {

    private String id;
    private String membername;
    private String email;
    private String password;
    private String access_token;
    private String created_at;
    private String updated_at;

    public User(){

    }


    public String getUid(){
        return id;
    }
    public String getUserName() {
        return membername;
    }

    public void setUsername(String membername) {
        this.membername = membername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return access_token;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }



}