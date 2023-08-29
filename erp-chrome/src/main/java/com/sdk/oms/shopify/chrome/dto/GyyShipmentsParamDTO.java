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
public class GyyShipmentsParamDTO  implements Serializable {

    private String fieldsText;

    private String fieldsName;


    private GyyShipmentsSearchParamDTO  searchParams;


}
