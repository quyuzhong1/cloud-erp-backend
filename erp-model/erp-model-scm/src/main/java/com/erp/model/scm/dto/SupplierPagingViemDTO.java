package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname SupplierPagingViemDTO
 * @Description TODO
 * @Date 2023-03-15 17:43
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPagingViemDTO implements Serializable {

    /**
     * 供应商表id
     */
    private String id;


    /**
     * 名称
     */
    private String name;

    /**
     * 编号
     */
    private String code;


    /**
     * 阶段
     */
    private String phase;


    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分类名
     */
    private String categoryName;


    /**
     * 等级id
     */
    private String gradeId;


    /**
     * 等级名
     */
    private String gradeName;

    /**
     * 启用 状态 true 启用 false 禁用
     */
    private Boolean openStatus;


    /**
     * 结算付款方式
     */
    private String payMethod;

    /**
     * 结算付款币种
     */
    private String payCurrency;

    /**
     * 采购员
     */
    private String purchaseUserName;

    /**
     * 联系人名
     */
    private String contactPerson;


    /**
     * 联系人
     */
    private String contactPhone;


    /**
     * 采购次数
     */
    private Integer purchasesCount;

    /**
     * 退货率
     */
    private BigDecimal rejectRate;

    /**
     * 延期率
     */
    private BigDecimal delayRate;

    /**
     * 次品率
     */
    private BigDecimal defectiveRate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 创建人
     */
    private String createUserName;

}
