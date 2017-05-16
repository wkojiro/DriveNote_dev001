package jp.techacademy.wakabayashi.kojiro.drivenote_dev001.Models;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wkojiro on 2017/04/20.
 */

public class Dest extends RealmObject implements Serializable {

    // id をプライマリーキーとして設定(Jsonで取得したデータをRealmで内部に保存する。）
    @PrimaryKey
    private Integer id;
    private Integer branch_id;
    private String destname;
    private String destemail;
    private String destaddress;
    private String latitude;
    private String longitude;
    private String url;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBranchId(){
        return branch_id;
    }

    public void setBranchId(int branch_id){
        this.branch_id = branch_id;
    }

    public String getDestName(){
        return destname;
    }

    public void setDestName(String destname) {
        this.destname = destname;
    }


    public String getDestEmail(){
        return  destemail;
    }

    public void setDestEmail(String destemail) {
        this.destemail = destemail;
    }

    public String getDestAddress(){
        return destaddress;
    }
    public void setDestAddress(String destaddress) {
        this.destaddress = destaddress;
    }

    public String getDestLatitude(){
        return latitude;
    }
    public void setDestLatitude(String latitude){
        this.latitude = latitude;
    }

    public String getDestLongitude(){
        return longitude;
    }

    public void setDestLongitude(String longitude){
        this.longitude = longitude;
    }

    public String getDestUrl(){
        return url;
    }
    public void setDestUrl(String url) {
        this.url = url;
    }

}
