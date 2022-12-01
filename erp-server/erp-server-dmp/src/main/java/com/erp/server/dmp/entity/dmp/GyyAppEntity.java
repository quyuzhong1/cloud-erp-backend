package com.erp.server.dmp.entity.dmp;

import lombok.Data;

@Data
public class GyyAppEntity {
    private String appKey;
    private String secretKey;
    private String sessionKey;

    public GyyAppEntity() {
        this.appKey = "135174";
        this.secretKey = "7e10a52a116149d38a760c9bf4dd3cbc";
        this.sessionKey = "7daa147a1cea4ae189b57d95bc2a66ce";
    }

}
