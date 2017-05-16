package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wkojiro on 2017/05/12.
 */

public class Note {
//public class Note {
    // id をプライマリーキーとして設定(Jsonで取得したデータをRealmで内部に保存する。）
    @PrimaryKey
    private Integer id;
    private Integer member_id;
    private float originaldistance;
    private String start_at;
    private String arrived_at;
    private String url;


    public int getDrivenoteId() {
        return id;
    }

    public void setDrivenoteId(int id) {
        this.id = id;
    }

    public int getMemberId(){
        return member_id;
    }

    public void setMemberId(int member_id){
        this.member_id = member_id;
    }

    public float getOriginaldistance(){
        return originaldistance;
    }

    public void setOriginaldistance(float originaldistance){ this.originaldistance = originaldistance; }


    public String getStart_at(){
        return start_at;
    }
    public void setStart_at(String start_at){
        this.start_at = start_at;
    }

    public String getArrived_at(){
        return arrived_at;
    }
    public void setArrived_at(String arrived_at){
        this.arrived_at = arrived_at;
    }

    public String getDestUrl(){
        return url;
    }
    public void setDestUrl(String url) {
        this.url = url;
    }

}
