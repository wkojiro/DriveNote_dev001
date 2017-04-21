package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

/**
 * Created by wkojiro on 2017/04/20.
 */


public class HttpException extends Exception{
    private final int httpCode;
    public HttpException(int httpCode) {
        this.httpCode = httpCode;
    }
}