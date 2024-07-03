package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 中台直接调拨单
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_direct_transfer")
public class DmpDirectTransferEntity extends BaseEntity<DmpDirectTransferEntity> {

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
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem;
    /**
    * 第三方单据编号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 销售平台原始单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 状态 
waitSubmit：待提交 
approveIngt：审核中 
rejectt：审核不通过 
approvet：已审核
    */
    @TableField("status")
    private String status;
    /**
    * 调拨类型
 InnerOrgTransfer：组织内调拨
OverOrgTransfer：跨组织调拨
    */
    @TableField("transfer_type")
    private String transferType;
    /**
    * 调拨方向 
GENERAL：普通 
RETURN：退货
    */
    @TableField("transfer_direct")
    private String transferDirect;
    /**
    * 调出库存组织编码
    */
    @TableField("out_org_code")
    private String outOrgCode;
    /**
    * 调出库存组织名称
    */
    @TableField("out_org_name")
    private String outOrgName;
    /**
    * 调入库存组织编码
    */
    @TableField("in_org_code")
    private String inOrgCode;
    /**
    * 调入库存组织名称
    */
    @TableField("in_org_name")
    private String inOrgName;
    /**
    * 仓管员编码
    */
    @TableField("warehouse_keeper_code")
    private String warehouseKeeperCode;
    /**
    * 仓管员名称
    */
    @TableField("warehouse_keeper_name")
    private String warehouseKeeperName;
    
    /**
     * 销售组织名称
     */
     @TableField("sale_org_name")
     private String saleOrgName;
     
     /**
      * 业务类型
      */
     @TableField("biz_type")
     private String bizType;
     
     /**
      * 调入库存组织id
      */
     @TableField("in_org_id")
     private String inOrgId;
     
     /**
      * 调出库存组织id
      */
     @TableField("out_org_id")
     private String outOrgId;
     
     /**
      * 创建人名称
      */
     @TableField("platform_create_user_name")
     private String platformCreateUserName;
     
     /**
      * 审核人名称
      */
     @TableField("approve_user_name")
     private String approveUserName;
     
     /**
      * 审核日期
      */
     @TableField("approve_time")
     private LocalDateTime approveTime;
     
     /**
      * 作废日期
      */
     @TableField("invalid_time")
     private LocalDateTime invalidTime;
     
     /**
      * 作废名称
      */
     @TableField("invalid_user_name")
     private String invalidUserName;
     
     /**
      * 最后更新时间
      */
     @TableField("last_updated_time")
     private LocalDateTime lastUpdatedTime;
     
     /**
      * 最后更新人
      */
     @TableField("last_updated_user_name")
     private String lastUpdatedUserName;
     
     
    
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
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


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String BILL_DATE = "bill_date";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String STATUS = "status";

    public static final String TRANSFER_TYPE = "transfer_type";

    public static final String TRANSFER_DIRECT = "transfer_direct";

    public static final String OUT_ORG_CODE = "out_org_code";

    public static final String OUT_ORG_NAME = "out_org_name";

    public static final String IN_ORG_CODE = "in_org_code";

    public static final String IN_ORG_NAME = "in_org_name";

    public static final String WAREHOUSE_KEEPER_CODE = "warehouse_keeper_code";

    public static final String WAREHOUSE_KEEPER_NAME = "warehouse_keeper_name";

    public static final String REMARK = "remark";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}