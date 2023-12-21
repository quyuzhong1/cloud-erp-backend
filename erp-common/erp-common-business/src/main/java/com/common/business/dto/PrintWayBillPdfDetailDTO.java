package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 填充html生成pfd面单
 */
@Data
public class PrintWayBillPdfDetailDTO {
    /**
     * 图片
     */
    private String skuImagesUrl;
    /**
     * skuNo
     */
    private String skuNo;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 仓位
     */
    private String warehouseLocation;
    /**
     * 数量
     */
    private Integer qty;
    /**
     * 多属性
     */
    private String variantProperty;
}
