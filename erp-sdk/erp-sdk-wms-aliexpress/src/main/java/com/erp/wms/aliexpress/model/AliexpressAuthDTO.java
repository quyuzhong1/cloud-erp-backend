package com.erp.wms.aliexpress.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AliexpressAuthDTO {

    private String url;

    private String appKey;

    private String appSecret;

    private String shopId;

    private String ownerCode;

    private String accessToken;
}
