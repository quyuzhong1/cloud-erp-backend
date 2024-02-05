package com.sdk.third.lingxing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    private String accessToken;
    private String refreshToken;
    private String expiresIn;

    public Token() {
        log.info("有参构造函数");
        this.accessToken = "hxh";
        this.refreshToken = "lzh";
        this.expiresIn = "11";
    }

    public Token(boolean inner) {
        log.info("有参构造函数");
        if(inner){
            this.accessToken = "hxh";
            this.refreshToken = "lzh";
            this.expiresIn = "11";
        }
    }

}
