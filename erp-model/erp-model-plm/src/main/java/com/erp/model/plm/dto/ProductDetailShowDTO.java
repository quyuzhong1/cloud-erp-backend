package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* @Description 产品明细查询列表返回值（VO）
* @Author Luo_WG
* @Date 2022/9/22 10:40
**/
@Data
@NoArgsConstructor
public class ProductDetailShowDTO implements Serializable {
    /**
     * 产品信息表id
     */
    private String id;

    /**
     * 产品sku信息表id
     */
    private String skuId;

    /**
     * imagesUrl
     */
    private String imagesUrl;

    /**
     * spuNo
     */
    private String spuNo;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 首批到货量
     */
    private String actualArrivalQty;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    private Integer arrivalState;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    private String arrivalStateName;

    /**
     * 侵权风险
     */
    private Integer pirateRisk;

    /**
     *侵权风险名
     */
    private String pirateRiskName;

    /**
     * 标准零售价
     */
    private BigDecimal retailPrice;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private Integer productState;

    private String productStateName;

    /**
     * 创建人
     */
    private String createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    private Integer specType;

    /**
     * 审核状态 0：待审核 1：审核中 2：审核通过 3：审核不通过
     */
    private Integer status;
    /**
     * 审核状态名称
     */
    private String statusName;

    /**
     * 是否可销售(0否，1是)
     */
    private Integer isMarketable;


    private String isMarketableName;

    /**
     * 流程id
     */
    private String processId;

    private Boolean isChangeIng;

    /**
     * 产品属性
     */
    private String property;


    /**
     * 产品英文名称
     */
    private String nameEn;


    /**
     * 产品品牌
     */
    private String brandName;

    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品负责人
     * product_info
     */
    private String productChargeName;

    /**
     * 项目负责人
     */
    private String projectChargeName;

    /**
     * 首批入库时间
     * product_detail
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime firstMassProductDate;

    /**
     * 预计计划上市时间
     * product_detail
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planListingTime;

    /**
     * 试产数量
     * product_purchase
     */
    private Integer trialProductionQty;

    /**
     * EAN码
     * product_purchase
     */
    private String ean;

    /**
     * 交货周期
     * product_purchase
     */
    private Integer deliveryCycle;

    /**
     * 一级供应商
     * product_purchase
     */
    private String mainSupplier;

    /**
     * 年度销量
     * product_sale
     */
    private Long yearSaleQty;

    /**
     * 年度销售额
     * product_sale
     */
    private BigDecimal yearSaleAmount;


    /**
     * 产品分类
     */
    private String category;


    /**
     * 销售方式
     * product_info
     */
    private String saleMethod;

    /**
     * 销售渠道
     * product_info
     */
    private String salesChannel;


    /**
     * 销售状态
     * product_sale
     */
    private Integer saleState;

    /**
     * 销售状态名
     * product_sale
     */
    private String saleStateName;

    /**
     * 上市时间
     * product_sale
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime listingTime;

    /**
     * 退市时间
     * product_sale
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime delistingTime;

    /**
     * 箱单数量
     */
    private Integer boxQty;

    /**
     * 最小起订量
     */
    private Integer moq;

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关中文名
     */
    private String declareChineseName;
}
