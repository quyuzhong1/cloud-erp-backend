package com.erp.model.dmp.kingdee;

import cn.hutool.core.annotation.Alias;
import com.common.business.dto.CleanBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: 汇率拉取实体
 * @date 2023/8/14 14:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KingdeeExchangeRateEntity extends CleanBaseDTO {

    /**
     * 主键id
     */
    @Alias("FRateID")
    private String id;

    /**
     * 汇率类型
     */
    @Alias("FRATETYPEID.FNumber")
    private String FRateTypeNumber;

    /**
     * 源币种
     */
    @Alias("FCyForID.FNumber")
    private String FCyForIDFNumber;

    /**
     * 目标币种
     */
    @Alias("FCyToID.FNumber")
    private String FCyToIDFNumber;

    /**
     * 直接汇率
     */
    @Alias("FExchangeRate")
    private BigDecimal FExchangeRate;

    /**
     * 间接汇率
     */
    @Alias("FReverseExRate")
    private BigDecimal FReverseExRate;

    /**
     * 生效日期
     */
    @Alias("FBegDate")
    private LocalDate FBegDate;

    /**
     * 失效日期
     */
    @Alias("FEndDate")
    private LocalDate FEndDate;

    /**
     * 审核状态
     */
    @Alias("FDocumentStatus")
    private String FDocumentStatus;

    /**
     * 审核日期
     */
    @Alias("FAuditDate")
    private LocalDate FAuditDate;

    /**
     * 禁用日期
     */
    @Alias("FForbidDate")
    private LocalDate FForbidDate;


}
