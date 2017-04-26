package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

/**
 * Created by wkojiro on 2017/04/20.
 */


public class HttpException extends Exception{
    private final int httpCode;

    public String body;
    public HttpException(int httpCode, String body) {
        super(body);
        this.httpCode = httpCode;
        this.body = body;
    }
}