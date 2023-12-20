package com.common.business.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 填充html生成pfd面单
 */
@Data
public class PrintWayBillPdfDTO {
    /**
     * 打印时间
     */
    private LocalDateTime printTime;
    /**
     * 店铺名称
     */
    private List<String> shopName;


}
