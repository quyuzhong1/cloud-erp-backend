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
 * 第三方仓退货入库
 * </p>
 *
 * @author Jim
 * @since 2024-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_return_inbound")
public class DmpThirdReturnInboundEntity extends BaseEntity<DmpThirdReturnInboundEntity> {

    /**
     * 仓库平台类型
     */
    @TableField("warehouse_platform_type")
    private String warehousePlatformType;
    /**
     * 来源平台（编码）：goodcang、iml、antu
     */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
     * 平台退货单号
     */
    @TableField("platform_return_order_no")
    private String platformReturnOrderNo;
    /**
     * 平台订单号
     */
    @TableField("platform_order_no")
    private String platformOrderNo;
    /**
     * 订单参考号
     */
    @TableField("order_reference_no")
    private String orderReferenceNo;
    /**
     * 退货状态
     */
    @TableField("status")
    private String status;
    /**
     * 退货类型
     */
    @TableField("return_type")
    private String returnType;
    /**
     * 仓库（第三方）
     */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
     * 异常原因
     */
    @TableField("reason")
    private String reason;
    /**
     * 输入任务id
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 转换id
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 下一层级id
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 唯一字段md5值
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 数据字段md5值
     */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
     * 平台创建时间
     */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
     * 平台修改时间
     */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
     * 平台上架时间
     */
    @TableField("put_away_time")
    private LocalDateTime putAwayTime;
    /**
     * 平台接收时间
     */
    @TableField("received_time")
    private LocalDateTime receivedTime;
    /**
     * 平台授权id
     */
    @TableField("auth_id")
    private String authId;
    /**
     * 来源单据id
     */
    @TableField("source_id")
    private String sourceId = "";


    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String PLATFORM_RETURN_ORDER_NO = "platform_return_order_no";

    public static final String PLATFORM_ORDER_NO = "platform_order_no";

    public static final String ORDER_REFERENCE_NO = "order_reference_no";

    public static final String STATUS = "status";

    public static final String RETURN_TYPE = "return_type";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String REASON = "reason";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}