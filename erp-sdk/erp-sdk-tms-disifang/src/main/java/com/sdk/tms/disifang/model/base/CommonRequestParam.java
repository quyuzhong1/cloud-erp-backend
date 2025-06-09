package com.sdk.tms.disifang.model.base;

/**
 * @author zdy
 * @ClassName CommonRequestParam

 * @date 2023年11月02日
 * @version: 1.0
 */
public class CommonRequestParam {
    private String method;

    private String appKey;

    private String version;

    private Long timestamp;

    private String format;

    private String sign;

    private String accessToken;

    private String language;



    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String toString() {
        return "CommonRequestParam{method='" + this.method + '\'' + ", appKey='" + this.appKey + '\'' + ", version='" + this.version + '\'' + ", timestamp=" + this.timestamp + ", format='" + this.format + '\'' + ", sign='" + this.sign + '\'' + ", accessToken='" + this.accessToken + '\'' + ", language='" + this.language + '\'' + '}';
    }
}
