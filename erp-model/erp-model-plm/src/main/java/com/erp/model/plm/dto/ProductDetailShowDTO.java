package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
     * 侵权风险
     */
    private Integer pirateRisk;

    /**
     * 标准零售价
     */
    private BigDecimal retailPrice;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private Integer productState;

    /**
     * 创建人
     */
    private String createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    private Integer specType;

    /**
     * 审核状态 0：待审核 1：审核中 2：审核通过 3：审核不通过
     */
    private Integer status;
}
