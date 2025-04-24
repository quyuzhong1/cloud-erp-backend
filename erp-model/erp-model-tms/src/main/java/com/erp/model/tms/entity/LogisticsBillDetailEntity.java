package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 物流单明细表
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_bill_detail")
public class LogisticsBillDetailEntity extends BaseEntity<LogisticsBillDetailEntity> {

    /**
    * 物流单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 运输状态
    */
    @TableField("track_status")
    private String trackStatus;
    /**
    * 跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 查询方式
    */
    @TableField("track_query_mode")
    private String trackQueryMode;
    /**
     * 上次查询轨迹时间
     */
    @TableField("track_time")
    private LocalDateTime trackTime;


    /**
     * 签收时间
     */
    @TableField(value = "sign_time" ,fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime signTime;
    /**
     * 是否需要进行物流轨迹查询
     */
    @TableField("track_enable")
    private Boolean trackEnable;

    /**
     * 物流商授权id
     */
    @TableField("logistics_auth_id")
    private String logisticsAuthId;

    /**
     * 注册状态（0未注册1注册成功-1注册失败）
     */
    @TableField("register_status")
    private Integer registerStatus;

    /**
     * 注册结果
     */
    @TableField("register_result")
    private String registerResult;

    /**
     * 平台订单号（track123平台方生成）
     */
    @TableField("platform_order_no")
    private String platformOrderNo;


    /**
     * 运输状态是否是api 更新 true 是  false 不是
     */
    @TableField("is_api_update")
    private Boolean isApiUpdate;
    /**
     * 最新物流轨迹记录
     */
    @TableField("track_content")
    private String trackContent;

    /**
     * 平台订单号
     */
    @TableField(exist = false)
    private String platformCode;

    public static final String MAIN_ID = "main_id";

    public static final String TRACK_STATUS = "track_status";

    public static final String TRACK_NO = "track_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}