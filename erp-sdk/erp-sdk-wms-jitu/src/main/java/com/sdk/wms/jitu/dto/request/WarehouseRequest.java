package com.sdk.wms.jitu.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName WarehouseRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class WarehouseRequest implements Serializable {
    //客户标识
    private String customerid;
}
