package com.sdk.oms.shopify.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname GyyShipmentsSearchParamDTO

 * @Date 2022-08-29 11:05
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class GyyShipmentsSearchParamDTO implements Serializable {

      private Integer queryType=9999;

      private String beginTime;

      private String endTime;

      private Integer itemType=-1;

      private String cod="";


}
