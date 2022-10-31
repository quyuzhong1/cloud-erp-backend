package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname GyyShipmentsSearchParamDTO
 * @Description TODO
 * @Date 2022-08-29 11:05
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class GyySearchParamDTO implements Serializable {

      private Integer queryType=9999;
      //一年的开始
      private String createBeginDate="2021-01-01 00:00:00";
      //一年的结束
      private String createEndDate="";
      private String deliveryBeginDate="";
      private String deliveryEndDate="";
      private String delivery="";
      private String wms="";
      private String printDeliveryList="";
      private Boolean cancel=false;


}
