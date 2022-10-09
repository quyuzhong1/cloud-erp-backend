package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ProductArchiveEntity
 * @Description TODO
 * @Date 2022-10-09 11:38
 * @Created by yl
 */
@TableName(value ="product_archive")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductArchiveEntity implements Serializable {

    /**
     *表id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 归档时间
     */
    private Date archiveTime;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 操作人
     */
    private String operator;
}
