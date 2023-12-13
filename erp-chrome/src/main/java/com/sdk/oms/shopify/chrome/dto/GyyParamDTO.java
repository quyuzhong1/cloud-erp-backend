package com.sdk.oms.shopify.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname GyyShipmentsParamDTO

 * @Date 2022-08-29 11:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class GyyParamDTO implements Serializable {

    private String fieldsText;

    private String fieldsName;


    private Integer secureExportDataType=0;


    private GyySearchParamDTO  searchParams;


}
