package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.CleanBaseDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 平台亚马逊FBA货件DTO
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAmazonFbaShipmentDTO extends CleanBaseDTO {

    private InboundShipmentInfo shipmentInfo;

    private String shopId;

    private String shopName;

    private LocalDateTime platformUpdateTime;

    public PlatformAmazonFbaShipmentDTO(InboundShipmentInfo shipmentInfo, ShopInfoEntity shop) {
        this.shipmentInfo = shipmentInfo;
        this.shopId = shop.getId();
        this.shopName = shop.getName();
        this.platformUpdateTime = LocalDateTime.now(ZoneId.systemDefault());
    }

    /**
     * 拼接配送地址
     */
    public String combineDeliveryFromAddress(){
        Address shipFromAddress = this.shipmentInfo.getShipFromAddress();
        if (null == shipFromAddress){
            return "";
        }
        return String.join(" ",
                shipFromAddress.getPostalCode(),
                shipFromAddress.getCountryCode(),
                shipFromAddress.getStateOrProvinceCode(),
                shipFromAddress.getCity(),
                shipFromAddress.getAddressLine1(),
                shipFromAddress.getName()
        );

    }
}
