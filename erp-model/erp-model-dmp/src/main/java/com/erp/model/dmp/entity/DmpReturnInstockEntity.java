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
 * 中台退货入库单主表
 * </p>
 *
 * @author shukai
 * @since 2025-04-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_return_instock")
public class DmpReturnInstockEntity extends BaseEntity<DmpReturnInstockEntity> {

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
    * 第三方退货入库id
    */
    @TableField("third_return_instock_id")
    private String thirdReturnInstockId;
    /**
    * 第三方退货入库单据编号
    */
    @TableField("third_return_instock_code")
    private String thirdReturnInstockCode;
    /**
    * 第三方创建时间
    */
    @TableField("third_create_time")
    private LocalDateTime thirdCreateTime;
    /**
    * 第三方更新时间
    */
    @TableField("third_update_time")
    private LocalDateTime thirdUpdateTime;
    /**
    * 退货入库状态
    */
    @TableField("return_instock_status")
    private String returnInstockStatus;
    /**
    * 退货入库时间
    */
    @TableField("return_instock_time")
    private LocalDateTime returnInstockTime;
    /**
    * 物流商代码
    */
    @TableField("logistic_company_code")
    private String logisticCompanyCode;
    /**
    * 物流商名称
    */
    @TableField("logistic_company_name")
    private String logisticCompanyName;
    /**
    * 退货入库物流单号
    */
    @TableField("return_logistic_code")
    private String returnLogisticCode;
    /**
    * 销售组织编码
    */
    @TableField("sales_company_code")
    private String salesCompanyCode;
    /**
    * 收款组织编码
    */
    @TableField("receiving_company_code")
    private String receivingCompanyCode;
    /**
    * 组织名称
    */
    @TableField("organization_name")
    private String organizationName;
    /**
    * 组织编码
    */
    @TableField("organization_code")
    private String organizationCode;
    /**
    * 平台名称
    */
    @TableField("platform_name")
    private String platformName;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 店铺编码
    */
    @TableField("shop_no")
    private String shopNo;
    /**
    * 平台退货入库单号
    */
    @TableField("platform_return_instock_code")
    private String platformReturnInstockCode;
    
    /**
     * 平台退货订单号
     */
     @TableField("platform_order_code")
     private String platformOrderCode;
    
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
     * 平台原始入库单号
     */
    @TableField("third_code")
    private String thirdCode;

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_RETURN_INSTOCK_ID = "third_return_instock_id";

    public static final String THIRD_RETURN_INSTOCK_CODE = "third_return_instock_code";

    public static final String THIRD_CREATE_TIME = "third_create_time";

    public static final String THIRD_UPDATE_TIME = "third_update_time";

    public static final String RETURN_INSTOCK_STATUS = "return_instock_status";

    public static final String RETURN_INSTOCK_TIME = "return_instock_time";

    public static final String LOGISTIC_COMPANY_CODE = "logistic_company_code";

    public static final String LOGISTIC_COMPANY_NAME = "logistic_company_name";

    public static final String RETURN_LOGISTIC_CODE = "return_logistic_code";

    public static final String SALES_COMPANY_CODE = "sales_company_code";

    public static final String RECEIVING_COMPANY_CODE = "receiving_company_code";

    public static final String ORGANIZATION_NAME = "organization_name";

    public static final String ORGANIZATION_CODE = "organization_code";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String SHOP_NAME = "shop_name";

    public static final String SHOP_NO = "shop_no";

    public static final String PLATFORM_RETURN_INSTOCK_CODE = "platform_return_instock_code";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}