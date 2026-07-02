package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
     * sku编号
     */
    private String skuNo;

    /**
     * 产品尺寸长
     */
    private BigDecimal productLength;
    /**
     * 产品尺寸宽
     */
    private BigDecimal productWidth;
    /**
     * 产品尺寸高
     */
    private BigDecimal productHeight;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 箱规长
     */
    private BigDecimal boxLength;
    /**
     * 箱规宽
     */
    private BigDecimal boxWidth;
    /**
     * 箱规高
     */
    private BigDecimal boxHeight;

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

    /**
     * 按 skuId 批量解析单品/组合品单位毛重查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListSingleBySkuIdsParam implements Serializable {
        /**
         * 待解析单位毛重的 skuId 列表
         */
        private List<String> skuIds;
        /**
         * BOM 审核状态，用于组合品子件查询（如已归档 BomStateEnum.AUDIT_PASS = 4）
         */
        private Integer state;
    }

}