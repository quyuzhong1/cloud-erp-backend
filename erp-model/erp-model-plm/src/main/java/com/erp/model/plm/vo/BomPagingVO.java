package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname BomPagingVO
 * @Description TODO
 * @Date 2023-01-10 17:19
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomPagingVO implements Serializable {


    /**
     * 表id
     */
    private String id;

    /**
     * bom 编号
     */
    private String serialNumber;

    /**
     * 版本
     */
    private Integer version;

    /**
     * sku 编号
     */
    private String skuNo;

    /**
     * sku 名称
     */
    private String skuName = "";

    /**
     * 数量
     */
    private Integer quantity;


    /**
     * spu 编号
     */
    private String spuNo = "";

    /**
     * spu 名称
     */
    private String spuName = "";

    /**
     * 状态值
     */
    private Integer state;

    /**
     * 状态名
     */
    private String stateName;


    /**
     * 备注
     */
    private String remark;


    /**
     * 创建人 用户id
     */
    private String createUserId;


    /**
     * 创建人 名
     */
    private String createUserName = "";

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 类型
     */
    private String type;





}
