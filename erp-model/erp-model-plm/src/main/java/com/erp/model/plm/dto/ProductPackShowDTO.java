package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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