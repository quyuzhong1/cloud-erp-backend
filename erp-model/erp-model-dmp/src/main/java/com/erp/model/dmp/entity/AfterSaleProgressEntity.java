package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 售后进度记录表
 * </p>
 *
 * @author jack
 * @since 2025-04-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("after_sale_progress")
public class AfterSaleProgressEntity extends BaseEntity<AfterSaleProgressEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 进度节点
    */
    @TableField("node")
    private String node;
    /**
    * 触发节点的时间
    */
    @TableField("node_time")
    private LocalDateTime nodeTime;
    /**
    * 快递单号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 排序
    */
    @TableField("index")
    private Integer index;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String NODE = "node";

    public static final String NODE_TIME = "node_time";

    public static final String TRACK_NO = "track_no";

    public static final String INDEX = "index";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}