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
public class GyySearchParamBackupDTO implements Serializable {

      private Integer queryType=9999;
      private String code="";
      private String shopIds="";
      private String drpIds="";
      private String mailNo="";
      private String platformCode="";
      //一年的开始
      private String createBeginDate="2021-01-01 00:00:00";
      //一年的结束
      private String createEndDate="";
      private String deliveryBeginDate="";
      private String deliveryEndDate="";
      private String wmsBeginOutDate="";
      private String wmsEndOutDate="";
      private String arrivalTimeBegin="";
      private String arrivalTimeEnd="";
      private String receiverAddress="";
      private String arrivalType="";
      private String warehouseIds="";
      private String expressId="";
      private String businessManName="";
      private String vipName="";
      private String platformItemName="";
      private String typeIds="";
      private String categoryIds="";
      private String itemCode="";
      private String itemSkuCode="";
      private String itemBrandId="";
      private String receiverName="";
      private String receiverMobile="";
      private String provinces="";
      private String sellerMemo="";
      private String delivery="";
      private String wms="";
      private String printDeliveryList="";
      private Boolean cancel=false;
      private String storeName="";
      private String shipperIds="";

}
