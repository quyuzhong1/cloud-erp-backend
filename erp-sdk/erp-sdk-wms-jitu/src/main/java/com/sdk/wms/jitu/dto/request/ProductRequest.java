package com.sdk.wms.jitu.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zdy
 * @ClassName WarehouseRequest
 * @description: TODO
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class ProductRequest implements Serializable {
    //客户标识
    private String customerid;
    //商品编码
    private String itemCode;
    //仓库编码
    private String warehouseCode;
    //查询起始页
    private Integer startPage;
    //分页条数
    private Integer pageSize;
    //商品创建开始时间
    private LocalDateTime startdate;
    //商品创建结束时间
    private LocalDateTime enddate;
}
