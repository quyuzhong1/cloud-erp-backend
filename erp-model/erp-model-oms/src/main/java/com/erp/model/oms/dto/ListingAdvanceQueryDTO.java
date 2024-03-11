package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ListingAdvanceQueryDTO {

    /**
     * erp SKUId
     */
    private String skuId;
    /**
     * erp shopId
     */
    private String shopId;
    /**
     * erp platform
     */
    private String platform;
}