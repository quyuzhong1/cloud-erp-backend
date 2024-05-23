package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 产品包装信息列表数据（VO）
 * @Author Luo_WG
 * @Date 2022/9/23 15:03
 **/
@Data
@NoArgsConstructor
public class ProductPackShowDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品sku表id
     */
    private String skuId;

    /**
     * 产品sku图片
     */
    private String imagesUrl;

    /**
     * skuNo
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     *禁止修改的字段
     */
    private List<String> disableFieldList;

}