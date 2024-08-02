package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Brand
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class ComplaintPolicy implements Serializable {
    @Alias( "warranty_time")
    private String warrantyTime;
    @Alias( "exclude_entrepreneur_warranty")
    private boolean excludeEntrepreneurWarranty;
    @Alias( "complaint_address_id")
    private Long complaintAddressId;
    @Alias( "additional_information")
    private String additionalInformation;

}
