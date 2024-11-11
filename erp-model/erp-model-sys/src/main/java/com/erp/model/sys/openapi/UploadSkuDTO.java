package com.erp.model.sys.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadSkuDTO implements Serializable {

    /**
     * ean
     */
    private String ean;
    /**
     * 文件地址url
     */
    private String attachUrl;

    /**
     * 文件名
     */
    private String attachName;
}
