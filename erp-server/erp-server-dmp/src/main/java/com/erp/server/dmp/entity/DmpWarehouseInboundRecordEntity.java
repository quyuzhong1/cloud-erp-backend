package com.erp.server.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.erp.model.dmp.dto.GoodcangDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 海外仓上架记录表
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_warehouse_inbound_record")
public class DmpWarehouseInboundRecordEntity extends BaseEntity<DmpWarehouseInboundRecordEntity> {

    /**
     * 入库单号
     */
    @TableField("receiving_code")
    private String receivingCode;

    /**
     * 参考号
     */
    @TableField("reference_no")
    private String referenceNo;

    /**
     * 状态
     */
    @TableField("receiving_status")
    private String receivingStatus;

    /**
     * 仓库编码
     */
    @TableField("warehouse_code")
    private String warehouseCode;

    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private Integer warehouseId;

    /**
     * 平台创建时间
     */
    @TableField("platform_add_time")
    private LocalDateTime platformAddTime;

    /**
     * 更新时间
     */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;

    /**
     * 入库单类型
     */
    @TableField("receiving_type")
    private String receivingType;

    /**
     * 平台类型
     */
    @TableField("platform_sign")
    private String platformSign;


    public static final String RECEIVING_CODE = "receiving_code";

    public static final String REFERENCE_NO = "reference_no";

    public static final String RECEIVING_STATUS = "receiving_status";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String PLATFORM_ADD_TIME = "platform_add_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String RECEIVING_TYPE = "receiving_type";

    public static final String PLATFORM_SIGN = "platform_sign";

    public DmpWarehouseInboundRecordEntity(GoodcangDTO.MessageDTO ext) {
        this.receivingCode = ext.getReceivingCode();
        this.referenceNo = ext.getReferenceNo();
        this.receivingStatus = ext.getReceivingStatus().toString();
        this.receivingType = ext.getReceivingType().toString();
        this.warehouseCode = ext.getWarehouseCode();
        this.warehouseId = ext.getWarehouseId();
        this.platformAddTime = ext.getAddTime();
        this.platformUpdateTime = ext.getUpdateTime();
        this.platformSign = "谷仓";
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
