package com.erp.model.plm.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 产品包装信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
public class ProductPackDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * 产品尺寸
     */
    private String productSize;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 箱规
     */
    private String boxSize;

    /**
     * 单箱重量
     */
    private BigDecimal boxWeight;

    /**
     * 单箱数量
     */
    private BigDecimal boxQty;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 修改人id
     */
    private String updateUserId;

}