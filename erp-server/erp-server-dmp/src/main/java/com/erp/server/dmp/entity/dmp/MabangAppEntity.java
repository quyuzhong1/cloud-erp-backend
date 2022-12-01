package com.erp.server.dmp.entity.dmp;

import lombok.Data;

@Data
public class MabangAppEntity {
    private String appKey;
    private String secretKey;

    public MabangAppEntity() {
        this.appKey = "200780";
        this.secretKey = "13c324fa18feaaeb0ebcc8a7746ebfca";
    }

    public MabangAppEntity(String appKey, String secretKey) {
        this.appKey = appKey;
        this.secretKey = secretKey;
    }
}
