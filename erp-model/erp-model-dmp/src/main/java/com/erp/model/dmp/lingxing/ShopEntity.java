package com.erp.model.dmp.lingxing;

import com.common.business.dto.CleanBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ShopEntity extends CleanBaseDTO {


    private Integer shopId;


    private Integer merchantId;


    private String shopName;


    private String sellerId;


    private String accountName;


    private Integer sellerAccountId;


    private String region;


    private String country;


    private Integer hasAdsSetting;


    private String marketplaceId;


    private Integer status;

    @Override
    public String toString() {
        return "ShopEntity{" +
                "shopId=" + shopId +
                ", merchantId=" + merchantId +
                ", shopName='" + shopName + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", accountName='" + accountName + '\'' +
                ", sellerAccountId=" + sellerAccountId +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", hasAdsSetting=" + hasAdsSetting +
                ", marketplaceId='" + marketplaceId + '\'' +
                ", status=" + status +
                '}';
    }
}
