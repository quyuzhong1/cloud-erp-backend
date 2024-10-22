package com.sdk.oms.shopee.dto.order.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Invoice
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class Invoice implements Serializable {
    @Alias( "number")
    private String number;
    @Alias( "series_number")
    private String seriesNumber;
    @Alias( "access_key")
    private String accessKey;
    @Alias( "issue_date")
    private Long issueDate;
    @Alias( "total_value")
    private Float totalValue;
    @Alias( "products_total_value")
    private Float productsTotalValue;
    @Alias( "tax_code")
    private String taxCode;
}
