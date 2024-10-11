package com.sdk.oms.shopee.dto.logistics.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Integrated
 * @description: TODO
 * @date 2024年10月10日
 * @version: 1.0
 */
@Data
@Builder
public class Integrated implements Serializable {
    private Integer branchId;
    private String senderRealName;
    private String trackingNumber;
    private String slug;
}
