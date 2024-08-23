package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 推送旺店通中间表明细DTO
 * @date 2024-07-25
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class DmpPushWdtDetailDTO implements Serializable {

    /**
     * 主表ID
     */
    private String mainId;

    /**
     * sku no
     */
    private String specNo;

    /**
     * 数量
     */
    private BigDecimal num;

    /**
     * 仓位编码
     */
    private String positionNo;
}
