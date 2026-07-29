package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 预入库单主表
 * </p>
 *
 * @author auto
 * @since 2026-06-30
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return_prestock")
public class SoReturnPrestockEntity extends BaseEntity<SoReturnPrestockEntity> {

    public static final String CODE = "code";
    public static final String TYPE = "type";
    public static final String RETURN_LOGISTIC_CODE = "return_logistic_code";
    public static final String CLAIM_STATUS = "claim_status";
    public static final String SOURCE_TYPE = "source_type";
    public static final String THIRD_CODE = "third_code";
    public static final String INVENTORY_ORG_ID = "inventory_org_id";
    public static final String INVENTORY_ORG_NAME = "inventory_org_name";
    public static final String WAREHOUSE_ID = "warehouse_id";
    public static final String WAREHOUSE_NAME = "warehouse_name";
    public static final String DICT_RETURN_TYPE = "dict_return_type";
    public static final String RECEIVED_TIME = "received_time";
    public static final String OPERATE_TIME = "operate_time";
    /**
     * 预入库单编号；格式：YRK + yyMMdd + 5位流水
     */
    @TableField("code")
    private String code;

    // ===================== 常量字段（用于 Wrapper 构建，避免硬编码字符串） =====================
    /**
     * 售后单据类型：B2B / B2C（复用 BillTypeEnum，表存字符串）
     */
    @TableField("type")
    private String type;
    /**
     * 物流单号（值来源于退货入库单的 return_logistic_code）
     */
    @TableField("return_logistic_code")
    private String returnLogisticCode;
    /**
     * 关联状态：UNLINKED=未关联，LINKED=已关联，PARTIAL=部分关联
     */
    @TableField("claim_status")
    private String claimStatus;
    /**
     * 来源类型：MANUAL=手动创建，OVERSEAS_WH=海外仓拉取
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 第三方单据编号（对应退货入库单的 third_code）
     */
    @TableField("third_code")
    private String thirdCode;
    /**
     * 库存组织 ID
     */
    @TableField("inventory_org_id")
    private String inventoryOrgId;
    /**
     * 库存组织名称
     */
    @TableField("inventory_org_name")
    private String inventoryOrgName;
    /**
     * 签收仓库 ID
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
     * 签收仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
     * 退货类型字典值（dict_basic type=ReturnType）
     */
    @TableField("dict_return_type")
    private String dictReturnType;
    /**
     * 实际收货时间/签收时间；创建预入库单时写入，不可修改
     */
    @TableField("received_time")
    private LocalDateTime receivedTime;
    /**
     * 操作时间；每次执行关联/解关联等业务操作时更新，首次值等于 received_time
     */
    @TableField("operate_time")
    private LocalDateTime operateTime;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
